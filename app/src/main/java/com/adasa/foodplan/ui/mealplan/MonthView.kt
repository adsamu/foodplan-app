package com.adasa.foodplan.ui.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate

// Semantic accent dots
private val AmberDot   = Color(0xFFE8A000)
private val GreenDot   = Color(0xFF1D9E75)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        // Month navigator
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
        val dowLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        Row(modifier = Modifier.fillMaxWidth()) {
            dowLabels.forEach { label ->
                Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))

        // Calendar grid — leading blanks = dayOfWeek.ordinal (Mon=0)
        val firstDay = state.days.first().date
        val leadingBlanks = firstDay.dayOfWeek.ordinal  // 0=Mon, 6=Sun
        val cells: List<MonthDayUi?> = List(leadingBlanks) { null } + state.days

        // Pad to full rows of 7
        val paddedCells = cells + List((7 - cells.size % 7) % 7) { null }

        paddedCells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)) {
                        if (day == null) return@Box  // empty cell

                        val isSelected = selectedDate == day.date
                        val bgColor = when {
                            day.isToday  -> MaterialTheme.colorScheme.primary
                            isSelected   -> MaterialTheme.colorScheme.primaryContainer
                            else         -> MaterialTheme.colorScheme.surfaceContainerLow
                        }
                        val textColor = when {
                            day.isToday  -> MaterialTheme.colorScheme.onPrimary
                            isSelected   -> MaterialTheme.colorScheme.onPrimaryContainer
                            else         -> MaterialTheme.colorScheme.onSurface
                        }
                        val dotColor = when {
                            day.isToday      -> MaterialTheme.colorScheme.onPrimary
                            day.isHighCal    -> AmberDot
                            day.isShoppingDay -> GreenDot
                            else             -> null
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable {
                                    selectedDate = if (selectedDate == day.date) null else day.date
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${day.date.dayOfMonth}",
                                    fontSize = 11.sp,
                                    fontWeight = if (day.isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                            // Dot indicator at bottom
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

        Spacer(Modifier.height(10.dp))

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendItem(AmberDot,  "High calorie")
            LegendItem(GreenDot,  "Shopping day")
            LegendItem(MaterialTheme.colorScheme.primary, "Today")
        }

        Spacer(Modifier.height(12.dp))
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