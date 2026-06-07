"""
Seed the Firestore emulator with the quality test dataset.

Usage:
    cd functions
    FIRESTORE_EMULATOR_HOST=localhost:8080 python tests/seed_emulator.py
"""

import datetime
import os
import sys

os.environ.setdefault("FIRESTORE_EMULATOR_HOST", "localhost:8080")

import firebase_admin  # noqa: E402
from firebase_admin import credentials, firestore  # noqa: E402

if not firebase_admin._apps:  # noqa: SLF001
    firebase_admin.initialize_app(credentials.ApplicationDefault(), {"projectId": "your-project-id"})

db = firestore.client()
USER_ID = "test_user"

# ---------------------------------------------------------------------------
# Ingredients
# ---------------------------------------------------------------------------
INGREDIENTS = [
    # id, name, category, kcal/100g, protein/100g, fat/100g, carbs/100g
    ("ing_chicken_breast",  "Chicken Breast",        "MEAT",        110, 23.0,  1.5,  0.0),
    ("ing_salmon",          "Salmon Fillet",          "FISH",        208, 20.0, 13.0,  0.0),
    ("ing_tuna_canned",     "Canned Tuna",            "FISH",        116, 26.0,  1.0,  0.0),
    ("ing_ground_beef",     "Ground Beef 15%",        "MEAT",        215, 17.0, 16.0,  0.0),
    ("ing_cod",             "Cod Fillet",             "FISH",         82, 18.0,  0.7,  0.0),
    ("ing_eggs",            "Eggs",                   "DAIRY_EGGS",  155, 13.0, 11.0,  1.0),
    ("ing_greek_yogurt",    "Greek Yogurt 0%",        "DAIRY_EGGS",   59, 10.0,  0.4,  3.6),
    ("ing_cottage_cheese",  "Cottage Cheese",         "CHEESE",       98, 11.0,  4.3,  3.4),
    ("ing_rice",            "White Rice (dry)",       "GRAINS",      360,  6.5,  0.5, 79.0),
    ("ing_pasta",           "Pasta (dry)",            "GRAINS",      350, 12.0,  1.5, 71.0),
    ("ing_oats",            "Rolled Oats",            "GRAINS",      389, 17.0,  7.0, 66.0),
    ("ing_sweet_potato",    "Sweet Potato",           "FRUIT_VEG",    86,  1.6,  0.1, 20.0),
    ("ing_broccoli",        "Broccoli",               "FRUIT_VEG",    34,  2.8,  0.4,  6.6),
    ("ing_spinach",         "Spinach",                "FRUIT_VEG",    23,  2.9,  0.4,  3.6),
    ("ing_tomato",          "Tomato",                 "FRUIT_VEG",    18,  0.9,  0.2,  3.9),
    ("ing_onion",           "Onion",                  "FRUIT_VEG",    40,  1.1,  0.1,  9.3),
    ("ing_garlic",          "Garlic",                 "SPICES",      149,  6.4,  0.5, 33.0),
    ("ing_olive_oil",       "Olive Oil",              "OILS_SAUCES", 884,  0.0,100.0,  0.0),
    ("ing_coconut_milk",    "Coconut Milk",           "CANNED",      197,  2.0, 21.0,  2.8),
    ("ing_chickpeas",       "Chickpeas (canned)",     "CANNED",      164,  8.9,  2.6, 27.0),
    ("ing_lentils",         "Red Lentils (dry)",      "DRY_GOODS",   353, 26.0,  1.1, 60.0),
    ("ing_kidney_beans",    "Kidney Beans (canned)",  "CANNED",      127,  8.7,  0.5, 22.0),
    ("ing_bread_whole",     "Wholegrain Bread",       "BREAD_BAKERY",247,  9.0,  3.5, 41.0),
    ("ing_banana",          "Banana",                 "FRUIT_VEG",    89,  1.1,  0.3, 23.0),
    ("ing_blueberries",     "Blueberries",            "FRUIT_VEG",    57,  0.7,  0.3, 14.0),
    ("ing_almonds",         "Almonds",                "NUTS",        579, 21.0, 50.0, 22.0),
    ("ing_protein_powder",  "Whey Protein Powder",    "SUPPLEMENT",  354, 72.0,  4.0, 12.0),
    ("ing_curry_paste",     "Red Curry Paste",        "OILS_SAUCES", 100,  2.0,  4.0, 14.0),
    ("ing_soy_sauce",       "Soy Sauce",              "OILS_SAUCES",  53,  8.1,  0.1,  4.9),
    ("ing_feta",            "Feta Cheese",            "CHEESE",      264, 14.0, 21.0,  4.1),
]

