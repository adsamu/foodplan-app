"""
FoodPlan Optimizer — Firebase Cloud Function (Python)

Reads a user's meal planning data from Firestore, solves a weekly meal
assignment problem using OR-Tools CP-SAT, and writes the resulting meal
plan back to Firestore.
"""

from __future__ import annotations

import datetime
import uuid
from dataclasses import dataclass, field
from typing import Any

import firebase_admin
from firebase_admin import firestore as admin_firestore
from firebase_functions import https_fn
from ortools.sat.python import cp_model

# ---------------------------------------------------------------------------
# Initialise Firebase Admin (idempotent — safe to call multiple times)
# ---------------------------------------------------------------------------
if not firebase_admin._apps:  # noqa: SLF001
    firebase_admin.initialize_app()


# ---------------------------------------------------------------------------
# Data classes
# ---------------------------------------------------------------------------

@dataclass
class RecipeNutrition:
    kcal: float = 0.0
    protein: float = 0.0
    fat: float = 0.0
    carbs: float = 0.0

    def __mul__(self, factor: float) -> "RecipeNutrition":
        return RecipeNutrition(
            self.kcal * factor,
            self.protein * factor,
            self.fat * factor,
            self.carbs * factor,
        )

    def __add__(self, other: "RecipeNutrition") -> "RecipeNutrition":
        return RecipeNutrition(
            self.kcal + other.kcal,
            self.protein + other.protein,
            self.fat + other.fat,
            self.carbs + other.carbs,
        )


@dataclass
class Goals:
    kcal_target: float
    protein_target: float
    fat_target: float
    carbs_target: float
    min_kcal: float | None = None
    max_kcal: float | None = None
    min_protein: float | None = None
    max_protein: float | None = None
    min_fat: float | None = None
    max_fat: float | None = None
    min_carbs: float | None = None
    max_carbs: float | None = None


@dataclass
class DayMealConfig:
    breakfast: bool
    lunch: bool
    dinner: bool
    snack_count: int  # 0=none, -1=unlimited, 1/2/3=fixed

    @property
    def is_active(self) -> bool:
        return self.breakfast or self.lunch or self.dinner or self.snack_count != 0


@dataclass
class BatchGroup:
    meal: str          # e.g. "LUNCH"
    days: list[int]    # ISO day numbers 1=Mon..7=Sun
    batch_number: int


@dataclass
class VarietyPerCategory:
    max_times_per_week: int | None
    max_consecutive_days: int | None


@dataclass
class VarietyConfig:
    level: str          # FLEXIBLE | BALANCED | STRICT
    lunch_dinner_shared_recency: bool
    breakfast_snack_shared_recency: bool
    protein_source_variety: bool
    per_category: dict[str, VarietyPerCategory]


@dataclass
class ProteinPowder:
    ingredient_id: str
    name: str
    protein_per_100g: float
    kcal_per_100g: float
    auto_fill_gap: bool


@dataclass
class CustomRule:
    id: str
    type: str        # INGREDIENT | DIET_CATEGORY
    target: str
    constraint: str  # MIN_PER_WEEK | MAX_PER_WEEK
    value: int


@dataclass
class Settings:
    meal_slots: dict[str, DayMealConfig]   # key = DAY_NAME e.g. "MONDAY"
    batch_groups: list[BatchGroup]
    goals: Goals
    variety: VarietyConfig
    protein_powder: ProteinPowder | None
    diet_excluded_ingredient_ids: list[str]
    excluded_recipe_ids: set[str]          # from ratings.isExcluded / stars==1
    rules: list[CustomRule]


@dataclass
class RecipeRating:
    recipe_id: str
    stars: int | None
    is_excluded: bool
    is_pinned: bool
    last_scheduled_date: datetime.date | None
    times_scheduled: int
    times_manually_removed: int


@dataclass
class RecipeIngredient:
    ingredient_id: str | None
    sub_recipe_id: str | None
    grams: float | None
    portions: float | None


@dataclass
class Recipe:
    id: str
    name: str
    type: str   # MEAL | COMPONENT
    meal_categories: list[str]
    component_category: str | None
    ingredients: list[RecipeIngredient]


# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

DAY_NAMES = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]
# ISO weekday 1=Mon..7=Sun → 0-based index
ISO_TO_IDX = {1: 0, 2: 1, 3: 2, 4: 3, 5: 4, 6: 5, 7: 6}
IDX_TO_ISO = {v: k for k, v in ISO_TO_IDX.items()}

RECENCY_WINDOW = {"FLEXIBLE": 14, "BALANCED": 28, "STRICT": 42}
RECENCY_WEEKS  = {"FLEXIBLE": 2,  "BALANCED": 4,  "STRICT": 6}
PENALTY_WEIGHT = {"FLEXIBLE": 0.2, "BALANCED": 0.6, "STRICT": 1.5}

NULL_SNACK = "null_snack"
POWDER_SCALE = 10     # integer units of 0.1g
POWDER_MAX_G = 100    # hard max per day

MEAL_TYPES = ["BREAKFAST", "LUNCH", "DINNER", "SNACK"]


# ---------------------------------------------------------------------------
# Firestore helpers
# ---------------------------------------------------------------------------

def _ts_to_date(value: Any) -> datetime.date | None:
    """Convert a Firestore timestamp, datetime, or ISO string to date."""
    if value is None:
        return None
    if hasattr(value, "date"):          # datetime / DatetimeWithNanoseconds
        return value.date()
    if isinstance(value, str):
        try:
            return datetime.date.fromisoformat(value[:10])
        except ValueError:
            return None
    return None


def _parse_recipe_ingredient(raw: dict) -> RecipeIngredient:
    return RecipeIngredient(
        ingredient_id=raw.get("ingredientId"),
        sub_recipe_id=raw.get("subRecipeId"),
        grams=raw.get("grams"),
        portions=raw.get("portions"),
    )


def _parse_recipe(doc_id: str, data: dict) -> Recipe:
    return Recipe(
        id=doc_id,
        name=data.get("name", ""),
        type=data.get("type", "MEAL"),
        meal_categories=list(data.get("mealCategories", [])),
        component_category=data.get("componentCategory"),
        ingredients=[_parse_recipe_ingredient(i) for i in data.get("ingredients", [])],
    )


