package com.adasa.foodplan.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ── Step model ────────────────────────────────────────────────────────────────

sealed interface StepUi {
    val id: String
    data class TextStep(override val id: String, val text: String)                     : StepUi
    data class TimerStep(override val id: String, val label: String, val totalSeconds: Int) : StepUi
}

// Serialisation helpers — stored as plain strings in Recipe.steps
private const val TIMER_PREFIX = "TIMER|"
private fun StepUi.serialise(): String = when (this) {
    is StepUi.TextStep  -> text
    is StepUi.TimerStep -> "$TIMER_PREFIX$label|$totalSeconds"
}
private fun String.deserialiseStep(): StepUi {
    val id = UUID.randomUUID().toString()
    return if (startsWith(TIMER_PREFIX)) {
        val rest  = removePrefix(TIMER_PREFIX)
        val parts = rest.split("|")
        val label   = parts.getOrElse(0) { "" }
        val seconds = parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0
        StepUi.TimerStep(id, label, seconds)
    } else {
        StepUi.TextStep(id, this)
    }
}

// ── Ingredient UI model ───────────────────────────────────────────────────────

data class RecipeIngredientUi(
    val ingredientId: String?,
    val subRecipeId:  String?,
    val name:         String,
    val amount:       Double,
    val unit:         String,       // "g" or "serv."
    val kcal:         Double,
    val protein:      Double = 0.0,
    val fat:          Double = 0.0,
    val carbs:        Double = 0.0,
)

// ── UI state ──────────────────────────────────────────────────────────────────

sealed interface AddEditUiState {
    data object Loading : AddEditUiState
    data object Ready   : AddEditUiState
    data object Saving  : AddEditUiState
    data object Saved   : AddEditUiState
    data class  Error(val message: String) : AddEditUiState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class AddEditRecipeViewModel @Inject constructor(
    private val recipeRepository:     RecipeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddEditUiState>(AddEditUiState.Ready)
    val uiState: StateFlow<AddEditUiState> = _uiState

    val name              = MutableStateFlow("")
    val type              = MutableStateFlow(RecipeType.MEAL)
    val mealCategories    = MutableStateFlow<Set<MealCategory>>(emptySet())
    val componentCategory = MutableStateFlow<ComponentCategory?>(null)
    val ingredients       = MutableStateFlow<List<RecipeIngredientUi>>(emptyList())
    val steps             = MutableStateFlow<List<StepUi>>(emptyList())

    val nutrition: StateFlow<RecipeNutrition> = ingredients.map { list ->
        RecipeNutrition(
            kcal    = list.sumOf { it.kcal },
            protein = list.sumOf { it.protein },
            fat     = list.sumOf { it.fat },
            carbs   = list.sumOf { it.carbs },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeNutrition(0.0, 0.0, 0.0, 0.0))

    private var editingRecipeId: String? = null

    // ── Load ──────────────────────────────────────────────────────────────────

    fun loadRecipe(recipeId: String?) {
        if (recipeId == null) { _uiState.value = AddEditUiState.Ready; return }
        // Don't reload if we already have this recipe in memory — prevents
        // in-flight edits from being wiped when the composable re-enters
        // composition after returning from a nested screen (IngredientDetail etc.)
        if (editingRecipeId == recipeId) return
        viewModelScope.launch {
            _uiState.value = AddEditUiState.Loading
            val recipe = recipeRepository.getRecipeWithIngredients(recipeId)
            if (recipe == null) { _uiState.value = AddEditUiState.Error("Recipe not found"); return@launch }

            editingRecipeId       = recipe.id
            name.value            = recipe.name
            type.value            = recipe.type
            mealCategories.value  = recipe.mealCategories
            componentCategory.value = recipe.componentCategory
            steps.value           = recipe.steps.map { it.deserialiseStep() }

            val uiIngredients = recipe.ingredients.mapNotNull { ri ->
                if (ri.ingredientId != null) {
                    val ing   = ingredientRepository.getIngredientById(ri.ingredientId) ?: return@mapNotNull null
                    val grams = ri.grams ?: 0.0
                    RecipeIngredientUi(
                        ingredientId = ri.ingredientId,
                        subRecipeId  = null,
                        name         = ing.name,
                        amount       = grams,
                        unit         = "g",
                        kcal         = grams * ing.kcalPer100g    / 100,
                        protein      = grams * ing.proteinPer100g / 100,
                        fat          = grams * ing.fatPer100g     / 100,
                        carbs        = grams * ing.carbsPer100g   / 100,
                    )
                } else if (ri.subRecipeId != null) {
                    val sub = recipeRepository.getRecipeById(ri.subRecipeId) ?: return@mapNotNull null
                    RecipeIngredientUi(
                        ingredientId = null,
                        subRecipeId  = ri.subRecipeId,
                        name         = sub.name,
                        amount       = ri.portions ?: 1.0,
                        unit         = "serv.",
                        kcal         = 0.0,
                    )
                } else null
            }
            ingredients.value = uiIngredients
            _uiState.value    = AddEditUiState.Ready
        }
    }

    // ── Edits ─────────────────────────────────────────────────────────────────

    fun onNameChange(n: String) { name.value = n }
    fun onTypeChange(t: RecipeType) { type.value = t }

    fun onMealCategoryToggle(category: MealCategory) {
        val current = mealCategories.value.toMutableSet()
        if (category in current) current.remove(category) else current.add(category)
        mealCategories.value = current
    }
    fun onComponentCategorySelect(category: ComponentCategory) { componentCategory.value = category }

    // ── Ingredients ───────────────────────────────────────────────────────────

    fun addIngredient(ingredientId: String, grams: Double) {
        viewModelScope.launch {
            val ing = ingredientRepository.getIngredientById(ingredientId) ?: return@launch
            ingredients.value = ingredients.value + RecipeIngredientUi(
                ingredientId = ingredientId,
                subRecipeId  = null,
                name         = ing.name,
                amount       = grams,
                unit         = "g",
                kcal         = grams * ing.kcalPer100g    / 100,
                protein      = grams * ing.proteinPer100g / 100,
                fat          = grams * ing.fatPer100g     / 100,
                carbs        = grams * ing.carbsPer100g   / 100,
            )
        }
    }

    fun addSubRecipe(recipeId: String, portions: Double) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById(recipeId) ?: return@launch
            ingredients.value = ingredients.value + RecipeIngredientUi(
                ingredientId = null,
                subRecipeId  = recipeId,
                name         = recipe.name,
                amount       = portions,
                unit         = "serv.",
                kcal         = 0.0,
            )
        }
    }

