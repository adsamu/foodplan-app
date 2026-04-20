package com.adasa.foodplan.data.repository

import com.adasa.foodplan.data.local.dao.RecipeDao
import com.adasa.foodplan.data.local.entity.toEntity
import com.adasa.foodplan.data.local.entity.RecipeIngredientEntity
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
) {
    fun getAllRecipes(): Flow<List<Recipe>> =
        recipeDao.getAllRecipes().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getMealRecipes(): Flow<List<Recipe>> =
        recipeDao.getMealRecipes().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getComponentRecipes(): Flow<List<Recipe>> =
        recipeDao.getComponentRecipes().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getRecipeById(id: String): Recipe? =
        recipeDao.getRecipeById(id)?.toDomain()

    suspend fun saveRecipe(recipe: Recipe): Result<Unit> {
        val recipeId = recipe.id.ifEmpty { UUID.randomUUID().toString() }

        for (ingredient in recipe.ingredients) {
            val subId = ingredient.subRecipeId ?: continue
            if (wouldCreateCircularReference(recipeId, subId)) {
                return Result.failure(
                    IllegalArgumentException(
                        "Circular reference detected: recipe '$recipeId' cannot include sub-recipe '$subId'"
                    )
                )
            }
        }

        val entity = recipe.toEntity().copy(id = recipeId)
        val ingredientEntities = recipe.ingredients.map {
            RecipeIngredientEntity(
                id = UUID.randomUUID().toString(),
                recipeId = recipeId,
                ingredientId = it.ingredientId,
                subRecipeId = it.subRecipeId,
                grams = it.grams,
                portions = it.portions
            )
        }
        recipeDao.upsertRecipe(entity)
        recipeDao.deleteRecipeIngredients(recipeId)
        recipeDao.upsertRecipeIngredients(ingredientEntities)
        return Result.success(Unit)
    }

    suspend fun deleteRecipe(recipe: Recipe) =
        recipeDao.deleteRecipe(recipe.toEntity())

    suspend fun getRecipeWithIngredients(id: String): Recipe? {
        val entity = recipeDao.getRecipeById(id) ?: return null
        val ingredientEntities = recipeDao.getIngredientsForRecipe(id)
        return entity.toDomain().copy(
            ingredients = ingredientEntities.map {
                RecipeIngredient(
                    ingredientId = it.ingredientId,
                    subRecipeId = it.subRecipeId,
                    grams = it.grams,
                    portions = it.portions
                )
            }
        )
    }

    private suspend fun wouldCreateCircularReference(recipeId: String, subRecipeId: String): Boolean {
        val visited = mutableSetOf<String>()
        return containsRecipe(recipeId, subRecipeId, visited)
    }

    private suspend fun containsRecipe(
        targetId: String,
        currentId: String,
        visited: MutableSet<String>
    ): Boolean {
        if (currentId == targetId) return true
        if (!visited.add(currentId)) return false
        val ingredients = recipeDao.getIngredientsForRecipe(currentId)
        for (ingredient in ingredients) {
            val subId = ingredient.subRecipeId ?: continue
            if (containsRecipe(targetId, subId, visited)) return true
        }
        return false
    }
}