import 'dart:async';

import 'package:drift/drift.dart';
import 'package:foodplan/data/local/app_database.dart';
import 'package:foodplan/domain/model/meal_plan_config.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

// ── SharedPreferences keys ─────────────────────────────────────────────────

const _kKcalTarget = 'kcal_target';
const _kProteinTarget = 'protein_target';
const _kFatTarget = 'fat_target';
const _kCarbsTarget = 'carbs_target';
const _kAutoField = 'auto_macro_field';
const _kMinKcal = 'min_kcal_per_day';
const _kMaxKcal = 'max_kcal_per_day';
const _kMinProtein = 'min_protein_per_day';
const _kMaxProtein = 'max_protein_per_day';
const _kMinFat = 'min_fat_per_day';
const _kMaxFat = 'max_fat_per_day';
const _kMinCarbs = 'min_carbs_per_day';
const _kMaxCarbs = 'max_carbs_per_day';
const _kDietTypes = 'diet_types';
const _kAllergies = 'allergies';
const _kExcludedIds = 'excluded_ingredient_ids';
const _kPreferredIds = 'preferred_ingredient_ids';
const _kDislikedIds = 'disliked_ingredient_ids';
const _kVarietyLevel = 'variety_level';
const _kVarietyLunchDinnerShared = 'variety_lunch_dinner_shared';
const _kVarietyBreakfastSnackShared = 'variety_breakfast_snack_shared';
const _kVarietyLunchDinnerMustDiffer = 'variety_lunch_dinner_must_differ';
const _kVarietyProteinSource = 'protein_source_variety';
const _kVarietyBreakfastMaxWeek = 'variety_breakfast_max_week';
const _kVarietyBreakfastMaxConsec = 'variety_breakfast_max_consec';
const _kVarietyLunchMaxWeek = 'variety_lunch_max_week';
const _kVarietyLunchMaxConsec = 'variety_lunch_max_consec';
const _kVarietyDinnerMaxWeek = 'variety_dinner_max_week';
const _kVarietyDinnerMaxConsec = 'variety_dinner_max_consec';
const _kVarietySnackMaxWeek = 'variety_snack_max_week';
const _kVarietySnackMaxConsec = 'variety_snack_max_consec';
const _kSnackOptionalFill = 'snack_optional_fill';
const _kShoppingDays = 'shopping_days';
const _kShoppingInterval = 'shopping_interval_weeks';
const _kPowderIngredientId = 'powder_ingredient_id';
const _kPowderName = 'powder_name';
const _kPowderProteinPer100g = 'powder_protein_per_100g';
const _kPowderKcalPer100g = 'powder_kcal_per_100g';
const _kPowderGramsInStock = 'powder_grams_in_stock';
const _kPowderAutoFill = 'powder_auto_fill';
const _kPowderLowStockWarning = 'powder_low_stock_warning';
const _kTheme = 'app_theme';
const _kUnlimited = -1;

class SettingsRepository {
  SettingsRepository({
    required SharedPreferences prefs,
    required SettingsDao dao,
  })  : _prefs = prefs,
        _dao = dao;

  final SharedPreferences _prefs;
  final SettingsDao _dao;
  final _uuid = const Uuid();

  // ── Config assembly ───────────────────────────────────────────────────────

  Future<MealPlanConfig> getMealPlanConfig() async {
    final slotRows = await _dao.getAllMealSlotConfigsOnce();
    final batchRows = await _dao.getAllBatchCookingGroupsOnce();
    final ruleRows = await _dao.getAllOptimizerRulesOnce();
    return MealPlanConfig(
      schedule: _buildScheduleConfig(slotRows, batchRows),
      goals: _buildNutritionGoals(),
      diet: _buildDietPreferences(),
      variety: _buildVarietyConfig(),
      rules: ruleRows.map(_optimizerRuleFromRow).toList(),
      proteinPowder: _buildProteinPowder(),
      shopping: _buildShoppingConfig(),
    );
  }

