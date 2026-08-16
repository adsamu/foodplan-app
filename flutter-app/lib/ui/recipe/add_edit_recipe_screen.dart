import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/ingredient.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:go_router/go_router.dart';

import 'add_edit_recipe_provider.dart';

// ── Screen ────────────────────────────────────────────────────────────────────

class AddEditRecipeScreen extends ConsumerStatefulWidget {
  final String? recipeId;

  const AddEditRecipeScreen({super.key, this.recipeId});

  @override
  ConsumerState<AddEditRecipeScreen> createState() =>
      _AddEditRecipeScreenState();
}

class _AddEditRecipeScreenState extends ConsumerState<AddEditRecipeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref
          .read(addEditRecipeProvider.notifier)
          .loadRecipe(widget.recipeId);
    });
  }

  @override
  Widget build(BuildContext context) {
    final asyncState = ref.watch(addEditRecipeProvider);

    return asyncState.when(
      loading: () => Scaffold(
        appBar: AppBar(title: const Text('Loading…')),
        body: const Center(child: CircularProgressIndicator()),
      ),
      error: (err, _) => Scaffold(
        appBar: AppBar(),
        body: Center(child: Text('Error: $err')),
      ),
      data: (recipeState) =>
          _RecipeForm(recipeState: recipeState, recipeId: widget.recipeId),
    );
  }
}

// ── Form ──────────────────────────────────────────────────────────────────────

class _RecipeForm extends ConsumerStatefulWidget {
  final AddEditRecipeState recipeState;
  final String? recipeId;

  const _RecipeForm({required this.recipeState, this.recipeId});

  @override
  ConsumerState<_RecipeForm> createState() => _RecipeFormState();
}

class _RecipeFormState extends ConsumerState<_RecipeForm> {
  late TextEditingController _nameCtrl;

  @override
  void initState() {
    super.initState();
    _nameCtrl = TextEditingController(text: widget.recipeState.name);
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    await ref.read(addEditRecipeProvider.notifier).saveRecipe();
    if (mounted) context.pop();
  }

