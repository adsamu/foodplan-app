package com.adasa.foodplan.domain.model

import kotlinx.datetime.LocalDate

data class RecipeRating(
    val recipeId: String,
    val stars: Int? = null,                    // 1–5, null = unrated
    val timesScheduled: Int = 0,
    val timesManuallyRemoved: Int = 0,
    val isPinned: Boolean = false,
    val isExcluded: Boolean = false,
    val lastScheduledDate: LocalDate? = null
)