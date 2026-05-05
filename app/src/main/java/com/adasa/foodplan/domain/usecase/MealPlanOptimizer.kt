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

private typealias State = Map<MealPlanOptimizer.Slot, String>

object MealPlanOptimizer {

    private const val NULL_SNACK_ID     = "null_snack"
    private const val EXTRA_SNACK_SLOTS = 3
    private const val MAX_ITERATIONS    = 4_000
    private const val INITIAL_TEMP      = 0.35
    private const val MAX_POWDER_GRAMS  = 100.0

    private const val W_KCAL    = 3.0
    private const val W_PROTEIN = 2.0
    private const val W_FAT     = 1.0
    private const val W_CARBS   = 1.0
    private const val W_VARIETY = 1.0

    fun generate(
        history:       List<MealPlan>,
        config:        MealPlanConfig,
        recipes:       List<Recipe>,
        ratings:       List<RecipeRating>,
        nutritionMap:  Map<String, RecipeNutrition>,
        ingredientMap: Map<String, Ingredient>,
        startDate:     LocalDate
    ): MealPlan {
        val rng       = Random(System.currentTimeMillis())
        val ratingMap = ratings.associateBy { it.recipeId }
        val variety   = config.variety

        val recencyIndices  = buildRecencyIndices(history, variety)
        val categoryToGroup = buildCategoryToGroup(variety)
        val weightedRecipes = buildWeightedRecipes(recipes, ratingMap)
        val poolsByCategory = buildPoolsByCategory(weightedRecipes)
        val activeDays      = buildActiveDays(startDate, config.schedule)

        if (activeDays.isEmpty()) return buildEmptyPlan(startDate, config)
        validatePools(activeDays, config.schedule, poolsByCategory)

        val frozenSlots = resolveBatchGroups(
            config.schedule.batchGroups, activeDays, poolsByCategory,
            recencyIndices, categoryToGroup, variety, startDate, rng
        )

        val ctx = Ctx(
            config, nutritionMap, ingredientMap, ratingMap,
            poolsByCategory, frozenSlots, activeDays,
            recencyIndices, categoryToGroup, startDate, history
        )

        var state     = initState(activeDays, config.schedule, poolsByCategory, frozenSlots, ctx, rng)
        var stateCost = cost(state, ctx)
        var bestState = state
        var bestCost  = stateCost

        repeat(MAX_ITERATIONS) { i ->
            val temp      = INITIAL_TEMP * (1.0 - i.toDouble() / MAX_ITERATIONS)
            val candidate = mutate(state, ctx, rng)
            val candCost  = cost(candidate, ctx)
            val delta     = candCost - stateCost
            if (delta < 0 || (temp > 0.0 && rng.nextDouble() < exp(-delta / temp))) {
                state = candidate; stateCost = candCost
            }
            if (stateCost < bestCost) { bestState = state; bestCost = stateCost }
        }
        return buildMealPlan(bestState, ctx)
    }

    // ── Types ──────────────────────────────────────────────────────────────

    public data class Slot(val date: LocalDate, val category: MealCategory, val index: Int)

    private data class WeightedRecipe(val recipe: Recipe, val samplingWeight: Double, val isPinned: Boolean)

    private data class Ctx(
        val config:             MealPlanConfig,
        val nutritionMap:       Map<String, RecipeNutrition>,
        val ingredientMap:      Map<String, Ingredient>,
        val ratingMap:          Map<String, RecipeRating>,
        val weightedByCategory: Map<MealCategory, List<WeightedRecipe>>,
        val frozenSlots:        Map<Slot, String>,
        val activeDays:         List<LocalDate>,
        val recencyIndices:     Map<Set<MealCategory>, Map<String, LocalDate>>,
        val categoryToGroup:    Map<MealCategory, Set<MealCategory>>,
        val startDate:          LocalDate,
        val history:            List<MealPlan>
    )

    // ── Recency ────────────────────────────────────────────────────────────

