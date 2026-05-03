package com.adasa.foodplan.ui.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.MealPlanRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.MealSlot
import com.adasa.foodplan.domain.model.RecipeNutrition
import com.adasa.foodplan.domain.model.computeNutrition
import com.adasa.foodplan.domain.usecase.GenerateMealPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

sealed interface OptimizerState {
    data object Idle    : OptimizerState
    data object Running : OptimizerState
    data class  Success(val planName: String) : OptimizerState
    data class  Error(val message: String)    : OptimizerState
}

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository:  MealPlanRepository,
    private val recipeRepository:    RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val generateMealPlan:    GenerateMealPlanUseCase   // ← injected use case
) : ViewModel() {

    private val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    val selectedView  = MutableStateFlow(PlanView.DAY)
    val selectedDate  = MutableStateFlow(today)
    val statsExpanded = MutableStateFlow(false)

    // Bumped after every successful optimization so combine() re-queries the DB
    private val _refreshTrigger = MutableStateFlow(0L)

    private val _optimizerState = MutableStateFlow<OptimizerState>(OptimizerState.Idle)
    val optimizerState: StateFlow<OptimizerState> = _optimizerState.asStateFlow()

    // Map<dateString, Set<mealIndex>> — in-memory meal check state
    private val _checkedMeals = MutableStateFlow<Map<String, Set<Int>>>(emptyMap())
    val checkedMeals: StateFlow<Map<String, Set<Int>>> = _checkedMeals

    fun onMealChecked(date: LocalDate, mealIndex: Int) {
        _checkedMeals.update { map ->
            val key     = date.toString()
            val current = map[key] ?: emptySet()
            val updated = if (mealIndex in current) current - mealIndex else current + mealIndex
            map + (key to updated)
        }
    }

    private val nutritionCache = mutableMapOf<String, RecipeNutrition>()

    // _refreshTrigger is included so all views re-query after optimization
    val dayUiState: StateFlow<DayUiState?> =
        combine(selectedDate, _checkedMeals, _refreshTrigger) { date, checked, _ ->
            buildDayState(date, checked)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val weekUiState: StateFlow<WeekUiState?> =
        combine(selectedDate, _checkedMeals, _refreshTrigger) { date, checked, _ ->
            buildWeekState(date, checked)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val monthUiState: StateFlow<MonthUiState?> =
        combine(selectedDate, _checkedMeals, _refreshTrigger) { date, checked, _ ->
            buildMonthState(date, checked)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onViewChange(view: PlanView) { selectedView.value = view }
    fun onToggleStats()              { statsExpanded.update { !it } }

    fun onNavigatePrevious() = selectedDate.update {
        when (selectedView.value) {
            PlanView.DAY   -> it.minus(1, DateTimeUnit.DAY)
            PlanView.WEEK  -> it.minus(7, DateTimeUnit.DAY)
            PlanView.MONTH -> it.minus(1, DateTimeUnit.MONTH)
        }
    }

    fun onNavigateNext() = selectedDate.update {
        when (selectedView.value) {
            PlanView.DAY   -> it.plus(1, DateTimeUnit.DAY)
            PlanView.WEEK  -> it.plus(7, DateTimeUnit.DAY)
            PlanView.MONTH -> it.plus(1, DateTimeUnit.MONTH)
        }
    }

    // ── Optimizer ─────────────────────────────────────────────────────────────

    /**
     * Generates a meal plan for the Monday of the currently selected week.
     * The button is disabled while running so this can only be called once at a time.
     */
    fun generatePlan() {
        if (_optimizerState.value is OptimizerState.Running) return
        viewModelScope.launch {
            _optimizerState.value = OptimizerState.Running
            val startDate = weekStart(selectedDate.value)
            val result    = generateMealPlan(startDate)
            _optimizerState.value = result.fold(
                onSuccess = { plan ->
                    _refreshTrigger.update { it + 1 }   // re-query all views
                    OptimizerState.Success(plan.name)
                },
                onFailure = { e ->
                    OptimizerState.Error(e.message ?: "Could not generate plan")
                }
            )
        }
    }

    /** Call from the UI after the snackbar has been shown so the button returns to normal. */
    fun onOptimizerMessageConsumed() {
        _optimizerState.value = OptimizerState.Idle
    }

    // ─── Builders ────────────────────────────────────────────────────────────

    private suspend fun buildDayState(date: LocalDate, checked: Map<String, Set<Int>>): DayUiState {
        val plan  = mealPlanRepository.getDayPlanByDate(date)
        val slots = plan?.meals?.map { buildSlotUi(it) } ?: emptyList()
        val nutrition = dayNutritionOf(slots)
        return DayUiState(
            date               = date,
            dayType            = dayTypeOf(date),
            kcalTarget         = plan?.goal?.kcalTarget ?: defaultKcal(date),
            meals              = slots,
            proteinPowderGrams = plan?.proteinPowderGrams ?: 0.0,
            nutrition          = nutrition,
            daysUntilShopping  = daysUntilShopping(date).takeIf { it > 0 },
            proteinPowderDaysLeft = null
        )
    }

    private suspend fun buildWeekState(date: LocalDate, checked: Map<String, Set<Int>>): WeekUiState {
        val monday = weekStart(date)
        val sunday = monday.plus(6, DateTimeUnit.DAY)
        val plans  = mealPlanRepository.getDayPlansForRange(monday, sunday)
        var totP = 0.0; var totF = 0.0; var totC = 0.0; var totK = 0.0; var dayCount = 0
        var fullDays = 0; var halfDays = 0; var kcalPctSum = 0.0; var kcalPctCount = 0
        val days = (0..6).map { offset ->
            val d     = monday.plus(offset, DateTimeUnit.DAY)
            val plan  = plans[d]
            val slots = plan?.meals?.map { buildSlotUi(it) } ?: emptyList()
            val kcal  = slots.sumOf { it.kcal }
            val checkedSet = checked[d.toString()] ?: emptySet()
            val checkedCount = checkedSet.size
            if (slots.isNotEmpty()) {
                totK += kcal; totP += slots.sumOf { it.protein }
                totF += slots.sumOf { it.fat }; totC += slots.sumOf { it.carbs }; dayCount++
                val pct = checkedCount.toDouble() / slots.size
                if (pct >= 1.0) fullDays++ else if (pct >= 0.5) halfDays++
                val target = plan?.goal?.kcalTarget ?: defaultKcal(d)
                if (target > 0) { kcalPctSum += (kcal / target) * 100; kcalPctCount++ }
            }
            val target = plan?.goal?.kcalTarget ?: defaultKcal(d)
            WeekDayUi(d, d == today, target > 1_400, isShoppingDay(d), kcal, target,
                slots.map { it.recipeName }, slots, checkedCount)
        }
        val n = dayCount.coerceAtLeast(1)
        return WeekUiState(
            weekNumber  = isoWeekNumber(monday), startDate = monday, endDate = sunday,
            days        = days,
            avgKcal     = totK / n, avgProtein = totP / n, avgFat = totF / n, avgCarbs = totC / n,
            weekTotalKcal = totK, highCalDays = days.count { it.isHighCal },
            daysUntilShopping = daysUntilShopping(date).takeIf { it > 0 },
            proteinPowderDaysLeft = null,
            fullDays = fullDays, halfDays = halfDays,
            avgKcalPct = if (kcalPctCount > 0) (kcalPctSum / kcalPctCount).toInt() else 0,
        )
    }

    private suspend fun buildMonthState(date: LocalDate, checked: Map<String, Set<Int>>): MonthUiState {
        val first = LocalDate(date.year, date.month, 1)
        val last  = first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        val plans = mealPlanRepository.getDayPlansForRange(first, last)
        var totK = 0.0; var totP = 0.0; var totF = 0.0; var totC = 0.0; var dayCount = 0
        var fullDays = 0; var halfDays = 0; var kcalPctSum = 0.0; var kcalPctCount = 0
        val days = buildList {
            var d = first
            while (d <= last) {
                val plan  = plans[d]
                val slots = plan?.meals?.map { buildSlotUi(it) } ?: emptyList()
                val checkedSet = checked[d.toString()] ?: emptySet()
                val checkedCount = checkedSet.size
                if (slots.isNotEmpty()) {
                    totK += slots.sumOf { it.kcal }; totP += slots.sumOf { it.protein }
                    totF += slots.sumOf { it.fat }; totC += slots.sumOf { it.carbs }; dayCount++
                    val pct = checkedCount.toDouble() / slots.size
                    if (pct >= 1.0) fullDays++ else if (pct >= 0.5) halfDays++
                    val target = plan?.goal?.kcalTarget ?: defaultKcal(d)
                    if (target > 0) { kcalPctSum += (slots.sumOf { it.kcal } / target) * 100; kcalPctCount++ }
                }
                add(MonthDayUi(d, d == today, (plan?.goal?.kcalTarget ?: defaultKcal(d)) > 1_400,
                    isShoppingDay(d), plan != null, checkedCount, slots.size))
                d = d.plus(1, DateTimeUnit.DAY)
            }
        }
        val n = dayCount.coerceAtLeast(1)
        return MonthUiState(
            month = date.month, year = date.year, days = days,
            avgKcal = totK / n, avgProtein = totP / n, avgFat = totF / n, avgCarbs = totC / n,
            monthTotalKcal = totK, shoppingDaysCount = days.count { it.isShoppingDay },
            highCalDaysCount = days.count { it.isHighCal }, proteinPowderDaysLeft = null,
            fullDays = fullDays, halfDays = halfDays,
            avgKcalPct = if (kcalPctCount > 0) (kcalPctSum / kcalPctCount).toInt() else 0,
        )
    }

    private suspend fun buildSlotUi(slot: MealSlot): MealSlotUi {
        val recipe    = recipeRepository.getRecipeById(slot.recipeId)
        val nutrition = recipe?.let { recipeNutrition(it.id) }
        return MealSlotUi(slot.type, slot.recipeId, recipe?.name ?: "—",
            nutrition?.kcal ?: 0.0, nutrition?.protein ?: 0.0,
            nutrition?.fat ?: 0.0, nutrition?.carbs ?: 0.0)
    }

    private suspend fun recipeNutrition(recipeId: String): RecipeNutrition {
        nutritionCache[recipeId]?.let { return it }
        val recipe = recipeRepository.getRecipeWithIngredients(recipeId)
            ?: return RecipeNutrition(0.0, 0.0, 0.0, 0.0)
        val ingMap = recipe.ingredients.mapNotNull { it.ingredientId }
            .mapNotNull { id -> ingredientRepository.getIngredientById(id)?.let { id to it } }.toMap()
        val subMap = recipe.ingredients.mapNotNull { it.subRecipeId }.mapNotNull { id ->
            val sub = recipeRepository.getRecipeWithIngredients(id) ?: return@mapNotNull null
            val subIng = sub.ingredients.mapNotNull { it.ingredientId }
                .mapNotNull { sid -> ingredientRepository.getIngredientById(sid)?.let { sid to it } }.toMap()
            id to sub.ingredients.computeNutrition(subIng, emptyMap())
        }.toMap()
        return recipe.ingredients.computeNutrition(ingMap, subMap).also { nutritionCache[recipeId] = it }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun dayTypeOf(d: LocalDate) = when (d.dayOfWeek) {
        DayOfWeek.SUNDAY                               -> DayType.SUNDAY
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY           -> DayType.WEEKEND
        else                                           -> DayType.WEEKDAY
    }
    private fun defaultKcal(d: LocalDate) = when (d.dayOfWeek) {
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY -> 1_539
        DayOfWeek.SUNDAY                     -> 1_257
        else                                 -> 1_350
    }
    private fun isShoppingDay(d: LocalDate) = d.dayOfWeek == DayOfWeek.SUNDAY
    private fun daysUntilShopping(from: LocalDate): Int {
        val raw = (6 - from.dayOfWeek.ordinal + 7) % 7
        return if (raw == 0) 7 else raw
    }
    private fun weekStart(d: LocalDate) = d.minus(d.dayOfWeek.ordinal, DateTimeUnit.DAY)
    private fun isoWeekNumber(d: LocalDate): Int {
        val j = java.time.LocalDate.of(d.year, d.monthNumber, d.dayOfMonth)
        return j.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
    }
    private fun dayNutritionOf(slots: List<MealSlotUi>) = DayNutrition(
        slots.sumOf { it.kcal }, slots.sumOf { it.protein },
        slots.sumOf { it.fat }, slots.sumOf { it.carbs }
    )
}