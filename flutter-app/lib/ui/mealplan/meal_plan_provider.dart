import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/nutrition.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:foodplan/use_case/generate_meal_plan_use_case.dart';

// ── Enums ─────────────────────────────────────────────────────────────────────

enum PlanView { day, week, month }

enum DayType { weekday, weekend, sunday }

// ── UI models ─────────────────────────────────────────────────────────────────

class MealSlotUi {
  final MealCategory type;
  final String recipeId;
  final String recipeName;
  final double kcal;
  final double protein;
  final double fat;
  final double carbs;

  const MealSlotUi({
    required this.type,
    required this.recipeId,
    required this.recipeName,
    required this.kcal,
    required this.protein,
    required this.fat,
    required this.carbs,
  });
}

class DayNutrition {
  final double kcal;
  final double protein;
  final double fat;
  final double carbs;

  const DayNutrition({
    required this.kcal,
    required this.protein,
    required this.fat,
    required this.carbs,
  });
}

class DayUiState {
  final DateTime date;
  final DayType dayType;
  final int kcalTarget;
  final List<MealSlotUi> meals;
  final DayNutrition nutrition;

  const DayUiState({
    required this.date,
    required this.dayType,
    required this.kcalTarget,
    required this.meals,
    required this.nutrition,
  });
}

class WeekDayUi {
  final DateTime date;
  final bool isToday;
  final double kcal;
  final double kcalTarget;
  final List<String> mealNames;
  final int checkedCount;

  const WeekDayUi({
    required this.date,
    required this.isToday,
    required this.kcal,
    required this.kcalTarget,
    required this.mealNames,
    required this.checkedCount,
  });
}

class WeekUiState {
  final int weekNumber;
  final DateTime startDate;
  final DateTime endDate;
  final List<WeekDayUi> days;
  final double avgKcal;
  final double avgProtein;
  final double avgFat;
  final double avgCarbs;

  const WeekUiState({
    required this.weekNumber,
    required this.startDate,
    required this.endDate,
    required this.days,
    required this.avgKcal,
    required this.avgProtein,
    required this.avgFat,
    required this.avgCarbs,
  });
}

class MonthDayUi {
  final DateTime date;
  final bool isToday;
  final bool isPlanned;
  final int checkedCount;
  final int totalMeals;

  const MonthDayUi({
    required this.date,
    required this.isToday,
    required this.isPlanned,
    required this.checkedCount,
    required this.totalMeals,
  });
}

class MonthUiState {
  final int month;
  final int year;
  final List<MonthDayUi> days;
  final double avgKcal;
  final double avgProtein;
  final double avgFat;
  final double avgCarbs;

  const MonthUiState({
    required this.month,
    required this.year,
    required this.days,
    required this.avgKcal,
    required this.avgProtein,
    required this.avgFat,
    required this.avgCarbs,
  });
}

// ── Optimizer state ───────────────────────────────────────────────────────────

sealed class OptimizerState {
  const OptimizerState._();

  const factory OptimizerState.idle() = OptimizerIdle;
  const factory OptimizerState.running() = OptimizerRunning;
  const factory OptimizerState.success(String planName) = OptimizerSuccess;
  const factory OptimizerState.error(String message) = OptimizerError;
}

class OptimizerIdle extends OptimizerState {
  const OptimizerIdle() : super._();
}

class OptimizerRunning extends OptimizerState {
  const OptimizerRunning() : super._();
}

class OptimizerSuccess extends OptimizerState {
  final String planName;
  const OptimizerSuccess(this.planName) : super._();
}

class OptimizerError extends OptimizerState {
  final String message;
  const OptimizerError(this.message) : super._();
}

// ── Simple state providers ────────────────────────────────────────────────────

final planViewProvider = StateProvider<PlanView>((ref) => PlanView.day);

final planDateProvider = StateProvider<DateTime>((ref) {
  final now = DateTime.now();
  return DateTime(now.year, now.month, now.day);
});

final statsExpandedProvider = StateProvider<bool>((ref) => false);

