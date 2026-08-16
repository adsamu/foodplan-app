import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:cloud_functions/cloud_functions.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/data/local/app_database.dart';
import 'package:foodplan/data/repository/database_seeder.dart';
import 'package:foodplan/data/repository/ingredient_repository.dart';
import 'package:foodplan/data/repository/meal_plan_repository.dart';
import 'package:foodplan/data/repository/recipe_rating_repository.dart';
import 'package:foodplan/data/repository/recipe_repository.dart';
import 'package:foodplan/data/repository/settings_repository.dart';
import 'package:foodplan/domain/model/meal_plan_config.dart';
import 'package:shared_preferences/shared_preferences.dart';

// ── Infrastructure ────────────────────────────────────────────────────────────

final appDatabaseProvider = Provider<AppDatabase>((_) => AppDatabase());

final firestoreProvider =
    Provider<FirebaseFirestore>((_) => FirebaseFirestore.instance);

final firebaseFunctionsProvider = Provider<FirebaseFunctions>(
  (_) => FirebaseFunctions.instanceFor(region: 'europe-north1'),
);

/// Async — pre-warmed in main() so requireValue is safe on first watch.
final sharedPreferencesProvider =
    FutureProvider<SharedPreferences>((_) => SharedPreferences.getInstance());

// ── Repositories ──────────────────────────────────────────────────────────────

final recipeRepositoryProvider = Provider<RecipeRepository>((ref) =>
    RecipeRepository(
      db: ref.watch(appDatabaseProvider),
      firestore: ref.watch(firestoreProvider),
    ));

final ingredientRepositoryProvider = Provider<IngredientRepository>((ref) =>
    IngredientRepository(
      db: ref.watch(appDatabaseProvider),
      firestore: ref.watch(firestoreProvider),
    ));

final mealPlanRepositoryProvider = Provider<MealPlanRepository>((ref) =>
    MealPlanRepository(
      db: ref.watch(appDatabaseProvider),
      firestore: ref.watch(firestoreProvider),
    ));

final settingsRepositoryProvider = Provider<SettingsRepository>((ref) {
  final prefs = ref.watch(sharedPreferencesProvider).requireValue;
  return SettingsRepository(
    prefs: prefs,
    dao: ref.watch(appDatabaseProvider).settingsDao,
  );
});

final recipeRatingRepositoryProvider =
    Provider<RecipeRatingRepository>((ref) => RecipeRatingRepository(
          firestore: ref.watch(firestoreProvider),
        ));

final databaseSeederProvider = Provider<DatabaseSeeder>((ref) => DatabaseSeeder(
      ingredientRepository: ref.watch(ingredientRepositoryProvider),
      recipeRepository: ref.watch(recipeRepositoryProvider),
    ));

// ── Derived streams ───────────────────────────────────────────────────────────

final settingsStreamProvider = StreamProvider<MealPlanConfig>((ref) {
  final repo = ref.watch(settingsRepositoryProvider);
  return repo.watchMealPlanConfig();
});
