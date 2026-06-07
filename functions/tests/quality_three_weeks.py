"""
Three-week quality test for the FoodPlan CP-SAT optimizer.

Runs the optimizer for three consecutive weeks, feeding each week's result
as history to the next, then reports recipe variety and cross-week overlap.

Usage:
    cd functions
    python tests/quality_three_weeks.py
"""

import datetime
import json
import pathlib
import sys
from collections import Counter, defaultdict

sys.path.insert(0, str(pathlib.Path(__file__).parent.parent))

from main import (
    build_history_index,
    compute_recipe_nutrition,
    filter_recipes,
    solve,
    _parse_recipe,
    _parse_settings,
    _parse_rating,
)
from tests.test_optimizer import INGREDIENTS_RAW, RECIPES_RAW

FIXTURE_PATH = pathlib.Path(__file__).parent / "fixtures" / "minimal.json"
WEEK_STARTS = [
    datetime.date(2025, 5, 19),
    datetime.date(2025, 5, 26),
    datetime.date(2025, 6, 2),
]


def _make_recipes():
    return {rid: _parse_recipe(rid, data) for rid, data in RECIPES_RAW.items()}


def _recipe_name(recipe_id: str, recipes: dict) -> str:
    r = recipes.get(recipe_id)
    return r.name if r else recipe_id


def _plan_to_history_entry(plan: dict) -> dict:
    """Convert a solved plan dict to the format build_history_index expects."""
    return {
        "id": plan["id"],
        "startDate": plan["startDate"],
        "endDate": plan["endDate"],
        "days": [
            {
                "date": day["date"],
                "meals": day["meals"],
            }
            for day in plan["days"]
        ],
    }


def _plan_recipe_set(plan: dict) -> set[str]:
    return {m["recipeId"] for day in plan["days"] for m in day["meals"]}


def _day_kcal(day: dict, nutrition: dict) -> float:
    food_kcal = sum(
        nutrition[m["recipeId"]].kcal
        for m in day["meals"]
        if m["recipeId"] in nutrition
    )
    return food_kcal + day.get("proteinPowderGrams", 0) * 354 / 100


def _day_protein(day: dict, nutrition: dict) -> float:
    food_protein = sum(
        nutrition[m["recipeId"]].protein
        for m in day["meals"]
        if m["recipeId"] in nutrition
    )
    return food_protein + day.get("proteinPowderGrams", 0) * 72 / 100


