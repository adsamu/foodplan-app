import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/nutrition.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:go_router/go_router.dart';

// ── State ─────────────────────────────────────────────────────────────────────

class InstructionSection {
  final String title;
  final List<_StepData> steps;

  const InstructionSection({required this.title, required this.steps});
}

class _StepData {
  final String text;
  final bool isTimer;
  final int timerSeconds; // 0 if not a timer

  const _StepData({
    required this.text,
    this.isTimer = false,
    this.timerSeconds = 0,
  });
}

class RecipeDetailState {
  final Recipe recipe;
  final RecipeNutrition nutrition;
  final Map<String, String> ingredientNames; // ingredientId -> name
  final List<InstructionSection> instructionSections;

  const RecipeDetailState({
    required this.recipe,
    required this.nutrition,
    required this.ingredientNames,
    required this.instructionSections,
  });
}

// ── Provider ──────────────────────────────────────────────────────────────────

final recipeDetailProvider = FutureProvider.autoDispose
    .family<RecipeDetailState?, String>((ref, recipeId) async {
  final recipeRepo = ref.read(recipeRepositoryProvider);
  final ingRepo = ref.read(ingredientRepositoryProvider);

  final recipe = await recipeRepo.getRecipeWithIngredients(recipeId);
  if (recipe == null) return null;

  // Build ingredient name map and compute nutrition
  final ingredientNames = <String, String>{};
  var nutrition = RecipeNutrition.zero;

  for (final ri in recipe.ingredients) {
    if (ri.ingredientId != null) {
      final ing = await ingRepo.getIngredientById(ri.ingredientId!);
      if (ing != null) {
        ingredientNames[ri.ingredientId!] = ing.name;
        final grams = ri.grams ?? 0.0;
        nutrition = nutrition + RecipeNutrition.fromIngredient(ing, grams);
      }
    } else if (ri.subRecipeId != null) {
      // Recursively compute sub-recipe nutrition
      final subNutrition =
          await _computeRecipeNutrition(ref, ri.subRecipeId!);
      final portions = ri.portions ?? 1.0;
      nutrition = nutrition + (subNutrition * portions);

      // Name the sub-recipe row
      final subRecipe = await recipeRepo.getRecipeById(ri.subRecipeId!);
      if (subRecipe != null) {
        ingredientNames[ri.subRecipeId!] = subRecipe.name;
      }
    }
  }

  // Parse steps into sections/steps
  final sections = _parseSteps(recipe.steps);

  return RecipeDetailState(
    recipe: recipe,
    nutrition: nutrition,
    ingredientNames: ingredientNames,
    instructionSections: sections,
  );
});

/// Recursively compute nutrition for a sub-recipe.
Future<RecipeNutrition> _computeRecipeNutrition(
    Ref ref, String recipeId) async {
  final recipeRepo = ref.read(recipeRepositoryProvider);
  final ingRepo = ref.read(ingredientRepositoryProvider);

  final recipe = await recipeRepo.getRecipeWithIngredients(recipeId);
  if (recipe == null) return RecipeNutrition.zero;

  var total = RecipeNutrition.zero;
  for (final ri in recipe.ingredients) {
    if (ri.ingredientId != null) {
      final ing = await ingRepo.getIngredientById(ri.ingredientId!);
      if (ing != null) {
        total = total +
            RecipeNutrition.fromIngredient(ing, ri.grams ?? 0.0);
      }
    } else if (ri.subRecipeId != null) {
      final sub = await _computeRecipeNutrition(ref, ri.subRecipeId!);
      total = total + (sub * (ri.portions ?? 1.0));
    }
  }
  return total;
}

/// Parse a flat steps list into sections.
/// A step that starts with '#' is treated as a section header.
/// A step that starts with 'TIMER:' is a timer.
List<InstructionSection> _parseSteps(List<String> steps) {
  if (steps.isEmpty) return [];

  final sections = <InstructionSection>[];
  String currentTitle = 'Instructions';
  final currentSteps = <_StepData>[];

  for (final step in steps) {
    if (step.startsWith('#')) {
      // New section
      if (currentSteps.isNotEmpty) {
        sections
            .add(InstructionSection(title: currentTitle, steps: List.from(currentSteps)));
        currentSteps.clear();
      }
      currentTitle = step.substring(1).trim();
    } else if (step.startsWith('TIMER:')) {
      final parts = step.split(':');
      final secs = int.tryParse(parts.elementAtOrNull(1) ?? '') ?? 0;
      final label = parts.elementAtOrNull(2) ?? '';
      currentSteps
          .add(_StepData(text: label, isTimer: true, timerSeconds: secs));
    } else if (step.isNotEmpty) {
      currentSteps.add(_StepData(text: step));
    }
  }

  if (currentSteps.isNotEmpty) {
    sections.add(
        InstructionSection(title: currentTitle, steps: List.from(currentSteps)));
  }

  return sections;
}