    private fun buildRecencyIndices(history: List<MealPlan>, variety: VarietyConfig): Map<Set<MealCategory>, Map<String, LocalDate>> {
        val result     = variety.recencyGroups.associateWith { mutableMapOf<String, LocalDate>() }
        val catToGroup = buildCategoryToGroup(variety)
        for (plan in history) {
            for (day in plan.days) {
                for (meal in day.meals) {
                    val group = catToGroup[meal.type] ?: continue
                    val index = result[group] ?: continue
                    val prev  = index[meal.recipeId]
                    if (prev == null || day.date > prev) index[meal.recipeId] = day.date
                }
            }
        }
        return result
    }

    private fun buildCategoryToGroup(variety: VarietyConfig): Map<MealCategory, Set<MealCategory>> =
        variety.recencyGroups.flatMap { group -> group.map { cat -> cat to group } }.toMap()

    private fun recencyPenaltyFor(recipeId: String, category: MealCategory, ctx: Ctx): Double {
        val group    = ctx.categoryToGroup[category] ?: return 0.0
        val lastUsed = ctx.recencyIndices[group]?.get(recipeId) ?: return 0.0
        val days     = lastUsed.daysUntil(ctx.startDate).toDouble()
        val window   = ctx.config.variety.level.recencyWindowDays.toDouble()
        return (1.0 - days / window).coerceIn(0.0, 1.0)
    }

    // ── Pool construction ──────────────────────────────────────────────────

    private fun buildWeightedRecipes(recipes: List<Recipe>, ratingMap: Map<String, RecipeRating>) =
        recipes.map { r ->
            val rating = ratingMap[r.id]
            WeightedRecipe(r, starsToWeight(rating?.stars), rating?.isPinned ?: false)
        }

    private fun starsToWeight(stars: Int?): Double = when (stars) {
        null -> 1.0; 1 -> 0.0; 2 -> 0.15; 3 -> 1.0; 4 -> 2.0; 5 -> 4.0; else -> 1.0
    }

    private fun buildPoolsByCategory(weighted: List<WeightedRecipe>): Map<MealCategory, List<WeightedRecipe>> {
        val map = MealCategory.entries.associateWith { mutableListOf<WeightedRecipe>() }
        for (rw in weighted) {
            for (cat in rw.recipe.mealCategories) {
                (map[cat] as MutableList).add(rw)
            }
        }
        return map
    }

    private fun validatePools(activeDays: List<LocalDate>, schedule: MealScheduleConfig, pools: Map<MealCategory, List<WeightedRecipe>>) {
        val required = activeDays.flatMap { date ->
            val dc = schedule.mealSlots[date.dayOfWeek] ?: return@flatMap emptyList()
            buildList {
                if (dc.breakfast)      add(MealCategory.BREAKFAST)
                if (dc.lunch)          add(MealCategory.LUNCH)
                if (dc.dinner)         add(MealCategory.DINNER)
                if (dc.snackCount > 0) add(MealCategory.SNACK)
            }
        }.toSet()
        for (cat in required) {
            if (pools[cat].isNullOrEmpty()) throw IllegalStateException(
                "No eligible recipes for ${cat.displayName}. Add recipes or adjust your settings."
            )
        }
    }

    private fun buildActiveDays(startDate: LocalDate, schedule: MealScheduleConfig) =
        (0 until 7).map { startDate.plus(it, DateTimeUnit.DAY) }
            .filter { schedule.mealSlots[it.dayOfWeek]?.isActive == true }

    // ── Batch pre-assignment ───────────────────────────────────────────────