  @override
  Widget build(BuildContext context) {
    final s = widget.recipeState;
    final notifier = ref.read(addEditRecipeProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: Text(s.isNew ? 'New recipe' : 'Edit recipe'),
        leading: BackButton(onPressed: () => context.pop()),
        actions: [
          IconButton(
            icon: s.isSaving
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.check),
            onPressed: s.isSaving ? null : _save,
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Name field
          TextField(
            controller: _nameCtrl,
            decoration: const InputDecoration(
              labelText: 'Recipe name',
              border: OutlineInputBorder(),
            ),
            onChanged: notifier.onNameChange,
          ),
          const SizedBox(height: 16),

          // Type toggle
          Text('Type', style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: RecipeType.values.map((type) {
              final label = switch (type) {
                RecipeType.meal => 'Meal',
                RecipeType.component => 'Component',
              };
              return FilterChip(
                label: Text(label),
                selected: s.type == type,
                onSelected: (_) => notifier.onTypeChange(type),
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Category chips
          Text('Category', style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 8),
          if (s.type == RecipeType.meal)
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children: MealCategory.values.map((cat) {
                final label = switch (cat) {
                  MealCategory.breakfast => 'Breakfast',
                  MealCategory.lunch => 'Lunch',
                  MealCategory.dinner => 'Dinner',
                  MealCategory.snack => 'Snack',
                };
                return FilterChip(
                  label: Text(label),
                  selected: s.mealCategories.contains(cat),
                  onSelected: (_) =>
                      notifier.onMealCategoryToggle(cat),
                );
              }).toList(),
            )
          else
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children: ComponentCategory.values.map((cat) {
                final label = switch (cat) {
                  ComponentCategory.sauce => 'Sauce',
                  ComponentCategory.dressing => 'Dressing',
                  ComponentCategory.salsa => 'Salsa',
                  ComponentCategory.salad => 'Salad',
                  ComponentCategory.side => 'Side',
                  ComponentCategory.other => 'Other',
                };
                return FilterChip(
                  label: Text(label),
                  selected: s.componentCategory == cat,
                  onSelected: (_) =>
                      notifier.onComponentCategorySelect(
                          s.componentCategory == cat ? null : cat),
                );
              }).toList(),
            ),
          const SizedBox(height: 16),

          // Sticky macro banner
          _MacroBanner(nutrition: s.nutrition),
          const SizedBox(height: 16),

          // Ingredients section
          Text('Ingredients',
              style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 8),
          ...s.ingredients.asMap().entries.map((entry) {
            final i = entry.key;
            final ing = entry.value;
            return _IngredientRow(
              ingredient: ing,
              onAmountChanged: (amount) =>
                  notifier.updateIngredientAmount(i, amount),
              onRemove: () => notifier.removeIngredient(i),
            );
          }),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            icon: const Icon(Icons.add),
            label: const Text('Add ingredient or recipe'),
            onPressed: () => _showIngredientSearch(context, notifier),
          ),
          const SizedBox(height: 24),

          // Instructions section
          Text('Instructions',
              style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 8),
          ReorderableListView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: s.steps.length,
            buildDefaultDragHandles: false,
            onReorder: notifier.reorderSteps,
            itemBuilder: (ctx, i) {
              final step = s.steps[i];
              return _StepRow(
                key: ValueKey(step.id),
                step: step,
                index: i,
                onTextChanged: (text) => notifier.updateStep(i, text),
                onTimerChanged: (label, secs) =>
                    notifier.updateTimer(i, label, secs),
                onRemove: () => notifier.removeStep(i),
              );
            },
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  icon: const Icon(Icons.add),
                  label: const Text('Add step'),
                  onPressed: notifier.addStep,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: OutlinedButton.icon(
                  icon: const Icon(Icons.timer_outlined),
                  label: const Text('Add timer'),
                  onPressed: () =>
                      notifier.addTimer('Timer', 60),
                ),
              ),
            ],
          ),
          const SizedBox(height: 32),
        ],
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: FilledButton(
            onPressed: s.isSaving ? null : _save,
            child: const Text('Save recipe'),
          ),
        ),
      ),
    );
  }

  void _showIngredientSearch(
      BuildContext context, AddEditRecipeNotifier notifier) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      builder: (ctx) => _IngredientSearchBottomSheet(
        onIngredientSelected: (id, grams) {
          notifier.addIngredient(id, grams);
        },
        onSubRecipeSelected: (id, portions) {
          notifier.addSubRecipe(id, portions);
        },
      ),
    );
  }
}

// ── Macro banner ──────────────────────────────────────────────────────────────

class _MacroBanner extends StatelessWidget {
  final RecipeNutrition nutrition;
  const _MacroBanner({required this.nutrition});

  @override
  Widget build(BuildContext context) {
    final total = nutrition.protein + nutrition.fat + nutrition.carbs;
    final pFrac = total > 0 ? nutrition.protein / total : 0.0;
    final fFrac = total > 0 ? nutrition.fat / total : 0.0;
    final cFrac = total > 0 ? nutrition.carbs / total : 0.0;

    final colorScheme = Theme.of(context).colorScheme;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            // Donut chart
            SizedBox(
              width: 56,
              height: 56,
              child: CustomPaint(
                painter: _DonutPainter(
                  fractions: [pFrac, fFrac, cFrac],
                  colors: [
                    Colors.blue,
                    Colors.orange,
                    Colors.green,
                  ],
                ),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  _MacroChip(
                      label: 'P',
                      value: nutrition.protein,
                      color: Colors.blue),
                  _MacroChip(
                      label: 'F',
                      value: nutrition.fat,
                      color: Colors.orange),
                  _MacroChip(
                      label: 'C',
                      value: nutrition.carbs,
                      color: Colors.green),
                  _MacroChip(
                      label: 'kcal',
                      value: nutrition.kcal,
                      color: colorScheme.primary),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _MacroChip extends StatelessWidget {
  final String label;
  final double value;
  final Color color;

  const _MacroChip(
      {required this.label, required this.value, required this.color});

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          label,
          style: TextStyle(
              color: color, fontWeight: FontWeight.w600, fontSize: 12),
        ),
        Text(
          value.toStringAsFixed(1),
          style: Theme.of(context).textTheme.bodySmall,
        ),
      ],
    );
  }
}

class _DonutPainter extends CustomPainter {
  final List<double> fractions;
  final List<Color> colors;

