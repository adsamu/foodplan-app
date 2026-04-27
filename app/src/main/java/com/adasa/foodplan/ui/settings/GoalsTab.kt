package com.adasa.foodplan.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adasa.foodplan.domain.model.*

@Composable
fun GoalsTab(config: MealPlanConfig?, viewModel: SettingsViewModel) {
    val scrollState = rememberScrollState()
    val goals = config?.goals
    val powder = config?.proteinPowder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ── Macro targets ─────────────────────────────────────────────────
        SettingsSection("Targets", "Weekly averages — one macro is auto-calculated from the others")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                val autoField = goals?.autoField ?: MacroField.PROTEIN

                MacroTargetRow(
                    label = "Calories",
                    value = goals?.kcalTarget?.toInt()?.toString() ?: "1450",
                    unit = "kcal",
                    isAuto = false,
                    onAutoClick = null,
                    onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setKcal(v) } }
                )

                listOf(
                    Triple(MacroField.PROTEIN, "Protein", goals?.proteinTarget),
                    Triple(MacroField.FAT,     "Fat",     goals?.fatTarget),
                    Triple(MacroField.CARBS,   "Carbs",   goals?.carbsTarget)
                ).forEach { (field, label, target) ->
                    val isAuto = autoField == field
                    val displayValue = if (isAuto) {
                        when (field) {
                            MacroField.PROTEIN -> goals?.resolvedProtein?.toInt()?.toString() ?: "—"
                            MacroField.FAT     -> goals?.resolvedFat?.toInt()?.toString() ?: "—"
                            MacroField.CARBS   -> goals?.resolvedCarbs?.toInt()?.toString() ?: "—"
                        }
                    } else {
                        target?.toInt()?.toString() ?: ""
                    }

                    MacroTargetRow(
                        label = label,
                        value = displayValue,
                        unit = "g",
                        isAuto = isAuto,
                        onAutoClick = { viewModel.setAutoField(field) },
                        onValueChange = if (!isAuto) { v ->
                            val d = v.toDoubleOrNull()
                            when (field) {
                                MacroField.PROTEIN -> viewModel.setProtein(d)
                                MacroField.FAT     -> viewModel.setFat(d)
                                MacroField.CARBS   -> viewModel.setCarbs(d)
                            }
                        } else null
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                MacroPieChart(goals = goals)
            }
        }

        // ── Daily limits ──────────────────────────────────────────────────
        SettingsSection("Daily limits", "Hard per-day constraints · Leave blank for none")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MinMaxCard(
                        label = "kcal",
                        minValue = goals?.minKcalPerDay?.toInt()?.toString() ?: "",
                        maxValue = goals?.maxKcalPerDay?.toInt()?.toString() ?: "",
                        onMinChange = { viewModel.setMinKcal(it.toDoubleOrNull()) },
                        onMaxChange = { viewModel.setMaxKcal(it.toDoubleOrNull()) },
                        modifier = Modifier.weight(1f)
                    )
                    MinMaxCard(
                        label = "Protein",
                        minValue = goals?.minProteinPerDay?.toInt()?.toString() ?: "",
                        maxValue = goals?.maxProteinPerDay?.toInt()?.toString() ?: "",
                        onMinChange = { viewModel.setMinProtein(it.toDoubleOrNull()) },
                        onMaxChange = { viewModel.setMaxProtein(it.toDoubleOrNull()) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MinMaxCard(
                        label = "Fat",
                        minValue = goals?.minFatPerDay?.toInt()?.toString() ?: "",
                        maxValue = goals?.maxFatPerDay?.toInt()?.toString() ?: "",
                        onMinChange = { viewModel.setMinFat(it.toDoubleOrNull()) },
                        onMaxChange = { viewModel.setMaxFat(it.toDoubleOrNull()) },
                        modifier = Modifier.weight(1f)
                    )
                    MinMaxCard(
                        label = "Carbs",
                        minValue = goals?.minCarbsPerDay?.toInt()?.toString() ?: "",
                        maxValue = goals?.maxCarbsPerDay?.toInt()?.toString() ?: "",
                        onMinChange = { viewModel.setMinCarbs(it.toDoubleOrNull()) },
                        onMaxChange = { viewModel.setMaxCarbs(it.toDoubleOrNull()) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Protein powder ─────────────────────────────────────────────────
        SettingsSection("Protein Supplement")
        SettingsCard {
            Column {
                if (powder != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💪", style = MaterialTheme.typography.headlineSmall)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                powder.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${powder.proteinPer100g.toInt()}g P · ${powder.kcalPer100g.toInt()} kcal / 100g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                progress = {
                                    (powder.gramsInStock / 600.0)
                                        .coerceIn(0.0, 1.0)
                                        .toFloat()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                            )
                            Text(
                                "${powder.gramsInStock.toInt()}g remaining · ~${powder.daysRemaining.toInt()} days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = "Auto-fill protein gap",
                        checked = powder.autoFillGap,
                        onCheckedChange = { viewModel.setPowderAutoFill(it) }
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = "Low stock warning",
                        checked = powder.lowStockWarning,
                        onCheckedChange = { viewModel.setPowderLowStockWarning(it) }
                    )
                    HorizontalDivider()
                    SettingsRow(icon = "🔄", title = "Change or restock powder")
                } else {
                    SettingsRow(icon = "💪", title = "Add protein powder")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MacroTargetRow(
    label: String,
    value: String,
    unit: String,
    isAuto: Boolean,
    onAutoClick: (() -> Unit)?,
    onValueChange: ((String) -> Unit)?
) {
    var text by remember(value) { mutableStateOf(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)

        if (onAutoClick != null) {
            SuggestionChip(
                onClick = onAutoClick,
                label = { Text("AUTO", style = MaterialTheme.typography.labelSmall) },
                colors = if (isAuto) SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) else SuggestionChipDefaults.suggestionChipColors(),
                modifier = Modifier.height(28.dp)
            )
        }

        OutlinedTextField(
            value = if (isAuto) value else text,
            onValueChange = { v: String ->
                text = v
                onValueChange?.invoke(v)
            },
            enabled = !isAuto,
            modifier = Modifier.width(80.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Text(
            unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
private fun MacroPieChart(goals: NutritionGoals?) {
    if (goals == null) return

    val p = goals.resolvedProtein * 4
    val f = goals.resolvedFat * 9
    val c = goals.resolvedCarbs * 4
    val total = p + f + c
    if (total <= 0) return

    val pPct = (p / total * 100).toInt()
    val fPct = (f / total * 100).toInt()
    val cPct = (c / total * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val strokeWidth = 16.dp.toPx()
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val arcOffset = Offset(inset, inset)

            val pSweep = (p / total * 360).toFloat()
            val fSweep = (f / total * 360).toFloat()
            val cSweep = 360f - pSweep - fSweep

            drawArc(Color(0xFF6750A4), -90f, pSweep, false,
                topLeft = arcOffset, size = arcSize, style = stroke)
            drawArc(Color(0xFFD4537E), -90f + pSweep, fSweep, false,
                topLeft = arcOffset, size = arcSize, style = stroke)
            drawArc(Color(0xFF1D9E75), -90f + pSweep + fSweep, cSweep, false,
                topLeft = arcOffset, size = arcSize, style = stroke)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(
                Triple(Color(0xFF6750A4), "Protein", "$pPct%"),
                Triple(Color(0xFFD4537E), "Fat",     "$fPct%"),
                Triple(Color(0xFF1D9E75), "Carbs",   "$cPct%")
            ).forEach { (color, label, pct) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = RoundedCornerShape(50),
                        color = color
                    ) {}
                    Text("$label — $pct", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "≈ ${total.toInt()} kcal from macros",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}