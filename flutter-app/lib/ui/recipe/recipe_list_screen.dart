import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:foodplan/di/providers.dart';
import 'package:foodplan/domain/model/ingredient.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:go_router/go_router.dart';

// ── Local providers ───────────────────────────────────────────────────────────

final recipeSearchQueryProvider =
    StateProvider.autoDispose<String>((ref) => '');

/// 0 = Meals, 1 = Components, 2 = Ingredients
final recipeListTabProvider =
    StateProvider.autoDispose<int>((ref) => 0);

final activeMealCatProvider =
    StateProvider.autoDispose<MealCategory?>((ref) => null);

final activeIngCatProvider =
    StateProvider.autoDispose<IngredientCategory?>((ref) => null);

final filteredRecipesProvider =
    FutureProvider.autoDispose<List<Recipe>>((ref) async {
  final query = ref.watch(recipeSearchQueryProvider);
  final tab = ref.watch(recipeListTabProvider);
  final mealCat = ref.watch(activeMealCatProvider);
  final repo = ref.watch(recipeRepositoryProvider);

  // Use the stream's first emission as a one-shot fetch
  final all = await repo.watchAllRecipes().first;

  // Filter by type tab
  var filtered = all.where((r) {
    if (tab == 0) return r.type == RecipeType.meal;
    if (tab == 1) return r.type == RecipeType.component;
    return false; // tab 2 = Ingredients, handled separately
  }).toList();

  // Filter by search query
  if (query.isNotEmpty) {
    final q = query.toLowerCase();
    filtered = filtered
        .where((r) => r.name.toLowerCase().contains(q))
        .toList();
  }

  // Filter by meal category (only relevant for Meals tab)
  if (tab == 0 && mealCat != null) {
    filtered =
        filtered.where((r) => r.mealCategories.contains(mealCat)).toList();
  }

  return filtered;
});

final filteredIngredientsProvider =
    FutureProvider.autoDispose<List<Ingredient>>((ref) async {
  final query = ref.watch(recipeSearchQueryProvider);
  final ingCat = ref.watch(activeIngCatProvider);

  // Watch ingredients stream — convert to a one-shot list via first
  final repo = ref.watch(ingredientRepositoryProvider);
  final all = await repo.watchAllIngredients().first;

  var filtered = all;

  if (query.isNotEmpty) {
    final q = query.toLowerCase();
    filtered =
        filtered.where((i) => i.name.toLowerCase().contains(q)).toList();
  }

  if (ingCat != null) {
    filtered = filtered.where((i) => i.category == ingCat).toList();
  }

  return filtered;
});

// ── Screen ────────────────────────────────────────────────────────────────────

class RecipeListScreen extends ConsumerStatefulWidget {
  const RecipeListScreen({super.key});

  @override
  ConsumerState<RecipeListScreen> createState() => _RecipeListScreenState();
}

class _RecipeListScreenState extends ConsumerState<RecipeListScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  late TextEditingController _searchController;
  bool _fabExpanded = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _searchController = TextEditingController();

    _tabController.addListener(() {
      if (!_tabController.indexIsChanging) {
        ref.read(recipeListTabProvider.notifier).state =
            _tabController.index;
      }
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final tab = ref.watch(recipeListTabProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Recipes'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Meals'),
            Tab(text: 'Components'),
            Tab(text: 'Ingredients'),
          ],
        ),
      ),
      body: Column(
        children: [
          // Search bar
          Padding(
            padding:
                const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Search…',
                prefixIcon: const Icon(Icons.search),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(28),
                ),
                contentPadding: const EdgeInsets.symmetric(
                    horizontal: 16, vertical: 10),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                          ref
                              .read(recipeSearchQueryProvider.notifier)
                              .state = '';
                        },
                      )
                    : null,
              ),
              onChanged: (val) {
                ref.read(recipeSearchQueryProvider.notifier).state = val;
              },
            ),
          ),

          // Category filter chips
          if (tab == 0)
            _MealCategoryFilters()
          else if (tab == 1)
            const SizedBox.shrink()
          else
            _IngredientCategoryFilters(),

          // Content list
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: const [
                _RecipeList(tab: 0),
                _RecipeList(tab: 1),
                _IngredientList(),
              ],
            ),
          ),
        ],
      ),
      floatingActionButton: _ExpandableFab(
        expanded: _fabExpanded,
        onToggle: () => setState(() => _fabExpanded = !_fabExpanded),
        onAddRecipe: () {
          setState(() => _fabExpanded = false);
          context.push('/recipes/edit');
        },
        onAddIngredient: () {
          setState(() => _fabExpanded = false);
          context.push('/ingredients/edit');
        },
      ),
    );
  }
}

// ── Filter chips ──────────────────────────────────────────────────────────────

class _MealCategoryFilters extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final active = ref.watch(activeMealCatProvider);

    return SizedBox(
      height: 48,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding:
            const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
        children: [
          Padding(
            padding: const EdgeInsets.only(right: 6),
            child: FilterChip(
              label: const Text('All'),
              selected: active == null,
              onSelected: (_) => ref
                  .read(activeMealCatProvider.notifier)
                  .state = null,
            ),
          ),
          ...MealCategory.values.map((cat) => Padding(
                padding: const EdgeInsets.only(right: 6),
                child: FilterChip(
                  label: Text(_mealCatLabel(cat)),
                  selected: active == cat,
                  onSelected: (_) => ref
                      .read(activeMealCatProvider.notifier)
                      .state = active == cat ? null : cat,
                ),
              )),
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

class _IngredientCategoryFilters extends ConsumerWidget {
  const _IngredientCategoryFilters();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final active = ref.watch(activeIngCatProvider);

    return SizedBox(
      height: 48,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding:
            const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
        children: [
          Padding(
            padding: const EdgeInsets.only(right: 6),
            child: FilterChip(
              label: const Text('All'),
              selected: active == null,
              onSelected: (_) => ref
                  .read(activeIngCatProvider.notifier)
                  .state = null,
            ),
          ),
          ...IngredientCategory.values.map((cat) => Padding(
                padding: const EdgeInsets.only(right: 6),
                child: FilterChip(
                  label: Text('${cat.emoji} ${cat.displayName}'),
                  selected: active == cat,
                  onSelected: (_) => ref
                      .read(activeIngCatProvider.notifier)
                      .state = active == cat ? null : cat,
                ),
              )),
        ],
      ),
    );
  }
}

