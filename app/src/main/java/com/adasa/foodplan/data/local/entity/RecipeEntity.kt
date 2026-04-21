package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adasa.foodplan.domain.model.ComponentCategory
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeType

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: RecipeType,
    val mealCategories: Set<MealCategory>,
    val componentCategory: ComponentCategory?,
    val steps: List<String> = emptyList(),
    val notes: String
) {
    fun toDomain() = Recipe(
        id = id,
        name = name,
        type = type,
        mealCategories = mealCategories,
        componentCategory = componentCategory,
        ingredients = emptyList(),
        steps = steps,
        notes = notes
    )
}

fun Recipe.toEntity() = RecipeEntity(
    id = id,
    name = name,
    type = type,
    mealCategories = mealCategories,
    componentCategory = componentCategory,
    steps = steps,
    notes = notes
)