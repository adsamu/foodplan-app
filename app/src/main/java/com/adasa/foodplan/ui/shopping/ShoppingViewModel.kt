package com.adasa.foodplan.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.domain.model.ShoppingList
import com.adasa.foodplan.domain.model.ShoppingUnit
import com.adasa.foodplan.domain.usecase.GetShoppingListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

// ── Adjustment tracking ───────────────────────────────────────────────────────

data class ShoppingAdjustment(
    val ingredientId:    String,
    val calculatedAmount: Double,
    val adjustedAmount:  Double,
) {
    val delta: Double get() = adjustedAmount - calculatedAmount
}

// ── UI state ──────────────────────────────────────────────────────────────────

data class PendingRegeneration(
    val newStartDate: LocalDate,
    val newEndDate:   LocalDate,
    val conflicts:    List<AdjustmentConflict>
)

data class AdjustmentConflict(
    val ingredientId:  String,
    val name:          String,
    val delta:         Double,
    val unit:          ShoppingUnit,
    val newCalculated: Double,
    val newWithDelta:  Double,
)

sealed interface ShoppingUiState {
    data object Loading : ShoppingUiState

    /** No meals planned for the selected period — but period is preserved so user can adjust. */
    data class Empty(
        val startDate: LocalDate,
        val endDate:   LocalDate,
    ) : ShoppingUiState

    data class Success(
        val shoppingList:   ShoppingList,
        val checkedItems:   Set<String>,
        /** Ingredient IDs in check order — newest at the end, so reversed() = latest first. */
        val checkedOrder:   List<String>,
        val adjustments:    Map<String, ShoppingAdjustment>,
        val expandedItemId: String?,
    ) : ShoppingUiState

    data class Error(val message: String) : ShoppingUiState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val getShoppingListUseCase: GetShoppingListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingUiState>(ShoppingUiState.Loading)
    val uiState: StateFlow<ShoppingUiState> = _uiState

    val pendingRegeneration = MutableStateFlow<PendingRegeneration?>(null)

    // Checked tracking — set for O(1) lookup, list for order (newest = last)
    private val checkedSet  = mutableSetOf<String>()
    private val checkedList = mutableListOf<String>()

    private val adjustments = mutableMapOf<String, ShoppingAdjustment>()

    private var currentStart: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private var currentEnd:   LocalDate = nextShoppingPeriodEnd()

    init { loadShoppingList(currentStart, currentEnd) }

    // ── Loading ───────────────────────────────────────────────────────────────

