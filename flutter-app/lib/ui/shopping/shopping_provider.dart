import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/shopping_list.dart';
import 'package:foodplan/use_case/get_shopping_list_use_case.dart';

// ── State ─────────────────────────────────────────────────────────────────────

class ShoppingState {
  const ShoppingState({
    this.shoppingList,
    this.checkedItems = const {},
    this.adjustments = const {},
    this.expandedItemId,
    this.selectedRecipeIds,
    required this.startDate,
    required this.endDate,
    this.pendingStartDate,
    this.pendingEndDate,
    this.isLoading = false,
    this.error,
  });

  final ShoppingList? shoppingList;
  final Set<String> checkedItems;
  final Map<String, double> adjustments;
  final String? expandedItemId;

  /// null means all recipes are included
  final Set<String>? selectedRecipeIds;

  final DateTime startDate;
  final DateTime endDate;

  /// Set when the user requests a period change — shown in dialog
  final DateTime? pendingStartDate;
  final DateTime? pendingEndDate;

  final bool isLoading;
  final Object? error;

  int get totalItems =>
      shoppingList?.categories.fold<int>(0, (s, c) => s + c.items.length) ?? 0;

  int get checkedCount => checkedItems.length;

  ShoppingState copyWith({
    ShoppingList? shoppingList,
    Set<String>? checkedItems,
    Map<String, double>? adjustments,
    Object? expandedItemId = _sentinel,
    Object? selectedRecipeIds = _sentinel,
    DateTime? startDate,
    DateTime? endDate,
    Object? pendingStartDate = _sentinel,
    Object? pendingEndDate = _sentinel,
    bool? isLoading,
    Object? error = _sentinel,
  }) =>
      ShoppingState(
        shoppingList: shoppingList ?? this.shoppingList,
        checkedItems: checkedItems ?? this.checkedItems,
        adjustments: adjustments ?? this.adjustments,
        expandedItemId:
            expandedItemId == _sentinel ? this.expandedItemId : expandedItemId as String?,
        selectedRecipeIds: selectedRecipeIds == _sentinel
            ? this.selectedRecipeIds
            : selectedRecipeIds as Set<String>?,
        startDate: startDate ?? this.startDate,
        endDate: endDate ?? this.endDate,
        pendingStartDate: pendingStartDate == _sentinel
            ? this.pendingStartDate
            : pendingStartDate as DateTime?,
        pendingEndDate: pendingEndDate == _sentinel
            ? this.pendingEndDate
            : pendingEndDate as DateTime?,
        isLoading: isLoading ?? this.isLoading,
        error: error == _sentinel ? this.error : error,
      );
}

const _sentinel = Object();

// ── Provider ──────────────────────────────────────────────────────────────────

final shoppingProvider =
    AsyncNotifierProvider<ShoppingNotifier, ShoppingState>(ShoppingNotifier.new);

class ShoppingNotifier extends AsyncNotifier<ShoppingState> {
  late GetShoppingListUseCase _useCase;

  static DateTime _defaultStart() {
    final now = DateTime.now();
    // Start of current week (Monday)
    return now.subtract(Duration(days: now.weekday - 1));
  }

  static DateTime _defaultEnd() {
    final start = _defaultStart();
    return start.add(const Duration(days: 6));
  }

  @override
  Future<ShoppingState> build() async {
    final recipeRepo = ref.watch(recipeRepositoryProvider);
    final ingredientRepo = ref.watch(ingredientRepositoryProvider);
    final mealPlanRepo = ref.watch(mealPlanRepositoryProvider);

    _useCase = GetShoppingListUseCase(
      recipeRepository: recipeRepo,
      ingredientRepository: ingredientRepo,
      mealPlanRepository: mealPlanRepo,
    );

    final start = _defaultStart();
    final end = _defaultEnd();

    final list = await _useCase(start, end);

    return ShoppingState(
      shoppingList: list,
      startDate: start,
      endDate: end,
    );
  }

