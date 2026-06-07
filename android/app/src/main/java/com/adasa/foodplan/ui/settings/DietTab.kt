package com.adasa.foodplan.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.*
import com.adasa.foodplan.ui.recipe.IngredientResultItem
import com.adasa.foodplan.ui.recipe.IngredientSearchViewModel

private enum class IngredientListType { EXCLUDED, PREFERRED, DISLIKED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietTab(config: MealPlanConfig?, viewModel: SettingsViewModel) {
    val diet = config?.diet

    var openPicker by remember { mutableStateOf<IngredientListType?>(null) }
    val searchVM: IngredientSearchViewModel = hiltViewModel()
    val searchResults by searchVM.ingredientResults.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Diet type ─────────────────────────────────────────────────────
        SettingsSection("Diet type", "Recipes must match all selected types")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    DietType.entries.forEach { type ->
                        FilterChip(
                            selected = type in (diet?.dietTypes ?: emptySet()),
                            onClick  = { viewModel.toggleDietType(type) },
                            label    = { Text(type.displayName) }
                        )
                    }
                }
            }
        }

        // ── Allergies ─────────────────────────────────────────────────────
        SettingsSection("Allergies & intolerances", "Recipes with these ingredients are never scheduled")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    AllergyType.entries.forEach { allergy ->
                        FilterChip(
                            selected = allergy in (diet?.allergies ?: emptySet()),
                            onClick  = { viewModel.toggleAllergy(allergy) },
                            label    = { Text(allergy.displayName) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor     = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Box(modifier = Modifier.clickable { openPicker = IngredientListType.EXCLUDED }) {
                    SettingsRow(
                        icon     = "🚫",
                        title    = "Excluded ingredients",
                        subtitle = "Block specific items from the plan",
                        badge    = diet?.excludedIngredientIds?.size?.toString()?.takeIf { it != "0" },
                        onClick  = { openPicker = IngredientListType.EXCLUDED }
                    )
                }
            }
        }

        // ── Preferences ───────────────────────────────────────────────────
        SettingsSection("Preferences", "Soft — optimizer avoids but does not exclude")
        SettingsCard {
            Column {
                Box(modifier = Modifier.clickable { openPicker = IngredientListType.PREFERRED }) {
                    SettingsRow(
                        icon     = "❤️",
                        title    = "Preferred ingredients",
                        subtitle = "Prioritise in recipe selection",
                        badge    = diet?.preferredIngredientIds?.size?.toString()?.takeIf { it != "0" },
                        onClick  = { openPicker = IngredientListType.PREFERRED }
                    )
                }
                HorizontalDivider()
                Box(modifier = Modifier.clickable { openPicker = IngredientListType.DISLIKED }) {
                    SettingsRow(
                        icon     = "👎",
                        title    = "Disliked ingredients",
                        subtitle = "Avoid but don't fully exclude",
                        badge    = diet?.dislikedIngredientIds?.size?.toString()?.takeIf { it != "0" },
                        onClick  = { openPicker = IngredientListType.DISLIKED }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    // ── Ingredient picker sheet ───────────────────────────────────────────
    openPicker?.let { listType ->
        val (title, subtitle, selectedIds, onToggle) = when (listType) {
            IngredientListType.EXCLUDED  -> PickerConfig(
                title      = "Excluded ingredients",
                subtitle   = "These ingredients will never appear in your plan",
                selectedIds = diet?.excludedIngredientIds ?: emptySet(),
                onToggle   = { viewModel.toggleExcludedIngredient(it) }
            )
            IngredientListType.PREFERRED -> PickerConfig(
                title      = "Preferred ingredients",
                subtitle   = "The optimizer will favour recipes with these",
                selectedIds = diet?.preferredIngredientIds ?: emptySet(),
                onToggle   = { viewModel.togglePreferredIngredient(it) }
            )
            IngredientListType.DISLIKED  -> PickerConfig(
                title      = "Disliked ingredients",
                subtitle   = "The optimizer will avoid these where possible",
                selectedIds = diet?.dislikedIngredientIds ?: emptySet(),
                onToggle   = { viewModel.toggleDislikedIngredient(it) }
            )
        }

        IngredientPickerSheet(
            title       = title,
            subtitle    = subtitle,
            selectedIds = selectedIds,
            results     = searchResults,
            onQueryChange = { searchVM.onQueryChange(it) },
            onToggle    = onToggle,
            onDismiss   = {
                searchVM.onQueryChange("")
                openPicker = null
            }
        )
    }
}

// ── Picker sheet ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientPickerSheet(
    title:         String,
    subtitle:      String,
    selectedIds:   Set<String>,
    results:       List<IngredientResultItem>,
    onQueryChange: (String) -> Unit,
    onToggle:      (String) -> Unit,
    onDismiss:     () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query      by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            title,
            style     = MaterialTheme.typography.titleMedium,
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            subtitle,
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            textAlign = TextAlign.Center
        )

        // Search bar
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
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                BasicTextField(
                    value         = query,
                    onValueChange = { query = it; onQueryChange(it) },
                    textStyle     = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                "Search ingredients…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        inner()
                    }
                )
            }
        }

        // Selected count chip
        if (selectedIds.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label   = { Text("${selectedIds.size} selected") }
                )
            }
        }

        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
            items(results, key = { it.id }) { item ->
                val isSelected = item.id in selectedIds
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(item.id) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.name,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(
                            item.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (item != results.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private data class PickerConfig(
    val title:       String,
    val subtitle:    String,
    val selectedIds: Set<String>,
    val onToggle:    (String) -> Unit,
)