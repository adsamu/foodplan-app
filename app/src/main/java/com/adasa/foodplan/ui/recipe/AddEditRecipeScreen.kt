package com.adasa.foodplan.ui.recipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.*

private val ProteinColor = Color(0xFF534AB7)
private val FatColor     = Color(0xFFBA7517)
private val CarbsColor   = Color(0xFF1D9E75)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditRecipeScreen(
    recipeId: String?,
    onSaved: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToAddIngredient: () -> Unit = {},
    onViewIngredient: (ingredientId: String, index: Int, currentGrams: Double) -> Unit = { _, _, _ -> },
    viewModel: AddEditRecipeViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.loadRecipe(recipeId) }

    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val name              by viewModel.name.collectAsStateWithLifecycle()
    val type              by viewModel.type.collectAsStateWithLifecycle()
    val mealCategories    by viewModel.mealCategories.collectAsStateWithLifecycle()
    val componentCategory by viewModel.componentCategory.collectAsStateWithLifecycle()
    val ingredients       by viewModel.ingredients.collectAsStateWithLifecycle()
    val steps             by viewModel.steps.collectAsStateWithLifecycle()
    val nutrition         by viewModel.nutrition.collectAsStateWithLifecycle()

    var showIngredientSheet  by remember { mutableStateOf(false) }
    var showAddTimerDialog   by remember { mutableStateOf(false) }
    var editingTimerIndex    by remember { mutableStateOf<Int?>(null) }

    // Drag-to-reorder — reorder happens ONCE on drag end, not live during drag
    val lazyListState  = rememberLazyListState()
    var draggingId     by remember { mutableStateOf<String?>(null) }
    var draggingFrom   by remember { mutableIntStateOf(-1) }
    var draggingDelta  by remember { mutableStateOf(0f) }
    // Lambda so the pointerInput closure always reads the latest delta without restarting
    val getDelta: () -> Float = { draggingDelta }

    LaunchedEffect(uiState) { if (uiState is AddEditUiState.Saved) onSaved() }

    if (showIngredientSheet) {
        IngredientSearchSheet(
            onDismiss          = { showIngredientSheet = false },
            onIngredientSelect = { id, g -> viewModel.addIngredient(id, g); showIngredientSheet = false },
            onRecipeSelect     = { id, p -> viewModel.addSubRecipe(id, p); showIngredientSheet = false },
            onNavigateToAddIngredient = { showIngredientSheet = false; onNavigateToAddIngredient() }
        )
    }

    if (showAddTimerDialog) {
        TimerEditDialog(
            step      = StepUi.TimerStep(id = "", label = "", totalSeconds = 0),
            onSave    = { label, secs -> viewModel.addTimer(label, secs); showAddTimerDialog = false },
            onDismiss = { showAddTimerDialog = false }
        )
    }

    editingTimerIndex?.let { idx ->
        val existing = steps.getOrNull(idx) as? StepUi.TimerStep
        if (existing != null) {
            TimerEditDialog(
                step      = existing,
                onSave    = { label, secs -> viewModel.updateTimer(idx, label, secs); editingTimerIndex = null },
                onDismiss = { editingTimerIndex = null }
            )
        }
    }

    val textStepNumbers = run {
        var counter = 0
        steps.map { if (it is StepUi.TextStep) ++counter else null }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recipeId == null) "New recipe" else "Edit recipe") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::saveRecipe, enabled = uiState !is AddEditUiState.Saving) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick  = viewModel::saveRecipe,
                    enabled  = uiState !is AddEditUiState.Saving,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    shape    = RoundedCornerShape(50)
                ) {
                    if (uiState is AddEditUiState.Saving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save recipe", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state          = lazyListState,
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // ── Name / type / categories ───────────────────────────────────
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name, onValueChange = viewModel::onNameChange,
                        label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    TypeToggle(type = type, onTypeChange = viewModel::onTypeChange)
                    CategorySection(
                        type = type, mealCategories = mealCategories,
                        componentCategory = componentCategory,
                        onMealCategoryToggle = viewModel::onMealCategoryToggle,
                        onComponentCategorySelect = viewModel::onComponentCategorySelect
                    )
                }
            }

            // ── Sticky macro banner ────────────────────────────────────────
            stickyHeader(key = "macro_banner") { MacroBanner(nutrition = nutrition) }

            item { FormSectionLabel("Ingredients", Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) }

            // ── Ingredients ────────────────────────────────────────────────
            itemsIndexed(ingredients, key = { _, ing -> ing.ingredientId ?: ing.subRecipeId ?: ing.name }) { index, ingredient ->
                IngredientRow(
                    ingredient     = ingredient,
                    onAmountChange = { viewModel.updateIngredientAmount(index, it) },
                    onRemove       = { viewModel.removeIngredient(index) },
                    onViewDetail   = { id -> onViewIngredient(id, index, ingredient.amount) }
                )
            }

            // ── Add ingredient button ──────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showIngredientSheet = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                            Text("+", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Add ingredient or recipe", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // ── Instructions sticky header — pushes macro banner away ──────
            stickyHeader(key = "instructions_header") {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                    FormSectionLabel("Instructions", Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }

            // ── Step rows with drag-to-reorder ─────────────────────────────
            itemsIndexed(steps, key = { _, step -> step.id }) { index, step ->
                val isDragging = step.id == draggingId

                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            if (isDragging) {
                                translationY  = draggingDelta
                                shadowElevation = 8f
                                alpha = 0.92f
                            }
                        }
                ) {
                    val handle = dragHandleModifier(
                        stepId       = step.id,
                        stepIndex    = index,
                        steps        = steps,
                        listState    = lazyListState,
                        getDelta     = getDelta,
                        onDragStart  = { fromIdx ->
                            draggingId    = step.id
                            draggingFrom  = fromIdx
                            draggingDelta = 0f
                        },
                        onDelta      = { dy -> draggingDelta += dy },
                        onDragEnd    = { from, to ->
                            if (from != to) viewModel.reorderSteps(from, to)
                            draggingId    = null
                            draggingFrom  = -1
                            draggingDelta = 0f
                        }
                    )
                    when (step) {
                        is StepUi.TextStep ->
                            TextStepRow(
                                step         = step,
                                stepNumber   = textStepNumbers[index] ?: (index + 1),
                                onTextChange = { viewModel.updateStep(index, it) },
                                onRemove     = { viewModel.removeStep(index) },
                                dragHandle   = handle
                            )
                        is StepUi.TimerStep ->
                            TimerStepEditRow(
                                step       = step,
                                onEdit     = { editingTimerIndex = index },
                                onRemove   = { viewModel.removeStep(index) },
                                dragHandle = handle
                            )
                    }
                }
            }

            // ── Inline timer config card ───────────────────────────────────
            // (replaced by dialog — nothing to render inline)

            // ── Add step / timer buttons ───────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AddTileButton(
                        label    = "+ Step",
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick  = { viewModel.addStep() }
                    )
                    AddTileButton(
                        label    = "⏱ Timer",
                        color    = FatColor,
                        modifier = Modifier.weight(1f),
                        onClick  = { showAddTimerDialog = true }
                    )
                }
            }
        }
    }
}

