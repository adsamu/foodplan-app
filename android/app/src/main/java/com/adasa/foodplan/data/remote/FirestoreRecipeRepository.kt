package com.adasa.foodplan.data.remote

import com.adasa.foodplan.domain.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRecipeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("recipes")

    fun getAllRecipes(): Flow<List<Recipe>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val recipes = snapshot?.documents?.mapNotNull { it.toRecipe() } ?: emptyList()
            trySend(recipes)
        }
        awaitClose { registration.remove() }
    }

    suspend fun getRecipeById(id: String): Recipe? =
        collection.document(id).get().await().toRecipe()

    suspend fun saveRecipe(recipe: Recipe) {
        collection.document(recipe.id).set(recipe.toFirestoreMap()).await()
    }

    suspend fun deleteRecipe(id: String) {
        collection.document(id).delete().await()
    }
}
