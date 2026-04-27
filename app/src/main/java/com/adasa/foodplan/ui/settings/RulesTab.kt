package com.adasa.foodplan.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adasa.foodplan.domain.model.*
import java.util.UUID

@Composable
fun RulesTab(config: MealPlanConfig?, viewModel: SettingsViewModel) {
    val rules = config?.rules ?: emptyList()
    val variety = config?.variety ?: VarietyConfig()
    var showAddRule by remember { mutableStateOf(false) }
    var maxDaysInARow by remember(variety) { mutableIntStateOf(variety.maxDaysInARow) }
    var uniqueWeeks by remember(variety) { mutableIntStateOf(variety.uniqueWeeksBeforeRepeat) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Active rules
        SettingsSection("Active rules", "Diet category or ingredient min/max per week")
        SettingsCard {
            Column {
                rules.forEach { rule ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(ruleEmoji(rule), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = ruleDescription(rule),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.deleteRule(rule.id) },
                            modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                    if (rule != rules.last()) HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                }
                HorizontalDivider()
                TextButton(
                    onClick = { showAddRule = !showAddRule },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("+ Add rule") }

                // Inline add rule form
                if (showAddRule) {
                    AddRuleForm(
                        onAdd = { rule ->
                            viewModel.addRule(rule)
                            showAddRule = false
                        },
                        onCancel = { showAddRule = false }
                    )
                }
            }
        }

        // Variety
        SettingsSection("Variety")
        SettingsCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Max days in a row", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Same meal, consecutive days (excl. batch)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StepperControl(
                        value = maxDaysInARow,
                        label = "$maxDaysInARow",
                        onDecrement = {
                            if (maxDaysInARow > 1) {
                                maxDaysInARow--
                                viewModel.setVariety(variety.copy(maxDaysInARow = maxDaysInARow))
                            }
                        },
                        onIncrement = {
                            maxDaysInARow++
                            viewModel.setVariety(variety.copy(maxDaysInARow = maxDaysInARow))
                        }
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unique weeks before repeat", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Different weeks before cycling back",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StepperControl(
                        value = uniqueWeeks,
                        label = "$uniqueWeeks",
                        onDecrement = {
                            if (uniqueWeeks > 1) {
                                uniqueWeeks--
                                viewModel.setVariety(variety.copy(uniqueWeeksBeforeRepeat = uniqueWeeks))
                            }
                        },
                        onIncrement = {
                            uniqueWeeks++
                            viewModel.setVariety(variety.copy(uniqueWeeksBeforeRepeat = uniqueWeeks))
                        }
                    )
                }
                HorizontalDivider()
                SettingsSwitchRow(
                    title = "Protein source variety",
                    subtitle = "Alternate chicken / beef / fish",
                    checked = variety.proteinSourceVariety
                ) { viewModel.setVariety(variety.copy(proteinSourceVariety = it)) }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AddRuleForm(onAdd: (OptimizerRule) -> Unit, onCancel: () -> Unit) {
    var ruleType by remember { mutableStateOf(RuleType.DIET_CATEGORY) }
    var target by remember { mutableStateOf("Fish") }
    var constraint by remember { mutableStateOf(RuleConstraint.MIN_PER_WEEK) }
    var value by remember { mutableIntStateOf(1) }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider()
        Text("New rule", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(RuleType.DIET_CATEGORY to "Diet category", RuleType.INGREDIENT to "Ingredient").forEach { (type, label) ->
                FilterChip(selected = ruleType == type, onClick = { ruleType = type }, label = { Text(label) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text(if (ruleType == RuleType.DIET_CATEGORY) "Category" else "Ingredient") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(RuleConstraint.MIN_PER_WEEK to "min", RuleConstraint.MAX_PER_WEEK to "max").forEach { (c, label) ->
                    FilterChip(selected = constraint == c, onClick = { constraint = c }, label = { Text(label) })
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Times per week:", style = MaterialTheme.typography.bodySmall)
            StepperControl(value = value, label = "$value", onDecrement = { if (value > 1) value-- }, onIncrement = { value++ })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    onAdd(OptimizerRule(
                        id = UUID.randomUUID().toString(),
                        type = ruleType,
                        target = target,
                        targetName = target,
                        constraint = constraint,
                        value = value
                    ))
                },
                modifier = Modifier.weight(1f)
            ) { Text("Add") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
        }
    }
}

private fun ruleEmoji(rule: OptimizerRule): String = when {
    rule.targetName.contains("fish", ignoreCase = true) -> "🐟"
    rule.targetName.contains("meat", ignoreCase = true) -> "🥩"
    rule.targetName.contains("vegan", ignoreCase = true) -> "🥦"
    rule.targetName.contains("chicken", ignoreCase = true) -> "🐓"
    rule.targetName.contains("tuna", ignoreCase = true) -> "🫙"
    rule.type == RuleType.DIET_CATEGORY -> "🍽️"
    else -> "🥘"
}

private fun ruleDescription(rule: OptimizerRule): String {
    val typeLabel = if (rule.type == RuleType.DIET_CATEGORY) "Diet" else "Ingredient"
    val constraintLabel = if (rule.constraint == RuleConstraint.MIN_PER_WEEK) "min" else "max"
    return "$typeLabel ${rule.targetName} — $constraintLabel ${rule.value}× / week"
}