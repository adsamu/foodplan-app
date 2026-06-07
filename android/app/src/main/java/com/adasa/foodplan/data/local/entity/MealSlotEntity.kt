package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.adasa.foodplan.domain.model.MealCategory

@Entity(
    tableName = "meal_slots",
    primaryKeys = ["dayPlanId", "type", "slotIndex"],
    foreignKeys = [
        ForeignKey(
            entity = DayPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MealSlotEntity(
    val dayPlanId: String,
    val type: MealCategory,
    val slotIndex: Int = 0,
    val recipeId: String
)