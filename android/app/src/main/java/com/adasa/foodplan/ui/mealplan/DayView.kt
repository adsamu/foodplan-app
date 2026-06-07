package com.adasa.foodplan.ui.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

// No private colour constants — all colours via MaterialTheme below

@Composable
fun DayView(
    state:         DayUiState?,
    onPrevious:    () -> Unit,
    onNext:        () -> Unit,
    onMealClick:   (String) -> Unit,
    checkedMeals:  Set<Int> = emptySet(),
    onMealChecked: (Int) -> Unit = {}
) {
    if (state == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    var swipeDrag by remember { mutableStateOf(0f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeDrag < -60) onNext()
                        else if (swipeDrag > 60) onPrevious()
                        swipeDrag = 0f
                    },
                    onHorizontalDrag = { _, amount -> swipeDrag += amount }
                )
            },
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Day navigator
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Previous day",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatDate(state.date),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Week ${isoWeek(state.date)} · ${dayTypeLabel(state.dayType)} · ${state.kcalTarget} kcal target",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Next day",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Meal cards
        if (state.meals.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow).padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("No meals planned for this day", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(state.meals.size) { index ->
                val slot    = state.meals[index]
                val checked = index in checkedMeals
                MealCard(
                    slot      = slot,
                    checked   = checked,
                    onCheck   = { onMealChecked(index) },
                    onClick   = { onMealClick(slot.recipeId) }
                )
            }
        }

        // Protein powder row
        if (state.proteinPowderGrams > 0) {
            item { ProteinPowderRow(state.proteinPowderGrams, state.kcalTarget, state.nutrition.kcal) }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun MealCard(slot: MealSlotUi, checked: Boolean, onCheck: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tap the circle to check without navigating to recipe
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onCheck() },
            contentAlignment = Alignment.Center
        ) {
            if (checked) Text("✓", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Restaurant, contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(slot.type.displayName.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp)
            Text(slot.recipeName, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                textDecoration = if (checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                modifier = Modifier.padding(top = 1.dp))
            Text("${slot.protein.toInt()}g P · ${slot.fat.toInt()}g F · ${slot.carbs.toInt()}g C",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
        Text("${slot.kcal.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProteinPowderRow(grams: Double, kcalTarget: Int, kcalActual: Double) {
    val extraKcal  = grams * 3.54  // ~354 kcal/100g protein powder
    val extraProt  = grams * 0.72  // ~72g protein/100g
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.FitnessCenter, contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
        Column {
            Text("Core Protein Pro — ${grams}g", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("+${extraProt.toInt()}g protein · +${extraKcal.toInt()} kcal to hit goal",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 1.dp))
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val dayName = when (date.dayOfWeek) {
        DayOfWeek.MONDAY    -> "Monday";    DayOfWeek.TUESDAY  -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"; DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY    -> "Friday";    DayOfWeek.SATURDAY -> "Saturday"
        else                -> "Sunday"
    }
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dayName, ${date.dayOfMonth} $monthName"
}

private fun dayTypeLabel(t: DayType) = when (t) {
    DayType.WEEKDAY -> "Weekday"; DayType.WEEKEND -> "Weekend"; DayType.SUNDAY -> "Shopping day"
}

private fun isoWeek(date: LocalDate): Int {
    val j = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
    return j.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
}