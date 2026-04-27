package com.adasa.foodplan.domain.usecase

import com.adasa.foodplan.domain.model.*
import kotlinx.datetime.LocalDate

object MealPlanOptimizer {

    fun generate(
        history: List<MealPlan>,
        config: MealPlanConfig,
        recipes: List<Recipe>,
        ratings: List<RecipeRating>,
        startDate: LocalDate
    ): MealPlan {
        // TODO: implement optimizer logic
        // Inputs are fully assembled and validated before this point
        throw NotImplementedError("Optimizer not yet implemented")
    }
}