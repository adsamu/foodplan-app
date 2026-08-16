import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/domain/model/ingredient.dart';
import 'package:go_router/go_router.dart';

import '../recipe/add_edit_recipe_provider.dart';
import 'add_edit_ingredient_provider.dart';

// ── Screen ────────────────────────────────────────────────────────────────────

class AddEditIngredientScreen extends ConsumerStatefulWidget {
  final String? ingredientId;

  const AddEditIngredientScreen({super.key, this.ingredientId});

  @override
  ConsumerState<AddEditIngredientScreen> createState() =>
      _AddEditIngredientScreenState();
}

class _AddEditIngredientScreenState
    extends ConsumerState<AddEditIngredientScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref
          .read(addEditIngredientProvider.notifier)
          .loadIngredient(widget.ingredientId);
    });
  }

  @override
  Widget build(BuildContext context) {
    final asyncState = ref.watch(addEditIngredientProvider);

    return asyncState.when(
      loading: () => Scaffold(
        appBar: AppBar(title: const Text('Loading…')),
        body: const Center(child: CircularProgressIndicator()),
      ),
      error: (err, _) => Scaffold(
        appBar: AppBar(),
        body: Center(child: Text('Error: $err')),
      ),
      data: (s) => _IngredientForm(
        ingredientState: s,
        ingredientId: widget.ingredientId,
      ),
    );
  }
}

// ── Form ──────────────────────────────────────────────────────────────────────

class _IngredientForm extends ConsumerStatefulWidget {
  final AddEditIngredientState ingredientState;
  final String? ingredientId;

  const _IngredientForm({
    required this.ingredientState,
    this.ingredientId,
  });

  @override
  ConsumerState<_IngredientForm> createState() => _IngredientFormState();
}

class _IngredientFormState extends ConsumerState<_IngredientForm> {
  late TextEditingController _nameCtrl;
  late TextEditingController _kcalCtrl;
  late TextEditingController _proteinCtrl;
  late TextEditingController _fatCtrl;
  late TextEditingController _carbsCtrl;

  @override
  void initState() {
    super.initState();
    final s = widget.ingredientState;
    _nameCtrl = TextEditingController(text: s.name);
    _kcalCtrl =
        TextEditingController(text: s.kcal.toStringAsFixed(1));
    _proteinCtrl =
        TextEditingController(text: s.protein.toStringAsFixed(1));
    _fatCtrl =
        TextEditingController(text: s.fat.toStringAsFixed(1));
    _carbsCtrl =
        TextEditingController(text: s.carbs.toStringAsFixed(1));
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _kcalCtrl.dispose();
    _proteinCtrl.dispose();
    _fatCtrl.dispose();
    _carbsCtrl.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    await ref.read(addEditIngredientProvider.notifier).saveIngredient();
    if (mounted) context.pop();
  }

  @override
  Widget build(BuildContext context) {
    final s = widget.ingredientState;
    final notifier = ref.read(addEditIngredientProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: Text(s.isNew ? 'New ingredient' : 'Edit ingredient'),
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
              labelText: 'Ingredient name',
              border: OutlineInputBorder(),
            ),
            onChanged: notifier.onNameChange,
          ),
          const SizedBox(height: 16),

          // Category grid
          Text('Category',
              style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 6,
            children: IngredientCategory.values.map((cat) {
              return FilterChip(
                avatar: Text(cat.emoji),
                label: Text(cat.displayName),
                selected: s.category == cat,
                onSelected: (_) => notifier.onCategoryChange(cat),
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Source chips
          Text('Source', style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: IngredientSource.values.map((src) {
              final label = switch (src) {
                IngredientSource.label => 'Label',
                IngredientSource.livsmedelsverket => 'Livsmedelsverket',
                IngredientSource.calculated => 'Calculated',
                IngredientSource.barcode => 'Barcode',
              };
              return FilterChip(
                label: Text(label),
                selected: s.source == src,
                onSelected: (_) => notifier.onSourceChange(src),
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Nutrition card (per 100g)
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Nutrition per 100g',
                      style: Theme.of(context).textTheme.titleSmall),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _kcalCtrl,
                          decoration: const InputDecoration(
                            labelText: 'Kcal',
                            border: OutlineInputBorder(),
                            isDense: true,
                          ),
                          keyboardType:
                              const TextInputType.numberWithOptions(
                                  decimal: true),
                          onChanged: (v) {
                            final d = double.tryParse(v);
                            if (d != null) notifier.onKcalChange(d);
                          },
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: TextField(
                          controller: _proteinCtrl,
                          decoration: const InputDecoration(
                            labelText: 'Protein (g)',
                            border: OutlineInputBorder(),
                            isDense: true,
                          ),
                          keyboardType:
                              const TextInputType.numberWithOptions(
                                  decimal: true),
                          onChanged: (v) {
                            final d = double.tryParse(v);
                            if (d != null) notifier.onProteinChange(d);
                          },
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _fatCtrl,
                          decoration: const InputDecoration(
                            labelText: 'Fat (g)',
                            border: OutlineInputBorder(),
                            isDense: true,
                          ),
                          keyboardType:
                              const TextInputType.numberWithOptions(
                                  decimal: true),
                          onChanged: (v) {
                            final d = double.tryParse(v);
                            if (d != null) notifier.onFatChange(d);
                          },
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: TextField(
                          controller: _carbsCtrl,
                          decoration: const InputDecoration(
                            labelText: 'Carbs (g)',
                            border: OutlineInputBorder(),
                            isDense: true,
                          ),
                          keyboardType:
                              const TextInputType.numberWithOptions(
                                  decimal: true),
                          onChanged: (v) {
                            final d = double.tryParse(v);
                            if (d != null) notifier.onCarbsChange(d);
                          },
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
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
                onRemove: () => notifier.removeStep(i),
              );
            },
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            icon: const Icon(Icons.add),
            label: const Text('Add step'),
            onPressed: notifier.addStep,
          ),
          const SizedBox(height: 32),
        ],
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: FilledButton(
            onPressed: s.isSaving ? null : _save,
            child: const Text('Save ingredient'),
          ),
        ),
      ),
    );
  }
}

// ── Step row ──────────────────────────────────────────────────────────────────

class _StepRow extends StatefulWidget {
  final StepUi step;
  final int index;
  final void Function(String) onTextChanged;
  final VoidCallback onRemove;

  const _StepRow({
    required super.key,
    required this.step,
    required this.index,
    required this.onTextChanged,
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
                    Text(_formatDuration(t.totalSeconds)),
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