// ── Drag handle helper ────────────────────────────────────────────────────────

@Composable
private fun dragHandleModifier(
    stepId:     String,
    stepIndex:  Int,
    steps:      List<StepUi>,
    listState:  LazyListState,
    getDelta:   () -> Float,
    onDragStart: (fromIndex: Int) -> Unit,
    onDelta:    (Float) -> Unit,
    onDragEnd:  (from: Int, to: Int) -> Unit,
): Modifier {
    val currentSteps by rememberUpdatedState(steps)
    val currentIndex by rememberUpdatedState(stepIndex)
    val stepIdSet    by rememberUpdatedState(steps.map { it.id }.toHashSet())

    return Modifier.pointerInput(stepId) {          // stable key — gesture never restarts
        detectDragGestures(
            onDragStart = { _ -> onDragStart(currentIndex) },
            onDrag      = { change, amount ->
                change.consume()
                onDelta(amount.y)
            },
            onDragEnd = {
                // Compute target index from the dragged item's VISUAL centre
                val delta    = getDelta()
                val info     = listState.layoutInfo.visibleItemsInfo
                val me       = info.firstOrNull { it.key == stepId }
                val toIndex  = if (me != null) {
                    val visualCenter = me.offset + me.size / 2 + delta.toInt()
                    // Find the step item whose centre is closest to our visual centre
                    val stepItems = info.filter { it.key in stepIdSet }
                    val nearest   = stepItems.minByOrNull {
                        kotlin.math.abs((it.offset + it.size / 2) - visualCenter)
                    }
                    currentSteps.indexOfFirst { it.id == nearest?.key }
                        .takeIf { it >= 0 } ?: currentIndex
                } else currentIndex
                onDragEnd(currentIndex, toIndex)
            },
            onDragCancel = { onDragEnd(currentIndex, currentIndex) }
        )
    }
}

