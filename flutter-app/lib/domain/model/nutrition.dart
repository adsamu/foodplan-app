import 'package:freezed_annotation/freezed_annotation.dart';
import 'ingredient.dart';

part 'nutrition.freezed.dart';

@freezed
class RecipeNutrition with _$RecipeNutrition {
  const factory RecipeNutrition({
    required double kcal,
    required double protein,
    required double fat,
    required double carbs,
  }) = _RecipeNutrition;

  const RecipeNutrition._();

  static const RecipeNutrition zero =
      RecipeNutrition(kcal: 0, protein: 0, fat: 0, carbs: 0);

  RecipeNutrition operator +(RecipeNutrition other) => RecipeNutrition(
        kcal: kcal + other.kcal,
        protein: protein + other.protein,
        fat: fat + other.fat,
        carbs: carbs + other.carbs,
      );

  RecipeNutrition operator *(double factor) => RecipeNutrition(
        kcal: kcal * factor,
        protein: protein * factor,
        fat: fat * factor,
        carbs: carbs * factor,
      );

  static RecipeNutrition fromIngredient(Ingredient ing, double grams) =>
      RecipeNutrition(
        kcal: ing.kcalPer100g * grams / 100,
        protein: ing.proteinPer100g * grams / 100,
        fat: ing.fatPer100g * grams / 100,
        carbs: ing.carbsPer100g * grams / 100,
      );
}
