package com.adasa.foodplan.data.remote

import com.adasa.foodplan.domain.model.RecipeRating
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRatingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun collection(userId: String) =
        firestore.collection("users").document(userId).collection("ratings")

    fun getAllRatings(userId: String): Flow<List<RecipeRating>> = callbackFlow {
        val registration = collection(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val ratings = snapshot?.documents?.mapNotNull { it.toRecipeRating() } ?: emptyList()
            trySend(ratings)
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveRating(userId: String, rating: RecipeRating) {
        collection(userId).document(rating.recipeId).set(rating.toFirestoreMap()).await()
    }
}
