package com.adasa.foodplan.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.BorderStroke

private val batchColors = listOf(
    Color(0xFF6750A4), // batch 1 — purple
    Color(0xFFE8A000)  // batch 2 — amber
)

// ── Batch logic helpers ────────────────────────────────────────────────────

private fun getBatchNum(
    meal: MealCategory,
    day: DayOfWeek,
    groups: List<BatchCookingGroup>
): Int = groups.find { it.meal == meal && day in it.days }?.batchNumber ?: 0

/**
 * A day is clickable if it is unset, OR if it is at the left or right end of its batch.
 * Interior days (both neighbors are in the same batch) cannot be modified.
 */
private fun isClickable(
    meal: MealCategory,
    day: DayOfWeek,
    days: List<DayOfWeek>,
    groups: List<BatchCookingGroup>
): Boolean {
    val batchNum = getBatchNum(meal, day, groups)
    if (batchNum == 0) return true
    val idx = days.indexOf(day)
    val prevBatch = days.getOrNull(idx - 1)?.let { getBatchNum(meal, it, groups) } ?: 0
    val nextBatch = days.getOrNull(idx + 1)?.let { getBatchNum(meal, it, groups) } ?: 0
    // Interior = both neighbors are the same batch → not clickable
    return !(prevBatch == batchNum && nextBatch == batchNum)
}

/**
 * Determines the next batch state for a click:
 * - Unset + adjacent batch exists → join that adjacent batch
 * - Unset + no adjacent → start batch 1
 * - In adjacent batch → switch to the OTHER batch number
 * - In non-adjacent batch → unset
 */
private fun nextBatchNum(
    meal: MealCategory,
    day: DayOfWeek,
    days: List<DayOfWeek>,
    groups: List<BatchCookingGroup>
): Int {
    val idx = days.indexOf(day)
    val current = getBatchNum(meal, day, groups)
    val prevBatch = days.getOrNull(idx - 1)?.let { getBatchNum(meal, it, groups) } ?: 0
    val nextBatch = days.getOrNull(idx + 1)?.let { getBatchNum(meal, it, groups) } ?: 0
    // Prefer left neighbor's batch, fall back to right
    val adjacentBatch = prevBatch.takeIf { it > 0 } ?: nextBatch.takeIf { it > 0 } ?: 0

    return when {
        current == 0 && adjacentBatch > 0 -> adjacentBatch          // join adjacent
        current == 0 -> 1                                            // start new batch 1
        current == adjacentBatch -> if (adjacentBatch == 1) 2 else 1 // switch to other batch
        else -> 0                                                    // unset
    }
}

/**
 * Applies a click to the batch groups list and returns the updated list.
 */
private fun applyBatchClick(
    meal: MealCategory,
    day: DayOfWeek,
    days: List<DayOfWeek>,
    groups: List<BatchCookingGroup>
): List<BatchCookingGroup> {
    if (!isClickable(meal, day, days, groups)) return groups

    val next = nextBatchNum(meal, day, days, groups)
    val updated = groups.toMutableList()

    // Remove day from its current group
    val currentGroup = updated.find { it.meal == meal && day in it.days }
    if (currentGroup != null) {
        val remaining = currentGroup.days - day
        updated.remove(currentGroup)
        if (remaining.isNotEmpty()) {
            updated.add(currentGroup.copy(days = remaining))
        }
    }

    // Add day to new batch if not unset
    if (next > 0) {
        val existing = updated.find { it.meal == meal && it.batchNumber == next }
        if (existing != null) {
            updated[updated.indexOf(existing)] = existing.copy(days = existing.days + day)
        } else {
            updated.add(BatchCookingGroup(meal, setOf(day), next))
        }
    }

    return updated
}

// ── Composable ─────────────────────────────────────────────────────────────