    fun updateIngredientAmount(index: Int, amount: Double) {
        val list = ingredients.value.toMutableList()
        val item = list.getOrNull(index) ?: return
        if (item.ingredientId != null) {
            viewModelScope.launch {
                val ing = ingredientRepository.getIngredientById(item.ingredientId) ?: return@launch
                list[index] = item.copy(
                    amount  = amount,
                    kcal    = amount * ing.kcalPer100g    / 100,
                    protein = amount * ing.proteinPer100g / 100,
                    fat     = amount * ing.fatPer100g     / 100,
                    carbs   = amount * ing.carbsPer100g   / 100,
                )
                ingredients.value = list
            }
        } else {
            list[index] = item.copy(amount = amount)
            ingredients.value = list
        }
    }

    fun removeIngredient(index: Int) {
        val list = ingredients.value.toMutableList()
        if (index in list.indices) { list.removeAt(index); ingredients.value = list }
    }

    // ── Steps & timers ────────────────────────────────────────────────────────

    fun addStep() {
        steps.value = steps.value + StepUi.TextStep(UUID.randomUUID().toString(), "")
    }

    fun addTimer(label: String, totalSeconds: Int) {
        steps.value = steps.value + StepUi.TimerStep(UUID.randomUUID().toString(), label, totalSeconds)
    }

    fun updateStep(index: Int, text: String) {
        val list = steps.value.toMutableList()
        val item = list.getOrNull(index) as? StepUi.TextStep ?: return
        list[index] = item.copy(text = text)
        steps.value = list
    }

    fun updateTimer(index: Int, label: String, totalSeconds: Int) {
        val list = steps.value.toMutableList()
        val item = list.getOrNull(index) as? StepUi.TimerStep ?: return
        list[index] = item.copy(label = label, totalSeconds = totalSeconds)
        steps.value = list
    }

    fun reorderSteps(fromIndex: Int, toIndex: Int) {
        val list = steps.value.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        steps.value = list
    }

    fun removeStep(index: Int) {
        val list = steps.value.toMutableList()
        if (index in list.indices) { list.removeAt(index); steps.value = list }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun saveRecipe() {
        viewModelScope.launch {
            _uiState.value = AddEditUiState.Saving
            val recipeIngredients = ingredients.value.map { ui ->
                if (ui.ingredientId != null) RecipeIngredient(ingredientId = ui.ingredientId, grams = ui.amount)
                else RecipeIngredient(subRecipeId = ui.subRecipeId!!, portions = ui.amount)
            }
            val recipe = Recipe(
                id                = editingRecipeId ?: "",
                name              = name.value,
                type              = type.value,
                mealCategories    = mealCategories.value,
                componentCategory = componentCategory.value,
                ingredients       = recipeIngredients,
                steps             = steps.value
                    .filter { it !is StepUi.TextStep || it.text.isNotBlank() }
                    .map { it.serialise() },
                notes             = "",
            )
            val result = recipeRepository.saveRecipe(recipe)
            _uiState.value = if (result.isSuccess) AddEditUiState.Saved
            else AddEditUiState.Error(result.exceptionOrNull()?.message ?: "Save failed")
        }
    }
}