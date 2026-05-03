package com.adasa.foodplan.ui.mealplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ProteinColor = Color(0xFF534AB7)
private val FatColor     = Color(0xFFBA7517)
private val CarbsColor   = Color(0xFF1D9E75)
private val WarnBg       = Color(0xFFFFF0C2); private val WarnText = Color(0xFF7D4E00)
private val ComplianceBg = Color(0xFFEADDFF)

@Composable
fun StatsCard(
    view:          PlanView,
    expanded:      Boolean,
    onToggle:      () -> Unit,
    primaryLabel:  String,
    subtitleLabel: String,
    protein:       Double,
    fat:           Double,
    carbs:         Double,
    kcalActual:    Double,
    kcalTarget:    Int,
    proteinPowderDaysLeft: Double? = null,
    // Compliance — null means don't show (day view)
    fullDays:    Int? = null,
    halfDays:    Int? = null,
    avgKcalPct:  Int? = null,
    modifier:    Modifier = Modifier
) {
    // Goals (approximate — could be passed from settings in the future)
    val proteinTarget = kcalTarget * 0.30 / 4   // 30% of kcal as protein
    val fatTarget     = kcalTarget * 0.28 / 9   // 28% as fat
    val carbsTarget   = kcalTarget * 0.42 / 4   // 42% as carbs

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            MacroPieChart(protein, fat, carbs, trackColor)
            Column(modifier = Modifier.weight(1f)) {
                Text(primaryLabel, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(subtitleLabel, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    MacroPill("${protein.toInt()}g P", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    MacroPill("${fat.toInt()}g F",     MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    MacroPill("${carbs.toInt()}g C",   MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp).rotate(if (expanded) 180f else 0f))
        }

        // ── Expanded ──────────────────────────────────────────────────────────
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Goal bars — fill to % of goal, not % of total
                GoalBarRow("Kcal",    kcalActual,  kcalTarget.toDouble(),  MaterialTheme.colorScheme.primary)
                GoalBarRow("Protein", protein,     proteinTarget,           ProteinColor)
                GoalBarRow("Fat",     fat,         fatTarget,               FatColor)
                GoalBarRow("Carbs",   carbs,        carbsTarget,             CarbsColor)

                // Protein powder
                if (proteinPowderDaysLeft != null) {
                    StatBox(
                        value    = "~${proteinPowderDaysLeft.toInt()} days",
                        label    = "Protein powder left",
                        bg       = WarnBg,
                        textColor = WarnText,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Compliance — week/month only
                if (fullDays != null && halfDays != null && avgKcalPct != null) {
                    val period = if (view == PlanView.WEEK) "this week" else "this month"
                    Text(period, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatBox("🟢 $fullDays",  "Full days",     MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Modifier.weight(1f))
                        StatBox("🟡 $halfDays",  "50%+ days",     MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Modifier.weight(1f))
                        StatBox("$avgKcalPct%",  "Avg kcal goal", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalBarRow(label: String, actual: Double, target: Double, color: Color) {
    val pct    = if (target > 0) (actual / target).coerceIn(0.0, 1.5) else 0.0
    val pctInt = (pct * 100).toInt()
    val barColor = color   // always same colour, no red

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp))
        Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)) {
            Box(modifier = Modifier.fillMaxHeight()
                .fillMaxWidth(pct.toFloat().coerceIn(0f, 1f))
                .clip(RoundedCornerShape(3.dp)).background(barColor))
        }
        Text("$pctInt%", fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun MacroPieChart(protein: Double, fat: Double, carbs: Double, trackColor: Color) {
    val total = (protein + fat + carbs).coerceAtLeast(1.0)
    val pF = (protein / total).toFloat(); val fF = (fat / total).toFloat()
    Canvas(modifier = Modifier.size(48.dp)) {
        val stroke    = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Butt)
        val diameter  = size.minDimension - 8.dp.toPx()
        val topLeft   = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 4.dp.toPx())
        val arcSize   = androidx.compose.ui.geometry.Size(diameter, diameter)
        drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = stroke)
        drawArc(ProteinColor, -90f, pF * 360f, false, topLeft, arcSize, style = stroke)
        drawArc(FatColor, -90f + pF * 360f, fF * 360f, false, topLeft, arcSize, style = stroke)
        drawArc(CarbsColor, -90f + (pF + fF) * 360f, (1f - pF - fF) * 360f, false, topLeft, arcSize, style = stroke)
    }
}

@Composable
private fun MacroPill(label: String, bg: Color, text: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

@Composable
private fun StatBox(value: String, label: String, bg: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(horizontal = 10.dp, vertical = 8.dp)) {
        Column {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textColor)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp))
        }
    }
}