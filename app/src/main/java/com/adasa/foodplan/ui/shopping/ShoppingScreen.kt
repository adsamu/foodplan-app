package com.adasa.foodplan.ui.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.ShoppingCategory
import com.adasa.foodplan.domain.model.ShoppingItem
import com.adasa.foodplan.domain.model.ShoppingList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping") },
                actions = {
                    IconButton(onClick = { /* share / export — later */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share list")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ShoppingUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ShoppingUiState.Empty -> {
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
                }
                is ShoppingUiState.Success -> {
                    ShoppingListContent(
                        shoppingList = state.shoppingList,
                        checkedItems = state.checkedItems,
                        onToggleItem = { viewModel.toggleItem(it) }
                    )
                }
                is ShoppingUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingListContent(
    shoppingList: ShoppingList,
    checkedItems: Set<String>,
    onToggleItem: (String) -> Unit
) {
    val checkedCount = checkedItems.size
    val totalCount = shoppingList.totalItems

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Period header card
        item {
            PeriodHeaderCard(shoppingList)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Progress summary
        item {
            Text(
                text = "$checkedCount of $totalCount checked",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            )
        }

        // Categories and items
        shoppingList.categories.forEach { category ->
            item(key = "header_${category.name}") {
                CategoryHeader(category)
            }
            items(
                items = category.items,
                key = { it.ingredientId }
            ) { item ->
                ShoppingItemRow(
                    item = item,
                    isChecked = item.ingredientId in checkedItems,
                    onToggle = { onToggleItem(item.ingredientId) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun PeriodHeaderCard(shoppingList: ShoppingList) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${shoppingList.period.startDate} — ${shoppingList.period.endDate}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = shoppingList.period.recipeNames.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun CategoryHeader(category: ShoppingCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = category.emoji, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                    color = if (isChecked)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (item.usedInRecipes.isNotEmpty()) {
                    Text(
                        text = item.usedInRecipes.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = formatAmount(item.totalGrams),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isChecked)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatAmount(grams: Double): String {
    return if (grams >= 1000) {
        val kg = grams / 1000.0
        if (kg == kotlin.math.floor(kg)) "${kg.toInt()} kg"
        else "${"%.1f".format(kg)} kg"
    } else {
        "${grams.toInt()} g"
    }
}