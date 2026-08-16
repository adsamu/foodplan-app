import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:drift/drift.dart';
import 'package:foodplan/data/local/app_database.dart';
import 'package:foodplan/data/remote/firestore_mappers.dart';
import 'package:foodplan/domain/model/meal_plan.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:uuid/uuid.dart';

class MealPlanRepository {
  MealPlanRepository({
    required AppDatabase db,
    required FirebaseFirestore firestore,
  })  : _db = db,
        _firestore = firestore;

  final AppDatabase _db;
  final FirebaseFirestore _firestore;
  final _uuid = const Uuid();

  StreamSubscription<QuerySnapshot>? _sub;

  // ── Firestore listener ────────────────────────────────────────────────────

  /// Subscribe to users/{userId}/mealPlans and mirror every plan to local DB.
  void startListening(String userId) {
    _sub?.cancel();
    _sub = _firestore
        .collection('users')
        .doc(userId)
        .collection('mealPlans')
        .snapshots()
        .listen((snapshot) {
      for (final doc in snapshot.docs) {
        final plan = doc.toMealPlan();
        if (plan != null) {
          saveMealPlan(plan);
        }
      }
    });
  }

  void stopListening() {
    _sub?.cancel();
    _sub = null;
  }

  // ── DB reads ──────────────────────────────────────────────────────────────

  Stream<List<MealPlan>> watchAllMealPlans() =>
      _db.mealPlanDao.watchAllMealPlans().map(
            (rows) => rows.map(_mealPlanFromRow).toList(),
          );

  Future<MealPlan?> getMealPlanWithDays(String id) async {
    final row = await _db.mealPlanDao.getMealPlanById(id);
    if (row == null) return null;
    final dayRows = await _db.mealPlanDao.getDayPlansForMealPlan(id);
    final days = <DayPlan>[];
    for (final dayRow in dayRows) {
      final slotRows =
          await _db.mealPlanDao.getMealSlotsForDayPlan(dayRow.id);
      days.add(_dayPlanFromRow(dayRow, slotRows));
    }
    return MealPlan(
      id: row.id,
      name: row.name,
      startDate: epochDaysToDate(row.startDate),
      endDate: epochDaysToDate(row.endDate),
      days: days,
    );
  }

  Future<DayPlan?> getDayPlanByDate(DateTime date) async {
    final row =
        await _db.mealPlanDao.getDayPlanByDate(dateToEpochDays(date));
    if (row == null) return null;
    final slotRows =
        await _db.mealPlanDao.getMealSlotsForDayPlan(row.id);
    return _dayPlanFromRow(row, slotRows);
  }

  Future<Map<DateTime, DayPlan>> getDayPlansForRange(
    DateTime startDate,
    DateTime endDate,
  ) async {
    final dayRows = await _db.mealPlanDao.getDayPlansInRange(
      dateToEpochDays(startDate),
      dateToEpochDays(endDate),
    );
    if (dayRows.isEmpty) return {};

    final slotRows = await _db.mealPlanDao
        .getMealSlotsForDayPlanIds(dayRows.map((r) => r.id).toList());
    final slotsByDayId = <String, List<MealSlotsTableData>>{};
    for (final slot in slotRows) {
      slotsByDayId.putIfAbsent(slot.dayPlanId, () => []).add(slot);
    }

    return {
      for (final row in dayRows)
        epochDaysToDate(row.date):
            _dayPlanFromRow(row, slotsByDayId[row.id] ?? []),
    };
  }

  // ── Persistence ───────────────────────────────────────────────────────────

  Future<void> saveMealPlan(MealPlan mealPlan) async {
    final planId = mealPlan.id.isEmpty ? _uuid.v4() : mealPlan.id;
    await _db.mealPlanDao.upsertMealPlan(MealPlansTableCompanion(
      id: Value(planId),
      name: Value(mealPlan.name),
      startDate: Value(dateToEpochDays(mealPlan.startDate)),
      endDate: Value(dateToEpochDays(mealPlan.endDate)),
    ));

    for (final day in mealPlan.days) {
      final dayId = day.id.isEmpty ? _uuid.v4() : day.id;
      await _db.mealPlanDao.upsertDayPlan(DayPlansTableCompanion(
        id: Value(dayId),
        mealPlanId: Value(planId),
        date: Value(dateToEpochDays(day.date)),
        proteinPowderGrams: Value(day.proteinPowderGrams),
        kcalTarget: Value(day.goal.kcalTarget),
        proteinTarget: Value(day.goal.proteinTarget),
      ));

      for (int i = 0; i < day.meals.length; i++) {
        final slot = day.meals[i];
        await _db.mealPlanDao.upsertMealSlot(MealSlotsTableCompanion(
          id: Value('${dayId}_${slot.type.firestoreName}_$i'),
          dayPlanId: Value(dayId),
          type: Value(slot.type.firestoreName),
          slotIndex: Value(i),
          recipeId: Value(slot.recipeId),
        ));
      }
    }
  }

  Future<void> deleteMealPlan(String id) =>
      _db.mealPlanDao.deleteMealPlan(id);

  // ── Recent plans for optimizer ────────────────────────────────────────────

  Future<List<MealPlan>> getRecentPlans(int weeks) async {
    final endDate = DateTime.now();
    final startDate = endDate.subtract(Duration(days: weeks * 7));
    final planRows = await _db.mealPlanDao.getMealPlansInRange(
      dateToEpochDays(startDate),
      dateToEpochDays(endDate),
    );
    final result = <MealPlan>[];
    for (final row in planRows) {
      final dayRows =
          await _db.mealPlanDao.getDayPlansForMealPlan(row.id);
      final days = <DayPlan>[];
      for (final dayRow in dayRows) {
        final slotRows =
            await _db.mealPlanDao.getMealSlotsForDayPlan(dayRow.id);
        days.add(_dayPlanFromRow(dayRow, slotRows));
      }
      result.add(_mealPlanFromRow(row).copyWith(days: days));
    }
    return result;
  }
}

// ── Entity <-> Domain helpers ─────────────────────────────────────────────────

MealPlan _mealPlanFromRow(MealPlansTableData row) => MealPlan(
      id: row.id,
      name: row.name,
      startDate: epochDaysToDate(row.startDate),
      endDate: epochDaysToDate(row.endDate),
    );

DayPlan _dayPlanFromRow(
  DayPlansTableData row,
  List<MealSlotsTableData> slots,
) =>
    DayPlan(
      id: row.id,
      date: epochDaysToDate(row.date),
      proteinPowderGrams: row.proteinPowderGrams,
      goal: DailyGoal(
        kcalTarget: row.kcalTarget,
        proteinTarget: row.proteinTarget,
      ),
      meals: slots
          .map((s) => MealSlot(
                type: MealCategory.fromFirestore(s.type) ?? MealCategory.lunch,
                recipeId: s.recipeId,
              ))
          .toList(),
    );
