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
        /** Maps each ingredientId / subRecipeId → display name */
        val ingredientNames: Map<String, String>
    ) : RecipeDetailUiState
}

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
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
            val data = computeRecipeData(recipe)
            _uiState.value = RecipeDetailUiState.Success(
                recipe = recipe,
                nutrition = data.nutrition,
                ingredientNames = data.ingredientNames
            )
        }
    }

    fun deleteRecipe() {
        val recipe = currentRecipe ?: return
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
        }
    }

    private data class RecipeComputedData(
        val nutrition: RecipeNutrition,
        val ingredientNames: Map<String, String>
    )

    private suspend fun computeRecipeData(recipe: Recipe): RecipeComputedData {
        val ingredientIds = recipe.ingredients.mapNotNull { it.ingredientId }
        val ingredientMap = mutableMapOf<String, Ingredient>()
        for (id in ingredientIds) {
            ingredientRepository.getIngredientById(id)?.let { ingredientMap[id] = it }
        }
        val subRecipeIds = recipe.ingredients.mapNotNull { it.subRecipeId }
        val subRecipeNutritionMap = mutableMapOf<String, RecipeNutrition>()
        val nameMap = mutableMapOf<String, String>()

        // Names for raw ingredients
        ingredientMap.forEach { (id, ing) -> nameMap[id] = ing.name }

        for (id in subRecipeIds) {
            val subRecipe = recipeRepository.getRecipeWithIngredients(id) ?: continue
            nameMap[id] = subRecipe.name
            val subIngredientIds = subRecipe.ingredients.mapNotNull { it.ingredientId }
            val subIngredientMap = mutableMapOf<String, Ingredient>()
            for (subIngId in subIngredientIds) {
                ingredientRepository.getIngredientById(subIngId)?.let { subIngredientMap[subIngId] = it }
            }
            subRecipeNutritionMap[id] = subRecipe.ingredients.computeNutrition(subIngredientMap, emptyMap())
        }
        return RecipeComputedData(
            nutrition = recipe.ingredients.computeNutrition(ingredientMap, subRecipeNutritionMap),
            ingredientNames = nameMap
        )
    }
}
