package com.adasa.foodplan.data.repository

import com.adasa.foodplan.data.local.dao.IngredientDao
import com.adasa.foodplan.data.local.entity.toEntity
import com.adasa.foodplan.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngredientRepository @Inject constructor(
    private val dao: IngredientDao
) {
    fun getAllIngredients(): Flow<List<Ingredient>> =
        dao.getAllIngredients().map { entities ->
            entities.map { it.toDomain() }
        }

    fun searchIngredients(query: String): Flow<List<Ingredient>> =
        dao.searchIngredients(query).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getIngredientById(id: String): Ingredient? =
        dao.getIngredientById(id)?.toDomain()

    suspend fun saveIngredient(ingredient: Ingredient) =
        dao.upsertIngredient(ingredient.toEntity())

    suspend fun saveIngredients(ingredients: List<Ingredient>) =
        dao.upsertIngredients(ingredients.map { it.toEntity() })

    suspend fun deleteIngredient(ingredient: Ingredient) =
        dao.deleteIngredient(ingredient.toEntity())
}