package com.adasa.foodplan.data.local.dao

import androidx.room.*
import com.adasa.foodplan.data.local.entity.DayPlanEntity
import com.adasa.foodplan.data.local.entity.MealPlanEntity
import com.adasa.foodplan.data.local.entity.MealSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY startDate ASC")
    fun getAllMealPlans(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE id = :id")
    suspend fun getMealPlanById(id: String): MealPlanEntity?

    @Query("SELECT * FROM day_plans WHERE mealPlanId = :mealPlanId ORDER BY date ASC")
    suspend fun getDayPlansForMealPlan(mealPlanId: String): List<DayPlanEntity>

    @Query("SELECT * FROM meal_slots WHERE dayPlanId = :dayPlanId")
    suspend fun getMealSlotsForDayPlan(dayPlanId: String): List<MealSlotEntity>

    @Upsert
    suspend fun upsertMealPlan(mealPlan: MealPlanEntity)

    @Upsert
    suspend fun upsertDayPlan(dayPlan: DayPlanEntity)

    @Upsert
    suspend fun upsertMealSlot(mealSlot: MealSlotEntity)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlanEntity)
}