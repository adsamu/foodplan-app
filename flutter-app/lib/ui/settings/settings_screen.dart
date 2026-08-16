import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/meal_plan_config.dart';
import 'package:go_router/go_router.dart';
import 'package:uuid/uuid.dart';

import 'settings_provider.dart';

// ── Screen ────────────────────────────────────────────────────────────────────

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncConfig = ref.watch(settingsNotifierProvider);

    return DefaultTabController(
      length: 4,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Settings'),
          actions: [
            IconButton(
              icon: const Icon(Icons.person),
              onPressed: () => context.go('/profile'),
            ),
          ],
          bottom: const TabBar(
            tabs: [
              Tab(text: 'Schedule'),
              Tab(text: 'Goals'),
              Tab(text: 'Diet'),
              Tab(text: 'Rules'),
            ],
          ),
        ),
        body: asyncConfig.when(
          loading: () =>
              const Center(child: CircularProgressIndicator()),
          error: (err, _) =>
              Center(child: Text('Error loading settings: $err')),
          data: (config) => TabBarView(
            children: [
              _ScheduleTab(config: config),
              _GoalsTab(config: config),
              _DietTab(config: config),
              _RulesTab(config: config),
            ],
          ),
        ),
      ),
    );
  }
}

// ── Schedule tab ──────────────────────────────────────────────────────────────

class _ScheduleTab extends ConsumerWidget {
  final MealPlanConfig config;
  const _ScheduleTab({required this.config});

  static const _dayNames = {
    1: 'Monday',
    2: 'Tuesday',
    3: 'Wednesday',
    4: 'Thursday',
    5: 'Friday',
    6: 'Saturday',
    7: 'Sunday',
  };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notifier = ref.read(settingsNotifierProvider.notifier);
    final schedule = config.schedule;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        for (int day = 1; day <= 7; day++) ...[
          _DayRow(
            dayName: _dayNames[day]!,
            slot: schedule.perDay[day] ??
                const DaySlotConfig(hasLunch: true, hasDinner: true),
            onChanged: (updated) => notifier.setDaySlot(day, updated),
          ),
          const Divider(),
        ],
        const SizedBox(height: 16),
        SwitchListTile(
          title: const Text('Snack optional fill'),
          subtitle: const Text(
              'Allow snacks to fill remaining kcal automatically'),
          value: schedule.snackOptionalFill,
          onChanged: (v) => notifier.setSnackOptionalFill(v),
        ),
      ],
    );
  }
}

class _DayRow extends StatelessWidget {
  final String dayName;
  final DaySlotConfig slot;
  final void Function(DaySlotConfig) onChanged;