    /**
     * Resolves batch groups BREAKFAST→LUNCH→DINNER→SNACK so shared-group exclusions
     * propagate correctly (lunch assigned before dinner when they share a recency group).
     * Recipes in the shared group's recency history are excluded regardless of level.
     */
    private fun resolveBatchGroups(
        batchGroups:     List<BatchCookingGroup>,
        activeDays:      List<LocalDate>,
        poolsByCategory: Map<MealCategory, List<WeightedRecipe>>,
        recencyIndices:  Map<Set<MealCategory>, Map<String, LocalDate>>,
        categoryToGroup: Map<MealCategory, Set<MealCategory>>,
        variety:         VarietyConfig,
        startDate:       LocalDate,
        rng:             Random
    ): Map<Slot, String> {
        val frozen             = mutableMapOf<Slot, String>()
        val assignedByCategory = mutableMapOf<MealCategory, MutableSet<String>>()
        val windowDays         = variety.level.recencyWindowDays

        for (category in listOf(MealCategory.BREAKFAST, MealCategory.LUNCH, MealCategory.DINNER, MealCategory.SNACK)) {
            val groups = batchGroups.filter { it.meal == category }
            if (groups.isEmpty()) continue
            val pool  = poolsByCategory[category] ?: continue
            val group = categoryToGroup[category]
            val index = group?.let { recencyIndices[it] } ?: emptyMap()

            // Recipes used in the shared recency group's history within the window (soft preference)
            val recencyExcluded = mutableSetOf<String>()
            index.forEach { (id, lastUsed) ->
                if (lastUsed.daysUntil(startDate) < windowDays) recencyExcluded += id
            }
            // Recipes already assigned to other categories in the same group this week (hard exclusion)
            val crossExcluded = mutableSetOf<String>()
            group?.forEach { otherCat ->
                if (otherCat != category) assignedByCategory[otherCat]?.let { crossExcluded += it }
            }

            val usedThisCategory = mutableSetOf<String>()
            for (g in groups.sortedBy { it.batchNumber }) {
                val notUsed         = pool.filter { it.recipe.id !in usedThisCategory }
                val noCrossConflict = notUsed.filter { it.recipe.id !in crossExcluded }
                // Prefer recipes outside the recency window; fall back gracefully if pool is small.
                // Final fallback allows recipe reuse within the category (pool exhausted) so that
                // batch pairs are always frozen together rather than handed to the annealer as
                // independent unfrozen slots — which would silently break the pair constraint.
                val preferred  = noCrossConflict.filter { it.recipe.id !in recencyExcluded }
                val available  = preferred
                    .ifEmpty { noCrossConflict }  // ignore recency, keep cross-exclusion
                    .ifEmpty { notUsed }           // ignore cross-exclusion, avoid same-category reuse
                    .ifEmpty { pool.filter { it.recipe.id !in crossExcluded } } // allow reuse, keep cross
                    .ifEmpty { pool }              // absolute last resort: use anything
                val picked     = weightedRandom(available, rng) ?: continue
                usedThisCategory += picked.recipe.id
                activeDays.filter { it.dayOfWeek in g.days }
                    .forEach { date -> frozen[Slot(date, category, 0)] = picked.recipe.id }
            }
            assignedByCategory[category] = usedThisCategory
        }
        return frozen
    }

    // ── State init ─────────────────────────────────────────────────────────

    private fun slotsForDay(date: LocalDate, schedule: MealScheduleConfig): List<Slot> {
        val dc = schedule.mealSlots[date.dayOfWeek] ?: return emptyList()
        return buildList {
            if (dc.breakfast) add(Slot(date, MealCategory.BREAKFAST, 0))
            if (dc.lunch)     add(Slot(date, MealCategory.LUNCH,     0))
            if (dc.dinner)    add(Slot(date, MealCategory.DINNER,    0))
            // snackCount: -1 = ∞ (optimizer fills freely), 0 = none, 1-3 = explicit required count
            // For -1 and 1-3: add EXTRA_SNACK_SLOTS optional slots on top
            val required   = dc.snackCount.coerceAtLeast(0)
            val snackTotal = if (dc.snackCount == 0) 0 else required + EXTRA_SNACK_SLOTS
            repeat(snackTotal) { i -> add(Slot(date, MealCategory.SNACK, i)) }
        }
    }

