import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../mealplan/meal_plan_screen.dart';
import '../recipe/recipe_list_screen.dart';
import '../recipe/recipe_detail_screen.dart';
import '../recipe/add_edit_recipe_screen.dart';
import '../ingredient/ingredient_detail_screen.dart';
import '../ingredient/add_edit_ingredient_screen.dart';
import '../shopping/shopping_screen.dart';
import '../settings/settings_screen.dart';
import '../profile/profile_screen.dart';

final appRouter = GoRouter(
  initialLocation: '/meal-plan',
  routes: [
    ShellRoute(
      builder: (context, state, child) => _ScaffoldWithNav(child: child),
      routes: [
        GoRoute(
          path: '/meal-plan',
          pageBuilder: (ctx, state) => const NoTransitionPage(
            child: MealPlanScreen(),
          ),
        ),
        GoRoute(
          path: '/recipes',
          pageBuilder: (ctx, state) => const NoTransitionPage(
            child: RecipeListScreen(),
          ),
        ),
        GoRoute(
          path: '/shopping',
          pageBuilder: (ctx, state) => const NoTransitionPage(
            child: ShoppingScreen(),
          ),
        ),
        GoRoute(
          path: '/settings',
          pageBuilder: (ctx, state) => const NoTransitionPage(
            child: SettingsScreen(),
          ),
        ),
      ],
    ),
    GoRoute(
      path: '/recipes/edit',
      builder: (ctx, state) {
        final recipeId = state.uri.queryParameters['recipeId'];
        return AddEditRecipeScreen(recipeId: recipeId);
      },
    ),
    GoRoute(
      path: '/recipes/:recipeId',
      builder: (ctx, state) {
        final recipeId = state.pathParameters['recipeId']!;
        return RecipeDetailScreen(recipeId: recipeId);
      },
    ),
    GoRoute(
      path: '/ingredients/edit',
      builder: (ctx, state) {
        final ingredientId = state.uri.queryParameters['ingredientId'];
        return AddEditIngredientScreen(ingredientId: ingredientId);
      },
    ),
    GoRoute(
      path: '/ingredients/:ingredientId',
      builder: (ctx, state) {
        final ingredientId = state.pathParameters['ingredientId']!;
        final idx = int.tryParse(state.uri.queryParameters['recipeIdx'] ?? '');
        final grams = double.tryParse(state.uri.queryParameters['grams'] ?? '');
        return IngredientDetailScreen(
          ingredientId: ingredientId,
          recipeIngredientIndex: idx,
          currentAmountGrams: grams,
        );
      },
    ),
    GoRoute(
      path: '/profile',
      builder: (ctx, state) => const ProfileScreen(),
    ),
  ],
);

const _tabs = ['/meal-plan', '/recipes', '/shopping', '/settings'];

class _ScaffoldWithNav extends StatelessWidget {
  final Widget child;
  const _ScaffoldWithNav({required this.child});

  @override
  Widget build(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;
    final idx = _tabs.indexWhere((t) => location.startsWith(t));

    return Scaffold(
      body: child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: idx < 0 ? 0 : idx,
        onDestinationSelected: (i) => context.go(_tabs[i]),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.date_range), label: 'Meal Plan'),
          NavigationDestination(icon: Icon(Icons.menu_book), label: 'Recipes'),
          NavigationDestination(icon: Icon(Icons.shopping_cart), label: 'Shopping'),
          NavigationDestination(icon: Icon(Icons.settings), label: 'Settings'),
        ],
      ),
    );
  }
}
