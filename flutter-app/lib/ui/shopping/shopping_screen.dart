import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/domain/model/shopping_list.dart';
import 'package:intl/intl.dart';

import 'shopping_provider.dart';

// ── Date formatter ────────────────────────────────────────────────────────────

final _dateFmt = DateFormat('MMM d');

// ── Screen ────────────────────────────────────────────────────────────────────

class ShoppingScreen extends ConsumerWidget {
  const ShoppingScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncState = ref.watch(shoppingProvider);

    return asyncState.when(
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (err, _) => Scaffold(
        body: Center(child: Text('Error: $err')),
      ),
      data: (state) => _ShoppingContent(state: state),
    );
  }
}

// ── Content ───────────────────────────────────────────────────────────────────

class _ShoppingContent extends ConsumerStatefulWidget {
  final ShoppingState state;
  const _ShoppingContent({required this.state});

  @override
  ConsumerState<_ShoppingContent> createState() => _ShoppingContentState();
}

class _ShoppingContentState extends ConsumerState<_ShoppingContent> {
  bool _periodExpanded = false;

  @override
  Widget build(BuildContext context) {
    final state = widget.state;
    final notifier = ref.read(shoppingProvider.notifier);

    // Show adjustments dialog if a pending period change is queued
    if (state.pendingStartDate != null && state.pendingEndDate != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _showAdjustmentsDialog(
          context,
          notifier,
          state.pendingStartDate!,
          state.pendingEndDate!,
        );
      });
    }

    final list = state.shoppingList;

    // Separate checked and unchecked items per category
    final uncheckedCategories = <_CategorySection>[];
    final checkedItems = <_FlatItem>[];

    if (list != null) {
      for (final cat in list.categories) {
        final unchecked = <ShoppingItem>[];
        for (final item in cat.items) {
          if (state.checkedItems.contains(item.ingredientId)) {
            checkedItems.add(_FlatItem(category: cat, item: item));
          } else {
            unchecked.add(item);
          }
        }
        if (unchecked.isNotEmpty) {
          uncheckedCategories.add(_CategorySection(category: cat, items: unchecked));
        }
      }
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Shopping'),
        actions: [
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: () => _shareList(state),
          ),
        ],
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : CustomScrollView(
              slivers: [
                // Period card
                SliverToBoxAdapter(
                  child: _PeriodCard(
                    state: state,
                    expanded: _periodExpanded,
                    onToggle: () =>
                        setState(() => _periodExpanded = !_periodExpanded),
                    onRangeChanged: (start, end) {
                      notifier.requestPeriodChange(start, end);
                    },
                  ),
                ),

                // Checked counter
                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 16, vertical: 8),
                    child: Text(
                      '${state.checkedCount} of ${state.totalItems} checked',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: Theme.of(context).colorScheme.outline,
                          ),
                    ),
                  ),
                ),

                // Unchecked items grouped by category
                for (final section in uncheckedCategories) ...[
                  SliverToBoxAdapter(
                    child: _CategoryHeader(
                      emoji: section.category.emoji,
                      name: section.category.name,
                    ),
                  ),
                  SliverList(
                    delegate: SliverChildBuilderDelegate(
                      (ctx, i) => _ShoppingItemRow(
                        item: section.items[i],
                        isChecked: false,
                        isExpanded: state.expandedItemId ==
                            section.items[i].ingredientId,
                        adjustment:
                            state.adjustments[section.items[i].ingredientId],
                        onToggle: () => ref
                            .read(shoppingProvider.notifier)
                            .toggleItem(section.items[i].ingredientId),
                        onExpand: () => ref
                            .read(shoppingProvider.notifier)
                            .setExpandedItem(section.items[i].ingredientId),
                        onCommitExpression: (expr) => ref
                            .read(shoppingProvider.notifier)
                            .commitExpression(
                                section.items[i].ingredientId, expr),
                      ),
                      childCount: section.items.length,
                    ),
                  ),
                ],

                // Checked items section
                if (checkedItems.isNotEmpty) ...[
                  const SliverToBoxAdapter(child: Divider()),
                  SliverToBoxAdapter(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 4),
                      child: Text(
                        'Checked',
                        style:
                            Theme.of(context).textTheme.labelMedium?.copyWith(
                                  color: Theme.of(context).colorScheme.outline,
                                ),
                      ),
                    ),
                  ),
                  SliverList(
                    delegate: SliverChildBuilderDelegate(
                      (ctx, i) => _ShoppingItemRow(
                        item: checkedItems[i].item,
                        isChecked: true,
                        isExpanded: state.expandedItemId ==
                            checkedItems[i].item.ingredientId,
                        adjustment: state.adjustments[
                            checkedItems[i].item.ingredientId],
                        onToggle: () => ref
                            .read(shoppingProvider.notifier)
                            .toggleItem(checkedItems[i].item.ingredientId),
                        onExpand: () => ref
                            .read(shoppingProvider.notifier)
                            .setExpandedItem(checkedItems[i].item.ingredientId),
                        onCommitExpression: (expr) => ref
                            .read(shoppingProvider.notifier)
                            .commitExpression(
                                checkedItems[i].item.ingredientId, expr),
                      ),
                      childCount: checkedItems.length,
                    ),
                  ),
                ],

                const SliverToBoxAdapter(child: SizedBox(height: 24)),
              ],
            ),
    );
  }

  void _shareList(ShoppingState state) {
    final list = state.shoppingList;
    if (list == null) return;
    final buf = StringBuffer();
    buf.writeln(
        'Shopping list ${_dateFmt.format(state.startDate)} – ${_dateFmt.format(state.endDate)}');
    buf.writeln();
    for (final cat in list.categories) {
      buf.writeln('${cat.emoji} ${cat.name}');
      for (final item in cat.items) {
        final adj = state.adjustments[item.ingredientId];
        final grams = adj ?? item.totalGrams;
        buf.writeln('  - ${item.name}: ${grams.toStringAsFixed(0)}g');
      }
      buf.writeln();
    }
    // In a real app, use share_plus package. For now show a snackbar.
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Sharing not yet wired up')),
    );
  }

  void _showAdjustmentsDialog(
    BuildContext context,
    ShoppingNotifier notifier,
    DateTime newStart,
    DateTime newEnd,
  ) {
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Change period?'),
        content: const Text(
            'You have adjustments or checked items. Changing the period will clear them.'),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(ctx).pop();
              notifier.dismissPeriodChange();
            },
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.of(ctx).pop();
              notifier.confirmPeriodChange(newStart, newEnd);
            },
            child: const Text('Change period'),
          ),
        ],
      ),
    );
  }
}

