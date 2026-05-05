package com.adasa.foodplan.domain.usecase

import com.adasa.foodplan.domain.model.*
import com.adasa.foodplan.domain.usecase.MealPlanOptimizer.Slot
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import java.util.UUID
import kotlin.math.abs
import kotlin.math.exp
import kotlin.random.Random


/** State: maps every slot in the week to a recipeId (or NULL_SNACK_ID). */
private typealias State = Map<MealPlanOptimizer.Slot, String>

/**
 * Pure optimizer — no I/O, no side effects. All inputs are pre-assembled by
 * GenerateMealPlanUseCase before this is called; all outputs are handed back
 * for the caller to persist.
 *
 * Algorithm: simulated annealing over a fixed-shape weekly state.
 *
 * State shape
 * -----------
 * Each active day has a fixed set of slots determined upfront:
 *   - breakfast / lunch / dinner  (0 or 1 each, per DayMealConfig)
 *   - snackCount + EXTRA_SNACK_SLOTS snack slots
 *
 * Optional snack slots are filled with NULL_SNACK_ID (a sentinel that contributes
 * zero macros and is omitted from the final MealPlan output). This keeps the state
 * fixed-size throughout the search so mutations stay simple and uniform.
 *
 * Protein powder
 * --------------
 * Not annealed. After every recipe mutation the optimal powder gram assignment is
 * computed analytically per day (close the protein gap, stay within kcal ceiling)
 * and included in the score. This prevents the annealer from finding plans that only
 * look good because powder was not yet accounted for.
 */
object MealPlanOptimizer {

    // ── Constants ──────────────────────────────────────────────────────────

    private const val NULL_SNACK_ID      = "null_snack"
    private const val EXTRA_SNACK_SLOTS  = 3
    private const val MAX_ITERATIONS     = 4_000
    private const val INITIAL_TEMP       = 0.35
    private const val MAX_POWDER_GRAMS   = 100.0

    // Soft-objective weights
    private const val W_KCAL            = 3.0
    private const val W_PROTEIN         = 2.0
    private const val W_FAT             = 1.0
    private const val W_CARBS           = 1.0
    private const val W_RECENCY         = 0.5
    private const val W_VARIETY         = 1.0

    // ── Public surface ─────────────────────────────────────────────────────

    fun generate(
        history:      List<MealPlan>,
        config:       MealPlanConfig,
        recipes:      List<Recipe>,
        ratings:      List<RecipeRating>,
        nutritionMap: Map<String, RecipeNutrition>,
        ingredientMap: Map<String, Ingredient>,
        startDate:    LocalDate
    ): MealPlan {
        val rng = Random(System.currentTimeMillis())

        // Pre-process ratings into fast-lookup structures
        val ratingMap = ratings.associateBy { it.recipeId }

        // Build per-category recipe pools with sampling weights
        val weightedRecipes = buildWeightedRecipes(recipes, ratingMap, startDate, config.variety)
        val poolsByCategory = buildPoolsByCategory(weightedRecipes)

        // Determine which days in this week are active
        val activeDays = buildActiveDays(startDate, config.schedule)

        if (activeDays.isEmpty()) {
            return buildEmptyPlan(startDate, config)
        }

        // Validate pools cover all required slot types
        validatePools(activeDays, config.schedule, poolsByCategory)

        // Pre-assign batch cooking groups (these slots are frozen during annealing)
        val frozenSlots = resolveBatchGroups(
            config.schedule.batchGroups, activeDays, poolsByCategory, rng
        )

        // Build the history recency index (recipeId → most recent date used)
        val historyRecency = buildHistoryRecency(history)

        val ctx = Ctx(
            config        = config,
            nutritionMap  = nutritionMap,
            ingredientMap = ingredientMap,
            ratingMap     = ratingMap,
            weightedByCategory = poolsByCategory,
            frozenSlots   = frozenSlots,
            activeDays    = activeDays,
            historyRecency = historyRecency,
            history       = history,
            startDate     = startDate
        )

        // Initialise state and begin annealing
        var state     = initState(activeDays, config.schedule, poolsByCategory, frozenSlots, rng)
        var stateCost = cost(state, ctx)
        var bestState = state
        var bestCost  = stateCost

        repeat(MAX_ITERATIONS) { i ->
            val temp      = INITIAL_TEMP * (1.0 - i.toDouble() / MAX_ITERATIONS)
            val candidate = mutate(state, ctx, rng)
            val candCost  = cost(candidate, ctx)
            val delta     = candCost - stateCost

            if (delta < 0 || (temp > 0.0 && rng.nextDouble() < exp(-delta / temp))) {
                state     = candidate
                stateCost = candCost
            }
            if (stateCost < bestCost) {
                bestState = state
                bestCost  = stateCost
            }
        }

        return buildMealPlan(bestState, ctx)
    }

