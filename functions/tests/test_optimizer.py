"""
Unit tests for the FoodPlan CP-SAT optimizer.

Run with:
    cd functions
    pip install -r requirements.txt pytest
    pytest tests/
"""

import datetime
import json
import pathlib
import sys

import pytest

# Ensure the functions package root is on the path
sys.path.insert(0, str(pathlib.Path(__file__).parent.parent))

from main import (  # noqa: E402
    NULL_SNACK,
    BatchGroup,
    CustomRule,
    DayMealConfig,
    Goals,
    ProteinPowder,
    Recipe,
    RecipeIngredient,
    RecipeNutrition,
    RecipeRating,
    Settings,
    VarietyConfig,
    VarietyPerCategory,
    compute_recency_penalty,
    compute_recipe_nutrition,
    filter_recipes,
    solve,
    _parse_recipe,
    _parse_settings,
    _parse_rating,
)

# ---------------------------------------------------------------------------
# Shared test data (mirrors the Quality Test Dataset from the brief)
# ---------------------------------------------------------------------------

INGREDIENTS_RAW = {
    "ing_chicken_breast": {"kcalPer100g": 110, "proteinPer100g": 23.0, "fatPer100g": 1.5,  "carbsPer100g": 0.0},
    "ing_salmon":         {"kcalPer100g": 208, "proteinPer100g": 20.0, "fatPer100g": 13.0, "carbsPer100g": 0.0},
    "ing_tuna_canned":    {"kcalPer100g": 116, "proteinPer100g": 26.0, "fatPer100g": 1.0,  "carbsPer100g": 0.0},
    "ing_ground_beef":    {"kcalPer100g": 215, "proteinPer100g": 17.0, "fatPer100g": 16.0, "carbsPer100g": 0.0},
    "ing_cod":            {"kcalPer100g": 82,  "proteinPer100g": 18.0, "fatPer100g": 0.7,  "carbsPer100g": 0.0},
    "ing_eggs":           {"kcalPer100g": 155, "proteinPer100g": 13.0, "fatPer100g": 11.0, "carbsPer100g": 1.0},
    "ing_greek_yogurt":   {"kcalPer100g": 59,  "proteinPer100g": 10.0, "fatPer100g": 0.4,  "carbsPer100g": 3.6},
    "ing_cottage_cheese": {"kcalPer100g": 98,  "proteinPer100g": 11.0, "fatPer100g": 4.3,  "carbsPer100g": 3.4},
    "ing_rice":           {"kcalPer100g": 360, "proteinPer100g": 6.5,  "fatPer100g": 0.5,  "carbsPer100g": 79.0},
    "ing_pasta":          {"kcalPer100g": 350, "proteinPer100g": 12.0, "fatPer100g": 1.5,  "carbsPer100g": 71.0},
    "ing_oats":           {"kcalPer100g": 389, "proteinPer100g": 17.0, "fatPer100g": 7.0,  "carbsPer100g": 66.0},
    "ing_sweet_potato":   {"kcalPer100g": 86,  "proteinPer100g": 1.6,  "fatPer100g": 0.1,  "carbsPer100g": 20.0},
    "ing_broccoli":       {"kcalPer100g": 34,  "proteinPer100g": 2.8,  "fatPer100g": 0.4,  "carbsPer100g": 6.6},
    "ing_spinach":        {"kcalPer100g": 23,  "proteinPer100g": 2.9,  "fatPer100g": 0.4,  "carbsPer100g": 3.6},
    "ing_tomato":         {"kcalPer100g": 18,  "proteinPer100g": 0.9,  "fatPer100g": 0.2,  "carbsPer100g": 3.9},
    "ing_onion":          {"kcalPer100g": 40,  "proteinPer100g": 1.1,  "fatPer100g": 0.1,  "carbsPer100g": 9.3},
    "ing_olive_oil":      {"kcalPer100g": 884, "proteinPer100g": 0.0,  "fatPer100g": 100.0,"carbsPer100g": 0.0},
    "ing_coconut_milk":   {"kcalPer100g": 197, "proteinPer100g": 2.0,  "fatPer100g": 21.0, "carbsPer100g": 2.8},
    "ing_chickpeas":      {"kcalPer100g": 164, "proteinPer100g": 8.9,  "fatPer100g": 2.6,  "carbsPer100g": 27.0},
    "ing_lentils":        {"kcalPer100g": 353, "proteinPer100g": 26.0, "fatPer100g": 1.1,  "carbsPer100g": 60.0},
    "ing_kidney_beans":   {"kcalPer100g": 127, "proteinPer100g": 8.7,  "fatPer100g": 0.5,  "carbsPer100g": 22.0},
    "ing_bread_whole":    {"kcalPer100g": 247, "proteinPer100g": 9.0,  "fatPer100g": 3.5,  "carbsPer100g": 41.0},
    "ing_banana":         {"kcalPer100g": 89,  "proteinPer100g": 1.1,  "fatPer100g": 0.3,  "carbsPer100g": 23.0},
    "ing_blueberries":    {"kcalPer100g": 57,  "proteinPer100g": 0.7,  "fatPer100g": 0.3,  "carbsPer100g": 14.0},
    "ing_almonds":        {"kcalPer100g": 579, "proteinPer100g": 21.0, "fatPer100g": 50.0, "carbsPer100g": 22.0},
    "ing_protein_powder": {"kcalPer100g": 354, "proteinPer100g": 72.0, "fatPer100g": 4.0,  "carbsPer100g": 12.0},
    "ing_curry_paste":    {"kcalPer100g": 100, "proteinPer100g": 2.0,  "fatPer100g": 4.0,  "carbsPer100g": 14.0},
    "ing_soy_sauce":      {"kcalPer100g": 53,  "proteinPer100g": 8.1,  "fatPer100g": 0.1,  "carbsPer100g": 4.9},
    "ing_feta":           {"kcalPer100g": 264, "proteinPer100g": 14.0, "fatPer100g": 21.0, "carbsPer100g": 4.1},
    "ing_garlic":         {"kcalPer100g": 149, "proteinPer100g": 6.4,  "fatPer100g": 0.5,  "carbsPer100g": 33.0},
}

