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
    val isActive: Boolean get() = breakfast || lunch || dinner || snackCount != 0
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

/**
 * Controls how aggressively the optimizer avoids repeating recipes.
 *
 * FLEXIBLE — small recency window, low penalty, no hard exclusions
 * BALANCED — moderate window, moderate penalty
 * STRICT   — large window, high penalty, recipes used this week are hard-excluded
 */
enum class VarietyLevel(val displayName: String, val description: String) {
    FLEXIBLE("Flexible", "Recipes can repeat often"),
    BALANCED("Balanced", "Moderate variety across weeks"),
    STRICT("Strict", "This week's recipes are completely blocked next week");

    val recencyWindowWeeks: Int get() = when (this) {
        FLEXIBLE -> 2
        BALANCED -> 4
        STRICT   -> 6
    }
    val recencyWindowDays: Int get() = recencyWindowWeeks * 7
    val penaltyWeight: Double get() = when (this) {
        FLEXIBLE -> 0.2
        BALANCED -> 0.6
        STRICT   -> 1.5
    }
    /** In STRICT mode, recipes within the window are hard-excluded from the pool. */
    val hardExcludeWithinWindow: Boolean get() = this == STRICT
}

/**
 * Per-category repeat limits. Both are batch-exempt: frozen slots are never
 * counted when enforcing these constraints.
 *
 * null = unlimited
 */
data class MealCategoryVariety(
    val maxTimesPerWeek: Int? = null,
    val maxConsecutiveDays: Int? = null
)

data class VarietyConfig(
    val level: VarietyLevel = VarietyLevel.BALANCED,
    val perCategory: Map<MealCategory, MealCategoryVariety> = defaultPerCategory(),
    val lunchDinnerSharedRecency: Boolean = true,
    val breakfastSnackSharedRecency: Boolean = false,
    val lunchDinnerMustDiffer: Boolean = true,
    val proteinSourceVariety: Boolean = true
) {
    /**
     * Derived from the two sharing toggles. Each set of categories shares one
     * recency index in the optimizer — a recipe used in any slot of the group
     * counts against all slots in that group.
     */
    val recencyGroups: List<Set<MealCategory>> get() = buildList {
        if (lunchDinnerSharedRecency) {
            add(setOf(MealCategory.LUNCH, MealCategory.DINNER))
        } else {
            add(setOf(MealCategory.LUNCH))
            add(setOf(MealCategory.DINNER))
        }
        if (breakfastSnackSharedRecency) {
            add(setOf(MealCategory.BREAKFAST, MealCategory.SNACK))
        } else {
            add(setOf(MealCategory.BREAKFAST))
            add(setOf(MealCategory.SNACK))
        }
    }
}

fun defaultPerCategory(): Map<MealCategory, MealCategoryVariety> = mapOf(
    MealCategory.BREAKFAST to MealCategoryVariety(maxTimesPerWeek = null, maxConsecutiveDays = null),
    MealCategory.LUNCH     to MealCategoryVariety(maxTimesPerWeek = 3,    maxConsecutiveDays = 2),
    MealCategory.DINNER    to MealCategoryVariety(maxTimesPerWeek = 2,    maxConsecutiveDays = 2),
    MealCategory.SNACK     to MealCategoryVariety(maxTimesPerWeek = 3,    maxConsecutiveDays = null)
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
    val daysRemaining: Double get() = gramsInStock / 25.0
}

// ── Shopping ──────────────────────────────────────────────

data class ShoppingConfig(
    val shoppingDays: Set<DayOfWeek>,
    val intervalWeeks: Int = 1
)