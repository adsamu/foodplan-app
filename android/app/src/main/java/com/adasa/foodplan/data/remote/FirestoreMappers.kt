package com.adasa.foodplan.data.remote

import com.adasa.foodplan.domain.model.ComponentCategory
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.IngredientCategory
import com.adasa.foodplan.domain.model.IngredientSource
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeIngredient
import com.adasa.foodplan.domain.model.RecipeRating
import com.adasa.foodplan.domain.model.RecipeType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date

// ── Recipe ────────────────────────────────────────────────────────────────

internal fun DocumentSnapshot.toRecipe(): Recipe? {
    val data = data ?: return null
    return Recipe(
        id = id,
        name = data["name"] as? String ?: "",
        type = (data["type"] as? String)?.let { runCatching { RecipeType.valueOf(it) }.getOrNull() }
            ?: RecipeType.MEAL,
        mealCategories = (data["mealCategories"] as? List<*>)
            ?.mapNotNull { (it as? String)?.let { v -> runCatching { MealCategory.valueOf(v) }.getOrNull() } }
            ?.toSet() ?: emptySet(),
        componentCategory = (data["componentCategory"] as? String)
            ?.let { runCatching { ComponentCategory.valueOf(it) }.getOrNull() },
        ingredients = (data["ingredients"] as? List<*>)
            ?.mapNotNull { (it as? Map<*, *>)?.toRecipeIngredient() } ?: emptyList(),
        steps = (data["steps"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        notes = data["notes"] as? String ?: ""
    )
}

internal fun Recipe.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "type" to type.name,
    "mealCategories" to mealCategories.map { it.name },
    "componentCategory" to componentCategory?.name,
    "ingredients" to ingredients.map { it.toFirestoreMap() },
    "steps" to steps,
    "notes" to notes
)

private fun Map<*, *>.toRecipeIngredient(): RecipeIngredient? {
    val ingredientId = this["ingredientId"] as? String
    val subRecipeId = this["subRecipeId"] as? String
    val grams = (this["grams"] as? Number)?.toDouble()
    val portions = (this["portions"] as? Number)?.toDouble()
    if ((ingredientId == null) == (subRecipeId == null)) return null
    return runCatching {
        RecipeIngredient(
            ingredientId = ingredientId,
            subRecipeId = subRecipeId,
            grams = grams,
            portions = portions
        )
    }.getOrNull()
}

private fun RecipeIngredient.toFirestoreMap(): Map<String, Any?> = mapOf(
    "ingredientId" to ingredientId,
    "subRecipeId" to subRecipeId,
    "grams" to grams,
    "portions" to portions
)

// ── Ingredient ────────────────────────────────────────────────────────────

internal fun DocumentSnapshot.toIngredient(): Ingredient? {
    val data = data ?: return null
    return Ingredient(
        id = id,
        name = data["name"] as? String ?: "",
        category = (data["category"] as? String)
            ?.let { runCatching { IngredientCategory.valueOf(it) }.getOrNull() }
            ?: IngredientCategory.OTHER,
        kcalPer100g = (data["kcalPer100g"] as? Number)?.toDouble() ?: 0.0,
        proteinPer100g = (data["proteinPer100g"] as? Number)?.toDouble() ?: 0.0,
        fatPer100g = (data["fatPer100g"] as? Number)?.toDouble() ?: 0.0,
        carbsPer100g = (data["carbsPer100g"] as? Number)?.toDouble() ?: 0.0,
        source = (data["source"] as? String)
            ?.let { runCatching { IngredientSource.valueOf(it) }.getOrNull() }
            ?: IngredientSource.LABEL,
        steps = (data["steps"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    )
}

internal fun Ingredient.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "category" to category.name,
    "kcalPer100g" to kcalPer100g,
    "proteinPer100g" to proteinPer100g,
    "fatPer100g" to fatPer100g,
    "carbsPer100g" to carbsPer100g,
    "source" to source.name,
    "steps" to steps
)

// ── RecipeRating ──────────────────────────────────────────────────────────

internal fun DocumentSnapshot.toRecipeRating(): RecipeRating? {
    val data = data ?: return null
    return RecipeRating(
        recipeId = id,
        stars = (data["stars"] as? Number)?.toInt(),
        timesScheduled = (data["timesScheduled"] as? Number)?.toInt() ?: 0,
        timesManuallyRemoved = (data["timesManuallyRemoved"] as? Number)?.toInt() ?: 0,
        isPinned = data["isPinned"] as? Boolean ?: false,
        isExcluded = data["isExcluded"] as? Boolean ?: false,
        lastScheduledDate = (data["lastScheduledDate"] as? Timestamp)?.toLocalDate()
            ?: (data["lastScheduledDate"] as? String)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    )
}

internal fun RecipeRating.toFirestoreMap(): Map<String, Any?> = mapOf(
    "stars" to stars,
    "timesScheduled" to timesScheduled,
    "timesManuallyRemoved" to timesManuallyRemoved,
    "isPinned" to isPinned,
    "isExcluded" to isExcluded,
    "lastScheduledDate" to lastScheduledDate?.toString()
)

private fun Timestamp.toLocalDate(): LocalDate =
    Date(seconds * 1000 + nanoseconds / 1_000_000).toInstant()
        .let { kotlinx.datetime.Instant.fromEpochMilliseconds(it.toEpochMilli()) }
        .toLocalDateTime(TimeZone.UTC)
        .date
