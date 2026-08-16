import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:uuid/uuid.dart';

// ── Step UI models ────────────────────────────────────────────────────────────

sealed class StepUi {
  final String id;
  const StepUi({required this.id});
}

class TextStep extends StepUi {
  final String text;
  const TextStep({required super.id, required this.text});
}

class TimerStep extends StepUi {
  final String label;
  final int totalSeconds;
  const TimerStep({
    required super.id,
    required this.label,
    required this.totalSeconds,
  });
}

// ── Ingredient UI model ───────────────────────────────────────────────────────

class RecipeIngredientUi {
  final String? ingredientId;
  final String? subRecipeId;
  final String name;
  final double amount; // grams (for ingredient) or portions (for sub-recipe)
  final String unit; // 'g' or 'portions'
  final double kcal;
  final double protein;
  final double fat;
  final double carbs;

  const RecipeIngredientUi({
    this.ingredientId,
    this.subRecipeId,
    required this.name,
    required this.amount,
    required this.unit,
    this.kcal = 0,
    this.protein = 0,
    this.fat = 0,
    this.carbs = 0,
  });

  RecipeIngredientUi copyWith({
    String? name,
    double? amount,
    String? unit,
    double? kcal,
    double? protein,
    double? fat,
    double? carbs,
  }) =>
      RecipeIngredientUi(
        ingredientId: ingredientId,
        subRecipeId: subRecipeId,
        name: name ?? this.name,
        amount: amount ?? this.amount,
        unit: unit ?? this.unit,
        kcal: kcal ?? this.kcal,
        protein: protein ?? this.protein,
        fat: fat ?? this.fat,
        carbs: carbs ?? this.carbs,
      );
}

// ── Nutrition summary ─────────────────────────────────────────────────────────

class RecipeNutrition {
  final double kcal;
  final double protein;
  final double fat;
  final double carbs;

  const RecipeNutrition({
    this.kcal = 0,
    this.protein = 0,
    this.fat = 0,
    this.carbs = 0,
  });
}

// ── State ─────────────────────────────────────────────────────────────────────

class AddEditRecipeState {
  final String? recipeId;
  final String name;
  final RecipeType type;
  final Set<MealCategory> mealCategories;
  final ComponentCategory? componentCategory;
  final List<RecipeIngredientUi> ingredients;
  final List<StepUi> steps;
  final RecipeNutrition nutrition;
  final bool isSaving;
  final Object? error;

  const AddEditRecipeState({
    this.recipeId,
    this.name = '',
    this.type = RecipeType.meal,
    this.mealCategories = const {},
    this.componentCategory,
    this.ingredients = const [],
    this.steps = const [],
    this.nutrition = const RecipeNutrition(),
    this.isSaving = false,
    this.error,
  });

  AddEditRecipeState copyWith({
    String? recipeId,
    String? name,
    RecipeType? type,
    Set<MealCategory>? mealCategories,
    Object? componentCategory = _sentinel,
    List<RecipeIngredientUi>? ingredients,
    List<StepUi>? steps,
    RecipeNutrition? nutrition,
    bool? isSaving,
    Object? error = _sentinel,
  }) =>
      AddEditRecipeState(
        recipeId: recipeId ?? this.recipeId,
        name: name ?? this.name,
        type: type ?? this.type,
        mealCategories: mealCategories ?? this.mealCategories,
        componentCategory: componentCategory == _sentinel
            ? this.componentCategory
            : componentCategory as ComponentCategory?,
        ingredients: ingredients ?? this.ingredients,
        steps: steps ?? this.steps,
        nutrition: nutrition ?? this.nutrition,
        isSaving: isSaving ?? this.isSaving,
        error: error == _sentinel ? this.error : error,
      );

  bool get isNew => recipeId == null;
}

const _sentinel = Object();

// ── Provider ──────────────────────────────────────────────────────────────────

final addEditRecipeProvider =
    AsyncNotifierProvider<AddEditRecipeNotifier, AddEditRecipeState>(
        AddEditRecipeNotifier.new);

class AddEditRecipeNotifier extends AsyncNotifier<AddEditRecipeState> {
  final _uuid = const Uuid();

  @override
  Future<AddEditRecipeState> build() async {
    return const AddEditRecipeState();
  }

  // ── Load ──────────────────────────────────────────────────────────────────