RECIPES_RAW = {
    "rec_chicken_rice": {
        "id": "rec_chicken_rice", "name": "Chicken & Rice", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_chicken_breast", "grams": 180},
            {"ingredientId": "ing_rice",           "grams": 80},
            {"ingredientId": "ing_broccoli",       "grams": 150},
            {"ingredientId": "ing_olive_oil",      "grams": 10},
        ],
    },
    "rec_salmon_sweet_potato": {
        "id": "rec_salmon_sweet_potato", "name": "Baked Salmon & Sweet Potato", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_salmon",       "grams": 180},
            {"ingredientId": "ing_sweet_potato", "grams": 200},
            {"ingredientId": "ing_spinach",      "grams": 100},
            {"ingredientId": "ing_olive_oil",    "grams": 10},
        ],
    },
    "rec_beef_pasta": {
        "id": "rec_beef_pasta", "name": "Beef Bolognese Pasta", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_ground_beef", "grams": 150},
            {"ingredientId": "ing_pasta",       "grams": 80},
            {"ingredientId": "ing_tomato",      "grams": 200},
            {"ingredientId": "ing_onion",       "grams": 80},
            {"ingredientId": "ing_olive_oil",   "grams": 10},
        ],
    },
    "rec_tuna_rice": {
        "id": "rec_tuna_rice", "name": "Tuna & Rice Bowl", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_tuna_canned", "grams": 160},
            {"ingredientId": "ing_rice",        "grams": 80},
            {"ingredientId": "ing_spinach",     "grams": 100},
            {"ingredientId": "ing_soy_sauce",   "grams": 15},
        ],
    },
    "rec_chicken_curry": {
        "id": "rec_chicken_curry", "name": "Chicken Coconut Curry", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_chicken_breast", "grams": 200},
            {"ingredientId": "ing_coconut_milk",   "grams": 100},
            {"ingredientId": "ing_curry_paste",    "grams": 30},
            {"ingredientId": "ing_rice",           "grams": 80},
            {"ingredientId": "ing_onion",          "grams": 80},
        ],
    },
    "rec_cod_lentils": {
        "id": "rec_cod_lentils", "name": "Baked Cod & Lentils", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_cod",      "grams": 200},
            {"ingredientId": "ing_lentils",  "grams": 80},
            {"ingredientId": "ing_tomato",   "grams": 150},
            {"ingredientId": "ing_spinach",  "grams": 80},
            {"ingredientId": "ing_olive_oil","grams": 10},
        ],
    },
    "rec_beef_sweet_potato": {
        "id": "rec_beef_sweet_potato", "name": "Ground Beef & Sweet Potato", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_ground_beef",  "grams": 150},
            {"ingredientId": "ing_sweet_potato", "grams": 200},
            {"ingredientId": "ing_broccoli",     "grams": 150},
            {"ingredientId": "ing_olive_oil",    "grams": 10},
        ],
    },
    "rec_chickpea_curry": {
        "id": "rec_chickpea_curry", "name": "Chickpea Curry", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_chickpeas",    "grams": 240},
            {"ingredientId": "ing_coconut_milk", "grams": 100},
            {"ingredientId": "ing_curry_paste",  "grams": 30},
            {"ingredientId": "ing_rice",         "grams": 80},
            {"ingredientId": "ing_spinach",      "grams": 100},
        ],
    },
    "rec_salmon_pasta": {
        "id": "rec_salmon_pasta", "name": "Salmon & Pasta", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_salmon",    "grams": 160},
            {"ingredientId": "ing_pasta",     "grams": 80},
            {"ingredientId": "ing_spinach",   "grams": 100},
            {"ingredientId": "ing_olive_oil", "grams": 10},
        ],
    },
    "rec_lentil_soup": {
        "id": "rec_lentil_soup", "name": "Red Lentil Soup", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_lentils",   "grams": 100},
            {"ingredientId": "ing_tomato",    "grams": 200},
            {"ingredientId": "ing_onion",     "grams": 80},
            {"ingredientId": "ing_garlic",    "grams": 10},
            {"ingredientId": "ing_olive_oil", "grams": 10},
        ],
    },
    "rec_tuna_pasta": {
        "id": "rec_tuna_pasta", "name": "Tuna Pasta", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_tuna_canned", "grams": 160},
            {"ingredientId": "ing_pasta",       "grams": 80},
            {"ingredientId": "ing_tomato",      "grams": 150},
            {"ingredientId": "ing_olive_oil",   "grams": 10},
        ],
    },
    "rec_chicken_feta_salad": {
        "id": "rec_chicken_feta_salad", "name": "Chicken & Feta Salad", "type": "MEAL",
        "mealCategories": ["LUNCH"],
        "ingredients": [
            {"ingredientId": "ing_chicken_breast", "grams": 180},
            {"ingredientId": "ing_feta",           "grams": 60},
            {"ingredientId": "ing_spinach",        "grams": 150},
            {"ingredientId": "ing_tomato",         "grams": 150},
            {"ingredientId": "ing_olive_oil",      "grams": 10},
        ],
    },
    "rec_kidney_bean_chili": {
        "id": "rec_kidney_bean_chili", "name": "Kidney Bean Chili", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_kidney_beans", "grams": 240},
            {"ingredientId": "ing_ground_beef",  "grams": 100},
            {"ingredientId": "ing_tomato",       "grams": 200},
            {"ingredientId": "ing_onion",        "grams": 80},
            {"ingredientId": "ing_olive_oil",    "grams": 10},
        ],
    },
    "rec_egg_fried_rice": {
        "id": "rec_egg_fried_rice", "name": "Egg Fried Rice", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_eggs",      "grams": 150},
            {"ingredientId": "ing_rice",      "grams": 100},
            {"ingredientId": "ing_broccoli",  "grams": 150},
            {"ingredientId": "ing_soy_sauce", "grams": 15},
            {"ingredientId": "ing_olive_oil", "grams": 10},
        ],
    },
    "rec_cod_sweet_potato": {
        "id": "rec_cod_sweet_potato", "name": "Cod & Sweet Potato Mash", "type": "MEAL",
        "mealCategories": ["DINNER"],
        "ingredients": [
            {"ingredientId": "ing_cod",          "grams": 200},
            {"ingredientId": "ing_sweet_potato", "grams": 250},
            {"ingredientId": "ing_broccoli",     "grams": 150},
            {"ingredientId": "ing_olive_oil",    "grams": 10},
        ],
    },
    "rec_almonds_yogurt": {
        "id": "rec_almonds_yogurt", "name": "Almonds & Yogurt", "type": "MEAL",
        "mealCategories": ["SNACK"],
        "ingredients": [
            {"ingredientId": "ing_almonds",      "grams": 30},
            {"ingredientId": "ing_greek_yogurt", "grams": 150},
        ],
    },
    "rec_banana_almonds": {
        "id": "rec_banana_almonds", "name": "Banana & Almonds", "type": "MEAL",
        "mealCategories": ["SNACK"],
        "ingredients": [
            {"ingredientId": "ing_banana",  "grams": 120},
            {"ingredientId": "ing_almonds", "grams": 25},
        ],
    },
    "rec_cottage_cheese_berries": {
        "id": "rec_cottage_cheese_berries", "name": "Cottage Cheese & Berries", "type": "MEAL",
        "mealCategories": ["SNACK"],
        "ingredients": [
            {"ingredientId": "ing_cottage_cheese", "grams": 150},
            {"ingredientId": "ing_blueberries",    "grams": 100},
        ],
    },
    "rec_eggs_spinach": {
        "id": "rec_eggs_spinach", "name": "Boiled Eggs & Spinach", "type": "MEAL",
        "mealCategories": ["SNACK"],
        "ingredients": [
            {"ingredientId": "ing_eggs",    "grams": 150},
            {"ingredientId": "ing_spinach", "grams": 80},
        ],
    },
    "comp_curry_sauce": {
        "id": "comp_curry_sauce", "name": "Red Curry Sauce", "type": "COMPONENT",
        "componentCategory": "SAUCE", "mealCategories": [],
        "ingredients": [
            {"ingredientId": "ing_coconut_milk", "grams": 200},
            {"ingredientId": "ing_curry_paste",  "grams": 40},
            {"ingredientId": "ing_garlic",       "grams": 10},
            {"ingredientId": "ing_onion",        "grams": 80},
        ],
    },
    "rec_chicken_curry_component": {
        "id": "rec_chicken_curry_component", "name": "Chicken Curry (with sauce component)", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_chicken_breast", "grams": 200},
            {"subRecipeId":  "comp_curry_sauce",   "portions": 0.5},
            {"ingredientId": "ing_rice",           "grams": 80},
        ],
    },
}

