package com.adasa.foodplan.ui.ingredient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adasa.foodplan.domain.model.IngredientSource

private val ChipActiveBackground = Color(0xFFE8DEF8)
private val ChipActiveText = Color(0xFF21005D)
private val CardBackground = Color(0xFFF3EDF7)
private val SectionLabelColor = Color(0xFF79747E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIngredientScreen(
    ingredientId: String?,
    onSaved: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AddEditIngredientViewModel = hiltViewModel()
) {
    LaunchedEffect(ingredientId) { viewModel.loadIngredient(ingredientId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val source by viewModel.source.collectAsStateWithLifecycle()
    val kcal by viewModel.kcal.collectAsStateWithLifecycle()
    val protein by viewModel.protein.collectAsStateWithLifecycle()
    val fat by viewModel.fat.collectAsStateWithLifecycle()
    val carbs by viewModel.carbs.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val saved = uiState
        if (saved is AddEditIngredientUiState.Saved) onSaved(saved.ingredientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (ingredientId == null) "New ingredient" else "Edit ingredient")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::saveIngredient,
                        enabled = uiState !is AddEditIngredientUiState.Saving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = category,
                    onValueChange = viewModel::onCategoryChange,
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Dairy, Grain, Protein – chicken") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true
                )
            }

            item {
                SourceSection(source = source, onSourceChange = viewModel::onSourceChange)
            }
            item {
                NutritionSection(
                    kcal = kcal, protein = protein, fat = fat, carbs = carbs,
                    onKcalChange = viewModel::onKcalChange,
                    onProteinChange = viewModel::onProteinChange,
                    onFatChange = viewModel::onFatChange,
                    onCarbsChange = viewModel::onCarbsChange
                )
            }

            item {
                Button(
                    onClick = viewModel::saveIngredient,
                    enabled = uiState !is AddEditIngredientUiState.Saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    if (uiState is AddEditIngredientUiState.Saving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save ingredient", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceSection(
    source: IngredientSource,
    onSourceChange: (IngredientSource) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "SOURCE",
            style = MaterialTheme.typography.labelSmall,
            color = SectionLabelColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IngredientSource.entries.forEach { s ->
                val active = source == s
                FilterChip(
                    selected = active,
                    onClick = { onSourceChange(s) },
                    label = {
                        Text(
                            s.displayName,
                            color = if (active) ChipActiveText else Color.Unspecified,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChipActiveBackground
                    )
                )
            }
        }
    }
}

@Composable
private fun NutritionSection(
    kcal: Double,
    protein: Double,
    fat: Double,
    carbs: Double,
    onKcalChange: (Double) -> Unit,
    onProteinChange: (Double) -> Unit,
    onFatChange: (Double) -> Unit,
    onCarbsChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "NUTRITION PER 100 G",
                style = MaterialTheme.typography.labelSmall,
                color = SectionLabelColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NutritionField(
                    label = "Calories (kcal)", value = kcal,
                    onChange = onKcalChange, modifier = Modifier.weight(1f)
                )
                NutritionField(
                    label = "Protein (g)", value = protein,
                    onChange = onProteinChange, modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NutritionField(
                    label = "Fat (g)", value = fat,
                    onChange = onFatChange, modifier = Modifier.weight(1f)
                )
                NutritionField(
                    label = "Carbs (g)", value = carbs,
                    onChange = onCarbsChange, modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NutritionField(
    label: String,
    value: Double,
    onChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(value) { mutableStateOf(if (value == 0.0) "" else value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            input.toDoubleOrNull()?.let { onChange(it) }
        },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}