  /// Stream that emits a new [MealPlanConfig] whenever prefs or DB tables change.
  Stream<MealPlanConfig> watchMealPlanConfig() {
    // Combine the three DB streams; prefs changes are handled by the broadcast
    // controller that is triggered on every setter call.
    final controller = StreamController<MealPlanConfig>.broadcast();

    // Emit on DB changes
    late StreamSubscription slotSub;
    late StreamSubscription batchSub;
    late StreamSubscription ruleSub;

    Future<void> emit() async {
      try {
        final config = await getMealPlanConfig();
        if (!controller.isClosed) controller.add(config);
      } catch (e) {
        if (!controller.isClosed) controller.addError(e);
      }
    }

    slotSub = _dao.watchMealSlotConfigs().listen((_) => emit());
    batchSub = _dao.watchBatchCookingGroups().listen((_) => emit());
    ruleSub = _dao.watchOptimizerRules().listen((_) => emit());

    // Emit immediately
    emit();

    controller.onCancel = () {
      slotSub.cancel();
      batchSub.cancel();
      ruleSub.cancel();
    };

    return controller.stream;
  }

  // ── Builders ──────────────────────────────────────────────────────────────

  MealScheduleConfig _buildScheduleConfig(
    List<MealSlotConfigsTableData> slots,
    List<BatchCookingGroupsTableData> batches,
  ) {
    final slotMap = {
      for (final row in slots)
        row.dayOfWeek: DaySlotConfig(
          hasBreakfast: row.hasBreakfast,
          hasLunch: row.hasLunch,
          hasDinner: row.hasDinner,
          snackCount: row.snackCount,
        ),
    };
    // Fill in defaults for missing days
    final perDay = {
      for (int d = 1; d <= 7; d++)
        d: slotMap[d] ??
            DaySlotConfig(
              hasBreakfast: false,
              hasLunch: true,
              hasDinner: true,
              snackCount: (d == 5 || d == 6) ? 1 : 0,
            ),
    };
    return MealScheduleConfig(
      perDay: perDay,
      batchCookingGroups: batches
          .map((row) => BatchCookingGroup(
                category: MealCategory.fromFirestore(row.category) ??
                    MealCategory.lunch,
                days: row.days.isEmpty
                    ? {}
                    : row.days
                        .split(',')
                        .map((s) => int.tryParse(s.trim()) ?? 1)
                        .toSet(),
                batchNumber: row.batchNumber,
              ))
          .toList(),
      snackOptionalFill: _prefs.getBool(_kSnackOptionalFill) ?? true,
    );
  }

  NutritionGoals _buildNutritionGoals() => NutritionGoals(
        kcalTarget: _prefs.getDouble(_kKcalTarget) ?? 1450.0,
        proteinTarget: _prefs.getDouble(_kProteinTarget),
        fatTarget: _prefs.getDouble(_kFatTarget),
        carbsTarget: _prefs.getDouble(_kCarbsTarget),
        autoField:
            MacroField.fromFirestore(_prefs.getString(_kAutoField)),
        minKcalPerDay: _prefs.getDouble(_kMinKcal),
        maxKcalPerDay: _prefs.getDouble(_kMaxKcal),
        minProteinPerDay: _prefs.getDouble(_kMinProtein),
        maxProteinPerDay: _prefs.getDouble(_kMaxProtein),
        minFatPerDay: _prefs.getDouble(_kMinFat),
        maxFatPerDay: _prefs.getDouble(_kMaxFat),
        minCarbsPerDay: _prefs.getDouble(_kMinCarbs),
        maxCarbsPerDay: _prefs.getDouble(_kMaxCarbs),
      );

