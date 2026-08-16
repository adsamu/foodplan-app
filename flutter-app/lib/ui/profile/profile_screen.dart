import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/domain/model/meal_plan_config.dart';

import 'profile_provider.dart';

// ── Screen ────────────────────────────────────────────────────────────────────

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncTheme = ref.watch(profileProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('App settings'),
        leading: const BackButton(),
      ),
      body: asyncTheme.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Center(child: Text('Error: $err')),
        data: (currentTheme) => ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'APPEARANCE',
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(
                            letterSpacing: 1.2,
                            color: Theme.of(context).colorScheme.outline,
                          ),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        const Expanded(
                          child: Text('Theme'),
                        ),
                        Wrap(
                          spacing: 8,
                          children: AppTheme.values.map((theme) {
                            final label = switch (theme) {
                              AppTheme.system => 'System',
                              AppTheme.light => 'Light',
                              AppTheme.dark => 'Dark',
                            };
                            return FilterChip(
                              label: Text(label),
                              selected: currentTheme == theme,
                              onSelected: (_) => ref
                                  .read(profileProvider.notifier)
                                  .setTheme(theme),
                            );
                          }).toList(),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
