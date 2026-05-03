package com.adasa.foodplan.domain.model

data class ShoppingList(
    val period: ShoppingPeriod,
    val categories: List<ShoppingCategory>
) {
    val totalItems: Int get() = categories.sumOf { it.items.size }
}

data class ShoppingPeriod(
    val startDate: kotlinx.datetime.LocalDate,
    val endDate:   kotlinx.datetime.LocalDate,
    /** All recipes in this period, for the filter selector */
    val recipes: List<SelectableRecipe>
) {
    val recipeNames: List<String> get() = recipes.map { it.name }
}

/** A recipe that can be toggled in/out of the shopping list */
data class SelectableRecipe(
    val id:   String,
    val name: String
)

data class ShoppingCategory(
    val name:  String,
    val emoji: String,
    val items: List<ShoppingItem>
)

/** Per-recipe breakdown: how many grams of this ingredient does recipe X need */
data class RecipeContribution(
    val recipeId:   String,
    val recipeName: String,
    val grams:      Double
)

data class ShoppingItem(
    val ingredientId:    String,
    val name:            String,
    val totalGrams:      Double,
    val unit:            ShoppingUnit,
    /** Non-empty: each recipe that uses this ingredient + how many grams */
    val contributions:   List<RecipeContribution>
) {
    /** Convenience — recipe names only, for legacy use */
    val usedInRecipes: List<String> get() = contributions.map { it.recipeName }.distinct().sorted()
}

enum class ShoppingUnit { GRAMS, PIECES, DECILITERS }