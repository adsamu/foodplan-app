package com.adasa.foodplan.ui.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.ShoppingCategory
import com.adasa.foodplan.domain.model.ShoppingItem
import com.adasa.foodplan.domain.model.ShoppingList
import com.adasa.foodplan.domain.model.ShoppingUnit
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = hiltViewModel()) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingRegen by viewModel.pendingRegeneration.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping") },
                actions = {
                    IconButton(onClick = { /* share / export */ }) {
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

                is ShoppingUiState.Empty ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No meals planned", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Add meals to your plan to generate a shopping list",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                is ShoppingUiState.Success ->
                    ShoppingListContent(
                        state              = state,
                        onToggleItem       = { viewModel.toggleItem(it) },
                        onExpandItem       = { viewModel.setExpandedItem(it) },
                        onCommitExpression = { id, expr -> viewModel.commitExpression(id, expr) },
                        onPeriodChange     = { start, end -> viewModel.requestPeriodChange(start, end) }
                    )

                is ShoppingUiState.Error ->
                    Text(
                        state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color    = MaterialTheme.colorScheme.error
                    )
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
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(
                                    conflict.name,
                                    style      = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatAdjustmentDelta(conflict.delta, conflict.unit),
                                        style          = MaterialTheme.typography.labelSmall,
                                        color          = MaterialTheme.colorScheme.onSurfaceVariant,
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
                Button(onClick = { viewModel.confirmKeepAdjustments() }) {
                    Text("Keep adjustments")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.confirmDiscardAdjustments() }) {
                    Text("Use new amounts")
                }
            }
        )
    }
}

// ── List content ──────────────────────────────────────────────────────────────

