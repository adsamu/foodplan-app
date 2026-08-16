import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/domain/model/ingredient.dart';
import 'package:go_router/go_router.dart';

import 'ingredient_detail_provider.dart';

// ── Screen ────────────────────────────────────────────────────────────────────

class IngredientDetailScreen extends ConsumerStatefulWidget {
  final String ingredientId;

  /// If non-null, the ingredient is being viewed in the context of a recipe.
  /// Editing the amount will write back to the AddEditRecipeNotifier.
  final int? recipeIngredientIndex;
  final double? currentAmountGrams;

  const IngredientDetailScreen({
    super.key,
    required this.ingredientId,
    this.recipeIngredientIndex,
    this.currentAmountGrams,
  });

  @override
  ConsumerState<IngredientDetailScreen> createState() =>
      _IngredientDetailScreenState();
}

class _IngredientDetailScreenState
    extends ConsumerState<IngredientDetailScreen> {
  late TextEditingController _amountCtrl;

  @override
  void initState() {
    super.initState();
    _amountCtrl = TextEditingController(
      text: widget.currentAmountGrams?.toStringAsFixed(0) ?? '100',
    );
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref
          .read(ingredientDetailProvider.notifier)
          .loadIngredient(widget.ingredientId);
    });
  }

  @override
  void dispose() {
    _amountCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final asyncState = ref.watch(ingredientDetailProvider);

    return asyncState.when(
      loading: () => Scaffold(
        appBar: AppBar(),
        body: const Center(child: CircularProgressIndicator()),
      ),
      error: (err, _) => Scaffold(
        appBar: AppBar(),
        body: Center(child: Text('Error: $err')),
      ),
      data: (s) {
        final ingredient = s.ingredient;
        if (ingredient == null) {
          return Scaffold(
            appBar: AppBar(title: const Text('Ingredient')),
            body: const Center(child: Text('Not found')),
          );
        }
        return _IngredientDetail(
          ingredient: ingredient,
          isLoading: s.isLoading,
          recipeIngredientIndex: widget.recipeIngredientIndex,
          amountCtrl: _amountCtrl,
          onDelete: () async {
            await ref
                .read(ingredientDetailProvider.notifier)
                .deleteIngredient();
            if (context.mounted) context.pop();
          },
          onSaveAmount: () {
            final grams = double.tryParse(_amountCtrl.text);
            if (grams == null || widget.recipeIngredientIndex == null) return;
            ref
                .read(ingredientDetailProvider.notifier)
                .saveAmountToRecipe(widget.recipeIngredientIndex!, grams);
            context.pop();
          },
          onRemoveFromRecipe: () => context.pop(),
        );
      },
    );
  }
}

// ── Detail content ────────────────────────────────────────────────────────────

class _IngredientDetail extends StatelessWidget {
  final Ingredient ingredient;
  final bool isLoading;
  final int? recipeIngredientIndex;
  final TextEditingController amountCtrl;
  final VoidCallback onDelete;
  final VoidCallback onSaveAmount;
  final VoidCallback onRemoveFromRecipe;

  const _IngredientDetail({
    required this.ingredient,
    required this.isLoading,
    required this.recipeIngredientIndex,
    required this.amountCtrl,
    required this.onDelete,
    required this.onSaveAmount,
    required this.onRemoveFromRecipe,
  });

  @override
  Widget build(BuildContext context) {
    final isRecipeContext = recipeIngredientIndex != null;

    return Scaffold(
      appBar: AppBar(
        title: Text(ingredient.name),
        leading: const BackButton(),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit_outlined),
            onPressed: () => context.push(
                '/ingredients/edit?ingredientId=${ingredient.id}'),
          ),
          IconButton(
            icon: const Icon(Icons.delete_outline),
            onPressed: () => _confirmDelete(context),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Hero card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    ingredient.name,
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Text(
                        ingredient.category.emoji,
                        style: const TextStyle(fontSize: 20),
                      ),
                      const SizedBox(width: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: Theme.of(context)
                              .colorScheme
                              .secondaryContainer,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          ingredient.category.displayName,
                          style: TextStyle(
                            color: Theme.of(context)
                                .colorScheme
                                .onSecondaryContainer,
                            fontSize: 13,
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // Nutrition card per 100g
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Nutrition per 100g',
                    style: Theme.of(context).textTheme.titleSmall,
                  ),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _NutritionCell(
                        label: 'Kcal',
                        value:
                            ingredient.kcalPer100g.toStringAsFixed(0),
                      ),
                      _NutritionCell(
                        label: 'Protein',
                        value:
                            '${ingredient.proteinPer100g.toStringAsFixed(1)}g',
                      ),
                      _NutritionCell(
                        label: 'Fat',
                        value:
                            '${ingredient.fatPer100g.toStringAsFixed(1)}g',
                      ),
                      _NutritionCell(
                        label: 'Carbs',
                        value:
                            '${ingredient.carbsPer100g.toStringAsFixed(1)}g',
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),

          // Recipe context panel
          if (isRecipeContext) ...[
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Amount in recipe',
                        style: Theme.of(context).textTheme.titleSmall),
                    const SizedBox(height: 12),
                    TextField(
                      controller: amountCtrl,
                      decoration: const InputDecoration(
                        border: OutlineInputBorder(),
                        suffixText: 'g',
                        labelText: 'Grams',
                      ),
                      keyboardType:
                          const TextInputType.numberWithOptions(
                              decimal: true),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: OutlinedButton(
                            onPressed: onRemoveFromRecipe,
                            child:
                                const Text('Remove from recipe'),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: FilledButton(
                            onPressed: onSaveAmount,
                            child: const Text('Save amount'),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }

  void _confirmDelete(BuildContext context) {
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Delete ingredient?'),
        content: Text('Delete "${ingredient.name}"? This cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.of(ctx).pop();
              onDelete();
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
}

// ── Nutrition cell ────────────────────────────────────────────────────────────

class _NutritionCell extends StatelessWidget {
  final String label;
  final String value;

  const _NutritionCell({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          value,
          style: Theme.of(context)
              .textTheme
              .titleMedium
              ?.copyWith(fontWeight: FontWeight.w600),
        ),
        const SizedBox(height: 2),
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Theme.of(context).colorScheme.outline,
              ),
        ),
      ],
    );
  }
}
