package com.adasa.foodplan.ui.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.ShoppingCategory
import com.adasa.foodplan.domain.model.ShoppingItem
import com.adasa.foodplan.domain.model.ShoppingUnit
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = hiltViewModel()) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingRegen     by viewModel.pendingRegeneration.collectAsStateWithLifecycle()
    val selectedRecipes  by viewModel.selectedRecipeIds.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping") },
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share list")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is ShoppingUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                // Fix 3: Empty still shows the period card so user isn't trapped
                is ShoppingUiState.Empty ->
                    LazyColumn(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                        item {
                            PeriodCard(
                                startDate         = state.startDate,
                                endDate           = state.endDate,
                                recipes           = emptyList(),
                                selectedRecipeIds = null,
                                onToggleRecipe    = {},
                                hasExpandedItem   = false,
                                onCloseEditor     = {},
                                onPeriodChange    = { s, e -> viewModel.requestPeriodChange(s, e) }
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                        item {
                            Column(
                                modifier            = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No meals planned for this period", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Try a different date range",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                is ShoppingUiState.Success ->
                    ShoppingListContent(
                        state              = state,
                        selectedRecipeIds  = selectedRecipes,
                        onToggleItem       = { viewModel.toggleItem(it) },
                        onExpandItem       = { viewModel.setExpandedItem(it) },
                        onCommitExpression = { id, expr -> viewModel.commitExpression(id, expr) },
                        onPeriodChange     = { s, e -> viewModel.requestPeriodChange(s, e) },
                        onToggleRecipe     = { viewModel.toggleRecipeFilter(it) }
                    )

                is ShoppingUiState.Error ->
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // ── Keep adjustments? dialog ──────────────────────────────────────────────
    pendingRegen?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissPendingRegeneration() },
            title = { Text("Keep your adjustments?") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "The new period changes these amounts. Your adjustments will be applied on top.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    pending.conflicts.forEach { conflict ->
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(conflict.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatAdjustmentDelta(conflict.delta, conflict.unit),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    Text(
                                        "${formatAmount(conflict.newCalculated, conflict.unit)} → ${formatAmount(conflict.newWithDelta, conflict.unit)}",
                                        style      = MaterialTheme.typography.labelSmall,
                                        color      = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmKeepAdjustments() }) { Text("Keep adjustments") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.confirmDiscardAdjustments() }) { Text("Use new amounts") }
            }
        )
    }
}

// ── List content ──────────────────────────────────────────────────────────────

