import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/meal_plan_config.dart';

// ── Provider ──────────────────────────────────────────────────────────────────

final settingsNotifierProvider =
    AsyncNotifierProvider<SettingsNotifier, MealPlanConfig>(
        SettingsNotifier.new);

class SettingsNotifier extends AsyncNotifier<MealPlanConfig> {
  @override
  Future<MealPlanConfig> build() async {
    // Watch the live stream so the notifier updates whenever the config changes
    final config = await ref.watch(
      settingsStreamProvider.future,
    );
    return config;
  }

  // ── Goals ──────────────────────────────────────────────────────────────────

  Future<void> setKcalTarget(double kcal) async {
    await ref.read(settingsRepositoryProvider).setKcalTarget(kcal);
    ref.invalidate(settingsStreamProvider);
  }

  Future<void> setProteinTarget(double? g) async {
    await ref.read(settingsRepositoryProvider).setProteinTarget(g);
    ref.invalidate(settingsStreamProvider);
  }

  Future<void> setFatTarget(double? g) async {
    await ref.read(settingsRepositoryProvider).setFatTarget(g);
    ref.invalidate(settingsStreamProvider);
  }

  Future<void> setCarbsTarget(double? g) async {
    await ref.read(settingsRepositoryProvider).setCarbsTarget(g);
    ref.invalidate(settingsStreamProvider);
  }

  Future<void> setAutoField(MacroField field) async {
    await ref.read(settingsRepositoryProvider).setAutoField(field);
    ref.invalidate(settingsStreamProvider);
  }

  // ── Diet ──────────────────────────────────────────────────────────────────

  Future<void> toggleDietType(DietType type) async {
    await ref.read(settingsRepositoryProvider).toggleDietType(type);
    ref.invalidate(settingsStreamProvider);
  }

  Future<void> toggleAllergy(AllergyType allergy) async {
    await ref.read(settingsRepositoryProvider).toggleAllergy(allergy);
    ref.invalidate(settingsStreamProvider);
  }

  // ── Rules ──────────────────────────────────────────────────────────────────

  Future<void> addRule(OptimizerRule rule) async {
    await ref.read(settingsRepositoryProvider).upsertRule(rule);
    ref.invalidate(settingsStreamProvider);
  }

  Future<void> deleteRule(String ruleId) async {
    await ref.read(settingsRepositoryProvider).deleteRule(ruleId);
    ref.invalidate(settingsStreamProvider);
  }

  // ── Variety ───────────────────────────────────────────────────────────────

  Future<void> setVarietyLevel(VarietyLevel level) async {
    await ref.read(settingsRepositoryProvider).setVarietyLevel(level);
    ref.invalidate(settingsStreamProvider);
  }

  // ── Schedule ──────────────────────────────────────────────────────────────

  Future<void> setDaySlot(int dayOfWeek, DaySlotConfig config) async {
    await ref
        .read(settingsRepositoryProvider)
        .setMealSlotConfig(dayOfWeek, config);
    ref.invalidate(settingsStreamProvider);
  }

  Future<void> setSnackOptionalFill(bool enabled) async {
    await ref
        .read(settingsRepositoryProvider)
        .setSnackOptionalFill(enabled);
    ref.invalidate(settingsStreamProvider);
  }

  // ── Shopping ──────────────────────────────────────────────────────────────

  Future<void> setShoppingDays(Set<int> days) async {
    await ref.read(settingsRepositoryProvider).setShoppingDays(days);
    ref.invalidate(settingsStreamProvider);
  }

  Future<void> setShoppingInterval(int weeks) async {
    await ref.read(settingsRepositoryProvider).setShoppingInterval(weeks);
    ref.invalidate(settingsStreamProvider);
  }
}
