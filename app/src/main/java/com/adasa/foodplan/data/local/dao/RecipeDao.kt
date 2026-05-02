package com.adasa.foodplan.data.local.dao

import androidx.room.*
import com.adasa.foodplan.data.local.entity.RecipeEntity
import com.adasa.foodplan.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE type = 'MEAL' ORDER BY name ASC")
    suspend fun getAllMealRecipesOnce(): List<RecipeEntity>
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE type = 'MEAL' ORDER BY name ASC")
    fun getMealRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE type = 'COMPONENT' ORDER BY name ASC")
    fun getComponentRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): RecipeEntity?

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getIngredientsForRecipe(recipeId: String): List<RecipeIngredientEntity>

    @Upsert
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Upsert
    suspend fun upsertRecipeIngredients(ingredients: List<RecipeIngredientEntity>)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: String)

    @Query("DELETE FROM recipe_ingredients WHERE ingredientId = :ingredientId")
    suspend fun deleteRecipeIngredientsByIngredientId(ingredientId: String)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)
}