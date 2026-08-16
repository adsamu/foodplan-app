import 'package:drift/drift.dart';
import 'package:foodplan/data/local/app_database.dart';

part 'settings_dao.g.dart';

@DriftAccessor(
  tables: [MealSlotConfigsTable, BatchCookingGroupsTable, OptimizerRulesTable],
)
class SettingsDao extends DatabaseAccessor<AppDatabase>
    with _$SettingsDaoMixin {
  SettingsDao(super.db);

  // ── MealSlotConfigs ───────────────────────────────────────────────────────

  Stream<List<MealSlotConfigsTableData>> watchMealSlotConfigs() =>
      (select(mealSlotConfigsTable)
            ..orderBy([(t) => OrderingTerm.asc(t.dayOfWeek)]))
          .watch();

  Future<List<MealSlotConfigsTableData>> getAllMealSlotConfigsOnce() =>
      (select(mealSlotConfigsTable)
            ..orderBy([(t) => OrderingTerm.asc(t.dayOfWeek)]))
          .get();

  Future<void> upsertMealSlotConfig(MealSlotConfigsTableCompanion row) =>
      into(mealSlotConfigsTable).insertOnConflictUpdate(row);

  Future<void> upsertAllMealSlotConfigs(
      List<MealSlotConfigsTableCompanion> rows) async {
    await batch((b) {
      b.insertAllOnConflictUpdate(mealSlotConfigsTable, rows);
    });
  }

  // ── BatchCookingGroups ────────────────────────────────────────────────────

  Stream<List<BatchCookingGroupsTableData>> watchBatchCookingGroups() =>
      select(batchCookingGroupsTable).watch();

  Future<List<BatchCookingGroupsTableData>> getAllBatchCookingGroupsOnce() =>
      select(batchCookingGroupsTable).get();

  Future<void> upsertBatchCookingGroup(BatchCookingGroupsTableCompanion row) =>
      into(batchCookingGroupsTable).insertOnConflictUpdate(row);

  Future<void> deleteBatchCookingGroup(String id) =>
      (delete(batchCookingGroupsTable)..where((t) => t.id.equals(id))).go();

  Future<void> deleteAllBatchCookingGroups() =>
      delete(batchCookingGroupsTable).go();

  // ── OptimizerRules ────────────────────────────────────────────────────────

  Stream<List<OptimizerRulesTableData>> watchOptimizerRules() =>
      select(optimizerRulesTable).watch();

  Future<List<OptimizerRulesTableData>> getAllOptimizerRulesOnce() =>
      select(optimizerRulesTable).get();

  Future<void> upsertOptimizerRule(OptimizerRulesTableCompanion row) =>
      into(optimizerRulesTable).insertOnConflictUpdate(row);

  Future<void> deleteOptimizerRule(String id) =>
      (delete(optimizerRulesTable)..where((t) => t.id.equals(id))).go();
}