// ── Macro banner ──────────────────────────────────────────────────────────────

@Composable
private fun MacroBanner(nutrition: RecipeNutrition) {
    val pK = nutrition.protein * 4
    val fK = nutrition.fat     * 9
    val cK = nutrition.carbs   * 4
    val total = pK + fK + cK

    Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (total > 0) {
                MacroDonut(
                    proteinFrac = (pK / total).toFloat(),
                    fatFrac     = (fK / total).toFloat(),
                    carbsFrac   = (cK / total).toFloat(),
                    size        = 60.dp, strokeWidth = 10.dp
                )
            } else {
                Box(Modifier.size(60.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("${nutrition.kcal.toInt()} kcal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MacroChip(ProteinColor, "P", "${nutrition.protein.toInt()}g")
                    MacroChip(FatColor,     "F", "${nutrition.fat.toInt()}g")
                    MacroChip(CarbsColor,   "C", "${nutrition.carbs.toInt()}g")
                }
            }
        }
    }
}

@Composable
private fun MacroChip(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text("$label $value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun MacroDonut(proteinFrac: Float, fatFrac: Float, carbsFrac: Float, size: Dp, strokeWidth: Dp) {
    val trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
    Canvas(modifier = Modifier.size(size)) {
        val sw = strokeWidth.toPx(); val inset = sw / 2f
        val arcSz = Size(this.size.width - sw, this.size.height - sw); val tl = Offset(inset, inset)
        val style = Stroke(width = sw, cap = StrokeCap.Butt)
        drawArc(trackColor, 0f, 360f, false, tl, arcSz, style = style)
        val pS = proteinFrac * 360f; val fS = fatFrac * 360f; val cS = carbsFrac * 360f
        drawArc(ProteinColor, -90f,          pS, false, tl, arcSz, style = style)
        drawArc(FatColor,     -90f + pS,     fS, false, tl, arcSz, style = style)
        drawArc(CarbsColor,   -90f + pS + fS, cS, false, tl, arcSz, style = style)
    }
}

// ── Ingredient row ────────────────────────────────────────────────────────────

@Composable
private fun IngredientRow(
    ingredient:     RecipeIngredientUi,
    onAmountChange: (Double) -> Unit,
    onRemove:       () -> Unit,
    onViewDetail:   (String) -> Unit = {},
) {
    // Fix: key on ingredient identity (id), not on amount — prevents keyboard dismissal on every keystroke
    var amountText by remember(ingredient.ingredientId, ingredient.subRecipeId) {
        mutableStateOf(
            if (ingredient.amount == 0.0) ""
            else ingredient.amount.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(
                if (ingredient.ingredientId != null)
                    Modifier.clickable { onViewDetail(ingredient.ingredientId) }
                else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier         = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (ingredient.subRecipeId != null) Icons.Default.Blender else Icons.Default.Restaurant,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(16.dp)
                )
            }
            Text(ingredient.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, fontWeight = FontWeight.Medium)
            BasicAmountField(value = amountText, unit = ingredient.unit, onValueChange = { text ->
                amountText = text
                text.toDoubleOrNull()?.let { onAmountChange(it) }
            })
            // Stop click propagation on remove so it doesn't also open the detail screen
            IconButton(
                onClick  = { onRemove() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
            }
        }
        if (ingredient.ingredientId != null) {
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.padding(start = 40.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MacroPill("P ${ingredient.protein.toInt()}g", ProteinColor)
                MacroPill("F ${ingredient.fat.toInt()}g",     FatColor)
                MacroPill("C ${ingredient.carbs.toInt()}g",   CarbsColor)
                MacroPill("${ingredient.kcal.toInt()} kcal", MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MacroPill(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.12f)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
    }
}

@Composable
private fun BasicAmountField(value: String, unit: String, onValueChange: (String) -> Unit) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier              = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        BasicTextField(
            value           = value,
            onValueChange   = onValueChange,
            modifier        = Modifier.width(44.dp),
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle       = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush     = SolidColor(MaterialTheme.colorScheme.primary)
        )
        Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Step rows ─────────────────────────────────────────────────────────────────

@Composable
private fun TextStepRow(
    step:         StepUi.TextStep,
    stepNumber:   Int,
    onTextChange: (String) -> Unit,
    onRemove:     () -> Unit,
    dragHandle:   Modifier,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Drag handle ≡
        Text(
            text     = "≡",
            fontSize = 18.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = dragHandle
        )
        Box(
            modifier         = Modifier.size(26.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text("$stepNumber", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        BasicTextField(
            value         = step.text,
            onValueChange = onTextChange,
            modifier      = Modifier.weight(1f),
            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                if (step.text.isEmpty()) Text("Step $stepNumber…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                inner()
            }
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
        }
    }
}

/** Timer row in EDIT mode — no countdown, shows edit + delete */
@Composable
private fun TimerStepEditRow(
    step:       StepUi.TimerStep,
    onEdit:     () -> Unit,
    onRemove:   () -> Unit,
    dragHandle: Modifier,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("≡", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f), modifier = dragHandle)
        Text("⏱", fontSize = 18.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatTime(step.totalSeconds),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = FatColor
            )
            if (step.label.isNotBlank()) {
                Text(step.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            }
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit timer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
        }
    }
}

internal fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

// ── Timer edit dialog ─────────────────────────────────────────────────────────

@Composable
private fun TimerEditDialog(
    step:      StepUi.TimerStep,
    onSave:    (String, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val isNew   = step.id.isEmpty()
    var label   by remember { mutableStateOf(step.label) }
    var minutes by remember { mutableStateOf(if (isNew) "" else (step.totalSeconds / 60).toString()) }
    var seconds by remember { mutableStateOf(if (isNew) "" else (step.totalSeconds % 60).toString().padStart(2, '0')) }
    val total   = (minutes.toIntOrNull() ?: 0) * 60 + (seconds.toIntOrNull() ?: 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "New timer" else "Edit timer") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = minutes, onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) minutes = it },
                        label = { Text("Min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    Text(":", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = seconds, onValueChange = { v -> if (v.length <= 2 && v.all(Char::isDigit) && (v.toIntOrNull() ?: 0) < 60) seconds = v },
                        label = { Text("Sec") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(label, total) }, enabled = total > 0) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Add tile button ───────────────────────────────────────────────────────────

@Composable
private fun AddTileButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .border(BorderStroke(1.5.dp, color), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Medium)
    }
}

// ── Form helpers ──────────────────────────────────────────────────────────────

@Composable
private fun FormSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp, modifier = modifier)
}

@Composable
private fun TypeToggle(type: RecipeType, onTypeChange: (RecipeType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(RecipeType.MEAL, RecipeType.COMPONENT).forEach { t ->
            FilterChip(selected = type == t, onClick = { onTypeChange(t) }, label = { Text(t.displayName) }, shape = RoundedCornerShape(8.dp))
        }
    }
}

@Composable
private fun CategorySection(
    type: RecipeType, mealCategories: Set<MealCategory>,
    componentCategory: ComponentCategory?,
    onMealCategoryToggle: (MealCategory) -> Unit,
    onComponentCategorySelect: (ComponentCategory) -> Unit,
) {
    if (type == RecipeType.MEAL) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MealCategory.entries.forEach { cat ->
                FilterChip(selected = cat in mealCategories, onClick = { onMealCategoryToggle(cat) }, label = { Text(cat.displayName) }, shape = RoundedCornerShape(8.dp))
            }
        }
    } else {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ComponentCategory.entries.forEach { cat ->
                FilterChip(selected = componentCategory == cat, onClick = { onComponentCategorySelect(cat) }, label = { Text(cat.displayName) }, shape = RoundedCornerShape(8.dp))
            }
        }
    }
}

