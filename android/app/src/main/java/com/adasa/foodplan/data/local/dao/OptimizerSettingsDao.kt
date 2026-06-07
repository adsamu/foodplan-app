package com.adasa.foodplan.data.local.dao

import androidx.room.*
import com.adasa.foodplan.data.local.entity.BatchCookingGroupEntity
import com.adasa.foodplan.data.local.entity.MealSlotConfigEntity
import com.adasa.foodplan.data.local.entity.OptimizerRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OptimizerSettingsDao {
    // Meal slot config
    @Query("SELECT * FROM meal_slot_configs")
    fun getAllMealSlotConfigs(): Flow<List<MealSlotConfigEntity>>

    @Query("SELECT * FROM meal_slot_configs")
    suspend fun getAllMealSlotConfigsOnce(): List<MealSlotConfigEntity>

    @Upsert
    suspend fun upsertMealSlotConfig(config: MealSlotConfigEntity)

    @Upsert
    suspend fun upsertAllMealSlotConfigs(configs: List<MealSlotConfigEntity>)

    // Batch cooking
    @Query("SELECT * FROM batch_cooking_groups")
    fun getAllBatchGroups(): Flow<List<BatchCookingGroupEntity>>

    @Query("SELECT * FROM batch_cooking_groups")
    suspend fun getAllBatchGroupsOnce(): List<BatchCookingGroupEntity>

    @Upsert
    suspend fun upsertBatchGroup(group: BatchCookingGroupEntity)

    @Query("DELETE FROM batch_cooking_groups")
    suspend fun deleteAllBatchGroups()

    // Optimizer rules
    @Query("SELECT * FROM optimizer_rules")
    fun getAllRules(): Flow<List<OptimizerRuleEntity>>

    @Query("SELECT * FROM optimizer_rules")
    suspend fun getAllRulesOnce(): List<OptimizerRuleEntity>

    @Upsert
    suspend fun upsertRule(rule: OptimizerRuleEntity)

    @Delete
    suspend fun deleteRule(rule: OptimizerRuleEntity)

    @Query("DELETE FROM optimizer_rules WHERE id = :id")
    suspend fun deleteRuleById(id: String)
}