  Future<void> loadRecipe(String? recipeId) async {
    if (recipeId == null) {
      state = const AsyncData(AddEditRecipeState());
      return;
    }

    state = const AsyncLoading();
    try {
      final recipe = await ref
          .read(recipeRepositoryProvider)
          .getRecipeById(recipeId);

      if (recipe == null) {
        state = const AsyncData(AddEditRecipeState());
        return;
      }

      final ingredients = await _buildIngredientUis(recipe.ingredients);
      final steps = _buildStepUis(recipe.steps);
      final nutrition = _computeNutrition(ingredients);

      state = AsyncData(AddEditRecipeState(
        recipeId: recipe.id,
        name: recipe.name,
        type: recipe.type,
        mealCategories: recipe.mealCategories,
        componentCategory: recipe.componentCategory,
        ingredients: ingredients,
        steps: steps,
        nutrition: nutrition,
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

  void onTypeChange(RecipeType type) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      type: type,
      mealCategories: {},
      componentCategory: null,
    ));
  }

  void onMealCategoryToggle(MealCategory category) {
    final current = state.value;
    if (current == null) return;
    final cats = Set<MealCategory>.from(current.mealCategories);
    if (cats.contains(category)) {
      cats.remove(category);
    } else {
      cats.add(category);
    }
    state = AsyncData(current.copyWith(mealCategories: cats));
  }

  void onComponentCategorySelect(ComponentCategory? category) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(componentCategory: category));
  }

  // ── Ingredients ───────────────────────────────────────────────────────────

  Future<void> addIngredient(String ingredientId, double grams) async {
    final current = state.value;
    if (current == null) return;

    final ingredient = await ref
        .read(ingredientRepositoryProvider)
        .getIngredientById(ingredientId);
    if (ingredient == null) return;

    final factor = grams / 100.0;
    final ui = RecipeIngredientUi(
      ingredientId: ingredientId,
      name: ingredient.name,
      amount: grams,
      unit: 'g',
      kcal: ingredient.kcalPer100g * factor,
      protein: ingredient.proteinPer100g * factor,
      fat: ingredient.fatPer100g * factor,
      carbs: ingredient.carbsPer100g * factor,
    );

    final updated = [...current.ingredients, ui];
    state = AsyncData(current.copyWith(
      ingredients: updated,
      nutrition: _computeNutrition(updated),
    ));
  }

  Future<void> addSubRecipe(String recipeId, double portions) async {
    final current = state.value;
    if (current == null) return;

    final recipe = await ref
        .read(recipeRepositoryProvider)
        .getRecipeById(recipeId);
    if (recipe == null) return;

    final ui = RecipeIngredientUi(
      subRecipeId: recipeId,
      name: recipe.name,
      amount: portions,
      unit: 'portions',
    );

    final updated = [...current.ingredients, ui];
    state = AsyncData(current.copyWith(
      ingredients: updated,
      nutrition: _computeNutrition(updated),
    ));
  }

  void removeIngredient(int index) {
    final current = state.value;
    if (current == null) return;
    final updated = List<RecipeIngredientUi>.from(current.ingredients)
      ..removeAt(index);
    state = AsyncData(current.copyWith(
      ingredients: updated,
      nutrition: _computeNutrition(updated),
    ));
  }

  void updateIngredientAmount(int index, double amount) {
    final current = state.value;
    if (current == null) return;
    final ingredient = current.ingredients[index];
    final updated = List<RecipeIngredientUi>.from(current.ingredients);

    if (ingredient.ingredientId != null && ingredient.unit == 'g') {
      // Recalculate nutrition
      ref
          .read(ingredientRepositoryProvider)
          .getIngredientById(ingredient.ingredientId!)
          .then((ing) {
        if (ing == null) return;
        final factor = amount / 100.0;
        updated[index] = ingredient.copyWith(
          amount: amount,
          kcal: ing.kcalPer100g * factor,
          protein: ing.proteinPer100g * factor,
          fat: ing.fatPer100g * factor,
          carbs: ing.carbsPer100g * factor,
        );
        final s = state.value;
        if (s == null) return;
        state = AsyncData(s.copyWith(
          ingredients: updated,
          nutrition: _computeNutrition(updated),
        ));
      });
    } else {
      updated[index] = ingredient.copyWith(amount: amount);
      state = AsyncData(current.copyWith(
        ingredients: updated,
        nutrition: _computeNutrition(updated),
      ));
    }
  }

  // ── Steps ─────────────────────────────────────────────────────────────────

  void addStep() {
    final current = state.value;
    if (current == null) return;
    final step = TextStep(id: _uuid.v4(), text: '');
    state = AsyncData(
        current.copyWith(steps: [...current.steps, step]));
  }

  void addTimer(String label, int totalSeconds) {
    final current = state.value;
    if (current == null) return;
    final step =
        TimerStep(id: _uuid.v4(), label: label, totalSeconds: totalSeconds);
    state = AsyncData(
        current.copyWith(steps: [...current.steps, step]));
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

  void updateTimer(int index, String label, int totalSeconds) {
    final current = state.value;
    if (current == null) return;
    final steps = List<StepUi>.from(current.steps);
    final existing = steps[index];
    if (existing is TimerStep) {
      steps[index] = TimerStep(
          id: existing.id, label: label, totalSeconds: totalSeconds);
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

  Future<void> saveRecipe() async {
    final current = state.value;
    if (current == null || current.name.trim().isEmpty) return;

    state = AsyncData(current.copyWith(isSaving: true));
    try {
      final id = current.recipeId ?? _uuid.v4();
      final ingredients = current.ingredients
          .map((ui) => RecipeIngredient(
                ingredientId: ui.ingredientId,
                subRecipeId: ui.subRecipeId,
                grams: ui.unit == 'g' ? ui.amount : null,
                portions: ui.unit == 'portions' ? ui.amount : null,
              ))
          .toList();

      final textSteps = current.steps
          .map((s) => switch (s) {
                TextStep t => t.text,
                TimerStep t =>
                  'TIMER:${t.totalSeconds}:${t.label}',
              })
          .where((s) => s.isNotEmpty)
          .toList();

      final recipe = Recipe(
        id: id,
        name: current.name.trim(),
        type: current.type,
        mealCategories: current.mealCategories,
        componentCategory: current.componentCategory,
        ingredients: ingredients,
        steps: textSteps,
      );

      final result = await ref.read(recipeRepositoryProvider).saveRecipe(recipe);
      if (result.isFailure) throw result.error!;
      state = AsyncData(current.copyWith(
          recipeId: id, isSaving: false, error: null));
    } catch (e) {
      final s = state.value;
      if (s != null) {
        state = AsyncData(s.copyWith(isSaving: false, error: e));
      }
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  Future<List<RecipeIngredientUi>> _buildIngredientUis(
      List<RecipeIngredient> ingredients) async {
    final result = <RecipeIngredientUi>[];
    for (final ri in ingredients) {
      if (ri.ingredientId != null) {
        final ing = await ref
            .read(ingredientRepositoryProvider)
            .getIngredientById(ri.ingredientId!);
        final grams = ri.grams ?? 0.0;
        final factor = grams / 100.0;
        result.add(RecipeIngredientUi(
          ingredientId: ri.ingredientId,
          name: ing?.name ?? ri.ingredientId!,
          amount: grams,
          unit: 'g',
          kcal: (ing?.kcalPer100g ?? 0) * factor,
          protein: (ing?.proteinPer100g ?? 0) * factor,
          fat: (ing?.fatPer100g ?? 0) * factor,
          carbs: (ing?.carbsPer100g ?? 0) * factor,
        ));
      } else if (ri.subRecipeId != null) {
        final sub = await ref
            .read(recipeRepositoryProvider)
            .getRecipeById(ri.subRecipeId!);
        result.add(RecipeIngredientUi(
          subRecipeId: ri.subRecipeId,
          name: sub?.name ?? ri.subRecipeId!,
          amount: ri.portions ?? 1.0,
          unit: 'portions',
        ));
      }
    }
    return result;
  }

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

  RecipeNutrition _computeNutrition(List<RecipeIngredientUi> ingredients) {
    double kcal = 0, protein = 0, fat = 0, carbs = 0;
    for (final i in ingredients) {
      kcal += i.kcal;
      protein += i.protein;
      fat += i.fat;
      carbs += i.carbs;
    }
    return RecipeNutrition(
        kcal: kcal, protein: protein, fat: fat, carbs: carbs);
  }
}