// ── Period card ───────────────────────────────────────────────────────────────

class _PeriodCard extends ConsumerStatefulWidget {
  final ShoppingState state;
  final bool expanded;
  final VoidCallback onToggle;
  final void Function(DateTime, DateTime) onRangeChanged;

  const _PeriodCard({
    required this.state,
    required this.expanded,
    required this.onToggle,
    required this.onRangeChanged,
  });

  @override
  ConsumerState<_PeriodCard> createState() => _PeriodCardState();
}

class _PeriodCardState extends ConsumerState<_PeriodCard> {
  DateTimeRange? _selectedRange;

  @override
  Widget build(BuildContext context) {
    final state = widget.state;
    final range = _selectedRange ??
        DateTimeRange(start: state.startDate, end: state.endDate);

    final recipes = state.shoppingList?.period.recipes ?? [];
    final selectedIds = state.selectedRecipeIds;

    return Card(
      margin: const EdgeInsets.all(12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          InkWell(
            borderRadius: BorderRadius.circular(12),
            onTap: widget.onToggle,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: Row(
                children: [
                  const Icon(Icons.calendar_today, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      '${_dateFmt.format(state.startDate)} – ${_dateFmt.format(state.endDate)}',
                      style: Theme.of(context).textTheme.titleSmall,
                    ),
                  ),
                  Icon(widget.expanded
                      ? Icons.expand_less
                      : Icons.expand_more),
                ],
              ),
            ),
          ),
          if (widget.expanded) ...[
            const Divider(height: 1),
            // Inline calendar range picker
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: CalendarDatePicker(
                initialDate: range.start,
                firstDate: DateTime(2020),
                lastDate: DateTime(2030),
                onDateChanged: (picked) {
                  final current = _selectedRange;
                  if (current == null || picked.isBefore(current.start)) {
                    setState(() => _selectedRange =
                        DateTimeRange(start: picked, end: picked));
                  } else {
                    final newRange =
                        DateTimeRange(start: current.start, end: picked);
                    setState(() => _selectedRange = newRange);
                    widget.onRangeChanged(newRange.start, newRange.end);
                  }
                },
              ),
            ),
            // Recipe filter chips
            if (recipes.isNotEmpty) ...[
              const Divider(height: 1),
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                child: Text('Filter recipes',
                    style: Theme.of(context).textTheme.labelSmall),
              ),
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                child: Wrap(
                  spacing: 6,
                  runSpacing: 4,
                  children: recipes.map((r) {
                    final isSelected = selectedIds == null ||
                        selectedIds.contains(r.recipeId);
                    return FilterChip(
                      label: Text(r.recipeName),
                      selected: isSelected,
                      onSelected: (_) => ref
                          .read(shoppingProvider.notifier)
                          .toggleRecipeFilter(r.recipeId),
                    );
                  }).toList(),
                ),
              ),
            ],
          ],
        ],
      ),
    );
  }
}

