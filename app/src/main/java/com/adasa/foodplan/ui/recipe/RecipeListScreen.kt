package com.adasa.foodplan.ui.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.Ingredient
import com.adasa.foodplan.domain.model.IngredientCategory
import com.adasa.foodplan.domain.model.MealCategory
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onRecipeClick:       (String) -> Unit,
    onIngredientClick:   (String) -> Unit,
    onAddRecipeClick:    () -> Unit,
    onAddIngredientClick: () -> Unit,
    viewModel:           RecipeListViewModel = hiltViewModel()
) {
    val uiState         by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery     by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTab       by viewModel.activeTab.collectAsStateWithLifecycle()
    val activeMealCat   by viewModel.activeMealCat.collectAsStateWithLifecycle()
    val activeIngCat    by viewModel.activeIngredientCat.collectAsStateWithLifecycle()
    var fabExpanded     by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recipes", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            ExpandableFab(
                expanded        = fabExpanded,
                onToggle        = { fabExpanded = !fabExpanded },
                onAddRecipe     = { fabExpanded = false; onAddRecipeClick() },
                onAddIngredient = { fabExpanded = false; onAddIngredientClick() }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // Search bar
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder   = { Text("Search recipes or ingredients") },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape         = RoundedCornerShape(50),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Color.Transparent
                ),
                singleLine = true
            )

            // Tab row
            TabRow(selectedTabIndex = activeTab.ordinal) {
                RecipeListTab.entries.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick  = { viewModel.onTabChange(tab) },
                        text     = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Sub-category chips
            when (activeTab) {
                RecipeListTab.MEALS -> {
                    LazyRow(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = activeMealCat == null,
                                onClick  = { viewModel.onMealCatChange(null) },
                                label    = { Text("All") },
                                shape    = RoundedCornerShape(8.dp)
                            )
                        }
                        items(MealCategory.entries) { cat ->
                            FilterChip(
                                selected = activeMealCat == cat,
                                onClick  = { viewModel.onMealCatChange(cat) },
                                label    = { Text(cat.displayName) },
                                shape    = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
                RecipeListTab.INGREDIENTS -> {
                    val categories = (uiState as? RecipeListUiState.Success)?.ingredientCategories ?: emptyList()
                    if (categories.isNotEmpty()) {
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = activeIngCat == null,
                                    onClick  = { viewModel.onIngredientCatChange(null) },
                                    label    = { Text("All") },
                                    shape    = RoundedCornerShape(8.dp)
                                )
                            }
                            items(categories) { cat ->
                                FilterChip(
                                    selected = activeIngCat == cat,
                                    onClick  = { viewModel.onIngredientCatChange(cat) },
                                    label    = { Text("${cat.emoji} ${cat.displayName}") },
                                    shape    = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
                RecipeListTab.COMPONENTS -> { /* no sub-filters */ }
            }

            // Content
            when (val state = uiState) {
                is RecipeListUiState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                is RecipeListUiState.Empty ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nothing found", style = MaterialTheme.typography.bodyLarge)
                    }
                is RecipeListUiState.Success -> {
                    when (activeTab) {
                        RecipeListTab.MEALS ->
                            RecipeTabContent(state.meals, onRecipeClick)
                        RecipeListTab.COMPONENTS ->
                            RecipeTabContent(state.components, onRecipeClick)
                        RecipeListTab.INGREDIENTS ->
                            IngredientTabContent(state.ingredients, onIngredientClick)
                    }
                }
            }
        }
    }
}

// ── Recipe tab ────────────────────────────────────────────────────────────────

@Composable
private fun RecipeTabContent(recipes: List<Recipe>, onRecipeClick: (String) -> Unit) {
    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recipes, key = { it.id }) { recipe ->
            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier              = Modifier.padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (recipe.type == RecipeType.COMPONENT) Icons.Default.Blender else Icons.Default.Restaurant,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                if (recipe.mealCategories.isNotEmpty() || recipe.type == RecipeType.COMPONENT) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (recipe.type == RecipeType.COMPONENT) {
                            RecipeBadge(
                                label     = recipe.componentCategory?.displayName ?: "Component",
                                background = MaterialTheme.colorScheme.tertiaryContainer,
                                textColor  = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        recipe.mealCategories.forEach { cat ->
                            RecipeBadge(
                                label     = cat.displayName,
                                background = MaterialTheme.colorScheme.primaryContainer,
                                textColor  = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeBadge(label: String, background: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(label, color = textColor, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

// ── Ingredient tab ────────────────────────────────────────────────────────────

@Composable
private fun IngredientTabContent(ingredients: List<Ingredient>, onIngredientClick: (String) -> Unit) {
    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ingredients, key = { it.id }) { ingredient ->
            IngredientCard(ingredient = ingredient, onClick = { onIngredientClick(ingredient.id) })
        }
    }
}

@Composable
private fun IngredientCard(ingredient: Ingredient, onClick: () -> Unit) {
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier              = Modifier.padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Kitchen,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ingredient.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                if (ingredient.category != IngredientCategory.OTHER || true) {
                    Text(
                        "${ingredient.category.emoji} ${ingredient.category.displayName}",
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            // Macro summary
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${ingredient.kcalPer100g.toInt()} kcal",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "P ${ingredient.proteinPer100g.toInt()}g · F ${ingredient.fatPer100g.toInt()}g · C ${ingredient.carbsPer100g.toInt()}g",
                    style  = MaterialTheme.typography.labelSmall,
                    color  = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "per 100g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ── FAB ───────────────────────────────────────────────────────────────────────

@Composable
private fun ExpandableFab(
    expanded:        Boolean,
    onToggle:        () -> Unit,
    onAddRecipe:     () -> Unit,
    onAddIngredient: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = expanded,
            enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFabOption("New Ingredient", { Icon(Icons.Default.Kitchen, null) }, onAddIngredient)
                SmallFabOption("New Recipe",     { Icon(Icons.Default.MenuBook, null) }, onAddRecipe)
            }
        }
        Spacer(Modifier.height(12.dp))
        FloatingActionButton(
            onClick         = onToggle,
            shape           = RoundedCornerShape(14.dp),
            containerColor  = MaterialTheme.colorScheme.primaryContainer,
            contentColor    = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector        = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Add",
                modifier           = Modifier.graphicsLayer { rotationZ = if (expanded) 45f else 0f }
            )
        }
    }
}

@Composable
private fun SmallFabOption(label: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 2.dp) {
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
        SmallFloatingActionButton(onClick = onClick) { icon() }
    }
}