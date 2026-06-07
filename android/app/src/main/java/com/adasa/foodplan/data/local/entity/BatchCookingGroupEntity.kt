package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adasa.foodplan.domain.model.BatchCookingGroup
import com.adasa.foodplan.domain.model.MealCategory
import kotlinx.datetime.isoDayNumber

@Entity(tableName = "batch_cooking_groups")
data class BatchCookingGroupEntity(
    @PrimaryKey val id: String,
    val meal: String,           // MealCategory.name
    val days: String,           // comma-separated ISO day numbers e.g. "1,2,3"
    val batchNumber: Int
) {
    fun toDomain() = BatchCookingGroup(
        meal = MealCategory.valueOf(meal),
        days = days.split(",").map { kotlinx.datetime.DayOfWeek(it.trim().toInt()) }.toSet(),
        batchNumber = batchNumber
    )
}

fun BatchCookingGroup.toEntity(id: String) = BatchCookingGroupEntity(
    id = id,
    meal = meal.name,
    days = days.joinToString(",") { it.isoDayNumber.toString() },
    batchNumber = batchNumber
)