    // ── Internal types ─────────────────────────────────────────────────────

    /**
     * Uniquely identifies one meal slot within the week.
     * [index] disambiguates multiple snack slots on the same day.
     */
    public data class Slot(
        val date:     LocalDate,
        val category: MealCategory,
        val index:    Int
    )


    private data class WeightedRecipe(
        val recipe:          Recipe,
        val samplingWeight:  Double,
        val recencyPenalty:  Double,
        val isPinned:        Boolean
    )

    /** All context needed by cost/mutate — assembled once before the loop. */
    private data class Ctx(
        val config:            MealPlanConfig,
        val nutritionMap:      Map<String, RecipeNutrition>,
        val ingredientMap:     Map<String, Ingredient>,
        val ratingMap:         Map<String, RecipeRating>,
        val weightedByCategory: Map<MealCategory, List<WeightedRecipe>>,
        val frozenSlots:       Map<Slot, String>,
        val activeDays:        List<LocalDate>,
        val historyRecency:    Map<String, LocalDate>,
        val history:           List<MealPlan>,
        val startDate:         LocalDate
    )

    // ── Recipe weight pre-processing ───────────────────────────────────────

    private fun buildWeightedRecipes(
        recipes:  List<Recipe>,
        ratingMap: Map<String, RecipeRating>,
        startDate: LocalDate,
        variety:  VarietyConfig
    ): List<WeightedRecipe> = recipes.map { recipe ->
        val rating = ratingMap[recipe.id]
        WeightedRecipe(
            recipe         = recipe,
            samplingWeight = starsToSamplingWeight(rating?.stars),
            recencyPenalty = recencyPenalty(rating?.lastScheduledDate, startDate, variety.uniqueWeeksBeforeRepeat),
            isPinned       = rating?.isPinned ?: false
        )
    }

    /** Exponential scale: 2★ rarely offered, 5★ four times more likely than 3★. */
    private fun starsToSamplingWeight(stars: Int?): Double = when (stars) {
        null -> 1.0
        1    -> 0.0   // should already be filtered, but guard
        2    -> 0.15
        3    -> 1.0
        4    -> 2.0
        5    -> 4.0
        else -> 1.0
    }

    /**
     * Returns 0..1 indicating how "recently used" a recipe is.
     * 0 = not used in the lookback window; 1 = used this week.
     */
    private fun recencyPenalty(lastUsed: LocalDate?, startDate: LocalDate, uniqueWeeks: Int): Double {
        lastUsed ?: return 0.0
        val weeksSince = lastUsed.daysUntil(startDate) / 7.0
        return (1.0 - weeksSince / uniqueWeeks.toDouble()).coerceIn(0.0, 1.0)
    }

    // ── Pool construction ──────────────────────────────────────────────────

    private fun buildPoolsByCategory(
        weighted: List<WeightedRecipe>
    ): Map<MealCategory, List<WeightedRecipe>> {
        val map = MealCategory.entries.associateWith { mutableListOf<WeightedRecipe>() }
        for (rw in weighted) {
            for (cat in rw.recipe.mealCategories) {
                (map[cat] as MutableList).add(rw)
            }
        }
        return map
    }

    private fun validatePools(
        activeDays:    List<LocalDate>,
        schedule:      MealScheduleConfig,
        poolsByCategory: Map<MealCategory, List<WeightedRecipe>>
    ) {
        val required = activeDays.flatMap { date ->
            val dc = schedule.mealSlots[date.dayOfWeek] ?: return@flatMap emptyList()
            buildList {
                if (dc.breakfast)     add(MealCategory.BREAKFAST)
                if (dc.lunch)         add(MealCategory.LUNCH)
                if (dc.dinner)        add(MealCategory.DINNER)
                if (dc.snackCount > 0) add(MealCategory.SNACK)
            }
        }.toSet()

        for (cat in required) {
            if (poolsByCategory[cat].isNullOrEmpty()) {
                throw IllegalStateException(
                    "No eligible recipes for required meal slot: ${cat.displayName}. " +
                            "Add recipes tagged as ${cat.displayName} or adjust your settings."
                )
            }
        }
    }

