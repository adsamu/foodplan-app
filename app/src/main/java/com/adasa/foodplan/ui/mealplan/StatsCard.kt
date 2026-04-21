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
import kotlin.math.PI

// Color tokens shared within this file
private val ProteinColor = Color(0xFF6750A4); private val ProteinBg = Color(0xFFEADDFF); private val ProteinText = Color(0xFF21005D)
private val FatColor     = Color(0xFFD4537E); private val FatBg     = Color(0xFFFFD8E4); private val FatText     = Color(0xFF31111D)
private val CarbsColor   = Color(0xFF1D9E75); private val CarbsBg   = Color(0xFFD8F5E4); private val CarbsText   = Color(0xFF0A3D22)
private val ShopBg       = Color(0xFFD8F5E4); private val ShopText   = Color(0xFF0A3D22)
private val WarnBg       = Color(0xFFFFF0C2); private val WarnText   = Color(0xFF7D4E00)
private val CardBg       = Color(0xFFF3EDF7); private val StatBoxBg  = Color(0xFFEADDFF)
private val MutedText    = Color(0xFF79747E)

@Composable
fun StatsCard(
    view: PlanView,
    expanded: Boolean,
    onToggle: () -> Unit,
    primaryLabel: String,
    subtitleLabel: String,
    protein: Double,
    fat: Double,
    carbs: Double,
    kcalTarget: Int = 0,
    // Expanded extras
    daysUntilShopping: Int? = null,
    proteinPowderDaysLeft: Double? = null,
    highCalDays: Int? = null,
    weekTotalKcal: Double? = null,
    shoppingDaysCount: Int? = null,
    highCalDaysCount: Int? = null,
    monthTotalKcal: Double? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        // ── Collapsed header ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MacroPieChart(protein = protein, fat = fat, carbs = carbs)
            Column(modifier = Modifier.weight(1f)) {
                Text(primaryLabel, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ProteinText)
                Text(subtitleLabel, fontSize = 11.sp, color = MutedText, modifier = Modifier.padding(top = 1.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    MacroPill("${protein.toInt()}g P", ProteinBg, ProteinText)
                    MacroPill("${fat.toInt()}g F", FatBg, FatText)
                    MacroPill("${carbs.toInt()}g C", CarbsBg, CarbsText)
                }
            }
            Icon(
                Icons.Default.KeyboardArrowDown, contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(20.dp).rotate(if (expanded) 180f else 0f)
            )
        }
        // ── Expanded content ─────────────────────────────────────────────────
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp)) {
                if (view == PlanView.DAY) {
                    // Macro progress bars
                    val total = (protein + fat + carbs).coerceAtLeast(1.0)
                    MacroBarRow("Protein", protein, total, ProteinColor)
                    MacroBarRow("Fat",     fat,     total, FatColor)
                    MacroBarRow("Carbs",   carbs,   total, CarbsColor)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox(daysUntilShopping?.let { "$it days" } ?: "—", "Until shopping", ShopBg, ShopText, Modifier.weight(1f))
                        StatBox(proteinPowderDaysLeft?.let { "~${it.toInt()} days" } ?: "—", "Protein powder left", WarnBg, WarnText, Modifier.weight(1f))
                    }
                } else if (view == PlanView.WEEK) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox(daysUntilShopping?.let { "$it days" } ?: "—", "Until shopping", ShopBg, ShopText, Modifier.weight(1f))
                        StatBox(proteinPowderDaysLeft?.let { "~${it.toInt()} days" } ?: "—", "Protein powder left", WarnBg, WarnText, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox(highCalDays?.let { "$it high-cal" } ?: "—", "Days this week", StatBoxBg, ProteinText, Modifier.weight(1f))
                        StatBox(weekTotalKcal?.let { "${it.toInt()} kcal" } ?: "—", "Week total", StatBoxBg, ProteinText, Modifier.weight(1f))
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox(shoppingDaysCount?.let { "$it shops" } ?: "—", "This month", ShopBg, ShopText, Modifier.weight(1f))
                        StatBox(highCalDaysCount?.let { "$it high-cal" } ?: "—", "Days this month", StatBoxBg, ProteinText, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox(monthTotalKcal?.let { "${it.toInt()}" } ?: "—", "Month kcal total", StatBoxBg, ProteinText, Modifier.weight(1f))
                        StatBox(proteinPowderDaysLeft?.let { "~${it.toInt()} days" } ?: "—", "Protein powder left", WarnBg, WarnText, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable private fun MacroPieChart(protein: Double, fat: Double, carbs: Double) {
    val total = (protein + fat + carbs).coerceAtLeast(1.0)
    val pF = (protein / total).toFloat(); val fF = (fat / total).toFloat(); val cF = (carbs / total).toFloat()
    Canvas(modifier = Modifier.size(48.dp)) {
        val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Butt)
        val diameter = size.minDimension - 8.dp.toPx()
        val topLeft = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 4.dp.toPx())
        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
        drawArc(Color(0xFFEADDFF), -90f, 360f, false, topLeft, arcSize, style = stroke)
        drawArc(ProteinColor, -90f, pF * 360f, false, topLeft, arcSize, style = stroke)
        drawArc(FatColor, -90f + pF * 360f, fF * 360f, false, topLeft, arcSize, style = stroke)
        drawArc(CarbsColor, -90f + (pF + fF) * 360f, cF * 360f, false, topLeft, arcSize, style = stroke)
    }
}

@Composable private fun MacroPill(label: String, bg: Color, text: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

@Composable private fun MacroBarRow(label: String, value: Double, total: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 3.dp)) {
        Text(label, fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.width(52.dp))
        Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE8DEF8))) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth((value / total).toFloat().coerceIn(0f, 1f))
                .clip(RoundedCornerShape(3.dp)).background(color))
        }
        Text("${value.toInt()}g", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.width(36.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable private fun StatBox(value: String, label: String, bg: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(horizontal = 10.dp, vertical = 8.dp)) {
        Column {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textColor)
            Text(label, fontSize = 10.sp, color = Color(0xFF49454F), modifier = Modifier.padding(top = 2.dp))
        }
    }
}