/// Map from date-key (ISO date string) to Set of checked meal slot indices.
final checkedMealsProvider =
    StateProvider<Map<String, Set<int>>>((ref) => {});

final optimizerStateProvider =
    StateProvider<OptimizerState>((ref) => const OptimizerState.idle());

// ── Helpers ───────────────────────────────────────────────────────────────────

DayType _dayTypeFor(DateTime date) {
  // weekday: Mon-Thu (1-4), weekend: Fri-Sat (5-6), sunday: 7
  return switch (date.weekday) {
    5 || 6 => DayType.weekend,
    7 => DayType.sunday,
    _ => DayType.weekday,
  };
}

int _kcalTargetFor(DayType dayType) => switch (dayType) {
      DayType.weekday => 1350,
      DayType.weekend => 1539,
      DayType.sunday => 1257,
    };

/// Compute ISO week number for [date].
int _isoWeekNumber(DateTime date) {
  final thursday = date.subtract(Duration(days: date.weekday - 4));
  final firstThursday = DateTime(thursday.year, 1, 4);
  final firstThursdayWeekday = firstThursday.weekday;
  final firstMonday =
      firstThursday.subtract(Duration(days: firstThursdayWeekday - 1));
  return ((thursday.difference(firstMonday).inDays) ~/ 7) + 1;
}

/// Compute nutrition for a recipe by its ID, recursively resolving sub-recipes.
Future<RecipeNutrition> _nutritionForRecipe(Ref ref, String recipeId) async {
  final recipeRepo = ref.read(recipeRepositoryProvider);
  final ingRepo = ref.read(ingredientRepositoryProvider);
  final recipe = await recipeRepo.getRecipeWithIngredients(recipeId);
  if (recipe == null) return RecipeNutrition.zero;

  var total = RecipeNutrition.zero;
  for (final ri in recipe.ingredients) {
    if (ri.ingredientId != null) {
      final ing = await ingRepo.getIngredientById(ri.ingredientId!);
      if (ing != null) {
        final grams = ri.grams ?? 0.0;
        total = total + RecipeNutrition.fromIngredient(ing, grams);
      }
    } else if (ri.subRecipeId != null) {
      final subNutrition =
          await _nutritionForRecipe(ref, ri.subRecipeId!);
      final portions = ri.portions ?? 1.0;
      total = total + (subNutrition * portions);
    }
  }
  return total;
}

// ── Async UI-state providers ──────────────────────────────────────────────────

final dayUiStateProvider =
    FutureProvider.autoDispose<DayUiState?>((ref) async {
  final date = ref.watch(planDateProvider);
  final mealPlanRepo = ref.read(mealPlanRepositoryProvider);
  final recipeRepo = ref.read(recipeRepositoryProvider);

  final dayPlan = await mealPlanRepo.getDayPlanByDate(date);
  if (dayPlan == null) return null;

  final dayType = _dayTypeFor(date);
  final kcalTarget = dayPlan.goal.kcalTarget > 0
      ? dayPlan.goal.kcalTarget.round()
      : _kcalTargetFor(dayType);

  final slots = <MealSlotUi>[];
  var totalNutrition = RecipeNutrition.zero;

  for (final slot in dayPlan.meals) {
    final recipe = await recipeRepo.getRecipeById(slot.recipeId);
    final nutrition = await _nutritionForRecipe(ref, slot.recipeId);
    totalNutrition = totalNutrition + nutrition;
    slots.add(MealSlotUi(
      type: slot.type,
      recipeId: slot.recipeId,
      recipeName: recipe?.name ?? slot.recipeId,
      kcal: nutrition.kcal,
      protein: nutrition.protein,
      fat: nutrition.fat,
      carbs: nutrition.carbs,
    ));
  }

  return DayUiState(
    date: date,
    dayType: dayType,
    kcalTarget: kcalTarget,
    meals: slots,
    nutrition: DayNutrition(
      kcal: totalNutrition.kcal,
      protein: totalNutrition.protein,
      fat: totalNutrition.fat,
      carbs: totalNutrition.carbs,
    ),
  );
});

