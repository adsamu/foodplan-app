import 'package:drift/drift.dart';
import 'package:foodplan/data/local/app_database.dart';

part 'meal_plan_dao.g.dart';

@DriftAccessor(
  tables: [MealPlansTable, DayPlansTable, MealSlotsTable],
)
class MealPlanDao extends DatabaseAccessor<AppDatabase>
    with _$MealPlanDaoMixin {
  MealPlanDao(super.db);

  // ── MealPlan ──────────────────────────────────────────────────────────────

  Stream<List<MealPlansTableData>> watchAllMealPlans() =>
      (select(mealPlansTable)
            ..orderBy([(t) => OrderingTerm.asc(t.startDate)]))
          .watch();

  Future<MealPlansTableData?> getMealPlanById(String id) =>
      (select(mealPlansTable)..where((t) => t.id.equals(id)))
          .getSingleOrNull();

  Future<void> upsertMealPlan(MealPlansTableCompanion row) =>
      into(mealPlansTable).insertOnConflictUpdate(row);

  Future<void> deleteMealPlan(String id) =>
      (delete(mealPlansTable)..where((t) => t.id.equals(id))).go();

  // ── DayPlan ───────────────────────────────────────────────────────────────

  Future<List<DayPlansTableData>> getDayPlansForMealPlan(
          String mealPlanId) =>
      (select(dayPlansTable)
            ..where((t) => t.mealPlanId.equals(mealPlanId))
            ..orderBy([(t) => OrderingTerm.asc(t.date)]))
          .get();

  Future<DayPlansTableData?> getDayPlanByDate(int epochDays) =>
      (select(dayPlansTable)..where((t) => t.date.equals(epochDays)))
          .getSingleOrNull();

  Future<void> upsertDayPlan(DayPlansTableCompanion row) =>
      into(dayPlansTable).insertOnConflictUpdate(row);

  /// Fetch day plans within [startEpochDays]..[endEpochDays] inclusive.
  Future<List<DayPlansTableData>> getDayPlansInRange(
          int startEpochDays, int endEpochDays) =>
      (select(dayPlansTable)
            ..where((t) =>
                t.date.isBiggerOrEqualValue(startEpochDays) &
                t.date.isSmallerOrEqualValue(endEpochDays))
            ..orderBy([(t) => OrderingTerm.asc(t.date)]))
          .get();

  // ── MealSlot ──────────────────────────────────────────────────────────────

  Future<List<MealSlotsTableData>> getMealSlotsForDayPlan(
          String dayPlanId) =>
      (select(mealSlotsTable)
            ..where((t) => t.dayPlanId.equals(dayPlanId))
            ..orderBy([(t) => OrderingTerm.asc(t.slotIndex)]))
          .get();

  Future<void> upsertMealSlot(MealSlotsTableCompanion row) =>
      into(mealSlotsTable).insertOnConflictUpdate(row);

  Future<void> deleteMealSlotsForDayPlan(String dayPlanId) =>
      (delete(mealSlotsTable)
            ..where((t) => t.dayPlanId.equals(dayPlanId)))
          .go();

  Future<List<MealSlotsTableData>> getMealSlotsForDayPlanIds(
          List<String> dayPlanIds) =>
      (select(mealSlotsTable)
            ..where((t) => t.dayPlanId.isIn(dayPlanIds))
            ..orderBy([(t) => OrderingTerm.asc(t.slotIndex)]))
          .get();

  /// Fetch meal plans whose dates fall within [startEpochDays]..[endEpochDays].
  Future<List<MealPlansTableData>> getMealPlansInRange(
          int startEpochDays, int endEpochDays) =>
      (select(mealPlansTable)
            ..where((t) =>
                t.startDate.isBiggerOrEqualValue(startEpochDays) &
                t.endDate.isSmallerOrEqualValue(endEpochDays)))
          .get();
}
