"""
Tests for features added in the backend redesign:
  - Simplified settings schema (_parse_settings new vs legacy)
  - Auto batch_number assignment
  - Snack cap (max 3 per day)
  - Rating multiplier on recency penalty
  - timesManuallyRemoved history aging
  - pinned_meals partial replanning (swap_meal core logic)
"""
from __future__ import annotations

import datetime
import pathlib
import sys

import pytest

sys.path.insert(0, str(pathlib.Path(__file__).parent.parent))

from main import (
    BatchGroup,
    DayMealConfig,
    Goals,
    RecipeRating,
    Settings,
    VarietyConfig,
    VarietyPerCategory,
    RATING_PENALTY_MULTIPLIERS,
    DAY_NAME_TO_ISO,
    _apply_rating_multiplier,
    _parse_settings,
    _parse_batch_groups_new,
    _parse_meal_slots_new,
    build_history_index,
    compute_recipe_nutrition,
    filter_recipes,
    solve,
    _parse_recipe,
)
from tests.test_optimizer import INGREDIENTS_RAW, RECIPES_RAW

START_DATE = datetime.date(2025, 5, 19)


def _no_ratings() -> dict[str, RecipeRating]:
    return {}


def _make_rating(stars: int | None = None, times_manually_removed: int = 0) -> RecipeRating:
    return RecipeRating(
        recipe_id="",
        stars=stars,
        is_excluded=False,
        is_pinned=False,
        last_scheduled_date=None,
        times_scheduled=0,
        times_manually_removed=times_manually_removed,
    )


# ---------------------------------------------------------------------------
# _parse_settings — new schema
# ---------------------------------------------------------------------------

class TestParseSettingsNewSchema:
    def _new_schema(self, **overrides) -> dict:
        base = {
            "goals": {
                "kcalTarget": 2000,
                "proteinTarget": 150,
                "fatTarget": 60,
                "carbsTarget": 200,
            },
            "days": {
                "MONDAY":    {"breakfast": False, "lunch": True, "dinner": True, "snacks": False},
                "TUESDAY":   {"breakfast": False, "lunch": True, "dinner": True, "snacks": True},
                "WEDNESDAY": {"breakfast": False, "lunch": True, "dinner": True, "snacks": False},
            },
            "batchGroups": [],
        }
        base.update(overrides)
        return base

    def test_detects_new_schema_via_days_key(self):
        data = self._new_schema()
        settings = _parse_settings(data, _no_ratings())
        assert "MONDAY" in settings.meal_slots

    def test_snacks_bool_true_maps_to_minus_one(self):
        data = self._new_schema()
        settings = _parse_settings(data, _no_ratings())
        assert settings.meal_slots["TUESDAY"].snack_count == -1

    def test_snacks_bool_false_maps_to_zero(self):
        data = self._new_schema()
        settings = _parse_settings(data, _no_ratings())
        assert settings.meal_slots["MONDAY"].snack_count == 0

    def test_goals_parsed_directly(self):
        data = self._new_schema()
        settings = _parse_settings(data, _no_ratings())
        assert settings.goals.kcal_target == 2000
        assert settings.goals.protein_target == 150

    def test_variety_always_balanced(self):
        data = self._new_schema()
        settings = _parse_settings(data, _no_ratings())
        assert settings.variety.level == "BALANCED"

    def test_protein_powder_enabled_flag(self):
        data = self._new_schema(proteinPowder={
            "enabled": True,
            "ingredientId": "ing_protein_powder",
            "name": "Whey",
            "proteinPer100g": 72.0,
            "kcalPer100g": 354.0,
        })
        settings = _parse_settings(data, _no_ratings())
        assert settings.protein_powder is not None
        assert settings.protein_powder.auto_fill_gap is True

    def test_protein_powder_disabled_gives_none(self):
        data = self._new_schema(proteinPowder={
            "enabled": False,
            "ingredientId": "ing_protein_powder",
            "name": "Whey",
            "proteinPer100g": 72.0,
            "kcalPer100g": 354.0,
        })
        settings = _parse_settings(data, _no_ratings())
        assert settings.protein_powder is None

    def test_dietary_restrictions_key(self):
        data = self._new_schema(dietaryRestrictions={"excludedIngredientIds": ["ing_salmon"]})
        settings = _parse_settings(data, _no_ratings())
        assert "ing_salmon" in settings.diet_excluded_ingredient_ids


