package com.adasa.foodplan.ui.ingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.domain.model.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface IngredientDetailUiState {
    data object Loading  : IngredientDetailUiState
    data object NotFound : IngredientDetailUiState
    data class  Success(val ingredient: Ingredient) : IngredientDetailUiState
}

@HiltViewModel
class IngredientDetailViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<IngredientDetailUiState>(IngredientDetailUiState.Loading)
    val uiState: StateFlow<IngredientDetailUiState> = _uiState

    fun loadIngredient(id: String) {
        viewModelScope.launch {
            val ingredient = ingredientRepository.getIngredientById(id)
            _uiState.value = if (ingredient != null)
                IngredientDetailUiState.Success(ingredient)
            else
                IngredientDetailUiState.NotFound
        }
    }

    fun deleteIngredient() {
        val state = _uiState.value as? IngredientDetailUiState.Success ?: return
        viewModelScope.launch {
            ingredientRepository.deleteIngredient(state.ingredient)
        }
    }
}