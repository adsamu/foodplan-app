import 'package:freezed_annotation/freezed_annotation.dart';

part 'ingredient.freezed.dart';

// ── Enums ─────────────────────────────────────────────────────────────────────

enum IngredientCategory {
  fruitVeg('Fruit & Vegetables', '🥦', 'FRUIT_VEG'),
  meat('Meat', '🥩', 'MEAT'),
  fish('Fish & Seafood', '🐟', 'FISH'),
  dairyEggs('Dairy & Eggs', '🥚', 'DAIRY_EGGS'),
  cheese('Cheese', '🧀', 'CHEESE'),
  grains('Grains & Rice', '🌾', 'GRAINS'),
  breadBakery('Bread & Bakery', '🍞', 'BREAD_BAKERY'),
  dryGoods('Dry Goods & Pasta', '🫙', 'DRY_GOODS'),
  nuts('Nuts & Seeds', '🥜', 'NUTS'),
  canned('Canned & Preserved', '🥫', 'CANNED'),
  frozen('Frozen', '🧊', 'FROZEN'),
  oilsSauces('Oils & Sauces', '🫗', 'OILS_SAUCES'),
  spices('Spices & Herbs', '🌿', 'SPICES'),
  drinks('Drinks', '🥤', 'DRINKS'),
  supplement('Supplements', '💊', 'SUPPLEMENT'),
  other('Other', '📦', 'OTHER');

  const IngredientCategory(this.displayName, this.emoji, this._firestoreName);

  final String displayName;
  final String emoji;
  final String _firestoreName;

  String get firestoreName => _firestoreName;

  static IngredientCategory fromFirestore(String? s) {
    if (s == null) return IngredientCategory.other;
    try {
      return IngredientCategory.values.firstWhere(
        (e) => e._firestoreName == s.toUpperCase(),
      );
    } catch (_) {
      return IngredientCategory.other;
    }
  }
}

enum IngredientSource {
  label,
  livsmedelsverket,
  calculated,
  barcode;

  String get firestoreName => name.toUpperCase();

  static IngredientSource fromFirestore(String? s) {
    if (s == null) return IngredientSource.label;
    try {
      return IngredientSource.values.firstWhere(
        (e) => e.firestoreName == s.toUpperCase(),
      );
    } catch (_) {
      return IngredientSource.label;
    }
  }
}

// ── Domain model ──────────────────────────────────────────────────────────────

@freezed
class Ingredient with _$Ingredient {
  const factory Ingredient({
    required String id,
    required String name,
    @Default(IngredientCategory.other) IngredientCategory category,
    @Default(0.0) double kcalPer100g,
    @Default(0.0) double proteinPer100g,
    @Default(0.0) double fatPer100g,
    @Default(0.0) double carbsPer100g,
    @Default(IngredientSource.label) IngredientSource source,
    @Default(<String>[]) List<String> steps,
  }) = _Ingredient;
}