    private fun initState(
        activeDays: List<LocalDate>, schedule: MealScheduleConfig,
        poolsByCategory: Map<MealCategory, List<WeightedRecipe>>,
        frozen: Map<Slot, String>, ctx: Ctx, rng: Random
    ): State {
        val state = mutableMapOf<Slot, String>()
        for (date in activeDays) {
            val dc = schedule.mealSlots[date.dayOfWeek] ?: continue
            for (slot in slotsForDay(date, schedule)) {
                state[slot] = when {
                    slot in frozen                         -> frozen[slot]!!
                    slot.category != MealCategory.SNACK   -> {
                        val pool = eligiblePool(slot, ctx, poolsByCategory)
                        weightedRandom(pool, rng)?.recipe?.id
                            ?: throw IllegalStateException("Empty pool for ${slot.category.displayName}")
                    }
                    slot.index < dc.snackCount.coerceAtLeast(0) -> {
                        weightedRandom(eligiblePool(slot, ctx, poolsByCategory), rng)?.recipe?.id ?: NULL_SNACK_ID
                    }
                    else                                   -> NULL_SNACK_ID
                }
            }
        }
        return state
    }

    // ── Mutation ───────────────────────────────────────────────────────────

    private fun mutate(state: State, ctx: Ctx, rng: Random): State {
        val mutable = state.keys.filter { it !in ctx.frozenSlots }
        if (mutable.isEmpty()) return state
        return if (rng.nextDouble() < 0.7) {
            val slot = mutable.random(rng)
            state.toMutableMap().also { it[slot] = pickForSlot(slot, ctx, rng) }
        } else {
            val slot1      = mutable.random(rng)
            val compatible = mutable.filter { it.category == slot1.category && it.date != slot1.date }
            if (compatible.isEmpty()) {
                state.toMutableMap().also { it[slot1] = pickForSlot(slot1, ctx, rng) }
            } else {
                val slot2 = compatible.random(rng)
                state.toMutableMap().also { it[slot1] = state[slot2]!!; it[slot2] = state[slot1]!! }
            }
        }
    }

    private fun pickForSlot(slot: Slot, ctx: Ctx, rng: Random): String {
        if (slot.category == MealCategory.SNACK && rng.nextDouble() < 0.25) return NULL_SNACK_ID
        val pool = eligiblePool(slot, ctx, ctx.weightedByCategory)
        return weightedRandom(pool, rng)?.recipe?.id ?: NULL_SNACK_ID
    }

    private fun eligiblePool(slot: Slot, ctx: Ctx, pools: Map<MealCategory, List<WeightedRecipe>>): List<WeightedRecipe> {
        val pool = pools[slot.category] ?: return emptyList()
        if (!ctx.config.variety.level.hardExcludeWithinWindow) return pool
        val group  = ctx.categoryToGroup[slot.category] ?: return pool
        val index  = ctx.recencyIndices[group] ?: return pool
        val windowDays = ctx.config.variety.level.recencyWindowDays
        return pool.filter { rw ->
            val last = index[rw.recipe.id] ?: return@filter true
            last.daysUntil(ctx.startDate) >= windowDays
        }
    }

    // ── Cost ───────────────────────────────────────────────────────────────

