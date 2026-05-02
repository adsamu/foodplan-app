package com.adasa.foodplan.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecipeListTab { MEALS, COMPONENTS, INGREDIENTS }

sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState
    data object Empty   : RecipeListUiState
    data class Success(
        val meals:               List<Recipe>,
        val components:          List<Recipe>,
        val ingredients:         List<Ingredient>,
        val ingredientCategories: List<String>,
    ) : RecipeListUiState
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository:     RecipeRepository,
    private val ingredientRepository: IngredientRepository,
) : ViewModel() {

    val searchQuery         = MutableStateFlow("")
    val activeTab           = MutableStateFlow(RecipeListTab.MEALS)
    val activeMealCat       = MutableStateFlow<MealCategory?>(null)
    val activeIngredientCat = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RecipeListUiState> = combine(
        recipeRepository.getAllRecipes(),
        ingredientRepository.getAllIngredients(),
        searchQuery,
        activeMealCat,
        activeIngredientCat,
    ) { recipes, ingredients, query, mealCat, ingCat ->

        val filteredRecipes = recipes.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        val meals           = filteredRecipes
            .filter { it.type == RecipeType.MEAL }
            .filter { mealCat == null || mealCat in it.mealCategories }
        val components      = filteredRecipes.filter { it.type == RecipeType.COMPONENT }
        val filteredIngs    = ingredients
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .filter { ingCat == null || it.category == ingCat }
        val categories      = ingredients.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()

        if (meals.isEmpty() && components.isEmpty() && filteredIngs.isEmpty()) RecipeListUiState.Empty
        else RecipeListUiState.Success(meals, components, filteredIngs, categories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeListUiState.Loading)

    fun onSearchQueryChange(query: String) { searchQuery.value = query }
    fun onTabChange(tab: RecipeListTab) {
        activeTab.value = tab
        activeMealCat.value       = null
        activeIngredientCat.value = null
    }
    fun onMealCatChange(cat: MealCategory?)  { activeMealCat.value = cat }
    fun onIngredientCatChange(cat: String?)  { activeIngredientCat.value = cat }
    fun deleteRecipe(recipe: Recipe) { viewModelScope.launch { recipeRepository.deleteRecipe(recipe) } }
}