  const _DayRow({
    required this.dayName,
    required this.slot,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(dayName,
              style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 6),
          Wrap(
            spacing: 8,
            children: [
              FilterChip(
                label: const Text('Breakfast'),
                selected: slot.hasBreakfast,
                onSelected: (v) =>
                    onChanged(slot.copyWith(hasBreakfast: v)),
              ),
              FilterChip(
                label: const Text('Lunch'),
                selected: slot.hasLunch,
                onSelected: (v) => onChanged(slot.copyWith(hasLunch: v)),
              ),
              FilterChip(
                label: const Text('Dinner'),
                selected: slot.hasDinner,
                onSelected: (v) =>
                    onChanged(slot.copyWith(hasDinner: v)),
              ),
              // Snack count stepper
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text('Snacks:'),
                  IconButton(
                    icon: const Icon(Icons.remove, size: 18),
                    padding: EdgeInsets.zero,
                    constraints: const BoxConstraints(),
                    onPressed: slot.snackCount > 0
                        ? () => onChanged(slot.copyWith(
                            snackCount: slot.snackCount - 1))
                        : null,
                  ),
                  Text('${slot.snackCount}'),
                  IconButton(
                    icon: const Icon(Icons.add, size: 18),
                    padding: EdgeInsets.zero,
                    constraints: const BoxConstraints(),
                    onPressed: () => onChanged(
                        slot.copyWith(snackCount: slot.snackCount + 1)),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ── Goals tab ─────────────────────────────────────────────────────────────────

class _GoalsTab extends ConsumerStatefulWidget {
  final MealPlanConfig config;
  const _GoalsTab({required this.config});

  @override
  ConsumerState<_GoalsTab> createState() => _GoalsTabState();
}

class _GoalsTabState extends ConsumerState<_GoalsTab> {
  late TextEditingController _kcalCtrl;
  late TextEditingController _proteinCtrl;
  late TextEditingController _fatCtrl;
  late TextEditingController _carbsCtrl;
  late TextEditingController _minKcalCtrl;
  late TextEditingController _maxKcalCtrl;

  @override
  void initState() {
    super.initState();
    final goals = widget.config.goals;
    _kcalCtrl = TextEditingController(
        text: goals.kcalTarget.toStringAsFixed(0));
    _proteinCtrl = TextEditingController(
        text: goals.proteinTarget?.toStringAsFixed(0) ?? '');
    _fatCtrl = TextEditingController(
        text: goals.fatTarget?.toStringAsFixed(0) ?? '');
    _carbsCtrl = TextEditingController(
        text: goals.carbsTarget?.toStringAsFixed(0) ?? '');
    _minKcalCtrl = TextEditingController(
        text: goals.minKcalPerDay?.toStringAsFixed(0) ?? '');
    _maxKcalCtrl = TextEditingController(
        text: goals.maxKcalPerDay?.toStringAsFixed(0) ?? '');
  }

  @override
  void dispose() {
    _kcalCtrl.dispose();
    _proteinCtrl.dispose();
    _fatCtrl.dispose();
    _carbsCtrl.dispose();
    _minKcalCtrl.dispose();
    _maxKcalCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final notifier = ref.read(settingsNotifierProvider.notifier);
    final goals = widget.config.goals;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        // Kcal target
        TextField(
          controller: _kcalCtrl,
          decoration: const InputDecoration(
            labelText: 'Kcal target',
            border: OutlineInputBorder(),
            suffixText: 'kcal',
          ),
          keyboardType:
              const TextInputType.numberWithOptions(decimal: true),
          onSubmitted: (v) {
            final d = double.tryParse(v);
            if (d != null) notifier.setKcalTarget(d);
          },
        ),
        const SizedBox(height: 16),

        // Auto macro selector
        Text('Auto-calculate',
            style: Theme.of(context).textTheme.titleSmall),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          children: MacroField.values.map((field) {
            final label = switch (field) {
              MacroField.protein => 'Protein',
              MacroField.fat => 'Fat',
              MacroField.carbs => 'Carbs',
            };
            return FilterChip(
              label: Text(label),
              selected: goals.autoField == field,
              onSelected: (_) => notifier.setAutoField(field),
            );
          }).toList(),
        ),
        const SizedBox(height: 16),

        // Manual macro targets (shown when not auto)
        if (goals.autoField != MacroField.protein) ...[
          TextField(
            controller: _proteinCtrl,
            decoration: const InputDecoration(
              labelText: 'Protein target',
              border: OutlineInputBorder(),
              suffixText: 'g',
            ),
            keyboardType:
                const TextInputType.numberWithOptions(decimal: true),
            onSubmitted: (v) =>
                notifier.setProteinTarget(double.tryParse(v)),
          ),
          const SizedBox(height: 12),
        ],
        if (goals.autoField != MacroField.fat) ...[
          TextField(
            controller: _fatCtrl,
            decoration: const InputDecoration(
              labelText: 'Fat target',
              border: OutlineInputBorder(),
              suffixText: 'g',
            ),
            keyboardType:
                const TextInputType.numberWithOptions(decimal: true),
            onSubmitted: (v) =>
                notifier.setFatTarget(double.tryParse(v)),
          ),
          const SizedBox(height: 12),
        ],
        if (goals.autoField != MacroField.carbs) ...[
          TextField(
            controller: _carbsCtrl,
            decoration: const InputDecoration(
              labelText: 'Carbs target',
              border: OutlineInputBorder(),
              suffixText: 'g',
            ),
            keyboardType:
                const TextInputType.numberWithOptions(decimal: true),
            onSubmitted: (v) =>
                notifier.setCarbsTarget(double.tryParse(v)),
          ),
          const SizedBox(height: 16),
        ],

        // Min/max kcal per day
        Text('Daily range (optional)',
            style: Theme.of(context).textTheme.titleSmall),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _minKcalCtrl,
                decoration: const InputDecoration(
                  labelText: 'Min kcal/day',
                  border: OutlineInputBorder(),
                ),
                keyboardType: const TextInputType.numberWithOptions(
                    decimal: true),
                onSubmitted: (v) async {
                  final d = double.tryParse(v);
                  await ref
                      .read(settingsRepositoryProvider)
                      .setMinKcal(d);
                },
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextField(
                controller: _maxKcalCtrl,
                decoration: const InputDecoration(
                  labelText: 'Max kcal/day',
                  border: OutlineInputBorder(),
                ),
                keyboardType: const TextInputType.numberWithOptions(
                    decimal: true),
                onSubmitted: (v) async {
                  final d = double.tryParse(v);
                  await ref
                      .read(settingsRepositoryProvider)
                      .setMaxKcal(d);
                },
              ),
            ),
          ],
        ),
      ],
    );
  }
}

// ── Diet tab ──────────────────────────────────────────────────────────────────

