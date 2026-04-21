package com.adasa.foodplan.ui.recipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.*

private val PurplePrimary = Color(0xFF6750A4)
private val ChipActiveBackground = Color(0xFFE8DEF8)
private val ChipActiveText = Color(0xFF21005D)
private val TimerBorderColor = Color(0xFFE8DEF8)
private val TimerTextColor = Color(0xFFCAC4D0)
private val IngRowBackground = Color(0xFFF3EDF7)
private val TotalsBackground = Color(0xFFEADDFF)
private val DashedBorderColor = Color(0xFF79747E)
private val SectionLabelColor = Color(0xFF79747E)
private val IconBoxBackground = Color(0xFFEADDFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
    recipeId: String?,
    onSaved: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToAddIngredient: () -> Unit = {},
    viewModel: AddEditRecipeViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.loadRecipe(recipeId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val type by viewModel.type.collectAsStateWithLifecycle()
    val mealCategories by viewModel.mealCategories.collectAsStateWithLifecycle()
    val componentCategory by viewModel.componentCategory.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val nutrition by viewModel.nutrition.collectAsStateWithLifecycle()

    var showIngredientSheet by remember { mutableStateOf(false) }
    var showAddStepOptions by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AddEditUiState.Saved) onSaved()
    }

    if (showIngredientSheet) {
        IngredientSearchSheet(
            onDismiss = { showIngredientSheet = false },
            onIngredientSelect = { id, grams ->
                viewModel.addIngredient(id, grams)
                showIngredientSheet = false
            },
            onRecipeSelect = { id, portions ->
                viewModel.addSubRecipe(id, portions)
                showIngredientSheet = false
            },
            onNavigateToAddIngredient = {
                showIngredientSheet = false
                onNavigateToAddIngredient()
            }
        )
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
                    IconButton(
                        onClick = viewModel::saveRecipe,
                        enabled = uiState !is AddEditUiState.Saving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        singleLine = true
                    )
                }

                item { TypeToggle(type = type, onTypeChange = viewModel::onTypeChange) }

                item {
                    CategorySection(
                        type = type,
                        mealCategories = mealCategories,
                        componentCategory = componentCategory,
                        onMealCategoryToggle = viewModel::onMealCategoryToggle,
                        onComponentCategorySelect = viewModel::onComponentCategorySelect
                    )
                }

                // Ingredients section
                item {
                    FormSectionLabel("Ingredients", Modifier.padding(horizontal = 16.dp))
                }
                itemsIndexed(ingredients) { index, ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        onAmountChange = { viewModel.updateIngredientAmount(index, it) },
                        onRemove = { viewModel.removeIngredient(index) }
                    )
                }
                item {
                    // Dashed border button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(
                                BorderStroke(1.dp, DashedBorderColor),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showIngredientSheet = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+ Add ingredient or recipe",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DashedBorderColor
                        )
                    }
                }

                // Directions section
                item {
                    FormSectionLabel("Directions", Modifier.padding(horizontal = 16.dp))
                }
                // Each step is its own f3edf7 card
                if (steps.isNotEmpty()) {
                    itemsIndexed(steps) { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(IngRowBackground)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedTextField(
                                value = step,
                                onValueChange = { viewModel.updateStep(index, it) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Step ${index + 1}") },
                                minLines = 1,
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            IconButton(onClick = { viewModel.removeStep(index) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove step", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                item {
                    if (!showAddStepOptions) {
                        // Dashed border "Add step" button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .border(BorderStroke(1.dp, DashedBorderColor), shape = RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showAddStepOptions = true }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Add step", style = MaterialTheme.typography.bodyMedium, color = DashedBorderColor)
                        }
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, DashedBorderColor), shape = RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.addStep(); showAddStepOptions = false }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Step", style = MaterialTheme.typography.bodyMedium, color = DashedBorderColor)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, TimerBorderColor), shape = RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Timer — coming soon", style = MaterialTheme.typography.bodyMedium, color = TimerTextColor)
                            }
                        }
                    }
                }
            }

            // Sticky totals + save button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // eaddff totals card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TotalsBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutritionTotal("kcal", nutrition.kcal.toInt().toString())
                    NutritionTotal("protein", "${nutrition.protein.toInt()}g")
                    NutritionTotal("fat", "${nutrition.fat.toInt()}g")
                    NutritionTotal("carbs", "${nutrition.carbs.toInt()}g")
                }
                Button(
                    onClick = viewModel::saveRecipe,
                    enabled = uiState !is AddEditUiState.Saving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                ) {
                    if (uiState is AddEditUiState.Saving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save recipe", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionTotal(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TypeToggle(type: RecipeType, onTypeChange: (RecipeType) -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(RecipeType.MEAL, RecipeType.COMPONENT).forEach { t ->
            val selected = type == t
            OutlinedButton(
                onClick = { onTypeChange(t) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) ChipActiveBackground else Color.Transparent,
                    contentColor = if (selected) ChipActiveText else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, if (selected) ChipActiveText else MaterialTheme.colorScheme.outline)
            ) {
                Text(t.displayName)
            }
        }
    }
}

