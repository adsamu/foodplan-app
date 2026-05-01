package com.adasa.foodplan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.domain.model.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PowderSearchViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    // Protein powders are ingredients with >= 50g protein per 100g
    val results: StateFlow<List<Ingredient>> = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) ingredientRepository.getAllIngredients()
            else ingredientRepository.searchIngredients(query)
        }
        .map { list -> list.filter { it.proteinPer100g >= 50.0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(query: String) { searchQuery.value = query }
}