import 'package:freezed_annotation/freezed_annotation.dart';

part 'shopping_list.freezed.dart';

// ── Enums ─────────────────────────────────────────────────────────────────────

enum ShoppingUnit { grams, pieces, deciliters }

// ── Domain models ─────────────────────────────────────────────────────────────

@freezed
class ShoppingList with _$ShoppingList {
  const factory ShoppingList({
    required ShoppingPeriod period,
    required List<ShoppingCategory> categories,
  }) = _ShoppingList;

  const ShoppingList._();

  int get totalItems => categories.fold(0, (sum, c) => sum + c.items.length);
}

@freezed
class ShoppingPeriod with _$ShoppingPeriod {
  const factory ShoppingPeriod({
    required DateTime startDate,
    required DateTime endDate,
    @Default(<SelectableRecipe>[]) List<SelectableRecipe> recipes,
  }) = _ShoppingPeriod;

  const ShoppingPeriod._();

  List<String> get recipeNames => recipes.map((r) => r.recipeName).toList();
}

@freezed
class SelectableRecipe with _$SelectableRecipe {
  const factory SelectableRecipe({
    required String recipeId,
    required String recipeName,
    @Default(true) bool isSelected,
  }) = _SelectableRecipe;
}

@freezed
class ShoppingCategory with _$ShoppingCategory {
  const factory ShoppingCategory({
    required String name,
    required String emoji,
    required List<ShoppingItem> items,
  }) = _ShoppingCategory;
}

@freezed
class ShoppingItem with _$ShoppingItem {
  const factory ShoppingItem({
    required String ingredientId,
    required String name,
    required double totalGrams,
    required ShoppingUnit unit,
    required List<RecipeContribution> contributions,
  }) = _ShoppingItem;

  const ShoppingItem._();

  List<String> get usedInRecipes =>
      contributions.map((c) => c.recipeName).toSet().toList()..sort();
}

@freezed
class RecipeContribution with _$RecipeContribution {
  const factory RecipeContribution({
    required String recipeName,
    required double grams,
  }) = _RecipeContribution;
}
