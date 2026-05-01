package com.adasa.foodplan.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.*
import com.adasa.foodplan.ui.ingredient.AddEditIngredientUiState
import com.adasa.foodplan.ui.ingredient.AddEditIngredientViewModel
import kotlin.math.*

private val ProteinColor = Color(0xFF534AB7)
private val FatColor     = Color(0xFFBA7517)
private val CarbsColor   = Color(0xFF1D9E75)

private sealed interface PickerStep {
    data object Search : PickerStep
    data object Create : PickerStep
    data class  Stock(val ingredient: Ingredient) : PickerStep
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsTab(config: MealPlanConfig?, viewModel: SettingsViewModel) {
    val scrollState      = rememberScrollState()
    val goals            = config?.goals
    val powder           = config?.proteinPowder
    var showPowderPicker by remember { mutableStateOf(false) }
    val powderVM: PowderSearchViewModel = hiltViewModel()
    val powderResults by powderVM.results.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SettingsSection("Targets", "Weekly average — optimizer balances across days")
        SettingsCard {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                KcalInput(
                    value         = goals?.kcalTarget ?: 1450.0,
                    onValueChange = { viewModel.setKcal(it) }
                )
                Spacer(Modifier.height(24.dp))
                MacroCircularSlider(
                    kcal            = goals?.kcalTarget ?: 1450.0,
                    goals           = goals,
                    onProteinChange = { viewModel.setProtein(it) },
                    onFatChange     = { viewModel.setFat(it) },
                    onCarbsChange   = { viewModel.setCarbs(it) }
                )
            }
        }

        SettingsSection("Daily limits", "Hard per-day constraints · Leave blank for none")
        SettingsCard {
            MacroLimitsTable(
                goals        = goals,
                onMinKcal    = { viewModel.setMinKcal(it) },
                onMaxKcal    = { viewModel.setMaxKcal(it) },
                onMinProtein = { viewModel.setMinProtein(it) },
                onMaxProtein = { viewModel.setMaxProtein(it) },
                onMinFat     = { viewModel.setMinFat(it) },
                onMaxFat     = { viewModel.setMaxFat(it) },
                onMinCarbs   = { viewModel.setMinCarbs(it) },
                onMaxCarbs   = { viewModel.setMaxCarbs(it) },
            )
        }

        SettingsSection("Protein supplement", "Auto-fills the daily protein gap")
        SupplementCard(
            powder       = powder,
            onAutoFill   = { viewModel.setPowderAutoFill(it) },
            onLowStock   = { viewModel.setPowderLowStockWarning(it) },
            onPickerOpen = { showPowderPicker = true }
        )

        Spacer(Modifier.height(16.dp))
    }

    if (showPowderPicker) {
        PowderPickerSheet(
            results       = powderResults,
            currentPowder = powder,
            onQueryChange = { powderVM.onQueryChange(it) },
            onConfirm     = { ingredient, grams ->
                viewModel.setPowder(ingredient, grams)
                showPowderPicker = false
            },
            onDismiss     = {
                powderVM.onQueryChange("")
                showPowderPicker = false
            }
        )
    }
}

// ── Supplement card ───────────────────────────────────────────────────────────

@Composable
private fun SupplementCard(
    powder:       ProteinPowder?,
    onAutoFill:   (Boolean) -> Unit,
    onLowStock:   (Boolean) -> Unit,
    onPickerOpen: () -> Unit,
) {
    if (powder == null) {
        OutlinedCard(
            onClick  = onPickerOpen,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("💪", fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text("No protein powder set", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    "Tap to pick one from your food database",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = onPickerOpen) { Text("Add powder") }
            }
        }
        return
    }

    SettingsCard {
        Column {
            Row(
                modifier              = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("💪", fontSize = 20.sp) }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(powder.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 4.dp)) {
                        MacroBadge("${powder.proteinPer100g.toInt()}g P / 100g", ProteinColor)
                        MacroBadge("${powder.kcalPer100g.toInt()} kcal / 100g", FatColor)
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                val isLow         = powder.daysRemaining < 7
                val progressColor = if (isLow) FatColor else ProteinColor
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Stock remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${powder.gramsInStock.toInt()}g · ~${powder.daysRemaining.toInt()} days",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color      = if (isLow) FatColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress   = { (powder.gramsInStock / 600.0).coerceIn(0.0, 1.0).toFloat() },
                    modifier   = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color      = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap  = StrokeCap.Round,
                )
            }

            HorizontalDivider()
            SettingsSwitchRow(title = "Auto-fill protein gap", subtitle = "Adds a shake to hit your daily target", checked = powder.autoFillGap, onCheckedChange = onAutoFill)
            HorizontalDivider()
            SettingsSwitchRow(title = "Low stock warning", subtitle = "Alert when below 5 days", checked = powder.lowStockWarning, onCheckedChange = onLowStock)
            HorizontalDivider()
            Box(modifier = Modifier.clickable(onClick = onPickerOpen)) {
                SettingsRow(icon = "🔄", title = "Change or restock powder", onClick = onPickerOpen)
            }
        }
    }
}

