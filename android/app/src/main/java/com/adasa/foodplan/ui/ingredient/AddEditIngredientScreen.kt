package com.adasa.foodplan.ui.ingredient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.IngredientCategory
import com.adasa.foodplan.domain.model.IngredientSource
import com.adasa.foodplan.ui.recipe.StepUi

private val FatColor = Color(0xFFBA7517)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditIngredientScreen(
    ingredientId: String?,
    onSaved: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AddEditIngredientViewModel = hiltViewModel()
) {
    LaunchedEffect(ingredientId) { viewModel.loadIngredient(ingredientId) }

    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val name     by viewModel.name.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val source   by viewModel.source.collectAsStateWithLifecycle()
    val kcal     by viewModel.kcal.collectAsStateWithLifecycle()
    val protein  by viewModel.protein.collectAsStateWithLifecycle()
    val fat      by viewModel.fat.collectAsStateWithLifecycle()
    val carbs    by viewModel.carbs.collectAsStateWithLifecycle()
    val steps    by viewModel.steps.collectAsStateWithLifecycle()

    var showAddTimerDialog  by remember { mutableStateOf(false) }
    var editingTimerIndex   by remember { mutableStateOf<Int?>(null) }

    // Drag state
    val lazyListState = rememberLazyListState()
    var draggingId    by remember { mutableStateOf<String?>(null) }
    var draggingFrom  by remember { mutableIntStateOf(-1) }
    var draggingDelta by remember { mutableStateOf(0f) }
    val getDelta: () -> Float = { draggingDelta }

    LaunchedEffect(uiState) {
        if (uiState is AddEditIngredientUiState.Saved) onSaved((uiState as AddEditIngredientUiState.Saved).ingredientId)
    }

    if (showAddTimerDialog) {
        TimerEditDialog(
            step      = StepUi.TimerStep(id = "", label = "", totalSeconds = 0),
            onSave    = { label, secs -> viewModel.addTimer(label, secs); showAddTimerDialog = false },
            onDismiss = { showAddTimerDialog = false }
        )
    }
    editingTimerIndex?.let { idx ->
        (steps.getOrNull(idx) as? StepUi.TimerStep)?.let { existing ->
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
                title = { Text(if (ingredientId == null) "New ingredient" else "Edit ingredient") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::saveIngredient, enabled = uiState !is AddEditIngredientUiState.Saving) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick  = viewModel::saveIngredient,
                    enabled  = uiState !is AddEditIngredientUiState.Saving,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    shape    = RoundedCornerShape(50)
                ) {
                    if (uiState is AddEditIngredientUiState.Saving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save ingredient", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state          = lazyListState,
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Name ──────────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = name, onValueChange = viewModel::onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp),
                    singleLine = true
                )
            }

            // ── Category ──────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    IngSectionLabel("Category")
                    Spacer(Modifier.height(6.dp))
                    // 2-column grid of chips
                    val cats = IngredientCategory.entries
                    cats.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick  = { viewModel.onCategoryChange(cat) },
                                    label    = { Text("${cat.emoji} ${cat.displayName}", style = MaterialTheme.typography.labelSmall) },
                                    shape    = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Pad last row if odd number
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // ── Source ────────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    IngSectionLabel("Source")
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IngredientSource.entries.forEach { s ->
                            FilterChip(
                                selected = source == s,
                                onClick  = { viewModel.onSourceChange(s) },
                                label    = { Text(s.displayName, style = MaterialTheme.typography.labelSmall) },
                                shape    = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // ── Nutrition ─────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    IngSectionLabel("Nutrition per 100 g")
                    Spacer(Modifier.height(6.dp))
                    Card(
                        shape  = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                NutritionField("Calories (kcal)", kcal, viewModel::onKcalChange, Modifier.weight(1f))
                                NutritionField("Protein (g)",     protein, viewModel::onProteinChange, Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                NutritionField("Fat (g)",   fat,   viewModel::onFatChange,   Modifier.weight(1f))
                                NutritionField("Carbs (g)", carbs, viewModel::onCarbsChange, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // ── Instructions label ────────────────────────────────────────────
            stickyHeader(key = "instructions_header") {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                    IngSectionLabel("Instructions (optional)", Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }

            // ── Step rows ─────────────────────────────────────────────────────
            itemsIndexed(steps, key = { _, step -> step.id }) { index, step ->
                val isDragging = step.id == draggingId
                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            if (isDragging) { translationY = draggingDelta; shadowElevation = 8f; alpha = 0.92f }
                        }
                ) {
                    val handle = ingDragHandleModifier(
                        stepId      = step.id,
                        stepIndex   = index,
                        steps       = steps,
                        listState   = lazyListState,
                        getDelta    = getDelta,
                        onDragStart = { fromIdx -> draggingId = step.id; draggingFrom = fromIdx; draggingDelta = 0f },
                        onDelta     = { dy -> draggingDelta += dy },
                        onDragEnd   = { from, to -> if (from != to) viewModel.reorderSteps(from, to); draggingId = null; draggingFrom = -1; draggingDelta = 0f }
                    )
                    when (step) {
                        is StepUi.TextStep ->
                            IngTextStepRow(
                                step        = step,
                                stepNumber  = textStepNumbers[index] ?: (index + 1),
                                onTextChange = { viewModel.updateStep(index, it) },
                                onRemove    = { viewModel.removeStep(index) },
                                dragHandle  = handle
                            )
                        is StepUi.TimerStep ->
                            IngTimerStepRow(
                                step       = step,
                                onEdit     = { editingTimerIndex = index },
                                onRemove   = { viewModel.removeStep(index) },
                                dragHandle = handle
                            )
                    }
                }
            }

            // ── Add step / timer buttons ──────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AddTileButton("+ Step", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { viewModel.addStep() }
                    AddTileButton("⏱ Timer", FatColor, Modifier.weight(1f)) { showAddTimerDialog = true }
                }
            }
        }
    }
}