    private fun cost(state: State, ctx: Ctx): Double {
        if (violatesHardConstraints(state, ctx)) return Double.MAX_VALUE

        val goals          = ctx.config.goals
        val activeDayCount = ctx.activeDays.size.toDouble()
        var weeklyKcal = 0.0; var weeklyProtein = 0.0; var weeklyFat = 0.0; var weeklyCarbs = 0.0
        var recencyTotal   = 0.0

        for (date in ctx.activeDays) {
            var dayKcal = 0.0; var dayProtein = 0.0; var dayFat = 0.0; var dayCarbs = 0.0
            for ((slot, recipeId) in state) {
                if (slot.date != date || recipeId == NULL_SNACK_ID) continue
                val n = ctx.nutritionMap[recipeId] ?: continue
                dayKcal    += n.kcal;    dayProtein += n.protein
                dayFat     += n.fat;     dayCarbs   += n.carbs
                recencyTotal += recencyPenaltyFor(recipeId, slot.category, ctx)
            }
            val powder = ctx.config.proteinPowder
            if (powder != null && powder.autoFillGap) {
                val g = computeOptimalPowderGrams(dayKcal, dayProtein, powder, goals)
                dayKcal += g * powder.kcalPer100g / 100.0; dayProtein += g * powder.proteinPer100g / 100.0
            }
            weeklyKcal += dayKcal; weeklyProtein += dayProtein; weeklyFat += dayFat; weeklyCarbs += dayCarbs
        }

        val kcalT = goals.kcalTarget * activeDayCount; val protT = goals.resolvedProtein * activeDayCount
        val fatT  = goals.resolvedFat * activeDayCount; val carbT = goals.resolvedCarbs  * activeDayCount

        val soft =
            W_KCAL    * abs(weeklyKcal    - kcalT) / kcalT.coerceAtLeast(1.0) +
                    W_PROTEIN * abs(weeklyProtein - protT) / protT.coerceAtLeast(1.0) +
                    W_FAT     * abs(weeklyFat     - fatT)  / fatT.coerceAtLeast(1.0)  +
                    W_CARBS   * abs(weeklyCarbs   - carbT) / carbT.coerceAtLeast(1.0) +
                    ctx.config.variety.level.penaltyWeight * recencyTotal

        val variety = if (ctx.config.variety.proteinSourceVariety) W_VARIETY * proteinSourceVarietyCost(state, ctx) else 0.0
        return soft + variety + customRulesCost(state, ctx)
    }

    // ── Hard constraints ───────────────────────────────────────────────────

