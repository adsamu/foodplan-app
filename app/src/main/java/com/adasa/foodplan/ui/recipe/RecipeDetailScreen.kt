package com.adasa.foodplan.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.RecipeNutrition
import com.adasa.foodplan.domain.model.RecipeType

private val HeroGradientStart = Color(0xFFEADDFF)
private val HeroGradientEnd = Color(0xFFE8DEF8)
private val MealBadgeBackground = Color(0xFFEADDFF)
private val MealBadgeText = Color(0xFF21005D)
private val ComponentBadgeBackground = Color(0xFFFFD8E4)
private val ComponentBadgeText = Color(0xFF31111D)
private val StepCircleColor = Color(0xFF6750A4)
private val StepCircleUnchecked = Color(0xFFCAC4D0)
private val EditButtonColor = Color(0xFF6750A4)
private val IconBoxBackground = Color(0xFFEADDFF)
private val SectionLabelColor = Color(0xFF79747E)
private val DividerColor = Color(0xFFE8DEF8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.loadRecipe(recipeId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete recipe?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecipe()
                    showDeleteDialog = false
                    onBackClick()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is RecipeDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RecipeDetailUiState.NotFound -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("Recipe not found")
                }
            }
            is RecipeDetailUiState.Success -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            // Hero section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(HeroGradientStart, HeroGradientEnd)
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = state.recipe.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (state.recipe.type == RecipeType.COMPONENT) {
                                            DetailBadge(
                                                label = state.recipe.componentCategory?.displayName ?: "Component",
                                                background = ComponentBadgeBackground,
                                                textColor = ComponentBadgeText
                                            )
                                        }
                                        state.recipe.mealCategories.forEach { cat ->
                                            DetailBadge(
                                                label = cat.displayName,
                                                background = MealBadgeBackground,
                                                textColor = MealBadgeText
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    NutritionRow(nutrition = state.nutrition)
                                }
                            }
                        }

                        // Ingredients section
                        if (state.recipe.ingredients.isNotEmpty()) {
                            item {
                                DetailSectionLabel(
                                    text = "Ingredients",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF3EDF7)
                                    )
                                ) {
                                    Column {
                                        state.recipe.ingredients.forEachIndexed { index, ingredient ->
                                            val id = ingredient.ingredientId ?: ingredient.subRecipeId ?: ""
                                            val name = state.ingredientNames[id]
                                                ?: if (ingredient.subRecipeId != null) "Recipe" else "Ingredient"
                                            val isComponent = ingredient.subRecipeId != null
                                            val amount = ingredient.grams?.let { "${it.toInt()} g" }
                                                ?: ingredient.portions?.let { "${it} serv." } ?: ""
                                            IngredientDetailRow(
                                                name = name,
                                                amount = amount,
                                                isComponent = isComponent
                                            )
                                            if (index < state.recipe.ingredients.lastIndex) {
                                                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Directions section
                        if (state.recipe.steps.isNotEmpty()) {
                            item {
                                DetailSectionLabel(
                                    text = "Directions",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                DirectionsCard(steps = state.recipe.steps)
                            }
                        }
                    }

                    // Floating edit button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        Button(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = EditButtonColor),
                            modifier = Modifier.widthIn(min = 120.dp)
                        ) {
                            Text("Edit", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionRow(nutrition: RecipeNutrition) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NutritionBox(label = "kcal", value = nutrition.kcal.toInt().toString())
        NutritionBox(label = "protein", value = "${nutrition.protein.toInt()}g")
        NutritionBox(label = "fat", value = "${nutrition.fat.toInt()}g")
        NutritionBox(label = "carbs", value = "${nutrition.carbs.toInt()}g")
    }
}

@Composable
private fun NutritionBox(label: String, value: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.6f)) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DetailBadge(label: String, background: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun DetailSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = SectionLabelColor,
        letterSpacing = 0.8.sp,
        modifier = modifier
    )
}

@Composable
private fun IngredientDetailRow(name: String, amount: String, isComponent: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IconBoxBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isComponent) Icons.Default.Blender else Icons.Default.Restaurant,
                contentDescription = null,
                tint = MealBadgeText,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall,
            color = SectionLabelColor
        )
    }
}

@Composable
private fun DirectionsCard(steps: List<String>) {
    val checkedSteps = remember { mutableStateListOf(*Array(steps.size) { false }) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            steps.forEachIndexed { index, step ->
                val checked = checkedSteps.getOrElse(index) { false }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.clickable { checkedSteps[index] = !checked }
                ) {
                    // Outline circle → filled when checked
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .then(
                                if (checked)
                                    Modifier.background(StepCircleColor)
                                else
                                    Modifier
                                        .background(Color.Transparent)
                                        .border(2.dp, StepCircleUnchecked, CircleShape)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (checked) Color.White else StepCircleUnchecked,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (checked) SectionLabelColor else Color.Unspecified,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
