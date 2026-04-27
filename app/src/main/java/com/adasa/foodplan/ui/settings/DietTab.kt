package com.adasa.foodplan.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adasa.foodplan.domain.model.*

@Composable
fun DietTab(config: MealPlanConfig?, viewModel: SettingsViewModel) {
    val diet = config?.diet
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Diet type
        SettingsSection("Diet type", "Recipes must match all selected types")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DietType.entries.forEach { type ->
                        val selected = type in (diet?.dietTypes ?: emptySet())
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleDietType(type) },
                            label = { Text(type.displayName) }
                        )
                    }
                }
            }
        }

        // Allergies
        SettingsSection("Allergies & intolerances", "Recipes with these ingredients are never scheduled")
        SettingsCard {
            Column(modifier = Modifier.padding(12.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AllergyType.entries.forEach { allergy ->
                        val selected = allergy in (diet?.allergies ?: emptySet())
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleAllergy(allergy) },
                            label = { Text(allergy.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                SettingsRow(icon = "🚫", title = "Excluded ingredients",
                    subtitle = "Block specific items from the plan")
            }
        }

        // Preferences
        SettingsSection("Preferences", "Soft — optimizer avoids but does not exclude")
        SettingsCard {
            Column {
                SettingsRow(icon = "❤️", title = "Preferred ingredients",
                    subtitle = "Prioritise in recipe selection",
                    badge = diet?.preferredIngredientIds?.size?.toString()?.takeIf { it != "0" })
                HorizontalDivider()
                SettingsRow(icon = "👎", title = "Disliked ingredients",
                    subtitle = "Avoid but don't fully exclude",
                    badge = diet?.dislikedIngredientIds?.size?.toString()?.takeIf { it != "0" })
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}