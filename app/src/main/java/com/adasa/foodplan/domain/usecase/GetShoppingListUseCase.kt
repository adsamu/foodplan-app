package com.adasa.foodplan.domain.usecase

import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.MealPlanRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.ShoppingCategory
import com.adasa.foodplan.domain.model.ShoppingItem
import com.adasa.foodplan.domain.model.ShoppingList
import com.adasa.foodplan.domain.model.ShoppingPeriod
import com.adasa.foodplan.domain.model.ShoppingUnit
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetShoppingListUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate
    ): ShoppingList {
        // 1. Fetch all day plans in the range
        val dayPlans = mealPlanRepository.getDayPlansForRange(startDate, endDate)

        // 2. Collect all unique recipe IDs used in this period
        val recipeIds = dayPlans.values
            .flatMap { it.meals }
            .map { it.recipeId }
            .distinct()

        // 3. Fetch all recipes with their full ingredient lists
        val recipes = recipeIds.mapNotNull { id ->
            recipeRepository.getRecipeWithIngredients(id)
        }

        // 4. Build ingredient cache — fetch every ingredient we'll need
        val allIngredientIds = collectAllIngredientIds(recipes)
        val ingredientCache = allIngredientIds.mapNotNull { id ->
            ingredientRepository.getIngredientById(id)
        }.associateBy { it.id }

        // 5. Build recipe cache for sub-recipe lookup
        val subRecipeIds = recipes.flatMap { recipe ->
            recipe.ingredients.mapNotNull { it.subRecipeId }
        }.distinct()
        val subRecipeCache = subRecipeIds.mapNotNull { id ->
            recipeRepository.getRecipeWithIngredients(id)
        }.associateBy { it.id }

        // 6. Aggregate grams per ingredient across all recipes
        // Map of ingredientId -> (totalGrams, Set of recipe names using it)
        val aggregated = mutableMapOf<String, Pair<Double, MutableSet<String>>>()

        recipes.forEach { recipe ->
            // Count how many times this recipe appears in the plan
            val portionCount = dayPlans.values
                .flatMap { it.meals }
                .count { it.recipeId == recipe.id }
                .toDouble()

            expandRecipeIngredients(
                recipe = recipe,
                portionMultiplier = portionCount,
                subRecipeCache = subRecipeCache,
                recipeName = recipe.name,
                onIngredient = { ingredientId, grams, recipeName ->
                    val existing = aggregated[ingredientId]
                    if (existing == null) {
                        aggregated[ingredientId] = Pair(grams, mutableSetOf(recipeName))
                    } else {
                        aggregated[ingredientId] = Pair(
                            existing.first + grams,
                            existing.second.also { it.add(recipeName) }
                        )
                    }
                }
            )
        }

        // 7. Build ShoppingItems from aggregated data
        val items = aggregated.mapNotNull { (ingredientId, data) ->
            val ingredient = ingredientCache[ingredientId] ?: return@mapNotNull null
            val (totalGrams, recipeNames) = data
            ShoppingItem(
                ingredientId = ingredientId,
                name = ingredient.name,
                totalGrams = totalGrams,
                unit = ShoppingUnit.GRAMS,
                usedInRecipes = recipeNames.toList().sorted()
            )
        }

        // 8. Group by category
        val categories = items
            .groupBy { item ->
                ingredientCache[item.ingredientId]?.category ?: "Other"
            }
            .map { (categoryName, categoryItems) ->
                ShoppingCategory(
                    name = categoryName,
                    emoji = emojiForCategory(categoryName),
                    items = categoryItems.sortedBy { it.name }
                )
            }
            .sortedBy { it.name }

        return ShoppingList(
            period = ShoppingPeriod(
                startDate = startDate,
                endDate = endDate,
                recipeNames = recipes.map { it.name }.distinct().sorted()
            ),
            categories = categories
        )
    }

    /**
     * Recursively collect all leaf ingredient IDs from a recipe,
     * expanding sub-recipes as needed.
     */
    private fun collectAllIngredientIds(
        recipes: List<Recipe>,
        subRecipeCache: Map<String, Recipe> = emptyMap()
    ): Set<String> {
        val ids = mutableSetOf<String>()
        recipes.forEach { recipe ->
            recipe.ingredients.forEach { ri ->
                when {
                    ri.ingredientId != null -> ids.add(ri.ingredientId)
                    ri.subRecipeId != null -> {
                        val sub = subRecipeCache[ri.subRecipeId]
                        if (sub != null) ids.addAll(
                            collectAllIngredientIds(listOf(sub), subRecipeCache)
                        )
                    }
                }
            }
        }
        return ids
    }

    /**
     * Walk a recipe's ingredients, recursing into sub-recipes,
     * and emit (ingredientId, scaledGrams, recipeName) for every leaf ingredient.
     */
    private fun expandRecipeIngredients(
        recipe: Recipe,
        portionMultiplier: Double,
        subRecipeCache: Map<String, Recipe>,
        recipeName: String,
        visited: MutableSet<String> = mutableSetOf(),
        onIngredient: (ingredientId: String, grams: Double, recipeName: String) -> Unit
    ) {
        if (recipe.id in visited) return
        visited.add(recipe.id)

        recipe.ingredients.forEach { ri ->
            when {
                ri.ingredientId != null && ri.grams != null -> {
                    onIngredient(ri.ingredientId, ri.grams * portionMultiplier, recipeName)
                }
                ri.subRecipeId != null && ri.portions != null -> {
                    val subRecipe = subRecipeCache[ri.subRecipeId] ?: return@forEach
                    expandRecipeIngredients(
                        recipe = subRecipe,
                        portionMultiplier = portionMultiplier * ri.portions,
                        subRecipeCache = subRecipeCache,
                        recipeName = recipeName,
                        visited = visited,
                        onIngredient = onIngredient
                    )
                }
            }
        }
    }

    private fun emojiForCategory(category: String): String = when {
        category.contains("kött", ignoreCase = true) ||
                category.contains("fisk", ignoreCase = true) ||
                category.contains("protein", ignoreCase = true) -> "🥩"
        category.contains("mejeri", ignoreCase = true) ||
                category.contains("ägg", ignoreCase = true) -> "🥚"
        category.contains("spannmål", ignoreCase = true) ||
                category.contains("torr", ignoreCase = true) ||
                category.contains("pasta", ignoreCase = true) ||
                category.contains("ris", ignoreCase = true) -> "🌾"
        category.contains("grönsak", ignoreCase = true) ||
                category.contains("frukt", ignoreCase = true) -> "🥦"
        category.contains("konserv", ignoreCase = true) ||
                category.contains("sås", ignoreCase = true) -> "🥫"
        category.contains("fryst", ignoreCase = true) -> "❄️"
        else -> "🛒"
    }
}