  // ── Public methods ─────────────────────────────────────────────────────────

  void toggleItem(String ingredientId) {
    final current = state.value;
    if (current == null) return;
    final checked = Set<String>.from(current.checkedItems);
    if (checked.contains(ingredientId)) {
      checked.remove(ingredientId);
    } else {
      checked.add(ingredientId);
    }
    state = AsyncData(current.copyWith(checkedItems: checked));
  }

  void setExpandedItem(String? ingredientId) {
    final current = state.value;
    if (current == null) return;
    final next = current.expandedItemId == ingredientId ? null : ingredientId;
    state = AsyncData(current.copyWith(expandedItemId: next));
  }

  /// Parse and commit an arithmetic expression for a given ingredient.
  /// Returns true if the expression was valid.
  bool commitExpression(String ingredientId, String expression) {
    final current = state.value;
    if (current == null) return false;

    final result = evaluateExpression(expression);
    if (result == null || result <= 0) return false;

    final adjustments = Map<String, double>.from(current.adjustments);
    adjustments[ingredientId] = result;

    state = AsyncData(current.copyWith(
      adjustments: adjustments,
      expandedItemId: null,
    ));
    return true;
  }

  void toggleRecipeFilter(String recipeId) {
    final current = state.value;
    if (current == null) return;

    final allIds = current.shoppingList?.period.recipes
            .map((r) => r.recipeId)
            .toSet() ??
        {};

    Set<String> updated;
    if (current.selectedRecipeIds == null) {
      // All selected → deselect only this one
      updated = allIds.difference({recipeId});
    } else {
      final selected = Set<String>.from(current.selectedRecipeIds!);
      if (selected.contains(recipeId)) {
        selected.remove(recipeId);
      } else {
        selected.add(recipeId);
      }
      // If everything is selected, go back to "all" (null)
      updated = selected.length == allIds.length ? allIds : selected;
    }

    state = AsyncData(current.copyWith(selectedRecipeIds: updated));
    _reload(
      current.startDate,
      current.endDate,
      selectedRecipeIds: updated,
    );
  }

  /// Called when user taps "Change period" in the adjustments dialog.
  void confirmPeriodChange(DateTime newStart, DateTime newEnd) {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      pendingStartDate: null,
      pendingEndDate: null,
      checkedItems: {},
      adjustments: {},
    ));
    _reload(newStart, newEnd, selectedRecipeIds: current.selectedRecipeIds);
  }

  /// Show dialog to let user decide what to do with adjustments before changing period.
  void requestPeriodChange(DateTime newStart, DateTime newEnd) {
    final current = state.value;
    if (current == null) return;
    if (current.adjustments.isEmpty && current.checkedItems.isEmpty) {
      confirmPeriodChange(newStart, newEnd);
      return;
    }
    state = AsyncData(current.copyWith(
      pendingStartDate: newStart,
      pendingEndDate: newEnd,
    ));
  }

  void dismissPeriodChange() {
    final current = state.value;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      pendingStartDate: null,
      pendingEndDate: null,
    ));
  }

  // ── Private helpers ────────────────────────────────────────────────────────

  Future<void> _reload(
    DateTime start,
    DateTime end, {
    Set<String>? selectedRecipeIds,
  }) async {
    final current = state.value;
    if (current != null) {
      state = AsyncData(current.copyWith(isLoading: true));
    }
    try {
      final list = await _useCase(
        start,
        end,
        selectedRecipeIds: selectedRecipeIds,
      );
      state = AsyncData((state.value ?? ShoppingState(startDate: start, endDate: end))
          .copyWith(
        shoppingList: list,
        startDate: start,
        endDate: end,
        isLoading: false,
        error: null,
      ));
    } catch (e) {
      final prev = state.value;
      if (prev != null) {
        state = AsyncData(prev.copyWith(isLoading: false, error: e));
      } else {
        state = AsyncError(e, StackTrace.current);
      }
    }
  }
}
