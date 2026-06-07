package com.adasa.foodplan.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

import com.adasa.foodplan.domain.model.IngredientCategory

data class IngredientResultItem(
    val id:            String,
    val name:          String,
    val category:      IngredientCategory,
    val kcalPer100g:   Double,
    val proteinPer100g: Double,
    val fatPer100g:    Double,
    val carbsPer100g:  Double,
)

data class RecipeResultItem(
    val id:        String,
    val name:      String,
    val typeBadge: String
)

@HiltViewModel
class IngredientSearchViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository:     RecipeRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val ingredientResults: StateFlow<List<IngredientResultItem>> = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) ingredientRepository.getAllIngredients()
            else ingredientRepository.searchIngredients(query)
        }
        .map { list ->
            list.map {
                IngredientResultItem(
                    id             = it.id,
                    name           = it.name,
                    category       = it.category,
                    kcalPer100g    = it.kcalPer100g,
                    proteinPer100g = it.proteinPer100g,
                    fatPer100g     = it.fatPer100g,
                    carbsPer100g   = it.carbsPer100g,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipeResults: StateFlow<List<RecipeResultItem>> = combine(
        searchQuery.debounce(300),
        recipeRepository.getComponentRecipes()
    ) { query, recipes ->
        recipes
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .map { RecipeResultItem(it.id, it.name, it.componentCategory?.displayName ?: "Component") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(query: String) { searchQuery.value = query }
}