  DietPreferences _buildDietPreferences() => DietPreferences(
        dietTypes: _splitToSet(_prefs.getString(_kDietTypes))
            .map((s) {
              try {
                return DietType.values.firstWhere(
                  (e) => e.firestoreName == s.toUpperCase(),
                );
              } catch (_) {
                return null;
              }
            })
            .whereType<DietType>()
            .toSet(),
        allergies: _splitToSet(_prefs.getString(_kAllergies))
            .map((s) {
              try {
                return AllergyType.values.firstWhere(
                  (e) => e.firestoreName == s.toUpperCase(),
                );
              } catch (_) {
                return null;
              }
            })
            .whereType<AllergyType>()
            .toSet(),
        excludedIngredientIds: _splitToSet(_prefs.getString(_kExcludedIds)),
        preferredIngredientIds:
            _splitToSet(_prefs.getString(_kPreferredIds)),
        dislikedIngredientIds: _splitToSet(_prefs.getString(_kDislikedIds)),
      );

  VarietyConfig _buildVarietyConfig() {
    int? toLimit(String key) {
      final v = _prefs.getInt(key);
      return (v == null || v == _kUnlimited) ? null : v;
    }

    return VarietyConfig(
      level: () {
        try {
          return VarietyLevel.values.firstWhere(
            (e) =>
                e.name.toUpperCase() ==
                (_prefs.getString(_kVarietyLevel) ?? '').toUpperCase(),
          );
        } catch (_) {
          return VarietyLevel.balanced;
        }
      }(),
      lunchDinnerSharedRecency:
          _prefs.getBool(_kVarietyLunchDinnerShared) ?? true,
      breakfastSnackSharedRecency:
          _prefs.getBool(_kVarietyBreakfastSnackShared) ?? false,
      lunchDinnerMustDiffer:
          _prefs.getBool(_kVarietyLunchDinnerMustDiffer) ?? true,
      proteinSourceVariety:
          _prefs.getBool(_kVarietyProteinSource) ?? true,
      perCategory: {
        MealCategory.breakfast: MealCategoryVariety(
          maxTimesPerWeek: toLimit(_kVarietyBreakfastMaxWeek),
          maxConsecutiveDays: toLimit(_kVarietyBreakfastMaxConsec),
        ),
        MealCategory.lunch: MealCategoryVariety(
          maxTimesPerWeek:
              _prefs.containsKey(_kVarietyLunchMaxWeek)
                  ? toLimit(_kVarietyLunchMaxWeek)
                  : 3,
          maxConsecutiveDays:
              _prefs.containsKey(_kVarietyLunchMaxConsec)
                  ? toLimit(_kVarietyLunchMaxConsec)
                  : 2,
        ),
        MealCategory.dinner: MealCategoryVariety(
          maxTimesPerWeek:
              _prefs.containsKey(_kVarietyDinnerMaxWeek)
                  ? toLimit(_kVarietyDinnerMaxWeek)
                  : 2,
          maxConsecutiveDays:
              _prefs.containsKey(_kVarietyDinnerMaxConsec)
                  ? toLimit(_kVarietyDinnerMaxConsec)
                  : 2,
        ),
        MealCategory.snack: MealCategoryVariety(
          maxTimesPerWeek:
              _prefs.containsKey(_kVarietySnackMaxWeek)
                  ? toLimit(_kVarietySnackMaxWeek)
                  : 3,
          maxConsecutiveDays: toLimit(_kVarietySnackMaxConsec),
        ),
      },
    );
  }

  ProteinPowder? _buildProteinPowder() {
    final id = _prefs.getString(_kPowderIngredientId);
    final name = _prefs.getString(_kPowderName);
    if (id == null || name == null) return null;
    return ProteinPowder(
      ingredientId: id,
      name: name,
      proteinPer100g: _prefs.getDouble(_kPowderProteinPer100g) ?? 0.0,
      kcalPer100g: _prefs.getDouble(_kPowderKcalPer100g) ?? 0.0,
      gramsInStock: _prefs.getDouble(_kPowderGramsInStock) ?? 0.0,
      autoFillGap: _prefs.getBool(_kPowderAutoFill) ?? true,
      lowStockWarning: _prefs.getBool(_kPowderLowStockWarning) ?? true,
    );
  }