    private fun violatesHardConstraints(state: State, ctx: Ctx): Boolean {
        val schedule = ctx.config.schedule
        val goals    = ctx.config.goals
        val variety  = ctx.config.variety

        for (date in ctx.activeDays) {
            val dc       = schedule.mealSlots[date.dayOfWeek] ?: continue
            val daySlots = state.entries.filter { it.key.date == date }

            // H1 — required slots filled
            if (dc.breakfast && daySlots.none { it.key.category == MealCategory.BREAKFAST && it.value != NULL_SNACK_ID }) return true
            if (dc.lunch     && daySlots.none { it.key.category == MealCategory.LUNCH     && it.value != NULL_SNACK_ID }) return true
            if (dc.dinner    && daySlots.none { it.key.category == MealCategory.DINNER    && it.value != NULL_SNACK_ID }) return true
            repeat(dc.snackCount.coerceAtLeast(0)) { i ->
                if (state[Slot(date, MealCategory.SNACK, i)].let { it == null || it == NULL_SNACK_ID }) return true
            }

            // H_DIFFER — lunch ≠ dinner on same day
            if (variety.lunchDinnerMustDiffer) {
                val l = daySlots.firstOrNull { it.key.category == MealCategory.LUNCH  && it.value != NULL_SNACK_ID }?.value
                val d = daySlots.firstOrNull { it.key.category == MealCategory.DINNER && it.value != NULL_SNACK_ID }?.value
                if (l != null && l == d) return true
            }

            // H4 — per-day hard bounds
            if (hasAnyDayBound(goals)) {
                val real = daySlots.filter { it.value != NULL_SNACK_ID }
                var dayKcal    = real.sumOf { ctx.nutritionMap[it.value]?.kcal    ?: 0.0 }
                var dayProtein = real.sumOf { ctx.nutritionMap[it.value]?.protein ?: 0.0 }
                var dayFat     = real.sumOf { ctx.nutritionMap[it.value]?.fat     ?: 0.0 }
                var dayCarbs   = real.sumOf { ctx.nutritionMap[it.value]?.carbs   ?: 0.0 }
                val powder = ctx.config.proteinPowder
                if (powder != null && powder.autoFillGap) {
                    val g = computeOptimalPowderGrams(dayKcal, dayProtein, powder, goals)
                    dayKcal += g * powder.kcalPer100g / 100.0; dayProtein += g * powder.proteinPer100g / 100.0
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

        if (violatesMaxConsecutiveDays(state, ctx)) return true
        if (violatesMaxTimesPerWeek(state, ctx))    return true
        return false
    }

    private fun hasAnyDayBound(g: NutritionGoals) =
        g.minKcalPerDay != null || g.maxKcalPerDay != null ||
                g.minProteinPerDay != null || g.maxProteinPerDay != null ||
                g.minFatPerDay != null || g.maxFatPerDay != null ||
                g.minCarbsPerDay != null || g.maxCarbsPerDay != null

    private fun violatesMaxConsecutiveDays(state: State, ctx: Ctx): Boolean {
        val variety      = ctx.config.variety
        val maxAcrossAll = variety.perCategory.values.mapNotNull { it.maxConsecutiveDays }.maxOrNull() ?: return false
        val cutoff       = ctx.startDate.minus(maxAcrossAll, DateTimeUnit.DAY)
        val appearances  = mutableMapOf<Pair<MealCategory, String>, MutableSet<LocalDate>>()

        for (plan in ctx.history) {
            for (day in plan.days) {
                if (day.date < cutoff) continue
                for (meal in day.meals) {
                    appearances.getOrPut(meal.type to meal.recipeId) { mutableSetOf() }.add(day.date)
                }
            }
        }
        for ((slot, recipeId) in state) {
            if (recipeId == NULL_SNACK_ID || slot in ctx.frozenSlots) continue
            appearances.getOrPut(slot.category to recipeId) { mutableSetOf() }.add(slot.date)
        }

        for ((key, dates) in appearances) {
            val category = key.first
            val maxInRow = variety.perCategory[category]?.maxConsecutiveDays ?: continue
            val sorted   = dates.sorted()
            var streak   = 1
            for (i in 1 until sorted.size) {
                streak = if (sorted[i] == sorted[i - 1].plus(1, DateTimeUnit.DAY)) streak + 1 else 1
                if (streak > maxInRow) return true
            }
        }
        return false
    }

    private fun violatesMaxTimesPerWeek(state: State, ctx: Ctx): Boolean {
        for ((category, catVariety) in ctx.config.variety.perCategory) {
            val max = catVariety.maxTimesPerWeek ?: continue
            val counts = mutableMapOf<String, Int>()
            for ((slot, recipeId) in state) {
                if (slot.category != category || slot in ctx.frozenSlots || recipeId == NULL_SNACK_ID) continue
                counts[recipeId] = (counts[recipeId] ?: 0) + 1
            }
            if (counts.values.any { it > max }) return true
        }
        return false
    }

    // ── Protein source variety ─────────────────────────────────────────────

    private fun proteinSourceVarietyCost(state: State, ctx: Ctx): Double {
        val proteinCats = setOf(IngredientCategory.MEAT, IngredientCategory.FISH, IngredientCategory.DAIRY_EGGS)
        val recipeById  = ctx.weightedByCategory.values.flatten().associate { it.recipe.id to it.recipe }

        fun dominantSource(id: String): IngredientCategory? =
            recipeById[id]?.ingredients?.filter { it.ingredientId != null }
                ?.mapNotNull { ri ->
                    val ing = ctx.ingredientMap[ri.ingredientId] ?: return@mapNotNull null
                    if (ing.category in proteinCats) ing.category to (ri.grams ?: 0.0) else null
                }?.maxByOrNull { it.second }?.first

        var penalty = 0.0
        for (cat in listOf(MealCategory.LUNCH, MealCategory.DINNER)) {
            ctx.activeDays
                .mapNotNull { date -> state[Slot(date, cat, 0)]?.takeIf { it != NULL_SNACK_ID }?.let { dominantSource(it) } }
                .groupingBy { it }.eachCount().values
                .forEach { count -> penalty += (count - 1).coerceAtLeast(0) * 0.15 }
        }
        return penalty
    }

    // ── Custom rules ───────────────────────────────────────────────────────

    private fun customRulesCost(state: State, ctx: Ctx): Double {
        if (ctx.config.rules.isEmpty()) return 0.0
        val recipeById   = ctx.weightedByCategory.values.flatten().associate { it.recipe.id to it.recipe }
        val recipeCounts = state.values.filter { it != NULL_SNACK_ID }.groupingBy { it }.eachCount()
        var penalty = 0.0
        for (rule in ctx.config.rules) {
            if (rule.type == RuleType.INGREDIENT) {
                val count = recipeCounts.entries.sumOf { (id, times) ->
                    if (recipeById[id]?.ingredients?.any { it.ingredientId == rule.target } == true) times else 0
                }
                when (rule.constraint) {
                    RuleConstraint.MIN_PER_WEEK -> penalty += (rule.value - count).coerceAtLeast(0).toDouble()
                    RuleConstraint.MAX_PER_WEEK -> penalty += (count - rule.value).coerceAtLeast(0).toDouble()
                }
            }
        }
        return penalty
    }

    // ── Powder ─────────────────────────────────────────────────────────────

    private fun computeOptimalPowderGrams(dayKcal: Double, dayProtein: Double, powder: ProteinPowder, goals: NutritionGoals): Int {
        val gap = goals.resolvedProtein - dayProtein
        if (gap <= 0.0) return 0
        val forProtein = gap * 100.0 / powder.proteinPer100g
        val headroom   = (goals.maxKcalPerDay ?: (dayKcal + MAX_POWDER_GRAMS * powder.kcalPer100g / 100.0)) - dayKcal
        val forKcal    = if (headroom <= 0.0) 0.0 else headroom * 100.0 / powder.kcalPer100g
        return minOf(forProtein, forKcal, MAX_POWDER_GRAMS).coerceAtLeast(0.0).toInt()
    }

    // ── Output ─────────────────────────────────────────────────────────────

    private fun buildMealPlan(state: State, ctx: Ctx): MealPlan {
        val goals   = ctx.config.goals
        val endDate = ctx.startDate.plus(6, DateTimeUnit.DAY)
        val days = ctx.activeDays.map { date ->
            val real = state.entries
                .filter { it.key.date == date && it.value != NULL_SNACK_ID }
                .sortedWith(compareBy({ it.key.category.ordinal }, { it.key.index }))
            val dayKcal    = real.sumOf { ctx.nutritionMap[it.value]?.kcal    ?: 0.0 }
            val dayProtein = real.sumOf { ctx.nutritionMap[it.value]?.protein ?: 0.0 }
            val powderGrams = ctx.config.proteinPowder?.let {
                if (it.autoFillGap) computeOptimalPowderGrams(dayKcal, dayProtein, it, goals).toDouble() else 0.0
            } ?: 0.0
            DayPlan(
                id                 = UUID.randomUUID().toString(),
                date               = date,
                meals              = real.map { (slot, id) -> MealSlot(type = slot.category, recipeId = id) },
                proteinPowderGrams = powderGrams,
                goal               = DailyGoal(goals.kcalTarget.toInt(), goals.resolvedProtein.toInt())
            )
        }
        return MealPlan(UUID.randomUUID().toString(), planName(ctx.startDate), ctx.startDate, endDate, days)
    }

    private fun buildEmptyPlan(startDate: LocalDate, config: MealPlanConfig) = MealPlan(
        UUID.randomUUID().toString(), planName(startDate), startDate, startDate.plus(6, DateTimeUnit.DAY), emptyList()
    )

    private fun planName(d: LocalDate): String {
        val w = d.dayOfYear / 7 + 1
        return "Week $w – ${d.dayOfMonth} ${d.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${d.year}"
    }

    private fun weightedRandom(pool: List<WeightedRecipe>, rng: Random): WeightedRecipe? {
        if (pool.isEmpty()) return null
        val total = pool.sumOf { it.samplingWeight }
        if (total <= 0.0) return pool.random(rng)
        var cursor = rng.nextDouble() * total
        for (rw in pool) { cursor -= rw.samplingWeight; if (cursor <= 0.0) return rw }
        return pool.last()
    }
}