@Composable
private fun CategorySection(
    type: RecipeType,
    mealCategories: Set<MealCategory>,
    componentCategory: ComponentCategory?,
    onMealCategoryToggle: (MealCategory) -> Unit,
    onComponentCategorySelect: (ComponentCategory) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        if (type == RecipeType.MEAL) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MealCategory.entries.forEach { cat ->
                    val active = cat in mealCategories
                    FilterChip(
                        selected = active,
                        onClick = { onMealCategoryToggle(cat) },
                        shape = RoundedCornerShape(8.dp),
                        label = { Text(cat.displayName, color = if (active) ChipActiveText else Color.Unspecified) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChipActiveBackground
                        )
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                ComponentCategory.entries.chunked(3).forEach { row ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { cat ->
                            val active = componentCategory == cat
                            FilterChip(
                                selected = active,
                                onClick = { onComponentCategorySelect(cat) },
                                shape = RoundedCornerShape(8.dp),
                                label = { Text(cat.displayName, color = if (active) ChipActiveText else Color.Unspecified) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChipActiveBackground
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = SectionLabelColor,
        letterSpacing = 0.8.sp,
        modifier = modifier.then(Modifier.padding(top = 4.dp, bottom = 2.dp))
    )
}

@Composable
private fun IngredientRow(
    ingredient: RecipeIngredientUi,
    onAmountChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    var amountText by remember(ingredient.amount) {
        mutableStateOf(if (ingredient.amount == 0.0) "" else ingredient.amount.let {
            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
        })
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(IngRowBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IconBoxBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (ingredient.subRecipeId != null) Icons.Default.Blender
                              else Icons.Default.Restaurant,
                contentDescription = null,
                tint = ChipActiveText,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            ingredient.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
        // Compact amount + unit inline input
        BasicAmountField(
            value = amountText,
            unit = ingredient.unit,
            onValueChange = { text ->
                amountText = text
                text.toDoubleOrNull()?.let { onAmountChange(it) }
            }
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun BasicAmountField(value: String, unit: String, onValueChange: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(44.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(unit, style = MaterialTheme.typography.labelSmall, color = SectionLabelColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientSearchSheet(
    onDismiss: () -> Unit,
    onIngredientSelect: (String, Double) -> Unit,
    onRecipeSelect: (String, Double) -> Unit,
    onNavigateToAddIngredient: () -> Unit,
    sheetViewModel: IngredientSearchViewModel = hiltViewModel()
) {
    val searchQuery by sheetViewModel.searchQuery.collectAsStateWithLifecycle()
    val ingredientResults by sheetViewModel.ingredientResults.collectAsStateWithLifecycle()
    val recipeResults by sheetViewModel.recipeResults.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingIngredientId by remember { mutableStateOf<String?>(null) }
    var pendingRecipeId by remember { mutableStateOf<String?>(null) }
    var amountText by remember { mutableStateOf("") }
    var showAmountDialog by remember { mutableStateOf(false) }

    if (showAmountDialog) {
        AlertDialog(
            onDismissRequest = { showAmountDialog = false; pendingIngredientId = null; pendingRecipeId = null },
            title = { Text(if (pendingRecipeId != null) "Portions" else "Amount (g)") },
            text = {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (pendingRecipeId != null) "Portions" else "Grams") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@TextButton
                    pendingIngredientId?.let { onIngredientSelect(it, amount) }
                    pendingRecipeId?.let { onRecipeSelect(it, amount) }
                    showAmountDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAmountDialog = false }) { Text("Cancel") }
            }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Add ingredient or recipe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = sheetViewModel::onQueryChange,
                placeholder = { Text("Search…") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(50)
            )
            Spacer(Modifier.height(4.dp))
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Ingredients") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Recipes") })
            }
            if (selectedTab == 0) {
                TextButton(
                    onClick = onNavigateToAddIngredient,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) { Text("+ New ingredient", color = PurplePrimary) }
                ingredientResults.forEach { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text(item.category, style = MaterialTheme.typography.labelSmall) },
                        trailingContent = { Text("${item.kcalPer100g.toInt()} kcal", style = MaterialTheme.typography.labelSmall) },
                        modifier = androidx.compose.ui.Modifier.clickable {
                            pendingIngredientId = item.id; pendingRecipeId = null
                            amountText = "100"; showAmountDialog = true
                        }
                    )
                }
            } else {
                recipeResults.forEach { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        trailingContent = {
                            Surface(shape = RoundedCornerShape(50), color = Color(0xFFFFD8E4)) {
                                Text(item.typeBadge, style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF31111D),
                                    modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        },
                        modifier = androidx.compose.ui.Modifier.clickable {
                            pendingRecipeId = item.id; pendingIngredientId = null
                            amountText = "1"; showAmountDialog = true
                        }
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
