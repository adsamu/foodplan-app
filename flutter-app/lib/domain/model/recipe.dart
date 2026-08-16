import 'package:freezed_annotation/freezed_annotation.dart';

part 'recipe.freezed.dart';

// ── Enums ─────────────────────────────────────────────────────────────────────

enum RecipeType {
  meal,
  component;

  /// Matches Kotlin enum name (MEAL, COMPONENT)
  String get firestoreName => name.toUpperCase();

  static RecipeType? fromFirestore(String? s) {
    if (s == null) return null;
    try {
      return RecipeType.values.firstWhere(
        (e) => e.firestoreName == s.toUpperCase(),
      );
    } catch (_) {
      return null;
    }
  }
}

enum MealCategory {
  breakfast,
  lunch,
  dinner,
  snack;

  String get firestoreName => name.toUpperCase();

  static MealCategory? fromFirestore(String? s) {
    if (s == null) return null;
    try {
      return MealCategory.values.firstWhere(
        (e) => e.firestoreName == s.toUpperCase(),
      );
    } catch (_) {
      return null;
    }
  }
}

enum ComponentCategory {
  sauce,
  dressing,
  salsa,
  salad,
  side,
  other;

  String get firestoreName => name.toUpperCase();

  static ComponentCategory? fromFirestore(String? s) {
    if (s == null) return null;
    try {
      return ComponentCategory.values.firstWhere(
        (e) => e.firestoreName == s.toUpperCase(),
      );
    } catch (_) {
      return null;
    }
  }
}

// ── Domain models ─────────────────────────────────────────────────────────────

@freezed
class Recipe with _$Recipe {
  const factory Recipe({
    required String id,
    required String name,
    @Default(RecipeType.meal) RecipeType type,
    @Default(<MealCategory>{}) Set<MealCategory> mealCategories,
    ComponentCategory? componentCategory,
    @Default(<RecipeIngredient>[]) List<RecipeIngredient> ingredients,
    @Default(<String>[]) List<String> steps,
    @Default('') String notes,
  }) = _Recipe;
}

@freezed
class RecipeIngredient with _$RecipeIngredient {
  const factory RecipeIngredient({
    String? ingredientId,
    String? subRecipeId,
    double? grams,
    double? portions,
  }) = _RecipeIngredient;

  const RecipeIngredient._();

  bool get isSubRecipe => subRecipeId != null;

  /// Effective grams given the per-portion gram weight of the sub-recipe.
  double effectiveGrams(double portionGrams) =>
      grams ?? ((portions ?? 1.0) * portionGrams);
}
