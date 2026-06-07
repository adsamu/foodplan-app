package com.adasa.foodplan.data.remote

import com.adasa.foodplan.domain.model.DailyGoal
import com.adasa.foodplan.domain.model.DayPlan
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.MealPlan
import com.adasa.foodplan.domain.model.MealSlot
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date

internal fun DocumentSnapshot.toMealPlan(): MealPlan? {
    val data = data ?: return null
    val days = (data["days"] as? List<*>)
        ?.mapNotNull { (it as? Map<*, *>)?.toDayPlan() } ?: emptyList()
    return MealPlan(
        id = (data["id"] as? String) ?: id,
        name = data["name"] as? String ?: "",
        startDate = parseDate(data["startDate"]) ?: return null,
        endDate = parseDate(data["endDate"]) ?: return null,
        days = days
    )
}

private fun Map<*, *>.toDayPlan(): DayPlan? {
    val date = parseDate(this["date"]) ?: return null
    val meals = (this["meals"] as? List<*>)
        ?.mapNotNull { (it as? Map<*, *>)?.toMealSlot() } ?: emptyList()
    return DayPlan(
        id = this["id"] as? String ?: "",
        date = date,
        meals = meals,
        proteinPowderGrams = (this["proteinPowderGrams"] as? Number)?.toDouble() ?: 0.0,
        goal = DailyGoal(
            kcalTarget = (this["kcalTarget"] as? Number)?.toInt() ?: 0,
            proteinTarget = (this["proteinTarget"] as? Number)?.toInt() ?: 0
        )
    )
}

private fun Map<*, *>.toMealSlot(): MealSlot? {
    val typeStr = this["type"] as? String ?: return null
    val type = runCatching { MealCategory.valueOf(typeStr) }.getOrNull() ?: return null
    val recipeId = this["recipeId"] as? String ?: return null
    return MealSlot(type = type, recipeId = recipeId)
}

private fun parseDate(value: Any?): LocalDate? = when (value) {
    is String -> runCatching { LocalDate.parse(value) }.getOrNull()
    is Timestamp -> Date(value.seconds * 1000 + value.nanoseconds / 1_000_000)
        .toInstant()
        .let { kotlinx.datetime.Instant.fromEpochMilliseconds(it.toEpochMilli()) }
        .toLocalDateTime(TimeZone.UTC)
        .date
    else -> null
}