// ── Recipe list ───────────────────────────────────────────────────────────────

class _RecipeList extends ConsumerWidget {
  final int tab;

  const _RecipeList({required this.tab});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncRecipes = ref.watch(filteredRecipesProvider);

    return asyncRecipes.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (err, _) => Center(child: Text('Error: $err')),
      data: (recipes) {
        if (recipes.isEmpty) {
          return Center(
            child: Text(
              tab == 0 ? 'No meals found.' : 'No components found.',
            ),
          );
        }
        return ListView.builder(
          padding:
              const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          itemCount: recipes.length,
          itemBuilder: (ctx, i) => _RecipeCard(recipe: recipes[i]),
        );
      },
    );
  }
}

class _RecipeCard extends StatelessWidget {
  final Recipe recipe;

  const _RecipeCard({required this.recipe});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Card(
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: colorScheme.secondaryContainer,
          child: Text(
            recipe.name.isNotEmpty
                ? recipe.name[0].toUpperCase()
                : '?',
            style: TextStyle(color: colorScheme.onSecondaryContainer),
          ),
        ),
        title: Text(recipe.name),
        subtitle: recipe.mealCategories.isNotEmpty
            ? Text(
                recipe.mealCategories
                    .map(_mealCatLabel)
                    .join(', '),
                style: Theme.of(context).textTheme.bodySmall,
              )
            : recipe.componentCategory != null
                ? Text(
                    recipe.componentCategory!.name,
                    style: Theme.of(context).textTheme.bodySmall,
                  )
                : null,
        trailing: const Icon(Icons.chevron_right),
        onTap: () => context.push('/recipes/${recipe.id}'),
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

// ── Ingredient list ───────────────────────────────────────────────────────────

class _IngredientList extends ConsumerWidget {
  const _IngredientList();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncIngredients = ref.watch(filteredIngredientsProvider);

    return asyncIngredients.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (err, _) => Center(child: Text('Error: $err')),
      data: (ingredients) {
        if (ingredients.isEmpty) {
          return const Center(child: Text('No ingredients found.'));
        }
        return ListView.builder(
          padding:
              const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          itemCount: ingredients.length,
          itemBuilder: (ctx, i) =>
              _IngredientCard(ingredient: ingredients[i]),
        );
      },
    );
  }
}

class _IngredientCard extends StatelessWidget {
  final Ingredient ingredient;

  const _IngredientCard({required this.ingredient});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Card(
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: colorScheme.tertiaryContainer,
          child: Text(
            ingredient.category.emoji,
            style: const TextStyle(fontSize: 18),
          ),
        ),
        title: Text(ingredient.name),
        subtitle: Text(
          ingredient.category.displayName,
          style: Theme.of(context).textTheme.bodySmall,
        ),
        trailing: Text(
          '${ingredient.kcalPer100g.round()} kcal/100g',
          style: Theme.of(context)
              .textTheme
              .bodySmall
              ?.copyWith(color: colorScheme.outline),
        ),
        onTap: () => context.push('/ingredients/${ingredient.id}'),
      ),
    );
  }
}

// ── Expandable FAB ────────────────────────────────────────────────────────────

class _ExpandableFab extends StatelessWidget {
  final bool expanded;
  final VoidCallback onToggle;
  final VoidCallback onAddRecipe;
  final VoidCallback onAddIngredient;

  const _ExpandableFab({
    required this.expanded,
    required this.onToggle,
    required this.onAddRecipe,
    required this.onAddIngredient,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        if (expanded) ...[
          _MiniActionButton(
            icon: Icons.menu_book_outlined,
            label: 'Add Recipe',
            onPressed: onAddRecipe,
          ),
          const SizedBox(height: 8),
          _MiniActionButton(
            icon: Icons.eco_outlined,
            label: 'Add Ingredient',
            onPressed: onAddIngredient,
          ),
          const SizedBox(height: 8),
        ],
        FloatingActionButton(
          onPressed: onToggle,
          child: AnimatedRotation(
            turns: expanded ? 0.125 : 0,
            duration: const Duration(milliseconds: 200),
            child: const Icon(Icons.add),
          ),
        ),
      ],
    );
  }
}

class _MiniActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onPressed;

  const _MiniActionButton({
    required this.icon,
    required this.label,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          padding:
              const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          decoration: BoxDecoration(
            color:
                Theme.of(context).colorScheme.secondaryContainer,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Text(
            label,
            style: Theme.of(context).textTheme.labelMedium?.copyWith(
                  color: Theme.of(context)
                      .colorScheme
                      .onSecondaryContainer,
                ),
          ),
        ),
        const SizedBox(width: 8),
        FloatingActionButton.small(
          onPressed: onPressed,
          heroTag: label,
          child: Icon(icon),
        ),
      ],
    );
  }
}
