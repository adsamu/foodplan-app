import 'package:drift/drift.dart';
import 'package:foodplan/data/local/app_database.dart';

part 'ingredient_dao.g.dart';

@DriftAccessor(tables: [IngredientsTable])
class IngredientDao extends DatabaseAccessor<AppDatabase>
    with _$IngredientDaoMixin {
  IngredientDao(super.db);

  /// Watch all ingredients ordered by name.
  Stream<List<IngredientsTableData>> watchAllIngredients() =>
      (select(ingredientsTable)
            ..orderBy([(t) => OrderingTerm.asc(t.name)]))
          .watch();

  /// Watch ingredients whose name contains [query] (case-insensitive LIKE).
  Stream<List<IngredientsTableData>> searchIngredients(String query) =>
      (select(ingredientsTable)
            ..where((t) => t.name.like('%$query%'))
            ..orderBy([(t) => OrderingTerm.asc(t.name)]))
          .watch();

  /// One-shot fetch of a single ingredient by id.
  Future<IngredientsTableData?> getIngredientById(String id) =>
      (select(ingredientsTable)..where((t) => t.id.equals(id)))
          .getSingleOrNull();

  /// Upsert a single ingredient row.
  Future<void> upsertIngredient(IngredientsTableCompanion row) =>
      into(ingredientsTable).insertOnConflictUpdate(row);

  /// Upsert a batch of ingredient rows.
  Future<void> upsertIngredients(List<IngredientsTableCompanion> rows) async {
    await batch((b) {
      b.insertAllOnConflictUpdate(ingredientsTable, rows);
    });
  }

  /// Delete an ingredient by id.
  Future<void> deleteIngredientById(String id) =>
      (delete(ingredientsTable)..where((t) => t.id.equals(id))).go();
}
