package com.adasa.foodplan.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeNutrition
import com.adasa.foodplan.domain.model.computeNutrition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState
    data object NotFound : RecipeDetailUiState
    data class Success(
        val recipe: Recipe,
        val nutrition: RecipeNutrition,
        val ingredientNames: Map<String, String>
    ) : RecipeDetailUiState
}

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository:     RecipeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState

    private var currentRecipe: Recipe? = null

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = RecipeDetailUiState.Loading
            val recipe = recipeRepository.getRecipeWithIngredients(recipeId)
            if (recipe == null) {
                _uiState.value = RecipeDetailUiState.NotFound
                return@launch
            }
            currentRecipe = recipe

            // Collect the live ingredient list — any name/macro change or deletion
            // immediately recomputes the displayed names and nutrition.
            ingredientRepository.getAllIngredients().collect { allIngredients ->
                val data = computeRecipeData(recipe, allIngredients)
                _uiState.value = RecipeDetailUiState.Success(
                    recipe          = recipe,
                    nutrition       = data.nutrition,
                    ingredientNames = data.ingredientNames
                )
            }
        }
    }

    fun deleteRecipe() {
        val recipe = currentRecipe ?: return
        viewModelScope.launch { recipeRepository.deleteRecipe(recipe) }
    }

    private data class RecipeComputedData(
        val nutrition:       RecipeNutrition,
        val ingredientNames: Map<String, String>
    )

    private suspend fun computeRecipeData(
        recipe:         Recipe,
        allIngredients: List<Ingredient>
    ): RecipeComputedData {
        val ingredientMap = allIngredients.associateBy { it.id }
        val nameMap       = mutableMapOf<String, String>()
        val subNutrition  = mutableMapOf<String, RecipeNutrition>()

        // Raw ingredient names from the live list
        recipe.ingredients.forEach { ri ->
            ri.ingredientId?.let { id -> ingredientMap[id]?.let { nameMap[id] = it.name } }
        }

        // Sub-recipe names and nutrition
        recipe.ingredients.mapNotNull { it.subRecipeId }.distinct().forEach { subId ->
            val sub = recipeRepository.getRecipeWithIngredients(subId) ?: return@forEach
            nameMap[subId]  = sub.name
            subNutrition[subId] = sub.ingredients.computeNutrition(ingredientMap, emptyMap())
        }

        return RecipeComputedData(
            nutrition       = recipe.ingredients.computeNutrition(ingredientMap, subNutrition),
            ingredientNames = nameMap
        )
    }
}