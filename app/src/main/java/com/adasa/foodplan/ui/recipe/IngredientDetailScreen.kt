package com.adasa.foodplan.ui.ingredient

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.Ingredient

private val ProteinColor = Color(0xFF534AB7)
private val FatColor     = Color(0xFFBA7517)
private val CarbsColor   = Color(0xFF1D9E75)

/**
 * Dual-mode ingredient detail screen.
 *
 * Standalone mode (recipeIngredientIndex == null):
 *   - Edit ✎ and Delete 🗑 in the top bar
 *   - No amount card
 *
 * Recipe context mode (recipeIngredientIndex != null):
 *   - No edit/delete — back arrow only
 *   - Amount card at the very top with live macro preview
 *   - "Save amount" stores the new grams via savedStateHandle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailScreen(
    ingredientId:           String,
    recipeIngredientIndex:  Int?    = null,
    currentAmountGrams:     Double? = null,
    onBackClick:            () -> Unit,
    onEditClick:            (String) -> Unit,
    onAmountSaved:          (index: Int, grams: Double) -> Unit = { _, _ -> },
    viewModel:              IngredientDetailViewModel = hiltViewModel()
) {
    val isRecipeContext = recipeIngredientIndex != null

    LaunchedEffect(ingredientId) { viewModel.loadIngredient(ingredientId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ingredient?") },
            text  = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteIngredient(); showDeleteDialog = false; onBackClick() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
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
                    if (!isRecipeContext && uiState is IngredientDetailUiState.Success) {
                        IconButton(onClick = { onEditClick(ingredientId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is IngredientDetailUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            is IngredientDetailUiState.NotFound ->
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("Ingredient not found")
                }
            is IngredientDetailUiState.Success -> {
                IngredientDetailContent(
                    ingredient            = state.ingredient,
                    isRecipeContext       = isRecipeContext,
                    recipeIngredientIndex = recipeIngredientIndex,
                    currentAmountGrams    = currentAmountGrams ?: 100.0,
                    onAmountSaved         = onAmountSaved,
                    modifier              = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun IngredientDetailContent(
    ingredient:            Ingredient,
    isRecipeContext:       Boolean,
    recipeIngredientIndex: Int?,
    currentAmountGrams:    Double,
    onAmountSaved:         (Int, Double) -> Unit,
    modifier:              Modifier,
) {
    // Amount state — only relevant in recipe context
    var amountText by remember { mutableStateOf(currentAmountGrams.toInt().toString()) }
    val grams = amountText.toDoubleOrNull() ?: currentAmountGrams

    val proteinKcal = ingredient.proteinPer100g * 4
    val fatKcal     = ingredient.fatPer100g     * 9
    val carbsKcal   = ingredient.carbsPer100g   * 4
    val totalMacro  = proteinKcal + fatKcal + carbsKcal

    LazyColumn(
        modifier       = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Recipe context: amount card ───────────────────────────────────
        if (isRecipeContext && recipeIngredientIndex != null) {
            item {
                Surface(
                    shape    = RoundedCornerShape(14.dp),
                    color    = MaterialTheme.colorScheme.surface,
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Amount in this recipe",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Grams", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Row(
                                modifier          = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                BasicTextField(
                                    value           = amountText,
                                    onValueChange   = { if (it.length <= 6 && it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                                    textStyle       = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush     = SolidColor(MaterialTheme.colorScheme.primary),
                                    singleLine      = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier        = Modifier.width(72.dp)
                                )
                                Text("g", style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        // Live macro pills
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MacroPill("P ${(grams * ingredient.proteinPer100g / 100).toInt()}g", ProteinColor)
                            MacroPill("F ${(grams * ingredient.fatPer100g     / 100).toInt()}g", FatColor)
                            MacroPill("C ${(grams * ingredient.carbsPer100g   / 100).toInt()}g", CarbsColor)
                            MacroPill("${(grams * ingredient.kcalPer100g      / 100).toInt()} kcal",
                                MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick  = { onAmountSaved(recipeIngredientIndex, grams) },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(50)
                        ) { Text("Save amount") }
                    }
                }
            }
        }

        // ── Hero — name, category, donut ─────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        ingredient.name,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
                        Text(
                            ingredient.category,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (totalMacro > 0) {
                            IngredientDonut(
                                proteinFrac = (proteinKcal / totalMacro).toFloat(),
                                fatFrac     = (fatKcal     / totalMacro).toFloat(),
                                carbsFrac   = (carbsKcal   / totalMacro).toFloat(),
                                kcalLabel   = "${ingredient.kcalPer100g.toInt()} kcal"
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "per 100 g",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            MacroLegendRow(ProteinColor, "Protein", "${ingredient.proteinPer100g.toInt()}g")
                            MacroLegendRow(FatColor,     "Fat",     "${ingredient.fatPer100g.toInt()}g")
                            MacroLegendRow(CarbsColor,   "Carbs",   "${ingredient.carbsPer100g.toInt()}g")
                        }
                    }
                }
            }
        }

        // ── Nutrition table ───────────────────────────────────────────────
        item {
            IngDetailSectionLabel("Nutrition per 100 g",
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column {
                    NutritionRow("Energy",       "${ingredient.kcalPer100g.toInt()} kcal")
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    NutritionRow("Protein",      "${ingredient.proteinPer100g.toInt()} g")
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    NutritionRow("Fat",          "${ingredient.fatPer100g.toInt()} g")
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    NutritionRow("Carbohydrates","${ingredient.carbsPer100g.toInt()} g")
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    NutritionRow("Source", ingredient.source.displayName,
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Small helpers ─────────────────────────────────────────────────────────────

@Composable
private fun IngredientDonut(proteinFrac: Float, fatFrac: Float, carbsFrac: Float, kcalLabel: String) {
    val trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sw    = 10.dp.toPx()
            val inset = sw / 2f
            val arcSz = Size(size.width - sw, size.height - sw)
            val tl    = Offset(inset, inset)
            val style = Stroke(width = sw, cap = StrokeCap.Butt)
            val pS = proteinFrac * 360f; val fS = fatFrac * 360f; val cS = carbsFrac * 360f
            drawArc(trackColor, 0f, 360f, false, tl, arcSz, style = style)
            drawArc(ProteinColor, -90f,          pS, false, tl, arcSz, style = style)
            drawArc(FatColor,     -90f + pS,     fS, false, tl, arcSz, style = style)
            drawArc(CarbsColor,   -90f + pS + fS, cS, false, tl, arcSz, style = style)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(kcalLabel.substringBefore(" "), style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("kcal", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun MacroLegendRow(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun MacroPill(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.12f)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
    }
}

@Composable
private fun IngDetailSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp, modifier = modifier)
}

@Composable
private fun NutritionRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor)
    }
}