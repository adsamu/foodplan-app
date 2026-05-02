package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.IngredientCategory
import com.adasa.foodplan.domain.model.IngredientSource

import androidx.room.ColumnInfo

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val source: String,
    @ColumnInfo(defaultValue = "") val steps: String = ""
) {
    fun toDomain() = Ingredient(
        id = id,
        name = name,
        category = runCatching { IngredientCategory.valueOf(category) }.getOrDefault(IngredientCategory.OTHER),
        kcalPer100g = kcalPer100g,
        proteinPer100g = proteinPer100g,
        fatPer100g = fatPer100g,
        carbsPer100g = carbsPer100g,
        source = IngredientSource.valueOf(source),
        steps = if (steps.isBlank()) emptyList() else steps.split("|||")
    )
}

fun Ingredient.toEntity() = IngredientEntity(
    id             = id,
    name           = name,
    category       = category.name,
    kcalPer100g    = kcalPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g     = fatPer100g,
    carbsPer100g   = carbsPer100g,
    source         = source.name,
    steps          = steps.joinToString("|||")
)