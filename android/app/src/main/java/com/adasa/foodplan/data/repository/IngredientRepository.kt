package com.adasa.foodplan.data.repository

import com.adasa.foodplan.data.local.dao.IngredientDao
import com.adasa.foodplan.data.local.entity.toEntity
import com.adasa.foodplan.data.remote.toFirestoreMap
import com.adasa.foodplan.data.remote.toIngredient
import com.adasa.foodplan.domain.model.Ingredient
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngredientRepository @Inject constructor(
    private val dao: IngredientDao,
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("ingredients")
    private var listenerRegistration: ListenerRegistration? = null

    /**
     * Subscribes to the global ingredients collection and mirrors every change
     * into Room. Idempotent — subsequent calls replace the previous listener.
     */
    fun startListening(scope: CoroutineScope) {
        listenerRegistration?.remove()
        listenerRegistration = collection.addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                when (change.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        val ingredient = change.document.toIngredient() ?: return@forEach
                        scope.launch { dao.upsertIngredient(ingredient.toEntity()) }
                    }
                    DocumentChange.Type.REMOVED -> {
                        val id = change.document.id
                        scope.launch { dao.deleteIngredientById(id) }
                    }
                }
            }
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

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

    suspend fun saveIngredient(ingredient: Ingredient) {
        collection.document(ingredient.id).set(ingredient.toFirestoreMap()).await()
    }

    suspend fun saveIngredients(ingredients: List<Ingredient>) {
        val batch = firestore.batch()
        ingredients.forEach { ingredient ->
            batch.set(collection.document(ingredient.id), ingredient.toFirestoreMap())
        }
        batch.commit().await()
    }

    suspend fun deleteIngredient(ingredient: Ingredient) {
        collection.document(ingredient.id).delete().await()
    }
}