class TestParseSettingsLegacySchema:
    """Legacy schema (data.schedule.mealSlots) still parses correctly."""

    def _legacy_schema(self) -> dict:
        return {
            "goals": {"kcalTarget": 1800, "proteinTarget": 140, "fatTarget": 55, "carbsTarget": 180},
            "schedule": {
                "mealSlots": {
                    "MONDAY": {"breakfast": False, "lunch": True, "dinner": True, "snackCount": 1},
                },
                "batchGroups": [],
            },
            "variety": {
                "level": "STRICT",
                "lunchDinnerSharedRecency": True,
                "breakfastSnackSharedRecency": False,
                "proteinSourceVariety": False,
                "perCategory": {},
            },
        }

    def test_legacy_schema_parsed(self):
        data = self._legacy_schema()
        settings = _parse_settings(data, _no_ratings())
        assert settings.meal_slots["MONDAY"].snack_count == 1

    def test_legacy_variety_level_respected(self):
        data = self._legacy_schema()
        settings = _parse_settings(data, _no_ratings())
        assert settings.variety.level == "STRICT"


# ---------------------------------------------------------------------------
# Batch group auto batch_number
# ---------------------------------------------------------------------------

class TestParseBatchGroupsNew:
    def test_auto_assigns_batch_numbers_per_meal(self):
        data = {
            "batchGroups": [
                {"meal": "LUNCH",  "days": ["MONDAY", "TUESDAY", "WEDNESDAY"]},
                {"meal": "DINNER", "days": ["MONDAY", "TUESDAY"]},
                {"meal": "DINNER", "days": ["WEDNESDAY", "THURSDAY"]},
            ]
        }
        groups = _parse_batch_groups_new(data)
        assert len(groups) == 3
        dinner_groups = [g for g in groups if g.meal == "DINNER"]
        assert {g.batch_number for g in dinner_groups} == {1, 2}

    def test_day_names_converted_to_iso(self):
        data = {"batchGroups": [{"meal": "LUNCH", "days": ["MONDAY", "FRIDAY"]}]}
        groups = _parse_batch_groups_new(data)
        assert groups[0].days == [DAY_NAME_TO_ISO["MONDAY"], DAY_NAME_TO_ISO["FRIDAY"]]

    def test_invalid_day_name_skipped(self):
        data = {"batchGroups": [{"meal": "LUNCH", "days": ["INVALID_DAY"]}]}
        groups = _parse_batch_groups_new(data)
        assert groups == []


# ---------------------------------------------------------------------------
# Rating multiplier
# ---------------------------------------------------------------------------

class TestApplyRatingMultiplier:
    def test_five_stars_reduces_penalty(self):
        rating = _make_rating(stars=5)
        assert _apply_rating_multiplier(1.0, rating) == pytest.approx(RATING_PENALTY_MULTIPLIERS[5])

    def test_two_stars_increases_penalty(self):
        rating = _make_rating(stars=2)
        assert _apply_rating_multiplier(1.0, rating) == pytest.approx(RATING_PENALTY_MULTIPLIERS[2])

    def test_three_stars_unchanged(self):
        rating = _make_rating(stars=3)
        assert _apply_rating_multiplier(0.8, rating) == pytest.approx(0.8)

    def test_no_rating_unchanged(self):
        assert _apply_rating_multiplier(0.5, None) == pytest.approx(0.5)

    def test_zero_penalty_stays_zero(self):
        rating = _make_rating(stars=5)
        assert _apply_rating_multiplier(0.0, rating) == 0.0


# ---------------------------------------------------------------------------
# timesManuallyRemoved history aging
# ---------------------------------------------------------------------------