# ---------------------------------------------------------------------------
# Fixture helpers
# ---------------------------------------------------------------------------

FIXTURE_PATH = pathlib.Path(__file__).parent / "fixtures" / "minimal.json"


def _load_fixture() -> dict:
    with open(FIXTURE_PATH) as f:
        return json.load(f)


def _make_recipes() -> dict[str, Recipe]:
    return {rid: _parse_recipe(rid, data) for rid, data in RECIPES_RAW.items()}


def _make_nutrition(recipes: dict) -> dict:
    return compute_recipe_nutrition(recipes, INGREDIENTS_RAW)


def _make_settings(fixture: dict, ratings: dict | None = None) -> Settings:
    if ratings is None:
        ratings = {}
    return _parse_settings(fixture["settings"], ratings)


def _make_eligible(recipes, settings, ratings, nutrition):
    return filter_recipes(recipes, settings, ratings, nutrition)


def _call_optimizer(
    fixture: dict,
    ratings: dict | None = None,
    pass_ingredients: bool = False,
) -> dict:
    if ratings is None:
        ratings = {}
    recipes = _make_recipes()
    nutrition = _make_nutrition(recipes)
    settings = _make_settings(fixture, ratings)
    eligible = _make_eligible(recipes, settings, ratings, nutrition)
    start_date = datetime.date.fromisoformat(fixture["startDate"])
    return solve(
        eligible, settings, nutrition, ratings, {}, start_date,
        INGREDIENTS_RAW if pass_ingredients else None,
    )