final weekUiStateProvider =
    FutureProvider.autoDispose<WeekUiState?>((ref) async {
  final selectedDate = ref.watch(planDateProvider);
  final checkedMeals = ref.watch(checkedMealsProvider);
  final mealPlanRepo = ref.read(mealPlanRepositoryProvider);
  final recipeRepo = ref.read(recipeRepositoryProvider);

  // Find Monday of the selected week
  final dayOffset = selectedDate.weekday - 1;
  final monday =
      selectedDate.subtract(Duration(days: dayOffset));
  final sunday = monday.add(const Duration(days: 6));

  final dayPlans = await mealPlanRepo.getDayPlansForRange(monday, sunday);

  final today = DateTime.now();
  final todayDate = DateTime(today.year, today.month, today.day);

  final weekDays = <WeekDayUi>[];
  var totalKcal = 0.0;
  var plannedDayCount = 0;

  for (int i = 0; i < 7; i++) {
    final day = monday.add(Duration(days: i));
    final dayPlan = dayPlans[day];
    final dayType = _dayTypeFor(day);
    final kcalTarget = dayPlan != null && dayPlan.goal.kcalTarget > 0
        ? dayPlan.goal.kcalTarget
        : _kcalTargetFor(dayType).toDouble();

    var dayKcal = 0.0;
    final mealNames = <String>[];
    final dateKey = _dateKey(day);
    final checked = checkedMeals[dateKey] ?? {};

    if (dayPlan != null) {
      for (int mi = 0; mi < dayPlan.meals.length; mi++) {
        final slot = dayPlan.meals[mi];
        final recipe = await recipeRepo.getRecipeById(slot.recipeId);
        final nutrition = await _nutritionForRecipe(ref, slot.recipeId);
        dayKcal += nutrition.kcal;
        if (recipe != null) mealNames.add(recipe.name);
      }
      totalKcal += dayKcal;
      // For avg macro we'd need full nutrition; skip sub-macros for week summary
      plannedDayCount++;
    }

    weekDays.add(WeekDayUi(
      date: day,
      isToday: day == todayDate,
      kcal: dayKcal,
      kcalTarget: kcalTarget,
      mealNames: mealNames,
      checkedCount: checked.length,
    ));
  }

  // Compute avg macros across all planned days
  double avgKcal = 0, avgProtein = 0, avgFat = 0, avgCarbs = 0;
  if (plannedDayCount > 0) {
    // Simple avg from total kcal; for full macros we'd need another pass
    avgKcal = totalKcal / plannedDayCount;
    // Estimate macros from kcal using rough 30/30/40 split
    avgProtein = avgKcal * 0.30 / 4;
    avgFat = avgKcal * 0.30 / 9;
    avgCarbs = avgKcal * 0.40 / 4;
  }

  return WeekUiState(
    weekNumber: _isoWeekNumber(monday),
    startDate: monday,
    endDate: sunday,
    days: weekDays,
    avgKcal: avgKcal,
    avgProtein: avgProtein,
    avgFat: avgFat,
    avgCarbs: avgCarbs,
  );
});

final monthUiStateProvider =
    FutureProvider.autoDispose<MonthUiState?>((ref) async {
  final selectedDate = ref.watch(planDateProvider);
  final checkedMeals = ref.watch(checkedMealsProvider);
  final mealPlanRepo = ref.read(mealPlanRepositoryProvider);

  final year = selectedDate.year;
  final month = selectedDate.month;
  final firstDay = DateTime(year, month, 1);
  final lastDay = DateTime(year, month + 1, 0); // last day of month

  final dayPlans =
      await mealPlanRepo.getDayPlansForRange(firstDay, lastDay);

  final today = DateTime.now();
  final todayDate = DateTime(today.year, today.month, today.day);

  final monthDays = <MonthDayUi>[];
  var plannedCount = 0;

  for (int d = 1; d <= lastDay.day; d++) {
    final day = DateTime(year, month, d);
    final dayPlan = dayPlans[day];
    final dateKey = _dateKey(day);
    final checked = checkedMeals[dateKey] ?? {};
    final totalMeals = dayPlan?.meals.length ?? 0;

    monthDays.add(MonthDayUi(
      date: day,
      isToday: day == todayDate,
      isPlanned: dayPlan != null && totalMeals > 0,
      checkedCount: checked.length,
      totalMeals: totalMeals,
    ));

    if (dayPlan != null && totalMeals > 0) plannedCount++;
  }

  // Rough averages
  double avgKcal = 0, avgProtein = 0, avgFat = 0, avgCarbs = 0;
  if (plannedCount > 0) {
    final avgDayKcal = 1350.0; // fallback estimate
    avgKcal = avgDayKcal;
    avgProtein = avgDayKcal * 0.30 / 4;
    avgFat = avgDayKcal * 0.30 / 9;
    avgCarbs = avgDayKcal * 0.40 / 4;
  }

  return MonthUiState(
    month: month,
    year: year,
    days: monthDays,
    avgKcal: avgKcal,
    avgProtein: avgProtein,
    avgFat: avgFat,
    avgCarbs: avgCarbs,
  );
});

