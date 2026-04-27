package com.adasa.foodplan.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.domain.model.ShoppingList
import com.adasa.foodplan.domain.usecase.GetShoppingListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

sealed interface ShoppingUiState {
    data object Loading : ShoppingUiState
    data object Empty : ShoppingUiState
    data class Success(
        val shoppingList: ShoppingList,
        val checkedItems: Set<String>   // set of ingredientIds that are checked off
    ) : ShoppingUiState
    data class Error(val message: String) : ShoppingUiState
}

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val getShoppingListUseCase: GetShoppingListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingUiState>(ShoppingUiState.Loading)
    val uiState: StateFlow<ShoppingUiState> = _uiState

    // Checked state is local — not persisted, resets when you leave the screen
    private val checkedItems = mutableSetOf<String>()

    init {
        loadShoppingList()
    }

    fun loadShoppingList(
        startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        endDate: LocalDate = nextShoppingPeriodEnd()
    ) {
        viewModelScope.launch {
            _uiState.value = ShoppingUiState.Loading
            try {
                val list = getShoppingListUseCase(startDate, endDate)
                _uiState.value = if (list.totalItems == 0) {
                    ShoppingUiState.Empty
                } else {
                    ShoppingUiState.Success(list, checkedItems.toSet())
                }
            } catch (e: Exception) {
                _uiState.value = ShoppingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleItem(ingredientId: String) {
        if (ingredientId in checkedItems) checkedItems.remove(ingredientId)
        else checkedItems.add(ingredientId)

        val current = _uiState.value
        if (current is ShoppingUiState.Success) {
            _uiState.value = current.copy(checkedItems = checkedItems.toSet())
        }
    }

    fun uncheckAll() {
        checkedItems.clear()
        val current = _uiState.value
        if (current is ShoppingUiState.Success) {
            _uiState.value = current.copy(checkedItems = emptySet())
        }
    }

    // Returns end of current shopping period based on next shopping day in settings.
    // For now defaults to 6 days ahead (one week). Later wire to settings.
    private fun nextShoppingPeriodEnd(): LocalDate {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return LocalDate(today.year, today.month, today.dayOfMonth + 6)
    }
}