# ---------------------------------------------------------------------------
# Fixtures (pytest)
# ---------------------------------------------------------------------------

@pytest.fixture(scope="module")
def fixture_data() -> dict:
    return _load_fixture()


@pytest.fixture(scope="module")
def recipes():
    return _make_recipes()


@pytest.fixture(scope="module")
def nutrition(recipes):
    return _make_nutrition(recipes)


@pytest.fixture(scope="module")
def solved_plan(fixture_data) -> dict:
    return _call_optimizer(fixture_data)


@pytest.fixture(scope="module")
def goals(fixture_data) -> dict:
    return fixture_data["settings"]["goals"]


# ---------------------------------------------------------------------------
# Tests — nutrition pre-computation
# ---------------------------------------------------------------------------

class TestNutritionComputation:
    def test_leaf_recipe_kcal(self, recipes, nutrition):
        # Chicken & Rice: 180g chicken@110 + 80g rice@360 + 150g broccoli@34 + 10g oil@884
        expected_kcal = 180*110/100 + 80*360/100 + 150*34/100 + 10*884/100
        assert abs(nutrition["rec_chicken_rice"].kcal - expected_kcal) < 0.5

    def test_component_recipe_resolved(self, nutrition):
        # comp_curry_sauce must be resolved
        assert "comp_curry_sauce" in nutrition
        assert nutrition["comp_curry_sauce"].kcal > 0

    def test_sub_recipe_composition(self, nutrition):
        # rec_chicken_curry_component uses comp_curry_sauce * 0.5
        comp = nutrition["comp_curry_sauce"]
        # Expected: 200g chicken + 0.5*comp_sauce + 80g rice
        chicken_kcal = 200 * 110 / 100
        rice_kcal    = 80  * 360 / 100
        expected_kcal = chicken_kcal + 0.5 * comp.kcal + rice_kcal
        assert abs(nutrition["rec_chicken_curry_component"].kcal - expected_kcal) < 0.5

    def test_component_excluded_from_meal_pool(self, recipes, nutrition):
        settings = Settings(
            meal_slots={"MONDAY": DayMealConfig(False, True, True, 0)},
            batch_groups=[],
            goals=Goals(1800, 150, 50, 180),
            variety=VarietyConfig("BALANCED", False, False, False, {}),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )
        eligible = filter_recipes(recipes, settings, {}, nutrition)
        ids = [r.id for r in eligible]
        assert "comp_curry_sauce" not in ids


