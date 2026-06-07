package com.adasa.foodplan.data.remote

import com.adasa.foodplan.domain.model.Ingredient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreIngredientRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("ingredients")

    fun getAllIngredients(): Flow<List<Ingredient>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val ingredients = snapshot?.documents?.mapNotNull { it.toIngredient() } ?: emptyList()
            trySend(ingredients)
        }
        awaitClose { registration.remove() }
    }

    suspend fun getIngredientById(id: String): Ingredient? =
        collection.document(id).get().await().toIngredient()

    suspend fun saveIngredient(ingredient: Ingredient) {
        collection.document(ingredient.id).set(ingredient.toFirestoreMap()).await()
    }

    suspend fun deleteIngredient(id: String) {
        collection.document(id).delete().await()
    }
}
