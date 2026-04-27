package com.adasa.foodplan.domain.model

import kotlinx.datetime.DayOfWeek

data class MealPlanConfig(
    val schedule: MealScheduleConfig,
    val goals: NutritionGoals,
    val diet: DietPreferences,
    val rules: List<OptimizerRule>,
    val variety: VarietyConfig,
    val proteinPowder: ProteinPowder?,
    val shopping: ShoppingConfig
)

// ── Schedule ──────────────────────────────────────────────

data class MealScheduleConfig(
    val mealSlots: Map<DayOfWeek, DayMealConfig>,
    val batchGroups: List<BatchCookingGroup>,
    val snackOptionalFill: Boolean = true
)

data class DayMealConfig(
    val breakfast: Boolean = false,
    val lunch: Boolean = true,
    val dinner: Boolean = true,
    val snackCount: Int = 0
) {
    val isActive: Boolean get() = breakfast || lunch || dinner || snackCount > 0
}

data class BatchCookingGroup(
    val meal: MealCategory,
    val days: Set<DayOfWeek>,
    val batchNumber: Int        // 1, 2, 3 — matches color in the UI
)

// ── Nutrition goals ───────────────────────────────────────

data class NutritionGoals(
    val kcalTarget: Double,
    val proteinTarget: Double?,     // null = auto-calculated
    val fatTarget: Double?,
    val carbsTarget: Double?,
    val autoField: MacroField,      // which macro is derived from the others
    val minKcalPerDay: Double? = null,
    val maxKcalPerDay: Double? = null,
    val minProteinPerDay: Double? = null,
    val maxProteinPerDay: Double? = null,
    val minFatPerDay: Double? = null,
    val maxFatPerDay: Double? = null,
    val minCarbsPerDay: Double? = null,
    val maxCarbsPerDay: Double? = null
) {
    // Resolve the auto field from the others at runtime
    val resolvedProtein: Double get() = proteinTarget ?: ((kcalTarget - (fatTarget ?: 0.0) * 9 - (carbsTarget ?: 0.0) * 4) / 4).coerceAtLeast(0.0)
    val resolvedFat: Double get() = fatTarget ?: ((kcalTarget - (proteinTarget ?: 0.0) * 4 - (carbsTarget ?: 0.0) * 4) / 9).coerceAtLeast(0.0)
    val resolvedCarbs: Double get() = carbsTarget ?: ((kcalTarget - (proteinTarget ?: 0.0) * 4 - (fatTarget ?: 0.0) * 9) / 4).coerceAtLeast(0.0)
}

enum class MacroField { PROTEIN, FAT, CARBS }

// ── Diet preferences ──────────────────────────────────────

data class DietPreferences(
    val dietTypes: Set<DietType> = emptySet(),
    val allergies: Set<AllergyType> = emptySet(),
    val excludedIngredientIds: Set<String> = emptySet(),
    val preferredIngredientIds: Set<String> = emptySet(),
    val dislikedIngredientIds: Set<String> = emptySet()
)

enum class DietType(val displayName: String) {
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    PESCATARIAN("Pescatarian"),
    KETO("Keto")
}

enum class AllergyType(val displayName: String) {
    GLUTEN("Gluten"),
    DAIRY("Dairy"),
    NUTS("Nuts"),
    SHELLFISH("Shellfish"),
    EGGS("Eggs"),
    SOY("Soy"),
    PORK("Pork")
}

// ── Variety ───────────────────────────────────────────────

data class VarietyConfig(
    val maxDaysInARow: Int = 2,
    val uniqueWeeksBeforeRepeat: Int = 3,
    val proteinSourceVariety: Boolean = true
)

// ── Protein powder ────────────────────────────────────────

data class ProteinPowder(
    val ingredientId: String,
    val name: String,
    val proteinPer100g: Double,
    val kcalPer100g: Double,
    val gramsInStock: Double,
    val autoFillGap: Boolean = true,
    val lowStockWarning: Boolean = true
) {
    val daysRemaining: Double get() = gramsInStock / 25.0 // rough estimate, refined by optimizer
}

// ── Shopping ──────────────────────────────────────────────

data class ShoppingConfig(
    val shoppingDays: Set<DayOfWeek>,
    val intervalWeeks: Int = 1
)