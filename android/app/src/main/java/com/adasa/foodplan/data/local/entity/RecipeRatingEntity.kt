package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.adasa.foodplan.domain.model.RecipeRating
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "recipe_ratings",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeRatingEntity(
    @PrimaryKey val recipeId: String,
    val stars: Int?,
    val timesScheduled: Int = 0,
    val timesManuallyRemoved: Int = 0,
    val isPinned: Boolean = false,
    val isExcluded: Boolean = false,
    val lastScheduledDate: LocalDate? = null
) {
    fun toDomain() = RecipeRating(
        recipeId = recipeId,
        stars = stars,
        timesScheduled = timesScheduled,
        timesManuallyRemoved = timesManuallyRemoved,
        isPinned = isPinned,
        isExcluded = isExcluded,
        lastScheduledDate = lastScheduledDate
    )
}

fun RecipeRating.toEntity() = RecipeRatingEntity(
    recipeId = recipeId,
    stars = stars,
    timesScheduled = timesScheduled,
    timesManuallyRemoved = timesManuallyRemoved,
    isPinned = isPinned,
    isExcluded = isExcluded,
    lastScheduledDate = lastScheduledDate
)