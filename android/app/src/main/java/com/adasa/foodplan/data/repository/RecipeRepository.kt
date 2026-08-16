package com.adasa.foodplan.data.repository

import com.adasa.foodplan.data.local.dao.RecipeDao
import com.adasa.foodplan.data.local.entity.toEntity
import com.adasa.foodplan.data.local.entity.RecipeIngredientEntity
import com.adasa.foodplan.data.remote.toFirestoreMap
import com.adasa.foodplan.data.remote.toRecipe
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeIngredient
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("recipes")
    private var listenerRegistration: ListenerRegistration? = null

    /**
     * Subscribes to the global recipes collection and mirrors every change
     * into Room (including rebuilding the recipe_ingredients join rows from
     * the doc's embedded ingredients array). Idempotent.
     */
    fun startListening(scope: CoroutineScope) {
        listenerRegistration?.remove()
        listenerRegistration = collection.addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                when (change.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        val recipe = change.document.toRecipe() ?: return@forEach
                        scope.launch { mirrorRecipeToRoom(recipe) }
                    }
                    DocumentChange.Type.REMOVED -> {
                        val id = change.document.id
                        scope.launch { recipeDao.deleteRecipeById(id) }
                    }
                }
            }
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    private suspend fun mirrorRecipeToRoom(recipe: Recipe) {
        recipeDao.upsertRecipe(recipe.toEntity())
        recipeDao.deleteRecipeIngredients(recipe.id)
        recipeDao.upsertRecipeIngredients(
            recipe.ingredients.map {
                RecipeIngredientEntity(
                    id = UUID.randomUUID().toString(),
                    recipeId = recipe.id,
                    ingredientId = it.ingredientId,
                    subRecipeId = it.subRecipeId,
                    grams = it.grams,
                    portions = it.portions
                )
            }
        )
    }

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

        val toSave = recipe.copy(id = recipeId)
        collection.document(recipeId).set(toSave.toFirestoreMap()).await()
        return Result.success(Unit)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        collection.document(recipe.id).delete().await()
    }

    /** Remove all recipe_ingredients rows that reference a deleted ingredient. */
    suspend fun deleteRecipeIngredientsByIngredientId(ingredientId: String) =
        recipeDao.deleteRecipeIngredientsByIngredientId(ingredientId)

    fun getRecipeIdsContainingIngredient(query: String) =
        recipeDao.getRecipeIdsContainingIngredient(query)

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

    suspend fun getAllMealRecipesWithIngredients(): List<Recipe> {
        return recipeDao.getAllMealRecipesOnce().map { entity ->
            val ingredients = recipeDao.getIngredientsForRecipe(entity.id).map {
                RecipeIngredient(
                    ingredientId = it.ingredientId,
                    subRecipeId = it.subRecipeId,
                    grams = it.grams,
                    portions = it.portions
                )
            }
            entity.toDomain().copy(ingredients = ingredients)
        }
    }
}
