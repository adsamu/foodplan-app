package com.adasa.foodplan.data.local

import androidx.room.TypeConverter
import com.adasa.foodplan.domain.model.ComponentCategory
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.RecipeType
import kotlinx.datetime.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDays().toLong()

    @TypeConverter
    fun toLocalDate(epoch: Long): LocalDate = LocalDate.fromEpochDays(epoch.toInt())

    @TypeConverter
    fun fromRecipeType(type: RecipeType): String = type.name

    @TypeConverter
    fun toRecipeType(value: String): RecipeType = RecipeType.valueOf(value)

    @TypeConverter
    fun fromMealCategories(categories: Set<MealCategory>): String =
        categories.joinToString(",") { it.name }

    @TypeConverter
    fun toMealCategories(value: String): Set<MealCategory> =
        if (value.isEmpty()) emptySet()
        else value.split(",").map { MealCategory.valueOf(it) }.toSet()

    @TypeConverter
    fun fromComponentCategory(category: ComponentCategory?): String? = category?.name

    @TypeConverter
    fun toComponentCategory(value: String?): ComponentCategory? =
        value?.let { ComponentCategory.valueOf(it) }

    @TypeConverter
    fun fromSteps(steps: List<String>): String = steps.joinToString("|||")

    @TypeConverter
    fun toSteps(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("|||")
}