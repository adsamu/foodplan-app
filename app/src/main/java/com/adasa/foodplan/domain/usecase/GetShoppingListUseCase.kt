package com.adasa.foodplan.domain.usecase

import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.MealPlanRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeContribution
import com.adasa.foodplan.domain.model.SelectableRecipe
import com.adasa.foodplan.domain.model.ShoppingCategory
import com.adasa.foodplan.domain.model.ShoppingItem
import com.adasa.foodplan.domain.model.ShoppingList
import com.adasa.foodplan.domain.model.ShoppingPeriod
import com.adasa.foodplan.domain.model.ShoppingUnit
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetShoppingListUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository:   RecipeRepository,
    private val ingredientRepository: IngredientRepository
) {
    /**
     * Build a shopping list for [startDate]..[endDate].
     *
     * @param selectedRecipeIds When non-null, only ingredients from these recipe IDs
     *   are included in the list. The full recipe list in [ShoppingPeriod] always
     *   contains all recipes so the UI can show the selector.
     *   null means all recipes are included.
     */
    suspend operator fun invoke(
        startDate:         LocalDate,
        endDate:           LocalDate,
        selectedRecipeIds: Set<String>? = null
    ): ShoppingList {

        // 1. Fetch all day plans in the range
        val dayPlans = mealPlanRepository.getDayPlansForRange(startDate, endDate)

        // 2. All unique recipe IDs in the period
        val allRecipeIds = dayPlans.values
            .flatMap { it.meals }
            .map { it.recipeId }
            .distinct()

        // 3. Fetch all recipes (full list for the period card selector)
        val allRecipes = allRecipeIds.mapNotNull { id ->
            recipeRepository.getRecipeWithIngredients(id)
        }

        // 4. Apply optional recipe filter
        val activeRecipeIds = selectedRecipeIds ?: allRecipeIds.toSet()
        val activeRecipes   = allRecipes.filter { it.id in activeRecipeIds }

        // 5. Build ingredient + sub-recipe caches
        val allIngredientIds = collectAllIngredientIds(allRecipes)
        val ingredientCache  = allIngredientIds.mapNotNull { id ->
            ingredientRepository.getIngredientById(id)
        }.associateBy { it.id }

        val subRecipeIds = allRecipes
            .flatMap { it.ingredients.mapNotNull { ri -> ri.subRecipeId } }
            .distinct()
        val subRecipeCache = subRecipeIds.mapNotNull { id ->
            recipeRepository.getRecipeWithIngredients(id)
        }.associateBy { it.id }

        // 6. Aggregate: ingredientId -> Map<recipeId, Pair<recipeName, grams>>
        //    Tracking per-recipe so we can show the breakdown on each item row.
        val aggregated = mutableMapOf<String, MutableMap<String, Pair<String, Double>>>()

        activeRecipes.forEach { recipe ->
            val portionCount = dayPlans.values
                .flatMap { it.meals }
                .count { it.recipeId == recipe.id }
                .toDouble().coerceAtLeast(1.0)

            expandRecipeIngredients(
                recipe            = recipe,
                portionMultiplier = portionCount,
                subRecipeCache    = subRecipeCache,
                recipeId          = recipe.id,
                recipeName        = recipe.name
            ) { ingredientId, grams, recipeId, recipeName ->
                val byRecipe = aggregated.getOrPut(ingredientId) { mutableMapOf() }
                val existing = byRecipe[recipeId]
                byRecipe[recipeId] = recipeName to ((existing?.second ?: 0.0) + grams)
            }
        }

        // 7. Build ShoppingItems with per-recipe contributions
        val items = aggregated.mapNotNull { (ingredientId, byRecipe) ->
            val ingredient    = ingredientCache[ingredientId] ?: return@mapNotNull null
            val contributions = byRecipe.entries
                .map { (recipeId, pair) -> RecipeContribution(recipeId, pair.first, pair.second) }
                .sortedBy { it.recipeName }
            ShoppingItem(
                ingredientId  = ingredientId,
                name          = ingredient.name,
                totalGrams    = contributions.sumOf { it.grams },
                unit          = ShoppingUnit.GRAMS,
                contributions = contributions
            )
        }

        // 8. Group by category
        val categories = items
            .groupBy { ingredientCache[it.ingredientId]?.category }
            .map { (category, catItems) ->
                ShoppingCategory(
                    name  = category?.displayName ?: "Other",
                    emoji = category?.emoji ?: "🛒",
                    items = catItems.sortedBy { it.name }
                )
            }
            .sortedBy { it.name }

        return ShoppingList(
            period = ShoppingPeriod(
                startDate = startDate,
                endDate   = endDate,
                recipes   = allRecipes
                    .map { SelectableRecipe(it.id, it.name) }
                    .distinctBy { it.id }
                    .sortedBy { it.name }
            ),
            categories = categories
        )
    }

    private fun collectAllIngredientIds(
        recipes:        List<Recipe>,
        subRecipeCache: Map<String, Recipe> = emptyMap()
    ): Set<String> {
        val ids = mutableSetOf<String>()
        recipes.forEach { recipe ->
            recipe.ingredients.forEach { ri ->
                when {
                    ri.ingredientId != null -> ids.add(ri.ingredientId)
                    ri.subRecipeId  != null -> {
                        val sub = subRecipeCache[ri.subRecipeId]
                        if (sub != null) ids.addAll(collectAllIngredientIds(listOf(sub), subRecipeCache))
                    }
                }
            }
        }
        return ids
    }

    private fun expandRecipeIngredients(
        recipe:            Recipe,
        portionMultiplier: Double,
        subRecipeCache:    Map<String, Recipe>,
        recipeId:          String,
        recipeName:        String,
        visited:           MutableSet<String> = mutableSetOf(),
        onIngredient:      (ingredientId: String, grams: Double, recipeId: String, recipeName: String) -> Unit
    ) {
        if (recipe.id in visited) return
        visited.add(recipe.id)

        recipe.ingredients.forEach { ri ->
            when {
                ri.ingredientId != null && ri.grams != null -> {
                    onIngredient(ri.ingredientId, ri.grams * portionMultiplier, recipeId, recipeName)
                }
                ri.subRecipeId != null && ri.portions != null -> {
                    val sub = subRecipeCache[ri.subRecipeId] ?: return@forEach
                    expandRecipeIngredients(
                        recipe            = sub,
                        portionMultiplier = portionMultiplier * ri.portions,
                        subRecipeCache    = subRecipeCache,
                        recipeId          = recipeId,
                        recipeName        = recipeName,
                        visited           = visited,
                        onIngredient      = onIngredient
                    )
                }
            }
        }
    }
}