# ---------------------------------------------------------------------------
# Tests — recipe filtering
# ---------------------------------------------------------------------------

class TestRecipeFiltering:
    def test_excluded_by_rating_isExcluded(self, recipes, nutrition, fixture_data):
        ratings = {
            "rec_chicken_rice": _parse_rating("rec_chicken_rice", {"isExcluded": True, "stars": None}),
        }
        settings = _make_settings(fixture_data, ratings)
        eligible = filter_recipes(recipes, settings, ratings, nutrition)
        assert not any(r.id == "rec_chicken_rice" for r in eligible)

    def test_excluded_by_rating_stars_1(self, recipes, nutrition, fixture_data):
        ratings = {
            "rec_beef_pasta": _parse_rating("rec_beef_pasta", {"isExcluded": False, "stars": 1}),
        }
        settings = _make_settings(fixture_data, ratings)
        eligible = filter_recipes(recipes, settings, ratings, nutrition)
        assert not any(r.id == "rec_beef_pasta" for r in eligible)

    def test_excluded_by_ingredient(self, recipes, nutrition, fixture_data):
        data = _load_fixture()
        data["settings"]["diet"]["excludedIngredientIds"] = ["ing_tuna_canned"]
        settings = _make_settings(data)
        eligible = filter_recipes(recipes, settings, {}, nutrition)
        for r in eligible:
            if r.id in ("rec_tuna_rice", "rec_tuna_pasta"):
                pytest.fail(f"Recipe {r.id} should be excluded due to excluded ingredient")

    def test_snack_count_minus1_activates_snack(self, recipes, nutrition):
        """snackCount=-1 must activate SNACK category (unlimited)."""
        settings = Settings(
            meal_slots={"MONDAY": DayMealConfig(False, False, False, -1)},
            batch_groups=[],
            goals=Goals(1800, 150, 50, 180),
            variety=VarietyConfig("BALANCED", False, False, False, {}),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )
        eligible = filter_recipes(recipes, settings, {}, nutrition)
        assert any(r.id == "rec_almonds_yogurt" for r in eligible)

    def test_snack_count_0_deactivates_snack(self, recipes, nutrition):
        """snackCount=0 must not include SNACK-only recipes."""
        settings = Settings(
            meal_slots={"MONDAY": DayMealConfig(False, True, True, 0)},
            batch_groups=[],
            goals=Goals(1800, 150, 50, 180),
            variety=VarietyConfig("BALANCED", False, False, False, {}),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )
        eligible = filter_recipes(recipes, settings, {}, nutrition)
        assert not any(r.id == "rec_almonds_yogurt" for r in eligible)


# ---------------------------------------------------------------------------
# Tests — solver output
# ---------------------------------------------------------------------------