    // ── Active days ────────────────────────────────────────────────────────

    private fun buildActiveDays(startDate: LocalDate, schedule: MealScheduleConfig): List<LocalDate> =
        (0 until 7)
            .map { startDate.plus(it, DateTimeUnit.DAY) }
            .filter { schedule.mealSlots[it.dayOfWeek]?.isActive == true }

    // ── Batch group pre-assignment ─────────────────────────────────────────

    /**
     * Resolves batch cooking groups before annealing begins. All days in the same
     * group receive the same recipe; different batchNumbers within the same meal
     * category must receive different recipes.
     *
     * Returns a map of frozen slot assignments that the annealer must never mutate.
     */
    private fun resolveBatchGroups(
        batchGroups:    List<BatchCookingGroup>,
        activeDays:     List<LocalDate>,
        poolsByCategory: Map<MealCategory, List<WeightedRecipe>>,
        rng:            Random
    ): Map<Slot, String> {
        val frozen = mutableMapOf<Slot, String>()

        for ((category, groups) in batchGroups.groupBy { it.meal }) {
            val pool = poolsByCategory[category] ?: continue
            val usedIds = mutableSetOf<String>()

            for (group in groups.sortedBy { it.batchNumber }) {
                val available = pool.filter { it.recipe.id !in usedIds }
                val picked    = weightedRandom(available, rng) ?: continue
                usedIds += picked.recipe.id

                activeDays
                    .filter { it.dayOfWeek in group.days }
                    .forEach { date -> frozen[Slot(date, category, 0)] = picked.recipe.id }
            }
        }
        return frozen
    }

    // ── State initialisation ───────────────────────────────────────────────

    private fun slotsForDay(date: LocalDate, schedule: MealScheduleConfig): List<Slot> {
        val dc = schedule.mealSlots[date.dayOfWeek] ?: return emptyList()
        return buildList {
            if (dc.breakfast) add(Slot(date, MealCategory.BREAKFAST, 0))
            if (dc.lunch)     add(Slot(date, MealCategory.LUNCH,     0))
            if (dc.dinner)    add(Slot(date, MealCategory.DINNER,    0))
            val snackTotal = dc.snackCount + if (schedule.snackOptionalFill) EXTRA_SNACK_SLOTS else 0
            repeat(snackTotal) { i -> add(Slot(date, MealCategory.SNACK, i)) }
        }
    }

    private fun initState(
        activeDays:    List<LocalDate>,
        schedule:      MealScheduleConfig,
        poolsByCategory: Map<MealCategory, List<WeightedRecipe>>,
        frozen:        Map<Slot, String>,
        rng:           Random
    ): State {
        val state = mutableMapOf<Slot, String>()
        for (date in activeDays) {
            val dc = schedule.mealSlots[date.dayOfWeek] ?: continue
            for (slot in slotsForDay(date, schedule)) {
                state[slot] = when {
                    slot in frozen -> frozen[slot]!!
                    slot.category != MealCategory.SNACK -> {
                        // Required non-snack slot: must have a real recipe
                        val pool = poolsByCategory[slot.category] ?: emptyList()
                        weightedRandom(pool, rng)?.recipe?.id
                            ?: throw IllegalStateException("Empty pool for ${slot.category.displayName}")
                    }
                    slot.index < dc.snackCount -> {
                        // Configured snack slot: real recipe
                        val pool = poolsByCategory[MealCategory.SNACK] ?: emptyList()
                        weightedRandom(pool, rng)?.recipe?.id ?: NULL_SNACK_ID
                    }
                    else -> NULL_SNACK_ID  // Optional snack slot: start empty
                }
            }
        }
        return state
    }

    // ── Mutation ───────────────────────────────────────────────────────────

