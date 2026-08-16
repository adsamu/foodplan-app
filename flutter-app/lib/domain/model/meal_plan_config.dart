import 'package:freezed_annotation/freezed_annotation.dart';
import 'recipe.dart';

part 'meal_plan_config.freezed.dart';

// ── Enums ─────────────────────────────────────────────────────────────────────

enum MacroField {
  protein,
  fat,
  carbs;

  String get firestoreName => name.toUpperCase();

  static MacroField fromFirestore(String? s) {
    if (s == null) return MacroField.protein;
    try {
      return MacroField.values.firstWhere(
        (e) => e.firestoreName == s.toUpperCase(),
      );
    } catch (_) {
      return MacroField.protein;
    }
  }
}

enum DietType {
  vegetarian,
  vegan,
  pescatarian,
  keto;

  String get firestoreName => name.toUpperCase();
}

enum AllergyType {
  gluten,
  dairy,
  nuts,
  shellfish,
  eggs,
  soy,
  pork;

  String get firestoreName => name.toUpperCase();
}

enum VarietyLevel {
  flexible,
  balanced,
  strict;

  int get recencyWindowWeeks => switch (this) {
    VarietyLevel.flexible => 2,
    VarietyLevel.balanced => 4,
    VarietyLevel.strict => 6,
  };

  int get recencyWindowDays => recencyWindowWeeks * 7;

  double get penaltyWeight => switch (this) {
    VarietyLevel.flexible => 0.2,
    VarietyLevel.balanced => 0.6,
    VarietyLevel.strict => 1.5,
  };

  bool get hardExcludeWithinWindow => this == VarietyLevel.strict;

  String get firestoreName => name.toUpperCase();
}

enum ConstraintType {
  minPerWeek,
  maxPerWeek;

  String get firestoreName =>
      name == 'minPerWeek' ? 'MIN_PER_WEEK' : 'MAX_PER_WEEK';

  static ConstraintType fromFirestore(String? s) {
    if (s == null) return ConstraintType.minPerWeek;
    return switch (s.toUpperCase()) {
      'MIN_PER_WEEK' => ConstraintType.minPerWeek,
      'MAX_PER_WEEK' => ConstraintType.maxPerWeek,
      _ => ConstraintType.minPerWeek,
    };
  }
}

enum RuleTargetType {
  dietCategory,
  ingredient;

  String get firestoreName =>
      name == 'dietCategory' ? 'DIET_CATEGORY' : 'INGREDIENT';

  static RuleTargetType fromFirestore(String? s) {
    if (s == null) return RuleTargetType.dietCategory;
    return switch (s.toUpperCase()) {
      'DIET_CATEGORY' => RuleTargetType.dietCategory,
      'INGREDIENT' => RuleTargetType.ingredient,
      _ => RuleTargetType.dietCategory,
    };
  }
}

enum AppTheme {
  system,
  light,
  dark;
}

// ── Domain models ─────────────────────────────────────────────────────────────

@freezed
class MealPlanConfig with _$MealPlanConfig {
  const factory MealPlanConfig({
    required MealScheduleConfig schedule,
    required NutritionGoals goals,
    required DietPreferences diet,
    required VarietyConfig variety,
    required List<OptimizerRule> rules,
    required ShoppingConfig shopping,
    ProteinPowder? proteinPowder,
  }) = _MealPlanConfig;

  factory MealPlanConfig.defaults() => MealPlanConfig(
        schedule: MealScheduleConfig.defaults(),
        goals: const NutritionGoals(kcalTarget: 1450.0, autoField: MacroField.protein),
        diet: const DietPreferences(),
        variety: const VarietyConfig(),
        rules: const [],
        shopping: ShoppingConfig.defaults(),
      );
}

// ── Schedule ──────────────────────────────────────────────────────────────────

@freezed
class MealScheduleConfig with _$MealScheduleConfig {
  const factory MealScheduleConfig({
    /// Key is ISO weekday: 1=Monday … 7=Sunday
    required Map<int, DaySlotConfig> perDay,
    @Default(<BatchCookingGroup>[]) List<BatchCookingGroup> batchCookingGroups,
    @Default(true) bool snackOptionalFill,
  }) = _MealScheduleConfig;

  factory MealScheduleConfig.defaults() => MealScheduleConfig(
        perDay: {
          for (int d = 1; d <= 7; d++)
            d: DaySlotConfig(
              hasBreakfast: false,
              hasLunch: true,
              hasDinner: true,
              snackCount: (d == 5 || d == 6) ? 1 : 0,
            ),
        },
      );
}

@freezed
class DaySlotConfig with _$DaySlotConfig {
  const factory DaySlotConfig({
    @Default(false) bool hasBreakfast,
    @Default(true) bool hasLunch,
    @Default(true) bool hasDinner,
    @Default(0) int snackCount,
  }) = _DaySlotConfig;
}

@freezed
class BatchCookingGroup with _$BatchCookingGroup {
  const factory BatchCookingGroup({
    required MealCategory category,
    /// ISO day numbers: 1=Monday … 7=Sunday
    required Set<int> days,
    required int batchNumber,
  }) = _BatchCookingGroup;
}

