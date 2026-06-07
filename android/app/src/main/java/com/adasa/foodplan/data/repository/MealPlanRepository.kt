package com.adasa.foodplan.data.repository

import com.adasa.foodplan.data.local.dao.MealPlanDao
import com.adasa.foodplan.data.local.entity.DayPlanEntity
import com.adasa.foodplan.data.local.entity.MealPlanEntity
import com.adasa.foodplan.data.local.entity.MealSlotEntity
import com.adasa.foodplan.data.remote.toMealPlan
import com.adasa.foodplan.domain.model.DailyGoal
import com.adasa.foodplan.domain.model.DayPlan
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.MealPlan
import com.adasa.foodplan.domain.model.MealSlot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanRepository @Inject constructor(
    private val dao: MealPlanDao,
    private val firestore: FirebaseFirestore
) {
    private var listenerRegistration: ListenerRegistration? = null

    /**
     * Subscribes to users/{userId}/mealPlans and mirrors every plan document to Room.
     * Idempotent — subsequent calls replace the previous listener.
     */
    fun startListening(userId: String, scope: CoroutineScope) {
        listenerRegistration?.remove()
        listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("mealPlans")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.forEach { doc ->
                    val plan = doc.toMealPlan() ?: return@forEach
                    scope.launch { saveMealPlan(plan) }
                }
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

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
            day.meals.forEachIndexed { i, slot ->
                dao.upsertMealSlot(
                    MealSlotEntity(
                        dayPlanId = dayId,
                        type = slot.type,
                        slotIndex = i,
                        recipeId = slot.recipeId
                    )
                )
            }
        }
    }

    suspend fun getDayPlanByDate(date: LocalDate): DayPlan? {
        val entity = dao.getDayPlanByDate(date.toEpochDays().toLong()) ?: return null
        val slots = dao.getMealSlotsForDayPlan(entity.id)
        return entity.toDomain(slots)
    }

    /** Returns a map of date → DayPlan for every day that has a plan in [startDate, endDate]. */
    suspend fun getDayPlansForRange(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, DayPlan> {
        val entities = dao.getDayPlansInRange(
            startEpoch = startDate.toEpochDays().toLong(),
            endEpoch = endDate.toEpochDays().toLong()
        )
        if (entities.isEmpty()) return emptyMap()
        val slots = dao.getMealSlotsForDayPlanIds(entities.map { it.id })
        val slotsByDayId = slots.groupBy { it.dayPlanId }
        return entities.associate { entity ->
            entity.date to entity.toDomain(slotsByDayId[entity.id] ?: emptyList())
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

    suspend fun getRecentPlans(weeks: Int): List<MealPlan> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val from = today.minus(weeks, DateTimeUnit.WEEK)
        val planEntities = dao.getMealPlansInRange(from, today)
        return planEntities.map { entity ->
            val days = dao.getDayPlansForMealPlan(entity.id).map { dayEntity ->
                val slots = dao.getMealSlotsForDayPlan(dayEntity.id)
                dayEntity.toDomain(slots)
            }
            entity.toDomain().copy(days = days)
        }
    }
}