def _parse_rating(doc_id: str, data: dict) -> RecipeRating:
    return RecipeRating(
        recipe_id=doc_id,
        stars=data.get("stars"),
        is_excluded=bool(data.get("isExcluded", False)),
        is_pinned=bool(data.get("isPinned", False)),
        last_scheduled_date=_ts_to_date(data.get("lastScheduledDate")),
        times_scheduled=int(data.get("timesScheduled", 0)),
        times_manually_removed=int(data.get("timesManuallyRemoved", 0)),
    )


def _parse_variety_per_category(raw: dict | None) -> VarietyPerCategory:
    if not raw:
        return VarietyPerCategory(None, None)
    return VarietyPerCategory(
        max_times_per_week=raw.get("maxTimesPerWeek"),
        max_consecutive_days=raw.get("maxConsecutiveDays"),
    )


def _parse_settings(data: dict, ratings: dict[str, RecipeRating]) -> Settings:
    schedule = data.get("schedule", {})
    raw_slots = schedule.get("mealSlots", {})
    meal_slots: dict[str, DayMealConfig] = {}
    for day, cfg in raw_slots.items():
        meal_slots[day] = DayMealConfig(
            breakfast=bool(cfg.get("breakfast", False)),
            lunch=bool(cfg.get("lunch", False)),
            dinner=bool(cfg.get("dinner", False)),
            snack_count=int(cfg.get("snackCount", 0)),
        )

    batch_groups = [
        BatchGroup(
            meal=bg["meal"],
            days=list(bg["days"]),
            batch_number=int(bg["batchNumber"]),
        )
        for bg in schedule.get("batchGroups", [])
    ]

    raw_goals = data.get("goals", {})
    kcal = float(raw_goals.get("kcalTarget", 2000))
    protein = raw_goals.get("proteinTarget")
    fat     = raw_goals.get("fatTarget")
    carbs   = raw_goals.get("carbsTarget")
    auto_field = raw_goals.get("autoField", "FAT")
    # Derive the auto field
    protein_kcal = (float(protein) * 4) if protein is not None else None
    fat_kcal     = (float(fat) * 9)     if fat     is not None else None
    carbs_kcal   = (float(carbs) * 4)   if carbs   is not None else None

    if auto_field == "PROTEIN":
        others = (fat_kcal or 0) + (carbs_kcal or 0)
        protein = (kcal - others) / 4
    elif auto_field == "FAT":
        others = (protein_kcal or 0) + (carbs_kcal or 0)
        fat = (kcal - others) / 9
    else:  # CARBS
        others = (protein_kcal or 0) + (fat_kcal or 0)
        carbs = (kcal - others) / 4

    goals = Goals(
        kcal_target=kcal,
        protein_target=float(protein),
        fat_target=float(fat),
        carbs_target=float(carbs),
        min_kcal=raw_goals.get("minKcalPerDay"),
        max_kcal=raw_goals.get("maxKcalPerDay"),
        min_protein=raw_goals.get("minProteinPerDay"),
        max_protein=raw_goals.get("maxProteinPerDay"),
        min_fat=raw_goals.get("minFatPerDay"),
        max_fat=raw_goals.get("maxFatPerDay"),
        min_carbs=raw_goals.get("minCarbsPerDay"),
        max_carbs=raw_goals.get("maxCarbsPerDay"),
    )

    raw_var = data.get("variety", {})
    per_cat_raw = raw_var.get("perCategory", {})
    variety = VarietyConfig(
        level=raw_var.get("level", "BALANCED"),
        lunch_dinner_shared_recency=bool(raw_var.get("lunchDinnerSharedRecency", False)),
        breakfast_snack_shared_recency=bool(raw_var.get("breakfastSnackSharedRecency", False)),
        protein_source_variety=bool(raw_var.get("proteinSourceVariety", False)),
        per_category={k: _parse_variety_per_category(v) for k, v in per_cat_raw.items()},
    )

    raw_pp = data.get("proteinPowder")
    protein_powder = None
    if raw_pp:
        protein_powder = ProteinPowder(
            ingredient_id=raw_pp.get("ingredientId", ""),
            name=raw_pp.get("name", ""),
            protein_per_100g=float(raw_pp.get("proteinPer100g", 0)),
            kcal_per_100g=float(raw_pp.get("kcalPer100g", 0)),
            auto_fill_gap=bool(raw_pp.get("autoFillGap", False)),
        )

    raw_diet = data.get("diet", {})
    excluded_ingredient_ids = list(raw_diet.get("excludedIngredientIds", []))

    # Build excluded recipe set from ratings
    excluded_recipe_ids = {
        r.recipe_id for r in ratings.values()
        if r.is_excluded or r.stars == 1
    }

    rules = [
        CustomRule(
            id=r.get("id", ""),
            type=r.get("type", "INGREDIENT"),
            target=r.get("target", ""),
            constraint=r.get("constraint", ""),
            value=int(r.get("value", 0)),
        )
        for r in data.get("rules", [])
    ]

    return Settings(
        meal_slots=meal_slots,
        batch_groups=batch_groups,
        goals=goals,
        variety=variety,
        protein_powder=protein_powder,
        diet_excluded_ingredient_ids=excluded_ingredient_ids,
        excluded_recipe_ids=excluded_recipe_ids,
        rules=rules,
    )


# ---------------------------------------------------------------------------
# Firestore read helpers
# ---------------------------------------------------------------------------

SETTINGS_DOC_ID = "main"


def _read_settings_doc(db: Any, user_id: str) -> dict:
    """Read the user's settings as a single document at users/{userId}/settings/main.

    Falls back to streaming the settings subcollection for backward compatibility
    with older data that stored settings as the first document in the collection.
    """
    ref = db.collection("users").document(user_id).collection("settings").document(SETTINGS_DOC_ID)
    snap = ref.get()
    if snap.exists:
        return snap.to_dict() or {}
    for doc in db.collection("users").document(user_id).collection("settings").stream():
        return doc.to_dict() or {}
    return {}