    private fun mutate(state: State, ctx: Ctx, rng: Random): State {
        val mutableSlots = state.keys.filter { it !in ctx.frozenSlots }
        if (mutableSlots.isEmpty()) return state

        return if (rng.nextDouble() < 0.7) {
            // Replace: assign a new recipe (or null sentinel) to one slot
            val slot = mutableSlots.random(rng)
            state.toMutableMap().also { it[slot] = pickForSlot(slot, ctx, rng) }
        } else {
            // Swap: exchange two same-category slots on different days
            val slot1 = mutableSlots.random(rng)
            val compatible = mutableSlots.filter {
                it.category == slot1.category && it.date != slot1.date
            }
            if (compatible.isEmpty()) {
                // Fall back to replace if no compatible partner
                state.toMutableMap().also { it[slot1] = pickForSlot(slot1, ctx, rng) }
            } else {
                val slot2 = compatible.random(rng)
                state.toMutableMap().also {
                    it[slot1] = state[slot2]!!
                    it[slot2] = state[slot1]!!
                }
            }
        }
    }

    private fun pickForSlot(slot: Slot, ctx: Ctx, rng: Random): String {
        if (slot.category == MealCategory.SNACK && rng.nextDouble() < 0.25) {
            return NULL_SNACK_ID  // Encourage trying null — let the annealer fill gaps naturally
        }
        val pool = ctx.weightedByCategory[slot.category] ?: emptyList()
        return weightedRandom(pool, rng)?.recipe?.id ?: NULL_SNACK_ID
    }

    // ── Cost function ──────────────────────────────────────────────────────

    /**
     * Returns a non-negative cost. Lower = better.
     * Returns [Double.MAX_VALUE] if any hard constraint is violated.
     */
    private fun cost(state: State, ctx: Ctx): Double {
        if (violatesHardConstraints(state, ctx)) return Double.MAX_VALUE

        val goals        = ctx.config.goals
        val activeDays   = ctx.activeDays
        val activeDayCount = activeDays.size.toDouble()

        var weeklyKcal    = 0.0
        var weeklyProtein = 0.0
        var weeklyFat     = 0.0
        var weeklyCarbs   = 0.0
        var recencyTotal  = 0.0

        for (date in activeDays) {
            val daySlots = state.entries.filter { it.key.date == date && it.value != NULL_SNACK_ID }

            var dayKcal    = 0.0
            var dayProtein = 0.0
            var dayFat     = 0.0
            var dayCarbs   = 0.0

            for ((_, recipeId) in daySlots) {
                val n = ctx.nutritionMap[recipeId] ?: continue
                dayKcal    += n.kcal
                dayProtein += n.protein
                dayFat     += n.fat
                dayCarbs   += n.carbs

                // Accumulate recency penalty (pre-computed per recipe)
                recencyTotal += ctx.weightedByCategory.values
                    .flatten()
                    .firstOrNull { it.recipe.id == recipeId }
                    ?.recencyPenalty ?: 0.0
            }

            // Powder: solved analytically inside cost so it's visible to the annealer
            val powder = ctx.config.proteinPowder
            if (powder != null && powder.autoFillGap) {
                val g = computeOptimalPowderGrams(dayKcal, dayProtein, powder, goals)
                dayKcal    += g * powder.kcalPer100g    / 100.0
                dayProtein += g * powder.proteinPer100g / 100.0
            }

            weeklyKcal    += dayKcal
            weeklyProtein += dayProtein
            weeklyFat     += dayFat
            weeklyCarbs   += dayCarbs
        }

        // Normalised weekly deviation (fractional, so different macro scales compare fairly)
        val kcalTarget    = goals.kcalTarget          * activeDayCount
        val proteinTarget = goals.resolvedProtein     * activeDayCount
        val fatTarget     = goals.resolvedFat         * activeDayCount
        val carbsTarget   = goals.resolvedCarbs       * activeDayCount

        val softCost =
            W_KCAL    * abs(weeklyKcal    - kcalTarget)    / kcalTarget.coerceAtLeast(1.0) +
                    W_PROTEIN * abs(weeklyProtein - proteinTarget)  / proteinTarget.coerceAtLeast(1.0) +
                    W_FAT     * abs(weeklyFat     - fatTarget)      / fatTarget.coerceAtLeast(1.0) +
                    W_CARBS   * abs(weeklyCarbs   - carbsTarget)    / carbsTarget.coerceAtLeast(1.0) +
                    W_RECENCY * recencyTotal

        val varietyCost = if (ctx.config.variety.proteinSourceVariety)
            W_VARIETY * proteinSourceVarietyCost(state, ctx)
        else 0.0

        val rulesCost = customRulesCost(state, ctx)

        return softCost + varietyCost + rulesCost
    }