@Composable
private fun ShoppingListContent(
    state:              ShoppingUiState.Success,
    onToggleItem:       (String) -> Unit,
    onExpandItem:       (String?) -> Unit,
    onCommitExpression: (String, String) -> Double?,
    onPeriodChange:     (LocalDate, LocalDate) -> Unit,
) {
    val checkedCount = state.checkedItems.size
    val totalCount   = state.shoppingList.totalItems

    LazyColumn(
        contentPadding     = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        item {
            PeriodCard(shoppingList = state.shoppingList, onPeriodChange = onPeriodChange)
            Spacer(Modifier.height(8.dp))
        }

        item {
            Text(
                "$checkedCount of $totalCount checked",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        state.shoppingList.categories.forEach { category ->
            item(key = "header_${category.name}") { CategoryHeader(category) }
            items(category.items, key = { it.ingredientId }) { item ->
                val adjustment = state.adjustments[item.ingredientId]
                val isExpanded = state.expandedItemId == item.ingredientId
                val isChecked  = item.ingredientId in state.checkedItems
                ShoppingItemRow(
                    item               = item,
                    adjustment         = adjustment,
                    isChecked          = isChecked,
                    isExpanded         = isExpanded,
                    onToggle           = { onToggleItem(item.ingredientId) },
                    onToggleExpand     = { onExpandItem(if (isExpanded) null else item.ingredientId) },
                    onCommitExpression = { expr -> onCommitExpression(item.ingredientId, expr) }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Period card with inline calendar ─────────────────────────────────────────

@Composable
private fun PeriodCard(
    shoppingList:   ShoppingList,
    onPeriodChange: (LocalDate, LocalDate) -> Unit,
) {
    var calendarOpen  by remember { mutableStateOf(false) }
    var selectedStart by remember(shoppingList.period.startDate) {
        mutableStateOf(shoppingList.period.startDate)
    }
    var selectedEnd   by remember(shoppingList.period.endDate) {
        mutableStateOf(shoppingList.period.endDate)
    }

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header — only the text+chevron area toggles the calendar;
            // the refresh button sits outside that clickable zone
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Clickable area: dates + chevron
                Row(
                    modifier          = Modifier.weight(1f).clickable { calendarOpen = !calendarOpen },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        if (shoppingList.period.recipeNames.isNotEmpty()) {
                            Text(
                                shoppingList.period.recipeNames.joinToString(" · "),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                                maxLines = 1
                            )
                        }
                    }
                    Icon(
                        if (calendarOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Refresh — separate from the clickable row so it never conflicts
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onPeriodChange(selectedStart, selectedEnd) }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        tint     = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = calendarOpen, enter = expandVertically(), exit = shrinkVertically()) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ShoppingCalendar(
                        selectedStart   = selectedStart,
                        selectedEnd     = selectedEnd,
                        onRangeSelected = { s, e -> selectedStart = s; selectedEnd = e },
                        onDone          = {
                            calendarOpen = false
                            onPeriodChange(selectedStart, selectedEnd)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingCalendar(
    selectedStart:   LocalDate,
    selectedEnd:     LocalDate,
    onRangeSelected: (LocalDate, LocalDate) -> Unit,
    onDone:          () -> Unit,
) {
    var displayYear  by remember { mutableStateOf(selectedStart.year) }
    var displayMonth by remember { mutableStateOf(selectedStart.monthNumber) }

    var localStart by remember { mutableStateOf(selectedStart) }
    var localEnd   by remember { mutableStateOf(selectedEnd) }

    val cells: List<LocalDate?> = remember(displayYear, displayMonth) {
        buildCalendarCells(displayYear, displayMonth)
    }
    val rows: List<List<LocalDate?>> = remember(cells) { cells.chunked(7) }

    var gridSize by remember { mutableStateOf(IntSize.Zero) }

    val monthName = remember(displayMonth) {
        kotlinx.datetime.Month(displayMonth).name
            .lowercase().replaceFirstChar { it.uppercase() }
    }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {

        // Month navigation
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                if (displayMonth == 1) { displayMonth = 12; displayYear-- }
                else displayMonth--
            }) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Previous month",
                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                "$monthName $displayYear",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            IconButton(onClick = {
                if (displayMonth == 12) { displayMonth = 1; displayYear++ }
                else displayMonth++
            }) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Next month",
                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Day-of-week header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                Text(
                    d,
                    modifier  = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // Calendar grid — tap or drag to set range
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { gridSize = it }
                .pointerInput(cells) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        val cw = if (gridSize.width > 0) gridSize.width.toFloat() / 7f else return@awaitEachGesture
                        val ch = if (gridSize.height > 0 && rows.isNotEmpty())
                            gridSize.height.toFloat() / rows.size.toFloat() else return@awaitEachGesture

                        fun dateAt(x: Float, y: Float): LocalDate? {
                            val col = (x / cw).toInt().coerceIn(0, 6)
                            val row = (y / ch).toInt().coerceIn(0, rows.size - 1)
                            return rows.getOrNull(row)?.getOrNull(col)
                        }

                        val touchedDate = dateAt(down.position.x, down.position.y)
                            ?: return@awaitEachGesture

                        // Nearest knob: compare epoch days
                        val startDist = kotlin.math.abs(touchedDate.toEpochDays() - localStart.toEpochDays())
                        val endDist   = kotlin.math.abs(touchedDate.toEpochDays() - localEnd.toEpochDays())
                        var activeKnob = if (startDist <= endDist) "start" else "end"

                        var dragging = false

                        fun applyDate(date: LocalDate) {
                            when (activeKnob) {
                                "start" ->
                                    if (date.compareTo(localEnd) <= 0) localStart = date
                                    else { localStart = localEnd; localEnd = date; activeKnob = "end" }
                                "end"   ->
                                    if (date.compareTo(localStart) >= 0) localEnd = date
                                    else { localEnd = localStart; localStart = date; activeKnob = "start" }
                            }
                            onRangeSelected(localStart, localEnd)
                        }

                        while (true) {
                            val event  = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                // Tap — move nearest knob to tapped cell
                                if (!dragging) applyDate(touchedDate)
                                break
                            }

                            val dist = (change.position - down.position).getDistance()
                            if (!dragging && dist > viewConfiguration.touchSlop) dragging = true

                            if (dragging) {
                                dateAt(change.position.x, change.position.y)?.let { applyDate(it) }
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
                            val inRange = date != null &&
                                    date.compareTo(localStart) >= 0 &&
                                    date.compareTo(localEnd) <= 0
                            val isStart = date != null && date.compareTo(localStart) == 0
                            val isEnd   = date != null && date.compareTo(localEnd)   == 0

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .then(
                                        if (inRange && !isStart && !isEnd)
                                            Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        else Modifier
                                    )
                                    .clip(RoundedCornerShape(50))
                                    .then(
                                        if (isStart || isEnd)
                                            Modifier.background(MaterialTheme.colorScheme.primary)
                                        else Modifier
                                    )
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
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)) {
            Text("Done")
        }
    }
}

private fun buildCalendarCells(year: Int, month: Int): List<LocalDate?> {
    val firstDow    = LocalDate(year, month, 1).dayOfWeek.isoDayNumber  // 1=Mon
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

// ── Shopping item row with inline expression editor ───────────────────────────

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

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
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
                Checkbox(
                    checked         = isChecked,
                    onCheckedChange = { onToggle() },
                    modifier        = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style          = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                        color          = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (item.usedInRecipes.isNotEmpty()) {
                        Text(
                            item.usedInRecipes.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun ExpressionEditor(
    item:          ShoppingItem,
    currentAmount: Double,
    onCommit:      (String) -> Double?,
    onDismiss:     () -> Unit,
) {
    val keyboard       = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val seedText       = formatAmountRaw(currentAmount, item.unit)

    var fieldValue by remember(item.ingredientId) {
        mutableStateOf(TextFieldValue(seedText, selection = TextRange(seedText.length)))
    }
    var errorState       by remember { mutableStateOf(false) }
    var evaluatedPreview by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(fieldValue.text) {
        evaluatedPreview = evaluateExpression(fieldValue.text)
        errorState       = false
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            "Adjust amount · calculated: ${formatAmount(item.totalGrams, item.unit)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BasicTextField(
                        value         = fieldValue,
                        onValueChange = { fieldValue = it; errorState = false },
                        textStyle     = TextStyle(
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color      = if (errorState) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush     = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val result = onCommit(fieldValue.text)
                                if (result == null) errorState = true
                                else { keyboard?.hide(); onDismiss() }
                            }
                        ),
                        modifier = Modifier.weight(1f).focusRequester(focusRequester)
                    )

                    // Live preview only shown when the expression contains an operator
                    val preview = evaluatedPreview
                    if (preview != null && fieldValue.text.any { it == '+' || it == '-' || it == '*' || it == '/' }) {
                        Text(
                            "= ${formatAmount(preview, item.unit)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            FilledTonalButton(
                onClick        = {
                    val result = onCommit(fieldValue.text)
                    if (result == null) errorState = true
                    else { keyboard?.hide(); onDismiss() }
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) { Text("Set") }
        }

        if (errorState) {
            Text(
                "Invalid expression",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (currentAmount != item.totalGrams) {
            TextButton(
                onClick        = { onCommit(formatAmountRaw(item.totalGrams, item.unit)) },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp)
            ) {
                Text(
                    "Reset to ${formatAmount(item.totalGrams, item.unit)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// ── Supporting composables ────────────────────────────────────────────────────

@Composable
private fun CategoryHeader(category: ShoppingCategory) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
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

internal fun formatAmountRaw(grams: Double, unit: ShoppingUnit): String = when (unit) {
    ShoppingUnit.PIECES     -> "${grams.toInt()}"
    ShoppingUnit.DECILITERS -> "%.1f".format(grams)
    ShoppingUnit.GRAMS      ->
        if (grams >= 1000) "%.3f".format(grams / 1000.0).trimEnd('0').trimEnd('.')
        else "${grams.toInt()}"
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