package com.adasa.foodplan.domain.usecase

import com.adasa.foodplan.data.repository.IngredientRepository
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
    private val settingsRepository: SettingsRepository,
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(startDate: LocalDate): Result<MealPlan> {
        return try {
            // 1. Fetch all inputs
            val config = settingsRepository.getMealPlanConfig()
            val history = mealPlanRepository.getRecentPlans(config.variety.uniqueWeeksBeforeRepeat)
            val ratings = ratingRepository.getAllRatings().first()

            // 2. Fetch all recipes (meals + components) — components needed for sub-recipe nutrition
            val allRecipes = recipeRepository.getAllRecipesWithIngredients()
            val mealRecipes = allRecipes.filter { it.type == RecipeType.MEAL }

            // 3. Build ingredient map for nutrition computation
            val ingredients = ingredientRepository.getAllIngredients().first()
            val ingredientMap = ingredients.associateBy { it.id }

            // 4. Pre-compute nutrition for every recipe (resolves sub-recipe references)
            val nutritionMap = computeNutritionMap(allRecipes, ingredientMap)

            // 5. Filter to eligible candidates — only MEAL recipes that pass all hard filters
            val eligibleRecipes = filterRecipes(mealRecipes, ratings, config)

            if (eligibleRecipes.isEmpty()) {
                return Result.failure(
                    IllegalStateException(
                        "No eligible recipes found. Check your dietary settings or add more recipes."
                    )
                )
            }

            // 6. Call the pure optimizer — no I/O inside
            val newPlan = MealPlanOptimizer.generate(
                history = history,
                config = config,
                recipes = eligibleRecipes,
                ratings = ratings,
                nutritionMap = nutritionMap,
                ingredientMap = ingredientMap,
                startDate = startDate
            )

            // 7. Persist and return
            mealPlanRepository.saveMealPlan(newPlan)
            Result.success(newPlan)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Recipe filtering ───────────────────────────────────────────────────

    private fun filterRecipes(
        recipes: List<Recipe>,
        ratings: List<RecipeRating>,
        config: MealPlanConfig
    ): List<Recipe> {
        val ratingMap = ratings.associateBy { it.recipeId }

        return recipes.filter { recipe ->
            val rating = ratingMap[recipe.id]

            // Hard exclude: manually excluded by user
            if (rating?.isExcluded == true) return@filter false

            // Hard exclude: 1-star rating — treated as "never again"
            if (rating?.stars == 1) return@filter false

            // Hard exclude: contains an ingredient the user has globally excluded
            val ingredientIds = recipe.ingredients.mapNotNull { it.ingredientId }.toSet()
            if (config.diet.excludedIngredientIds.any { it in ingredientIds }) return@filter false

            // Must be a MEAL type (components are never scheduled directly)
            if (recipe.type != RecipeType.MEAL) return@filter false

            // Must fit at least one active meal slot in the schedule
            val activeCategories = config.schedule.mealSlots.values
                .filter { it.isActive }
                .flatMap { day ->
                    buildList {
                        if (day.breakfast) add(MealCategory.BREAKFAST)
                        if (day.lunch)     add(MealCategory.LUNCH)
                        if (day.dinner)    add(MealCategory.DINNER)
                        if (day.snackCount > 0) add(MealCategory.SNACK)
                    }
                }.toSet()
            if (recipe.mealCategories.none { it in activeCategories }) return@filter false

            true
        }
    }

    // ── Nutrition pre-computation ──────────────────────────────────────────

    /**
     * Computes RecipeNutrition for every recipe in [allRecipes], resolving sub-recipe
     * references iteratively. Terminates because circular references are prevented at
     * save time in RecipeRepository.
     */
    private fun computeNutritionMap(
        allRecipes: List<Recipe>,
        ingredientMap: Map<String, Ingredient>
    ): Map<String, RecipeNutrition> {
        val result = mutableMapOf<String, RecipeNutrition>()
        val remaining = allRecipes.toMutableList()

        var prevSize = -1
        while (remaining.isNotEmpty() && remaining.size != prevSize) {
            prevSize = remaining.size
            val iterator = remaining.iterator()
            while (iterator.hasNext()) {
                val recipe = iterator.next()
                // Only resolve when all sub-recipe dependencies are already computed
                val allSubRecipesResolved = recipe.ingredients
                    .filter { it.subRecipeId != null }
                    .all { it.subRecipeId in result }
                if (allSubRecipesResolved) {
                    result[recipe.id] = recipe.ingredients.computeNutrition(ingredientMap, result)
                    iterator.remove()
                }
            }
        }
        return result
    }
}