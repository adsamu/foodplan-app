package com.adasa.foodplan.ui.ingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.IngredientCategory
import com.adasa.foodplan.domain.model.IngredientSource
import com.adasa.foodplan.ui.recipe.StepUi
import com.adasa.foodplan.ui.recipe.deserialiseStep
import com.adasa.foodplan.ui.recipe.serialise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface AddEditIngredientUiState {
    data object Loading : AddEditIngredientUiState
    data object Ready   : AddEditIngredientUiState
    data object Saving  : AddEditIngredientUiState
    data class  Saved(val ingredientId: String) : AddEditIngredientUiState
    data class  Error(val message: String) : AddEditIngredientUiState
}

@HiltViewModel
class AddEditIngredientViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddEditIngredientUiState>(AddEditIngredientUiState.Ready)
    val uiState: StateFlow<AddEditIngredientUiState> = _uiState

    val name     = MutableStateFlow("")
    val category = MutableStateFlow(IngredientCategory.OTHER)
    val source   = MutableStateFlow(IngredientSource.LABEL)
    val kcal     = MutableStateFlow(0.0)
    val protein  = MutableStateFlow(0.0)
    val fat      = MutableStateFlow(0.0)
    val carbs    = MutableStateFlow(0.0)
    val steps    = MutableStateFlow<List<StepUi>>(emptyList())

    private var editingIngredientId: String? = null

    fun loadIngredient(ingredientId: String?) {
        if (ingredientId == null) { _uiState.value = AddEditIngredientUiState.Ready; return }
        if (editingIngredientId == ingredientId) return  // idempotent guard
        viewModelScope.launch {
            _uiState.value = AddEditIngredientUiState.Loading
            val ingredient = ingredientRepository.getIngredientById(ingredientId)
            if (ingredient == null) { _uiState.value = AddEditIngredientUiState.Error("Ingredient not found"); return@launch }
            editingIngredientId = ingredient.id
            name.value     = ingredient.name
            category.value = ingredient.category
            source.value   = ingredient.source
            kcal.value     = ingredient.kcalPer100g
            protein.value  = ingredient.proteinPer100g
            fat.value      = ingredient.fatPer100g
            carbs.value    = ingredient.carbsPer100g
            steps.value    = ingredient.steps.map { it.deserialiseStep() }
            _uiState.value = AddEditIngredientUiState.Ready
        }
    }

    fun onNameChange(value: String)              { name.value     = value }
    fun onCategoryChange(value: IngredientCategory) { category.value = value }
    fun onSourceChange(value: IngredientSource)  { source.value   = value }
    fun onKcalChange(value: Double)              { kcal.value     = value }
    fun onProteinChange(value: Double)           { protein.value  = value }
    fun onFatChange(value: Double)               { fat.value      = value }
    fun onCarbsChange(value: Double)             { carbs.value    = value }

    // ── Steps ─────────────────────────────────────────────────────────────────

    fun addStep() {
        steps.value = steps.value + StepUi.TextStep(UUID.randomUUID().toString(), "")
    }

    fun addTimer(label: String, totalSeconds: Int) {
        steps.value = steps.value + StepUi.TimerStep(UUID.randomUUID().toString(), label, totalSeconds)
    }

    fun updateStep(index: Int, text: String) {
        val list = steps.value.toMutableList()
        val item = list.getOrNull(index) as? StepUi.TextStep ?: return
        list[index] = item.copy(text = text)
        steps.value = list
    }

    fun updateTimer(index: Int, label: String, totalSeconds: Int) {
        val list = steps.value.toMutableList()
        val item = list.getOrNull(index) as? StepUi.TimerStep ?: return
        list[index] = item.copy(label = label, totalSeconds = totalSeconds)
        steps.value = list
    }

    fun removeStep(index: Int) {
        val list = steps.value.toMutableList()
        if (index in list.indices) { list.removeAt(index); steps.value = list }
    }

    fun reorderSteps(fromIndex: Int, toIndex: Int) {
        val list = steps.value.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        list.add(toIndex, list.removeAt(fromIndex))
        steps.value = list
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun saveIngredient() {
        viewModelScope.launch {
            _uiState.value = AddEditIngredientUiState.Saving
            val resolvedId = editingIngredientId ?: UUID.randomUUID().toString()
            val ingredient = Ingredient(
                id = resolvedId,
                name = name.value,
                category = category.value,
                kcalPer100g = kcal.value,
                proteinPer100g = protein.value,
                fatPer100g = fat.value,
                carbsPer100g = carbs.value,
                source = source.value,
                steps = steps.value
                    .filter { it !is StepUi.TextStep || it.text.isNotBlank() }
                    .map { it.serialise() }
            )
            try {
                ingredientRepository.saveIngredient(ingredient)
                _uiState.value = AddEditIngredientUiState.Saved(resolvedId)
            } catch (e: Exception) {
                _uiState.value = AddEditIngredientUiState.Error(e.message ?: "Save failed")
            }
        }
    }
}