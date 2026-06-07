package com.adasa.foodplan.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class MealSlot(
    val type: MealCategory = MealCategory.LUNCH,
    val recipeId: String = ""
)

data class DailyGoal(
    val kcalTarget: Int = 1350,
    val proteinTarget: Int = 120
)

data class DayPlan(
    val id: String = "",
    val date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val meals: List<MealSlot> = emptyList(),
    val proteinPowderGrams: Double = 0.0,
    val goal: DailyGoal = DailyGoal()
)

data class MealPlan(
    val id: String = "",
    val name: String = "",
    val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val days: List<DayPlan> = emptyList()
)