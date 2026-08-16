import 'package:foodplan/data/repository/ingredient_repository.dart';
import 'package:foodplan/data/repository/meal_plan_repository.dart';
import 'package:foodplan/data/repository/recipe_repository.dart';
import 'package:foodplan/domain/model/ingredient.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:foodplan/domain/model/shopping_list.dart';

/// Mirrors the Kotlin GetShoppingListUseCase.
///
/// Builds a [ShoppingList] for [startDate]..[endDate], optionally filtered
/// to [selectedRecipeIds].  null means all recipes are included.
class GetShoppingListUseCase {
  GetShoppingListUseCase({
    required MealPlanRepository mealPlanRepository,
    required RecipeRepository recipeRepository,
    required IngredientRepository ingredientRepository,
  })  : _mealPlanRepository = mealPlanRepository,
        _recipeRepository = recipeRepository,
        _ingredientRepository = ingredientRepository;

  final MealPlanRepository _mealPlanRepository;
  final RecipeRepository _recipeRepository;
  final IngredientRepository _ingredientRepository;

  Future<ShoppingList> call(
    DateTime startDate,
    DateTime endDate, {
    Set<String>? selectedRecipeIds,
  }) async {
    // 1. Fetch all day plans in the range.
    final dayPlans =
        await _mealPlanRepository.getDayPlansForRange(startDate, endDate);

    // 2. All unique recipe IDs in the period.
    final allRecipeIds = dayPlans.values
        .expand((dp) => dp.meals)
        .map((s) => s.recipeId)
        .toSet()
        .toList();

    // 3. Fetch all recipes (full list for the period card selector).
    final allRecipes = <Recipe>[];
    for (final id in allRecipeIds) {
      final recipe = await _recipeRepository.getRecipeWithIngredients(id);
      if (recipe != null) allRecipes.add(recipe);
    }

    // 4. Apply optional recipe filter.
    final activeRecipeIds = selectedRecipeIds ?? allRecipeIds.toSet();
    final activeRecipes =
        allRecipes.where((r) => activeRecipeIds.contains(r.id)).toList();

    // 5. Build ingredient + sub-recipe caches.
    final allIngredientIds =
        _collectAllIngredientIds(allRecipes, {});
    final ingredientCache = <String, Ingredient>{};
    for (final id in allIngredientIds) {
      final ing = await _ingredientRepository.getIngredientById(id);
      if (ing != null) ingredientCache[id] = ing;
    }

    final subRecipeIds = allRecipes
        .expand((r) => r.ingredients)
        .map((ri) => ri.subRecipeId)
        .whereType<String>()
        .toSet();
    final subRecipeCache = <String, Recipe>{};
    for (final id in subRecipeIds) {
      final recipe = await _recipeRepository.getRecipeWithIngredients(id);
      if (recipe != null) subRecipeCache[id] = recipe;
    }

    // 6. Aggregate: ingredientId -> Map<recipeName, grams>
    final aggregated = <String, Map<String, double>>{};

    for (final recipe in activeRecipes) {
      final portionCount = dayPlans.values
          .expand((dp) => dp.meals)
          .where((s) => s.recipeId == recipe.id)
          .length
          .toDouble()
          .clamp(1.0, double.infinity);

      _expandRecipeIngredients(
        recipe: recipe,
        portionMultiplier: portionCount,
        subRecipeCache: subRecipeCache,
        recipeName: recipe.name,
        visited: {},
        onIngredient: (ingredientId, grams, recipeName) {
          final byRecipe =
              aggregated.putIfAbsent(ingredientId, () => {});
          byRecipe[recipeName] = (byRecipe[recipeName] ?? 0.0) + grams;
        },
      );
    }

    // 7. Build ShoppingItems with per-recipe contributions.
    final items = <ShoppingItem>[];
    for (final entry in aggregated.entries) {
      final ing = ingredientCache[entry.key];
      if (ing == null) continue;
      final contributions = entry.value.entries
          .map((e) => RecipeContribution(recipeName: e.key, grams: e.value))
          .toList()
        ..sort((a, b) => a.recipeName.compareTo(b.recipeName));
      items.add(ShoppingItem(
        ingredientId: entry.key,
        name: ing.name,
        totalGrams: contributions.fold(0.0, (s, c) => s + c.grams),
        unit: ShoppingUnit.grams,
        contributions: contributions,
      ));
    }

    // 8. Group by category.
    final categoryMap = <IngredientCategory?, List<ShoppingItem>>{};
    for (final item in items) {
      final cat = ingredientCache[item.ingredientId]?.category;
      categoryMap.putIfAbsent(cat, () => []).add(item);
    }

    final categories = categoryMap.entries
        .map((e) => ShoppingCategory(
              name: e.key?.displayName ?? 'Other',
              emoji: e.key?.emoji ?? '🛒',
              items: e.value..sort((a, b) => a.name.compareTo(b.name)),
            ))
        .toList()
      ..sort((a, b) => a.name.compareTo(b.name));

    return ShoppingList(
      period: ShoppingPeriod(
        startDate: startDate,
        endDate: endDate,
        recipes: allRecipes
            .map((r) => SelectableRecipe(
                  recipeId: r.id,
                  recipeName: r.name,
                ))
            .toList()
          ..sort((a, b) => a.recipeName.compareTo(b.recipeName)),
      ),
      categories: categories,
    );
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  Set<String> _collectAllIngredientIds(
    List<Recipe> recipes,
    Map<String, Recipe> subRecipeCache,
  ) {
    final ids = <String>{};
    for (final recipe in recipes) {
      for (final ri in recipe.ingredients) {
        if (ri.ingredientId != null) {
          ids.add(ri.ingredientId!);
        } else if (ri.subRecipeId != null) {
          final sub = subRecipeCache[ri.subRecipeId!];
          if (sub != null) {
            ids.addAll(_collectAllIngredientIds([sub], subRecipeCache));
          }
        }
      }
    }
    return ids;
  }

  void _expandRecipeIngredients({
    required Recipe recipe,
    required double portionMultiplier,
    required Map<String, Recipe> subRecipeCache,
    required String recipeName,
    required Set<String> visited,
    required void Function(
            String ingredientId, double grams, String recipeName)
        onIngredient,
  }) {
    if (visited.contains(recipe.id)) return;
    visited.add(recipe.id);

    for (final ri in recipe.ingredients) {
      if (ri.ingredientId != null && ri.grams != null) {
        onIngredient(
            ri.ingredientId!, ri.grams! * portionMultiplier, recipeName);
      } else if (ri.subRecipeId != null && ri.portions != null) {
        final sub = subRecipeCache[ri.subRecipeId!];
        if (sub == null) continue;
        _expandRecipeIngredients(
          recipe: sub,
          portionMultiplier: portionMultiplier * ri.portions!,
          subRecipeCache: subRecipeCache,
          recipeName: recipeName,
          visited: visited,
          onIngredient: onIngredient,
        );
      }
    }
  }
}

// ── Simple math expression evaluator ─────────────────────────────────────────
//
// Supports +, -, *, / and parentheses.  Returns null if the expression is
// invalid.  Used for the shopping list quantity adjustment feature.

double? evaluateExpression(String expr) {
  try {
    final result = _Parser(expr.trim()).parse();
    if (result.isNaN || result.isInfinite) return null;
    return result;
  } catch (_) {
    return null;
  }
}

class _Parser {
  _Parser(this._input);