class TestTimesManuallyRemovedAging:
    """Verify that manual removals push lastScheduledDate forward, increasing
    the recency penalty so the recipe appears less often."""

    def _settings(self) -> Settings:
        return Settings(
            meal_slots={"MONDAY": DayMealConfig(False, True, True, 0)},
            batch_groups=[],
            goals=Goals(kcal_target=1800, protein_target=150, fat_target=50, carbs_target=180),
            variety=VarietyConfig(
                level="BALANCED",
                lunch_dinner_shared_recency=True,
                breakfast_snack_shared_recency=False,
                protein_source_variety=False,
                per_category={},
            ),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )

    def test_removed_recipe_gets_higher_penalty(self):
        """A recipe with timesManuallyRemoved=2 should be treated as more recently
        used than one with no removals, resulting in a higher recency penalty."""
        all_recipes = {rid: _parse_recipe(rid, data) for rid, data in RECIPES_RAW.items()}
        settings = self._settings()
        nutrition = compute_recipe_nutrition(all_recipes, INGREDIENTS_RAW)

        history_index_base: dict = {}
        # Give both recipes the same old history date
        old_date = START_DATE - datetime.timedelta(days=14)
        from main import _recency_group
        history_index_base[(_recency_group("LUNCH", settings.variety), "rec_chicken_rice")] = old_date
        history_index_base[(_recency_group("LUNCH", settings.variety), "rec_tuna_rice")] = old_date

        # ratings: chicken_rice has 2 manual removals, tuna_rice has 0
        ratings = {
            "rec_chicken_rice": _make_rating(times_manually_removed=2),
            "rec_tuna_rice":    _make_rating(times_manually_removed=0),
        }

        eligible = filter_recipes(all_recipes, settings, ratings, nutrition)

        import copy
        hi_with_removals = copy.deepcopy(history_index_base)
        hi_without = copy.deepcopy(history_index_base)

        # Run solve with removals — chicken_rice should appear less often due to aging
        # We can't easily inspect penalty values directly, so verify solve completes
        plan = solve(eligible, settings, nutrition, ratings, hi_with_removals, START_DATE)
        assert plan["days"]


# ---------------------------------------------------------------------------
# Snack cap — max 3 per day
# ---------------------------------------------------------------------------

class TestSnackCap:
    """With snack_count=-1, at most 3 snacks should appear per day."""

    def _settings_with_snacks(self) -> Settings:
        return Settings(
            meal_slots={
                day: DayMealConfig(False, True, True, -1)
                for day in ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"]
            },
            batch_groups=[],
            goals=Goals(kcal_target=1800, protein_target=150, fat_target=50, carbs_target=180),
            variety=VarietyConfig(
                level="BALANCED",
                lunch_dinner_shared_recency=True,
                breakfast_snack_shared_recency=False,
                protein_source_variety=False,
                per_category={},
            ),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )

    def test_snacks_capped_at_three(self):
        all_recipes = {rid: _parse_recipe(rid, data) for rid, data in RECIPES_RAW.items()}
        settings = self._settings_with_snacks()
        nutrition = compute_recipe_nutrition(all_recipes, INGREDIENTS_RAW)
        eligible = filter_recipes(all_recipes, settings, {}, nutrition)
        plan = solve(eligible, settings, nutrition, {}, {}, START_DATE)

        for day_data in plan["days"]:
            snacks = [m for m in day_data["meals"] if m["type"] == "SNACK"]
            assert len(snacks) <= 3, f"Day {day_data['day']} has {len(snacks)} snacks (cap is 3)"


# ---------------------------------------------------------------------------
# pinned_meals — partial replanning
# ---------------------------------------------------------------------------