  const _DonutPainter({required this.fractions, required this.colors});

  @override
  void paint(Canvas canvas, Size size) {
    final rect = Offset.zero & size;
    double startAngle = -math.pi / 2;
    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 8;

    for (int i = 0; i < fractions.length; i++) {
      paint.color = colors[i % colors.length];
      final sweep = 2 * math.pi * fractions[i];
      canvas.drawArc(
          rect.deflate(4), startAngle, sweep.clamp(0.0, 2 * math.pi), false,
          paint);
      startAngle += sweep;
    }
  }

  @override
  bool shouldRepaint(_DonutPainter oldDelegate) => true;
}

// ── Ingredient row ────────────────────────────────────────────────────────────

class _IngredientRow extends StatefulWidget {
  final RecipeIngredientUi ingredient;
  final void Function(double) onAmountChanged;
  final VoidCallback onRemove;

  const _IngredientRow({
    required this.ingredient,
    required this.onAmountChanged,
    required this.onRemove,
  });

  @override
  State<_IngredientRow> createState() => _IngredientRowState();
}

class _IngredientRowState extends State<_IngredientRow> {
  late TextEditingController _amountCtrl;

  @override
  void initState() {
    super.initState();
    _amountCtrl = TextEditingController(
        text: widget.ingredient.amount.toStringAsFixed(0));
  }

  @override
  void dispose() {
    _amountCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ing = widget.ingredient;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(ing.name),
                if (ing.ingredientId != null)
                  Wrap(
                    spacing: 4,
                    children: [
                      _MacroPill(
                          'P ${ing.protein.toStringAsFixed(1)}g',
                          Colors.blue),
                      _MacroPill(
                          'F ${ing.fat.toStringAsFixed(1)}g',
                          Colors.orange),
                      _MacroPill(
                          'C ${ing.carbs.toStringAsFixed(1)}g',
                          Colors.green),
                    ],
                  ),
              ],
            ),
          ),
          SizedBox(
            width: 72,
            child: TextField(
              controller: _amountCtrl,
              decoration: InputDecoration(
                isDense: true,
                border: const OutlineInputBorder(),
                suffixText: ing.unit,
                contentPadding: const EdgeInsets.symmetric(
                    horizontal: 8, vertical: 8),
              ),
              keyboardType:
                  const TextInputType.numberWithOptions(decimal: true),
              onSubmitted: (v) {
                final d = double.tryParse(v);
                if (d != null) widget.onAmountChanged(d);
              },
            ),
          ),
          IconButton(
            icon: const Icon(Icons.close, size: 20),
            onPressed: widget.onRemove,
          ),
        ],
      ),
    );
  }
}

class _MacroPill extends StatelessWidget {
  final String label;
  final Color color;

  const _MacroPill(this.label, this.color);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        label,
        style: TextStyle(color: color, fontSize: 11),
      ),
    );
  }
}

// ── Step row ──────────────────────────────────────────────────────────────────

class _StepRow extends StatefulWidget {
  final StepUi step;
  final int index;
  final void Function(String) onTextChanged;
  final void Function(String, int) onTimerChanged;
  final VoidCallback onRemove;

  const _StepRow({
    required super.key,
    required this.step,
    required this.index,
    required this.onTextChanged,
    required this.onTimerChanged,
    required this.onRemove,
  });

  @override
  State<_StepRow> createState() => _StepRowState();
}

class _StepRowState extends State<_StepRow> {
  late TextEditingController _textCtrl;

  @override
  void initState() {
    super.initState();
    _textCtrl = TextEditingController(
      text: widget.step is TextStep
          ? (widget.step as TextStep).text
          : '',
    );
  }

