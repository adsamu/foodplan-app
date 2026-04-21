package com.adasa.foodplan.ui.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onNavigateToRecipeDetail: (String) -> Unit = {},
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val selectedView  by viewModel.selectedView.collectAsStateWithLifecycle()
    val statsExpanded by viewModel.statsExpanded.collectAsStateWithLifecycle()
    val dayState      by viewModel.dayUiState.collectAsStateWithLifecycle()
    val weekState     by viewModel.weekUiState.collectAsStateWithLifecycle()
    val monthState    by viewModel.monthUiState.collectAsStateWithLifecycle()

    val primaryLabel = when (selectedView) {
        PlanView.DAY   -> dayState?.nutrition?.kcal?.toInt()?.let { "$it kcal" } ?: "— kcal"
        PlanView.WEEK  -> weekState?.avgKcal?.toInt()?.let { "~$it kcal/day" } ?: "—"
        PlanView.MONTH -> monthState?.avgKcal?.toInt()?.let { "~$it kcal/day" } ?: "—"
    }
    val subtitleLabel = when (selectedView) {
        PlanView.DAY   -> dayState?.let { d ->
            val mon = d.date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            "${d.date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}, ${d.date.dayOfMonth} $mon · ${d.dayType.name.lowercase().replaceFirstChar { it.uppercase() }}"
        } ?: ""
        PlanView.WEEK  -> weekState?.let { "Week ${it.weekNumber} · ${shortDate(it.startDate)}–${shortDate(it.endDate)}" } ?: ""
        PlanView.MONTH -> monthState?.let { "${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.year} · ${it.days.size} days" } ?: ""
    }
    val protein = when (selectedView) {
        PlanView.DAY   -> dayState?.nutrition?.protein ?: 0.0
        PlanView.WEEK  -> weekState?.avgProtein ?: 0.0
        PlanView.MONTH -> monthState?.avgProtein ?: 0.0
    }
    val fat = when (selectedView) {
        PlanView.DAY   -> dayState?.nutrition?.fat ?: 0.0
        PlanView.WEEK  -> weekState?.avgFat ?: 0.0
        PlanView.MONTH -> monthState?.avgFat ?: 0.0
    }
    val carbs = when (selectedView) {
        PlanView.DAY   -> dayState?.nutrition?.carbs ?: 0.0
        PlanView.WEEK  -> weekState?.avgCarbs ?: 0.0
        PlanView.MONTH -> monthState?.avgCarbs ?: 0.0
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Meal Plan", fontWeight = FontWeight.Medium) },
            actions = { IconButton(onClick = {}) { Icon(Icons.Default.Settings, contentDescription = "Settings") } }
        )
        ViewToggle(selected = selectedView, onSelect = viewModel::onViewChange,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
        StatsCard(
            view = selectedView, expanded = statsExpanded, onToggle = viewModel::onToggleStats,
            primaryLabel = primaryLabel, subtitleLabel = subtitleLabel,
            protein = protein, fat = fat, carbs = carbs,
            kcalTarget = dayState?.kcalTarget ?: 1350,
            daysUntilShopping = dayState?.daysUntilShopping ?: weekState?.daysUntilShopping,
            proteinPowderDaysLeft = null,
            highCalDays = weekState?.highCalDays, weekTotalKcal = weekState?.weekTotalKcal,
            shoppingDaysCount = monthState?.shoppingDaysCount, highCalDaysCount = monthState?.highCalDaysCount,
            monthTotalKcal = monthState?.monthTotalKcal,
            modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 6.dp)
        )
        when (selectedView) {
            PlanView.DAY   -> DayView(dayState, viewModel::onNavigatePrevious, viewModel::onNavigateNext, onNavigateToRecipeDetail)
            PlanView.WEEK  -> WeekView(weekState, viewModel::onNavigatePrevious, viewModel::onNavigateNext)
            PlanView.MONTH -> MonthView(monthState, viewModel::onNavigatePrevious, viewModel::onNavigateNext)
        }
    }
}

@Composable
private fun ViewToggle(selected: PlanView, onSelect: (PlanView) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFECE6F0)).padding(3.dp)) {
        PlanView.entries.forEach { view ->
            val active = view == selected
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                    .background(if (active) Color(0xFF6750A4) else Color.Transparent)
                    .clickable { onSelect(view) }.padding(vertical = 6.dp),
                contentAlignment = Alignment.Center) {
                Text(view.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = if (active) Color.White else Color(0xFF49454F))
            }
        }
    }
}

private fun shortDate(date: kotlinx.datetime.LocalDate): String {
    val mon = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "${date.dayOfMonth} $mon"
}
