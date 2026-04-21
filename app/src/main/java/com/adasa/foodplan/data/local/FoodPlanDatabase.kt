package com.adasa.foodplan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adasa.foodplan.data.local.dao.IngredientDao
import com.adasa.foodplan.data.local.dao.MealPlanDao
import com.adasa.foodplan.data.local.dao.RecipeDao
import com.adasa.foodplan.data.local.entity.DayPlanEntity
import com.adasa.foodplan.data.local.entity.IngredientEntity
import com.adasa.foodplan.data.local.entity.MealPlanEntity
import com.adasa.foodplan.data.local.entity.MealSlotEntity
import com.adasa.foodplan.data.local.entity.RecipeEntity
import com.adasa.foodplan.data.local.entity.RecipeIngredientEntity

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        MealPlanEntity::class,
        DayPlanEntity::class,
        MealSlotEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FoodPlanDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
}