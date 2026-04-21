package com.adasa.foodplan.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeIngredientUi(
    val ingredientId: String?,
    val subRecipeId: String?,
    val name: String,
    val amount: Double,
    val unit: String,   // "g" or "serv."
    val kcal: Double
)

sealed interface AddEditUiState {
    data object Loading : AddEditUiState
    data object Ready : AddEditUiState
    data object Saving : AddEditUiState
    data object Saved : AddEditUiState
    data class Error(val message: String) : AddEditUiState
}

@HiltViewModel
class AddEditRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddEditUiState>(AddEditUiState.Ready)
    val uiState: StateFlow<AddEditUiState> = _uiState

    val name = MutableStateFlow("")
    val type = MutableStateFlow(RecipeType.MEAL)
    val mealCategories = MutableStateFlow<Set<MealCategory>>(emptySet())
    val componentCategory = MutableStateFlow<ComponentCategory?>(null)
    val ingredients = MutableStateFlow<List<RecipeIngredientUi>>(emptyList())
    val steps = MutableStateFlow<List<String>>(emptyList())

    val nutrition: StateFlow<RecipeNutrition> = ingredients.map { list ->
        RecipeNutrition(
            kcal = list.sumOf { it.kcal },
            protein = 0.0,
            fat = 0.0,
            carbs = 0.0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeNutrition(0.0, 0.0, 0.0, 0.0))

    private var editingRecipeId: String? = null

    fun loadRecipe(recipeId: String?) {
        if (recipeId == null) {
            _uiState.value = AddEditUiState.Ready
            return
        }
        viewModelScope.launch {
            _uiState.value = AddEditUiState.Loading
            val recipe = recipeRepository.getRecipeWithIngredients(recipeId)
            if (recipe == null) {
                _uiState.value = AddEditUiState.Error("Recipe not found")
                return@launch
            }
            editingRecipeId = recipe.id
            name.value = recipe.name
            type.value = recipe.type
            mealCategories.value = recipe.mealCategories
            componentCategory.value = recipe.componentCategory
            steps.value = recipe.steps.toMutableList()
            val uiIngredients = recipe.ingredients.mapNotNull { ri ->
                if (ri.ingredientId != null) {
                    val ing = ingredientRepository.getIngredientById(ri.ingredientId) ?: return@mapNotNull null
                    val grams = ri.grams ?: 0.0
                    RecipeIngredientUi(
                        ingredientId = ri.ingredientId,
                        subRecipeId = null,
                        name = ing.name,
                        amount = grams,
                        unit = "g",
                        kcal = grams * ing.kcalPer100g / 100
                    )
                } else if (ri.subRecipeId != null) {
                    val sub = recipeRepository.getRecipeById(ri.subRecipeId) ?: return@mapNotNull null
                    RecipeIngredientUi(
                        ingredientId = null,
                        subRecipeId = ri.subRecipeId,
                        name = sub.name,
                        amount = ri.portions ?: 1.0,
                        unit = "serv.",
                        kcal = 0.0
                    )
                } else null
            }
            ingredients.value = uiIngredients
            _uiState.value = AddEditUiState.Ready
        }
    }

    fun onNameChange(n: String) { name.value = n }
    fun onTypeChange(t: RecipeType) { type.value = t }

    fun onMealCategoryToggle(category: MealCategory) {
        val current = mealCategories.value.toMutableSet()
        if (category in current) current.remove(category) else current.add(category)
        mealCategories.value = current
    }

    fun onComponentCategorySelect(category: ComponentCategory) {
        componentCategory.value = category
    }

    fun addIngredient(ingredientId: String, grams: Double) {
        viewModelScope.launch {
            val ing = ingredientRepository.getIngredientById(ingredientId) ?: return@launch
            val kcal = grams * ing.kcalPer100g / 100
            val ui = RecipeIngredientUi(
                ingredientId = ingredientId,
                subRecipeId = null,
                name = ing.name,
                amount = grams,
                unit = "g",
                kcal = kcal
            )
            ingredients.value = ingredients.value + ui
        }
    }

    fun addSubRecipe(recipeId: String, portions: Double) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById(recipeId) ?: return@launch
            val ui = RecipeIngredientUi(
                ingredientId = null,
                subRecipeId = recipeId,
                name = recipe.name,
                amount = portions,
                unit = "serv.",
                kcal = 0.0
            )
            ingredients.value = ingredients.value + ui
        }
    }

    fun updateIngredientAmount(index: Int, amount: Double) {
        val list = ingredients.value.toMutableList()
        val item = list.getOrNull(index) ?: return
        val updatedKcal = if (item.ingredientId != null) {
            viewModelScope.launch {
                val ing = ingredientRepository.getIngredientById(item.ingredientId)
                if (ing != null) {
                    val newKcal = amount * ing.kcalPer100g / 100
                    val updated = list.toMutableList()
                    updated[index] = item.copy(amount = amount, kcal = newKcal)
                    ingredients.value = updated
                }
            }
            return
        } else 0.0
        list[index] = item.copy(amount = amount, kcal = updatedKcal)
        ingredients.value = list
    }

    fun removeIngredient(index: Int) {
        val list = ingredients.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            ingredients.value = list
        }
    }

    fun addStep() {
        steps.value = steps.value + ""
    }

    fun updateStep(index: Int, text: String) {
        val list = steps.value.toMutableList()
        if (index in list.indices) {
            list[index] = text
            steps.value = list
        }
    }

    fun removeStep(index: Int) {
        val list = steps.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            steps.value = list
        }
    }

    fun saveRecipe() {
        viewModelScope.launch {
            _uiState.value = AddEditUiState.Saving
            val recipeIngredients = ingredients.value.map { ui ->
                if (ui.ingredientId != null) {
                    RecipeIngredient(ingredientId = ui.ingredientId, grams = ui.amount)
                } else {
                    RecipeIngredient(subRecipeId = ui.subRecipeId!!, portions = ui.amount)
                }
            }
            val recipe = Recipe(
                id = editingRecipeId ?: "",
                name = name.value,
                type = type.value,
                mealCategories = mealCategories.value,
                componentCategory = componentCategory.value,
                ingredients = recipeIngredients,
                steps = steps.value.filter { it.isNotBlank() },
                notes = ""
            )
            val result = recipeRepository.saveRecipe(recipe)
            _uiState.value = if (result.isSuccess) AddEditUiState.Saved
            else AddEditUiState.Error(result.exceptionOrNull()?.message ?: "Save failed")
        }
    }
}