// ── Category header ───────────────────────────────────────────────────────────

class _CategoryHeader extends StatelessWidget {
  final String emoji;
  final String name;
  const _CategoryHeader({required this.emoji, required this.name});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 4),
      child: Row(
        children: [
          Text(emoji, style: const TextStyle(fontSize: 18)),
          const SizedBox(width: 8),
          Text(
            name,
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
          ),
        ],
      ),
    );
  }
}

// ── Item row ──────────────────────────────────────────────────────────────────

class _ShoppingItemRow extends StatefulWidget {
  final ShoppingItem item;
  final bool isChecked;
  final bool isExpanded;
  final double? adjustment;
  final VoidCallback onToggle;
  final VoidCallback onExpand;
  final bool Function(String) onCommitExpression;

  const _ShoppingItemRow({
    required this.item,
    required this.isChecked,
    required this.isExpanded,
    required this.adjustment,
    required this.onToggle,
    required this.onExpand,
    required this.onCommitExpression,
  });

  @override
  State<_ShoppingItemRow> createState() => _ShoppingItemRowState();
}

class _ShoppingItemRowState extends State<_ShoppingItemRow> {
  late TextEditingController _exprController;

  @override
  void initState() {
    super.initState();
    final grams = widget.adjustment ?? widget.item.totalGrams;
    _exprController =
        TextEditingController(text: grams.toStringAsFixed(0));
  }

  @override
  void dispose() {
    _exprController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final isAdjusted = widget.adjustment != null;
    final displayGrams = widget.adjustment ?? widget.item.totalGrams;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        ListTile(
          leading: Checkbox(
            value: widget.isChecked,
            onChanged: (_) => widget.onToggle(),
          ),
          title: Text(
            widget.item.name,
            style: widget.isChecked
                ? TextStyle(
                    decoration: TextDecoration.lineThrough,
                    color: colorScheme.outline,
                  )
                : null,
          ),
          subtitle: widget.item.contributions.isNotEmpty
              ? Wrap(
                  spacing: 4,
                  children: widget.item.contributions
                      .take(3)
                      .map((c) => Text(
                            c.recipeName,
                            style:
                                Theme.of(context).textTheme.bodySmall?.copyWith(
                                      color: colorScheme.outline,
                                    ),
                          ))
                      .toList(),
                )
              : null,
          trailing: GestureDetector(
            onTap: widget.onExpand,
            child: Text(
              '${displayGrams.toStringAsFixed(0)}g',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: isAdjusted ? colorScheme.primary : null,
                    fontWeight:
                        isAdjusted ? FontWeight.w600 : null,
                  ),
            ),
          ),
        ),

        // Inline expression editor
        if (widget.isExpanded)
          Padding(
            padding:
                const EdgeInsets.fromLTRB(72, 0, 16, 8),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _exprController,
                    decoration: InputDecoration(
                      isDense: true,
                      border: const OutlineInputBorder(),
                      labelText: 'Amount (e.g. 200+50)',
                      suffixText: 'g',
                    ),
                    keyboardType: const TextInputType.numberWithOptions(
                        decimal: true),
                    onSubmitted: (val) {
                      final ok = widget.onCommitExpression(val);
                      if (!ok) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                              content: Text('Invalid expression')),
                        );
                      }
                    },
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.check),
                  onPressed: () {
                    final ok =
                        widget.onCommitExpression(_exprController.text);
                    if (!ok) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                            content: Text('Invalid expression')),
                      );
                    }
                  },
                ),
              ],
            ),
          ),
      ],
    );
  }
}

// ── Data helpers ──────────────────────────────────────────────────────────────

class _CategorySection {
  final ShoppingCategory category;
  final List<ShoppingItem> items;
  const _CategorySection({required this.category, required this.items});
}

class _FlatItem {
  final ShoppingCategory category;
  final ShoppingItem item;
  const _FlatItem({required this.category, required this.item});
}
