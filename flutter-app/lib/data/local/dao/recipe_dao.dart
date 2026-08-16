import 'package:drift/drift.dart';
import 'package:foodplan/data/local/app_database.dart';

part 'recipe_dao.g.dart';

@DriftAccessor(tables: [RecipesTable, RecipeIngredientsTable, IngredientsTable])
class RecipeDao extends DatabaseAccessor<AppDatabase> with _$RecipeDaoMixin {
  RecipeDao(super.db);

  // ── Recipe queries ────────────────────────────────────────────────────────

  /// Watch all recipes ordered by name.
  Stream<List<RecipesTableData>> watchAllRecipes() =>
      (select(recipesTable)..orderBy([(t) => OrderingTerm.asc(t.name)]))
          .watch();

  /// Watch recipes filtered by type string (e.g. 'MEAL', 'COMPONENT').
  Stream<List<RecipesTableData>> watchRecipesByType(String type) =>
      (select(recipesTable)
            ..where((t) => t.type.equals(type))
            ..orderBy([(t) => OrderingTerm.asc(t.name)]))
          .watch();

  /// One-shot fetch of a single recipe by id.
  Future<RecipesTableData?> getRecipeById(String id) =>
      (select(recipesTable)..where((t) => t.id.equals(id)))
          .getSingleOrNull();

  /// Upsert a recipe row.
  Future<void> upsertRecipe(RecipesTableCompanion recipe) =>
      into(recipesTable).insertOnConflictUpdate(recipe);

  /// Delete a recipe by id (cascades to recipe_ingredients).
  Future<void> deleteRecipeById(String id) =>
      (delete(recipesTable)..where((t) => t.id.equals(id))).go();

  // ── RecipeIngredient queries ──────────────────────────────────────────────

  /// All ingredient rows for a recipe.
  Future<List<RecipeIngredientsTableData>> getIngredientsForRecipe(
          String recipeId) =>
      (select(recipeIngredientsTable)
            ..where((t) => t.recipeId.equals(recipeId)))
          .get();

  /// Upsert a batch of recipe ingredient rows.
  Future<void> upsertRecipeIngredients(
      List<RecipeIngredientsTableCompanion> rows) async {
    await batch((b) {
      b.insertAllOnConflictUpdate(recipeIngredientsTable, rows);
    });
  }

  /// Delete all ingredient rows for a recipe.
  Future<void> deleteRecipeIngredients(String recipeId) =>
      (delete(recipeIngredientsTable)
            ..where((t) => t.recipeId.equals(recipeId)))
          .go();

  /// Delete recipe ingredient rows that reference a deleted ingredient.
  Future<void> deleteRecipeIngredientsByIngredientId(String ingredientId) =>
      (delete(recipeIngredientsTable)
            ..where((t) => t.ingredientId.equals(ingredientId)))
          .go();

  // ── JOIN query ────────────────────────────────────────────────────────────

  /// Stream of distinct recipe IDs whose ingredients match [query] on name.
  Stream<List<String>> watchRecipeIdsContainingIngredient(String query) {
    final q = customSelect(
      'SELECT DISTINCT ri.recipeId FROM recipe_ingredients ri '
      'INNER JOIN ingredients i ON ri.ingredientId = i.id '
      "WHERE i.name LIKE '%' || ? || '%'",
      variables: [Variable.withString(query)],
      readsFrom: {recipeIngredientsTable, ingredientsTable},
    );
    return q.watch().map(
          (rows) => rows.map((r) => r.read<String>('recipeId')).toList(),
        );
  }

  /// One-shot list of all meal recipes (type = 'MEAL').
  Future<List<RecipesTableData>> getAllMealRecipesOnce() =>
      (select(recipesTable)
            ..where((t) => t.type.equals('MEAL'))
            ..orderBy([(t) => OrderingTerm.asc(t.name)]))
          .get();
}
