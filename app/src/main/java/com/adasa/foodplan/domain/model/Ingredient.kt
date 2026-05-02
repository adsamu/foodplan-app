package com.adasa.foodplan.domain.model

enum class IngredientCategory(val displayName: String, val emoji: String) {
    FRUIT_VEG(      "Fruit & Vegetables",   "🥦"),
    MEAT(           "Meat",                 "🥩"),
    FISH(           "Fish & Seafood",       "🐟"),
    DAIRY_EGGS(     "Dairy & Eggs",         "🥚"),
    CHEESE(         "Cheese",               "🧀"),
    GRAINS(         "Grains & Rice",        "🌾"),
    BREAD_BAKERY(   "Bread & Bakery",       "🍞"),
    DRY_GOODS(      "Dry Goods & Pasta",    "🫙"),
    NUTS(           "Nuts & Seeds",         "🥜"),
    CANNED(         "Canned & Preserved",   "🥫"),
    FROZEN(         "Frozen",               "🧊"),
    OILS_SAUCES(    "Oils & Sauces",        "🫗"),
    SPICES(         "Spices & Herbs",       "🌿"),
    DRINKS(         "Drinks",               "🥤"),
    SUPPLEMENT(     "Supplements",          "💊"),
    OTHER(          "Other",                "📦"),
}

data class Ingredient(
    val id: String = "",
    val name: String = "",
    val category: IngredientCategory = IngredientCategory.OTHER,
    val kcalPer100g: Double = 0.0,
    val proteinPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val source: IngredientSource = IngredientSource.LABEL,
    val steps: List<String> = emptyList(),
)

enum class IngredientSource(val displayName: String) {
    LABEL("Produktetikett"),
    LIVSMEDELSVERKET("Livsmedelsverket"),
    CALCULATED("Beräknat"),
    BARCODE("Streckkod")
}