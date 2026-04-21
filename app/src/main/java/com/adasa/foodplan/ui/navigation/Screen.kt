package com.adasa.foodplan.ui.navigation

sealed class Screen(val route: String) {
    // Top-level (bottom nav) destinations
    data object MealPlan : Screen("meal_plan")
    data object RecipeList : Screen("recipe_list")
    data object Shopping : Screen("shopping")
    data object Profile : Screen("profile")

    // Detail / add-edit destinations (no bottom nav)
    data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }

    data object AddEditRecipe : Screen("add_edit_recipe?recipeId={recipeId}") {
        fun createRoute(recipeId: String? = null) =
            if (recipeId != null) "add_edit_recipe?recipeId=$recipeId"
            else "add_edit_recipe"
    }

    data object AddEditIngredient : Screen("add_edit_ingredient?ingredientId={ingredientId}") {
        fun createRoute(ingredientId: String? = null) =
            if (ingredientId != null) "add_edit_ingredient?ingredientId=$ingredientId"
            else "add_edit_ingredient"
    }
}

/** Routes where the bottom navigation bar is visible. */
val topLevelRoutes = setOf(
    Screen.MealPlan.route,
    Screen.RecipeList.route,
    Screen.Shopping.route,
    Screen.Profile.route
)
