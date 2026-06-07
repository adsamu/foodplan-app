package com.adasa.foodplan.domain.model

data class RecipeNutrition(
    val kcal: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

data class RecipeIngredient(
    val ingredientId: String? = null,
    val subRecipeId: String? = null,
    val grams: Double? = null,
    val portions: Double? = null
) {
    init {
        require((ingredientId != null) xor (subRecipeId != null)) {
            "Exactly one of ingredientId or subRecipeId must be non-null"
        }
        if (ingredientId != null) {
            require(grams != null) { "grams must be set when ingredientId is set" }
        }
        if (subRecipeId != null) {
            require(portions != null) { "portions must be set when subRecipeId is set" }
        }
    }
}

fun RecipeIngredient.kcal(nutrition: RecipeNutrition?): Double {
    nutrition ?: return 0.0
    return if (ingredientId != null) {
        (grams ?: 0.0) * nutrition.kcal / 100
    } else {
        (portions ?: 0.0) * nutrition.kcal
    }
}

fun RecipeIngredient.protein(nutrition: RecipeNutrition?): Double {
    nutrition ?: return 0.0
    return if (ingredientId != null) {
        (grams ?: 0.0) * nutrition.protein / 100
    } else {
        (portions ?: 0.0) * nutrition.protein
    }
}

fun RecipeIngredient.fat(nutrition: RecipeNutrition?): Double {
    nutrition ?: return 0.0
    return if (ingredientId != null) {
        (grams ?: 0.0) * nutrition.fat / 100
    } else {
        (portions ?: 0.0) * nutrition.fat
    }
}

fun RecipeIngredient.carbs(nutrition: RecipeNutrition?): Double {
    nutrition ?: return 0.0
    return if (ingredientId != null) {
        (grams ?: 0.0) * nutrition.carbs / 100
    } else {
        (portions ?: 0.0) * nutrition.carbs
    }
}

fun List<RecipeIngredient>.computeNutrition(
    ingredientMap: Map<String, Ingredient>,
    subRecipeNutritionMap: Map<String, RecipeNutrition>
): RecipeNutrition {
    var totalKcal = 0.0
    var totalProtein = 0.0
    var totalFat = 0.0
    var totalCarbs = 0.0
    for (ri in this) {
        if (ri.ingredientId != null) {
            val ingredient = ingredientMap[ri.ingredientId]
            val nutrition = ingredient?.let {
                RecipeNutrition(
                    kcal = it.kcalPer100g,
                    protein = it.proteinPer100g,
                    fat = it.fatPer100g,
                    carbs = it.carbsPer100g
                )
            }
            totalKcal += ri.kcal(nutrition)
            totalProtein += ri.protein(nutrition)
            totalFat += ri.fat(nutrition)
            totalCarbs += ri.carbs(nutrition)
        } else if (ri.subRecipeId != null) {
            val nutrition = subRecipeNutritionMap[ri.subRecipeId]
            totalKcal += ri.kcal(nutrition)
            totalProtein += ri.protein(nutrition)
            totalFat += ri.fat(nutrition)
            totalCarbs += ri.carbs(nutrition)
        }
    }
    return RecipeNutrition(
        kcal = totalKcal,
        protein = totalProtein,
        fat = totalFat,
        carbs = totalCarbs
    )
}