def _read_firestore_data(
    db: Any,
    user_id: str,
    variety_level: str,
) -> tuple[dict[str, dict], dict[str, dict], dict[str, RecipeRating], dict, list[dict]]:
    """Read all required Firestore data and return raw dicts."""
    # Ingredients
    ingredients: dict[str, dict] = {
        doc.id: doc.to_dict() for doc in db.collection("ingredients").stream()
    }
    # Recipes
    recipes_raw: dict[str, dict] = {
        doc.id: doc.to_dict() for doc in db.collection("recipes").stream()
    }
    # Ratings (document ID = recipeId)
    ratings: dict[str, RecipeRating] = {
        doc.id: _parse_rating(doc.id, doc.to_dict())
        for doc in db.collection("users").document(user_id).collection("ratings").stream()
    }
    # Settings — single document at users/{userId}/settings/main
    settings_data: dict = _read_settings_doc(db, user_id)

    # Recent meal plans (for recency scoring)
    recency_weeks = RECENCY_WEEKS[variety_level]
    recent_plans_query = (
        db.collection("users").document(user_id).collection("mealPlans")
        .order_by("startDate", direction="DESCENDING")
        .limit(recency_weeks)
    )
    recent_plans = [doc.to_dict() for doc in recent_plans_query.stream() if doc.to_dict()]

    return ingredients, recipes_raw, ratings, settings_data, recent_plans


# ---------------------------------------------------------------------------
# Nutrition pre-computation
# ---------------------------------------------------------------------------

def compute_recipe_nutrition(
    recipes: dict[str, Recipe],
    ingredients: dict[str, dict],
) -> dict[str, RecipeNutrition]:
    """
    Iteratively resolve recipe nutrition, handling sub-recipe references.
    Circular references and unresolvable recipes are skipped.
    """
    resolved: dict[str, RecipeNutrition] = {}
    remaining = dict(recipes)

    while True:
        progress = False
        for rid, recipe in list(remaining.items()):
            # Check all sub-recipe references are resolved
            sub_ids = [
                ing.sub_recipe_id
                for ing in recipe.ingredients
                if ing.sub_recipe_id
            ]
            if any(sid not in resolved for sid in sub_ids):
                continue  # not ready yet

            nutrition = RecipeNutrition()
            for ing in recipe.ingredients:
                if ing.ingredient_id:
                    raw_ing = ingredients.get(ing.ingredient_id, {})
                    g = (ing.grams or 0) / 100.0
                    nutrition = nutrition + RecipeNutrition(
                        kcal=g * float(raw_ing.get("kcalPer100g", 0)),
                        protein=g * float(raw_ing.get("proteinPer100g", 0)),
                        fat=g * float(raw_ing.get("fatPer100g", 0)),
                        carbs=g * float(raw_ing.get("carbsPer100g", 0)),
                    )
                elif ing.sub_recipe_id and ing.sub_recipe_id in resolved:
                    portions = ing.portions or 1.0
                    nutrition = nutrition + (resolved[ing.sub_recipe_id] * portions)

            resolved[rid] = nutrition
            del remaining[rid]
            progress = True

        if not progress:
            break  # no further resolution possible (circular refs remain)

    return resolved


# ---------------------------------------------------------------------------
# Recipe filtering
# ---------------------------------------------------------------------------

def _get_ingredient_ids_for_recipe(recipe: Recipe) -> set[str]:
    """Return all direct ingredientIds referenced by a recipe."""
    return {ing.ingredient_id for ing in recipe.ingredients if ing.ingredient_id}


def _dominant_protein_source(
    recipe: Recipe,
    ingredients_raw: dict[str, dict],
) -> str | None:
    """Return the ingredientId contributing the most protein, or None if no protein."""
    best_id: str | None = None
    best_protein = 0.0
    for ing in recipe.ingredients:
        if not ing.ingredient_id:
            continue
        raw = ingredients_raw.get(ing.ingredient_id, {})
        protein_per_100g = float(raw.get("proteinPer100g", 0))
        grams = float(ing.grams or 0)
        protein = grams * protein_per_100g / 100.0
        if protein > best_protein:
            best_protein = protein
            best_id = ing.ingredient_id
    return best_id


def filter_recipes(
    recipes: dict[str, Recipe],
    settings: Settings,
    ratings: dict[str, RecipeRating],
    nutrition: dict[str, RecipeNutrition],
) -> list[Recipe]:
    """Return only MEAL recipes eligible for the optimizer pool."""
    # Which meal types are active this week?
    active_meal_types: set[str] = set()
    for day_name, cfg in settings.meal_slots.items():
        if not cfg.is_active:
            continue
        if cfg.breakfast:
            active_meal_types.add("BREAKFAST")
        if cfg.lunch:
            active_meal_types.add("LUNCH")
        if cfg.dinner:
            active_meal_types.add("DINNER")
        if cfg.snack_count != 0:
            active_meal_types.add("SNACK")

    variety_level = settings.variety.level
    window_days = RECENCY_WINDOW[variety_level]
    today = datetime.date.today()

    eligible = []
    for recipe in recipes.values():
        # Must be type MEAL
        if recipe.type != "MEAL":
            continue
        # Must have resolved nutrition
        if recipe.id not in nutrition:
            continue
        # Excluded by rating
        if recipe.id in settings.excluded_recipe_ids:
            continue
        # Must have at least one active meal category
        if not set(recipe.meal_categories) & active_meal_types:
            continue
        # Contains excluded ingredients
        recipe_ingredient_ids = _get_ingredient_ids_for_recipe(recipe)
        if recipe_ingredient_ids & set(settings.diet_excluded_ingredient_ids):
            continue
        # STRICT mode: hard pre-filter on recency
        if variety_level == "STRICT":
            rating = ratings.get(recipe.id)
            if rating and rating.last_scheduled_date:
                days_ago = (today - rating.last_scheduled_date).days
                if days_ago < window_days:
                    continue

        eligible.append(recipe)

    return eligible


# ---------------------------------------------------------------------------
# Recency scoring
# ---------------------------------------------------------------------------

def _recency_group(meal_type: str, variety: VarietyConfig) -> str:
    """Return the recency group key for a given meal type."""
    if variety.lunch_dinner_shared_recency and meal_type in ("LUNCH", "DINNER"):
        return "LUNCH_DINNER"
    if variety.breakfast_snack_shared_recency and meal_type in ("BREAKFAST", "SNACK"):
        return "BREAKFAST_SNACK"
    return meal_type


def build_history_index(
    recent_plans: list[dict],
    variety: VarietyConfig,
) -> dict[tuple[str, str], datetime.date]:
    """
    Build a map of (recency_group, recipe_id) -> most recent scheduled date
    from historical meal plans.
    """
    index: dict[tuple[str, str], datetime.date] = {}
    for plan in recent_plans:
        for day_data in plan.get("days", []):
            day_date = _ts_to_date(day_data.get("date"))
            if not day_date:
                continue
            for meal in day_data.get("meals", []):
                meal_type = meal.get("type", "")
                recipe_id = meal.get("recipeId", "")
                if meal_type not in MEAL_TYPES or not recipe_id:
                    continue
                group = _recency_group(meal_type, variety)
                key = (group, recipe_id)
                if key not in index or day_date > index[key]:
                    index[key] = day_date
    return index


