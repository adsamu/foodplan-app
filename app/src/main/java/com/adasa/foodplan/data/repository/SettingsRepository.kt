package com.adasa.foodplan.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.adasa.foodplan.data.local.dao.OptimizerSettingsDao
import com.adasa.foodplan.data.local.entity.BatchCookingGroupEntity
import com.adasa.foodplan.data.local.entity.MealSlotConfigEntity
import com.adasa.foodplan.data.local.entity.toEntity
import com.adasa.foodplan.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>,
    private val dao: OptimizerSettingsDao
) {
    companion object {
        // Nutrition goals
        val KCAL_TARGET = doublePreferencesKey("kcal_target")
        val PROTEIN_TARGET = doublePreferencesKey("protein_target")
        val FAT_TARGET = doublePreferencesKey("fat_target")
        val CARBS_TARGET = doublePreferencesKey("carbs_target")
        val AUTO_FIELD = stringPreferencesKey("auto_macro_field")
        val MIN_KCAL = doublePreferencesKey("min_kcal_per_day")
        val MAX_KCAL = doublePreferencesKey("max_kcal_per_day")
        val MIN_PROTEIN = doublePreferencesKey("min_protein_per_day")
        val MAX_PROTEIN = doublePreferencesKey("max_protein_per_day")
        val MIN_FAT = doublePreferencesKey("min_fat_per_day")
        val MAX_FAT = doublePreferencesKey("max_fat_per_day")
        val MIN_CARBS = doublePreferencesKey("min_carbs_per_day")
        val MAX_CARBS = doublePreferencesKey("max_carbs_per_day")
        // Diet
        val DIET_TYPES = stringPreferencesKey("diet_types")
        val ALLERGIES = stringPreferencesKey("allergies")
        val EXCLUDED_INGREDIENT_IDS = stringPreferencesKey("excluded_ingredient_ids")
        val PREFERRED_INGREDIENT_IDS = stringPreferencesKey("preferred_ingredient_ids")
        val DISLIKED_INGREDIENT_IDS = stringPreferencesKey("disliked_ingredient_ids")
        // Variety
        val MAX_DAYS_IN_A_ROW = intPreferencesKey("max_days_in_a_row")
        val UNIQUE_WEEKS = intPreferencesKey("unique_weeks")
        val PROTEIN_VARIETY = booleanPreferencesKey("protein_source_variety")
        // Schedule
        val SNACK_OPTIONAL_FILL = booleanPreferencesKey("snack_optional_fill")
        // Shopping
        val SHOPPING_DAYS = stringPreferencesKey("shopping_days")
        val SHOPPING_INTERVAL = intPreferencesKey("shopping_interval_weeks")
        // Protein powder
        val POWDER_INGREDIENT_ID = stringPreferencesKey("powder_ingredient_id")
        val POWDER_NAME = stringPreferencesKey("powder_name")
        val POWDER_PROTEIN_PER_100G = doublePreferencesKey("powder_protein_per_100g")
        val POWDER_KCAL_PER_100G = doublePreferencesKey("powder_kcal_per_100g")
        val POWDER_GRAMS_IN_STOCK = doublePreferencesKey("powder_grams_in_stock")
        val POWDER_AUTO_FILL = booleanPreferencesKey("powder_auto_fill")
        val POWDER_LOW_STOCK_WARNING = booleanPreferencesKey("powder_low_stock_warning")
        // App
        val THEME = stringPreferencesKey("app_theme")
    }

    // ── Full config assembly ───────────────────────────────────────────────

    suspend fun getMealPlanConfig(): MealPlanConfig {
        val prefs = dataStore.data.first()
        val slotConfigs = dao.getAllMealSlotConfigsOnce()
        val batchGroups = dao.getAllBatchGroupsOnce()
        val rules = dao.getAllRulesOnce()

        return MealPlanConfig(
            schedule = buildScheduleConfig(prefs, slotConfigs, batchGroups),
            goals = buildNutritionGoals(prefs),
            diet = buildDietPreferences(prefs),
            rules = rules.map { it.toDomain() },
            variety = buildVarietyConfig(prefs),
            proteinPowder = buildProteinPowder(prefs),
            shopping = buildShoppingConfig(prefs)
        )
    }

    fun getMealPlanConfigFlow(): Flow<MealPlanConfig> =
        combine(
            dataStore.data,
            dao.getAllMealSlotConfigs(),
            dao.getAllBatchGroups(),
            dao.getAllRules()
        ) { prefs, slots, batches, rules ->
            MealPlanConfig(
                schedule = buildScheduleConfig(prefs, slots, batches),
                goals = buildNutritionGoals(prefs),
                diet = buildDietPreferences(prefs),
                rules = rules.map { it.toDomain() },
                variety = buildVarietyConfig(prefs),
                proteinPowder = buildProteinPowder(prefs),
                shopping = buildShoppingConfig(prefs)
            )
        }

    // ── Builders ──────────────────────────────────────────────────────────

    private fun buildScheduleConfig(
        prefs: androidx.datastore.preferences.core.Preferences,
        slots: List<MealSlotConfigEntity>,
        batches: List<BatchCookingGroupEntity>
    ): MealScheduleConfig {
        val slotMap = slots.associate { entity ->
            DayOfWeek(entity.dayOfWeek) to entity.toDomain()
        }.let { map ->
            DayOfWeek.entries.associateWith { map[it] ?: defaultDayConfig(it) }
        }
        return MealScheduleConfig(
            mealSlots = slotMap,
            batchGroups = batches.map { it.toDomain() },
            snackOptionalFill = prefs[SNACK_OPTIONAL_FILL] ?: true
        )
    }

    private fun buildNutritionGoals(prefs: androidx.datastore.preferences.core.Preferences) =
        NutritionGoals(
            kcalTarget = prefs[KCAL_TARGET] ?: 1450.0,
            proteinTarget = prefs[PROTEIN_TARGET],
            fatTarget = prefs[FAT_TARGET],
            carbsTarget = prefs[CARBS_TARGET],
            autoField = prefs[AUTO_FIELD]?.let { MacroField.valueOf(it) } ?: MacroField.PROTEIN,
            minKcalPerDay = prefs[MIN_KCAL],
            maxKcalPerDay = prefs[MAX_KCAL],
            minProteinPerDay = prefs[MIN_PROTEIN],
            maxProteinPerDay = prefs[MAX_PROTEIN],
            minFatPerDay = prefs[MIN_FAT],
            maxFatPerDay = prefs[MAX_FAT],
            minCarbsPerDay = prefs[MIN_CARBS],
            maxCarbsPerDay = prefs[MAX_CARBS]
        )

    private fun buildDietPreferences(prefs: androidx.datastore.preferences.core.Preferences) =
        DietPreferences(
            dietTypes = prefs[DIET_TYPES]?.splitToSet()
                ?.mapNotNull { runCatching { DietType.valueOf(it) }.getOrNull() }
                ?.toSet() ?: emptySet(),
            allergies = prefs[ALLERGIES]?.splitToSet()
                ?.mapNotNull { runCatching { AllergyType.valueOf(it) }.getOrNull() }
                ?.toSet() ?: emptySet(),
            excludedIngredientIds = prefs[EXCLUDED_INGREDIENT_IDS]?.splitToSet() ?: emptySet(),
            preferredIngredientIds = prefs[PREFERRED_INGREDIENT_IDS]?.splitToSet() ?: emptySet(),
            dislikedIngredientIds = prefs[DISLIKED_INGREDIENT_IDS]?.splitToSet() ?: emptySet()
        )

    private fun buildVarietyConfig(prefs: androidx.datastore.preferences.core.Preferences) =
        VarietyConfig(
            maxDaysInARow = prefs[MAX_DAYS_IN_A_ROW] ?: 2,
            uniqueWeeksBeforeRepeat = prefs[UNIQUE_WEEKS] ?: 3,
            proteinSourceVariety = prefs[PROTEIN_VARIETY] ?: true
        )

    private fun buildProteinPowder(prefs: androidx.datastore.preferences.core.Preferences): ProteinPowder? {
        val id   = prefs[POWDER_INGREDIENT_ID] ?: return null
        val name = prefs[POWDER_NAME] ?: return null
        return ProteinPowder(
            ingredientId   = id,
            name           = name,
            proteinPer100g = prefs[POWDER_PROTEIN_PER_100G] ?: 0.0,
            kcalPer100g    = prefs[POWDER_KCAL_PER_100G]    ?: 0.0,
            gramsInStock   = prefs[POWDER_GRAMS_IN_STOCK]   ?: 0.0,
            autoFillGap    = prefs[POWDER_AUTO_FILL]         ?: true,
            lowStockWarning = prefs[POWDER_LOW_STOCK_WARNING] ?: true
        )
    }

    private fun buildShoppingConfig(prefs: androidx.datastore.preferences.core.Preferences) =
        ShoppingConfig(
            shoppingDays = prefs[SHOPPING_DAYS]?.splitToSet()
                ?.mapNotNull { runCatching { DayOfWeek(it.toInt()) }.getOrNull() }
                ?.toSet() ?: setOf(DayOfWeek.SUNDAY),
            intervalWeeks = prefs[SHOPPING_INTERVAL] ?: 1
        )

    private fun defaultDayConfig(day: DayOfWeek) = DayMealConfig(
        breakfast = false,
        lunch = true,
        dinner = true,
        snackCount = if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) 1 else 0
    )

    // ── Individual setters ────────────────────────────────────────────────

    suspend fun setKcalTarget(kcal: Double) = dataStore.edit { it[KCAL_TARGET] = kcal }
    suspend fun setProteinTarget(g: Double?) = dataStore.edit {
        if (g != null) it[PROTEIN_TARGET] = g else it.remove(PROTEIN_TARGET)
    }
    suspend fun setFatTarget(g: Double?) = dataStore.edit {
        if (g != null) it[FAT_TARGET] = g else it.remove(FAT_TARGET)
    }
    suspend fun setCarbsTarget(g: Double?) = dataStore.edit {
        if (g != null) it[CARBS_TARGET] = g else it.remove(CARBS_TARGET)
    }
    suspend fun setAutoField(field: MacroField) = dataStore.edit { it[AUTO_FIELD] = field.name }
    suspend fun setMinKcal(v: Double?) = dataStore.edit { if (v != null) it[MIN_KCAL] = v else it.remove(MIN_KCAL) }
    suspend fun setMaxKcal(v: Double?) = dataStore.edit { if (v != null) it[MAX_KCAL] = v else it.remove(MAX_KCAL) }
    suspend fun setMinProtein(v: Double?) = dataStore.edit { if (v != null) it[MIN_PROTEIN] = v else it.remove(MIN_PROTEIN) }
    suspend fun setMaxProtein(v: Double?) = dataStore.edit { if (v != null) it[MAX_PROTEIN] = v else it.remove(MAX_PROTEIN) }
    suspend fun setMinFat(v: Double?) = dataStore.edit { if (v != null) it[MIN_FAT] = v else it.remove(MIN_FAT) }
    suspend fun setMaxFat(v: Double?) = dataStore.edit { if (v != null) it[MAX_FAT] = v else it.remove(MAX_FAT) }
    suspend fun setMinCarbs(v: Double?) = dataStore.edit { if (v != null) it[MIN_CARBS] = v else it.remove(MIN_CARBS) }
    suspend fun setMaxCarbs(v: Double?) = dataStore.edit { if (v != null) it[MAX_CARBS] = v else it.remove(MAX_CARBS) }

    suspend fun setDietTypes(types: Set<DietType>) = dataStore.edit {
        it[DIET_TYPES] = types.joinToString(",") { t -> t.name }
    }
    suspend fun setAllergies(allergies: Set<AllergyType>) = dataStore.edit {
        it[ALLERGIES] = allergies.joinToString(",") { a -> a.name }
    }
    suspend fun setExcludedIngredients(ids: Set<String>) = dataStore.edit {
        it[EXCLUDED_INGREDIENT_IDS] = ids.joinToString(",")
    }
    suspend fun setPreferredIngredients(ids: Set<String>) = dataStore.edit {
        it[PREFERRED_INGREDIENT_IDS] = ids.joinToString(",")
    }
    suspend fun setDislikedIngredients(ids: Set<String>) = dataStore.edit {
        it[DISLIKED_INGREDIENT_IDS] = ids.joinToString(",")
    }

    suspend fun setVariety(config: VarietyConfig) = dataStore.edit {
        it[MAX_DAYS_IN_A_ROW] = config.maxDaysInARow
        it[UNIQUE_WEEKS] = config.uniqueWeeksBeforeRepeat
        it[PROTEIN_VARIETY] = config.proteinSourceVariety
    }

    suspend fun setSnackOptionalFill(enabled: Boolean) = dataStore.edit {
        it[SNACK_OPTIONAL_FILL] = enabled
    }

    suspend fun setShoppingDays(days: Set<DayOfWeek>) = dataStore.edit {
        it[SHOPPING_DAYS] = days.joinToString(",") { d -> d.isoDayNumber.toString() }
    }
    suspend fun setShoppingInterval(weeks: Int) = dataStore.edit {
        it[SHOPPING_INTERVAL] = weeks
    }

    suspend fun setProteinPowder(
        ingredientId:   String,
        name:           String,
        proteinPer100g: Double,
        kcalPer100g:    Double,
        gramsInStock:   Double
    ) = dataStore.edit {
        it[POWDER_INGREDIENT_ID]   = ingredientId
        it[POWDER_NAME]            = name
        it[POWDER_PROTEIN_PER_100G] = proteinPer100g
        it[POWDER_KCAL_PER_100G]   = kcalPer100g
        it[POWDER_GRAMS_IN_STOCK]  = gramsInStock
    }

    suspend fun restockPowder(grams: Double) = dataStore.edit {
        it[POWDER_GRAMS_IN_STOCK] = grams
    }
    suspend fun setPowderAutoFill(enabled: Boolean) = dataStore.edit { it[POWDER_AUTO_FILL] = enabled }
    suspend fun setPowderLowStockWarning(enabled: Boolean) = dataStore.edit { it[POWDER_LOW_STOCK_WARNING] = enabled }

    suspend fun setTheme(theme: String) = dataStore.edit { it[THEME] = theme }
    fun getThemeFlow(): Flow<String> = dataStore.data.map { it[THEME] ?: "system" }

    suspend fun setMealSlotConfig(dayOfWeek: DayOfWeek, config: com.adasa.foodplan.domain.model.DayMealConfig) =
        dao.upsertMealSlotConfig(
            MealSlotConfigEntity(
                dayOfWeek  = dayOfWeek.isoDayNumber,
                breakfast  = config.breakfast,
                lunch      = config.lunch,
                dinner     = config.dinner,
                snackCount = config.snackCount
            )
        )

    suspend fun saveBatchGroups(groups: List<BatchCookingGroup>) {
        dao.deleteAllBatchGroups()
        groups.forEachIndexed { i, g ->
            dao.upsertBatchGroup(g.toEntity("batch_$i"))
        }
    }

    suspend fun upsertRule(rule: OptimizerRule) =
        dao.upsertRule(rule.copy(id = rule.id.ifEmpty { UUID.randomUUID().toString() }).toEntity())

    suspend fun deleteRule(ruleId: String) = dao.deleteRuleById(ruleId)

    fun getRulesFlow(): Flow<List<OptimizerRule>> =
        dao.getAllRules().map { it.map { e -> e.toDomain() } }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun String.splitToSet(): Set<String> =
        split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}