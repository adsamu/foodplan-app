package com.adasa.foodplan.domain.model

data class Recipe(
    val id: String = "",
    val name: String = "",
    val type: RecipeType = RecipeType.MEAL,
    val mealCategories: Set<MealCategory> = emptySet(),
    val componentCategory: ComponentCategory? = null,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val notes: String = ""
)

enum class RecipeType(val displayName: String) {
    MEAL("Måltid"),
    COMPONENT("Komponent")
}

enum class MealCategory(val displayName: String) {
    BREAKFAST("Frukost"),
    LUNCH("Lunch"),
    DINNER("Middag"),
    SNACK("Mellanmål")
}

enum class ComponentCategory(val displayName: String) {
    SAUCE("Sås"),
    DRESSING("Dressing"),
    SALSA("Salsa"),
    SALAD("Sallad"),
    SIDE("Tillbehör"),
    OTHER("Övrigt")
}