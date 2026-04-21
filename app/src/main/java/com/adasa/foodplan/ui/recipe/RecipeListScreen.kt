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
import com.adasa.foodplan.domain.model.Recipe
import com.adasa.foodplan.domain.model.RecipeType

private val ChipActiveBackground = Color(0xFFE8DEF8)
private val ChipActiveText = Color(0xFF21005D)
private val CardBackground = Color(0xFFF3EDF7)
private val MealBadgeBackground = Color(0xFFEADDFF)
private val MealBadgeText = Color(0xFF21005D)
private val ComponentBadgeBackground = Color(0xFFFFD8E4)
private val ComponentBadgeText = Color(0xFF31111D)
private val SearchBarBackground = Color(0xFFECE6F0)
private val SectionLabelColor = Color(0xFF79747E)
private val IconBoxBackground = Color(0xFFEADDFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    onAddIngredientClick: () -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()

    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recipes", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            ExpandableFab(
                expanded = fabExpanded,
                onToggle = { fabExpanded = !fabExpanded },
                onAddRecipe = { fabExpanded = false; onAddRecipeClick() },
                onAddIngredient = { fabExpanded = false; onAddIngredientClick() }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search recipes") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SearchBarBackground,
                    focusedContainerColor = SearchBarBackground,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )
            // Filter chips
            val filters = listOf(
                RecipeFilter.ALL to "All",
                RecipeFilter.MEALS to "Meals",
                RecipeFilter.COMPONENTS to "Components",
                RecipeFilter.BREAKFAST to "Breakfast",
                RecipeFilter.LUNCH to "Lunch",
                RecipeFilter.DINNER to "Dinner",
                RecipeFilter.SNACK to "Snack"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { (filter, label) ->
                    val active = activeFilter == filter
                    FilterChip(
                        selected = active,
                        onClick = { viewModel.onFilterChange(filter) },
                        label = {
                            Text(
                                label,
                                color = if (active) ChipActiveText else Color.Unspecified,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChipActiveBackground
                        )
                    )
                }
            }
            // Content
            when (val state = uiState) {
                is RecipeListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RecipeListUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No recipes found", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                is RecipeListUiState.Success -> {
                    RecipeListContent(
                        meals = state.meals,
                        components = state.components,
                        activeFilter = activeFilter,
                        onRecipeClick = onRecipeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeListContent(
    meals: List<Recipe>,
    components: List<Recipe>,
    activeFilter: RecipeFilter,
    onRecipeClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val showSections = activeFilter == RecipeFilter.ALL
        if (showSections && meals.isNotEmpty()) {
            item { SectionHeader("Meals") }
        }
        if (meals.isNotEmpty()) {
            items(meals) { recipe ->
                RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
            }
        }
        if (showSections && components.isNotEmpty()) {
            item { SectionHeader("Components") }
        }
        if (components.isNotEmpty()) {
            items(components) { recipe ->
                RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = SectionLabelColor,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(IconBoxBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (recipe.type == RecipeType.COMPONENT)
                        Icons.Default.Blender else Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MealBadgeText,
                    modifier = Modifier.size(22.dp)
                )
            }
            // Name + badges
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (recipe.mealCategories.isNotEmpty() || recipe.type == RecipeType.COMPONENT) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (recipe.type == RecipeType.COMPONENT) {
                            RecipeBadge(
                                label = recipe.componentCategory?.displayName ?: "Component",
                                background = ComponentBadgeBackground,
                                textColor = ComponentBadgeText
                            )
                        }
                        recipe.mealCategories.forEach { cat ->
                            RecipeBadge(
                                label = cat.displayName,
                                background = MealBadgeBackground,
                                textColor = MealBadgeText
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
    Surface(
        shape = RoundedCornerShape(50),
        color = background
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ExpandableFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddRecipe: () -> Unit,
    onAddIngredient: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = expanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFabOption(
                    label = "New Ingredient",
                    icon = { Icon(Icons.Default.Kitchen, contentDescription = null) },
                    onClick = onAddIngredient
                )
                SmallFabOption(
                    label = "New Recipe",
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    onClick = onAddRecipe
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        FloatingActionButton(
            onClick = onToggle,
            shape = RoundedCornerShape(14.dp),
            containerColor = IconBoxBackground,
            contentColor = ChipActiveText
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Add",
                modifier = Modifier.graphicsLayer {
                    rotationZ = if (expanded) 45f else 0f
                }
            )
        }
    }
}

@Composable
private fun SmallFabOption(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        SmallFloatingActionButton(onClick = onClick) {
            icon()
        }
    }
}
