import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'dart:io';

import 'dao/recipe_dao.dart';
import 'dao/ingredient_dao.dart';
import 'dao/meal_plan_dao.dart';
import 'dao/settings_dao.dart';

export 'dao/recipe_dao.dart';
export 'dao/ingredient_dao.dart';
export 'dao/meal_plan_dao.dart';
export 'dao/settings_dao.dart';

part 'app_database.g.dart';

// ── Epoch-day helpers ─────────────────────────────────────────────────────────

/// Days since 1970-01-01 UTC (matches Kotlin LocalDate.toEpochDays()).
int dateToEpochDays(DateTime d) {
  final epoch = DateTime.utc(1970, 1, 1);
  final day = DateTime.utc(d.year, d.month, d.day);
  return day.difference(epoch).inDays;
}

DateTime epochDaysToDate(int days) =>
    DateTime.utc(1970, 1, 1).add(Duration(days: days));

// ── Tables ────────────────────────────────────────────────────────────────────

class RecipesTable extends Table {
  @override
  String get tableName => 'recipes';

  TextColumn get id => text()();
  TextColumn get name => text()();
  TextColumn get type => text()(); // RecipeType.firestoreName
  TextColumn get mealCategories =>
      text().withDefault(const Constant(''))(); // comma-joined
  TextColumn get componentCategory => text().nullable()();
  TextColumn get steps => text().withDefault(const Constant('[]'))(); // JSON array as string
  TextColumn get notes => text().withDefault(const Constant(''))();

  @override
  Set<Column> get primaryKey => {id};
}

class RecipeIngredientsTable extends Table {
  @override
  String get tableName => 'recipe_ingredients';

  TextColumn get id => text()();
  TextColumn get recipeId => text().references(RecipesTable, #id,
      onDelete: KeyAction.cascade)();
  TextColumn get ingredientId => text().nullable()();
  TextColumn get subRecipeId => text().nullable()();
  RealColumn get grams => real().nullable()();
  RealColumn get portions => real().nullable()();

  @override
  Set<Column> get primaryKey => {id};
}

class IngredientsTable extends Table {
  @override
  String get tableName => 'ingredients';

  TextColumn get id => text()();
  TextColumn get name => text()();
  TextColumn get category => text()(); // IngredientCategory.firestoreName
  RealColumn get kcalPer100g => real().withDefault(const Constant(0.0))();
  RealColumn get proteinPer100g => real().withDefault(const Constant(0.0))();
  RealColumn get fatPer100g => real().withDefault(const Constant(0.0))();
  RealColumn get carbsPer100g => real().withDefault(const Constant(0.0))();
  TextColumn get source => text()(); // IngredientSource.firestoreName
  TextColumn get steps =>
      text().withDefault(const Constant(''))(); // pipe-separated

  @override
  Set<Column> get primaryKey => {id};
}

class MealPlansTable extends Table {
  @override
  String get tableName => 'meal_plans';

  TextColumn get id => text()();
  TextColumn get name => text()();
  IntColumn get startDate => integer()(); // epoch days
  IntColumn get endDate => integer()(); // epoch days

  @override
  Set<Column> get primaryKey => {id};
}

class DayPlansTable extends Table {
  @override
  String get tableName => 'day_plans';

  TextColumn get id => text()();
  TextColumn get mealPlanId => text().references(MealPlansTable, #id,
      onDelete: KeyAction.cascade)();
  IntColumn get date => integer()(); // epoch days
  RealColumn get proteinPowderGrams =>
      real().withDefault(const Constant(0.0))();
  RealColumn get kcalTarget => real().withDefault(const Constant(1350.0))();
  RealColumn get proteinTarget => real().withDefault(const Constant(120.0))();

  @override
  Set<Column> get primaryKey => {id};
}

class MealSlotsTable extends Table {
  @override
  String get tableName => 'meal_slots';

  TextColumn get id => text()();
  TextColumn get dayPlanId => text().references(DayPlansTable, #id,
      onDelete: KeyAction.cascade)();
  TextColumn get type => text()(); // MealCategory.firestoreName
  IntColumn get slotIndex => integer().withDefault(const Constant(0))();
  TextColumn get recipeId => text()();

  @override
  Set<Column> get primaryKey => {id};
}

class RecipeRatingsTable extends Table {
  @override
  String get tableName => 'recipe_ratings';

  TextColumn get recipeId => text()();
  IntColumn get stars => integer().nullable()();
  IntColumn get timesScheduled => integer().withDefault(const Constant(0))();
  IntColumn get timesManuallyRemoved =>
      integer().withDefault(const Constant(0))();
  BoolColumn get isPinned => boolean().withDefault(const Constant(false))();
  BoolColumn get isExcluded => boolean().withDefault(const Constant(false))();
  IntColumn get lastScheduledDate => integer().nullable()(); // epoch days

  @override
  Set<Column> get primaryKey => {recipeId};
}

class MealSlotConfigsTable extends Table {
  @override
  String get tableName => 'meal_slot_configs';

  IntColumn get dayOfWeek => integer()(); // 1=Monday … 7=Sunday (ISO)
  BoolColumn get hasBreakfast => boolean().withDefault(const Constant(false))();
  BoolColumn get hasLunch => boolean().withDefault(const Constant(true))();
  BoolColumn get hasDinner => boolean().withDefault(const Constant(true))();
  IntColumn get snackCount => integer().withDefault(const Constant(0))();

  @override
  Set<Column> get primaryKey => {dayOfWeek};
}

class BatchCookingGroupsTable extends Table {
  @override
  String get tableName => 'batch_cooking_groups';

  TextColumn get id => text()();
  TextColumn get category => text()(); // MealCategory.firestoreName
  TextColumn get days => text()(); // comma-joined ISO day numbers
  IntColumn get batchNumber => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class OptimizerRulesTable extends Table {
  @override
  String get tableName => 'optimizer_rules';

  TextColumn get id => text()();
  TextColumn get type => text()(); // RuleTargetType.firestoreName
  TextColumn get target => text()();
  TextColumn get targetName => text()();
  TextColumn get constraint => text()(); // ConstraintType.firestoreName
  IntColumn get value => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

// ── Database ──────────────────────────────────────────────────────────────────

@DriftDatabase(
  tables: [
    RecipesTable,
    RecipeIngredientsTable,
    IngredientsTable,
    MealPlansTable,
    DayPlansTable,
    MealSlotsTable,
    RecipeRatingsTable,
    MealSlotConfigsTable,
    BatchCookingGroupsTable,
    OptimizerRulesTable,
  ],
  daos: [
    RecipeDao,
    IngredientDao,
    MealPlanDao,
    SettingsDao,
  ],
)
class AppDatabase extends _$AppDatabase {
  AppDatabase([QueryExecutor? executor]) : super(executor ?? _openConnection());

  @override
  int get schemaVersion => 1;
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dir = await getApplicationDocumentsDirectory();
    final file = File(p.join(dir.path, 'foodplan.db'));
    return NativeDatabase.createInBackground(file);
  });
}