def compute_recency_penalty(
    recipe_id: str,
    meal_type: str,
    variety: VarietyConfig,
    history_index: dict[tuple[str, str], datetime.date],
    within_week_index: dict[tuple[str, str], datetime.date],
    reference_date: datetime.date,
) -> float:
    """Return penalty in [0.0, 1.0] — 1.0 means used today, 0.0 means beyond window."""
    window_days = RECENCY_WINDOW[variety.level]
    group = _recency_group(meal_type, variety)
    key = (group, recipe_id)

    last_used: datetime.date | None = None
    if key in history_index:
        last_used = history_index[key]
    if key in within_week_index:
        ww = within_week_index[key]
        if last_used is None or ww > last_used:
            last_used = ww

    if last_used is None:
        return 0.0

    days_ago = (reference_date - last_used).days
    return max(0.0, 1.0 - days_ago / window_days)


# ---------------------------------------------------------------------------
# CP-SAT Model
# ---------------------------------------------------------------------------

def _int_coeff(value: float, scale: int = 1000) -> int:
    return int(round(value * scale))


def solve(
    eligible_recipes: list[Recipe],
    settings: Settings,
    nutrition: dict[str, RecipeNutrition],
    ratings: dict[str, RecipeRating],
    history_index: dict[tuple[str, str], datetime.date],
    start_date: datetime.date,
    ingredients_raw: dict[str, dict] | None = None,
) -> dict:
    """
    Build and solve the CP-SAT model. Returns a dict matching the Firestore
    mealPlans schema or raises an exception if INFEASIBLE.
    """
    model = cp_model.CpModel()
    variety = settings.variety
    goals = settings.goals

    # -----------------------------------------------------------------------
    # Day / schedule setup
    # -----------------------------------------------------------------------
    # Map day index (0=Mon) to DayMealConfig
    day_configs: list[tuple[int, str, DayMealConfig]] = []
    for idx, day_name in enumerate(DAY_NAMES):
        cfg = settings.meal_slots.get(day_name)
        if cfg and cfg.is_active:
            day_configs.append((idx, day_name, cfg))

    active_day_indices = [idx for idx, _, _ in day_configs]
    n_active = len(active_day_indices)

    if n_active == 0:
        raise ValueError("No active days in schedule")

    # Which days belong to each batch group (by 0-based index)
    # batch_group_day_sets[g] = set of 0-based day indices
    batch_slots: dict[int, set[str]] = {}  # day_idx -> set of meal_types that are batched
    for bg in settings.batch_groups:
        for iso_day in bg.days:
            di = ISO_TO_IDX.get(iso_day)
            if di is not None:
                batch_slots.setdefault(di, set()).add(bg.meal)

    # -----------------------------------------------------------------------
    # Recipe pools per meal type
    # -----------------------------------------------------------------------
    pool: dict[str, list[Recipe]] = {mt: [] for mt in MEAL_TYPES}
    for recipe in eligible_recipes:
        for mt in recipe.meal_categories:
            if mt in pool:
                pool[mt].append(recipe)

    snack_pool_size = len(pool["SNACK"])

    # -----------------------------------------------------------------------
    # Decision variables: x[day_idx][meal_type][recipe_id] in {0,1}
    # -----------------------------------------------------------------------
    x: dict[tuple[int, str, str], cp_model.IntVar] = {}

    for di, day_name, cfg in day_configs:
        active_meals = []
        if cfg.breakfast:
            active_meals.append("BREAKFAST")
        if cfg.lunch:
            active_meals.append("LUNCH")
        if cfg.dinner:
            active_meals.append("DINNER")

        for mt in active_meals:
            # Only create vars if not a batch slot (batch slots get y vars)
            is_batch = mt in batch_slots.get(di, set())
            if not is_batch:
                for recipe in pool[mt]:
                    var = model.NewBoolVar(f"x_{di}_{mt}_{recipe.id}")
                    x[(di, mt, recipe.id)] = var

        # Snack variables
        if cfg.snack_count != 0:
            sc = cfg.snack_count
            # Required slots
            required = 0 if sc == -1 else sc
            # Optional slots (up to pool size)
            total_slots = snack_pool_size if sc == -1 else max(sc, snack_pool_size)

            for slot_i in range(total_slots):
                is_required = slot_i < required
                # One var per recipe for this slot
                for recipe in pool["SNACK"]:
                    var = model.NewBoolVar(f"x_{di}_SNACK_{slot_i}_{recipe.id}")
                    x[(di, f"SNACK_{slot_i}", recipe.id)] = var
                # NULL_SNACK sentinel for optional slots
                if not is_required:
                    var = model.NewBoolVar(f"x_{di}_SNACK_{slot_i}_{NULL_SNACK}")
                    x[(di, f"SNACK_{slot_i}", NULL_SNACK)] = var

    # -----------------------------------------------------------------------
    # Decision variables: y[batch_group_index][recipe_id] in {0,1}
    # -----------------------------------------------------------------------
    y: dict[tuple[int, str], cp_model.IntVar] = {}
    for gi, bg in enumerate(settings.batch_groups):
        for recipe in pool[bg.meal]:
            var = model.NewBoolVar(f"y_{gi}_{recipe.id}")
            y[(gi, recipe.id)] = var

    # -----------------------------------------------------------------------
    # H1 — One recipe per required non-batch slot
    # -----------------------------------------------------------------------
    for di, day_name, cfg in day_configs:
        active_meals = []
        if cfg.breakfast:
            active_meals.append("BREAKFAST")
        if cfg.lunch:
            active_meals.append("LUNCH")
        if cfg.dinner:
            active_meals.append("DINNER")

        for mt in active_meals:
            is_batch = mt in batch_slots.get(di, set())
            if not is_batch:
                slot_vars = [x[(di, mt, r.id)] for r in pool[mt] if (di, mt, r.id) in x]
                if not slot_vars:
                    # No eligible recipes for a required slot → immediately infeasible
                    raise ValueError(f"INFEASIBLE: No eligible {mt} recipes for {day_name}")
                model.Add(sum(slot_vars) == 1)

        # Snack constraints
        if cfg.snack_count != 0:
            sc = cfg.snack_count
            required = 0 if sc == -1 else sc
            total_slots = snack_pool_size if sc == -1 else max(sc, snack_pool_size)

            for slot_i in range(total_slots):
                is_required = slot_i < required
                slot_key = f"SNACK_{slot_i}"
                slot_vars = [x[(di, slot_key, r.id)] for r in pool["SNACK"] if (di, slot_key, r.id) in x]
                if is_required:
                    if slot_vars:
                        model.Add(sum(slot_vars) == 1)
                else:
                    null_var = x.get((di, slot_key, NULL_SNACK))
                    if null_var is not None:
                        all_vars = slot_vars + [null_var]
                        model.Add(sum(all_vars) == 1)

            # Each recipe used at most once per day across all snack slots
            for recipe in pool["SNACK"]:
                recipe_snack_vars = [
                    x[(di, f"SNACK_{slot_i}", recipe.id)]
                    for slot_i in range(total_slots)
                    if (di, f"SNACK_{slot_i}", recipe.id) in x
                ]
                if recipe_snack_vars:
                    model.Add(sum(recipe_snack_vars) <= 1)

    # -----------------------------------------------------------------------
    # H2 — Batch group consistency
    # -----------------------------------------------------------------------
    for gi, bg in enumerate(settings.batch_groups):
        # sum(y[gi][r]) == 1
        y_vars = [y[(gi, r.id)] for r in pool[bg.meal] if (gi, r.id) in y]
        if y_vars:
            model.Add(sum(y_vars) == 1)

        # x[d][meal][r] == y[gi][r] for each day in the group
        for iso_day in bg.days:
            di = ISO_TO_IDX.get(iso_day)
            if di is None:
                continue
            for recipe in pool[bg.meal]:
                x_var_key = (di, bg.meal, recipe.id)
                # Create x var for this batch day/meal/recipe if not exists
                if x_var_key not in x:
                    var = model.NewBoolVar(f"x_{di}_{bg.meal}_{recipe.id}")
                    x[x_var_key] = var
                if (gi, recipe.id) in y:
                    model.Add(x[x_var_key] == y[(gi, recipe.id)])
            # Exactly one recipe for this batch slot
            slot_vars = [x.get((di, bg.meal, r.id)) for r in pool[bg.meal]]
            slot_vars = [v for v in slot_vars if v is not None]
            if slot_vars:
                model.Add(sum(slot_vars) == 1)

    # Different batch groups for same meal type get different recipes
    groups_by_meal: dict[str, list[int]] = {}
    for gi, bg in enumerate(settings.batch_groups):
        groups_by_meal.setdefault(bg.meal, []).append(gi)
    for meal_type, group_indices in groups_by_meal.items():
        for i in range(len(group_indices)):
            for j in range(i + 1, len(group_indices)):
                gi1, gi2 = group_indices[i], group_indices[j]
                for recipe in pool[meal_type]:
                    v1 = y.get((gi1, recipe.id))
                    v2 = y.get((gi2, recipe.id))
                    if v1 is not None and v2 is not None:
                        model.Add(v1 + v2 <= 1)

    # -----------------------------------------------------------------------
    # Protein powder variables (integer, scaled by POWDER_SCALE)
    # -----------------------------------------------------------------------
    use_powder = (
        settings.protein_powder is not None
        and settings.protein_powder.auto_fill_gap
    )
    powder_vars: dict[int, cp_model.IntVar] = {}
    for di, _, _ in day_configs:
        if use_powder:
            var = model.NewIntVar(0, POWDER_MAX_G * POWDER_SCALE, f"powder_{di}")
        else:
            var = model.NewConstant(0)
        powder_vars[di] = var

    pp = settings.protein_powder

    # -----------------------------------------------------------------------
    # Helper: all x vars for a day (meal assignments)
    # -----------------------------------------------------------------------
    def _day_meal_vars(di: int) -> list[tuple[cp_model.IntVar, RecipeNutrition]]:
        """Return list of (var, nutrition) for all non-NULL meal assignments on day di."""
        result = []
        cfg = next((c for d, _, c in day_configs if d == di), None)
        if cfg is None:
            return result
        # Regular meals
        for mt in ("BREAKFAST", "LUNCH", "DINNER"):
            for r in pool[mt]:
                key = (di, mt, r.id)
                if key in x:
                    result.append((x[key], nutrition.get(r.id, RecipeNutrition())))
        # Batch meals
        for mt in batch_slots.get(di, set()):
            for r in pool[mt]:
                key = (di, mt, r.id)
                if key in x:
                    result.append((x[key], nutrition.get(r.id, RecipeNutrition())))
        # Snacks
        if cfg.snack_count != 0:
            sc = cfg.snack_count
            total_slots = snack_pool_size if sc == -1 else max(sc, snack_pool_size)
            for slot_i in range(total_slots):
                for r in pool["SNACK"]:
                    key = (di, f"SNACK_{slot_i}", r.id)
                    if key in x:
                        result.append((x[key], nutrition.get(r.id, RecipeNutrition())))
        return result

    # -----------------------------------------------------------------------
    # H4 — Per-day hard bounds
    # -----------------------------------------------------------------------
    # Use SCALE=10 for integer arithmetic (1 unit = 0.1 kcal / 0.1g macro).
    # powder_vars[di] is in units of 0.1g (scaled by POWDER_SCALE=10).
    # Coefficient for powder kcal at SCALE units: kcalPer100g / (100 * POWDER_SCALE) * SCALE
    #   = kcalPer100g / (100 * 10) * 10 = kcalPer100g / 100
    SCALE = 10
    for di, _, _ in day_configs:
        day_vars = _day_meal_vars(di)
        if not day_vars:
            continue

        for bound, field, is_lower in [
            (goals.min_kcal,    "kcal",    True),
            (goals.max_kcal,    "kcal",    False),
            (goals.min_protein, "protein", True),
            (goals.max_protein, "protein", False),
            (goals.min_fat,     "fat",     True),
            (goals.max_fat,     "fat",     False),
            (goals.min_carbs,   "carbs",   True),
            (goals.max_carbs,   "carbs",   False),
        ]:
            if bound is None:
                continue

            terms = [_int_coeff(getattr(nut, field), SCALE) * var for var, nut in day_vars]

            # Add powder contribution (powder_vars in 0.1g units)
            # kcal per 0.1g = kcalPer100g / 100 / 10 → in SCALE units: kcalPer100g / 100
            if use_powder and pp is not None:
                if field == "kcal":
                    pw_coeff = _int_coeff(pp.kcal_per_100g / 100, SCALE)
                    if pw_coeff:
                        terms.append(pw_coeff * powder_vars[di])
                elif field == "protein":
                    pw_coeff = _int_coeff(pp.protein_per_100g / 100, SCALE)
                    if pw_coeff:
                        terms.append(pw_coeff * powder_vars[di])

            expr = sum(terms)
            bound_int = _int_coeff(float(bound), SCALE)

            if is_lower:
                model.Add(expr >= bound_int)
            else:
                model.Add(expr <= bound_int)

    # -----------------------------------------------------------------------
    # H5 — maxTimesPerWeek per category (non-batch slots)
    # -----------------------------------------------------------------------
    for mt, per_cat in variety.per_category.items():
        if per_cat.max_times_per_week is None:
            continue
        limit = per_cat.max_times_per_week
        # Determine which recipes are in this pool
        for recipe in pool.get(mt, []):
            appearances = []
            for di, _, cfg in day_configs:
                is_batch = mt in batch_slots.get(di, set())
                if is_batch:
                    continue
                if mt == "SNACK":
                    sc = cfg.snack_count
                    total_slots = snack_pool_size if sc == -1 else max(sc, snack_pool_size)
                    for slot_i in range(total_slots):
                        key = (di, f"SNACK_{slot_i}", recipe.id)
                        if key in x:
                            appearances.append(x[key])
                else:
                    key = (di, mt, recipe.id)
                    if key in x:
                        appearances.append(x[key])
            if appearances:
                model.Add(sum(appearances) <= limit)

    # -----------------------------------------------------------------------
    # H6 — maxConsecutiveDays per category (non-batch slots)
    # -----------------------------------------------------------------------
    for mt, per_cat in variety.per_category.items():
        if per_cat.max_consecutive_days is None:
            continue
        max_consec = per_cat.max_consecutive_days
        # Build list of active days for this meal type (non-batch)
        mt_active_days: list[int] = []
        for di, _, cfg in day_configs:
            is_batch = mt in batch_slots.get(di, set())
            if is_batch:
                continue
            has_mt = (
                (mt == "BREAKFAST" and cfg.breakfast)
                or (mt == "LUNCH" and cfg.lunch)
                or (mt == "DINNER" and cfg.dinner)
                or (mt == "SNACK" and cfg.snack_count != 0)
            )
            if has_mt:
                mt_active_days.append(di)

        window_size = max_consec + 1
        for start_i in range(len(mt_active_days) - max_consec):
            window_days_idx = mt_active_days[start_i:start_i + window_size]
            for recipe in pool.get(mt, []):
                window_vars = []
                for di in window_days_idx:
                    if mt == "SNACK":
                        cfg = next((c for d, _, c in day_configs if d == di), None)
                        if cfg is None:
                            continue
                        sc = cfg.snack_count
                        total_slots = snack_pool_size if sc == -1 else max(sc, snack_pool_size)
                        for slot_i in range(total_slots):
                            key = (di, f"SNACK_{slot_i}", recipe.id)
                            if key in x:
                                window_vars.append(x[key])
                    else:
                        key = (di, mt, recipe.id)
                        if key in x:
                            window_vars.append(x[key])
                if window_vars:
                    model.Add(sum(window_vars) <= max_consec)

    # -----------------------------------------------------------------------
    # Objective function
    # -----------------------------------------------------------------------
    OBJ_SCALE = 1000

    # Weekly targets
    weekly_kcal_target    = goals.kcal_target    * n_active
    weekly_protein_target = goals.protein_target * n_active
    weekly_fat_target     = goals.fat_target     * n_active
    weekly_carbs_target   = goals.carbs_target   * n_active

    # Weekly totals as linear expressions
    def _weekly_expr(field: str) -> Any:
        terms = []
        for di, _, _ in day_configs:
            for var, nut in _day_meal_vars(di):
                coeff = _int_coeff(getattr(nut, field), OBJ_SCALE)
                if coeff != 0:
                    terms.append(coeff * var)
        return sum(terms) if terms else 0

    # Deviation variables (over/under)
    def _make_deviation_vars(field: str, target: float):
        weekly_target_int = _int_coeff(target, OBJ_SCALE)
        weekly_expr = _weekly_expr(field)
        # Add powder contribution to kcal and protein
        if field == "kcal" and use_powder and pp is not None:
            for di, _, _ in day_configs:
                pv = powder_vars[di]
                coeff = _int_coeff(pp.kcal_per_100g / (100 * POWDER_SCALE), OBJ_SCALE)
                if coeff != 0:
                    weekly_expr = weekly_expr + coeff * pv
        elif field == "protein" and use_powder and pp is not None:
            for di, _, _ in day_configs:
                pv = powder_vars[di]
                coeff = _int_coeff(pp.protein_per_100g / (100 * POWDER_SCALE), OBJ_SCALE)
                if coeff != 0:
                    weekly_expr = weekly_expr + coeff * pv

        big_m = _int_coeff(target * 3, OBJ_SCALE)
        over  = model.NewIntVar(0, big_m, f"over_{field}")
        under = model.NewIntVar(0, big_m, f"under_{field}")
        model.Add(weekly_expr - weekly_target_int == over - under)
        return over, under

    kcal_over,    kcal_under    = _make_deviation_vars("kcal",    weekly_kcal_target)
    protein_over, protein_under = _make_deviation_vars("protein", weekly_protein_target)
    fat_over,     fat_under     = _make_deviation_vars("fat",     weekly_fat_target)
    carbs_over,   carbs_under   = _make_deviation_vars("carbs",   weekly_carbs_target)

    # Macro deviation terms (normalised, scaled by OBJ_SCALE).
    # The weight for each term is:  weight_i * OBJ_SCALE / weekly_target_i
    # All arithmetic on scalar Python ints first; only then multiply by CP-SAT vars.
    macro_terms = []
    if weekly_kcal_target > 0:
        coeff = max(1, round(3 * OBJ_SCALE * OBJ_SCALE / _int_coeff(weekly_kcal_target, OBJ_SCALE)))
        macro_terms.append(coeff * (kcal_over + kcal_under))
    if weekly_protein_target > 0:
        coeff = max(1, round(2 * OBJ_SCALE * OBJ_SCALE / _int_coeff(weekly_protein_target, OBJ_SCALE)))
        macro_terms.append(coeff * (protein_over + protein_under))
    if weekly_fat_target > 0:
        coeff = max(1, round(1 * OBJ_SCALE * OBJ_SCALE / _int_coeff(weekly_fat_target, OBJ_SCALE)))
        macro_terms.append(coeff * (fat_over + fat_under))
    if weekly_carbs_target > 0:
        coeff = max(1, round(1 * OBJ_SCALE * OBJ_SCALE / _int_coeff(weekly_carbs_target, OBJ_SCALE)))
        macro_terms.append(coeff * (carbs_over + carbs_under))

    # Recency penalty terms
    penalty_weight = PENALTY_WEIGHT[variety.level]
    within_week_index: dict[tuple[str, str], datetime.date] = {}
    recency_terms = []

    for di, day_name, cfg in day_configs:
        ref_date = start_date + datetime.timedelta(days=di)
        slot_updates: list[tuple[str, str]] = []

        for mt in ("BREAKFAST", "LUNCH", "DINNER"):
            for recipe in pool[mt]:
                key = (di, mt, recipe.id)
                if key not in x:
                    continue
                pen = compute_recency_penalty(
                    recipe.id, mt, variety, history_index, within_week_index, ref_date
                )
                if pen > 0:
                    coeff = _int_coeff(penalty_weight * pen, OBJ_SCALE)
                    recency_terms.append(coeff * x[key])
                slot_updates.append((_recency_group(mt, variety), recipe.id))

        if cfg.snack_count != 0:
            sc = cfg.snack_count
            total_slots = snack_pool_size if sc == -1 else max(sc, snack_pool_size)
            for slot_i in range(total_slots):
                for recipe in pool["SNACK"]:
                    key = (di, f"SNACK_{slot_i}", recipe.id)
                    if key not in x:
                        continue
                    pen = compute_recency_penalty(
                        recipe.id, "SNACK", variety, history_index, within_week_index, ref_date
                    )
                    if pen > 0:
                        coeff = _int_coeff(penalty_weight * pen, OBJ_SCALE)
                        recency_terms.append(coeff * x[key])
                    slot_updates.append((_recency_group("SNACK", variety), recipe.id))

        # Update within-week index after scoring this slot so later slots in the
        # same week treat these candidates as "used today" when computing penalties.
        for group_key in slot_updates:
            existing = within_week_index.get(group_key)
            if existing is None or ref_date > existing:
                within_week_index[group_key] = ref_date

    # Batch group recency penalties
    for gi, bg in enumerate(settings.batch_groups):
        # Representative date: first day of the batch
        if not bg.days:
            continue
        first_iso = min(bg.days)
        di = ISO_TO_IDX.get(first_iso, 0)
        ref_date = start_date + datetime.timedelta(days=di)
        for recipe in pool[bg.meal]:
            if (gi, recipe.id) not in y:
                continue
            pen = compute_recency_penalty(
                recipe.id, bg.meal, variety, history_index, within_week_index, ref_date
            )
            if pen > 0:
                coeff = _int_coeff(penalty_weight * pen, OBJ_SCALE)
                recency_terms.append(coeff * y[(gi, recipe.id)])

    # Powder minimisation term
    total_powder = sum(powder_vars[di] for di, _, _ in day_configs)
    powder_term_scale = _int_coeff(0.5 / (POWDER_MAX_G * POWDER_SCALE * n_active), OBJ_SCALE)
    powder_term = powder_term_scale * total_powder

    # Custom rule soft penalties
    rule_terms = []
    for rule in settings.rules:
        if rule.type != "INGREDIENT":
            continue
        # Find all recipes containing the target ingredient
        target_recipes = [
            r for r in eligible_recipes
            if rule.target in _get_ingredient_ids_for_recipe(r)
        ]
        if not target_recipes:
            continue
        # Count total appearances across all meal slots
        appearance_vars = []
        for recipe in target_recipes:
            for di, _, cfg in day_configs:
                for mt in MEAL_TYPES:
                    key = (di, mt, recipe.id)
                    if key in x:
                        appearance_vars.append(x[key])
                    # Snack slots
                    if mt == "SNACK" and cfg.snack_count != 0:
                        sc = cfg.snack_count
                        total_slots = snack_pool_size if sc == -1 else max(sc, snack_pool_size)
                        for slot_i in range(total_slots):
                            key = (di, f"SNACK_{slot_i}", recipe.id)
                            if key in x:
                                appearance_vars.append(x[key])
        if not appearance_vars:
            continue
        count_var = model.NewIntVar(0, len(appearance_vars), f"rule_{rule.id}_count")
        model.Add(count_var == sum(appearance_vars))

        if rule.constraint == "MIN_PER_WEEK":
            shortfall = model.NewIntVar(0, rule.value, f"rule_{rule.id}_short")
            model.AddMaxEquality(shortfall, [rule.value - count_var, model.NewConstant(0)])
            rule_terms.append(_int_coeff(1.0, OBJ_SCALE) * shortfall)
        elif rule.constraint == "MAX_PER_WEEK":
            excess = model.NewIntVar(0, len(appearance_vars), f"rule_{rule.id}_excess")
            model.AddMaxEquality(excess, [count_var - rule.value, model.NewConstant(0)])
            rule_terms.append(_int_coeff(1.0, OBJ_SCALE) * excess)

    # Protein source variety — soft penalty for repeating the dominant protein
    # source across lunch/dinner slots in the same week.
    protein_variety_terms = []
    if variety.protein_source_variety and ingredients_raw:
        # Group eligible LUNCH/DINNER recipes by dominant protein ingredient
        source_to_recipes: dict[str, list[Recipe]] = {}
        for recipe in eligible_recipes:
            if not (set(recipe.meal_categories) & {"LUNCH", "DINNER"}):
                continue
            src = _dominant_protein_source(recipe, ingredients_raw)
            if src:
                source_to_recipes.setdefault(src, []).append(recipe)

        for src, recipes_for_src in source_to_recipes.items():
            appearance_vars = []
            for recipe in recipes_for_src:
                for di, _, _ in day_configs:
                    for mt in ("LUNCH", "DINNER"):
                        key = (di, mt, recipe.id)
                        if key in x:
                            appearance_vars.append(x[key])
            if len(appearance_vars) < 2:
                continue
            count_var = model.NewIntVar(0, len(appearance_vars), f"pv_{src}_count")
            model.Add(count_var == sum(appearance_vars))
            excess = model.NewIntVar(0, len(appearance_vars), f"pv_{src}_excess")
            model.AddMaxEquality(excess, [count_var - 1, model.NewConstant(0)])
            protein_variety_terms.append(_int_coeff(penalty_weight, OBJ_SCALE) * excess)

    # Combine objective
    obj_terms = macro_terms + recency_terms + [powder_term] + rule_terms + protein_variety_terms
    model.Minimize(sum(obj_terms) if obj_terms else 0)

    # -----------------------------------------------------------------------
    # Solve
    # -----------------------------------------------------------------------
    solver = cp_model.CpSolver()
    solver.parameters.max_time_in_seconds = 30.0
    solver.parameters.num_search_workers = 4
    status = solver.Solve(model)

    if status not in (cp_model.OPTIMAL, cp_model.FEASIBLE):
        raise ValueError("INFEASIBLE: No valid meal plan could be constructed with the given constraints")

    # -----------------------------------------------------------------------
    # Extract solution
    # -----------------------------------------------------------------------
    plan_id = str(uuid.uuid4())
    days_out = []

    for di, day_name, cfg in day_configs:
        ref_date = start_date + datetime.timedelta(days=di)
        meals_out = []

        # Regular meals
        for mt in ("BREAKFAST", "LUNCH", "DINNER"):
            for recipe in pool[mt]:
                key = (di, mt, recipe.id)
                if key in x and solver.Value(x[key]) == 1:
                    meals_out.append({"type": mt, "recipeId": recipe.id})

        # Snacks
        if cfg.snack_count != 0:
            sc = cfg.snack_count
            total_slots = snack_pool_size if sc == -1 else max(sc, snack_pool_size)
            for slot_i in range(total_slots):
                slot_key = f"SNACK_{slot_i}"
                null_val = solver.Value(x[(di, slot_key, NULL_SNACK)]) if (di, slot_key, NULL_SNACK) in x else 0
                if null_val == 1:
                    continue  # omit null snack from output
                for recipe in pool["SNACK"]:
                    key = (di, slot_key, recipe.id)
                    if key in x and solver.Value(x[key]) == 1:
                        meals_out.append({"type": "SNACK", "recipeId": recipe.id})

        # Compute actual macros for the day
        day_kcal = sum(nutrition[m["recipeId"]].kcal for m in meals_out if m["recipeId"] in nutrition)
        day_protein = sum(nutrition[m["recipeId"]].protein for m in meals_out if m["recipeId"] in nutrition)

        # Protein powder — fill the daily protein gap analytically
        protein_gap = max(0.0, goals.protein_target - day_protein)
        kcal_gap_for_powder = max(0.0, goals.kcal_target - day_kcal)
        powder_grams = 0.0
        if use_powder and pp is not None and protein_gap > 0:
            # How many grams needed to close protein gap?
            grams_for_protein = protein_gap / (pp.protein_per_100g / 100)
            # How many grams permitted by kcal gap?
            if pp.kcal_per_100g > 0:
                grams_for_kcal = kcal_gap_for_powder / (pp.kcal_per_100g / 100)
            else:
                grams_for_kcal = POWDER_MAX_G
            powder_grams = min(grams_for_protein, grams_for_kcal, float(POWDER_MAX_G))
            powder_grams = max(0.0, powder_grams)

        days_out.append({
            "id": str(uuid.uuid4()),
            "date": ref_date.isoformat(),
            "dayOfWeek": day_name,
            "meals": meals_out,
            "proteinPowderGrams": round(powder_grams, 1),
            "kcalTarget": int(round(goals.kcal_target)),
            "proteinTarget": int(round(goals.protein_target)),
        })

    # Build week label
    end_date = start_date + datetime.timedelta(days=6)
    week_num = start_date.isocalendar()[1]
    plan_name = f"Week {week_num} – {start_date.strftime('%-d %b %Y')}"

    return {
        "id": plan_id,
        "name": plan_name,
        "startDate": start_date.isoformat(),
        "endDate": end_date.isoformat(),
        "days": days_out,
    }


