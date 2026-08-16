import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'di/providers.dart';
import 'firebase_options.dart';
import 'ui/app.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  final prefs = await SharedPreferences.getInstance();
  runApp(
    ProviderScope(
      overrides: [
        sharedPreferencesProvider.overrideWith((ref) => Future.value(prefs)),
      ],
      child: const FoodPlanApp(),
    ),
  );
}

class FoodPlanApp extends ConsumerStatefulWidget {
  const FoodPlanApp({super.key});

  @override
  ConsumerState<FoodPlanApp> createState() => _FoodPlanAppState();
}

class _FoodPlanAppState extends ConsumerState<FoodPlanApp> {
  @override
  void initState() {
    super.initState();
    _startListeners();
  }

  Future<void> _startListeners() async {
    final recipeRepo = ref.read(recipeRepositoryProvider);
    final ingredientRepo = ref.read(ingredientRepositoryProvider);
    final mealPlanRepo = ref.read(mealPlanRepositoryProvider);
    final seeder = ref.read(databaseSeederProvider);

    recipeRepo.startListening();
    ingredientRepo.startListening();
    mealPlanRepo.startListening('default_user');
    await seeder.seedIfEmpty();
  }

  @override
  Widget build(BuildContext context) => const AppRoot();
}
