package com.adasa.foodplan.ui.mealplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

// Semantic accent colours
private val AmberAcc    = Color(0xFFE8A000)
private val GreenAcc    = Color(0xFF1D9E75)
private val HCalBg      = Color(0xFFFFF0C2); private val HCalTxt = Color(0xFF7D4E00)
private val ShopBg      = Color(0xFFD8F5E4); private val ShopTxt = Color(0xFF0A3D22)

@Composable
fun WeekView(
    state: WeekUiState?,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    if (state == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Week navigator
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

        items(state.days) { day -> WeekDayRow(day) }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun WeekDayRow(day: WeekDayUi) {
    val borderModifier = when {
        day.isToday      -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        day.isHighCal    -> Modifier.border(BorderStroke(3.dp, AmberAcc),
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp))
        day.isShoppingDay -> Modifier.border(BorderStroke(3.dp, GreenAcc),
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp))
        else             -> Modifier
    }

    val barColor = when {
        day.isHighCal    -> AmberAcc
        day.isShoppingDay -> GreenAcc
        else             -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(borderModifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top row: date + badges + kcal
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
            Text(dateLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                if (day.isHighCal) Badge("High cal", HCalBg, HCalTxt)
                if (day.isShoppingDay) Badge("Shopping", ShopBg, ShopTxt)
                Text("${day.kcal.toInt()} kcal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Meal name pills
        if (day.mealNames.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                day.mealNames.take(3).forEach { name ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 90.dp))
                    }
                }
            }
        }

        // Kcal progress bar
        val fraction = if (day.kcalTarget > 0) (day.kcal / day.kcalTarget).toFloat().coerceIn(0f, 1f) else 0f
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.secondaryContainer)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction).clip(RoundedCornerShape(2.dp)).background(barColor))
        }
    }
}

@Composable private fun Badge(text: String, bg: Color, textColor: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(bg).padding(horizontal = 7.dp, vertical = 2.dp)) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = textColor)
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