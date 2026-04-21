package com.adasa.foodplan.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.RecipeRepository
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

enum class RecipeFilter {
    ALL, MEALS, COMPONENTS, BREAKFAST, LUNCH, DINNER, SNACK
}

sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState
    data object Empty : RecipeListUiState
    data class Success(
        val meals: List<Recipe>,
        val components: List<Recipe>
    ) : RecipeListUiState
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val activeFilter = MutableStateFlow(RecipeFilter.ALL)

    val uiState: StateFlow<RecipeListUiState> = combine(
        recipeRepository.getAllRecipes(),
        searchQuery,
        activeFilter
    ) { recipes, query, filter ->
        val filtered = recipes.filter { recipe ->
            val matchesQuery = query.isBlank() ||
                recipe.name.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                RecipeFilter.ALL -> true
                RecipeFilter.MEALS -> recipe.type == RecipeType.MEAL
                RecipeFilter.COMPONENTS -> recipe.type == RecipeType.COMPONENT
                RecipeFilter.BREAKFAST -> recipe.type == RecipeType.MEAL &&
                    MealCategory.BREAKFAST in recipe.mealCategories
                RecipeFilter.LUNCH -> recipe.type == RecipeType.MEAL &&
                    MealCategory.LUNCH in recipe.mealCategories
                RecipeFilter.DINNER -> recipe.type == RecipeType.MEAL &&
                    MealCategory.DINNER in recipe.mealCategories
                RecipeFilter.SNACK -> recipe.type == RecipeType.MEAL &&
                    MealCategory.SNACK in recipe.mealCategories
            }
            matchesQuery && matchesFilter
        }
        val meals = filtered.filter { it.type == RecipeType.MEAL }
        val components = filtered.filter { it.type == RecipeType.COMPONENT }
        if (meals.isEmpty() && components.isEmpty()) RecipeListUiState.Empty
        else RecipeListUiState.Success(meals = meals, components = components)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeListUiState.Loading
    )

    fun onSearchQueryChange(query: String) { searchQuery.value = query }
    fun onFilterChange(filter: RecipeFilter) { activeFilter.value = filter }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
        }
    }
}