@Composable
fun ScheduleTab(config: MealPlanConfig?, viewModel: SettingsViewModel) {
    val days = DayOfWeek.entries
    val scrollState = rememberScrollState()
    val mealSlots = config?.schedule?.mealSlots ?: emptyMap()

    // Local state synced from config — updates immediately on tap for snappy UX
    var batchGroups by remember(config?.schedule?.batchGroups) {
        mutableStateOf(config?.schedule?.batchGroups ?: emptyList())
    }

    val snackOptional = config?.schedule?.snackOptionalFill ?: true
    val shoppingDays = config?.shopping?.shoppingDays ?: emptySet()
    var shoppingInterval by remember(config) {
        mutableIntStateOf(config?.shopping?.intervalWeeks ?: 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ── Meal slots ────────────────────────────────────────────────────
        SettingsSection("Meals", "Uncheck a column to disable that day")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                MealMatrixHeader(days)
                Spacer(Modifier.height(4.dp))
                listOf(
                    "Breakfast" to { c: DayMealConfig -> c.breakfast },
                    "Lunch"     to { c: DayMealConfig -> c.lunch },
                    "Dinner"    to { c: DayMealConfig -> c.dinner }
                ).forEach { (label, getter) ->
                    MealMatrixRow(
                        label = label,
                        days = days,
                        isActive = { day -> getter(mealSlots[day] ?: DayMealConfig()) },
                        onToggle = { day ->
                            val current = mealSlots[day] ?: DayMealConfig()
                            val updated = when (label) {
                                "Breakfast" -> current.copy(breakfast = !current.breakfast)
                                "Lunch"     -> current.copy(lunch = !current.lunch)
                                else        -> current.copy(dinner = !current.dinner)
                            }
                            viewModel.setMealSlot(day, updated)
                        }
                    )
                    Spacer(Modifier.height(3.dp))
                }

                // ── Snack ─────────────────────────────────────────────────
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Counter row first
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        "Snack",
                        modifier = Modifier
                            .width(68.dp)
                            .align(Alignment.CenterVertically),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    days.forEach { day ->
                        // -1 = unset (optimizer decides), 0 = no snack, 1-3 = explicit count
                        val count = (mealSlots[day] ?: DayMealConfig()).snackCount
                        val isInfinity = count == -1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isInfinity) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.secondaryContainer
                                )
                                .combinedClickable(
                                    onClick = {
                                        val current = mealSlots[day] ?: DayMealConfig()
                                        // cycle: ∞(-1) → 1 → 2 → 3 → ∞(-1)
                                        val next = when (count) {
                                            -1   -> 1
                                            3    -> -1
                                            else -> count + 1
                                        }
                                        viewModel.setMealSlot(day, current.copy(snackCount = next))
                                    },
                                    onLongClick = {
                                        val current = mealSlots[day] ?: DayMealConfig()
                                        viewModel.setMealSlot(day, current.copy(snackCount = -1))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isInfinity) "∞" else count.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInfinity) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }


            }
        }

        // ── Batch cooking ─────────────────────────────────────────────────
        SettingsSection("Batch Cooking")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                MealMatrixHeader(days)
                Spacer(Modifier.height(4.dp))
                listOf(MealCategory.LUNCH, MealCategory.DINNER, MealCategory.BREAKFAST)
                    .forEach { meal ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                meal.displayName,
                                modifier = Modifier
                                    .width(68.dp)
                                    .align(Alignment.CenterVertically),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            days.forEach { day ->
                                val batchNum = getBatchNum(meal, day, batchGroups)
                                val clickable = isClickable(meal, day, days, batchGroups)

                                val bgColor = when {
                                    batchNum > 0 -> batchColors[batchNum - 1]
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (clickable) bgColor
                                            else bgColor.copy(alpha = 0.5f)
                                        )
                                        .then(
                                            if (clickable) Modifier.clickable {
                                                val updated = applyBatchClick(
                                                    meal, day, days, batchGroups
                                                )
                                                batchGroups = updated
                                                viewModel.saveBatchGroups(updated)
                                            } else Modifier
                                        )
                                )
                                Spacer(Modifier.width(3.dp))
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                    }
            }
        }

        // ── Variety ───────────────────────────────────────────────────────
        val variety = config?.variety ?: VarietyConfig()
        VarietyCard(variety = variety, viewModel = viewModel)

        // ── Shopping ──────────────────────────────────────────────────────
        SettingsSection("Shopping")
        SettingsCard {
            Column {
                SettingsRow(icon = "🛒", title = "Shopping day")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    days.forEach { day ->
                        val isSelected = day in shoppingDays
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF1D9E75)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.setShoppingDay(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.name.first().toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Interval",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "How often to shop",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StepperControl(
                        value = shoppingInterval,
                        label = "$shoppingInterval wk",
                        onDecrement = {
                            if (shoppingInterval > 1) {
                                shoppingInterval--
                                viewModel.setShoppingInterval(shoppingInterval)
                            }
                        },
                        onIncrement = {
                            if (shoppingInterval < 4) {
                                shoppingInterval++
                                viewModel.setShoppingInterval(shoppingInterval)
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Shared matrix components ───────────────────────────────────────────────

@Composable
private fun MealMatrixHeader(days: List<DayOfWeek>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Spacer(Modifier.width(68.dp))
        days.forEach { day ->
            Text(
                text = day.name.first().toString(),
                modifier = Modifier.weight(1f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            label,
            modifier = Modifier
                .width(68.dp)
                .align(Alignment.CenterVertically),
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
                    .background(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onToggle(day) },
                contentAlignment = Alignment.Center
            ) {
                if (active) {
                    Text("✓", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
// ── Variety card ───────────────────────────────────────────────────────────

@Composable
private fun VarietyCard(
    variety: VarietyConfig,
    viewModel: SettingsViewModel
) {
    SettingsSection("Variety", "Controls how the optimizer avoids repeating recipes")
    SettingsCard {
        Column {

            // ── Level picker ──────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    "Recency strictness",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    variety.level.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    VarietyLevel.entries.forEach { level ->
                        val selected = level == variety.level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { viewModel.setVariety(variety.copy(level = level)) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                level.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // ── Shared recency toggles ────────────────────────────────────
            SettingsSwitchRow(
                title   = "Lunch and dinner share history",
                subtitle = "A recipe used for either won't repeat in the other",
                checked = variety.lunchDinnerSharedRecency
            ) { viewModel.setVariety(variety.copy(lunchDinnerSharedRecency = it)) }

            HorizontalDivider()

            SettingsSwitchRow(
                title   = "Breakfast and snacks share history",
                subtitle = "Useful if you eat the same breakfast and snack foods",
                checked = variety.breakfastSnackSharedRecency
            ) { viewModel.setVariety(variety.copy(breakfastSnackSharedRecency = it)) }

            HorizontalDivider()

            // ── Cross-meal rules ──────────────────────────────────────────
            SettingsSwitchRow(
                title   = "Lunch and dinner must differ",
                subtitle = "No identical recipe in both slots on the same day",
                checked = variety.lunchDinnerMustDiffer
            ) { viewModel.setVariety(variety.copy(lunchDinnerMustDiffer = it)) }

            HorizontalDivider()

            SettingsSwitchRow(
                title   = "Vary protein sources",
                subtitle = "Avoid same meat / fish / dairy for lunch and dinner",
                checked = variety.proteinSourceVariety
            ) { viewModel.setVariety(variety.copy(proteinSourceVariety = it)) }

        }
    }
}