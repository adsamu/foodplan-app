package com.adasa.foodplan.data.remote

import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.MealPlanConfig
import com.adasa.foodplan.domain.model.VarietyConfig
import kotlinx.datetime.DayOfWeek

/**
 * Serialises a [MealPlanConfig] into the shape the Python Cloud Function expects.
 * Field names mirror the keys consumed by `_parse_settings` in functions/main.py.
 */
internal fun MealPlanConfig.toFirestoreMap(): Map<String, Any?> = mapOf(
    "schedule" to mapOf(
        "mealSlots" to schedule.mealSlots.entries.associate { (day, cfg) ->
            day.name to mapOf(
                "breakfast" to cfg.breakfast,
                "lunch" to cfg.lunch,
                "dinner" to cfg.dinner,
                "snackCount" to cfg.snackCount
            )
        },
        "batchGroups" to schedule.batchGroups.map { bg ->
            mapOf(
                "meal" to bg.meal.name,
                "days" to bg.days.map(DayOfWeek::name),
                "batchNumber" to bg.batchNumber
            )
        },
        "snackOptionalFill" to schedule.snackOptionalFill
    ),
    "goals" to mapOf(
        "kcalTarget" to goals.kcalTarget,
        "proteinTarget" to goals.proteinTarget,
        "fatTarget" to goals.fatTarget,
        "carbsTarget" to goals.carbsTarget,
        "autoField" to goals.autoField.name,
        "minKcalPerDay" to goals.minKcalPerDay,
        "maxKcalPerDay" to goals.maxKcalPerDay,
        "minProteinPerDay" to goals.minProteinPerDay,
        "maxProteinPerDay" to goals.maxProteinPerDay,
        "minFatPerDay" to goals.minFatPerDay,
        "maxFatPerDay" to goals.maxFatPerDay,
        "minCarbsPerDay" to goals.minCarbsPerDay,
        "maxCarbsPerDay" to goals.maxCarbsPerDay
    ),
    "diet" to mapOf(
        "dietTypes" to diet.dietTypes.map { it.name },
        "allergies" to diet.allergies.map { it.name },
        "excludedIngredientIds" to diet.excludedIngredientIds.toList(),
        "preferredIngredientIds" to diet.preferredIngredientIds.toList(),
        "dislikedIngredientIds" to diet.dislikedIngredientIds.toList()
    ),
    "variety" to variety.toFirestoreMap(),
    "proteinPowder" to proteinPowder?.let { pp ->
        mapOf(
            "ingredientId" to pp.ingredientId,
            "name" to pp.name,
            "proteinPer100g" to pp.proteinPer100g,
            "kcalPer100g" to pp.kcalPer100g,
            "gramsInStock" to pp.gramsInStock,
            "autoFillGap" to pp.autoFillGap,
            "lowStockWarning" to pp.lowStockWarning
        )
    },
    "shopping" to mapOf(
        "shoppingDays" to shopping.shoppingDays.map { it.name },
        "intervalWeeks" to shopping.intervalWeeks
    ),
    "rules" to rules.map { rule ->
        mapOf(
            "id" to rule.id,
            "type" to rule.type.name,
            "target" to rule.target,
            "targetName" to rule.targetName,
            "constraint" to rule.constraint.name,
            "value" to rule.value
        )
    }
)

private fun VarietyConfig.toFirestoreMap(): Map<String, Any?> = mapOf(
    "level" to level.name,
    "lunchDinnerSharedRecency" to lunchDinnerSharedRecency,
    "breakfastSnackSharedRecency" to breakfastSnackSharedRecency,
    "lunchDinnerMustDiffer" to lunchDinnerMustDiffer,
    "proteinSourceVariety" to proteinSourceVariety,
    "perCategory" to perCategory.entries.associate { (cat: MealCategory, cv) ->
        cat.name to mapOf(
            "maxTimesPerWeek" to cv.maxTimesPerWeek,
            "maxConsecutiveDays" to cv.maxConsecutiveDays
        )
    }
)