# ---------------------------------------------------------------------------
# Cloud Function entry point
# ---------------------------------------------------------------------------

@https_fn.on_call(timeout_sec=60)
def optimise_meal_plan(req: https_fn.CallableRequest) -> dict:
    """
    Firebase callable Cloud Function.
    Expected payload: { "userId": "...", "startDate": "2025-05-19" }
    Returns: { "success": true, "planId": "..." }
    """
    user_id        = req.data.get("userId")
    start_date_str = req.data.get("startDate")

    if not user_id:
        raise https_fn.HttpsError(code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT, message="userId is required")
    if not start_date_str:
        raise https_fn.HttpsError(code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT, message="startDate is required")

    try:
        start_date = datetime.date.fromisoformat(start_date_str)
    except ValueError:
        raise https_fn.HttpsError(code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT, message="startDate must be ISO format YYYY-MM-DD")

    db = admin_firestore.client()

    try:
        # 1. Read settings first to get variety level for the recency window query
        raw_settings_data = _read_settings_doc(db, user_id)
        variety_level = raw_settings_data.get("variety", {}).get("level", "BALANCED")

        ingredients_raw, recipes_raw, ratings, settings_data, recent_plans = _read_firestore_data(
            db, user_id, variety_level
        )
        if not settings_data:
            settings_data = raw_settings_data

        # 2. Parse
        recipes: dict[str, Recipe] = {
            doc_id: _parse_recipe(doc_id, data)
            for doc_id, data in recipes_raw.items()
        }
        settings = _parse_settings(settings_data, ratings)

        # 3. Compute nutrition
        nutrition = compute_recipe_nutrition(recipes, ingredients_raw)

        # 4. Filter
        eligible = filter_recipes(recipes, settings, ratings, nutrition)

        # 5. History index
        history_index = build_history_index(recent_plans, settings.variety)

        # 6. Solve
        plan = solve(
            eligible, settings, nutrition, ratings, history_index, start_date, ingredients_raw
        )

        # 7. Write plan to Firestore
        plan_id = plan["id"]
        plan_ref = (
            db.collection("users")
            .document(user_id)
            .collection("mealPlans")
            .document(plan_id)
        )
        plan_ref.set(plan)

        return {"success": True, "planId": plan_id}

    except ValueError as exc:
        msg = str(exc)
        if "INFEASIBLE" in msg:
            raise https_fn.HttpsError(
                code=https_fn.FunctionsErrorCode.FAILED_PRECONDITION,
                message=msg,
            )
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INTERNAL,
            message=msg,
        )
    except Exception as exc:  # noqa: BLE001
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INTERNAL,
            message=str(exc),
        )