# ---------------------------------------------------------------------------
# Recipes
# ---------------------------------------------------------------------------
RECIPES = [
    {
        "id": "rec_chicken_rice", "name": "Chicken & Rice", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_chicken_breast", "grams": 180},
            {"ingredientId": "ing_rice",           "grams": 80},
            {"ingredientId": "ing_broccoli",       "grams": 150},
            {"ingredientId": "ing_olive_oil",      "grams": 10},
        ],
    },
    {
        "id": "rec_salmon_sweet_potato", "name": "Baked Salmon & Sweet Potato", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_salmon",       "grams": 180},
            {"ingredientId": "ing_sweet_potato", "grams": 200},
            {"ingredientId": "ing_spinach",      "grams": 100},
            {"ingredientId": "ing_olive_oil",    "grams": 10},
        ],
    },
    {
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
    {
        "id": "rec_tuna_rice", "name": "Tuna & Rice Bowl", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_tuna_canned", "grams": 160},
            {"ingredientId": "ing_rice",        "grams": 80},
            {"ingredientId": "ing_spinach",     "grams": 100},
            {"ingredientId": "ing_soy_sauce",   "grams": 15},
        ],
    },
    {
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
    {
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
    {
        "id": "rec_beef_sweet_potato", "name": "Ground Beef & Sweet Potato", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_ground_beef",  "grams": 150},
            {"ingredientId": "ing_sweet_potato", "grams": 200},
            {"ingredientId": "ing_broccoli",     "grams": 150},
            {"ingredientId": "ing_olive_oil",    "grams": 10},
        ],
    },
    {
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
    {
        "id": "rec_salmon_pasta", "name": "Salmon & Pasta", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_salmon",    "grams": 160},
            {"ingredientId": "ing_pasta",     "grams": 80},
            {"ingredientId": "ing_spinach",   "grams": 100},
            {"ingredientId": "ing_olive_oil", "grams": 10},
        ],
    },
    {
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
    {
        "id": "rec_tuna_pasta", "name": "Tuna Pasta", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_tuna_canned", "grams": 160},
            {"ingredientId": "ing_pasta",       "grams": 80},
            {"ingredientId": "ing_tomato",      "grams": 150},
            {"ingredientId": "ing_olive_oil",   "grams": 10},
        ],
    },
    {
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
    {
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
    {
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
    {
        "id": "rec_cod_sweet_potato", "name": "Cod & Sweet Potato Mash", "type": "MEAL",
        "mealCategories": ["DINNER"],
        "ingredients": [
            {"ingredientId": "ing_cod",          "grams": 200},
            {"ingredientId": "ing_sweet_potato", "grams": 250},
            {"ingredientId": "ing_broccoli",     "grams": 150},
            {"ingredientId": "ing_olive_oil",    "grams": 10},
        ],
    },
    {
        "id": "rec_oat_porridge", "name": "Oat Porridge with Banana", "type": "MEAL",
        "mealCategories": ["BREAKFAST"],
        "ingredients": [
            {"ingredientId": "ing_oats",   "grams": 80},
            {"ingredientId": "ing_banana", "grams": 120},
            {"ingredientId": "ing_eggs",   "grams": 50},
        ],
    },
    {
        "id": "rec_greek_yogurt_berries", "name": "Greek Yogurt & Blueberries", "type": "MEAL",
        "mealCategories": ["BREAKFAST", "SNACK"],
        "ingredients": [
            {"ingredientId": "ing_greek_yogurt", "grams": 200},
            {"ingredientId": "ing_blueberries",  "grams": 100},
            {"ingredientId": "ing_oats",         "grams": 30},
        ],
    },
    {
        "id": "rec_scrambled_eggs_toast", "name": "Scrambled Eggs on Toast", "type": "MEAL",
        "mealCategories": ["BREAKFAST"],
        "ingredients": [
            {"ingredientId": "ing_eggs",       "grams": 200},
            {"ingredientId": "ing_bread_whole","grams": 80},
            {"ingredientId": "ing_olive_oil",  "grams": 10},
            {"ingredientId": "ing_spinach",    "grams": 50},
        ],
    },
    {
        "id": "rec_cottage_cheese_toast", "name": "Cottage Cheese on Toast", "type": "MEAL",
        "mealCategories": ["BREAKFAST", "SNACK"],
        "ingredients": [
            {"ingredientId": "ing_cottage_cheese", "grams": 200},
            {"ingredientId": "ing_bread_whole",    "grams": 80},
            {"ingredientId": "ing_blueberries",    "grams": 80},
        ],
    },
    {
        "id": "rec_oat_banana_eggs", "name": "Oat & Egg Pancakes", "type": "MEAL",
        "mealCategories": ["BREAKFAST"],
        "ingredients": [
            {"ingredientId": "ing_oats",   "grams": 80},
            {"ingredientId": "ing_eggs",   "grams": 150},
            {"ingredientId": "ing_banana", "grams": 100},
        ],
    },
    {
        "id": "rec_almonds_yogurt", "name": "Almonds & Yogurt", "type": "MEAL",
        "mealCategories": ["SNACK"],
        "ingredients": [
            {"ingredientId": "ing_almonds",      "grams": 30},
            {"ingredientId": "ing_greek_yogurt", "grams": 150},
        ],
    },
    {
        "id": "rec_banana_almonds", "name": "Banana & Almonds", "type": "MEAL",
        "mealCategories": ["SNACK"],
        "ingredients": [
            {"ingredientId": "ing_banana",  "grams": 120},
            {"ingredientId": "ing_almonds", "grams": 25},
        ],
    },
    {
        "id": "rec_cottage_cheese_berries", "name": "Cottage Cheese & Berries", "type": "MEAL",
        "mealCategories": ["SNACK"],
        "ingredients": [
            {"ingredientId": "ing_cottage_cheese", "grams": 150},
            {"ingredientId": "ing_blueberries",    "grams": 100},
        ],
    },
    {
        "id": "rec_eggs_spinach", "name": "Boiled Eggs & Spinach", "type": "MEAL",
        "mealCategories": ["SNACK"],
        "ingredients": [
            {"ingredientId": "ing_eggs",    "grams": 150},
            {"ingredientId": "ing_spinach", "grams": 80},
        ],
    },
    {
        "id": "rec_oat_blueberry", "name": "Oat & Blueberry Snack", "type": "MEAL",
        "mealCategories": ["SNACK", "BREAKFAST"],
        "ingredients": [
            {"ingredientId": "ing_oats",        "grams": 50},
            {"ingredientId": "ing_blueberries", "grams": 80},
            {"ingredientId": "ing_greek_yogurt","grams": 100},
        ],
    },
    {
        "id": "comp_curry_sauce", "name": "Red Curry Sauce", "type": "COMPONENT",
        "componentCategory": "SAUCE", "mealCategories": [],
        "ingredients": [
            {"ingredientId": "ing_coconut_milk", "grams": 200},
            {"ingredientId": "ing_curry_paste",  "grams": 40},
            {"ingredientId": "ing_garlic",       "grams": 10},
            {"ingredientId": "ing_onion",        "grams": 80},
        ],
    },
    {
        "id": "rec_chicken_curry_component",
        "name": "Chicken Curry (with sauce component)", "type": "MEAL",
        "mealCategories": ["LUNCH", "DINNER"],
        "ingredients": [
            {"ingredientId": "ing_chicken_breast", "grams": 200},
            {"subRecipeId":  "comp_curry_sauce",   "portions": 0.5},
            {"ingredientId": "ing_rice",           "grams": 80},
        ],
    },
]

# ---------------------------------------------------------------------------
# Settings
# ---------------------------------------------------------------------------
SETTINGS = {
    "schedule": {
        "mealSlots": {
            "MONDAY":    {"breakfast": False, "lunch": True, "dinner": True, "snackCount": 0},
            "TUESDAY":   {"breakfast": False, "lunch": True, "dinner": True, "snackCount": 0},
            "WEDNESDAY": {"breakfast": False, "lunch": True, "dinner": True, "snackCount": 0},
            "THURSDAY":  {"breakfast": False, "lunch": True, "dinner": True, "snackCount": 0},
            "FRIDAY":    {"breakfast": False, "lunch": True, "dinner": True, "snackCount": 1},
            "SATURDAY":  {"breakfast": False, "lunch": True, "dinner": True, "snackCount": 1},
            "SUNDAY":    {"breakfast": False, "lunch": False, "dinner": True, "snackCount": 0},
        },
        "batchGroups": [
            {"meal": "LUNCH", "days": [1, 2, 3, 4, 5], "batchNumber": 1},
            {"meal": "LUNCH", "days": [6],              "batchNumber": 2},
        ],
    },
    "goals": {
        "kcalTarget": 1800,
        "proteinTarget": 150,
        "fatTarget": None,
        "carbsTarget": 180,
        "autoField": "FAT",
        "maxKcalPerDay": 2200,
    },
    "variety": {
        "level": "BALANCED",
        "lunchDinnerSharedRecency": True,
        "breakfastSnackSharedRecency": False,
        "proteinSourceVariety": True,
        "perCategory": {
            "LUNCH":     {"maxTimesPerWeek": None, "maxConsecutiveDays": None},
            "DINNER":    {"maxTimesPerWeek": 2,    "maxConsecutiveDays": 2},
            "BREAKFAST": {"maxTimesPerWeek": None, "maxConsecutiveDays": None},
            "SNACK":     {"maxTimesPerWeek": 3,    "maxConsecutiveDays": None},
        },
    },
    "proteinPowder": {
        "ingredientId": "ing_protein_powder",
        "name": "Whey Protein",
        "proteinPer100g": 72,
        "kcalPer100g": 354,
        "autoFillGap": True,
    },
    "diet": {
        "dietTypes": [],
        "allergies": [],
        "excludedIngredientIds": [],
        "preferredIngredientIds": [],
        "dislikedIngredientIds": [],
    },
    "rules": [],
    "shopping": {"shoppingDays": [7], "intervalWeeks": 1},
}


# ---------------------------------------------------------------------------
# Seed
# ---------------------------------------------------------------------------
def seed():
    print("Seeding ingredients...")
    for ing in INGREDIENTS:
        fields = {
            "id": ing[0], "name": ing[1], "category": ing[2],
            "kcalPer100g": ing[3], "proteinPer100g": ing[4],
            "fatPer100g": ing[5], "carbsPer100g": ing[6],
            "source": "LABEL", "steps": [],
        }
        db.collection("ingredients").document(ing[0]).set(fields)

    print("Seeding recipes...")
    for recipe in RECIPES:
        doc = {k: v for k, v in recipe.items() if k != "id"}
        doc["id"] = recipe["id"]
        doc.setdefault("notes", "")
        doc.setdefault("steps", [])
        doc.setdefault("componentCategory", recipe.get("componentCategory"))
        db.collection("recipes").document(recipe["id"]).set(doc)

    print("Seeding settings...")
    db.collection("users").document(USER_ID).collection("settings").document("main").set(SETTINGS)

    print("Seeding history plan...")
    two_weeks_ago = datetime.date.today() - datetime.timedelta(weeks=2)
    db.collection("users").document(USER_ID).collection("mealPlans").document("history_plan_1").set({
        "id": "history_plan_1",
        "name": "Previous Week",
        "startDate": two_weeks_ago.isoformat(),
        "endDate": (two_weeks_ago + datetime.timedelta(days=6)).isoformat(),
        "days": [
            {
                "id": "hist_day_1",
                "date": two_weeks_ago.isoformat(),
                "dayOfWeek": "MONDAY",
                "meals": [
                    {"type": "LUNCH",  "recipeId": "rec_chicken_rice"},
                    {"type": "DINNER", "recipeId": "rec_salmon_sweet_potato"},
                ],
                "proteinPowderGrams": 25,
                "kcalTarget": 1800,
                "proteinTarget": 150,
            }
        ],
    })

    print("Seeded emulator successfully.")


if __name__ == "__main__":
    seed()