// ── Screen ────────────────────────────────────────────────────────────────────

class RecipeDetailScreen extends ConsumerWidget {
  final String recipeId;

  const RecipeDetailScreen({super.key, required this.recipeId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncState = ref.watch(recipeDetailProvider(recipeId));

    return asyncState.when(
      loading: () => Scaffold(
        appBar: AppBar(),
        body: const Center(child: CircularProgressIndicator()),
      ),
      error: (err, _) => Scaffold(
        appBar: AppBar(),
        body: Center(child: Text('Error: $err')),
      ),
      data: (state) {
        if (state == null) {
          return Scaffold(
            appBar: AppBar(title: const Text('Recipe')),
            body: const Center(child: Text('Recipe not found.')),
          );
        }
        return _RecipeDetail(
          state: state,
          recipeId: recipeId,
        );
      },
    );
  }
}

// ── Detail content ────────────────────────────────────────────────────────────

class _RecipeDetail extends ConsumerStatefulWidget {
  final RecipeDetailState state;
  final String recipeId;

  const _RecipeDetail({required this.state, required this.recipeId});

  @override
  ConsumerState<_RecipeDetail> createState() => _RecipeDetailState();
}

class _RecipeDetailState extends ConsumerState<_RecipeDetail> {
  final Set<int> _checkedSteps = {};

  @override
  Widget build(BuildContext context) {
    final state = widget.state;
    final recipe = state.recipe;

    return Scaffold(
      appBar: AppBar(
        title: Text(recipe.name),
        leading: const BackButton(),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit_outlined),
            onPressed: () =>
                context.push('/recipes/edit?recipeId=${widget.recipeId}'),
          ),
          IconButton(
            icon: const Icon(Icons.delete_outline),
            onPressed: () => _confirmDelete(context, recipe),
          ),
        ],
      ),
      body: ListView(
        children: [
          // Hero header
          _HeroHeader(recipe: recipe),

          // Macro summary
          _MacroSummaryCard(nutrition: state.nutrition),

          // Ingredients section
          if (recipe.ingredients.isNotEmpty)
            _IngredientsSection(
              ingredients: recipe.ingredients,
              ingredientNames: state.ingredientNames,
            ),

          // Instructions section
          for (final section in state.instructionSections)
            _InstructionSectionCard(
              section: section,
              checkedSteps: _checkedSteps,
              onToggleStep: (index) {
                setState(() {
                  if (_checkedSteps.contains(index)) {
                    _checkedSteps.remove(index);
                  } else {
                    _checkedSteps.add(index);
                  }
                });
              },
            ),

          const SizedBox(height: 32),
        ],
      ),
    );
  }

  void _confirmDelete(BuildContext context, Recipe recipe) {
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Delete recipe?'),
        content: Text(
            'Delete "${recipe.name}"? This cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () async {
              Navigator.of(ctx).pop();
              await ref
                  .read(recipeRepositoryProvider)
                  .deleteRecipe(recipe);
              if (context.mounted) context.pop();
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
}

// ── Hero header ───────────────────────────────────────────────────────────────

class _HeroHeader extends StatelessWidget {
  final Recipe recipe;

  const _HeroHeader({required this.recipe});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Container(
      color: colorScheme.primaryContainer,
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            recipe.name,
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  color: colorScheme.onPrimaryContainer,
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 4,
            children: [
              _Badge(
                label: recipe.type == RecipeType.meal ? 'Meal' : 'Component',
                color: colorScheme.primary,
                onColor: colorScheme.onPrimary,
              ),
              ...recipe.mealCategories.map((cat) => _Badge(
                    label: _mealCatLabel(cat),
                    color: colorScheme.secondary,
                    onColor: colorScheme.onSecondary,
                  )),
              if (recipe.componentCategory != null)
                _Badge(
                  label: recipe.componentCategory!.name,
                  color: colorScheme.tertiary,
                  onColor: colorScheme.onTertiary,
                ),
            ],
          ),
        ],
      ),
    );
  }

  String _mealCatLabel(MealCategory cat) => switch (cat) {
        MealCategory.breakfast => 'Breakfast',
        MealCategory.lunch => 'Lunch',
        MealCategory.dinner => 'Dinner',
        MealCategory.snack => 'Snack',
      };
}

class _Badge extends StatelessWidget {
  final String label;
  final Color color;
  final Color onColor;

