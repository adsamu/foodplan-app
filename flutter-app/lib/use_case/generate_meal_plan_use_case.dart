import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:cloud_functions/cloud_functions.dart';
import 'package:foodplan/data/repository/meal_plan_repository.dart';
import 'package:foodplan/data/repository/settings_repository.dart';
import 'package:foodplan/domain/model/meal_plan.dart';
import 'package:foodplan/domain/model/meal_plan_config.dart';

/// Mirrors the Kotlin GenerateMealPlanUseCase + SettingsSync.pushNow flow.
///
/// 1. Pushes the current [MealPlanConfig] to Firestore (users/{userId}/settings/main)
///    so the Cloud Function reads fresh settings.
/// 2. Calls the `optimise_meal_plan` Firebase Function.
/// 3. Waits briefly for the Firestore listener to mirror the returned plan
///    to the local DB, then fetches and returns it.
class GenerateMealPlanUseCase {
  GenerateMealPlanUseCase({
    required SettingsRepository settingsRepository,
    required MealPlanRepository mealPlanRepository,
    required FirebaseFunctions firebaseFunctions,
    required FirebaseFirestore firestore,
  })  : _settingsRepository = settingsRepository,
        _mealPlanRepository = mealPlanRepository,
        _functions = firebaseFunctions,
        _firestore = firestore;

  final SettingsRepository _settingsRepository;
  final MealPlanRepository _mealPlanRepository;
  final FirebaseFunctions _functions;
  final FirebaseFirestore _firestore;

  Future<MealPlan> call(String userId, DateTime startDate) async {
    // 1. Push settings to Firestore so the function reads the latest config.
    await _pushSettingsToFirestore(userId);

    // 2. Call the Cloud Function.
    final startDateStr =
        '${startDate.year.toString().padLeft(4, '0')}'
        '-${startDate.month.toString().padLeft(2, '0')}'
        '-${startDate.day.toString().padLeft(2, '0')}';

    final result = await _functions
        .httpsCallable('optimise_meal_plan')
        .call(<String, dynamic>{
      'userId': userId,
      'startDate': startDateStr,
    });

    final planId = (result.data as Map)['planId'] as String;

    // 3. Wait briefly for the Firestore listener to mirror the plan to local DB.
    await Future<void>.delayed(const Duration(seconds: 2));

    final plan = await _mealPlanRepository.getMealPlanWithDays(planId);
    if (plan == null) {
      throw Exception('Plan not found after generation (planId=$planId)');
    }
    return plan;
  }

  Future<void> _pushSettingsToFirestore(String userId) async {
    final config = await _settingsRepository.getMealPlanConfig();
    await _firestore
        .collection('users')
        .doc(userId)
        .collection('settings')
        .doc('main')
        .set(_configToFirestoreMap(config));
  }

  Map<String, dynamic> _configToFirestoreMap(MealPlanConfig config) => {
        'schedule': {
          'mealSlots': {
            for (final entry in config.schedule.perDay.entries)
              _isoDayName(entry.key): {
                'breakfast': entry.value.hasBreakfast,
                'lunch': entry.value.hasLunch,
                'dinner': entry.value.hasDinner,
                'snackCount': entry.value.snackCount,
              },
          },
          'batchGroups': config.schedule.batchCookingGroups
              .map((bg) => {
                    'meal': bg.category.firestoreName,
                    'days': bg.days.map(_isoDayName).toList(),
                    'batchNumber': bg.batchNumber,
                  })
              .toList(),
          'snackOptionalFill': config.schedule.snackOptionalFill,
        },
        'goals': {
          'kcalTarget': config.goals.kcalTarget,
          'proteinTarget': config.goals.proteinTarget,
          'fatTarget': config.goals.fatTarget,
          'carbsTarget': config.goals.carbsTarget,
          'autoField': config.goals.autoField.firestoreName,
          'minKcalPerDay': config.goals.minKcalPerDay,
          'maxKcalPerDay': config.goals.maxKcalPerDay,
          'minProteinPerDay': config.goals.minProteinPerDay,
          'maxProteinPerDay': config.goals.maxProteinPerDay,
          'minFatPerDay': config.goals.minFatPerDay,
          'maxFatPerDay': config.goals.maxFatPerDay,
          'minCarbsPerDay': config.goals.minCarbsPerDay,
          'maxCarbsPerDay': config.goals.maxCarbsPerDay,
        },
        'diet': {
          'dietTypes':
              config.diet.dietTypes.map((t) => t.firestoreName).toList(),
          'allergies':
              config.diet.allergies.map((a) => a.firestoreName).toList(),
          'excludedIngredientIds':
              config.diet.excludedIngredientIds.toList(),
          'preferredIngredientIds':
              config.diet.preferredIngredientIds.toList(),
          'dislikedIngredientIds':
              config.diet.dislikedIngredientIds.toList(),
        },
        'variety': {
          'level': config.variety.level.firestoreName,
          'lunchDinnerSharedRecency': config.variety.lunchDinnerSharedRecency,
          'breakfastSnackSharedRecency':
              config.variety.breakfastSnackSharedRecency,
          'lunchDinnerMustDiffer': config.variety.lunchDinnerMustDiffer,
          'proteinSourceVariety': config.variety.proteinSourceVariety,
          'perCategory': {
            for (final entry
                in (config.variety.perCategory ?? config.variety.resolvedPerCategory).entries)
              entry.key.firestoreName: {
                'maxTimesPerWeek': entry.value.maxTimesPerWeek,
                'maxConsecutiveDays': entry.value.maxConsecutiveDays,
              },
          },
        },
        'proteinPowder': config.proteinPowder == null
            ? null
            : {
                'ingredientId': config.proteinPowder!.ingredientId,
                'name': config.proteinPowder!.name,
                'proteinPer100g': config.proteinPowder!.proteinPer100g,
                'kcalPer100g': config.proteinPowder!.kcalPer100g,
                'gramsInStock': config.proteinPowder!.gramsInStock,
                'autoFillGap': config.proteinPowder!.autoFillGap,
                'lowStockWarning': config.proteinPowder!.lowStockWarning,
              },
        'shopping': {
          'shoppingDays':
              config.shopping.shoppingDays.map(_isoDayName).toList(),
          'intervalWeeks': config.shopping.intervalWeeks,
        },
        'rules': config.rules
            .map((r) => {
                  'id': r.id,
                  'type': r.type.firestoreName,
                  'target': r.target,
                  'targetName': r.targetName,
                  'constraint': r.constraint.firestoreName,
                  'value': r.value,
                })
            .toList(),
      };

  /// ISO day number → DayOfWeek name matching Kotlin's DayOfWeek.name
  String _isoDayName(int isoDay) => switch (isoDay) {
        1 => 'MONDAY',
        2 => 'TUESDAY',
        3 => 'WEDNESDAY',
        4 => 'THURSDAY',
        5 => 'FRIDAY',
        6 => 'SATURDAY',
        7 => 'SUNDAY',
        _ => 'MONDAY',
      };
}
