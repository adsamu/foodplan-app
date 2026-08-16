import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import 'meal_plan_provider.dart';

// ── Date formatters ───────────────────────────────────────────────────────────

final _dayFmt = DateFormat('EEE, MMM d');
final _shortDayFmt = DateFormat('EEE');
final _monthFmt = DateFormat('MMMM yyyy');
final _weekDayFmt = DateFormat('MMM d');

// ── Screen ────────────────────────────────────────────────────────────────────

class MealPlanScreen extends ConsumerStatefulWidget {
  const MealPlanScreen({super.key});

  @override
  ConsumerState<MealPlanScreen> createState() => _MealPlanScreenState();
}

class _MealPlanScreenState extends ConsumerState<MealPlanScreen> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final optimizerState = ref.watch(optimizerStateProvider);
    final view = ref.watch(planViewProvider);
    final actions = ref.read(mealPlanActionsProvider);

    // Show SnackBar when optimizer finishes
    ref.listen<OptimizerState>(optimizerStateProvider, (prev, next) {
      switch (next) {
        case OptimizerSuccess(:final planName):
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Plan "$planName" generated!')),
          );
          ref.read(optimizerStateProvider.notifier).state =
              const OptimizerState.idle();
        case OptimizerError(:final message):
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error: $message')),
          );
          ref.read(optimizerStateProvider.notifier).state =
              const OptimizerState.idle();
        default:
          break;
      }
    });

    final isRunning = optimizerState is OptimizerRunning;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Meal Plan'),
        actions: [
          _OptimizerButton(isRunning: isRunning, onPressed: () {
            final date = ref.read(planDateProvider);
            final dayOffset = date.weekday - 1;
            final monday = date.subtract(Duration(days: dayOffset));
            actions.generatePlan(monday);
          }),
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => context.go('/settings'),
          ),
        ],
      ),
      body: Column(
        children: [
          // View toggle
          Padding(
            padding:
                const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: _ViewToggle(
              selected: view,
              onChanged: actions.onViewChange,
            ),
          ),

          // Stats card
          const _StatsCard(),

          // Content
          Expanded(
            child: switch (view) {
              PlanView.day => const _DayView(),
              PlanView.week => const _WeekView(),
              PlanView.month => const _MonthView(),
            },
          ),
        ],
      ),
    );
  }
}

// ── Optimizer button (spins while running) ────────────────────────────────────

class _OptimizerButton extends StatefulWidget {
  final bool isRunning;
  final VoidCallback onPressed;

  const _OptimizerButton({
    required this.isRunning,
    required this.onPressed,
  });

  @override
  State<_OptimizerButton> createState() => _OptimizerButtonState();
}

class _OptimizerButtonState extends State<_OptimizerButton>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 1),
    );
    if (widget.isRunning) _controller.repeat();
  }

  @override
  void didUpdateWidget(_OptimizerButton oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.isRunning && !_controller.isAnimating) {
      _controller.repeat();
    } else if (!widget.isRunning && _controller.isAnimating) {
      _controller.stop();
      _controller.reset();
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: RotationTransition(
        turns: _controller,
        child: const Icon(Icons.autorenew),
      ),
      onPressed: widget.isRunning ? null : widget.onPressed,
    );
  }
}

// ── View toggle ───────────────────────────────────────────────────────────────

class _ViewToggle extends StatelessWidget {
  final PlanView selected;
  final void Function(PlanView) onChanged;

  const _ViewToggle({required this.selected, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    return SegmentedButton<PlanView>(
      segments: const [
        ButtonSegment(value: PlanView.day, label: Text('Day')),
        ButtonSegment(value: PlanView.week, label: Text('Week')),
        ButtonSegment(value: PlanView.month, label: Text('Month')),
      ],
      selected: {selected},
      onSelectionChanged: (s) => onChanged(s.first),
    );
  }
}

// ── Stats card ────────────────────────────────────────────────────────────────

class _StatsCard extends ConsumerWidget {
  const _StatsCard();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final expanded = ref.watch(statsExpandedProvider);
    final view = ref.watch(planViewProvider);
    final date = ref.watch(planDateProvider);
    final actions = ref.read(mealPlanActionsProvider);

    String primaryLabel;
    String subtitle;
    double kcal = 0, protein = 0, fat = 0, carbs = 0;
    int? kcalTarget;