// ── Ingredient search sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientSearchSheet(
    onDismiss: () -> Unit,
    onIngredientSelect: (String, Double) -> Unit,
    onRecipeSelect: (String, Double) -> Unit,
    onNavigateToAddIngredient: () -> Unit,
    sheetViewModel: IngredientSearchViewModel = hiltViewModel(),
) {
    val searchQuery       by sheetViewModel.searchQuery.collectAsStateWithLifecycle()
    val ingredientResults by sheetViewModel.ingredientResults.collectAsStateWithLifecycle()
    val recipeResults     by sheetViewModel.recipeResults.collectAsStateWithLifecycle()
    var selectedTab        by remember { mutableIntStateOf(0) }
    var pendingIngId       by remember { mutableStateOf<String?>(null) }
    var pendingRecId       by remember { mutableStateOf<String?>(null) }
    var amountText         by remember { mutableStateOf("") }
    var showAmountDialog   by remember { mutableStateOf(false) }

    if (showAmountDialog) {
        AlertDialog(
            onDismissRequest = { showAmountDialog = false },
            title = { Text(if (pendingRecId != null) "Portions" else "Amount (g)") },
            text  = {
                OutlinedTextField(value = amountText, onValueChange = { amountText = it },
                    label = { Text(if (pendingRecId != null) "Portions" else "Grams") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            },
            confirmButton = {
                TextButton(onClick = {
                    val a = amountText.toDoubleOrNull() ?: return@TextButton
                    pendingIngId?.let { onIngredientSelect(it, a) }
                    pendingRecId?.let { onRecipeSelect(it, a) }
                    showAmountDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAmountDialog = false }) { Text("Cancel") } }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        // Give the sheet a bounded height so LazyColumn can scroll within it
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)) {

            // ── Fixed header ──────────────────────────────────────────────
            Text(
                "Add ingredient",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape    = RoundedCornerShape(28.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    BasicTextField(
                        value         = searchQuery,
                        onValueChange = sheetViewModel::onQueryChange,
                        textStyle     = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine    = true,
                        modifier      = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) Text("Search…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            inner()
                        }
                    )
                }
            }
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Ingredients") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Recipes") })
            }

            // ── Scrollable list ───────────────────────────────────────────
            LazyColumn(
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (selectedTab == 0) {
                    item {
                        TextButton(
                            onClick  = onNavigateToAddIngredient,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) { Text("+ New ingredient") }
                    }
                    items(ingredientResults, key = { it.id }) { item ->
                        ListItem(
                            headlineContent   = { Text(item.name, fontWeight = FontWeight.Medium) },
                            supportingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MacroPill("P ${item.proteinPer100g.toInt()}g", ProteinColor)
                                    MacroPill("F ${item.fatPer100g.toInt()}g",     FatColor)
                                    MacroPill("C ${item.carbsPer100g.toInt()}g",   CarbsColor)
                                    MacroPill("${item.kcalPer100g.toInt()} kcal",  MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            modifier = Modifier.clickable {
                                pendingIngId = item.id; pendingRecId = null
                                amountText = "100"; showAmountDialog = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                } else {
                    items(recipeResults, key = { it.id }) { item ->
                        ListItem(
                            headlineContent = { Text(item.name, fontWeight = FontWeight.Medium) },
                            trailingContent = {
                                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
                                    Text(item.typeBadge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            },
                            modifier = Modifier.clickable {
                                pendingRecId = item.id; pendingIngId = null
                                amountText = "1"; showAmountDialog = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}