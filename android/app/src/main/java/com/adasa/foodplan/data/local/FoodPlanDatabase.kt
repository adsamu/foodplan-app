package com.adasa.foodplan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FoodPlanDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun recipeRatingDao(): RecipeRatingDao      // new
    abstract fun optimizerSettingsDao(): OptimizerSettingsDao

    companion object {
        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add slotIndex to meal_slots with default 0 and widen the PK to
                // (dayPlanId, type, slotIndex) so multiple snack slots per day are allowed.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS meal_slots_new (
                        dayPlanId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        slotIndex INTEGER NOT NULL DEFAULT 0,
                        recipeId TEXT NOT NULL,
                        PRIMARY KEY(dayPlanId, type, slotIndex),
                        FOREIGN KEY(dayPlanId) REFERENCES day_plans(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO meal_slots_new (dayPlanId, type, slotIndex, recipeId)
                    SELECT dayPlanId, type, 0, recipeId FROM meal_slots
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE meal_slots")
                db.execSQL("ALTER TABLE meal_slots_new RENAME TO meal_slots")
            }
        }
    }
}