  ShoppingConfig _buildShoppingConfig() => ShoppingConfig(
        shoppingDays: _splitToSet(_prefs.getString(_kShoppingDays))
            .map((s) => int.tryParse(s))
            .whereType<int>()
            .toSet()
            .let((s) => s.isEmpty ? {7} : s),
        intervalWeeks: _prefs.getInt(_kShoppingInterval) ?? 1,
      );

  // ── Individual setters ────────────────────────────────────────────────────

  Future<void> setKcalTarget(double kcal) =>
      _prefs.setDouble(_kKcalTarget, kcal);

  Future<void> setProteinTarget(double? g) => g != null
      ? _prefs.setDouble(_kProteinTarget, g)
      : _prefs.remove(_kProteinTarget);

  Future<void> setFatTarget(double? g) => g != null
      ? _prefs.setDouble(_kFatTarget, g)
      : _prefs.remove(_kFatTarget);

  Future<void> setCarbsTarget(double? g) => g != null
      ? _prefs.setDouble(_kCarbsTarget, g)
      : _prefs.remove(_kCarbsTarget);

  Future<void> setAutoField(MacroField field) =>
      _prefs.setString(_kAutoField, field.firestoreName);

  Future<void> setMinKcal(double? v) =>
      v != null ? _prefs.setDouble(_kMinKcal, v) : _prefs.remove(_kMinKcal);

  Future<void> setMaxKcal(double? v) =>
      v != null ? _prefs.setDouble(_kMaxKcal, v) : _prefs.remove(_kMaxKcal);

  Future<void> setMinProtein(double? v) => v != null
      ? _prefs.setDouble(_kMinProtein, v)
      : _prefs.remove(_kMinProtein);

  Future<void> setMaxProtein(double? v) => v != null
      ? _prefs.setDouble(_kMaxProtein, v)
      : _prefs.remove(_kMaxProtein);

  Future<void> setMinFat(double? v) =>
      v != null ? _prefs.setDouble(_kMinFat, v) : _prefs.remove(_kMinFat);

  Future<void> setMaxFat(double? v) =>
      v != null ? _prefs.setDouble(_kMaxFat, v) : _prefs.remove(_kMaxFat);

  Future<void> setMinCarbs(double? v) => v != null
      ? _prefs.setDouble(_kMinCarbs, v)
      : _prefs.remove(_kMinCarbs);

  Future<void> setMaxCarbs(double? v) => v != null
      ? _prefs.setDouble(_kMaxCarbs, v)
      : _prefs.remove(_kMaxCarbs);

  Future<void> setDietTypes(Set<DietType> types) =>
      _prefs.setString(_kDietTypes, types.map((t) => t.firestoreName).join(','));

  Future<void> toggleDietType(DietType type) async {
    final current = _buildDietPreferences().dietTypes;
    final updated =
        current.contains(type) ? current.difference({type}) : {...current, type};
    await setDietTypes(updated);
  }

  Future<void> setAllergies(Set<AllergyType> allergies) =>
      _prefs.setString(
          _kAllergies, allergies.map((a) => a.firestoreName).join(','));

  Future<void> toggleAllergy(AllergyType allergy) async {
    final current = _buildDietPreferences().allergies;
    final updated = current.contains(allergy)
        ? current.difference({allergy})
        : {...current, allergy};
    await setAllergies(updated);
  }

  Future<void> setExcludedIngredients(Set<String> ids) =>
      _prefs.setString(_kExcludedIds, ids.join(','));

  Future<void> setPreferredIngredients(Set<String> ids) =>
      _prefs.setString(_kPreferredIds, ids.join(','));

  Future<void> setDislikedIngredients(Set<String> ids) =>
      _prefs.setString(_kDislikedIds, ids.join(','));

  Future<void> setVarietyLevel(VarietyLevel level) =>
      _prefs.setString(_kVarietyLevel, level.firestoreName);

  Future<void> setSnackOptionalFill(bool enabled) =>
      _prefs.setBool(_kSnackOptionalFill, enabled);

  Future<void> setShoppingDays(Set<int> days) =>
      _prefs.setString(_kShoppingDays, days.join(','));

