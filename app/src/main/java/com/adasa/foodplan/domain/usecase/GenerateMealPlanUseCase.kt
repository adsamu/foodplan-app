package com.adasa.foodplan.domain.usecase

import com.adasa.foodplan.data.repository.MealPlanRepository
import com.adasa.foodplan.data.repository.RecipeRatingRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.data.repository.SettingsRepository
import com.adasa.foodplan.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GenerateMealPlanUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val ratingRepository: RecipeRatingRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(startDate: LocalDate): Result<MealPlan> {
        return try {
            // 1. Fetch all inputs
            val config = settingsRepository.getMealPlanConfig()
            val history = mealPlanRepository.getRecentPlans(config.variety.uniqueWeeksBeforeRepeat)
            val allRecipes = recipeRepository.getAllMealRecipesWithIngredients()
            val ratings = ratingRepository.getAllRatings().first()

            // 2. Pre-filter the recipe pool — only pass valid candidates to the optimizer
            val eligibleRecipes = filterRecipes(allRecipes, ratings, config)

            if (eligibleRecipes.isEmpty()) {
                return Result.failure(IllegalStateException("No eligible recipes found. Check your dietary settings."))
            }

            // 3. Call the pure optimizer (logic to be implemented separately)
            val newPlan = MealPlanOptimizer.generate(
                history = history,
                config = config,
                recipes = eligibleRecipes,
                ratings = ratings,
                startDate = startDate
            )

            Result.success(newPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun filterRecipes(
        recipes: List<Recipe>,
        ratings: List<RecipeRating>,
        config: MealPlanConfig
    ): List<Recipe> {
        val ratingMap = ratings.associateBy { it.recipeId }
        val excludedByUser = ratings.filter { it.isExcluded }.map { it.recipeId }.toSet()

        return recipes.filter { recipe ->
            // 1. Not manually excluded
            if (recipe.id in excludedByUser) return@filter false

            // 2. Not excluded by allergy — check all ingredients recursively
            val ingredientIds = recipe.ingredients.mapNotNull { it.ingredientId }.toSet()
            if (config.diet.excludedIngredientIds.any { it in ingredientIds }) return@filter false

            // 3. Must be a MEAL type (components are never scheduled directly)
            if (recipe.type != RecipeType.MEAL) return@filter false

            // 4. Diet type compatibility — if vegan is set, recipe must be tagged vegan etc.
            // (requires recipe-level diet tags — to be added to Recipe model)
            // For now skip this filter until recipe diet tags are implemented

            true
        }
    }
}