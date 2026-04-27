package com.adasa.foodplan.domain.model

data class ShoppingList(
    val period: ShoppingPeriod,
    val categories: List<ShoppingCategory>
) {
    val totalItems: Int get() = categories.sumOf { it.items.size }
}

data class ShoppingPeriod(
    val startDate: kotlinx.datetime.LocalDate,
    val endDate: kotlinx.datetime.LocalDate,
    val recipeNames: List<String>   // distinct recipe names in this period
)

data class ShoppingCategory(
    val name: String,
    val emoji: String,
    val items: List<ShoppingItem>
)

data class ShoppingItem(
    val ingredientId: String,
    val name: String,
    val totalGrams: Double,
    val unit: ShoppingUnit,
    val usedInRecipes: List<String>   // recipe names this ingredient appears in
)

enum class ShoppingUnit { GRAMS, PIECES, DECILITERS }