    switch (view) {
      case PlanView.day:
        final asyncDay = ref.watch(dayUiStateProvider);
        asyncDay.whenData((day) {
          if (day != null) {
            kcal = day.nutrition.kcal;
            protein = day.nutrition.protein;
            fat = day.nutrition.fat;
            carbs = day.nutrition.carbs;
            kcalTarget = day.kcalTarget;
          }
        });
        primaryLabel = '${kcal.round()} kcal';
        if (kcalTarget != null) primaryLabel += ' / $kcalTarget';
        subtitle = _dayFmt.format(date);
      case PlanView.week:
        final asyncWeek = ref.watch(weekUiStateProvider);
        asyncWeek.whenData((week) {
          if (week != null) {
            kcal = week.avgKcal;
            protein = week.avgProtein;
            fat = week.avgFat;
            carbs = week.avgCarbs;
          }
        });
        primaryLabel = '~${kcal.round()} kcal/day';
        final dayOffset = date.weekday - 1;
        final monday = date.subtract(Duration(days: dayOffset));
        final sunday = monday.add(const Duration(days: 6));
        subtitle =
            '${_weekDayFmt.format(monday)} – ${_weekDayFmt.format(sunday)}';
      case PlanView.month:
        final asyncMonth = ref.watch(monthUiStateProvider);
        asyncMonth.whenData((month) {
          if (month != null) {
            kcal = month.avgKcal;
            protein = month.avgProtein;
            fat = month.avgFat;
            carbs = month.avgCarbs;
          }
        });
        primaryLabel = '~${kcal.round()} kcal/day';
        subtitle = _monthFmt.format(date);
    }

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: actions.toggleStats,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          primaryLabel,
                          style: Theme.of(context)
                              .textTheme
                              .titleMedium
                              ?.copyWith(fontWeight: FontWeight.w600),
                        ),
                        Text(
                          subtitle,
                          style: Theme.of(context)
                              .textTheme
                              .bodySmall
                              ?.copyWith(
                                color:
                                    Theme.of(context).colorScheme.outline,
                              ),
                        ),
                      ],
                    ),
                  ),
                  Icon(expanded ? Icons.expand_less : Icons.expand_more),
                ],
              ),
              if (expanded) ...[
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    _MacroCell(
                      label: 'Protein',
                      value: '${protein.round()}g',
                      color: const Color(0xFF534AB7),
                    ),
                    _MacroCell(
                      label: 'Fat',
                      value: '${fat.round()}g',
                      color: const Color(0xFFBA7517),
                    ),
                    _MacroCell(
                      label: 'Carbs',
                      value: '${carbs.round()}g',
                      color: const Color(0xFF1D9E75),
                    ),
                  ],
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _MacroCell extends StatelessWidget {
  final String label;
  final String value;
  final Color color;

  const _MacroCell({
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          value,
          style: Theme.of(context)
              .textTheme
              .titleSmall
              ?.copyWith(color: color, fontWeight: FontWeight.w600),
        ),
        Text(
          label,
          style: Theme.of(context)
              .textTheme
              .bodySmall
              ?.copyWith(color: Theme.of(context).colorScheme.outline),
        ),
      ],
    );
  }
}

// ── Navigation row ─────────────────────────────────────────────────────────────

class _NavigationRow extends StatelessWidget {
  final String label;
  final VoidCallback onPrevious;
  final VoidCallback onNext;

  const _NavigationRow({
    required this.label,
    required this.onPrevious,
    required this.onNext,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        IconButton(
          icon: const Icon(Icons.chevron_left),
          onPressed: onPrevious,
        ),
        Text(
          label,
          style: Theme.of(context).textTheme.titleSmall,
        ),
        IconButton(
          icon: const Icon(Icons.chevron_right),
          onPressed: onNext,
        ),
      ],
    );
  }
}

// ── Day view ──────────────────────────────────────────────────────────────────

class _DayView extends ConsumerWidget {
  const _DayView();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncDay = ref.watch(dayUiStateProvider);
    final date = ref.watch(planDateProvider);
    final actions = ref.read(mealPlanActionsProvider);
    final checkedMeals = ref.watch(checkedMealsProvider);