    fun loadShoppingList(
        startDate: LocalDate = currentStart,
        endDate:   LocalDate = currentEnd,
    ) {
        currentStart = startDate
        currentEnd   = endDate
        viewModelScope.launch {
            _uiState.value = ShoppingUiState.Loading
            try {
                val list = getShoppingListUseCase(startDate, endDate)
                _uiState.value = if (list.totalItems == 0) {
                    ShoppingUiState.Empty(startDate, endDate)
                } else {
                    ShoppingUiState.Success(
                        shoppingList   = list,
                        checkedItems   = checkedSet.toSet(),
                        checkedOrder   = checkedList.toList(),
                        adjustments    = adjustments.toMap(),
                        expandedItemId = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ShoppingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ── Period change — may show conflict dialog ───────────────────────────────

    fun requestPeriodChange(newStart: LocalDate, newEnd: LocalDate) {
        if (adjustments.isEmpty()) {
            loadShoppingList(newStart, newEnd)
            return
        }
        viewModelScope.launch {
            try {
                val newList  = getShoppingListUseCase(newStart, newEnd)
                val allItems = newList.categories.flatMap { it.items }

                val conflicts = adjustments.values.mapNotNull { adj ->
                    val newItem = allItems.find { it.ingredientId == adj.ingredientId }
                        ?: return@mapNotNull null
                    val newCalc = newItem.totalGrams
                    if (newCalc == adj.calculatedAmount) return@mapNotNull null
                    AdjustmentConflict(
                        ingredientId  = adj.ingredientId,
                        name          = newItem.name,
                        delta         = adj.delta,
                        unit          = newItem.unit,
                        newCalculated = newCalc,
                        newWithDelta  = (newCalc + adj.delta).coerceAtLeast(0.0),
                    )
                }

                if (conflicts.isEmpty()) {
                    applyNewListKeepingAdjustments(newList, newStart, newEnd)
                } else {
                    pendingRegeneration.value = PendingRegeneration(newStart, newEnd, conflicts)
                }
            } catch (e: Exception) {
                _uiState.value = ShoppingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun confirmKeepAdjustments() {
        val pending = pendingRegeneration.value ?: return
        pending.conflicts.forEach { conflict ->
            adjustments[conflict.ingredientId] = ShoppingAdjustment(
                ingredientId     = conflict.ingredientId,
                calculatedAmount = conflict.newCalculated,
                adjustedAmount   = conflict.newWithDelta,
            )
        }
        pendingRegeneration.value = null
        loadShoppingList(pending.newStartDate, pending.newEndDate)
    }

    fun confirmDiscardAdjustments() {
        val pending = pendingRegeneration.value ?: return
        pending.conflicts.forEach { adjustments.remove(it.ingredientId) }
        pendingRegeneration.value = null
        loadShoppingList(pending.newStartDate, pending.newEndDate)
    }

    fun dismissPendingRegeneration() { pendingRegeneration.value = null }

    // ── Amount editing ────────────────────────────────────────────────────────

    fun commitExpression(ingredientId: String, expression: String): Double? {
        val value   = evaluateExpression(expression) ?: return null
        val current = _uiState.value as? ShoppingUiState.Success ?: return null
        val item    = current.shoppingList.categories
            .flatMap { it.items }
            .find { it.ingredientId == ingredientId } ?: return null

        if (value == item.totalGrams) {
            adjustments.remove(ingredientId)
        } else {
            adjustments[ingredientId] = ShoppingAdjustment(
                ingredientId     = ingredientId,
                calculatedAmount = item.totalGrams,
                adjustedAmount   = value,
            )
        }
        pushAdjustments()
        return value
    }

    fun setExpandedItem(ingredientId: String?) {
        _uiState.update { state ->
            (state as? ShoppingUiState.Success)?.copy(expandedItemId = ingredientId) ?: state
        }
    }

    // ── Checked state ─────────────────────────────────────────────────────────

    fun toggleItem(ingredientId: String) {
        if (ingredientId in checkedSet) {
            checkedSet.remove(ingredientId)
            checkedList.remove(ingredientId)
        } else {
            checkedSet.add(ingredientId)
            checkedList.add(ingredientId)   // newest at end
        }
        pushChecked()
    }

    fun uncheckAll() {
        checkedSet.clear()
        checkedList.clear()
        pushChecked()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun applyNewListKeepingAdjustments(
        newList:  ShoppingList,
        newStart: LocalDate,
        newEnd:   LocalDate,
    ) {
        newList.categories.flatMap { it.items }.forEach { item ->
            adjustments[item.ingredientId]?.let { adj ->
                adjustments[item.ingredientId] = adj.copy(calculatedAmount = item.totalGrams)
            }
        }
        currentStart = newStart
        currentEnd   = newEnd
        _uiState.value = ShoppingUiState.Success(
            shoppingList   = newList,
            checkedItems   = checkedSet.toSet(),
            checkedOrder   = checkedList.toList(),
            adjustments    = adjustments.toMap(),
            expandedItemId = null,
        )
    }

    private fun pushAdjustments() {
        _uiState.update { state ->
            (state as? ShoppingUiState.Success)
                ?.copy(adjustments = adjustments.toMap()) ?: state
        }
    }

    private fun pushChecked() {
        _uiState.update { state ->
            (state as? ShoppingUiState.Success)
                ?.copy(checkedItems = checkedSet.toSet(), checkedOrder = checkedList.toList()) ?: state
        }
    }

    private fun nextShoppingPeriodEnd(): LocalDate {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return LocalDate(today.year, today.monthNumber, today.dayOfMonth + 6)
    }
}

// ── Expression evaluator ──────────────────────────────────────────────────────

fun evaluateExpression(raw: String): Double? {
    val expr = raw.trim().replace(',', '.')
    if (expr.isBlank()) return null
    return try {
        parseAddSub(expr.replace(" ", ""), Index(0))
    } catch (_: Exception) { null }
}

private class Index(var value: Int)

private fun parseAddSub(expr: String, i: Index): Double {
    var result = parseMulDiv(expr, i)
    while (i.value < expr.length) {
        when (expr[i.value]) {
            '+' -> { i.value++; result += parseMulDiv(expr, i) }
            '-' -> { i.value++; result -= parseMulDiv(expr, i) }
            else -> break
        }
    }
    return result
}

private fun parseMulDiv(expr: String, i: Index): Double {
    var result = parseNumber(expr, i)
    while (i.value < expr.length) {
        when (expr[i.value]) {
            '*' -> { i.value++; result *= parseNumber(expr, i) }
            '/' -> {
                i.value++
                val denom = parseNumber(expr, i)
                if (denom == 0.0) throw ArithmeticException("div by zero")
                result /= denom
            }
            else -> break
        }
    }
    return result
}

private fun parseNumber(expr: String, i: Index): Double {
    val start = i.value
    if (i.value < expr.length && expr[i.value] == '-') i.value++
    while (i.value < expr.length && (expr[i.value].isDigit() || expr[i.value] == '.')) i.value++
    if (i.value == start) throw IllegalArgumentException("expected number at $start")
    return expr.substring(start, i.value).toDouble()
}