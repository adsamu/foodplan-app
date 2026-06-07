"""
Quality analysis script for a generated meal plan.

Usage:
    cd functions
    FIRESTORE_EMULATOR_HOST=localhost:8080 \\
        python tests/analyse_plan.py --userId test_user --planId <planId>

Fetches the generated plan from the Firestore emulator and prints a quality
report covering macro accuracy, recipe variety, recency, and batch consistency.
"""

import argparse
import datetime
import os
import sys
from collections import Counter

os.environ.setdefault("FIRESTORE_EMULATOR_HOST", "localhost:8080")

import firebase_admin  # noqa: E402
from firebase_admin import credentials, firestore  # noqa: E402

if not firebase_admin._apps:  # noqa: SLF001
    firebase_admin.initialize_app(credentials.ApplicationDefault(), {"projectId": "your-project-id"})

db = firestore.client()


def recipe_nutrition(recipe_id: str, recipes: dict, ingreds: dict) -> tuple[float, float, float, float]:
    """Return (kcal, protein, fat, carbs) for a recipe, resolving sub-recipes one level deep."""
    r = recipes.get(recipe_id, {})
    kcal = protein = fat = carbs = 0.0

    resolved_components: dict[str, tuple[float, float, float, float]] = {}

    # First pass: resolve components
    for rid, rec in recipes.items():
        if rec.get("type") != "COMPONENT":
            continue
        c_kcal = c_protein = c_fat = c_carbs = 0.0
        for ing in rec.get("ingredients", []):
            if ing.get("ingredientId"):
                i = ingreds.get(ing["ingredientId"], {})
                g = ing.get("grams", 0) / 100.0
                c_kcal    += g * i.get("kcalPer100g", 0)
                c_protein += g * i.get("proteinPer100g", 0)
                c_fat     += g * i.get("fatPer100g", 0)
                c_carbs   += g * i.get("carbsPer100g", 0)
        resolved_components[rid] = (c_kcal, c_protein, c_fat, c_carbs)

    for ing in r.get("ingredients", []):
        if ing.get("ingredientId"):
            i = ingreds.get(ing["ingredientId"], {})
            g = ing.get("grams", 0) / 100.0
            kcal    += g * i.get("kcalPer100g", 0)
            protein += g * i.get("proteinPer100g", 0)
            fat     += g * i.get("fatPer100g", 0)
            carbs   += g * i.get("carbsPer100g", 0)
        elif ing.get("subRecipeId"):
            sub_id   = ing["subRecipeId"]
            portions = ing.get("portions", 1.0)
            if sub_id in resolved_components:
                c = resolved_components[sub_id]
                kcal    += portions * c[0]
                protein += portions * c[1]
                fat     += portions * c[2]
                carbs   += portions * c[3]

    return kcal, protein, fat, carbs


def _iso_day(date_val) -> int:
    """Return ISO weekday (1=Mon…7=Sun) from a date string or datetime."""
    if isinstance(date_val, str):
        return datetime.date.fromisoformat(date_val[:10]).isoweekday()
    if hasattr(date_val, "date"):
        return date_val.date().isoweekday()
    return 1