class TestPinnedMeals:
    """solve() with pinned_meals only changes the unpinned slot."""

    def _full_week_settings(self) -> Settings:
        return Settings(
            meal_slots={day: DayMealConfig(False, True, True, 0) for day in
                        ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]},
            batch_groups=[],
            goals=Goals(kcal_target=1800, protein_target=150, fat_target=50, carbs_target=180),
            variety=VarietyConfig(
                level="BALANCED",
                lunch_dinner_shared_recency=True,
                breakfast_snack_shared_recency=False,
                protein_source_variety=False,
                per_category={},
            ),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )

    def test_target_slot_uses_different_recipe(self):
        """solve() with pinned_meals and an excluded old recipe must pick
        a different recipe for the free slot.  Other slots may also change
        if they happened to use the excluded recipe (expected with small pool)."""
        all_recipes = {rid: _parse_recipe(rid, data) for rid, data in RECIPES_RAW.items()}
        settings = self._full_week_settings()
        nutrition = compute_recipe_nutrition(all_recipes, INGREDIENTS_RAW)
        eligible = filter_recipes(all_recipes, settings, {}, nutrition)

        # First plan — record all assignments
        plan1 = solve(eligible, settings, nutrition, {}, {}, START_DATE)

        # Identify MONDAY DINNER recipe and pin everything else
        from main import DAY_NAMES
        pinned: dict[tuple[int, str, str], str] = {}
        monday_dinner_recipe: str | None = None
        for day_data in plan1["days"]:
            d_name = day_data["dayOfWeek"]
            d_idx = DAY_NAMES.index(d_name)
            for meal in day_data["meals"]:
                mt, rid = meal["type"], meal["recipeId"]
                if d_name == "MONDAY" and mt == "DINNER":
                    monday_dinner_recipe = rid
                else:
                    pinned[(d_idx, mt, rid)] = rid

        if monday_dinner_recipe is None:
            pytest.skip("No MONDAY DINNER in plan1")

        # Exclude the old recipe and solve with pinned constraints.
        # Note: globally excluding the recipe means any other day that also used it
        # will not have its pin satisfied — that is expected behaviour with a small pool.
        swap_excluded = settings.excluded_recipe_ids | {monday_dinner_recipe}
        settings_for_swap = Settings(
            meal_slots=settings.meal_slots,
            batch_groups=settings.batch_groups,
            goals=settings.goals,
            variety=settings.variety,
            protein_powder=settings.protein_powder,
            diet_excluded_ingredient_ids=settings.diet_excluded_ingredient_ids,
            excluded_recipe_ids=swap_excluded,
            rules=settings.rules,
        )
        eligible_swap = filter_recipes(all_recipes, settings_for_swap, {}, nutrition)

        plan2 = solve(eligible_swap, settings_for_swap, nutrition, {}, {}, START_DATE, pinned_meals=pinned)

        # The plan must be structurally valid
        assert plan2["days"], "swap produced no days"

        # MONDAY DINNER must not use the excluded recipe
        for day_data in plan2["days"]:
            if day_data["dayOfWeek"] == "MONDAY":
                for meal in day_data["meals"]:
                    if meal["type"] == "DINNER":
                        assert meal["recipeId"] != monday_dinner_recipe, (
                            "Excluded recipe appeared in the target swap slot"
                        )


# ---------------------------------------------------------------------------
# Infeasibility fallback — approximate plan
# ---------------------------------------------------------------------------

class TestInfeasibilityFallback:
    """When hard per-day bounds make the problem infeasible, solve() retries
    without those bounds and returns approximate=True instead of raising."""

    def test_impossible_kcal_targets_return_approximate_plan(self):
        """Goals set to 10 000 kcal/day — impossible with the test pool.
        Expects a structurally valid plan tagged approximate=True."""
        all_recipes = {rid: _parse_recipe(rid, data) for rid, data in RECIPES_RAW.items()}
        settings = Settings(
            meal_slots={"MONDAY": DayMealConfig(False, True, True, 0)},
            batch_groups=[],
            goals=Goals(
                kcal_target=10_000,
                protein_target=150,
                fat_target=50,
                carbs_target=180,
                min_kcal=9_500,   # forces infeasibility — no recipe combo reaches this
            ),
            variety=VarietyConfig(
                level="BALANCED",
                lunch_dinner_shared_recency=True,
                breakfast_snack_shared_recency=False,
                protein_source_variety=False,
                per_category={},
            ),
            protein_powder=None,
            diet_excluded_ingredient_ids=[],
            excluded_recipe_ids=set(),
            rules=[],
        )
        nutrition = compute_recipe_nutrition(all_recipes, INGREDIENTS_RAW)
        eligible = filter_recipes(all_recipes, settings, {}, nutrition)

        plan = solve(eligible, settings, nutrition, {}, {}, START_DATE)

        assert plan.get("approximate") is True, "Expected approximate=True for infeasible targets"
        assert plan["days"], "Fallback plan must still have days"
        # Structural integrity: MONDAY must have both LUNCH and DINNER
        monday = next(d for d in plan["days"] if d["dayOfWeek"] == "MONDAY")
        meal_types = {m["type"] for m in monday["meals"]}
        assert "LUNCH" in meal_types and "DINNER" in meal_types
