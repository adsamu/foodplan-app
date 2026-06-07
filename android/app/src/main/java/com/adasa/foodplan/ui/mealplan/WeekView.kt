package com.adasa.foodplan.ui.mealplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
fun WeekView(
    state: WeekUiState?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRecipeClick: (String) -> Unit = {}
) {
    if (state == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    var expandedDate by remember { mutableStateOf<LocalDate?>(null) }
    var swipeDrag    by remember { mutableStateOf(0f) }

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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrevious) {
                    Text("‹", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "Week ${state.weekNumber}  ·  ${formatShortDate(state.startDate)}–${formatShortDate(state.endDate)}",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNext) {
                    Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(state.days) { day ->
            val isExpanded = expandedDate == day.date
            WeekDayRow(
                day           = day,
                isExpanded    = isExpanded,
                onToggle      = { expandedDate = if (isExpanded) null else day.date },
                onRecipeClick = onRecipeClick
            )
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun WeekDayRow(day: WeekDayUi, isExpanded: Boolean, onToggle: () -> Unit, onRecipeClick: (String) -> Unit = {}) {
    val todayBorder = if (day.isToday)
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    else Modifier

    val barColor = if (isExpanded) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(todayBorder)
            .clickable { onToggle() }
    ) {
        // ── Collapsed row ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dateLabel = buildString {
                    append(shortDayName(day.date))
                    append(" ${day.date.dayOfMonth}")
                    if (day.isToday) append(" · Today")
                }
                Text(dateLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Compliance badge
                    if (day.meals.isNotEmpty()) {
                        val badgeBg = when {
                            day.checkedCount >= day.meals.size -> MaterialTheme.colorScheme.tertiaryContainer
                            day.checkedCount * 2 >= day.meals.size -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val badgeText = if (day.checkedCount >= day.meals.size) "✓"
                        else "${day.checkedCount}/${day.meals.size}"
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Text("${day.kcal.toInt()} kcal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Meal name pills
            if (day.mealNames.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    day.mealNames.take(3).forEach { name ->
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 90.dp))
                        }
                    }
                }
            }

            // Kcal progress bar vs target
            val fraction = if (day.kcalTarget > 0) (day.kcal / day.kcalTarget).toFloat().coerceIn(0f, 1f) else 0f
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(2.dp)).background(barColor))
            }
        }

        // ── Expanded detail ───────────────────────────────────────────────────
        AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (day.meals.isEmpty()) {
                        Text("No meals planned", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        day.meals.forEach { meal ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onRecipeClick(meal.recipeId) }
                                    .padding(vertical = 3.dp)
                            ) {
                                // Meal type chip
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(meal.type.displayName, fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(meal.recipeName, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${meal.kcal.toInt()} kcal", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        // Daily macro summary
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val p = day.meals.sumOf { it.protein }.toInt()
                            val f = day.meals.sumOf { it.fat }.toInt()
                            val c = day.meals.sumOf { it.carbs }.toInt()
                            MacroChip("P ${p}g", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                            MacroChip("F ${f}g", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                            MacroChip("C ${c}g", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroChip(label: String, bg: androidx.compose.ui.graphics.Color, text: androidx.compose.ui.graphics.Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

private fun shortDayName(date: LocalDate) = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "Mon"; DayOfWeek.TUESDAY -> "Tue"; DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"; DayOfWeek.FRIDAY -> "Fri"; DayOfWeek.SATURDAY -> "Sat"
    else -> "Sun"
}

private fun formatShortDate(date: LocalDate): String {
    val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "${date.dayOfMonth} $month"
}