    // ── Hard constraint checks ─────────────────────────────────────────────

    private fun violatesHardConstraints(state: State, ctx: Ctx): Boolean {
        val schedule = ctx.config.schedule
        val goals    = ctx.config.goals

        for (date in ctx.activeDays) {
            val dc       = schedule.mealSlots[date.dayOfWeek] ?: continue
            val daySlots = state.entries.filter { it.key.date == date }

            // H1 — All configured slots must contain a real recipe (null sentinel forbidden)
            if (dc.breakfast && daySlots.none { it.key.category == MealCategory.BREAKFAST && it.value != NULL_SNACK_ID }) return true
            if (dc.lunch     && daySlots.none { it.key.category == MealCategory.LUNCH     && it.value != NULL_SNACK_ID }) return true
            if (dc.dinner    && daySlots.none { it.key.category == MealCategory.DINNER    && it.value != NULL_SNACK_ID }) return true
            repeat(dc.snackCount) { i ->
                if (state[Slot(date, MealCategory.SNACK, i)].let { it == null || it == NULL_SNACK_ID }) return true
            }

            // H4 — Per-day hard bounds (evaluated including powder)
            if (hasAnyDayBound(goals)) {
                val realSlots = daySlots.filter { it.value != NULL_SNACK_ID }
                var dayKcal    = realSlots.sumOf { ctx.nutritionMap[it.value]?.kcal    ?: 0.0 }
                var dayProtein = realSlots.sumOf { ctx.nutritionMap[it.value]?.protein ?: 0.0 }
                var dayFat     = realSlots.sumOf { ctx.nutritionMap[it.value]?.fat     ?: 0.0 }
                var dayCarbs   = realSlots.sumOf { ctx.nutritionMap[it.value]?.carbs   ?: 0.0 }

                val powder = ctx.config.proteinPowder
                if (powder != null && powder.autoFillGap) {
                    val g = computeOptimalPowderGrams(dayKcal, dayProtein, powder, goals)
                    dayKcal    += g * powder.kcalPer100g    / 100.0
                    dayProtein += g * powder.proteinPer100g / 100.0
                }

                if (goals.minKcalPerDay    != null && dayKcal    < goals.minKcalPerDay)    return true
                if (goals.maxKcalPerDay    != null && dayKcal    > goals.maxKcalPerDay)    return true
                if (goals.minProteinPerDay != null && dayProtein < goals.minProteinPerDay) return true
                if (goals.maxProteinPerDay != null && dayProtein > goals.maxProteinPerDay) return true
                if (goals.minFatPerDay     != null && dayFat     < goals.minFatPerDay)     return true
                if (goals.maxFatPerDay     != null && dayFat     > goals.maxFatPerDay)     return true
                if (goals.minCarbsPerDay   != null && dayCarbs   < goals.minCarbsPerDay)   return true
                if (goals.maxCarbsPerDay   != null && dayCarbs   > goals.maxCarbsPerDay)   return true
            }
        }

        // H3 — maxDaysInARow (checked across history boundary)
        if (violatesMaxDaysInARow(state, ctx)) return true

        return false
    }

    private fun hasAnyDayBound(goals: NutritionGoals): Boolean =
        goals.minKcalPerDay    != null || goals.maxKcalPerDay    != null ||
                goals.minProteinPerDay != null || goals.maxProteinPerDay != null ||
                goals.minFatPerDay     != null || goals.maxFatPerDay     != null ||
                goals.minCarbsPerDay   != null || goals.maxCarbsPerDay   != null