@Composable
private fun MacroBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(5.dp), color = color.copy(alpha = 0.12f)) {
        Text(label, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}

// ── Powder picker sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PowderPickerSheet(
    results:       List<Ingredient>,
    currentPowder: ProteinPowder?,
    onQueryChange: (String) -> Unit,
    onConfirm:     (Ingredient, Double) -> Unit,
    onDismiss:     () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var step       by remember { mutableStateOf<PickerStep>(PickerStep.Search) }
    var query      by remember { mutableStateOf("") }
    var stockText  by remember { mutableStateOf("") }

    val addEditVM: AddEditIngredientViewModel = hiltViewModel()
    val addEditState by addEditVM.uiState.collectAsStateWithLifecycle()
    val addName      by addEditVM.name.collectAsStateWithLifecycle()
    val addProtein   by addEditVM.protein.collectAsStateWithLifecycle()
    val addKcal      by addEditVM.kcal.collectAsStateWithLifecycle()
    val addFat       by addEditVM.fat.collectAsStateWithLifecycle()
    val addCarbs     by addEditVM.carbs.collectAsStateWithLifecycle()

    // When ingredient is saved, build the Ingredient and move to the Stock step
    LaunchedEffect(addEditState) {
        val state = addEditState
        if (state is AddEditIngredientUiState.Saved) {
            val ingredient = Ingredient(
                id             = state.ingredientId,
                name           = addName,
                category       = "Kosttillskott",
                kcalPer100g    = addKcal,
                proteinPer100g = addProtein,
                fatPer100g     = addFat,
                carbsPer100g   = addCarbs,
            )
            step = PickerStep.Stock(ingredient)
        }
    }

    // Pre-fill stock when restocking the same powder
    LaunchedEffect(step) {
        val s = step
        if (s is PickerStep.Stock && s.ingredient.id == currentPowder?.ingredientId) {
            stockText = currentPowder!!.gramsInStock.toInt().toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
    ) {
        when (val s = step) {

            // ── Step 1: search ────────────────────────────────────────────
            is PickerStep.Search -> {
                Text(
                    "Choose protein powder",
                    style     = MaterialTheme.typography.titleMedium,
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape    = RoundedCornerShape(28.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        BasicTextField(
                            value         = query,
                            onValueChange = { query = it; onQueryChange(it) },
                            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                            singleLine    = true,
                            modifier      = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (query.isEmpty()) Text("Search powders…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                inner()
                            }
                        )
                    }
                }

                LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                    items(results, key = { it.id }) { ingredient ->
                        PowderRow(
                            ingredient = ingredient,
                            isCurrent  = ingredient.id == currentPowder?.ingredientId,
                            onClick    = { step = PickerStep.Stock(ingredient) }
                        )
                        if (ingredient != results.last()) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }

                // ── Add new powder button ─────────────────────────────────
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                TextButton(
                    onClick  = {
                        addEditVM.loadIngredient(null)
                        addEditVM.onCategoryChange("Kosttillskott")
                        step = PickerStep.Create
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("+ Add new powder to database")
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Step 2: create new ingredient ─────────────────────────────
            is PickerStep.Create -> {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { step = PickerStep.Search }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "New protein powder",
                        style    = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(48.dp)) // balance the back button
                }

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier              = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value         = addName,
                        onValueChange = addEditVM::onNameChange,
                        label         = { Text("Name") },
                        placeholder   = { Text("e.g. Core Protein Pro Vanilla") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                    )

                    // Nutrition grid
                    Surface(
                        shape  = RoundedCornerShape(12.dp),
                        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Nutrition per 100 g",
                                style  = MaterialTheme.typography.labelSmall,
                                color  = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                QuickNutritionField("Protein (g)", addProtein, addEditVM::onProteinChange, Modifier.weight(1f))
                                QuickNutritionField("Calories (kcal)", addKcal, addEditVM::onKcalChange, Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                QuickNutritionField("Fat (g)", addFat, addEditVM::onFatChange, Modifier.weight(1f))
                                QuickNutritionField("Carbs (g)", addCarbs, addEditVM::onCarbsChange, Modifier.weight(1f))
                            }
                        }
                    }

                    val isSaving = addEditState is AddEditIngredientUiState.Saving
                    val canSave  = addName.isNotBlank() && addProtein > 0 && addKcal > 0 && !isSaving

                    Button(
                        onClick  = addEditVM::saveIngredient,
                        enabled  = canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Save & continue")
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }

            // ── Step 3: stock amount ──────────────────────────────────────
            is PickerStep.Stock -> {
                Text("How much in stock?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("💪", fontSize = 18.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.ingredient.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${s.ingredient.proteinPer100g.toInt()}g P · ${s.ingredient.kcalPer100g.toInt()} kcal / 100g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = { step = PickerStep.Search }, modifier = Modifier.size(32.dp)) {
                            Text("✕", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Grams currently in stock", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value           = stockText,
                            onValueChange   = { v -> if (v.length <= 5 && v.all { it.isDigit() }) stockText = v },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine      = true,
                            modifier        = Modifier.width(90.dp),
                            textStyle       = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End)
                        )
                        Text("g", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick  = { onConfirm(s.ingredient, stockText.toDoubleOrNull() ?: 0.0) },
                    enabled  = stockText.toDoubleOrNull()?.let { it > 0 } == true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) { Text("Save powder") }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PowderRow(ingredient: Ingredient, isCurrent: Boolean, onClick: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(38.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("💪", fontSize = 16.sp) }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(ingredient.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (isCurrent) {
                    Surface(shape = RoundedCornerShape(4.dp), color = ProteinColor.copy(alpha = 0.12f)) {
                        Text("current", modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = ProteinColor, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Text(ingredient.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 3.dp)) {
                MacroBadge("${ingredient.proteinPer100g.toInt()}g P / 100g", ProteinColor)
                MacroBadge("${ingredient.kcalPer100g.toInt()} kcal", FatColor)
            }
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickNutritionField(label: String, value: Double, onChange: (Double) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(if (value == 0.0) "" else value.toBigDecimal().stripTrailingZeros().toPlainString()) }
    OutlinedTextField(
        value         = text,
        onValueChange = { input -> text = input; input.toDoubleOrNull()?.let { onChange(it) } },
        label         = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier      = modifier
    )
}

// ── Kcal input ────────────────────────────────────────────────────────────────

@Composable
private fun KcalInput(value: Double, onValueChange: (Double) -> Unit) {
    var text by remember(value.toInt()) { mutableStateOf(value.toInt().toString()) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            BasicTextField(
                value         = text,
                onValueChange = { v ->
                    if (v.all { it.isDigit() } && v.length <= 5) {
                        text = v
                        v.toDoubleOrNull()?.let { onValueChange(it) }
                    }
                },
                textStyle = TextStyle(
                    fontSize      = 48.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = MaterialTheme.colorScheme.onSurface,
                    textAlign     = TextAlign.End,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.width(160.dp),
                singleLine      = true
            )
            Text(" kcal", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
        }
        HorizontalDivider(modifier = Modifier.width(200.dp), thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
        Text("daily average target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    }
}

// ── Circular macro slider ─────────────────────────────────────────────────────

@Composable
private fun MacroCircularSlider(
    kcal:            Double,
    goals:           NutritionGoals?,
    onProteinChange: (Double?) -> Unit,
    onFatChange:     (Double?) -> Unit,
    onCarbsChange:   (Double?) -> Unit
) {
    fun arcFrac(from: Float, to: Float): Float {
        var d = to - from
        if (d < 0f) d += 1f
        return d
    }

    val init = remember(Unit) {
        val pF = goals?.let { (it.resolvedProtein * 4 / kcal).toFloat() } ?: 0.33f
        val fF = goals?.let { (it.resolvedFat * 9 / kcal).toFloat() }     ?: 0.27f
        floatArrayOf(pF.coerceIn(0.05f, 0.90f), (pF + fF).coerceIn(0.10f, 0.95f), 0f)
    }
    var handles by remember { mutableStateOf(init.copyOf()) }

    val pFrac = arcFrac(handles[2], handles[0])
    val fFrac = arcFrac(handles[0], handles[1])
    val pG = (kcal * pFrac / 4).roundToInt()
    val fG = (kcal * fFrac / 9).roundToInt()
    val cG = (kcal * arcFrac(handles[1], handles[2]) / 4).roundToInt()
    val pP = (pFrac * 100).roundToInt()
    val fP = (fFrac * 100).roundToInt()
    val cP = 100 - pP - fP

    fun snapFrac(frac: Float, kcalPerGram: Float): Float {
        val grams = (kcal * frac / kcalPerGram).roundToInt().coerceAtLeast(1)
        return (grams * kcalPerGram / kcal).toFloat()
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        val canvasSize   = 168.dp
        val strokeWidth  = 24.dp
        val handleRadius = 12.dp

        Canvas(modifier = Modifier.size(canvasSize).pointerInput(Unit) {
            val cx = size.width / 2f; val cy = size.height / 2f
            val r = cx - strokeWidth.toPx() / 2f
            val hitR = handleRadius.toPx() + 14f
            val MIN_ARC = 0.04f

            fun touchFrac(pos: Offset): Float {
                val dx = pos.x - cx; val dy = pos.y - cy
                var a = atan2(dy, dx) + PI.toFloat() / 2f
                while (a < 0f) a += 2f * PI.toFloat()
                while (a >= 2f * PI.toFloat()) a -= 2f * PI.toFloat()
                return a / (2f * PI.toFloat())
            }

            fun handlePos(h: Float): Offset {
                val a = h * 2f * PI.toFloat() - PI.toFloat() / 2f
                return Offset(cx + cos(a) * r, cy + sin(a) * r)
            }

            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var activeHandle = -1; var bestDist = Float.MAX_VALUE
                handles.forEachIndexed { i, h ->
                    val d = (down.position - handlePos(h)).getDistance()
                    if (d < hitR && d < bestDist) { bestDist = d; activeHandle = i }
                }
                if (activeHandle < 0) return@awaitEachGesture
                down.consume()

                while (true) {
                    val event  = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    val raw = touchFrac(change.position)
                    val h   = handles.copyOf()
                    when (activeHandle) {
                        0 -> {
                            val maxP  = (arcFrac(h[2], h[1]) - MIN_ARC).coerceAtLeast(MIN_ARC)
                            val pFrac = snapFrac(arcFrac(h[2], raw), 4f).coerceIn(MIN_ARC, maxP)
                            h[0] = (h[2] + pFrac) % 1f
                            onProteinChange((kcal * pFrac / 4).toDouble())
                            onFatChange((kcal * arcFrac(h[0], h[1]) / 9).toDouble())
                        }
                        1 -> {
                            val maxF  = (arcFrac(h[0], h[2]) - MIN_ARC).coerceAtLeast(MIN_ARC)
                            val fFrac = snapFrac(arcFrac(h[0], raw), 9f).coerceIn(MIN_ARC, maxF)
                            h[1] = (h[0] + fFrac) % 1f
                            onFatChange((kcal * fFrac / 9).toDouble())
                            onCarbsChange((kcal * arcFrac(h[1], h[2]) / 4).toDouble())
                        }
                        2 -> {
                            val maxC  = (arcFrac(h[1], h[0]) - MIN_ARC).coerceAtLeast(MIN_ARC)
                            val cFrac = snapFrac(arcFrac(h[1], raw), 4f).coerceIn(MIN_ARC, maxC)
                            h[2] = (h[1] + cFrac) % 1f
                            onCarbsChange((kcal * cFrac / 4).toDouble())
                            onProteinChange((kcal * arcFrac(h[2], h[0]) / 4).toDouble())
                        }
                    }
                    handles = h; change.consume()
                }
            }
        }) {
            val cx = size.width / 2f; val cy = size.height / 2f
            val sw = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val inset = strokeWidth.toPx() / 2f
            val arcSz = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx())
            val arcTl = Offset(inset, inset)
            val r = cx - strokeWidth.toPx() / 2f

            fun startDeg(f: Float) = f * 360f - 90f
            fun sweepDeg(from: Float, to: Float) = arcFrac(from, to) * 360f

            drawArc(ProteinColor, startDeg(handles[2]), sweepDeg(handles[2], handles[0]), false, arcTl, arcSz, style = sw)
            drawArc(FatColor,     startDeg(handles[0]), sweepDeg(handles[0], handles[1]), false, arcTl, arcSz, style = sw)
            drawArc(CarbsColor,   startDeg(handles[1]), sweepDeg(handles[1], handles[2]), false, arcTl, arcSz, style = sw)

            val handleCols = listOf(Pair(ProteinColor, FatColor), Pair(FatColor, CarbsColor), Pair(CarbsColor, ProteinColor))
            handles.forEachIndexed { i, h ->
                val angle = h * 2f * PI.toFloat() - PI.toFloat() / 2f
                val hx = cx + cos(angle).toFloat() * r; val hy = cy + sin(angle).toFloat() * r
                drawCircle(Color.White,          handleRadius.toPx(), Offset(hx, hy))
                drawCircle(handleCols[i].first,  handleRadius.toPx(), Offset(hx, hy), style = Stroke(width = 2.5.dp.toPx()))
                drawCircle(handleCols[i].second, 4.5.dp.toPx(),       Offset(hx, hy))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(Triple(ProteinColor, "Protein", "$pG g  ($pP%)"), Triple(FatColor, "Fat", "$fG g  ($fP%)"), Triple(CarbsColor, "Carbs", "$cG g  ($cP%)"))
                .forEach { (color, label, info) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(modifier = Modifier.size(10.dp), shape = RoundedCornerShape(50), color = color) {}
                        Column {
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(info,  style = MaterialTheme.typography.bodySmall,  fontWeight = FontWeight.Medium)
                        }
                    }
                }
        }
    }
}

// ── Daily limits table ────────────────────────────────────────────────────────

@Composable
private fun MacroLimitsTable(
    goals:        NutritionGoals?,
    onMinKcal:    (Double?) -> Unit,
    onMaxKcal:    (Double?) -> Unit,
    onMinProtein: (Double?) -> Unit,
    onMaxProtein: (Double?) -> Unit,
    onMinFat:     (Double?) -> Unit,
    onMaxFat:     (Double?) -> Unit,
    onMinCarbs:   (Double?) -> Unit,
    onMaxCarbs:   (Double?) -> Unit,
) {
    data class Row(val label: String, val unit: String, val color: Color, val alpha: Float, val min: Double?, val max: Double?, val onMin: (Double?) -> Unit, val onMax: (Double?) -> Unit)

    val rows = listOf(
        Row("kcal",    "",  ProteinColor, 1.0f, goals?.minKcalPerDay,    goals?.maxKcalPerDay,    onMinKcal,    onMaxKcal),
        Row("Protein", "g", ProteinColor, 0.5f, goals?.minProteinPerDay, goals?.maxProteinPerDay, onMinProtein, onMaxProtein),
        Row("Fat",     "g", FatColor,     1.0f, goals?.minFatPerDay,     goals?.maxFatPerDay,     onMinFat,     onMaxFat),
        Row("Carbs",   "g", CarbsColor,   1.0f, goals?.minCarbsPerDay,   goals?.maxCarbsPerDay,   onMinCarbs,   onMaxCarbs),
    )

    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Spacer(Modifier.weight(1f))
            Text("Min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
            Spacer(Modifier.width(24.dp))
            Text("Max", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
        }
        rows.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(row.color.copy(alpha = row.alpha)))
                Spacer(Modifier.width(8.dp))
                Text(row.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                if (row.unit.isNotEmpty()) Text(" ${row.unit}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                LimitInput(value = row.min, onChange = row.onMin)
                Text(" – ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LimitInput(value = row.max, onChange = row.onMax)
            }
        }
    }
}

@Composable
private fun LimitInput(value: Double?, onChange: (Double?) -> Unit) {
    var text by remember(value?.toInt()) { mutableStateOf(value?.toInt()?.toString() ?: "") }
    BasicTextField(
        value           = text,
        onValueChange   = { v -> if (v.length <= 5 && v.all { it.isDigit() }) { text = v; onChange(v.toDoubleOrNull()) } },
        textStyle       = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine      = true,
        decorationBox   = { inner ->
            Box(modifier = Modifier.width(64.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 5.dp), contentAlignment = Alignment.CenterEnd) {
                if (text.isEmpty()) Text("—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                inner()
            }
        }
    )
}