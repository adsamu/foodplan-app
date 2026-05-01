package com.adasa.foodplan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.SettingsRepository
import com.adasa.foodplan.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val config: StateFlow<MealPlanConfig?> = repo.getMealPlanConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Schedule ──────────────────────────────────────────────────────────

    fun setMealSlot(day: DayOfWeek, newConfig: DayMealConfig) {
        viewModelScope.launch { repo.setMealSlotConfig(day, newConfig) }
    }

    fun setSnackOptionalFill(enabled: Boolean) {
        viewModelScope.launch { repo.setSnackOptionalFill(enabled) }
    }

    fun saveBatchGroups(groups: List<BatchCookingGroup>) {
        viewModelScope.launch { repo.saveBatchGroups(groups) }
    }

    fun setShoppingDay(day: DayOfWeek) {
        viewModelScope.launch {
            val current = config.value?.shopping?.shoppingDays ?: emptySet()
            val updated = if (day in current) current - day else current + day
            repo.setShoppingDays(updated)
        }
    }

    fun setShoppingInterval(weeks: Int) {
        viewModelScope.launch { repo.setShoppingInterval(weeks) }
    }

    // ── Goals ─────────────────────────────────────────────────────────────

    fun setKcal(value: Double) { viewModelScope.launch { repo.setKcalTarget(value) } }
    fun setProtein(value: Double?) { viewModelScope.launch { repo.setProteinTarget(value) } }
    fun setFat(value: Double?) { viewModelScope.launch { repo.setFatTarget(value) } }
    fun setCarbs(value: Double?) { viewModelScope.launch { repo.setCarbsTarget(value) } }
    fun setAutoField(field: MacroField) { viewModelScope.launch { repo.setAutoField(field) } }

    fun setMinKcal(v: Double?) { viewModelScope.launch { repo.setMinKcal(v) } }
    fun setMaxKcal(v: Double?) { viewModelScope.launch { repo.setMaxKcal(v) } }
    fun setMinProtein(v: Double?) { viewModelScope.launch { repo.setMinProtein(v) } }
    fun setMaxProtein(v: Double?) { viewModelScope.launch { repo.setMaxProtein(v) } }
    fun setMinFat(v: Double?) { viewModelScope.launch { repo.setMinFat(v) } }
    fun setMaxFat(v: Double?) { viewModelScope.launch { repo.setMaxFat(v) } }
    fun setMinCarbs(v: Double?) { viewModelScope.launch { repo.setMinCarbs(v) } }
    fun setMaxCarbs(v: Double?) { viewModelScope.launch { repo.setMaxCarbs(v) } }

    fun setPowder(ingredient: Ingredient, gramsInStock: Double) {
        viewModelScope.launch {
            repo.setProteinPowder(
                ingredientId   = ingredient.id,
                name           = ingredient.name,
                proteinPer100g = ingredient.proteinPer100g,
                kcalPer100g    = ingredient.kcalPer100g,
                gramsInStock   = gramsInStock
            )
        }
    }

    fun setPowderAutoFill(enabled: Boolean) { viewModelScope.launch { repo.setPowderAutoFill(enabled) } }
    fun setPowderLowStockWarning(enabled: Boolean) { viewModelScope.launch { repo.setPowderLowStockWarning(enabled) } }
    fun restockPowder(grams: Double) { viewModelScope.launch { repo.restockPowder(grams) } }

    // ── Diet ─────────────────────────────────────────────────────────────

    fun toggleDietType(type: DietType) {
        viewModelScope.launch {
            val current = config.value?.diet?.dietTypes ?: emptySet()
            repo.setDietTypes(if (type in current) current - type else current + type)
        }
    }

    fun toggleAllergy(allergy: AllergyType) {
        viewModelScope.launch {
            val current = config.value?.diet?.allergies ?: emptySet()
            repo.setAllergies(if (allergy in current) current - allergy else current + allergy)
        }
    }

    fun toggleExcludedIngredient(id: String) {
        viewModelScope.launch {
            val current = config.value?.diet?.excludedIngredientIds ?: emptySet()
            repo.setExcludedIngredients(if (id in current) current - id else current + id)
        }
    }

    fun togglePreferredIngredient(id: String) {
        viewModelScope.launch {
            val current = config.value?.diet?.preferredIngredientIds ?: emptySet()
            repo.setPreferredIngredients(if (id in current) current - id else current + id)
        }
    }

    fun toggleDislikedIngredient(id: String) {
        viewModelScope.launch {
            val current = config.value?.diet?.dislikedIngredientIds ?: emptySet()
            repo.setDislikedIngredients(if (id in current) current - id else current + id)
        }
    }

    // ── Rules ─────────────────────────────────────────────────────────────

    fun addRule(rule: OptimizerRule) {
        viewModelScope.launch {
            repo.upsertRule(rule.copy(id = UUID.randomUUID().toString()))
        }
    }

    fun deleteRule(ruleId: String) {
        viewModelScope.launch { repo.deleteRule(ruleId) }
    }

    fun setVariety(config: VarietyConfig) {
        viewModelScope.launch { repo.setVariety(config) }
    }
}