class TestSolverOutput:
    def test_all_required_slots_filled(self, solved_plan):
        """Every active day must have LUNCH and DINNER assigned."""
        for day in solved_plan["days"]:
            types = {m["type"] for m in day["meals"]}
            day_name = day.get("dayOfWeek", "")
            # Sunday only has dinner in the fixture
            if day_name == "SUNDAY":
                assert "DINNER" in types
            else:
                assert "LUNCH" in types, f"LUNCH missing on {day_name}"
                assert "DINNER" in types, f"DINNER missing on {day_name}"

    def test_batch_consistency_mon_fri(self, solved_plan):
        """Monday–Friday lunch must all share the same recipe (batch group 1)."""
        batch_days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"}
        lunch_ids = set()
        for day in solved_plan["days"]:
            if day.get("dayOfWeek") in batch_days:
                for meal in day["meals"]:
                    if meal["type"] == "LUNCH":
                        lunch_ids.add(meal["recipeId"])
        assert len(lunch_ids) == 1, f"Batch Mon-Fri lunches not consistent: {lunch_ids}"

    def test_no_null_snacks_in_output(self, solved_plan):
        """NULL_SNACK sentinel must never appear in output meals list."""
        for day in solved_plan["days"]:
            for meal in day["meals"]:
                assert meal["recipeId"] != NULL_SNACK

    def test_dinner_max_times_per_week(self, solved_plan):
        """No dinner recipe should appear more than maxTimesPerWeek=2 times."""
        from collections import Counter
        dinner_ids = [
            m["recipeId"]
            for d in solved_plan["days"]
            for m in d["meals"]
            if m["type"] == "DINNER"
        ]
        counts = Counter(dinner_ids)
        for recipe_id, count in counts.items():
            assert count <= 2, f"Recipe {recipe_id} appears {count} times in DINNER (limit 2)"

    def test_plan_has_correct_number_of_days(self, solved_plan):
        """Fixture has 7 active days (Mon–Sun all active)."""
        assert len(solved_plan["days"]) == 7

    def test_plan_id_is_uuid(self, solved_plan):
        import re
        uuid_re = re.compile(r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        assert uuid_re.match(solved_plan["id"]), f"Plan ID is not a UUID: {solved_plan['id']}"

    def test_kcal_target_fields_present(self, solved_plan, goals):
        for day in solved_plan["days"]:
            assert "kcalTarget" in day
            assert "proteinTarget" in day
            assert day["kcalTarget"] == goals["kcalTarget"]
            assert day["proteinTarget"] == goals["proteinTarget"]

    def test_protein_powder_grams_non_negative(self, solved_plan):
        for day in solved_plan["days"]:
            assert day["proteinPowderGrams"] >= 0.0

    def test_protein_powder_within_cap(self, solved_plan):
        for day in solved_plan["days"]:
            assert day["proteinPowderGrams"] <= 100.0


# ---------------------------------------------------------------------------
# Tests — macro accuracy
# ---------------------------------------------------------------------------

class TestMacroAccuracy:
    def _compute_actual_macros(self, plan: dict) -> tuple[float, float]:
        """Returns (total_kcal, total_protein) across all days including powder."""
        nutrition = _make_nutrition(_make_recipes())
        total_kcal = total_protein = 0.0
        for day in plan["days"]:
            for meal in day["meals"]:
                nut = nutrition.get(meal["recipeId"], RecipeNutrition())
                total_kcal    += nut.kcal
                total_protein += nut.protein
            powder_g = day.get("proteinPowderGrams", 0)
            total_kcal    += powder_g * 354 / 100
            total_protein += powder_g * 72 / 100
        return total_kcal, total_protein

    def test_weekly_kcal_within_20_pct(self, solved_plan, goals):
        """Weekly kcal should be within 20% of target.

        The fixture has Sunday with only DINNER (no lunch/breakfast/snack), so
        that day structurally contributes ~50% of the daily kcal target. We use
        20% tolerance to account for this single-meal day while still validating
        that the optimizer is broadly targeting the right calorie range.
        """
        total_kcal, _ = self._compute_actual_macros(solved_plan)
        n_days = len(solved_plan["days"])
        target = goals["kcalTarget"] * n_days
        err = abs(total_kcal - target) / target
        assert err < 0.20, f"Weekly kcal error {err:.1%} exceeds 20%"

    def test_weekly_protein_within_10_pct(self, solved_plan, goals):
        _, total_protein = self._compute_actual_macros(solved_plan)
        n_days = len(solved_plan["days"])
        target = goals["proteinTarget"] * n_days
        err = abs(total_protein - target) / target
        assert err < 0.10, f"Weekly protein error {err:.1%} exceeds 10%"


# ---------------------------------------------------------------------------
# Tests — infeasible / edge cases
# ---------------------------------------------------------------------------

class TestEdgeCases:
    def test_infeasible_returns_error_with_empty_pool(self):
        """An empty recipe pool must raise ValueError containing 'INFEASIBLE'."""
        settings = Settings(
            meal_slots={"MONDAY": DayMealConfig(False, True, True, 0)},
            batch_groups=[],
            goals=Goals(1800, 150, 50, 180),
            variety=VarietyConfig("BALANCED", False, False, False, {}),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )
        with pytest.raises(ValueError, match="INFEASIBLE"):
            solve([], settings, {}, {}, {}, datetime.date(2025, 5, 19))

    def test_no_protein_powder_when_auto_fill_false(self, fixture_data):
        data = _load_fixture()
        data["settings"]["proteinPowder"]["autoFillGap"] = False
        plan = _call_optimizer(data)
        for day in plan["days"]:
            assert day["proteinPowderGrams"] == 0.0

    def test_day_meal_config_is_active_unlimited_snack(self):
        cfg = DayMealConfig(breakfast=False, lunch=False, dinner=False, snack_count=-1)
        assert cfg.is_active is True

    def test_day_meal_config_is_active_zero_snack_no_meals(self):
        cfg = DayMealConfig(breakfast=False, lunch=False, dinner=False, snack_count=0)
        assert cfg.is_active is False

    def test_sub_recipe_component_not_in_eligible_pool(self):
        """COMPONENT-type recipes must never appear in the optimizer pool."""
        recipes = _make_recipes()
        nutrition = _make_nutrition(recipes)
        settings = Settings(
            meal_slots={"MONDAY": DayMealConfig(False, True, True, 0)},
            batch_groups=[],
            goals=Goals(1800, 150, 50, 180),
            variety=VarietyConfig("BALANCED", False, False, False, {}),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )
        eligible = filter_recipes(recipes, settings, {}, nutrition)
        for r in eligible:
            assert r.type == "MEAL", f"Non-MEAL recipe {r.id} (type={r.type}) in pool"



# ---------------------------------------------------------------------------
# Tests — within-week recency update
# ---------------------------------------------------------------------------

class TestWithinWeekRecency:
    """The optimizer updates an in-memory within_week_index after scoring each
    slot so subsequent slots in the same week treat just-considered recipes as
    "used today" when computing penalties. These tests pin that behaviour."""

    def _balanced_variety(self, lunch_dinner_shared: bool) -> VarietyConfig:
        return VarietyConfig(
            level="BALANCED",
            lunch_dinner_shared_recency=lunch_dinner_shared,
            breakfast_snack_shared_recency=False,
            protein_source_variety=False,
            per_category={},
        )

    def test_penalty_zero_when_no_history(self):
        variety = self._balanced_variety(lunch_dinner_shared=True)
        pen = compute_recency_penalty(
            "rec_chicken_rice", "LUNCH", variety, {}, {}, datetime.date(2025, 5, 19)
        )
        assert pen == 0.0

    def test_within_week_index_drives_penalty(self):
        """If a recipe was used earlier in the same week, the penalty applies
        even when history_index is empty."""
        variety = self._balanced_variety(lunch_dinner_shared=True)
        ref = datetime.date(2025, 5, 22)             # Thursday
        used = datetime.date(2025, 5, 19)            # Monday — 3 days earlier
        within = {(("LUNCH_DINNER"), "rec_chicken_rice"): used}
        pen = compute_recency_penalty(
            "rec_chicken_rice", "DINNER", variety, {}, within, ref
        )
        # window = 28 days, days_ago = 3 → penalty = 1 - 3/28
        assert abs(pen - (1.0 - 3 / 28)) < 1e-9

    def test_within_week_uses_most_recent_of_history_vs_window(self):
        """When both indices contain the recipe, the more recent date wins."""
        variety = self._balanced_variety(lunch_dinner_shared=True)
        ref = datetime.date(2025, 5, 22)
        hist = {("LUNCH_DINNER", "rec_chicken_rice"): datetime.date(2025, 5, 1)}
        within = {("LUNCH_DINNER", "rec_chicken_rice"): datetime.date(2025, 5, 20)}
        pen_within = compute_recency_penalty(
            "rec_chicken_rice", "LUNCH", variety, hist, within, ref
        )
        # Using within (2 days ago) wins over history (21 days ago)
        assert abs(pen_within - (1.0 - 2 / 28)) < 1e-9

    def test_shared_recency_group_couples_lunch_and_dinner(self):
        """With lunch_dinner_shared_recency=True a within-week DINNER entry
        keyed under LUNCH_DINNER must penalise a subsequent LUNCH lookup."""
        variety = self._balanced_variety(lunch_dinner_shared=True)
        ref = datetime.date(2025, 5, 22)
        within = {("LUNCH_DINNER", "rec_chicken_rice"): datetime.date(2025, 5, 21)}
        pen_lunch = compute_recency_penalty(
            "rec_chicken_rice", "LUNCH", variety, {}, within, ref
        )
        assert pen_lunch > 0

    def test_unshared_recency_isolates_lunch_and_dinner(self):
        """With lunch_dinner_shared_recency=False a within-week DINNER entry
        must NOT penalise a subsequent LUNCH lookup (they live in distinct
        recency groups)."""
        variety = self._balanced_variety(lunch_dinner_shared=False)
        ref = datetime.date(2025, 5, 22)
        within = {("DINNER", "rec_chicken_rice"): datetime.date(2025, 5, 21)}
        pen_lunch = compute_recency_penalty(
            "rec_chicken_rice", "LUNCH", variety, {}, within, ref
        )
        assert pen_lunch == 0.0


# ---------------------------------------------------------------------------
# Tests — protein source variety
# ---------------------------------------------------------------------------

class TestProteinSourceVariety:
    """When proteinSourceVariety is enabled the optimizer penalises repeating
    the dominant protein ingredient across lunch/dinner slots."""

    def test_dominant_source_field_present_in_nutrition_data(self):
        # Sanity check — chicken-rice's dominant protein is chicken_breast
        from main import _dominant_protein_source
        r = _make_recipes()["rec_chicken_rice"]
        assert _dominant_protein_source(r, INGREDIENTS_RAW) == "ing_chicken_breast"

    def _build_settings(self, protein_variety: bool) -> dict:
        data = _load_fixture()
        data["settings"]["variety"]["proteinSourceVariety"] = protein_variety
        # Loosen per-category limits so the only differentiator is the new penalty
        data["settings"]["variety"]["perCategory"] = {
            "LUNCH":  {"maxTimesPerWeek": None, "maxConsecutiveDays": None},
            "DINNER": {"maxTimesPerWeek": None, "maxConsecutiveDays": None},
        }
        # Remove batch groups so every lunch slot is independent
        data["settings"]["schedule"]["batchGroups"] = []
        # Disable powder so it doesn't perturb the objective
        data["settings"]["proteinPowder"]["autoFillGap"] = False
        return data

    @staticmethod
    def _count_dominant_sources(plan: dict) -> dict[str, int]:
        from collections import Counter
        from main import _dominant_protein_source
        recipes = _make_recipes()
        counts: Counter[str] = Counter()
        for day in plan["days"]:
            for meal in day["meals"]:
                if meal["type"] not in ("LUNCH", "DINNER"):
                    continue
                src = _dominant_protein_source(recipes[meal["recipeId"]], INGREDIENTS_RAW)
                if src:
                    counts[src] += 1
        return dict(counts)

    def test_variety_off_allows_concentrated_protein_sources(self):
        """Without the penalty the solver is free to repeat protein sources."""
        plan_off = _call_optimizer(self._build_settings(protein_variety=False),
                                   pass_ingredients=True)
        counts_off = self._count_dominant_sources(plan_off)
        assert sum(counts_off.values()) > 0  # smoke check: plan has meals

    def test_variety_on_spreads_protein_sources(self):
        """Enabling proteinSourceVariety should not increase the maximum number
        of times any single protein source dominates lunch+dinner slots."""
        plan_off = _call_optimizer(self._build_settings(protein_variety=False),
                                   pass_ingredients=True)
        plan_on = _call_optimizer(self._build_settings(protein_variety=True),
                                  pass_ingredients=True)

        counts_off = self._count_dominant_sources(plan_off)
        counts_on  = self._count_dominant_sources(plan_on)

        max_off = max(counts_off.values()) if counts_off else 0
        max_on  = max(counts_on.values())  if counts_on  else 0
        assert max_on <= max_off, (
            f"proteinSourceVariety penalty did not spread sources: "
            f"max repeat off={max_off}, on={max_on}, "
            f"off={counts_off}, on={counts_on}"
        )

    def test_variety_penalty_inactive_when_ingredients_missing(self):
        """Smoke test: the existing API path (no ingredients_raw passed) must
        still solve cleanly with proteinSourceVariety=True in the settings."""
        data = self._build_settings(protein_variety=True)
        plan = _call_optimizer(data, pass_ingredients=False)
        assert len(plan["days"]) == 7
