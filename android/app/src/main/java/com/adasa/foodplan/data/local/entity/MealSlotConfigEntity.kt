package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adasa.foodplan.domain.model.DayMealConfig

@Entity(tableName = "meal_slot_configs")
data class MealSlotConfigEntity(
    @PrimaryKey val dayOfWeek: Int,    // 1=Monday … 7=Sunday (ISO)
    val breakfast: Boolean = false,
    val lunch: Boolean = true,
    val dinner: Boolean = true,
    val snackCount: Int = 0
) {
    fun toDomain() = DayMealConfig(
        breakfast = breakfast,
        lunch = lunch,
        dinner = dinner,
        snackCount = snackCount
    )
}