import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/model/meal_plan_config.dart';
import '../ui/profile/profile_provider.dart';
import 'navigation/app_router.dart';
import 'theme/app_theme.dart';

class AppRoot extends ConsumerWidget {
  const AppRoot({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final appTheme = ref.watch(profileProvider).valueOrNull ?? AppTheme.system;

    return MaterialApp.router(
      title: 'FoodPlan',
      theme: lightTheme,
      darkTheme: darkTheme,
      themeMode: switch (appTheme) {
        AppTheme.light  => ThemeMode.light,
        AppTheme.dark   => ThemeMode.dark,
        AppTheme.system => ThemeMode.system,
      },
      routerConfig: appRouter,
      debugShowCheckedModeBanner: false,
    );
  }
}
