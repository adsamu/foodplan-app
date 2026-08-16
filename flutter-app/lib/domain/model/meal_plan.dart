import 'package:freezed_annotation/freezed_annotation.dart';
import 'recipe.dart';

part 'meal_plan.freezed.dart';

// ── Domain models ─────────────────────────────────────────────────────────────

@freezed
class MealPlan with _$MealPlan {
  const factory MealPlan({
    required String id,
    required String name,
    required DateTime startDate,
    required DateTime endDate,
    @Default(<DayPlan>[]) List<DayPlan> days,
  }) = _MealPlan;
}

@freezed
class DayPlan with _$DayPlan {
  const factory DayPlan({
    required String id,
    required DateTime date,
    @Default(<MealSlot>[]) List<MealSlot> meals,
    @Default(0.0) double proteinPowderGrams,
    @Default(DailyGoal()) DailyGoal goal,
  }) = _DayPlan;
}

@freezed
class MealSlot with _$MealSlot {
  const factory MealSlot({
    required MealCategory type,
    required String recipeId,
  }) = _MealSlot;
}

@freezed
class DailyGoal with _$DailyGoal {
  const factory DailyGoal({
    @Default(1350.0) double kcalTarget,
    @Default(120.0) double proteinTarget,
  }) = _DailyGoal;
}
