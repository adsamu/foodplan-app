import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:drift/drift.dart';
import 'package:foodplan/data/local/app_database.dart';
import 'package:foodplan/data/remote/firestore_mappers.dart';
import 'package:foodplan/domain/model/ingredient.dart';

class IngredientRepository {
  IngredientRepository({
    required AppDatabase db,
    required FirebaseFirestore firestore,
  })  : _db = db,
        _firestore = firestore;

  final AppDatabase _db;
  final FirebaseFirestore _firestore;

  CollectionReference<Map<String, dynamic>> get _collection =>
      _firestore.collection('ingredients');

  StreamSubscription<QuerySnapshot>? _sub;

  // ── Firestore listener ────────────────────────────────────────────────────

  /// Subscribe to the global ingredients collection and mirror to local DB.
  /// Idempotent — subsequent calls replace the previous listener.
  void startListening() {
    _sub?.cancel();
    _sub = _collection.snapshots().listen((snapshot) {
      for (final change in snapshot.docChanges) {
        switch (change.type) {
          case DocumentChangeType.added:
          case DocumentChangeType.modified:
            final ing = change.doc.toIngredient();
            if (ing != null) {
              _db.ingredientDao.upsertIngredient(ing.toEntity());
            }
          case DocumentChangeType.removed:
            _db.ingredientDao.deleteIngredientById(change.doc.id);
        }
      }
    });
  }

  void stopListening() {
    _sub?.cancel();
    _sub = null;
  }

  // ── DB reads ──────────────────────────────────────────────────────────────

  Stream<List<Ingredient>> watchAllIngredients() =>
      _db.ingredientDao.watchAllIngredients().map(
            (rows) => rows.map(_ingredientFromRow).toList(),
          );

  Stream<List<Ingredient>> searchIngredients(String query) =>
      _db.ingredientDao.searchIngredients(query).map(
            (rows) => rows.map(_ingredientFromRow).toList(),
          );

  Future<Ingredient?> getIngredientById(String id) async {
    final row = await _db.ingredientDao.getIngredientById(id);
    return row == null ? null : _ingredientFromRow(row);
  }

  // ── Firestore writes ──────────────────────────────────────────────────────

  Future<void> saveIngredient(Ingredient ing) =>
      _collection.doc(ing.id).set(ing.toFirestoreMap());

  Future<void> saveIngredients(List<Ingredient> ings) async {
    final batch = _firestore.batch();
    for (final ing in ings) {
      batch.set(_collection.doc(ing.id), ing.toFirestoreMap());
    }
    await batch.commit();
  }

  Future<void> deleteIngredient(Ingredient ing) =>
      _collection.doc(ing.id).delete();
}

// ── Entity conversion helpers ─────────────────────────────────────────────────

Ingredient _ingredientFromRow(IngredientsTableData row) => Ingredient(
      id: row.id,
      name: row.name,
      category: IngredientCategory.fromFirestore(row.category),
      kcalPer100g: row.kcalPer100g,
      proteinPer100g: row.proteinPer100g,
      fatPer100g: row.fatPer100g,
      carbsPer100g: row.carbsPer100g,
      source: IngredientSource.fromFirestore(row.source),
      steps: row.steps.isEmpty ? [] : row.steps.split('|||'),
    );

IngredientsTableCompanion _ingredientToEntity(Ingredient ing) =>
    IngredientsTableCompanion(
      id: Value(ing.id),
      name: Value(ing.name),
      category: Value(ing.category.firestoreName),
      kcalPer100g: Value(ing.kcalPer100g),
      proteinPer100g: Value(ing.proteinPer100g),
      fatPer100g: Value(ing.fatPer100g),
      carbsPer100g: Value(ing.carbsPer100g),
      source: Value(ing.source.firestoreName),
      steps: Value(ing.steps.join('|||')),
    );

// Extension on Ingredient to get the table companion (used by repositories).
extension IngredientEntityX on Ingredient {
  IngredientsTableCompanion toEntity() => _ingredientToEntity(this);
}
