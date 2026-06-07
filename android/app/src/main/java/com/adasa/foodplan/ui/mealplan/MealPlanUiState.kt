package com.adasa.foodplan.ui.mealplan

import com.adasa.foodplan.domain.model.MealCategory
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

enum class PlanView { DAY, WEEK, MONTH }
enum class DayType  { WEEKDAY, WEEKEND, SUNDAY }

// ─── Day ────────────────────────────────────────────────────────────────────

data class MealSlotUi(
    val type: MealCategory,
    val recipeId: String,
    val recipeName: String,
    val kcal: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

data class DayNutrition(
    val kcal: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

data class DayUiState(
    val date: LocalDate,
    val dayType: DayType,
    val kcalTarget: Int,
    val meals: List<MealSlotUi>,
    val proteinPowderGrams: Double,
    val nutrition: DayNutrition,
    val daysUntilShopping: Int?,
    val proteinPowderDaysLeft: Double?
)

// ─── Week ────────────────────────────────────────────────────────────────────

data class WeekDayUi(
    val date: LocalDate,
    val isToday: Boolean,
    val isHighCal: Boolean,
    val isShoppingDay: Boolean,
    val kcal: Double,
    val kcalTarget: Int,
    val mealNames: List<String>,
    val meals: List<MealSlotUi> = emptyList(),
    val checkedCount: Int = 0,
)

data class WeekUiState(
    val weekNumber: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<WeekDayUi>,
    val avgKcal: Double,
    val avgProtein: Double,
    val avgFat: Double,
    val avgCarbs: Double,
    val weekTotalKcal: Double,
    val highCalDays: Int,
    val daysUntilShopping: Int?,
    val proteinPowderDaysLeft: Double?,
    val fullDays: Int = 0,
    val halfDays: Int = 0,
    val avgKcalPct: Int = 0,
)

data class MonthDayUi(
    val date: LocalDate,
    val isToday: Boolean,
    val isHighCal: Boolean,
    val isShoppingDay: Boolean,
    val isPlanned: Boolean,
    val checkedCount: Int = 0,
    val totalMeals: Int = 0,
)

data class MonthUiState(
    val month: Month,
    val year: Int,
    val days: List<MonthDayUi>,
    val avgKcal: Double,
    val avgProtein: Double,
    val avgFat: Double,
    val avgCarbs: Double,
    val monthTotalKcal: Double,
    val shoppingDaysCount: Int,
    val highCalDaysCount: Int,
    val proteinPowderDaysLeft: Double?,
    val fullDays: Int = 0,
    val halfDays: Int = 0,
    val avgKcalPct: Int = 0,
)