  @override
  void dispose() {
    _textCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Drag handle
          ReorderableDragStartListener(
            index: widget.index,
            child: const Padding(
              padding: EdgeInsets.only(top: 12, right: 8),
              child: Icon(Icons.drag_handle, size: 20),
            ),
          ),
          Text(
            '${widget.index + 1}.',
            style: const TextStyle(
                fontWeight: FontWeight.w600, fontSize: 16),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: switch (widget.step) {
              TextStep _ => TextField(
                  controller: _textCtrl,
                  decoration: const InputDecoration(
                    hintText: 'Step description',
                    border: OutlineInputBorder(),
                    isDense: true,
                  ),
                  maxLines: null,
                  onChanged: widget.onTextChanged,
                ),
              TimerStep t => Row(
                  children: [
                    const Icon(Icons.timer_outlined, size: 18),
                    const SizedBox(width: 4),
                    Text(
                        '${t.label} – ${_formatDuration(t.totalSeconds)}'),
                  ],
                ),
            },
          ),
          IconButton(
            icon: const Icon(Icons.close, size: 20),
            onPressed: widget.onRemove,
          ),
        ],
      ),
    );
  }

  String _formatDuration(int seconds) {
    final m = seconds ~/ 60;
    final s = seconds % 60;
    return s == 0 ? '${m}m' : '${m}m ${s}s';
  }
}

// ── Ingredient search bottom sheet ────────────────────────────────────────────

class _IngredientSearchBottomSheet extends ConsumerStatefulWidget {
  final void Function(String ingredientId, double grams) onIngredientSelected;
  final void Function(String recipeId, double portions) onSubRecipeSelected;

  const _IngredientSearchBottomSheet({
    required this.onIngredientSelected,
    required this.onSubRecipeSelected,
  });

  @override
  ConsumerState<_IngredientSearchBottomSheet> createState() =>
      _IngredientSearchBottomSheetState();
}

class _IngredientSearchBottomSheetState
    extends ConsumerState<_IngredientSearchBottomSheet> {
  final _searchCtrl = TextEditingController();
  List<_SearchResult> _results = [];
  bool _isLoading = false;

  @override
  void dispose() {
    _searchCtrl.dispose();
    super.dispose();
  }

  Future<void> _search(String query) async {
    if (query.trim().isEmpty) {
      setState(() => _results = []);
      return;
    }
    setState(() => _isLoading = true);
    try {
      final ingredients = await ref
          .read(ingredientRepositoryProvider)
          .searchIngredients(query)
          .first;
      setState(() {
        _results = ingredients
            .map((i) => _SearchResult.ingredient(i))
            .toList();
        _isLoading = false;
      });
    } catch (_) {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      expand: false,
      initialChildSize: 0.7,
      minChildSize: 0.4,
      maxChildSize: 0.95,
      builder: (ctx, scrollCtrl) => Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchCtrl,
              autofocus: true,
              decoration: const InputDecoration(
                prefixIcon: Icon(Icons.search),
                hintText: 'Search ingredients…',
                border: OutlineInputBorder(),
              ),
              onChanged: _search,
            ),
          ),
          if (_isLoading)
            const LinearProgressIndicator()
          else
            Expanded(
              child: ListView.builder(
                controller: scrollCtrl,
                itemCount: _results.length,
                itemBuilder: (ctx, i) {
                  final result = _results[i];
                  return ListTile(
                    leading: Text(
                        result.ingredient?.category.emoji ?? '🍳',
                        style: const TextStyle(fontSize: 20)),
                    title: Text(result.ingredient?.name ?? ''),
                    subtitle: result.ingredient != null
                        ? Text(
                            '${result.ingredient!.kcalPer100g.toStringAsFixed(0)} kcal/100g')
                        : null,
                    onTap: () => _showAmountDialog(context, result),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }

  void _showAmountDialog(
      BuildContext context, _SearchResult result) {
    final ctrl = TextEditingController(text: '100');
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(result.ingredient?.name ?? ''),
        content: TextField(
          controller: ctrl,
          autofocus: true,
          decoration: const InputDecoration(
            labelText: 'Amount',
            border: OutlineInputBorder(),
            suffixText: 'g',
          ),
          keyboardType:
              const TextInputType.numberWithOptions(decimal: true),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.of(ctx).pop(),
              child: const Text('Cancel')),
          FilledButton(
            onPressed: () {
              final grams = double.tryParse(ctrl.text) ?? 100.0;
              if (result.ingredient != null) {
                widget.onIngredientSelected(
                    result.ingredient!.id, grams);
              }
              Navigator.of(ctx).pop();
              Navigator.of(context).pop();
            },
            child: const Text('Add'),
          ),
        ],
      ),
    );
  }
}

class _SearchResult {
  final Ingredient? ingredient;

  const _SearchResult.ingredient(this.ingredient);
}
