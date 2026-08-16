import 'dart:async';
import 'dart:convert';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:drift/drift.dart';
import 'package:foodplan/data/local/app_database.dart';
import 'package:foodplan/data/remote/firestore_mappers.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:uuid/uuid.dart';

class RecipeRepository {
  RecipeRepository({
    required AppDatabase db,
    required FirebaseFirestore firestore,
  })  : _db = db,
        _firestore = firestore;

  final AppDatabase _db;
  final FirebaseFirestore _firestore;
  final _uuid = const Uuid();

  CollectionReference<Map<String, dynamic>> get _collection =>
      _firestore.collection('recipes');

  StreamSubscription<QuerySnapshot>? _sub;

  // ── Firestore listener ────────────────────────────────────────────────────

  void startListening() {
    _sub?.cancel();
    _sub = _collection.snapshots().listen((snapshot) {
      for (final change in snapshot.docChanges) {
        switch (change.type) {
          case DocumentChangeType.added:
          case DocumentChangeType.modified:
            final recipe = change.doc.toRecipe();
            if (recipe != null) {
              _mirrorRecipeToDb(recipe);
            }
          case DocumentChangeType.removed:
            _db.recipeDao.deleteRecipeById(change.doc.id);
        }
      }
    });
  }

  void stopListening() {
    _sub?.cancel();
    _sub = null;
  }

  Future<void> _mirrorRecipeToDb(Recipe recipe) async {
    await _db.recipeDao.upsertRecipe(_recipeToCompanion(recipe));
    await _db.recipeDao.deleteRecipeIngredients(recipe.id);
    await _db.recipeDao.upsertRecipeIngredients(
      recipe.ingredients
          .map((ri) => RecipeIngredientsTableCompanion(
                id: Value(_uuid.v4()),
                recipeId: Value(recipe.id),
                ingredientId: Value(ri.ingredientId),
                subRecipeId: Value(ri.subRecipeId),
                grams: Value(ri.grams),
                portions: Value(ri.portions),
              ))
          .toList(),
    );
  }

  // ── DB reads ──────────────────────────────────────────────────────────────

  Stream<List<Recipe>> watchAllRecipes() =>
      _db.recipeDao.watchAllRecipes().map(
            (rows) => rows.map(_recipeFromRow).toList(),
          );

  Stream<List<Recipe>> watchMealRecipes() =>
      _db.recipeDao.watchRecipesByType('MEAL').map(
            (rows) => rows.map(_recipeFromRow).toList(),
          );

  Stream<List<Recipe>> watchComponentRecipes() =>
      _db.recipeDao.watchRecipesByType('COMPONENT').map(
            (rows) => rows.map(_recipeFromRow).toList(),
          );

  Future<Recipe?> getRecipeById(String id) async {
    final row = await _db.recipeDao.getRecipeById(id);
    return row == null ? null : _recipeFromRow(row);
  }

  Future<Recipe?> getRecipeWithIngredients(String id) async {
    final row = await _db.recipeDao.getRecipeById(id);
    if (row == null) return null;
    final ingredientRows =
        await _db.recipeDao.getIngredientsForRecipe(id);
    return _recipeFromRow(row).copyWith(
      ingredients: ingredientRows.map(_recipeIngredientFromRow).toList(),
    );
  }

  Stream<List<String>> watchRecipeIdsContainingIngredient(String query) =>
      _db.recipeDao.watchRecipeIdsContainingIngredient(query);

  Future<void> deleteRecipeIngredientsByIngredientId(String id) =>
      _db.recipeDao.deleteRecipeIngredientsByIngredientId(id);

  Future<List<Recipe>> getAllMealRecipesWithIngredients() async {
    final rows = await _db.recipeDao.getAllMealRecipesOnce();
    final result = <Recipe>[];
    for (final row in rows) {
      final ingredientRows =
          await _db.recipeDao.getIngredientsForRecipe(row.id);
      result.add(_recipeFromRow(row).copyWith(
        ingredients: ingredientRows.map(_recipeIngredientFromRow).toList(),
      ));
    }
    return result;
  }

