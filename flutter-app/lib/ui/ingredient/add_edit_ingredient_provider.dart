import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/ingredient.dart';
import 'package:uuid/uuid.dart';

import '../recipe/add_edit_recipe_provider.dart';

// ── State ─────────────────────────────────────────────────────────────────────

class AddEditIngredientState {
  final String? ingredientId;
  final String name;
  final IngredientCategory category;
  final IngredientSource source;
  final double kcal;
  final double protein;
  final double fat;
  final double carbs;
  final List<StepUi> steps;
  final bool isSaving;
  final String? savedIngredientId;
  final Object? error;

  const AddEditIngredientState({
    this.ingredientId,
    this.name = '',
    this.category = IngredientCategory.other,
    this.source = IngredientSource.label,
    this.kcal = 0.0,
    this.protein = 0.0,
    this.fat = 0.0,
    this.carbs = 0.0,
    this.steps = const [],
    this.isSaving = false,
    this.savedIngredientId,
    this.error,
  });

  bool get isNew => ingredientId == null;

  AddEditIngredientState copyWith({
    String? ingredientId,
    String? name,
    IngredientCategory? category,
    IngredientSource? source,
    double? kcal,
    double? protein,
    double? fat,
    double? carbs,
    List<StepUi>? steps,
    bool? isSaving,
    Object? savedIngredientId = _sentinel,
    Object? error = _sentinel,
  }) =>
      AddEditIngredientState(
        ingredientId: ingredientId ?? this.ingredientId,
        name: name ?? this.name,
        category: category ?? this.category,
        source: source ?? this.source,
        kcal: kcal ?? this.kcal,
        protein: protein ?? this.protein,
        fat: fat ?? this.fat,
        carbs: carbs ?? this.carbs,
        steps: steps ?? this.steps,
        isSaving: isSaving ?? this.isSaving,
        savedIngredientId: savedIngredientId == _sentinel
            ? this.savedIngredientId
            : savedIngredientId as String?,
        error: error == _sentinel ? this.error : error,
      );
}

const _sentinel = Object();

// ── Provider ──────────────────────────────────────────────────────────────────

final addEditIngredientProvider =
    AsyncNotifierProvider<AddEditIngredientNotifier, AddEditIngredientState>(
        AddEditIngredientNotifier.new);

class AddEditIngredientNotifier
    extends AsyncNotifier<AddEditIngredientState> {
  final _uuid = const Uuid();

  @override
  Future<AddEditIngredientState> build() async {
    return const AddEditIngredientState();
  }

  // ── Load ──────────────────────────────────────────────────────────────────

  Future<void> loadIngredient(String? ingredientId) async {
    if (ingredientId == null) {
      state = const AsyncData(AddEditIngredientState());
      return;
    }
    state = const AsyncLoading();
    try {
      final ingredient = await ref
          .read(ingredientRepositoryProvider)
          .getIngredientById(ingredientId);

      if (ingredient == null) {
        state = const AsyncData(AddEditIngredientState());
        return;
      }

      final steps = _buildStepUis(ingredient.steps);

      state = AsyncData(AddEditIngredientState(
        ingredientId: ingredient.id,
        name: ingredient.name,
        category: ingredient.category,
        source: ingredient.source,
        kcal: ingredient.kcalPer100g,
        protein: ingredient.proteinPer100g,
        fat: ingredient.fatPer100g,
        carbs: ingredient.carbsPer100g,
        steps: steps,
      ));
    } catch (e, st) {
      state = AsyncError(e, st);
    }
  }

  // ── Field setters ─────────────────────────────────────────────────────────

  void onNameChange(String name) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(name: name));
  }

  void onCategoryChange(IngredientCategory category) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(category: category));
  }

  void onSourceChange(IngredientSource source) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(source: source));
  }

  void onKcalChange(double kcal) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(kcal: kcal));
  }

  void onProteinChange(double protein) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(protein: protein));
  }

  void onFatChange(double fat) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(fat: fat));
  }

  void onCarbsChange(double carbs) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(carbs: carbs));
  }

  // ── Steps ─────────────────────────────────────────────────────────────────

  void addStep() {
    final current = state.value;
    if (current == null) return;
    final step = TextStep(id: _uuid.v4(), text: '');
    state =
        AsyncData(current.copyWith(steps: [...current.steps, step]));
  }

  void addTimer(String label, int totalSeconds) {
    final current = state.value;
    if (current == null) return;
    final step =
        TimerStep(id: _uuid.v4(), label: label, totalSeconds: totalSeconds);
    state =
        AsyncData(current.copyWith(steps: [...current.steps, step]));
  }

  void updateStep(int index, String text) {
    final current = state.value;
    if (current == null) return;
    final steps = List<StepUi>.from(current.steps);
    final existing = steps[index];
    if (existing is TextStep) {
      steps[index] = TextStep(id: existing.id, text: text);
    }
    state = AsyncData(current.copyWith(steps: steps));
  }

  void removeStep(int index) {
    final current = state.value;
    if (current == null) return;
    final steps = List<StepUi>.from(current.steps)..removeAt(index);
    state = AsyncData(current.copyWith(steps: steps));
  }

  void reorderSteps(int oldIndex, int newIndex) {
    final current = state.value;
    if (current == null) return;
    final steps = List<StepUi>.from(current.steps);
    if (newIndex > oldIndex) newIndex--;
    final item = steps.removeAt(oldIndex);
    steps.insert(newIndex, item);
    state = AsyncData(current.copyWith(steps: steps));
  }

  // ── Save ──────────────────────────────────────────────────────────────────

  Future<void> saveIngredient() async {
    final current = state.value;
    if (current == null || current.name.trim().isEmpty) return;

    state = AsyncData(current.copyWith(isSaving: true));
    try {
      final id = current.ingredientId ?? _uuid.v4();

      final textSteps = current.steps
          .map((s) => switch (s) {
                TextStep t => t.text,
                TimerStep t =>
                  'TIMER:${t.totalSeconds}:${t.label}',
              })
          .where((s) => s.isNotEmpty)
          .toList();

      final ingredient = Ingredient(
        id: id,
        name: current.name.trim(),
        category: current.category,
        source: current.source,
        kcalPer100g: current.kcal,
        proteinPer100g: current.protein,
        fatPer100g: current.fat,
        carbsPer100g: current.carbs,
        steps: textSteps,
      );

      await ref
          .read(ingredientRepositoryProvider)
          .saveIngredient(ingredient);

      state = AsyncData(current.copyWith(
        ingredientId: id,
        savedIngredientId: id,
        isSaving: false,
        error: null,
      ));
    } catch (e) {
      final s = state.value;
      if (s != null) {
        state = AsyncData(s.copyWith(isSaving: false, error: e));
      }
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  List<StepUi> _buildStepUis(List<String> steps) {
    return steps.map((s) {
      if (s.startsWith('TIMER:')) {
        final parts = s.split(':');
        final secs = int.tryParse(parts.elementAtOrNull(1) ?? '') ?? 0;
        final label = parts.elementAtOrNull(2) ?? '';
        return TimerStep(id: _uuid.v4(), label: label, totalSeconds: secs);
      }
      return TextStep(id: _uuid.v4(), text: s);
    }).toList();
  }
}
