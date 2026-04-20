package com.adasa.foodplan.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    val uiState: StateFlow<RecipeListUiState> = recipeRepository
        .getAllRecipes()
        .map { recipes ->
            if (recipes.isEmpty()) RecipeListUiState.Empty
            else RecipeListUiState.Success(recipes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecipeListUiState.Loading
        )

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
        }
    }
}

sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState
    data object Empty : RecipeListUiState
    data class Success(val recipes: List<Recipe>) : RecipeListUiState
}