  // ── Firestore writes ──────────────────────────────────────────────────────

  /// Saves a recipe to Firestore.  Returns a failure Result if a circular
  /// sub-recipe reference is detected.
  Future<Result<void>> saveRecipe(Recipe recipe) async {
    final recipeId = recipe.id.isEmpty ? _uuid.v4() : recipe.id;

    for (final ri in recipe.ingredients) {
      final subId = ri.subRecipeId;
      if (subId != null &&
          await _wouldCreateCircularReference(recipeId, subId)) {
        return Result.failure(ArgumentError(
          "Circular reference detected: recipe '$recipeId' cannot include "
          "sub-recipe '$subId'",
        ));
      }
    }

    final toSave = recipe.copyWith(id: recipeId);
    await _collection.doc(recipeId).set(toSave.toFirestoreMap());
    return Result.success(null);
  }

  Future<void> deleteRecipe(Recipe recipe) =>
      _collection.doc(recipe.id).delete();

  // ── Circular reference check ──────────────────────────────────────────────

  Future<bool> _wouldCreateCircularReference(
      String recipeId, String subRecipeId) async {
    final visited = <String>{};
    return _containsRecipe(recipeId, subRecipeId, visited);
  }

  Future<bool> _containsRecipe(
    String targetId,
    String currentId,
    Set<String> visited,
  ) async {
    if (currentId == targetId) return true;
    if (!visited.add(currentId)) return false;
    final ingredients =
        await _db.recipeDao.getIngredientsForRecipe(currentId);
    for (final ri in ingredients) {
      final subId = ri.subRecipeId;
      if (subId == null) continue;
      if (await _containsRecipe(targetId, subId, visited)) return true;
    }
    return false;
  }
}

// ── Entity <-> Domain helpers ─────────────────────────────────────────────────

Recipe _recipeFromRow(RecipesTableData row) => Recipe(
      id: row.id,
      name: row.name,
      type: RecipeType.fromFirestore(row.type) ?? RecipeType.meal,
      mealCategories: row.mealCategories.isEmpty
          ? {}
          : row.mealCategories
              .split(',')
              .map((s) => MealCategory.fromFirestore(s.trim()))
              .whereType<MealCategory>()
              .toSet(),
      componentCategory:
          ComponentCategory.fromFirestore(row.componentCategory),
      steps: row.steps.isEmpty || row.steps == '[]'
          ? []
          : List<String>.from(jsonDecode(row.steps) as List),
      notes: row.notes,
    );

RecipeIngredient _recipeIngredientFromRow(RecipeIngredientsTableData row) =>
    RecipeIngredient(
      ingredientId: row.ingredientId,
      subRecipeId: row.subRecipeId,
      grams: row.grams,
      portions: row.portions,
    );

RecipesTableCompanion _recipeToCompanion(Recipe r) => RecipesTableCompanion(
      id: Value(r.id),
      name: Value(r.name),
      type: Value(r.type.firestoreName),
      mealCategories: Value(
        r.mealCategories.map((c) => c.firestoreName).join(','),
      ),
      componentCategory: Value(r.componentCategory?.firestoreName),
      steps: Value(_encodeSteps(r.steps)),
      notes: Value(r.notes),
    );

/// Encode steps list to a JSON string for SQLite storage.
String _encodeSteps(List<String> steps) => jsonEncode(steps);

/// Thin wrapper matching Kotlin's Result<T> shape for [RecipeRepository.saveRecipe].
class Result<T> {
  const Result._({this.value, this.error});

  final T? value;
  final Object? error;

  bool get isSuccess => error == null;
  bool get isFailure => error != null;

  static Result<T> success<T>(T value) => Result._(value: value);
  static Result<T> failure<T>(Object error) => Result._(error: error);
}