  const _Badge({
    required this.label,
    required this.color,
    required this.onColor,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding:
          const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: onColor,
          fontSize: 12,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}

// ── Macro summary ─────────────────────────────────────────────────────────────

class _MacroSummaryCard extends StatelessWidget {
  final RecipeNutrition nutrition;

  const _MacroSummaryCard({required this.nutrition});

  @override
  Widget build(BuildContext context) {
    final total =
        (nutrition.protein * 4) + (nutrition.fat * 9) + (nutrition.carbs * 4);
    final proteinFraction = total > 0 ? (nutrition.protein * 4) / total : 0.0;
    final fatFraction = total > 0 ? (nutrition.fat * 9) / total : 0.0;
    final carbsFraction = total > 0 ? (nutrition.carbs * 4) / total : 0.0;

    return Card(
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            // Donut chart
            SizedBox(
              width: 80,
              height: 80,
              child: CustomPaint(
                painter: _DonutChartPainter(
                  proteinFraction: proteinFraction,
                  fatFraction: fatFraction,
                  carbsFraction: carbsFraction,
                ),
                child: Center(
                  child: Text(
                    '${nutrition.kcal.round()}',
                    style: Theme.of(context).textTheme.labelSmall,
                  ),
                ),
              ),
            ),
            const SizedBox(width: 20),
            // Legend
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '${nutrition.kcal.round()} kcal',
                    style: Theme.of(context)
                        .textTheme
                        .titleMedium
                        ?.copyWith(fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 8),
                  _MacroLegendRow(
                    label: 'Protein',
                    value: '${nutrition.protein.round()}g',
                    color: const Color(0xFF534AB7),
                  ),
                  const SizedBox(height: 4),
                  _MacroLegendRow(
                    label: 'Fat',
                    value: '${nutrition.fat.round()}g',
                    color: const Color(0xFFBA7517),
                  ),
                  const SizedBox(height: 4),
                  _MacroLegendRow(
                    label: 'Carbs',
                    value: '${nutrition.carbs.round()}g',
                    color: const Color(0xFF1D9E75),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _MacroLegendRow extends StatelessWidget {
  final String label;
  final String value;
  final Color color;

  const _MacroLegendRow({
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 10,
          height: 10,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 6),
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall,
        ),
        const Spacer(),
        Text(
          value,
          style: Theme.of(context)
              .textTheme
              .bodySmall
              ?.copyWith(fontWeight: FontWeight.w600),
        ),
      ],
    );
  }
}

class _DonutChartPainter extends CustomPainter {
  final double proteinFraction;
  final double fatFraction;
  final double carbsFraction;

  const _DonutChartPainter({
    required this.proteinFraction,
    required this.fatFraction,
    required this.carbsFraction,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = math.min(size.width, size.height) / 2;
    const strokeWidth = 14.0;

    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.butt;

    final rect = Rect.fromCircle(center: center, radius: radius - strokeWidth / 2);

    double startAngle = -math.pi / 2;

    void drawArc(double fraction, Color color) {
      if (fraction <= 0) return;
      paint.color = color;
      canvas.drawArc(
        rect,
        startAngle,
        fraction * 2 * math.pi,
        false,
        paint,
      );
      startAngle += fraction * 2 * math.pi;
    }

    // Background ring
    paint.color = Colors.grey.withOpacity(0.15);
    canvas.drawArc(rect, 0, 2 * math.pi, false, paint);

    drawArc(proteinFraction, const Color(0xFF534AB7));
    drawArc(fatFraction, const Color(0xFFBA7517));
    drawArc(carbsFraction, const Color(0xFF1D9E75));
  }

  @override
  bool shouldRepaint(_DonutChartPainter oldDelegate) =>
      oldDelegate.proteinFraction != proteinFraction ||
      oldDelegate.fatFraction != fatFraction ||
      oldDelegate.carbsFraction != carbsFraction;
}

// ── Ingredients section ───────────────────────────────────────────────────────

class _IngredientsSection extends StatelessWidget {
  final List<RecipeIngredient> ingredients;
  final Map<String, String> ingredientNames;

  const _IngredientsSection({
    required this.ingredients,
    required this.ingredientNames,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding:
              const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: Text(
            'Ingredients',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
          ),
        ),
        ...ingredients.map((ri) => _IngredientRow(
              ri: ri,
              name: ri.ingredientId != null
                  ? ingredientNames[ri.ingredientId] ?? ri.ingredientId!
                  : ri.subRecipeId != null
                      ? ingredientNames[ri.subRecipeId] ?? ri.subRecipeId!
                      : '?',
              isIngredient: ri.ingredientId != null,
            )),
      ],
    );
  }
}

class _IngredientRow extends StatelessWidget {
  final RecipeIngredient ri;
  final String name;
  final bool isIngredient;

  const _IngredientRow({
    required this.ri,
    required this.name,
    required this.isIngredient,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final amount = ri.grams != null
        ? '${ri.grams!.round()}g'
        : ri.portions != null
            ? '${ri.portions!.toStringAsFixed(1)} portions'
            : '';

    return ListTile(
      dense: true,
      leading: Icon(
        isIngredient ? Icons.eco_outlined : Icons.menu_book_outlined,
        size: 20,
        color: colorScheme.outline,
      ),
      title: Text(name),
      trailing: Text(
        amount,
        style: Theme.of(context)
            .textTheme
            .bodySmall
            ?.copyWith(color: colorScheme.outline),
      ),
      onTap: ri.ingredientId != null
          ? () => context.push('/ingredients/${ri.ingredientId}')
          : ri.subRecipeId != null
              ? () => context.push('/recipes/${ri.subRecipeId}')
              : null,
    );
  }
}

// ── Instruction section ───────────────────────────────────────────────────────

class _InstructionSectionCard extends StatelessWidget {
  final InstructionSection section;
  final Set<int> checkedSteps;
  final void Function(int index) onToggleStep;

  const _InstructionSectionCard({
    required this.section,
    required this.checkedSteps,
    required this.onToggleStep,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding:
              const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: Text(
            section.title,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
          ),
        ),
        ...section.steps.asMap().entries.map((entry) {
          final index = entry.key;
          final step = entry.value;
          final globalKey = section.title.hashCode ^ index;
          return step.isTimer
              ? _TimerStepRow(
                  step: step,
                  isChecked: checkedSteps.contains(globalKey),
                  onToggle: () => onToggleStep(globalKey),
                )
              : _TextStepRow(
                  step: step,
                  stepNumber: index + 1,
                  isChecked: checkedSteps.contains(globalKey),
                  onToggle: () => onToggleStep(globalKey),
                );
        }),
      ],
    );
  }
}

class _TextStepRow extends StatelessWidget {
  final _StepData step;
  final int stepNumber;
  final bool isChecked;
  final VoidCallback onToggle;

