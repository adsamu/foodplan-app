package com.adasa.foodplan.ui.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate

private val GreenFull  = Color(0xFF1D9E75)
private val AmberHalf  = Color(0xFFE8A000)

@Composable
fun MonthView(
    state: MonthUiState?,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    if (state == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var swipeDrag    by remember { mutableStateOf(0f) }

    // Longest streak of fully completed days
    val longestStreak = remember(state.days) {
        var max = 0; var cur = 0
        state.days.forEach { d ->
            if (d.totalMeals > 0 && d.checkedCount >= d.totalMeals) { cur++; if (cur > max) max = cur }
            else cur = 0
        }
        max
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeDrag < -60) onNext()
                        else if (swipeDrag > 60) onPrevious()
                        swipeDrag = 0f
                    },
                    onHorizontalDrag = { _, amount -> swipeDrag += amount }
                )
            }
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        // Navigator
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Text("‹", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "${monthName(state.month)} ${state.year}",
                fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onNext) {
                Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Day-of-week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M","T","W","T","F","S","S").forEach { label ->
                Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))

        // Calendar grid
        val firstDay     = state.days.first().date
        val leadingBlanks = firstDay.dayOfWeek.ordinal
        val cells: List<MonthDayUi?> = List(leadingBlanks) { null } + state.days
        val paddedCells  = cells + List((7 - cells.size % 7) % 7) { null }

        paddedCells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)) {
                        if (day == null) return@Box

                        val isSelected  = selectedDate == day.date
                        val isFull      = day.totalMeals > 0 && day.checkedCount >= day.totalMeals
                        val isHalf      = day.totalMeals > 0 && day.checkedCount > 0 && !isFull

                        val bgColor = when {
                            day.isToday  -> MaterialTheme.colorScheme.primary
                            isSelected   -> MaterialTheme.colorScheme.primaryContainer
                            isFull       -> GreenFull.copy(alpha = 0.25f)
                            isHalf       -> AmberHalf.copy(alpha = 0.25f)
                            else         -> MaterialTheme.colorScheme.surfaceContainerLow
                        }
                        val textColor = when {
                            day.isToday  -> MaterialTheme.colorScheme.onPrimary
                            isSelected   -> MaterialTheme.colorScheme.onPrimaryContainer
                            else         -> MaterialTheme.colorScheme.onSurface
                        }
                        // Small dot for fully/half completed (when today is not overriding)
                        val dotColor: Color? = when {
                            day.isToday -> null
                            isFull      -> GreenFull
                            isHalf      -> AmberHalf
                            else        -> null
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable { selectedDate = if (selectedDate == day.date) null else day.date },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${day.date.dayOfMonth}",
                                fontSize = 11.sp,
                                fontWeight = if (day.isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                            if (dotColor != null) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .align(Alignment.BottomCenter)
                                        .offset(y = (-3).dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendItem(GreenFull,  "All meals done")
            LegendItem(AmberHalf,  "Partially done")
            LegendItem(MaterialTheme.colorScheme.primary, "Today")
        }

        Spacer(Modifier.height(14.dp))

        // Streak card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(14.dp),
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("🔥", fontSize = 28.sp)
                Column {
                    Text(
                        "$longestStreak",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "day${if (longestStreak != 1) "s" else ""} longest streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${state.fullDays}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "full days this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun monthName(month: kotlinx.datetime.Month): String =
    month.name.lowercase().replaceFirstChar { it.uppercase() }