    return GestureDetector(
      onHorizontalDragEnd: (details) {
        if (details.primaryVelocity == null) return;
        if (details.primaryVelocity! < -200) {
          actions.navigateNext();
        } else if (details.primaryVelocity! > 200) {
          actions.navigatePrevious();
        }
      },
      child: Column(
        children: [
          _NavigationRow(
            label: _dayFmt.format(date),
            onPrevious: actions.navigatePrevious,
            onNext: actions.navigateNext,
          ),
          Expanded(
            child: asyncDay.when(
              loading: () =>
                  const Center(child: CircularProgressIndicator()),
              error: (err, _) =>
                  Center(child: Text('Error: $err')),
              data: (day) {
                if (day == null) {
                  return const Center(
                    child: Text(
                      'No meals planned for this day.',
                      style: TextStyle(fontSize: 16),
                    ),
                  );
                }
                final dateKey = _dateKeyFromDate(day.date);
                final checked = checkedMeals[dateKey] ?? {};

                return ListView.builder(
                  padding: const EdgeInsets.all(8),
                  itemCount: day.meals.length,
                  itemBuilder: (ctx, i) {
                    final meal = day.meals[i];
                    final isChecked = checked.contains(i);
                    return _MealSlotCard(
                      meal: meal,
                      index: i,
                      isChecked: isChecked,
                      onTap: () =>
                          context.push('/recipes/${meal.recipeId}'),
                      onToggleCheck: () =>
                          actions.toggleMealChecked(day.date, i),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _MealSlotCard extends StatelessWidget {
  final MealSlotUi meal;
  final int index;
  final bool isChecked;
  final VoidCallback onTap;
  final VoidCallback onToggleCheck;

  const _MealSlotCard({
    required this.meal,
    required this.index,
    required this.isChecked,
    required this.onTap,
    required this.onToggleCheck,
  });

  IconData _iconForCategory(MealCategory cat) => switch (cat) {
        MealCategory.breakfast => Icons.free_breakfast_outlined,
        MealCategory.lunch => Icons.lunch_dining_outlined,
        MealCategory.dinner => Icons.dinner_dining_outlined,
        MealCategory.snack => Icons.local_cafe_outlined,
      };

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Card(
      child: ListTile(
        leading: Icon(
          _iconForCategory(meal.type),
          color: colorScheme.primary,
        ),
        title: Text(
          meal.recipeName,
          style: isChecked
              ? TextStyle(
                  decoration: TextDecoration.lineThrough,
                  color: colorScheme.outline,
                )
              : null,
        ),
        subtitle: Text(
          _categoryLabel(meal.type),
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: colorScheme.outline,
              ),
        ),
        trailing: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              '${meal.kcal.round()} kcal',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            const SizedBox(width: 8),
            Checkbox(
              value: isChecked,
              onChanged: (_) => onToggleCheck(),
            ),
          ],
        ),
        onTap: onTap,
      ),
    );
  }

  String _categoryLabel(MealCategory cat) => switch (cat) {
        MealCategory.breakfast => 'Breakfast',
        MealCategory.lunch => 'Lunch',
        MealCategory.dinner => 'Dinner',
        MealCategory.snack => 'Snack',
      };
}

// ── Week view ─────────────────────────────────────────────────────────────────

class _WeekView extends ConsumerWidget {
  const _WeekView();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncWeek = ref.watch(weekUiStateProvider);
    final date = ref.watch(planDateProvider);
    final actions = ref.read(mealPlanActionsProvider);

    final dayOffset = date.weekday - 1;
    final monday = date.subtract(Duration(days: dayOffset));
    final sunday = monday.add(const Duration(days: 6));
    final weekLabel =
        '${_weekDayFmt.format(monday)} – ${_weekDayFmt.format(sunday)}';

    return Column(
      children: [
        _NavigationRow(
          label: weekLabel,
          onPrevious: actions.navigatePrevious,
          onNext: actions.navigateNext,
        ),
        Expanded(
          child: asyncWeek.when(
            loading: () =>
                const Center(child: CircularProgressIndicator()),
            error: (err, _) =>
                Center(child: Text('Error: $err')),
            data: (week) {
              if (week == null) {
                return const Center(
                  child: Text('No meal plan for this week.'),
                );
              }
              return ListView.builder(
                padding: const EdgeInsets.symmetric(
                    horizontal: 12, vertical: 4),
                itemCount: week.days.length,
                itemBuilder: (ctx, i) =>
                    _WeekDayCard(day: week.days[i]),
              );
            },
          ),
        ),
      ],
    );
  }
}

class _WeekDayCard extends StatelessWidget {
  final WeekDayUi day;

  const _WeekDayCard({required this.day});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final progress = day.kcalTarget > 0
        ? (day.kcal / day.kcalTarget).clamp(0.0, 1.0)
        : 0.0;

    return Card(
      margin: const EdgeInsets.symmetric(vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text(
                  _shortDayFmt.format(day.date),
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w600,
                        color: day.isToday ? colorScheme.primary : null,
                      ),
                ),
                const SizedBox(width: 6),
                Text(
                  day.date.day.toString(),
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: day.isToday ? colorScheme.primary : null,
                      ),
                ),
                const Spacer(),
                Text(
                  '${day.kcal.round()} / ${day.kcalTarget.round()} kcal',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: colorScheme.outline,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 6),
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: progress,
                minHeight: 6,
                backgroundColor: colorScheme.surfaceContainerHighest,
                valueColor:
                    AlwaysStoppedAnimation<Color>(colorScheme.primary),
              ),
            ),
            if (day.mealNames.isNotEmpty) ...[
              const SizedBox(height: 6),
              Text(
                day.mealNames.join(' · '),
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: colorScheme.onSurface,
                    ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ],
        ),
      ),
    );
  }
}