    /**
     * For each (category, recipeId) pair, counts the longest run of consecutive days
     * where that recipe appears. Returns true if any run exceeds maxDaysInARow.
     *
     * Only the tail of history that could form a consecutive run with the new plan's
     * start is considered, for efficiency.
     */
    private fun violatesMaxDaysInARow(state: State, ctx: Ctx): Boolean {
        val maxInRow = ctx.config.variety.maxDaysInARow
        val cutoff   = ctx.startDate.minus(maxInRow, DateTimeUnit.DAY)

        // (category, recipeId) → sorted set of dates where it appears
        val appearances = mutableMapOf<Pair<MealCategory, String>, MutableSet<LocalDate>>()

        // Relevant history tail
        for (plan in ctx.history) {
            for (day in plan.days) {
                if (day.date < cutoff) continue
                for (meal in day.meals) {
                    appearances
                        .getOrPut(meal.type to meal.recipeId) { mutableSetOf() }
                        .add(day.date)
                }
            }
        }

        // Current week's state
        for ((slot, recipeId) in state) {
            if (recipeId == NULL_SNACK_ID) continue
            appearances
                .getOrPut(slot.category to recipeId) { mutableSetOf() }
                .add(slot.date)
        }

        for ((_, dates) in appearances) {
            val sorted = dates.sorted()
            var streak = 1
            for (i in 1 until sorted.size) {
                if (sorted[i] == sorted[i - 1].plus(1, DateTimeUnit.DAY)) {
                    streak++
                    if (streak > maxInRow) return true
                } else {
                    streak = 1
                }
            }
        }
        return false
    }

    // ── Protein source variety penalty ─────────────────────────────────────

    /**
     * Penalises plans where lunch or dinner slots share the same dominant protein
     * source (MEAT / FISH / DAIRY_EGGS) within the week.
     */
    private fun proteinSourceVarietyCost(state: State, ctx: Ctx): Double {
        val proteinCategories = setOf(
            IngredientCategory.MEAT, IngredientCategory.FISH, IngredientCategory.DAIRY_EGGS
        )

        // Flatten weighted pool for quick id → Recipe lookup
        val recipeById = ctx.weightedByCategory.values.flatten()
            .associate { it.recipe.id to it.recipe }

        fun dominantSource(recipeId: String): IngredientCategory? =
            recipeById[recipeId]?.ingredients
                ?.filter { it.ingredientId != null }
                ?.mapNotNull { ri ->
                    val ing = ctx.ingredientMap[ri.ingredientId] ?: return@mapNotNull null
                    if (ing.category in proteinCategories) ing.category to (ri.grams ?: 0.0)
                    else null
                }
                ?.maxByOrNull { it.second }?.first

        var penalty = 0.0
        for (cat in listOf(MealCategory.LUNCH, MealCategory.DINNER)) {
            val sources = ctx.activeDays.mapNotNull { date ->
                val recipeId = state[Slot(date, cat, 0)]?.takeIf { it != NULL_SNACK_ID }
                    ?: return@mapNotNull null
                dominantSource(recipeId)
            }
            sources.groupingBy { it }.eachCount().values.forEach { count ->
                penalty += (count - 1).coerceAtLeast(0) * 0.15
            }
        }
        return penalty
    }

    // ── Custom rules penalty ───────────────────────────────────────────────

    private fun customRulesCost(state: State, ctx: Ctx): Double {
        if (ctx.config.rules.isEmpty()) return 0.0

        val recipeById = ctx.weightedByCategory.values.flatten()
            .associate { it.recipe.id to it.recipe }

        // Count how many times each recipe appears in the plan
        val recipeCounts = state.values
            .filter { it != NULL_SNACK_ID }
            .groupingBy { it }.eachCount()

        var penalty = 0.0
        for (rule in ctx.config.rules) {
            when (rule.type) {
                RuleType.INGREDIENT -> {
                    val count = recipeCounts.entries.sumOf { (id, times) ->
                        if (recipeById[id]?.ingredients?.any { it.ingredientId == rule.target } == true)
                            times else 0
                    }
                    when (rule.constraint) {
                        RuleConstraint.MIN_PER_WEEK -> penalty += (rule.value - count).coerceAtLeast(0) * 1.0
                        RuleConstraint.MAX_PER_WEEK -> penalty += (count - rule.value).coerceAtLeast(0) * 1.0
                    }
                }
                RuleType.DIET_CATEGORY -> {
                    // Requires recipe-level diet tags — no-op until Recipe carries diet flags
                }
            }
        }
        return penalty
    }

    // ── Protein powder analytical solve ────────────────────────────────────