def analyse(user_id: str, plan_id: str) -> None:
    plan = (
        db.collection("users").document(user_id)
        .collection("mealPlans").document(plan_id)
        .get().to_dict()
    )
    if not plan:
        print(f"Plan {plan_id} not found for user {user_id}.")
        sys.exit(1)

    days = plan.get("days", [])

    # Try settings from either sub-collection path
    settings_doc = None
    for coll in ("settings", "settings_doc"):
        docs = list(db.collection("users").document(user_id).collection(coll).stream())
        if docs:
            settings_doc = docs[0].to_dict()
            break
    config = settings_doc or {}

    recipes = {r.id: r.to_dict() for r in db.collection("recipes").stream()}
    ingreds = {i.id: i.to_dict() for i in db.collection("ingredients").stream()}

    total_kcal = total_protein = total_fat = total_carbs = 0.0
    total_powder_kcal = total_powder_protein = 0.0
    all_recipe_ids: list[str] = []
    lunch_ids: list[str] = []
    dinner_ids: list[str] = []

    for day in days:
        powder_g             = day.get("proteinPowderGrams", 0)
        total_powder_kcal    += powder_g * 354 / 100
        total_powder_protein += powder_g * 72  / 100

        for meal in day.get("meals", []):
            rid = meal["recipeId"]
            all_recipe_ids.append(rid)
            if meal["type"] == "LUNCH":   lunch_ids.append(rid)
            if meal["type"] == "DINNER":  dinner_ids.append(rid)
            k, p, f, c = recipe_nutrition(rid, recipes, ingreds)
            total_kcal    += k
            total_protein += p
            total_fat     += f
            total_carbs   += c

    total_kcal    += total_powder_kcal
    total_protein += total_powder_protein

    n_days         = len(days)
    goals          = config.get("goals", {})
    kcal_target    = goals.get("kcalTarget", 0) * n_days
    protein_target = goals.get("proteinTarget", 0) * n_days

    print("\n" + "=" * 55)
    print("  OPTIMIZER QUALITY REPORT")
    print("=" * 55)
    print(f"\n📅 Days planned: {n_days}")
    print(f"🍽  Total meals:  {len(all_recipe_ids)}")

    print("\n── MACRO ACCURACY ─────────────────────────────────────")
    if kcal_target:
        kcal_err = (total_kcal - kcal_target) / kcal_target * 100
        print(f"  Weekly kcal:    {total_kcal:,.0f} / {kcal_target:,.0f}  ({kcal_err:+.1f}%)")
    if protein_target:
        protein_err = (total_protein - protein_target) / protein_target * 100
        print(f"  Weekly protein: {total_protein:,.0f}g / {protein_target:,.0f}g  ({protein_err:+.1f}%)")
    total_powder = sum(d.get("proteinPowderGrams", 0) for d in days)
    print(f"  Powder used:    {total_powder:.0f}g total")

    print("\n── RECIPE VARIETY ─────────────────────────────────────")
    recipe_counts = Counter(all_recipe_ids)
    unique = len(recipe_counts)
    most_common = recipe_counts.most_common(3)
    print(f"  Unique recipes: {unique}")
    named = [(recipes.get(r, {}).get("name", r), c) for r, c in most_common]
    print(f"  Most repeated:  {named}")

    print("\n── LUNCH VARIETY ──────────────────────────────────────")
    for rid, count in Counter(lunch_ids).most_common():
        print(f"  {recipes.get(rid, {}).get('name', rid):<35} ×{count}")

    print("\n── DINNER VARIETY ─────────────────────────────────────")
    for rid, count in Counter(dinner_ids).most_common():
        print(f"  {recipes.get(rid, {}).get('name', rid):<35} ×{count}")

    print("\n── BATCH CONSISTENCY ──────────────────────────────────")
    batch_groups = config.get("schedule", {}).get("batchGroups", [])
    for g in batch_groups:
        meal      = g["meal"]
        iso_days  = set(g["days"])
        grp_days  = [d for d in days if _iso_day(d.get("date", "")) in iso_days]
        grp_ids: set[str] = set()
        for d in grp_days:
            for m in d.get("meals", []):
                if m["type"] == meal:
                    grp_ids.add(m["recipeId"])
        status = "✓ consistent" if len(grp_ids) == 1 else f"✗ INCONSISTENT ({grp_ids})"
        print(f"  Batch {g['batchNumber']} ({meal}, {len(grp_days)} days): {status}")

    print("\n── PROTEIN POWDER ─────────────────────────────────────")
    for day in days:
        g = day.get("proteinPowderGrams", 0)
        if g > 0:
            date_val = day.get("date", "")
            if hasattr(date_val, "date"):
                date_val = date_val.date().isoformat()
            print(f"  {date_val}: {g:.1f}g powder")

    print("\n" + "=" * 55 + "\n")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="Analyse a generated meal plan from the Firestore emulator.")
    p.add_argument("--userId",  default="test_user")
    p.add_argument("--planId",  required=True)
    args = p.parse_args()
    analyse(args.userId, args.planId)