// ── Month view ────────────────────────────────────────────────────────────────

class _MonthView extends ConsumerWidget {
  const _MonthView();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncMonth = ref.watch(monthUiStateProvider);
    final date = ref.watch(planDateProvider);
    final actions = ref.read(mealPlanActionsProvider);

    return Column(
      children: [
        _NavigationRow(
          label: _monthFmt.format(date),
          onPrevious: actions.navigatePrevious,
          onNext: actions.navigateNext,
        ),
        Expanded(
          child: asyncMonth.when(
            loading: () =>
                const Center(child: CircularProgressIndicator()),
            error: (err, _) =>
                Center(child: Text('Error: $err')),
            data: (month) {
              if (month == null) {
                return const Center(
                    child: Text('No data for this month.'));
              }
              return _MonthGrid(month: month);
            },
          ),
        ),
      ],
    );
  }
}

class _MonthGrid extends StatelessWidget {
  final MonthUiState month;

  const _MonthGrid({required this.month});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    // Day-of-week header
    const headers = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

    // Padding before first day (weekday is 1-based Mon=1)
    final firstDayPadding =
        month.days.isNotEmpty ? month.days.first.date.weekday - 1 : 0;

    return GridView.builder(
      padding: const EdgeInsets.all(12),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 7,
        childAspectRatio: 1,
        crossAxisSpacing: 4,
        mainAxisSpacing: 4,
      ),
      itemCount: headers.length + firstDayPadding + month.days.length,
      itemBuilder: (ctx, i) {
        // Header row
        if (i < 7) {
          return Center(
            child: Text(
              headers[i],
              style: Theme.of(ctx).textTheme.labelSmall?.copyWith(
                    color: colorScheme.outline,
                  ),
            ),
          );
        }

        final adjustedIndex = i - 7;
        if (adjustedIndex < firstDayPadding) {
          return const SizedBox.shrink();
        }

        final dayIndex = adjustedIndex - firstDayPadding;
        if (dayIndex >= month.days.length) {
          return const SizedBox.shrink();
        }

        final day = month.days[dayIndex];
        return _MonthDayCell(day: day);
      },
    );
  }
}

class _MonthDayCell extends StatelessWidget {
  final MonthDayUi day;

  const _MonthDayCell({required this.day});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    Color? bgColor;
    Color textColor = colorScheme.onSurface;

    if (day.isToday) {
      bgColor = colorScheme.primary;
      textColor = colorScheme.onPrimary;
    } else if (day.isPlanned) {
      bgColor = colorScheme.primaryContainer;
      textColor = colorScheme.onPrimaryContainer;
    }

    return Container(
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            day.date.day.toString(),
            style: Theme.of(context)
                .textTheme
                .bodySmall
                ?.copyWith(color: textColor),
          ),
          if (day.isPlanned)
            Container(
              width: 4,
              height: 4,
              decoration: BoxDecoration(
                color: day.isToday
                    ? colorScheme.onPrimary
                    : colorScheme.primary,
                shape: BoxShape.circle,
              ),
            ),
        ],
      ),
    );
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

String _dateKeyFromDate(DateTime date) =>
    '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
