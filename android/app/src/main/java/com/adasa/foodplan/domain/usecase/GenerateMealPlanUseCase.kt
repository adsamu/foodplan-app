package com.adasa.foodplan.domain.usecase

import com.adasa.foodplan.data.remote.SettingsSync
import com.adasa.foodplan.data.repository.MealPlanRepository
import com.adasa.foodplan.domain.model.MealPlan
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GenerateMealPlanUseCase @Inject constructor(
    private val optimizer: RemoteMealPlanOptimizer,
    private val mealPlanRepository: MealPlanRepository,
    private val settingsSync: SettingsSync
) {
    suspend operator fun invoke(
        userId: String,
        startDate: LocalDate
    ): Result<MealPlan> = try {
        settingsSync.pushNow(userId)
        val planId = optimizer.generate(userId, startDate)
        val plan = mealPlanRepository.getMealPlanWithDays(planId)
            ?: error("Plan not found after generation")
        Result.success(plan)
    } catch (e: Exception) {
        Result.failure(e)
    }
}