def run_three_weeks():
    with open(FIXTURE_PATH) as f:
        fixture = json.load(f)

    recipes = _make_recipes()
    nutrition = compute_recipe_nutrition(recipes, INGREDIENTS_RAW)
    settings = _parse_settings(fixture["settings"], {})

    plans: list[dict] = []
    history_plans: list[dict] = []

    # ── Solve each week ─────────────────────────────────────────────────────
    for week_num, start_date in enumerate(WEEK_STARTS, 1):
        print(f"\n{'='*60}")
        print(f"  WEEK {week_num}  ({start_date})")
        print(f"{'='*60}")

        # Re-build settings & filter with up-to-date history
        settings = _parse_settings(fixture["settings"], {})
        eligible = filter_recipes(recipes, settings, {}, nutrition)
        history_index = build_history_index(history_plans, settings.variety)

        plan = solve(eligible, settings, nutrition, {}, history_index, start_date)
        plans.append(plan)
        history_plans.append(_plan_to_history_entry(plan))

        # ── Per-day report ───────────────────────────────────────────────────
        kcal_target   = fixture["settings"]["goals"]["kcalTarget"]
        protein_target = fixture["settings"]["goals"]["proteinTarget"]

        print(f"\n{'Day':<10} {'Meals':<50} {'kcal':>5} {'prot':>5} {'pwd':>4}")
        print("-" * 80)
        total_kcal = total_protein = 0.0
        for day in plan["days"]:
            meal_str = " | ".join(
                f"{m['type'][:2]}:{_recipe_name(m['recipeId'], recipes)[:18]}"
                for m in day["meals"]
            )
            day_kcal    = _day_kcal(day, nutrition)
            day_protein = _day_protein(day, nutrition)
            total_kcal    += day_kcal
            total_protein += day_protein
            print(
                f"{day['dayOfWeek'][:3]:<10} {meal_str:<50} "
                f"{day_kcal:>5.0f} {day_protein:>5.0f} "
                f"{day.get('proteinPowderGrams', 0):>4.0f}g"
            )

        n_days = len(plan["days"])
        kcal_err    = (total_kcal    - kcal_target * n_days)    / (kcal_target * n_days)    * 100
        protein_err = (total_protein - protein_target * n_days) / (protein_target * n_days) * 100
        print(f"\n  Weekly kcal:    {total_kcal:,.0f} / {kcal_target * n_days:,.0f}  ({kcal_err:+.1f}%)")
        print(f"  Weekly protein: {total_protein:,.0f}g / {protein_target * n_days:,.0f}g  ({protein_err:+.1f}%)")
        print(f"  Powder total:   {sum(d.get('proteinPowderGrams',0) for d in plan['days']):.0f}g")

        # ── Recipe use count ─────────────────────────────────────────────────
        dinner_ids = [m["recipeId"] for d in plan["days"] for m in d["meals"] if m["type"] == "DINNER"]
        print(f"\n  Dinner choices:")
        for rid, cnt in Counter(dinner_ids).most_common():
            print(f"    {'×'+str(cnt):<4} {_recipe_name(rid, recipes)}")

    # ── Cross-week comparison ────────────────────────────────────────────────
    print(f"\n{'='*60}")
    print("  CROSS-WEEK VARIETY ANALYSIS")
    print(f"{'='*60}")

    sets = [_plan_recipe_set(p) for p in plans]

    # Dinner overlap
    def _dinner_ids(plan):
        return {m["recipeId"] for d in plan["days"] for m in d["meals"] if m["type"] == "DINNER"}

    dinner_sets = [_dinner_ids(p) for p in plans]

    print("\n── Recipe overlap (all meal types) ────────────────────────")
    print(f"  W1 ∩ W2 : {len(sets[0] & sets[1])} recipes  →  {sorted(_recipe_name(r, recipes) for r in sets[0] & sets[1])}")
    print(f"  W2 ∩ W3 : {len(sets[1] & sets[2])} recipes  →  {sorted(_recipe_name(r, recipes) for r in sets[1] & sets[2])}")
    print(f"  W1 ∩ W3 : {len(sets[0] & sets[2])} recipes  →  {sorted(_recipe_name(r, recipes) for r in sets[0] & sets[2])}")
    print(f"  W1 ∩ W2 ∩ W3 : {len(sets[0] & sets[1] & sets[2])} recipes in all 3 weeks")

    print("\n── Dinner overlap ─────────────────────────────────────────")
    print(f"  W1 ∩ W2 dinners : {sorted(_recipe_name(r, recipes) for r in dinner_sets[0] & dinner_sets[1])}")
    print(f"  W2 ∩ W3 dinners : {sorted(_recipe_name(r, recipes) for r in dinner_sets[1] & dinner_sets[2])}")
    print(f"  In all 3 weeks  : {sorted(_recipe_name(r, recipes) for r in dinner_sets[0] & dinner_sets[1] & dinner_sets[2])}")

    print("\n── Lunch batch selection per week ─────────────────────────")
    for wi, plan in enumerate(plans, 1):
        lunch_ids = {m["recipeId"] for d in plan["days"] for m in d["meals"] if m["type"] == "LUNCH"}
        print(f"  W{wi}: {sorted(_recipe_name(r, recipes) for r in lunch_ids)}")

    print("\n── Summary ────────────────────────────────────────────────")
    dinner_identical_w1_w2 = dinner_sets[0] == dinner_sets[1]
    dinner_identical_w2_w3 = dinner_sets[1] == dinner_sets[2]
    all_identical = sets[0] == sets[1] == sets[2]
    lunch_w1 = {m["recipeId"] for d in plans[0]["days"] for m in d["meals"] if m["type"] == "LUNCH"}
    lunch_w2 = {m["recipeId"] for d in plans[1]["days"] for m in d["meals"] if m["type"] == "LUNCH"}
    lunch_w3 = {m["recipeId"] for d in plans[2]["days"] for m in d["meals"] if m["type"] == "LUNCH"}
    lunch_vary = not (lunch_w1 == lunch_w2 == lunch_w3)

    print(f"  Plans completely identical:   {'✗ YES (BUG)' if all_identical else '✓ No — plans differ'}")
    print(f"  Dinner sets W1=W2:            {'✗ Identical' if dinner_identical_w1_w2 else '✓ Different'}")
    print(f"  Dinner sets W2=W3:            {'✗ Identical' if dinner_identical_w2_w3 else '✓ Different'}")
    print(f"  Lunch batch varies:           {'✓ Yes' if lunch_vary else '~ Same batch all 3 weeks (ok if only one good fit)'}")
    print()

    return plans


if __name__ == "__main__":
    run_three_weeks()
