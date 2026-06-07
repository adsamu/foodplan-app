package com.adasa.foodplan.domain.usecase

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate
import javax.inject.Inject

interface RemoteMealPlanOptimizer {
    suspend fun generate(userId: String, startDate: LocalDate): String
}

class FirebaseRemoteMealPlanOptimizer @Inject constructor(
    private val functions: FirebaseFunctions
) : RemoteMealPlanOptimizer {
    override suspend fun generate(userId: String, startDate: LocalDate): String {
        val result = functions
            .getHttpsCallable("optimise_meal_plan")
            .call(
                mapOf(
                    "userId" to userId,
                    "startDate" to startDate.toString()
                )
            )
            .await()
        return (result.data as Map<*, *>)["planId"] as String
    }
}
