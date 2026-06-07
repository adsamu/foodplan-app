package com.adasa.foodplan.data.repository

import com.adasa.foodplan.data.local.dao.RecipeRatingDao
import com.adasa.foodplan.data.local.entity.toEntity
import com.adasa.foodplan.domain.model.RecipeRating
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRatingRepository @Inject constructor(
    private val dao: RecipeRatingDao
) {
    fun getAllRatings(): Flow<List<RecipeRating>> =
        dao.getAllRatings().map { it.map { e -> e.toDomain() } }

    suspend fun getRatingForRecipe(recipeId: String): RecipeRating? =
        dao.getRatingForRecipe(recipeId)?.toDomain()

    suspend fun setStars(recipeId: String, stars: Int) {
        val existing = dao.getRatingForRecipe(recipeId)
        val updated = existing?.copy(stars = stars)
            ?: RecipeRating(recipeId = recipeId, stars = stars).toEntity()
        dao.upsertRating(updated as? com.adasa.foodplan.data.local.entity.RecipeRatingEntity
            ?: RecipeRating(recipeId = recipeId, stars = stars).toEntity())
    }

    suspend fun upsertRating(rating: RecipeRating) =
        dao.upsertRating(rating.toEntity())

    suspend fun recordScheduled(recipeId: String, date: LocalDate) =
        dao.incrementScheduled(recipeId, date.toEpochDays().toLong())

    suspend fun recordManuallyRemoved(recipeId: String) =
        dao.incrementManuallyRemoved(recipeId)

    suspend fun setPinned(recipeId: String, pinned: Boolean) {
        val existing = dao.getRatingForRecipe(recipeId)
            ?: RecipeRating(recipeId = recipeId).toEntity()
        dao.upsertRating(existing.copy(isPinned = pinned))
    }

    suspend fun setExcluded(recipeId: String, excluded: Boolean) {
        val existing = dao.getRatingForRecipe(recipeId)
            ?: RecipeRating(recipeId = recipeId).toEntity()
        dao.upsertRating(existing.copy(isExcluded = excluded))
    }
}