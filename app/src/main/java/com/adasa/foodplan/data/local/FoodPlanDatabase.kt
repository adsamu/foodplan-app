package com.adasa.foodplan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adasa.foodplan.data.local.dao.IngredientDao
import com.adasa.foodplan.data.local.dao.MealPlanDao
import com.adasa.foodplan.data.local.dao.OptimizerSettingsDao
import com.adasa.foodplan.data.local.dao.RecipeDao
import com.adasa.foodplan.data.local.dao.RecipeRatingDao
import com.adasa.foodplan.data.local.entity.DayPlanEntity
import com.adasa.foodplan.data.local.entity.IngredientEntity
import com.adasa.foodplan.data.local.entity.MealPlanEntity
import com.adasa.foodplan.data.local.entity.MealSlotEntity
import com.adasa.foodplan.data.local.entity.RecipeEntity
import com.adasa.foodplan.data.local.entity.RecipeIngredientEntity
import com.adasa.foodplan.data.local.entity.RecipeRatingEntity
import com.adasa.foodplan.data.local.entity.OptimizerRuleEntity
import com.adasa.foodplan.data.local.entity.MealSlotConfigEntity
import com.adasa.foodplan.data.local.entity.BatchCookingGroupEntity

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        MealPlanEntity::class,
        DayPlanEntity::class,
        MealSlotEntity::class,
        RecipeRatingEntity::class,
        OptimizerRuleEntity::class,
        MealSlotConfigEntity::class,
        BatchCookingGroupEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FoodPlanDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun recipeRatingDao(): RecipeRatingDao      // new
    abstract fun optimizerSettingsDao(): OptimizerSettingsDao
}