// ── Drag handle ───────────────────────────────────────────────────────────────

@Composable
private fun ingDragHandleModifier(
    stepId:      String,
    stepIndex:   Int,
    steps:       List<StepUi>,
    listState:   LazyListState,
    getDelta:    () -> Float,
    onDragStart: (Int) -> Unit,
    onDelta:     (Float) -> Unit,
    onDragEnd:   (Int, Int) -> Unit,
): Modifier {
    val currentSteps by rememberUpdatedState(steps)
    val currentIndex by rememberUpdatedState(stepIndex)
    val stepIdSet    by rememberUpdatedState(steps.map { it.id }.toHashSet())

    return Modifier.pointerInput(stepId) {
        detectDragGestures(
            onDragStart = { onDragStart(currentIndex) },
            onDrag      = { change, amount -> change.consume(); onDelta(amount.y) },
            onDragEnd   = {
                val delta        = getDelta()
                val info         = listState.layoutInfo.visibleItemsInfo
                val me           = info.firstOrNull { it.key == stepId }
                val toIndex      = if (me != null) {
                    val vc   = me.offset + me.size / 2 + delta.toInt()
                    val near = info.filter { it.key in stepIdSet }
                        .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - vc) }
                    currentSteps.indexOfFirst { it.id == near?.key }.takeIf { it >= 0 } ?: currentIndex
                } else currentIndex
                onDragEnd(currentIndex, toIndex)
            },
            onDragCancel = { onDragEnd(currentIndex, currentIndex) }
        )
    }
}

// ── Step rows ─────────────────────────────────────────────────────────────────

@Composable
private fun IngTextStepRow(
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
        Text("≡", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = dragHandle)
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

@Composable
private fun IngTimerStepRow(
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
                "%02d:%02d".format(step.totalSeconds / 60, step.totalSeconds % 60),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FatColor
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

// ── Timer dialog ──────────────────────────────────────────────────────────────

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

// ── Helpers ───────────────────────────────────────────────────────────────────

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

@Composable
private fun IngSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp,
        modifier = modifier
    )
}

@Composable
private fun NutritionField(label: String, value: Double, onChange: (Double) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(if (value == 0.0) "" else value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { input -> text = input; input.toDoubleOrNull()?.let { onChange(it) } },
        label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}