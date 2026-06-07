package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "day_plans",
    foreignKeys = [
        ForeignKey(
            entity = MealPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DayPlanEntity(
    @PrimaryKey val id: String,
    val mealPlanId: String,
    val date: LocalDate,
    val proteinPowderGrams: Double,
    val kcalTarget: Int,
    val proteinTarget: Int
)