  Future<void> setShoppingInterval(int weeks) =>
      _prefs.setInt(_kShoppingInterval, weeks);

  Future<void> setProteinPowder({
    required String ingredientId,
    required String name,
    required double proteinPer100g,
    required double kcalPer100g,
    required double gramsInStock,
  }) async {
    await _prefs.setString(_kPowderIngredientId, ingredientId);
    await _prefs.setString(_kPowderName, name);
    await _prefs.setDouble(_kPowderProteinPer100g, proteinPer100g);
    await _prefs.setDouble(_kPowderKcalPer100g, kcalPer100g);
    await _prefs.setDouble(_kPowderGramsInStock, gramsInStock);
  }

  Future<void> setPowderAutoFill(bool enabled) =>
      _prefs.setBool(_kPowderAutoFill, enabled);

  Future<void> setPowderLowStockWarning(bool enabled) =>
      _prefs.setBool(_kPowderLowStockWarning, enabled);

  Future<void> setTheme(AppTheme theme) =>
      _prefs.setString(_kTheme, theme.name);

  Stream<AppTheme> watchTheme() async* {
    // SharedPreferences doesn't have a native stream; yield current value.
    final s = _prefs.getString(_kTheme) ?? 'system';
    try {
      yield AppTheme.values.firstWhere((e) => e.name == s);
    } catch (_) {
      yield AppTheme.system;
    }
  }

  // ── MealSlotConfig ────────────────────────────────────────────────────────

  Future<void> setMealSlotConfig(int dayOfWeek, DaySlotConfig config) =>
      _dao.upsertMealSlotConfig(MealSlotConfigsTableCompanion(
        dayOfWeek: Value(dayOfWeek),
        hasBreakfast: Value(config.hasBreakfast),
        hasLunch: Value(config.hasLunch),
        hasDinner: Value(config.hasDinner),
        snackCount: Value(config.snackCount),
      ));

  // ── BatchCookingGroups ────────────────────────────────────────────────────

  Future<void> saveBatchCookingGroups(
      List<BatchCookingGroup> groups) async {
    await _dao.deleteAllBatchCookingGroups();
    for (int i = 0; i < groups.length; i++) {
      final g = groups[i];
      await _dao.upsertBatchCookingGroup(BatchCookingGroupsTableCompanion(
        id: Value('batch_$i'),
        category: Value(g.category.firestoreName),
        days: Value(g.days.join(',')),
        batchNumber: Value(g.batchNumber),
      ));
    }
  }

  // ── OptimizerRules ────────────────────────────────────────────────────────

  Future<void> upsertRule(OptimizerRule rule) {
    final id = rule.id.isEmpty ? _uuid.v4() : rule.id;
    return _dao.upsertOptimizerRule(OptimizerRulesTableCompanion(
      id: Value(id),
      type: Value(rule.type.firestoreName),
      target: Value(rule.target),
      targetName: Value(rule.targetName),
      constraint: Value(rule.constraint.firestoreName),
      value: Value(rule.value),
    ));
  }

  Future<void> deleteRule(String ruleId) =>
      _dao.deleteOptimizerRule(ruleId);

  Stream<List<OptimizerRule>> watchRules() =>
      _dao.watchOptimizerRules().map(
            (rows) => rows.map(_optimizerRuleFromRow).toList(),
          );

  // ── Helpers ───────────────────────────────────────────────────────────────

  Set<String> _splitToSet(String? s) {
    if (s == null || s.isEmpty) return {};
    return s.split(',').map((e) => e.trim()).where((e) => e.isNotEmpty).toSet();
  }
}

// ── Entity conversion ─────────────────────────────────────────────────────────

OptimizerRule _optimizerRuleFromRow(OptimizerRulesTableData row) =>
    OptimizerRule(
      id: row.id,
      type: RuleTargetType.fromFirestore(row.type),
      target: row.target,
      targetName: row.targetName,
      constraint: ConstraintType.fromFirestore(row.constraint),
      value: row.value,
    );

extension<T> on T {
  R let<R>(R Function(T) block) => block(this);
}
