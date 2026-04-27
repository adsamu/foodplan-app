package com.adasa.foodplan.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import com.adasa.foodplan.domain.model.*
import kotlinx.datetime.DayOfWeek

private val batchColors = listOf(
    Color(0xFF6750A4), // batch 1 — purple
    Color(0xFFE8A000), // batch 2 — amber
    Color(0xFF1D9E75), // batch 3 — green
    Color(0xFFD4537E)  // batch 4 — pink
)

@Composable
fun ScheduleTab(config: MealPlanConfig?, viewModel: SettingsViewModel) {
    val days = DayOfWeek.entries
    val scrollState = rememberScrollState()
    val mealSlots = config?.schedule?.mealSlots ?: emptyMap()
    val batchGroups = remember(config) {
        mutableStateListOf<BatchCookingGroup>().also {
            it.addAll(config?.schedule?.batchGroups ?: emptyList())
        }
    }
    val snackOptional = config?.schedule?.snackOptionalFill ?: true
    val shoppingDays = config?.shopping?.shoppingDays ?: emptySet()
    var shoppingInterval by remember(config) {
        mutableIntStateOf(config?.shopping?.intervalWeeks ?: 1)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Meal slots ────────────────────────────────────────────────────
        SettingsSection("Meals", "Uncheck a column to disable that day")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header row
                MealMatrixHeader(days)
                Spacer(Modifier.height(4.dp))
                // Binary meal rows
                listOf(
                    "Breakfast" to { c: DayMealConfig -> c.breakfast },
                    "Lunch" to { c: DayMealConfig -> c.lunch },
                    "Dinner" to { c: DayMealConfig -> c.dinner }
                ).forEach { (label, getter) ->
                    MealMatrixRow(
                        label = label,
                        days = days,
                        isActive = { day -> getter(mealSlots[day] ?: DayMealConfig()) },
                        onToggle = { day ->
                            val current = mealSlots[day] ?: DayMealConfig()
                            val updated = when (label) {
                                "Breakfast" -> current.copy(breakfast = !current.breakfast)
                                "Lunch" -> current.copy(lunch = !current.lunch)
                                else -> current.copy(dinner = !current.dinner)
                            }
                            viewModel.setMealSlot(day, updated)
                        }
                    )
                    Spacer(Modifier.height(3.dp))
                }

                // ── Snack section ─────────────────────────────────────────
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Snack", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Optional fill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Switch(
                            checked = snackOptional,
                            onCheckedChange = { viewModel.setSnackOptionalFill(it) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                Text(
                    "Tap to increment · Hold to reset",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Spacer(Modifier.width(68.dp))
                    days.forEach { day ->
                        val count = (mealSlots[day] ?: DayMealConfig()).snackCount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (count > 0) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .combinedClickable(
                                    onClick = {
                                        val current = mealSlots[day] ?: DayMealConfig()
                                        viewModel.setMealSlot(day, current.copy(snackCount = (count + 1) % 4))
                                    },
                                    onLongClick = {
                                        val current = mealSlots[day] ?: DayMealConfig()
                                        viewModel.setMealSlot(day, current.copy(snackCount = 0))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (count > 0) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ── Batch cooking ─────────────────────────────────────────────────
        SettingsSection("Batch Cooking", "Tap days to group into batches · Different colors = different batches")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    batchColors.forEachIndexed { i, color ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
                            Text("Batch ${i + 1}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                MealMatrixHeader(days)
                Spacer(Modifier.height(4.dp))
                listOf(MealCategory.LUNCH, MealCategory.DINNER, MealCategory.BREAKFAST).forEach { meal ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            meal.displayName,
                            modifier = Modifier.width(68.dp).align(Alignment.CenterVertically),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        days.forEach { day ->
                            val batchNum = batchGroups
                                .find { it.meal == meal && day in it.days }?.batchNumber ?: 0
                            BatchCell(
                                batchNum = batchNum,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val next = (batchNum + 1) % (batchColors.size + 1)
                                    // Remove day from any existing batch for this meal
                                    val updated = batchGroups.toMutableList()
                                    updated.removeAll { it.meal == meal && day in it.days }
                                    if (next > 0) {
                                        val existing = updated.find { it.meal == meal && it.batchNumber == next }
                                        if (existing != null) {
                                            updated[updated.indexOf(existing)] = existing.copy(days = existing.days + day)
                                        } else {
                                            updated.add(BatchCookingGroup(meal, setOf(day), next))
                                        }
                                    }
                                    batchGroups.clear()
                                    batchGroups.addAll(updated)
                                    viewModel.saveBatchGroups(updated)
                                }
                            )
                            Spacer(Modifier.width(3.dp))
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                }
            }
        }

        // ── Shopping ──────────────────────────────────────────────────────
        SettingsSection("Shopping")
        SettingsCard {
            Column {
                SettingsRow(icon = "🛒", title = "Shopping day")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    days.forEach { day ->
                        val isSelected = day in shoppingDays
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF1D9E75) else MaterialTheme.colorScheme.surfaceVariant)
                                .then(Modifier.wrapContentSize())
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.name.first().toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF1D9E75) else MaterialTheme.colorScheme.surfaceVariant)
                                    .wrapContentSize(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Interval", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("How often to shop", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StepperControl(
                        value = shoppingInterval,
                        label = "${shoppingInterval} wk",
                        onDecrement = { if (shoppingInterval > 1) { shoppingInterval--; viewModel.setShoppingInterval(shoppingInterval) } },
                        onIncrement = { if (shoppingInterval < 4) { shoppingInterval++; viewModel.setShoppingInterval(shoppingInterval) } }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MealMatrixHeader(days: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Spacer(Modifier.width(68.dp))
        days.forEach { day ->
            Text(
                text = day.name.first().toString(),
                modifier = Modifier.weight(1f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY)
                    Color(0xFFE8A000) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MealMatrixRow(
    label: String,
    days: List<DayOfWeek>,
    isActive: (DayOfWeek) -> Boolean,
    onToggle: (DayOfWeek) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label,
            modifier = Modifier.width(68.dp).align(Alignment.CenterVertically),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        days.forEach { day ->
            val active = isActive(day)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(onClick = { onToggle(day) }),
                contentAlignment = Alignment.Center
            ) {
                if (active) Text("✓", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BatchCell(batchNum: Int, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = if (batchNum > 0) batchColors[batchNum - 1] else Color(0xFFECE6F0)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .combinedClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (batchNum > 0) Text(batchNum.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}