// ── Nutrition goals ───────────────────────────────────────────────────────────

@freezed
class NutritionGoals with _$NutritionGoals {
  const factory NutritionGoals({
    required double kcalTarget,
    double? proteinTarget,
    double? fatTarget,
    double? carbsTarget,
    @Default(MacroField.protein) MacroField autoField,
    double? minKcalPerDay,
    double? maxKcalPerDay,
    double? minProteinPerDay,
    double? maxProteinPerDay,
    double? minFatPerDay,
    double? maxFatPerDay,
    double? minCarbsPerDay,
    double? maxCarbsPerDay,
  }) = _NutritionGoals;

  const NutritionGoals._();

  double get resolvedProtein => proteinTarget ??
      ((kcalTarget - (fatTarget ?? 0.0) * 9 - (carbsTarget ?? 0.0) * 4) / 4)
          .clamp(0.0, double.infinity);

  double get resolvedFat => fatTarget ??
      ((kcalTarget - (proteinTarget ?? 0.0) * 4 - (carbsTarget ?? 0.0) * 4) / 9)
          .clamp(0.0, double.infinity);

  double get resolvedCarbs => carbsTarget ??
      ((kcalTarget - (proteinTarget ?? 0.0) * 4 - (fatTarget ?? 0.0) * 9) / 4)
          .clamp(0.0, double.infinity);
}

// ── Diet preferences ──────────────────────────────────────────────────────────

@freezed
class DietPreferences with _$DietPreferences {
  const factory DietPreferences({
    @Default(<DietType>{}) Set<DietType> dietTypes,
    @Default(<AllergyType>{}) Set<AllergyType> allergies,
    @Default(<String>{}) Set<String> excludedIngredientIds,
    @Default(<String>{}) Set<String> preferredIngredientIds,
    @Default(<String>{}) Set<String> dislikedIngredientIds,
  }) = _DietPreferences;
}

// ── Variety ───────────────────────────────────────────────────────────────────

@freezed
class MealCategoryVariety with _$MealCategoryVariety {
  const factory MealCategoryVariety({
    int? maxTimesPerWeek,
    int? maxConsecutiveDays,
  }) = _MealCategoryVariety;
}

@freezed
class VarietyConfig with _$VarietyConfig {
  const factory VarietyConfig({
    @Default(VarietyLevel.balanced) VarietyLevel level,
    Map<MealCategory, MealCategoryVariety>? perCategory,
    @Default(true) bool lunchDinnerSharedRecency,
    @Default(false) bool breakfastSnackSharedRecency,
    @Default(true) bool lunchDinnerMustDiffer,
    @Default(true) bool proteinSourceVariety,
  }) = _VarietyConfig;

  const VarietyConfig._();

  Map<MealCategory, MealCategoryVariety> get resolvedPerCategory =>
      perCategory ?? _defaultPerCategory;

  List<Set<MealCategory>> get recencyGroups => [
        if (lunchDinnerSharedRecency)
          {MealCategory.lunch, MealCategory.dinner}
        else ...[
          {MealCategory.lunch},
          {MealCategory.dinner},
        ],
        if (breakfastSnackSharedRecency)
          {MealCategory.breakfast, MealCategory.snack}
        else ...[
          {MealCategory.breakfast},
          {MealCategory.snack},
        ],
      ];
}

Map<MealCategory, MealCategoryVariety> get _defaultPerCategory => {
      MealCategory.breakfast: const MealCategoryVariety(),
      MealCategory.lunch:
          const MealCategoryVariety(maxTimesPerWeek: 3, maxConsecutiveDays: 2),
      MealCategory.dinner:
          const MealCategoryVariety(maxTimesPerWeek: 2, maxConsecutiveDays: 2),
      MealCategory.snack: const MealCategoryVariety(maxTimesPerWeek: 3),
    };

// ── Optimizer rules ───────────────────────────────────────────────────────────

@freezed
class OptimizerRule with _$OptimizerRule {
  const factory OptimizerRule({
    @Default('') String id,
    required RuleTargetType type,
    required String target,
    required String targetName,
    required ConstraintType constraint,
    required int value,
  }) = _OptimizerRule;
}

// ── Shopping config ───────────────────────────────────────────────────────────

@freezed
class ShoppingConfig with _$ShoppingConfig {
  const factory ShoppingConfig({
    required Set<int> shoppingDays,
    @Default(1) int intervalWeeks,
  }) = _ShoppingConfig;

  factory ShoppingConfig.defaults() =>
      const ShoppingConfig(shoppingDays: {7}); // Sunday = 7
}

// ── Protein powder ────────────────────────────────────────────────────────────

@freezed
class ProteinPowder with _$ProteinPowder {
  const factory ProteinPowder({
    required String ingredientId,
    required String name,
    required double proteinPer100g,
    required double kcalPer100g,
    required double gramsInStock,
    @Default(true) bool autoFillGap,
    @Default(true) bool lowStockWarning,
  }) = _ProteinPowder;

  const ProteinPowder._();

  double get daysRemaining => gramsInStock / 25.0;
}
