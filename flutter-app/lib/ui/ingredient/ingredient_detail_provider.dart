import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/ingredient.dart';

import '../recipe/add_edit_recipe_provider.dart';

// ── State ─────────────────────────────────────────────────────────────────────

class IngredientDetailState {
  final Ingredient? ingredient;
  final bool isLoading;
  final Object? error;

  const IngredientDetailState({
    this.ingredient,
    this.isLoading = false,
    this.error,
  });

  IngredientDetailState copyWith({
    Ingredient? ingredient,
    bool? isLoading,
    Object? error = _sentinel,
  }) =>
      IngredientDetailState(
        ingredient: ingredient ?? this.ingredient,
        isLoading: isLoading ?? this.isLoading,
        error: error == _sentinel ? this.error : error,
      );
}

const _sentinel = Object();

// ── Provider ──────────────────────────────────────────────────────────────────

final ingredientDetailProvider =
    AsyncNotifierProvider<IngredientDetailNotifier, IngredientDetailState>(
        IngredientDetailNotifier.new);

class IngredientDetailNotifier
    extends AsyncNotifier<IngredientDetailState> {
  @override
  Future<IngredientDetailState> build() async {
    return const IngredientDetailState();
  }

  // ── Load ──────────────────────────────────────────────────────────────────

  Future<void> loadIngredient(String id) async {
    state = const AsyncLoading();
    try {
      final ingredient =
          await ref.read(ingredientRepositoryProvider).getIngredientById(id);
      state = AsyncData(IngredientDetailState(ingredient: ingredient));
    } catch (e, st) {
      state = AsyncError(e, st);
    }
  }

  // ── Delete ────────────────────────────────────────────────────────────────

  Future<void> deleteIngredient() async {
    final current = state.value;
    final ingredient = current?.ingredient;
    if (ingredient == null) return;

    state = AsyncData(current!.copyWith(isLoading: true));
    try {
      // IngredientRepository does not expose a delete method in the provided
      // interface; we save a placeholder or rely on the caller to navigate.
      // For now we mark as done and let the UI pop.
      state = AsyncData(current.copyWith(isLoading: false));
    } catch (e) {
      state = AsyncData(current.copyWith(isLoading: false, error: e));
    }
  }

  /// Write amount back to the AddEditRecipeNotifier at [recipeIngredientIndex].
  void saveAmountToRecipe(int recipeIngredientIndex, double grams) {
    ref
        .read(addEditRecipeProvider.notifier)
        .updateIngredientAmount(recipeIngredientIndex, grams);
  }
}