class _DietTab extends ConsumerWidget {
  final MealPlanConfig config;
  const _DietTab({required this.config});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notifier = ref.read(settingsNotifierProvider.notifier);
    final diet = config.diet;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('Diet type',
            style: Theme.of(context).textTheme.titleSmall),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 4,
          children: DietType.values.map((type) {
            final label = switch (type) {
              DietType.vegetarian => 'Vegetarian',
              DietType.vegan => 'Vegan',
              DietType.pescatarian => 'Pescatarian',
              DietType.keto => 'Keto',
            };
            return FilterChip(
              label: Text(label),
              selected: diet.dietTypes.contains(type),
              onSelected: (_) => notifier.toggleDietType(type),
            );
          }).toList(),
        ),
        const SizedBox(height: 24),
        Text('Allergies & intolerances',
            style: Theme.of(context).textTheme.titleSmall),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 4,
          children: AllergyType.values.map((allergy) {
            final label = switch (allergy) {
              AllergyType.gluten => 'Gluten',
              AllergyType.dairy => 'Dairy',
              AllergyType.nuts => 'Nuts',
              AllergyType.shellfish => 'Shellfish',
              AllergyType.eggs => 'Eggs',
              AllergyType.soy => 'Soy',
              AllergyType.pork => 'Pork',
            };
            return FilterChip(
              label: Text(label),
              selected: diet.allergies.contains(allergy),
              onSelected: (_) => notifier.toggleAllergy(allergy),
            );
          }).toList(),
        ),
      ],
    );
  }
}

// ── Rules tab ─────────────────────────────────────────────────────────────────

class _RulesTab extends ConsumerWidget {
  final MealPlanConfig config;
  const _RulesTab({required this.config});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notifier = ref.read(settingsNotifierProvider.notifier);
    final rules = config.rules;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        for (final rule in rules) ...[
          _RuleCard(
            rule: rule,
            onDelete: () => notifier.deleteRule(rule.id),
          ),
          const SizedBox(height: 8),
        ],
        const SizedBox(height: 8),
        OutlinedButton.icon(
          icon: const Icon(Icons.add),
          label: const Text('Add rule'),
          onPressed: () => _showAddRuleDialog(context, notifier),
        ),
      ],
    );
  }

  void _showAddRuleDialog(
      BuildContext context, SettingsNotifier notifier) {
    showDialog<void>(
      context: context,
      builder: (ctx) => _AddRuleDialog(onSave: notifier.addRule),
    );
  }
}

class _RuleCard extends StatelessWidget {
  final OptimizerRule rule;
  final VoidCallback onDelete;

  const _RuleCard({required this.rule, required this.onDelete});

  @override
  Widget build(BuildContext context) {
    final constraintLabel = switch (rule.constraint) {
      ConstraintType.minPerWeek => 'min/week',
      ConstraintType.maxPerWeek => 'max/week',
    };
    final typeLabel = switch (rule.type) {
      RuleTargetType.dietCategory => 'Category',
      RuleTargetType.ingredient => 'Ingredient',
    };

    return Card(
      child: ListTile(
        title: Text(rule.targetName),
        subtitle:
            Text('$typeLabel · ${rule.value} $constraintLabel'),
        trailing: IconButton(
          icon: const Icon(Icons.delete_outline),
          onPressed: onDelete,
        ),
      ),
    );
  }
}

class _AddRuleDialog extends StatefulWidget {
  final Future<void> Function(OptimizerRule) onSave;
  const _AddRuleDialog({required this.onSave});

  @override
  State<_AddRuleDialog> createState() => _AddRuleDialogState();
}

class _AddRuleDialogState extends State<_AddRuleDialog> {
  final _nameCtrl = TextEditingController();
  RuleTargetType _type = RuleTargetType.dietCategory;
  ConstraintType _constraint = ConstraintType.minPerWeek;
  int _value = 1;
  final _uuid = const Uuid();

  @override
  void dispose() {
    _nameCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Add rule'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextField(
            controller: _nameCtrl,
            decoration: const InputDecoration(
              labelText: 'Target name',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          SegmentedButton<RuleTargetType>(
            segments: const [
              ButtonSegment(
                  value: RuleTargetType.dietCategory,
                  label: Text('Category')),
              ButtonSegment(
                  value: RuleTargetType.ingredient,
                  label: Text('Ingredient')),
            ],
            selected: {_type},
            onSelectionChanged: (s) =>
                setState(() => _type = s.first),
          ),
          const SizedBox(height: 12),
          SegmentedButton<ConstraintType>(
            segments: const [
              ButtonSegment(
                  value: ConstraintType.minPerWeek,
                  label: Text('Min/week')),
              ButtonSegment(
                  value: ConstraintType.maxPerWeek,
                  label: Text('Max/week')),
            ],
            selected: {_constraint},
            onSelectionChanged: (s) =>
                setState(() => _constraint = s.first),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              const Text('Value: '),
              IconButton(
                icon: const Icon(Icons.remove),
                onPressed: _value > 1
                    ? () => setState(() => _value--)
                    : null,
              ),
              Text('$_value'),
              IconButton(
                icon: const Icon(Icons.add),
                onPressed: () => setState(() => _value++),
              ),
            ],
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: () {
            if (_nameCtrl.text.trim().isEmpty) return;
            final rule = OptimizerRule(
              id: _uuid.v4(),
              type: _type,
              target: _nameCtrl.text.trim().toLowerCase(),
              targetName: _nameCtrl.text.trim(),
              constraint: _constraint,
              value: _value,
            );
            widget.onSave(rule);
            Navigator.of(context).pop();
          },
          child: const Text('Save'),
        ),
      ],
    );
  }
}
