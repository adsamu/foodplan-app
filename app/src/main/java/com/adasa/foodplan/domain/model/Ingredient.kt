package com.adasa.foodplan.domain.model

data class Ingredient(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val kcalPer100g: Double = 0.0,
    val proteinPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val source: IngredientSource = IngredientSource.LABEL
)

enum class IngredientSource(val displayName: String) {
    LABEL("Produktetikett"),
    LIVSMEDELSVERKET("Livsmedelsverket"),
    CALCULATED("Beräknat"),
    BARCODE("Streckkod")
}