@Composable
private fun ShoppingListContent(
    state:              ShoppingUiState.Success,
    selectedRecipeIds:  Set<String>?,
    onToggleItem:       (String) -> Unit,
    onExpandItem:       (String?) -> Unit,
    onCommitExpression: (String, String) -> Double?,
    onPeriodChange:     (LocalDate, LocalDate) -> Unit,
    onToggleRecipe:     (String) -> Unit,
) {
    // All items flat, keyed by ingredientId
    val allItems = remember(state.shoppingList) {
        state.shoppingList.categories.flatMap { cat -> cat.items.map { cat to it } }
    }

    // Fix 2: split into unchecked (keep categories) and checked (sorted latest-first)
    val checkedIdSet = state.checkedItems
    val checkedByRecency: List<Pair<ShoppingCategory, ShoppingItem>> = remember(state.checkedOrder, allItems) {
        state.checkedOrder.reversed().mapNotNull { id ->
            allItems.find { it.second.ingredientId == id }
        }
    }

    val uncheckedCount = allItems.count { it.second.ingredientId !in checkedIdSet }
    val totalCount     = allItems.size

    LazyColumn(
        contentPadding     = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        item {
            PeriodCard(
                startDate         = state.shoppingList.period.startDate,
                endDate           = state.shoppingList.period.endDate,
                recipes           = state.shoppingList.period.recipes,
                selectedRecipeIds = selectedRecipeIds,
                onToggleRecipe    = onToggleRecipe,
                hasExpandedItem   = state.expandedItemId != null,
                onCloseEditor     = { onExpandItem(null) },
                onPeriodChange    = onPeriodChange
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            Text(
                "${totalCount - uncheckedCount} of $totalCount checked",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // Unchecked items grouped by category
        state.shoppingList.categories.forEach { category ->
            val catUnchecked = category.items.filter { it.ingredientId !in checkedIdSet }
            if (catUnchecked.isEmpty()) return@forEach

            item(key = "hdr_${category.name}") { CategoryHeader(category) }
            items(catUnchecked, key = { it.ingredientId }) { item ->
                ShoppingItemRow(
                    item               = item,
                    adjustment         = state.adjustments[item.ingredientId],
                    isChecked          = false,
                    isExpanded         = state.expandedItemId == item.ingredientId,
                    onToggle           = { onToggleItem(item.ingredientId) },
                    onToggleExpand     = { onExpandItem(if (state.expandedItemId == item.ingredientId) null else item.ingredientId) },
                    onCommitExpression = { expr -> onCommitExpression(item.ingredientId, expr) }
                )
            }
        }

        // Fix 2: Checked section at the bottom, most recently checked first
        if (checkedByRecency.isNotEmpty()) {
            item(key = "checked_header") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "Checked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }
            items(checkedByRecency, key = { it.second.ingredientId }) { (_, item) ->
                ShoppingItemRow(
                    item               = item,
                    adjustment         = state.adjustments[item.ingredientId],
                    isChecked          = true,
                    isExpanded         = state.expandedItemId == item.ingredientId,
                    onToggle           = { onToggleItem(item.ingredientId) },
                    onToggleExpand     = { onExpandItem(if (state.expandedItemId == item.ingredientId) null else item.ingredientId) },
                    onCommitExpression = { expr -> onCommitExpression(item.ingredientId, expr) }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Period card with inline calendar ─────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodCard(
    startDate:         LocalDate,
    endDate:           LocalDate,
    recipes:           List<com.adasa.foodplan.domain.model.SelectableRecipe>,
    selectedRecipeIds: Set<String>?,   // null = all selected
    onToggleRecipe:    (String) -> Unit,
    hasExpandedItem:   Boolean,
    onCloseEditor:     () -> Unit,
    onPeriodChange:    (LocalDate, LocalDate) -> Unit,
) {
    var expanded      by remember { mutableStateOf(false) }
    var selectedStart by remember(startDate) { mutableStateOf(startDate) }
    var selectedEnd   by remember(endDate)   { mutableStateOf(endDate) }

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header — entire row taps to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Shopping period",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "${formatDate(selectedStart)} — ${formatDate(selectedEnd)}",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (recipes.isNotEmpty() && !expanded) {
                        val filterLabel = if (selectedRecipeIds == null)
                            recipes.joinToString(" · ") { it.name }
                        else
                            "${selectedRecipeIds.size} of ${recipes.size} recipes"
                        Text(
                            filterLabel,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Refresh — always tappable; closes editor first if needed
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            if (hasExpandedItem) onCloseEditor()
                            onPeriodChange(selectedStart, selectedEnd)
                        }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        tint     = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded body — recipe filter chips then calendar
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column {
                    if (recipes.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Filter by recipe",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement   = Arrangement.spacedBy(4.dp)
                            ) {
                                recipes.forEach { recipe ->
                                    val isSelected = selectedRecipeIds == null || recipe.id in selectedRecipeIds
                                    FilterChip(
                                        selected = isSelected,
                                        onClick  = { onToggleRecipe(recipe.id) },
                                        label    = { Text(recipe.name, style = MaterialTheme.typography.labelSmall) },
                                        shape    = RoundedCornerShape(8.dp),
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ShoppingCalendar(
                        selectedStart   = selectedStart,
                        selectedEnd     = selectedEnd,
                        onRangeSelected = { s, e -> selectedStart = s; selectedEnd = e },
                        onDone          = {
                            expanded = false
                            onPeriodChange(selectedStart, selectedEnd)
                        }
                    )
                }
            }
        }
    }
}

// ── Calendar ──────────────────────────────────────────────────────────────────

@Composable
private fun ShoppingCalendar(
    selectedStart:   LocalDate,
    selectedEnd:     LocalDate,
    onRangeSelected: (LocalDate, LocalDate) -> Unit,
    onDone:          () -> Unit,
) {
    var displayYear  by remember { mutableStateOf(selectedStart.year) }
    var displayMonth by remember { mutableStateOf(selectedStart.monthNumber) }
    var localStart   by remember { mutableStateOf(selectedStart) }
    var localEnd     by remember { mutableStateOf(selectedEnd) }
    var pickingStart by remember { mutableStateOf(true) }
    var gridSize     by remember { mutableStateOf(IntSize.Zero) }

    val cells: List<LocalDate?> = remember(displayYear, displayMonth) {
        buildCalendarCells(displayYear, displayMonth)
    }
    val rows: List<List<LocalDate?>> = remember(cells) { cells.chunked(7) }

    val monthName = remember(displayMonth) {
        kotlinx.datetime.Month(displayMonth).name.lowercase().replaceFirstChar { it.uppercase() }
    }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {

        // Month navigation — Fix: left=Up=prev, right=Down=next
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                if (displayMonth == 1) { displayMonth = 12; displayYear-- } else displayMonth--
            }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous month", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
            Text("$monthName $displayYear", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            IconButton(onClick = {
                if (displayMonth == 12) { displayMonth = 1; displayYear++ } else displayMonth++
            }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next month", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
        }

        // Day-of-week header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                Text(d, modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.height(4.dp))

        // Draggable grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { gridSize = it }
                .pointerInput(cells) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        val cw = if (gridSize.width > 0) gridSize.width.toFloat() / 7f else return@awaitEachGesture
                        val ch = if (gridSize.height > 0 && rows.isNotEmpty()) gridSize.height.toFloat() / rows.size else return@awaitEachGesture

                        fun dateAt(x: Float, y: Float): LocalDate? {
                            val col = (x / cw).toInt().coerceIn(0, 6)
                            val row = (y / ch).toInt().coerceIn(0, rows.size - 1)
                            return rows.getOrNull(row)?.getOrNull(col)
                        }

                        val touchedDate = dateAt(down.position.x, down.position.y) ?: return@awaitEachGesture

                        // Nearest knob for drag
                        val startDist = kotlin.math.abs(touchedDate.toEpochDays() - localStart.toEpochDays())
                        val endDist   = kotlin.math.abs(touchedDate.toEpochDays() - localEnd.toEpochDays())
                        var activeKnob = if (startDist <= endDist) "start" else "end"
                        var dragging = false

                        fun applyDrag(date: LocalDate) {
                            when (activeKnob) {
                                "start" ->
                                    if (date.compareTo(localEnd) <= 0) localStart = date
                                    else { localStart = localEnd; localEnd = date; activeKnob = "end" }
                                "end" ->
                                    if (date.compareTo(localStart) >= 0) localEnd = date
                                    else { localEnd = localStart; localStart = date; activeKnob = "start" }
                            }
                            onRangeSelected(localStart, localEnd)
                        }

                        while (true) {
                            val event  = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                if (!dragging) {
                                    // Fix: first tap = start, second tap = end
                                    if (pickingStart) {
                                        localStart   = touchedDate
                                        localEnd     = touchedDate
                                        pickingStart = false
                                        onRangeSelected(localStart, localEnd)
                                    } else {
                                        if (touchedDate.compareTo(localStart) >= 0) localEnd = touchedDate
                                        else { localEnd = localStart; localStart = touchedDate }
                                        pickingStart = true
                                        onRangeSelected(localStart, localEnd)
                                    }
                                }
                                break
                            }

                            val dist = (change.position - down.position).getDistance()
                            if (!dragging && dist > viewConfiguration.touchSlop) dragging = true

                            if (dragging) {
                                dateAt(change.position.x, change.position.y)?.let { applyDrag(it) }
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            Column {
                rows.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            val inRange = date != null && date.compareTo(localStart) >= 0 && date.compareTo(localEnd) <= 0
                            val isStart = date != null && date.compareTo(localStart) == 0
                            val isEnd   = date != null && date.compareTo(localEnd)   == 0

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .then(if (inRange && !isStart && !isEnd) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) else Modifier)
                                    .clip(RoundedCornerShape(50))
                                    .then(if (isStart || isEnd) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier)
                            ) {
                                if (date != null) {
                                    Text(
                                        "${date.dayOfMonth}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when {
                                            isStart || isEnd -> MaterialTheme.colorScheme.onPrimary
                                            inRange          -> MaterialTheme.colorScheme.primary
                                            else             -> MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick  = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(50)
        ) {
            Text("Done")
        }
    }
}

private fun buildCalendarCells(year: Int, month: Int): List<LocalDate?> {
    val firstDow    = LocalDate(year, month, 1).dayOfWeek.isoDayNumber
    val daysInMonth = daysInMonth(year, month)
    return buildList {
        repeat(firstDow - 1) { add(null) }
        repeat(daysInMonth)  { d -> add(LocalDate(year, month, d + 1)) }
        val trailing = (7 - size % 7) % 7
        repeat(trailing)     { add(null) }
    }
}

private fun daysInMonth(year: Int, month: Int) = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11            -> 30
    2                      -> if (isLeapYear(year)) 29 else 28
    else                   -> 31
}

private fun isLeapYear(year: Int) = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

// ── Shopping item row ─────────────────────────────────────────────────────────

@Composable
private fun ShoppingItemRow(
    item:               ShoppingItem,
    adjustment:         ShoppingAdjustment?,
    isChecked:          Boolean,
    isExpanded:         Boolean,
    onToggle:           () -> Unit,
    onToggleExpand:     () -> Unit,
    onCommitExpression: (String) -> Double?,
) {
    val displayAmount = adjustment?.adjustedAmount ?: item.totalGrams
    val isAdjusted    = adjustment != null

    // Fix 2: checked items are slightly transparent
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().alpha(if (isChecked) 0.55f else 1f)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(checked = isChecked, onCheckedChange = { onToggle() }, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style          = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                        color          = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (item.contributions.isNotEmpty()) {
                        // Per-recipe breakdown: "Curry (500g) · Pasta (300g)"
                        Text(
                            item.contributions.joinToString(" · ") { c ->
                                "${c.recipeName} (${formatAmount(c.grams, item.unit)})"
                            },
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    formatAmount(displayAmount, item.unit),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isAdjusted) FontWeight.Medium else FontWeight.Normal,
                    color      = when {
                        isAdjusted -> MaterialTheme.colorScheme.primary
                        isChecked  -> MaterialTheme.colorScheme.onSurfaceVariant
                        else       -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                ExpressionEditor(
                    item          = item,
                    currentAmount = displayAmount,
                    onCommit      = onCommitExpression,
                    onDismiss     = onToggleExpand
                )
            }
        }
    }
}

// ── Expression editor ─────────────────────────────────────────────────────────

@Composable
private fun ExpressionEditor(
    item:          ShoppingItem,
    currentAmount: Double,
    onCommit:      (String) -> Double?,
    onDismiss:     () -> Unit,
) {
    val keyboard       = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Fix 4: always seed with base unit (grams, not kg) to avoid unit ambiguity
    val seedText = formatAmountRaw(currentAmount, item.unit)
    val unitLabel = when (item.unit) {
        ShoppingUnit.GRAMS      -> "g"
        ShoppingUnit.PIECES     -> "pcs"
        ShoppingUnit.DECILITERS -> "dl"
    }

    var fieldValue by remember(item.ingredientId) {
        mutableStateOf(TextFieldValue(seedText, selection = TextRange(seedText.length)))
    }
    var errorState       by remember { mutableStateOf(false) }
    var evaluatedPreview by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(fieldValue.text) { evaluatedPreview = evaluateExpression(fieldValue.text); errorState = false }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        // Show calculated in base units so it matches what the user types
        Text(
            "Adjust amount · calculated: ${formatAmountRaw(item.totalGrams, item.unit)} $unitLabel",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicTextField(
                        value         = fieldValue,
                        onValueChange = { fieldValue = it; errorState = false },
                        textStyle     = TextStyle(
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color      = if (errorState) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush     = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            val result = onCommit(fieldValue.text)
                            if (result == null) errorState = true else { keyboard?.hide(); onDismiss() }
                        }),
                        modifier = Modifier.weight(1f).focusRequester(focusRequester)
                    )
                    // Unit label
                    Text(unitLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // Live preview for expressions
                    val preview = evaluatedPreview
                    if (preview != null && fieldValue.text.any { it == '+' || it == '-' || it == '*' || it == '/' }) {
                        Text("= ${preview.toInt()} $unitLabel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            FilledTonalButton(
                onClick        = {
                    val result = onCommit(fieldValue.text)
                    if (result == null) errorState = true else { keyboard?.hide(); onDismiss() }
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) { Text("Set") }
        }

        if (errorState) {
            Text("Invalid expression", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }

        if (currentAmount != item.totalGrams) {
            TextButton(
                onClick        = { onCommit(formatAmountRaw(item.totalGrams, item.unit)) },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp)
            ) {
                Text("Reset to ${formatAmountRaw(item.totalGrams, item.unit)} $unitLabel", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// ── Supporting composables ────────────────────────────────────────────────────

@Composable
private fun CategoryHeader(category: ShoppingCategory) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(category.emoji, style = MaterialTheme.typography.bodyMedium)
        Text(category.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── Formatting helpers ────────────────────────────────────────────────────────

internal fun formatAmount(grams: Double, unit: ShoppingUnit): String = when (unit) {
    ShoppingUnit.PIECES     -> "${grams.toInt()} pcs"
    ShoppingUnit.DECILITERS -> "${"%.1f".format(grams)} dl"
    ShoppingUnit.GRAMS      ->
        if (grams >= 1000) {
            val kg = grams / 1000.0
            if (kg == kotlin.math.floor(kg)) "${kg.toInt()} kg" else "${"%.1f".format(kg)} kg"
        } else "${grams.toInt()} g"
}

// Fix 4: always base units — never converts to kg so the expression editor is unambiguous
internal fun formatAmountRaw(grams: Double, unit: ShoppingUnit): String = when (unit) {
    ShoppingUnit.PIECES     -> "${grams.toInt()}"
    ShoppingUnit.DECILITERS -> "%.1f".format(grams)
    ShoppingUnit.GRAMS      -> "${grams.toInt()}"
}

private fun formatAdjustmentDelta(delta: Double, unit: ShoppingUnit): String {
    val prefix = if (delta >= 0) "+" else ""
    return "$prefix${formatAmount(kotlin.math.abs(delta), unit)} applied"
}

private fun formatDate(date: LocalDate): String {
    val dow = when (date.dayOfWeek.isoDayNumber) {
        1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"
        5 -> "Fri"; 6 -> "Sat"; else -> "Sun"
    }
    val mon = date.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }
    return "$dow ${date.dayOfMonth} $mon"
}