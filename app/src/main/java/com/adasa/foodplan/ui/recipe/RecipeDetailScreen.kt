package com.adasa.foodplan.ui.recipe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.RecipeNutrition
import com.adasa.foodplan.domain.model.RecipeType

// Macro palette — same as GoalsTab for visual consistency
private val ProteinColor = Color(0xFF534AB7)
private val FatColor     = Color(0xFFBA7517)
private val CarbsColor   = Color(0xFF1D9E75)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    onIngredientClick: (String) -> Unit = {},
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.loadRecipe(recipeId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete recipe?") },
            text  = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecipe()
                    showDeleteDialog = false
                    onBackClick()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is RecipeDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RecipeDetailUiState.NotFound -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("Recipe not found")
                }
            }
            is RecipeDetailUiState.Success -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // ── Hero header ───────────────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text       = state.recipe.name,
                                    style      = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (state.recipe.type == RecipeType.COMPONENT) {
                                        DetailBadge(
                                            label      = state.recipe.componentCategory?.displayName ?: "Component",
                                            background = MaterialTheme.colorScheme.tertiaryContainer,
                                            textColor  = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    state.recipe.mealCategories.forEach { cat ->
                                        DetailBadge(
                                            label      = cat.displayName,
                                            background = MaterialTheme.colorScheme.secondaryContainer,
                                            textColor  = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                                MacroSummary(nutrition = state.nutrition)
                            }
                        }
                    }

                    // ── Ingredients ───────────────────────────────────────────
                    if (state.recipe.ingredients.isNotEmpty()) {
                        item {
                            DetailSectionLabel(
                                text     = "Ingredients",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                shape    = RoundedCornerShape(14.dp),
                                colors   = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column {
                                    state.recipe.ingredients.forEachIndexed { index, ingredient ->
                                        val id   = ingredient.ingredientId ?: ingredient.subRecipeId ?: ""
                                        val name = state.ingredientNames[id]
                                            ?: if (ingredient.subRecipeId != null) "Recipe" else "Ingredient"
                                        val isComponent = ingredient.subRecipeId != null
                                        val amount = ingredient.grams?.let { "${it.toInt()} g" }
                                            ?: ingredient.portions?.let { "$it serv." } ?: ""
                                        IngredientDetailRow(
                                            name        = name,
                                            amount      = amount,
                                            isComponent = isComponent,
                                            onClick     = if (ingredient.ingredientId != null)
                                            {{ onIngredientClick(ingredient.ingredientId) }}
                                            else null
                                        )
                                        if (index < state.recipe.ingredients.lastIndex) {
                                            HorizontalDivider(
                                                color     = MaterialTheme.colorScheme.outlineVariant,
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Instructions ──────────────────────────────────────────
                    if (state.recipe.steps.isNotEmpty()) {
                        item {
                            DetailSectionLabel(
                                text     = "Instructions",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            InstructionsCard(steps = state.recipe.steps)
                        }
                    }
                }
            }
        }
    }
}

// ── Macro summary ─────────────────────────────────────────────────────────────

@Composable
private fun MacroSummary(nutrition: RecipeNutrition) {
    val proteinKcal = nutrition.protein * 4
    val fatKcal     = nutrition.fat     * 9
    val carbsKcal   = nutrition.carbs   * 4
    val totalMacro  = proteinKcal + fatKcal + carbsKcal

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (totalMacro > 0) {
            MacroDonut(
                proteinFrac = (proteinKcal / totalMacro).toFloat(),
                fatFrac     = (fatKcal     / totalMacro).toFloat(),
                carbsFrac   = (carbsKcal   / totalMacro).toFloat(),
                size        = 88.dp,
                strokeWidth = 14.dp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text       = "${nutrition.kcal.toInt()} kcal",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(2.dp))
            MacroLegendRow(ProteinColor, "Protein", "${nutrition.protein.toInt()}g")
            MacroLegendRow(FatColor,     "Fat",     "${nutrition.fat.toInt()}g")
            MacroLegendRow(CarbsColor,   "Carbs",   "${nutrition.carbs.toInt()}g")
        }
    }
}

@Composable
private fun MacroDonut(
    proteinFrac: Float,
    fatFrac:     Float,
    carbsFrac:   Float,
    size:        Dp,
    strokeWidth: Dp,
) {
    val trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)

    Canvas(modifier = Modifier.size(size)) {
        val sw    = strokeWidth.toPx()
        val inset = sw / 2f
        val arcSz = Size(this.size.width - sw, this.size.height - sw)
        val tl    = Offset(inset, inset)
        val style = Stroke(width = sw, cap = StrokeCap.Butt)

        drawArc(trackColor, 0f, 360f, false, tl, arcSz, style = style)

        val pSweep = proteinFrac * 360f
        val fSweep = fatFrac     * 360f
        val cSweep = carbsFrac   * 360f

        drawArc(ProteinColor, -90f,                   pSweep, false, tl, arcSz, style = style)
        drawArc(FatColor,     -90f + pSweep,           fSweep, false, tl, arcSz, style = style)
        drawArc(CarbsColor,   -90f + pSweep + fSweep,  cSweep, false, tl, arcSz, style = style)
    }
}

@Composable
private fun MacroLegendRow(color: Color, label: String, value: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            value,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// ── Supporting composables ────────────────────────────────────────────────────

@Composable
private fun DetailBadge(label: String, background: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(
            text     = label,
            color    = textColor,
            style    = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun DetailSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text          = text.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.SemiBold,
        color         = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier      = modifier
    )
}

@Composable
private fun IngredientDetailRow(name: String, amount: String, isComponent: Boolean, onClick: (() -> Unit)? = null) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = if (isComponent) Icons.Default.Blender else Icons.Default.Restaurant,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier           = Modifier.size(16.dp)
            )
        }
        Text(text = name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(text = amount, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InstructionsCard(steps: List<String>) {
    // Parse serialised steps — timers are "TIMER|label|seconds"
    data class ParsedStep(val isTimer: Boolean, val text: String, val label: String, val totalSeconds: Int)
    val parsed = remember(steps) {
        steps.map { s ->
            if (s.startsWith("TIMER|")) {
                val parts = s.removePrefix("TIMER|").split("|")
                ParsedStep(true, "", parts.getOrElse(0) { "" }, parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0)
            } else {
                ParsedStep(false, s, "", 0)
            }
        }
    }

    val checkedSteps   = remember(steps.size) { mutableStateListOf(*Array(steps.size) { false }) }
    val timerRemaining = remember(steps) { mutableStateListOf(*Array(steps.size) { parsed[it].totalSeconds }) }
    val timerRunning   = remember(steps.size) { mutableStateListOf(*Array(steps.size) { false }) }

    parsed.forEachIndexed { index, p ->
        if (p.isTimer) {
            LaunchedEffect(timerRunning[index]) {
                if (timerRunning[index]) {
                    while (timerRemaining[index] > 0) {
                        kotlinx.coroutines.delay(1000L)
                        timerRemaining[index]--
                    }
                    timerRunning[index] = false
                }
            }
        }
    }

    var textStepCounter = 0

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            parsed.forEachIndexed { index, p ->
                if (p.isTimer) {
                    val remaining  = timerRemaining[index]
                    val isRunning  = timerRunning[index]
                    val isDone     = remaining == 0
                    val timerColor = when {
                        isDone    -> CarbsColor
                        isRunning -> FatColor
                        else      -> MaterialTheme.colorScheme.primary
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⏱", fontSize = 18.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "%02d:%02d".format(remaining / 60, remaining % 60),
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = timerColor
                            )
                            if (p.label.isNotBlank()) {
                                Text(p.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                            }
                        }
                        if (!isDone) {
                            FilledTonalButton(
                                onClick        = { timerRunning[index] = !isRunning },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors         = ButtonDefaults.filledTonalButtonColors(containerColor = timerColor, contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) {
                                Text(if (isRunning) "Pause" else "Start", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        OutlinedButton(
                            onClick        = { timerRemaining[index] = p.totalSeconds; timerRunning[index] = false },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Reset", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    textStepCounter++
                    val checked = checkedSteps.getOrElse(index) { false }
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.clickable { checkedSteps[index] = !checked }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .then(
                                    if (checked) Modifier.background(MaterialTheme.colorScheme.primary)
                                    else Modifier.background(Color.Transparent).border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$textStepCounter",
                                color      = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outlineVariant,
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text           = p.text,
                            style          = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                            color          = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier       = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}