package com.adasa.foodplan.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adasa.foodplan.ui.ingredient.AddEditIngredientScreen
import com.adasa.foodplan.ui.ingredient.IngredientDetailScreen
import com.adasa.foodplan.ui.mealplan.MealPlanScreen
import com.adasa.foodplan.ui.profile.ProfileScreen
import com.adasa.foodplan.ui.recipe.AddEditRecipeScreen
import com.adasa.foodplan.ui.recipe.RecipeDetailScreen
import com.adasa.foodplan.ui.recipe.RecipeListScreen
import com.adasa.foodplan.ui.settings.SettingsScreen
import com.adasa.foodplan.ui.shopping.ShoppingScreen

private data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: @Composable () -> Unit
)

private val navItems = listOf(
    NavItem(Screen.MealPlan, "Meal Plan") {
        Icon(Icons.Default.DateRange, contentDescription = "Meal Plan")
    },
    NavItem(Screen.RecipeList, "Recipes") {
        Icon(Icons.Default.MenuBook, contentDescription = "Recipes")
    },
    NavItem(Screen.Shopping, "Shopping") {
        Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping")
    },
    NavItem(Screen.Settings, "Settings") {
        Icon(Icons.Default.Settings, contentDescription = "Settings")
    }
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MealPlan.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.MealPlan.route) {
                MealPlanScreen(
                    onNavigateToRecipeDetail = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    }
                )
            }
            composable(Screen.Shopping.route) { ShoppingScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToAppSettings = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.RecipeList.route) {
                RecipeListScreen(
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    },
                    onIngredientClick = { ingredientId ->
                        navController.navigate(Screen.IngredientDetail.createRoute(ingredientId))
                    },
                    onAddRecipeClick = {
                        navController.navigate(Screen.AddEditRecipe.createRoute())
                    },
                    onAddIngredientClick = {
                        navController.navigate(Screen.AddEditIngredient.createRoute())
                    }
                )
            }

            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
                RecipeDetailScreen(
                    recipeId          = recipeId,
                    onEditClick       = { navController.navigate(Screen.AddEditRecipe.createRoute(recipeId)) },
                    onBackClick       = { navController.popBackStack() },
                    onIngredientClick = { ingredientId ->
                        navController.navigate(Screen.IngredientDetail.createRoute(ingredientId))
                    }
                )
            }

            composable(
                route = Screen.AddEditRecipe.route,
                arguments = listOf(
                    navArgument("recipeId") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")
                val viewModel =
                    androidx.hilt.navigation.compose.hiltViewModel<com.adasa.foodplan.ui.recipe.AddEditRecipeViewModel>()

                // Auto-add ingredient when returning from AddEditIngredientScreen
                val newIngredientId by backStackEntry.savedStateHandle
                    .getStateFlow<String?>("new_ingredient_id", null)
                    .collectAsStateWithLifecycle()
                LaunchedEffect(newIngredientId) {
                    newIngredientId?.let { id ->
                        viewModel.addIngredient(id, 100.0)
                        backStackEntry.savedStateHandle["new_ingredient_id"] = null
                    }
                }

                AddEditRecipeScreen(
                    recipeId    = recipeId,
                    onSaved     = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() },
                    onNavigateToAddIngredient = {
                        navController.navigate(Screen.AddEditIngredient.createRoute())
                    },
                    onViewIngredient = { ingredientId, index, grams ->
                        navController.navigate(
                            Screen.IngredientDetail.createRoute(ingredientId, index, grams)
                        )
                    },
                    viewModel = viewModel
                )
            }

            composable(
                route = Screen.AddEditIngredient.route,
                arguments = listOf(
                    navArgument("ingredientId") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val ingredientId = backStackEntry.arguments?.getString("ingredientId")
                AddEditIngredientScreen(
                    ingredientId = ingredientId,
                    onSaved = { savedId ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("new_ingredient_id", savedId)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.IngredientDetail.route,
                arguments = listOf(
                    navArgument("ingredientId")           { type = NavType.StringType },
                    navArgument("recipeIngredientIndex")  { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("currentAmountGrams")     { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val ingredientId          = backStackEntry.arguments?.getString("ingredientId") ?: return@composable
                val recipeIngredientIndex = backStackEntry.arguments?.getString("recipeIngredientIndex")?.toIntOrNull()
                val currentAmountGrams    = backStackEntry.arguments?.getString("currentAmountGrams")?.toDoubleOrNull()

                // Get the same AddEditRecipeViewModel instance that AddEditRecipeScreen is using.
                // Explicit type parameter avoids any inference ambiguity.
                val addEditViewModel: com.adasa.foodplan.ui.recipe.AddEditRecipeViewModel? =
                    if (recipeIngredientIndex != null)
                        androidx.hilt.navigation.compose.hiltViewModel<com.adasa.foodplan.ui.recipe.AddEditRecipeViewModel>(
                            navController.getBackStackEntry(Screen.AddEditRecipe.route)
                        )
                    else null

                IngredientDetailScreen(
                    ingredientId           = ingredientId,
                    recipeIngredientIndex  = recipeIngredientIndex,
                    currentAmountGrams     = currentAmountGrams,
                    onBackClick            = { navController.popBackStack() },
                    onEditClick            = { id ->
                        navController.navigate(Screen.AddEditIngredient.createRoute(id))
                    },
                    // Index is captured here in the NavGraph — no need to pass it through the screen
                    onRemoveFromRecipe     = {
                        if (recipeIngredientIndex != null) {
                            addEditViewModel?.removeIngredient(recipeIngredientIndex)
                        }
                        navController.popBackStack()
                    },
                    onAmountSaved          = { grams ->
                        if (recipeIngredientIndex != null) {
                            addEditViewModel?.updateIngredientAmount(recipeIngredientIndex, grams)
                        }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}