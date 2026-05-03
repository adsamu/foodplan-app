package com.adasa.foodplan.ui.mealplan

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onNavigateToRecipeDetail: (String) -> Unit = {},
    onNavigateToSettings:     () -> Unit       = {},
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val selectedView   by viewModel.selectedView.collectAsStateWithLifecycle()
    val statsExpanded  by viewModel.statsExpanded.collectAsStateWithLifecycle()
    val dayState       by viewModel.dayUiState.collectAsStateWithLifecycle()
    val weekState      by viewModel.weekUiState.collectAsStateWithLifecycle()
    val monthState     by viewModel.monthUiState.collectAsStateWithLifecycle()
    val checkedMeals   by viewModel.checkedMeals.collectAsStateWithLifecycle()
    val optimizerState by viewModel.optimizerState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    // Show snackbar when optimizer finishes and then reset state
    LaunchedEffect(optimizerState) {
        when (val s = optimizerState) {
            is OptimizerState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message  = "Plan generated: ${s.planName}",
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.onOptimizerMessageConsumed()
            }
            is OptimizerState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message  = s.message,
                        duration = SnackbarDuration.Long
                    )
                }
                viewModel.onOptimizerMessageConsumed()
            }
            else -> Unit
        }
    }

    val isRunning = optimizerState is OptimizerState.Running

    // Continuously rotating animation for the icon while optimizer runs
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 360f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "iconRotation"
    )

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
    val kcalActual = when (selectedView) {
        PlanView.DAY   -> dayState?.nutrition?.kcal ?: 0.0
        PlanView.WEEK  -> weekState?.avgKcal ?: 0.0
        PlanView.MONTH -> monthState?.avgKcal ?: 0.0
    }
    val kcalTarget = dayState?.kcalTarget ?: 1350

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopAppBar(
                title = { Text("Meal Plan", fontWeight = FontWeight.Medium) },
                actions = {
                    // Optimizer button — spins while running, disabled until idle
                    IconButton(
                        onClick  = { viewModel.generatePlan() },
                        enabled  = !isRunning
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Autorenew,
                            contentDescription = if (isRunning) "Generating plan…" else "Generate plan",
                            modifier           = Modifier.rotate(if (isRunning) rotation else 0f)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
            ViewToggle(
                selected = selectedView, onSelect = viewModel::onViewChange,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
            val views = PlanView.entries
            StatsCard(
                view          = selectedView,
                expanded      = statsExpanded,
                onToggle      = viewModel::onToggleStats,
                primaryLabel  = primaryLabel,
                subtitleLabel = subtitleLabel,
                protein       = protein, fat = fat, carbs = carbs,
                kcalActual    = kcalActual, kcalTarget = kcalTarget,
                proteinPowderDaysLeft = weekState?.proteinPowderDaysLeft ?: monthState?.proteinPowderDaysLeft,
                fullDays  = when (selectedView) { PlanView.DAY -> null; PlanView.WEEK -> weekState?.fullDays; PlanView.MONTH -> monthState?.fullDays },
                halfDays  = when (selectedView) { PlanView.DAY -> null; PlanView.WEEK -> weekState?.halfDays; PlanView.MONTH -> monthState?.halfDays },
                avgKcalPct = when (selectedView) { PlanView.DAY -> null; PlanView.WEEK -> weekState?.avgKcalPct; PlanView.MONTH -> monthState?.avgKcalPct },
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 6.dp)
                    .pointerInput(selectedView) {
                        var drag = 0f
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val idx = views.indexOf(selectedView)
                                if (drag < -60 && idx < views.lastIndex) viewModel.onViewChange(views[idx + 1])
                                else if (drag > 60 && idx > 0) viewModel.onViewChange(views[idx - 1])
                                drag = 0f
                            },
                            onHorizontalDrag = { _, amount -> drag += amount }
                        )
                    }
            )
            when (selectedView) {
                PlanView.DAY -> DayView(
                    state         = dayState,
                    onPrevious    = viewModel::onNavigatePrevious,
                    onNext        = viewModel::onNavigateNext,
                    onMealClick   = onNavigateToRecipeDetail,
                    checkedMeals  = checkedMeals[dayState?.date?.toString()] ?: emptySet(),
                    onMealChecked = { idx -> dayState?.date?.let { viewModel.onMealChecked(it, idx) } }
                )
                PlanView.WEEK  -> WeekView(
                    state          = weekState,
                    onPrevious     = viewModel::onNavigatePrevious,
                    onNext         = viewModel::onNavigateNext,
                    onRecipeClick  = onNavigateToRecipeDetail
                )
                PlanView.MONTH -> MonthView(monthState, viewModel::onNavigatePrevious, viewModel::onNavigateNext)
            }
        }
    }
}

@Composable
private fun ViewToggle(selected: PlanView, onSelect: (PlanView) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp)) {
        PlanView.entries.forEach { view ->
            val active = view == selected
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(view) }.padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    view.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun shortDate(date: kotlinx.datetime.LocalDate): String {
    val mon = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "${date.dayOfMonth} $mon"
}