    /**
     * Given the recipe-only macros for one day, returns the integer gram amount that:
     *   1. Closes as much of the protein gap as possible
     *   2. Does not push daily kcal above maxKcalPerDay (if set)
     *   3. Does not exceed MAX_POWDER_GRAMS
     *
     * Called inside every cost evaluation so the annealer always sees the true
     * combined score — it cannot find plans that look good only because powder
     * was not yet accounted for.
     */
    private fun computeOptimalPowderGrams(
        dayKcal:    Double,
        dayProtein: Double,
        powder:     ProteinPowder,
        goals:      NutritionGoals
    ): Int {
        val proteinGap = goals.resolvedProtein - dayProtein
        if (proteinGap <= 0.0) return 0

        val gramsForProtein = proteinGap * 100.0 / powder.proteinPer100g

        // If no kcal ceiling is set, allow up to MAX_POWDER_GRAMS
        val kcalHeadroom    = (goals.maxKcalPerDay ?: (dayKcal + MAX_POWDER_GRAMS * powder.kcalPer100g / 100.0)) - dayKcal
        val gramsForKcal    = if (kcalHeadroom <= 0.0) 0.0 else kcalHeadroom * 100.0 / powder.kcalPer100g

        return minOf(gramsForProtein, gramsForKcal, MAX_POWDER_GRAMS)
            .coerceAtLeast(0.0)
            .toInt()   // floor — never overshoot the kcal ceiling
    }

    // ── History recency index ──────────────────────────────────────────────

    private fun buildHistoryRecency(history: List<MealPlan>): Map<String, LocalDate> {
        val map = mutableMapOf<String, LocalDate>()
        for (plan in history) {
            for (day in plan.days) {
                for (meal in day.meals) {
                    val prev = map[meal.recipeId]
                    if (prev == null || day.date > prev) map[meal.recipeId] = day.date
                }
            }
        }
        return map
    }

    // ── Output assembly ────────────────────────────────────────────────────

    private fun buildMealPlan(state: State, ctx: Ctx): MealPlan {
        val goals   = ctx.config.goals
        val endDate = ctx.startDate.plus(6, DateTimeUnit.DAY)

        val days = ctx.activeDays.map { date ->
            // Real meals only, sorted by category ordinal then slot index
            val realEntries = state.entries
                .filter { it.key.date == date && it.value != NULL_SNACK_ID }
                .sortedWith(compareBy({ it.key.category.ordinal }, { it.key.index }))

            val meals = realEntries.map { (slot, recipeId) ->
                MealSlot(type = slot.category, recipeId = recipeId)
            }

            val dayKcal    = realEntries.sumOf { ctx.nutritionMap[it.value]?.kcal    ?: 0.0 }
            val dayProtein = realEntries.sumOf { ctx.nutritionMap[it.value]?.protein ?: 0.0 }

            val powderGrams = ctx.config.proteinPowder?.let { powder ->
                if (powder.autoFillGap)
                    computeOptimalPowderGrams(dayKcal, dayProtein, powder, goals).toDouble()
                else 0.0
            } ?: 0.0

            DayPlan(
                id                 = UUID.randomUUID().toString(),
                date               = date,
                meals              = meals,
                proteinPowderGrams = powderGrams,
                goal               = DailyGoal(
                    kcalTarget    = goals.kcalTarget.toInt(),
                    proteinTarget = goals.resolvedProtein.toInt()
                )
            )
        }

        return MealPlan(
            id        = UUID.randomUUID().toString(),
            name      = planName(ctx.startDate),
            startDate = ctx.startDate,
            endDate   = endDate,
            days      = days
        )
    }

    private fun buildEmptyPlan(startDate: LocalDate, config: MealPlanConfig) = MealPlan(
        id        = UUID.randomUUID().toString(),
        name      = planName(startDate),
        startDate = startDate,
        endDate   = startDate.plus(6, DateTimeUnit.DAY),
        days      = emptyList()
    )

    private fun planName(startDate: LocalDate): String {
        val weekNumber = startDate.dayOfYear / 7 + 1
        return "Week $weekNumber – ${startDate.dayOfMonth} ${startDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${startDate.year}"
    }

    // ── Weighted random ────────────────────────────────────────────────────

    private fun weightedRandom(pool: List<WeightedRecipe>, rng: Random): WeightedRecipe? {
        if (pool.isEmpty()) return null
        val total = pool.sumOf { it.samplingWeight }
        if (total <= 0.0) return pool.random(rng)
        var cursor = rng.nextDouble() * total
        for (rw in pool) {
            cursor -= rw.samplingWeight
            if (cursor <= 0.0) return rw
        }
        return pool.last()
    }
}