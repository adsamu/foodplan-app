package com.adasa.foodplan.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.IngredientCategory
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecipeListTab { MEALS, COMPONENTS, INGREDIENTS }

sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState
    data object Empty   : RecipeListUiState
    data class Success(
        val meals:                List<Recipe>,
        val components:           List<Recipe>,
        val ingredients:          List<Ingredient>,
        val ingredientCategories: List<IngredientCategory>,
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
    val activeIngredientCat = MutableStateFlow<IngredientCategory?>(null)

    // Recipe IDs that contain ingredients whose name matches the query.
    // Returns empty list when query is blank (we show all recipes in that case).
    private val recipeIdsWithMatchingIngredient: kotlinx.coroutines.flow.Flow<List<String>> =
        searchQuery.flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else recipeRepository.getRecipeIdsContainingIngredient(query)
        }

    // Pre-combine query + ingredient-match IDs into a single flow so we stay
    // within the 5-parameter limit of kotlinx.coroutines combine.
    private val queryWithMatches = combine(searchQuery, recipeIdsWithMatchingIngredient) { q, ids ->
        Pair(q, ids.toHashSet())
    }

    val uiState: StateFlow<RecipeListUiState> = combine(
        recipeRepository.getAllRecipes(),
        ingredientRepository.getAllIngredients(),
        queryWithMatches,
        activeMealCat,
        activeIngredientCat,
    ) { recipes, ingredients, (query, ingredientMatchIds), mealCat, ingCat ->

        fun Recipe.matchesQuery(): Boolean {
            if (query.isBlank()) return true
            if (name.contains(query, ignoreCase = true)) return true
            return id in ingredientMatchIds
        }

        val meals = recipes
            .filter { it.type == RecipeType.MEAL }
            .filter { it.matchesQuery() }
            .filter { mealCat == null || mealCat in it.mealCategories }

        val components = recipes
            .filter { it.type == RecipeType.COMPONENT }
            .filter { it.matchesQuery() }

        val filteredIngs = ingredients
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .filter { ingCat == null || it.category == ingCat }

        val usedCategories = ingredients
            .map { it.category }
            .distinct()
            .sortedBy { it.ordinal }

        if (meals.isEmpty() && components.isEmpty() && filteredIngs.isEmpty()) RecipeListUiState.Empty
        else RecipeListUiState.Success(meals, components, filteredIngs, usedCategories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeListUiState.Loading)

    fun onSearchQueryChange(query: String) { searchQuery.value = query }
    fun onTabChange(tab: RecipeListTab) {
        activeTab.value = tab
        activeMealCat.value       = null
        activeIngredientCat.value = null
    }
    fun onMealCatChange(cat: MealCategory?)           { activeMealCat.value = cat }
    fun onIngredientCatChange(cat: IngredientCategory?) { activeIngredientCat.value = cat }
    fun deleteRecipe(recipe: Recipe) { viewModelScope.launch { recipeRepository.deleteRecipe(recipe) } }
}