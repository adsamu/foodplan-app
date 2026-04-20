package com.adasa.foodplan.data.repository

import com.adasa.foodplan.data.local.dao.MealPlanDao
import com.adasa.foodplan.data.local.entity.DayPlanEntity
import com.adasa.foodplan.data.local.entity.MealPlanEntity
import com.adasa.foodplan.data.local.entity.MealSlotEntity
import com.adasa.foodplan.domain.model.DailyGoal
import com.adasa.foodplan.domain.model.DayPlan
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.MealPlan
import com.adasa.foodplan.domain.model.MealSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanRepository @Inject constructor(
    private val dao: MealPlanDao
) {
    fun getAllMealPlans(): Flow<List<MealPlan>> =
        dao.getAllMealPlans().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getMealPlanWithDays(id: String): MealPlan? {
        val entity = dao.getMealPlanById(id) ?: return null
        val dayEntities = dao.getDayPlansForMealPlan(id)
        val days = dayEntities.map { dayEntity ->
            val slots = dao.getMealSlotsForDayPlan(dayEntity.id)
            dayEntity.toDomain(slots)
        }
        return MealPlan(
            id = entity.id,
            name = entity.name,
            startDate = entity.startDate,
            endDate = entity.endDate,
            days = days
        )
    }

    suspend fun saveMealPlan(mealPlan: MealPlan) {
        val planId = mealPlan.id.ifEmpty { UUID.randomUUID().toString() }
        dao.upsertMealPlan(
            MealPlanEntity(
                id = planId,
                name = mealPlan.name,
                startDate = mealPlan.startDate,
                endDate = mealPlan.endDate
            )
        )
        mealPlan.days.forEach { day ->
            val dayId = day.id.ifEmpty { UUID.randomUUID().toString() }
            dao.upsertDayPlan(
                DayPlanEntity(
                    id = dayId,
                    mealPlanId = planId,
                    date = day.date,
                    proteinPowderGrams = day.proteinPowderGrams,
                    kcalTarget = day.goal.kcalTarget,
                    proteinTarget = day.goal.proteinTarget
                )
            )
            day.meals.forEach { slot ->
                dao.upsertMealSlot(
                    MealSlotEntity(
                        dayPlanId = dayId,
                        type = slot.type,
                        recipeId = slot.recipeId
                    )
                )
            }
        }
    }

    suspend fun deleteMealPlan(mealPlan: MealPlan) =
        dao.deleteMealPlan(
            MealPlanEntity(
                id = mealPlan.id,
                name = mealPlan.name,
                startDate = mealPlan.startDate,
                endDate = mealPlan.endDate
            )
        )

    private fun MealPlanEntity.toDomain() = MealPlan(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
        days = emptyList() // loaded separately when needed
    )

    private fun DayPlanEntity.toDomain(slots: List<MealSlotEntity>) = DayPlan(
        id = id,
        date = date,
        proteinPowderGrams = proteinPowderGrams,
        goal = DailyGoal(
            kcalTarget = kcalTarget,
            proteinTarget = proteinTarget
        ),
        meals = slots.map {
            MealSlot(
                type = it.type,
                recipeId = it.recipeId
            )
        }
    )
}