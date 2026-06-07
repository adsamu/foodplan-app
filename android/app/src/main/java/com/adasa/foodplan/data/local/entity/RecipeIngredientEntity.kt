package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeIngredientEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val ingredientId: String?,
    val subRecipeId: String?,
    val grams: Double?,
    val portions: Double?
)