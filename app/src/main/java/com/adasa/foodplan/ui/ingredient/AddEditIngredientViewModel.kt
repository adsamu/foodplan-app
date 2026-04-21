package com.adasa.foodplan.ui.ingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.IngredientSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface AddEditIngredientUiState {
    data object Loading : AddEditIngredientUiState
    data object Ready : AddEditIngredientUiState
    data object Saving : AddEditIngredientUiState
    data class Saved(val ingredientId: String) : AddEditIngredientUiState
    data class Error(val message: String) : AddEditIngredientUiState
}

@HiltViewModel
class AddEditIngredientViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddEditIngredientUiState>(AddEditIngredientUiState.Ready)
    val uiState: StateFlow<AddEditIngredientUiState> = _uiState

    val name = MutableStateFlow("")
    val category = MutableStateFlow("")
    val source = MutableStateFlow(IngredientSource.LABEL)
    val kcal = MutableStateFlow(0.0)
    val protein = MutableStateFlow(0.0)
    val fat = MutableStateFlow(0.0)
    val carbs = MutableStateFlow(0.0)

    private var editingIngredientId: String? = null

    fun loadIngredient(ingredientId: String?) {
        if (ingredientId == null) {
            _uiState.value = AddEditIngredientUiState.Ready
            return
        }
        viewModelScope.launch {
            _uiState.value = AddEditIngredientUiState.Loading
            val ingredient = ingredientRepository.getIngredientById(ingredientId)
            if (ingredient == null) {
                _uiState.value = AddEditIngredientUiState.Error("Ingredient not found")
                return@launch
            }
            editingIngredientId = ingredient.id
            name.value = ingredient.name
            category.value = ingredient.category
            source.value = ingredient.source
            kcal.value = ingredient.kcalPer100g
            protein.value = ingredient.proteinPer100g
            fat.value = ingredient.fatPer100g
            carbs.value = ingredient.carbsPer100g
            _uiState.value = AddEditIngredientUiState.Ready
        }
    }

    fun onNameChange(value: String) { name.value = value }
    fun onCategoryChange(value: String) { category.value = value }
    fun onSourceChange(value: IngredientSource) { source.value = value }
    fun onKcalChange(value: Double) { kcal.value = value }
    fun onProteinChange(value: Double) { protein.value = value }
    fun onFatChange(value: Double) { fat.value = value }
    fun onCarbsChange(value: Double) { carbs.value = value }

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
                source = source.value
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
