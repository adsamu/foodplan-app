package com.adasa.foodplan.data.local.dao

import androidx.room.*
import com.adasa.foodplan.data.local.entity.RecipeRatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeRatingDao {
    @Query("SELECT * FROM recipe_ratings")
    fun getAllRatings(): Flow<List<RecipeRatingEntity>>

    @Query("SELECT * FROM recipe_ratings WHERE recipeId = :recipeId")
    suspend fun getRatingForRecipe(recipeId: String): RecipeRatingEntity?

    @Upsert
    suspend fun upsertRating(rating: RecipeRatingEntity)

    @Query("UPDATE recipe_ratings SET timesScheduled = timesScheduled + 1, lastScheduledDate = :date WHERE recipeId = :recipeId")
    suspend fun incrementScheduled(recipeId: String, date: Long)

    @Query("UPDATE recipe_ratings SET timesManuallyRemoved = timesManuallyRemoved + 1 WHERE recipeId = :recipeId")
    suspend fun incrementManuallyRemoved(recipeId: String)

    @Query("SELECT * FROM recipe_ratings WHERE isPinned = 1")
    fun getPinnedRecipes(): Flow<List<RecipeRatingEntity>>

    @Query("SELECT * FROM recipe_ratings WHERE isExcluded = 1")
    fun getExcludedRecipes(): Flow<List<RecipeRatingEntity>>

    @Delete
    suspend fun deleteRating(rating: RecipeRatingEntity)
}