  const _TextStepRow({
    required this.step,
    required this.stepNumber,
    required this.isChecked,
    required this.onToggle,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return ListTile(
      leading: GestureDetector(
        onTap: onToggle,
        child: CircleAvatar(
          radius: 14,
          backgroundColor: isChecked
              ? colorScheme.primary
              : colorScheme.surfaceContainerHighest,
          child: isChecked
              ? Icon(Icons.check,
                  size: 16, color: colorScheme.onPrimary)
              : Text(
                  '$stepNumber',
                  style: TextStyle(
                    fontSize: 12,
                    color: colorScheme.onSurfaceVariant,
                  ),
                ),
        ),
      ),
      title: Text(
        step.text,
        style: isChecked
            ? TextStyle(
                decoration: TextDecoration.lineThrough,
                color: colorScheme.outline,
              )
            : null,
      ),
      onTap: onToggle,
    );
  }
}

class _TimerStepRow extends StatefulWidget {
  final _StepData step;
  final bool isChecked;
  final VoidCallback onToggle;

  const _TimerStepRow({
    required this.step,
    required this.isChecked,
    required this.onToggle,
  });

  @override
  State<_TimerStepRow> createState() => _TimerStepRowState();
}

class _TimerStepRowState extends State<_TimerStepRow> {
  Timer? _timer;
  late int _remaining;
  bool _running = false;

  @override
  void initState() {
    super.initState();
    _remaining = widget.step.timerSeconds;
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  void _toggleTimer() {
    if (_running) {
      _timer?.cancel();
      setState(() => _running = false);
    } else {
      if (_remaining <= 0) {
        setState(() => _remaining = widget.step.timerSeconds);
      }
      _timer = Timer.periodic(const Duration(seconds: 1), (_) {
        if (_remaining <= 1) {
          _timer?.cancel();
          setState(() {
            _remaining = 0;
            _running = false;
          });
        } else {
          setState(() => _remaining--);
        }
      });
      setState(() => _running = true);
    }
  }

  String _formatTime(int secs) {
    final m = secs ~/ 60;
    final s = secs % 60;
    return '${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final isDone = _remaining == 0 && widget.step.timerSeconds > 0;

    return ListTile(
      leading: Icon(
        Icons.timer_outlined,
        color: _running ? colorScheme.primary : colorScheme.outline,
      ),
      title: Text(
        widget.step.text.isNotEmpty
            ? widget.step.text
            : _formatTime(widget.step.timerSeconds),
      ),
      subtitle: Text(
        _formatTime(_remaining),
        style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: _running ? colorScheme.primary : colorScheme.outline,
              fontWeight:
                  _running ? FontWeight.w600 : FontWeight.normal,
            ),
      ),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (isDone)
            IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: () =>
                  setState(() => _remaining = widget.step.timerSeconds),
            ),
          IconButton(
            icon: Icon(_running ? Icons.pause : Icons.play_arrow),
            onPressed: _toggleTimer,
          ),
          Checkbox(
            value: widget.isChecked,
            onChanged: (_) => widget.onToggle(),
          ),
        ],
      ),
    );
  }
}
