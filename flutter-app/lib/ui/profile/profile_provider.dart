import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/meal_plan_config.dart';

// ── Provider ──────────────────────────────────────────────────────────────────

final profileProvider =
    AsyncNotifierProvider<ProfileNotifier, AppTheme>(ProfileNotifier.new);

class ProfileNotifier extends AsyncNotifier<AppTheme> {
  @override
  Future<AppTheme> build() async {
    final repo = ref.watch(settingsRepositoryProvider);
    // watchTheme emits the current value once (SharedPreferences-backed).
    return await repo.watchTheme().first;
  }

  Future<void> setTheme(AppTheme theme) async {
    await ref.read(settingsRepositoryProvider).setTheme(theme);
    state = AsyncData(theme);
    // Also refresh the global settings stream so the app theme updates.
    ref.invalidate(settingsStreamProvider);
  }
}