  final String _input;
  int _pos = 0;

  double parse() {
    final result = _parseAddSub();
    _skipWs();
    if (_pos < _input.length) throw FormatException('Unexpected char');
    return result;
  }

  double _parseAddSub() {
    var left = _parseMulDiv();
    _skipWs();
    while (_pos < _input.length &&
        (_input[_pos] == '+' || _input[_pos] == '-')) {
      final op = _input[_pos++];
      final right = _parseMulDiv();
      left = op == '+' ? left + right : left - right;
      _skipWs();
    }
    return left;
  }

  double _parseMulDiv() {
    var left = _parseUnary();
    _skipWs();
    while (_pos < _input.length &&
        (_input[_pos] == '*' || _input[_pos] == '/')) {
      final op = _input[_pos++];
      final right = _parseUnary();
      left = op == '*' ? left * right : left / right;
      _skipWs();
    }
    return left;
  }

  double _parseUnary() {
    _skipWs();
    if (_pos < _input.length && _input[_pos] == '-') {
      _pos++;
      return -_parsePrimary();
    }
    if (_pos < _input.length && _input[_pos] == '+') {
      _pos++;
    }
    return _parsePrimary();
  }

  double _parsePrimary() {
    _skipWs();
    if (_pos < _input.length && _input[_pos] == '(') {
      _pos++; // consume '('
      final result = _parseAddSub();
      _skipWs();
      if (_pos >= _input.length || _input[_pos] != ')') {
        throw FormatException('Expected )');
      }
      _pos++; // consume ')'
      return result;
    }
    return _parseNumber();
  }

  double _parseNumber() {
    _skipWs();
    final start = _pos;
    while (_pos < _input.length &&
        (_input[_pos] == '.' ||
            (_input[_pos].codeUnitAt(0) >= 48 &&
                _input[_pos].codeUnitAt(0) <= 57))) {
      _pos++;
    }
    if (_pos == start) throw FormatException('Expected number at pos $_pos');
    return double.parse(_input.substring(start, _pos));
  }

  void _skipWs() {
    while (_pos < _input.length && _input[_pos] == ' ') {
      _pos++;
    }
  }
}