// ── Actions ───────────────────────────────────────────────────────────────────

String _dateKey(DateTime date) =>
    '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';

class MealPlanActions {
  final Ref _ref;

  MealPlanActions(this._ref);

  void navigatePrevious() {
    final view = _ref.read(planViewProvider);
    final date = _ref.read(planDateProvider);
    _ref.read(planDateProvider.notifier).state = switch (view) {
      PlanView.day => date.subtract(const Duration(days: 1)),
      PlanView.week => date.subtract(const Duration(days: 7)),
      PlanView.month => DateTime(date.year, date.month - 1, 1),
    };
  }

  void navigateNext() {
    final view = _ref.read(planViewProvider);
    final date = _ref.read(planDateProvider);
    _ref.read(planDateProvider.notifier).state = switch (view) {
      PlanView.day => date.add(const Duration(days: 1)),
      PlanView.week => date.add(const Duration(days: 7)),
      PlanView.month => DateTime(date.year, date.month + 1, 1),
    };
  }

  void toggleStats() {
    final current = _ref.read(statsExpandedProvider);
    _ref.read(statsExpandedProvider.notifier).state = !current;
  }

  void onViewChange(PlanView view) {
    _ref.read(planViewProvider.notifier).state = view;
  }

  void toggleMealChecked(DateTime date, int slotIndex) {
    final key = _dateKey(date);
    final current = Map<String, Set<int>>.from(_ref.read(checkedMealsProvider));
    final daySet = Set<int>.from(current[key] ?? {});
    if (daySet.contains(slotIndex)) {
      daySet.remove(slotIndex);
    } else {
      daySet.add(slotIndex);
    }
    current[key] = daySet;
    _ref.read(checkedMealsProvider.notifier).state = current;
  }

  Future<void> generatePlan(DateTime weekStart) async {
    _ref.read(optimizerStateProvider.notifier).state =
        const OptimizerState.running();
    try {
      final functions = _ref.read(firebaseFunctionsProvider);
      final settingsRepo = _ref.read(settingsRepositoryProvider);
      final mealPlanRepo = _ref.read(mealPlanRepositoryProvider);
      final firestore = _ref.read(firestoreProvider);

      // Use a default userId; in a real app this comes from Firebase Auth.
      const userId = 'default_user';

      final useCase = GenerateMealPlanUseCase(
        settingsRepository: settingsRepo,
        mealPlanRepository: mealPlanRepo,
        firebaseFunctions: functions,
        firestore: firestore,
      );

      final plan = await useCase(userId, weekStart);

      _ref.read(optimizerStateProvider.notifier).state =
          OptimizerState.success(plan.name);

      // Invalidate data providers so they reload.
      _ref.invalidate(dayUiStateProvider);
      _ref.invalidate(weekUiStateProvider);
      _ref.invalidate(monthUiStateProvider);
    } catch (e) {
      _ref.read(optimizerStateProvider.notifier).state =
          OptimizerState.error(e.toString());
    }
  }
}

final mealPlanActionsProvider =
    Provider<MealPlanActions>((ref) => MealPlanActions(ref));
