package com.adasa.foodplan.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adasa.foodplan.domain.model.*
import kotlin.math.*

private val ProteinColor = Color(0xFF534AB7)
private val FatColor     = Color(0xFFBA7517)
private val CarbsColor   = Color(0xFF1D9E75)

@Composable
fun GoalsTab(config: MealPlanConfig?, viewModel: SettingsViewModel) {
    val scrollState = rememberScrollState()
    val goals  = config?.goals
    val powder = config?.proteinPowder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Targets ───────────────────────────────────────────────────────
        SettingsSection("Targets", "Weekly average — optimizer balances across days")
        SettingsCard {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                KcalInput(
                    value = goals?.kcalTarget ?: 1450.0,
                    onValueChange = { viewModel.setKcal(it) }
                )
                Spacer(Modifier.height(24.dp))
                MacroCircularSlider(
                    kcal   = goals?.kcalTarget ?: 1450.0,
                    goals  = goals,
                    onProteinChange = { viewModel.setProtein(it) },
                    onFatChange     = { viewModel.setFat(it) },
                    onCarbsChange   = { viewModel.setCarbs(it) }
                )
            }
        }

        // ── Daily limits ──────────────────────────────────────────────────
        SettingsSection("Daily limits", "Hard per-day constraints · Leave blank for none")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MinMaxCard(
                        label    = "kcal",
                        minValue = goals?.minKcalPerDay?.toInt()?.toString() ?: "",
                        maxValue = goals?.maxKcalPerDay?.toInt()?.toString() ?: "",
                        onMinChange = { viewModel.setMinKcal(it.toDoubleOrNull()) },
                        onMaxChange = { viewModel.setMaxKcal(it.toDoubleOrNull()) },
                        modifier = Modifier.weight(1f)
                    )
                    MinMaxCard(
                        label    = "Protein",
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
                        label    = "Fat",
                        minValue = goals?.minFatPerDay?.toInt()?.toString() ?: "",
                        maxValue = goals?.maxFatPerDay?.toInt()?.toString() ?: "",
                        onMinChange = { viewModel.setMinFat(it.toDoubleOrNull()) },
                        onMaxChange = { viewModel.setMaxFat(it.toDoubleOrNull()) },
                        modifier = Modifier.weight(1f)
                    )
                    MinMaxCard(
                        label    = "Carbs",
                        minValue = goals?.minCarbsPerDay?.toInt()?.toString() ?: "",
                        maxValue = goals?.maxCarbsPerDay?.toInt()?.toString() ?: "",
                        onMinChange = { viewModel.setMinCarbs(it.toDoubleOrNull()) },
                        onMaxChange = { viewModel.setMaxCarbs(it.toDoubleOrNull()) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Protein supplement ────────────────────────────────────────────
        SettingsSection("Protein Supplement")
        SettingsCard {
            Column {
                if (powder != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
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
                                progress = { (powder.gramsInStock / 600.0).coerceIn(0.0, 1.0).toFloat() },
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
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

// ── Kcal input ────────────────────────────────────────────────────────────

@Composable
private fun KcalInput(value: Double, onValueChange: (Double) -> Unit) {
    var text by remember(value.toInt()) { mutableStateOf(value.toInt().toString()) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            BasicTextField(
                value = text,
                onValueChange = { v ->
                    if (v.all { it.isDigit() } && v.length <= 5) {
                        text = v
                        v.toDoubleOrNull()?.let { onValueChange(it) }
                    }
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize   = 48.sp,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface,
                    textAlign  = TextAlign.End,
                    platformStyle  = PlatformTextStyle(includeFontPadding = false)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier  = Modifier.width(160.dp),
                singleLine = true
            )
            Text(
                " kcal",
                style    = MaterialTheme.typography.titleLarge,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        HorizontalDivider(
            modifier  = Modifier.width(200.dp),
            thickness = 2.dp,
            color     = MaterialTheme.colorScheme.primary
        )
        Text(
            "daily average target",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ── Circular macro slider ─────────────────────────────────────────────────

@Composable
private fun MacroCircularSlider(
    kcal: Double,
    goals: NutritionGoals?,
    onProteinChange: (Double?) -> Unit,
    onFatChange:     (Double?) -> Unit,
    onCarbsChange:   (Double?) -> Unit
) {
    // Three handles on the circle:
    //   handles[0] = protein / fat  boundary
    //   handles[1] = fat    / carbs boundary
    //   handles[2] = carbs  / protein boundary
    //
    // Segments (clockwise):
    //   Protein : handles[2] → handles[0]
    //   Fat     : handles[0] → handles[1]
    //   Carbs   : handles[1] → handles[2]

    fun arcFrac(from: Float, to: Float): Float {
        var d = to - from
        if (d < 0f) d += 1f
        return d
    }

    // Initialise from persisted goals — run once
    val init = remember(Unit) {
        val pF = goals?.let { (it.resolvedProtein * 4 / kcal).toFloat() } ?: 0.33f
        val fF = goals?.let { (it.resolvedFat * 9 / kcal).toFloat() }     ?: 0.27f
        floatArrayOf(
            pF.coerceIn(0.05f, 0.90f),
            (pF + fF).coerceIn(0.10f, 0.95f),
            0f
        )
    }
    var handles by remember { mutableStateOf(init.copyOf()) }

    // Derived values
    val pFrac = arcFrac(handles[2], handles[0])
    val fFrac = arcFrac(handles[0], handles[1])
    val cFrac = arcFrac(handles[1], handles[2])
    val pG = (kcal * pFrac / 4).roundToInt()
    val fG = (kcal * fFrac / 9).roundToInt()
    val cG = (kcal * cFrac / 4).roundToInt()
    val pP = (pFrac * 100).roundToInt()
    val fP = (fFrac * 100).roundToInt()
    val cP = 100 - pP - fP

    // Snap a fraction to the nearest whole gram
    fun snapFrac(frac: Float, kcalPerGram: Float): Float {
        val grams = (kcal * frac / kcalPerGram).roundToInt().coerceAtLeast(1)
        return (grams * kcalPerGram / kcal).toFloat()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val canvasSize   = 168.dp
        val strokeWidth  = 24.dp
        val handleRadius = 12.dp

        Canvas(
            modifier = Modifier
                .size(canvasSize)
                .pointerInput(Unit) {
                    val cx = size.width  / 2f
                    val cy = size.height / 2f
                    val r  = cx - strokeWidth.toPx() / 2f
                    val hitR = handleRadius.toPx() + 14f
                    val MIN_ARC = 0.04f

                    fun touchFrac(pos: Offset): Float {
                        val dx = pos.x - cx
                        val dy = pos.y - cy
                        var a = atan2(dy, dx) + PI.toFloat() / 2f
                        while (a < 0f)            a += 2f * PI.toFloat()
                        while (a >= 2f * PI.toFloat()) a -= 2f * PI.toFloat()
                        return a / (2f * PI.toFloat())
                    }

                    fun handlePos(h: Float): Offset {
                        val a = h * 2f * PI.toFloat() - PI.toFloat() / 2f
                        return Offset(cx + cos(a) * r, cy + sin(a) * r)
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val pos  = down.position

                        // Pick the closest handle within the hit radius
                        var activeHandle = -1
                        var bestDist = Float.MAX_VALUE
                        handles.forEachIndexed { i, h ->
                            val d = (pos - handlePos(h)).getDistance()
                            if (d < hitR && d < bestDist) { bestDist = d; activeHandle = i }
                        }
                        if (activeHandle < 0) return@awaitEachGesture
                        down.consume()

                        // Drag loop
                        while (true) {
                            val event  = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break

                            val raw = touchFrac(change.position)
                            val h   = handles.copyOf()

                            when (activeHandle) {
                                0 -> { // protein / fat boundary
                                    val maxP = (arcFrac(h[2], h[1]) - MIN_ARC).coerceAtLeast(MIN_ARC)
                                    val pFrac = snapFrac(arcFrac(h[2], raw), 4f).coerceIn(MIN_ARC, maxP)
                                    h[0] = (h[2] + pFrac) % 1f
                                    onProteinChange((kcal * pFrac / 4).toDouble())
                                    onFatChange((kcal * arcFrac(h[0], h[1]) / 9).toDouble())
                                }
                                1 -> { // fat / carbs boundary
                                    val maxF = (arcFrac(h[0], h[2]) - MIN_ARC).coerceAtLeast(MIN_ARC)
                                    val fFrac = snapFrac(arcFrac(h[0], raw), 9f).coerceIn(MIN_ARC, maxF)
                                    h[1] = (h[0] + fFrac) % 1f
                                    onFatChange((kcal * fFrac / 9).toDouble())
                                    onCarbsChange((kcal * arcFrac(h[1], h[2]) / 4).toDouble())
                                }
                                2 -> { // carbs / protein boundary
                                    val maxC = (arcFrac(h[1], h[0]) - MIN_ARC).coerceAtLeast(MIN_ARC)
                                    val cFrac = snapFrac(arcFrac(h[1], raw), 4f).coerceIn(MIN_ARC, maxC)
                                    h[2] = (h[1] + cFrac) % 1f
                                    onCarbsChange((kcal * cFrac / 4).toDouble())
                                    onProteinChange((kcal * arcFrac(h[2], h[0]) / 4).toDouble())
                                }
                            }
                            handles = h
                            change.consume()
                        }
                    }
                }
        ) {
            val cx   = size.width  / 2f
            val cy   = size.height / 2f
            val sw   = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val inset = strokeWidth.toPx() / 2f
            val arcSz = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx())
            val arcTl = Offset(inset, inset)
            val r     = cx - strokeWidth.toPx() / 2f

            fun startDeg(f: Float) = f * 360f - 90f
            fun sweepDeg(from: Float, to: Float) = arcFrac(from, to) * 360f

            // Arcs
            drawArc(ProteinColor, startDeg(handles[2]), sweepDeg(handles[2], handles[0]), false, arcTl, arcSz, style = sw)
            drawArc(FatColor,     startDeg(handles[0]), sweepDeg(handles[0], handles[1]), false, arcTl, arcSz, style = sw)
            drawArc(CarbsColor,   startDeg(handles[1]), sweepDeg(handles[1], handles[2]), false, arcTl, arcSz, style = sw)

            // Handles — white ring + colored center dot
            // Each handle sits between two segments, outer ring = left segment, dot = right segment
            val handleCols = listOf(
                Pair(ProteinColor, FatColor),
                Pair(FatColor,     CarbsColor),
                Pair(CarbsColor,   ProteinColor)
            )
            handles.forEachIndexed { i, h ->
                val angle = h * 2f * PI.toFloat() - PI.toFloat() / 2f
                val hx = cx + cos(angle).toFloat() * r
                val hy = cy + sin(angle).toFloat() * r
                drawCircle(Color.White,                handleRadius.toPx(),      Offset(hx, hy))
                drawCircle(handleCols[i].first,        handleRadius.toPx(),      Offset(hx, hy), style = Stroke(width = 2.5.dp.toPx()))
                drawCircle(handleCols[i].second,       4.5.dp.toPx(),            Offset(hx, hy))
            }
        }

        // Legend
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                Triple(ProteinColor, "Protein", "$pG g  ($pP%)"),
                Triple(FatColor,     "Fat",     "$fG g  ($fP%)"),
                Triple(CarbsColor,   "Carbs",   "$cG g  ($cP%)")
            ).forEach { (color, label, info) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape    = RoundedCornerShape(50),
                        color    = color
                    ) {}
                    Column {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            info,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}