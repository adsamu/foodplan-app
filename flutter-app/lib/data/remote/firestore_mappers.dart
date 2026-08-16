import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:foodplan/domain/model/ingredient.dart';
import 'package:foodplan/domain/model/meal_plan.dart';
import 'package:foodplan/domain/model/recipe.dart';
import 'package:foodplan/domain/model/recipe_rating.dart';

// ── Recipe ────────────────────────────────────────────────────────────────────

extension RecipeDocumentSnapshot on DocumentSnapshot {
  Recipe? toRecipe() {
    try {
      final data = this.data() as Map<String, dynamic>?;
      if (data == null) return null;
      return Recipe(
        id: id,
        name: data['name'] as String? ?? '',
        type: RecipeType.fromFirestore(data['type'] as String?) ??
            RecipeType.meal,
        mealCategories: ((data['mealCategories'] as List?)
                    ?.map((e) => MealCategory.fromFirestore(e as String?))
                    .whereType<MealCategory>()
                    .toSet()) ??
                const {},
        componentCategory:
            ComponentCategory.fromFirestore(data['componentCategory'] as String?),
        ingredients: ((data['ingredients'] as List?)
                ?.map((e) => _mapToRecipeIngredient(e as Map?))
                .whereType<RecipeIngredient>()
                .toList()) ??
            const [],
        steps: ((data['steps'] as List?)?.cast<String>()) ?? const [],
        notes: data['notes'] as String? ?? '',
      );
    } catch (_) {
      return null;
    }
  }

  Ingredient? toIngredient() {
    try {
      final data = this.data() as Map<String, dynamic>?;
      if (data == null) return null;
      return Ingredient(
        id: id,
        name: data['name'] as String? ?? '',
        category:
            IngredientCategory.fromFirestore(data['category'] as String?),
        kcalPer100g: (data['kcalPer100g'] as num?)?.toDouble() ?? 0.0,
        proteinPer100g: (data['proteinPer100g'] as num?)?.toDouble() ?? 0.0,
        fatPer100g: (data['fatPer100g'] as num?)?.toDouble() ?? 0.0,
        carbsPer100g: (data['carbsPer100g'] as num?)?.toDouble() ?? 0.0,
        source: IngredientSource.fromFirestore(data['source'] as String?),
        steps: ((data['steps'] as List?)?.cast<String>()) ?? const [],
      );
    } catch (_) {
      return null;
    }
  }

  RecipeRating? toRecipeRating() {
    try {
      final data = this.data() as Map<String, dynamic>?;
      if (data == null) return null;
      return RecipeRating(
        recipeId: id,
        stars: (data['stars'] as num?)?.toInt(),
        timesScheduled: (data['timesScheduled'] as num?)?.toInt() ?? 0,
        timesManuallyRemoved:
            (data['timesManuallyRemoved'] as num?)?.toInt() ?? 0,
        isPinned: data['isPinned'] as bool? ?? false,
        isExcluded: data['isExcluded'] as bool? ?? false,
        lastScheduledDate: _parseDate(data['lastScheduledDate']),
      );
    } catch (_) {
      return null;
    }
  }

  MealPlan? toMealPlan() {
    try {
      final data = this.data() as Map<String, dynamic>?;
      if (data == null) return null;
      final startDate = _parseDate(data['startDate']);
      final endDate = _parseDate(data['endDate']);
      if (startDate == null || endDate == null) return null;

      final days = ((data['days'] as List?)
              ?.map((e) => _mapToDayPlan(e as Map?))
              .whereType<DayPlan>()
              .toList()) ??
          const [];

      return MealPlan(
        id: (data['id'] as String?) ?? id,
        name: data['name'] as String? ?? '',
        startDate: startDate,
        endDate: endDate,
        days: days,
      );
    } catch (_) {
      return null;
    }
  }
}

// ── Recipe → Firestore ────────────────────────────────────────────────────────

extension RecipeFirestoreMap on Recipe {
  Map<String, dynamic> toFirestoreMap() => {
        'id': id,
        'name': name,
        'type': type.firestoreName,
        'mealCategories': mealCategories.map((c) => c.firestoreName).toList(),
        'componentCategory': componentCategory?.firestoreName,
        'ingredients': ingredients.map((i) => i.toFirestoreMap()).toList(),
        'steps': steps,
        'notes': notes,
      };
}

extension RecipeIngredientFirestoreMap on RecipeIngredient {
  Map<String, dynamic> toFirestoreMap() => {
        'ingredientId': ingredientId,
        'subRecipeId': subRecipeId,
        'grams': grams,
        'portions': portions,
      };
}

// ── Ingredient → Firestore ────────────────────────────────────────────────────

extension IngredientFirestoreMap on Ingredient {
  Map<String, dynamic> toFirestoreMap() => {
        'id': id,
        'name': name,
        'category': category.firestoreName,
        'kcalPer100g': kcalPer100g,
        'proteinPer100g': proteinPer100g,
        'fatPer100g': fatPer100g,
        'carbsPer100g': carbsPer100g,
        'source': source.firestoreName,
        'steps': steps,
      };
}

// ── RecipeRating → Firestore ──────────────────────────────────────────────────

extension RecipeRatingFirestoreMap on RecipeRating {
  Map<String, dynamic> toFirestoreMap() => {
        'stars': stars,
        'timesScheduled': timesScheduled,
        'timesManuallyRemoved': timesManuallyRemoved,
        'isPinned': isPinned,
        'isExcluded': isExcluded,
        // Stored as ISO string yyyy-MM-dd (matching Kotlin)
        'lastScheduledDate': lastScheduledDate != null
            ? '${lastScheduledDate!.year.toString().padLeft(4, '0')}'
                '-${lastScheduledDate!.month.toString().padLeft(2, '0')}'
                '-${lastScheduledDate!.day.toString().padLeft(2, '0')}'
            : null,
      };
}

// ── Private helpers ───────────────────────────────────────────────────────────

RecipeIngredient? _mapToRecipeIngredient(Map? map) {
  if (map == null) return null;
  try {
    final ingredientId = map['ingredientId'] as String?;
    final subRecipeId = map['subRecipeId'] as String?;
    final grams = (map['grams'] as num?)?.toDouble();
    final portions = (map['portions'] as num?)?.toDouble();
    // Exactly one of ingredientId / subRecipeId must be set
    if ((ingredientId == null) == (subRecipeId == null)) return null;
    return RecipeIngredient(
      ingredientId: ingredientId,
      subRecipeId: subRecipeId,
      grams: grams,
      portions: portions,
    );
  } catch (_) {
    return null;
  }
}

DayPlan? _mapToDayPlan(Map? map) {
  if (map == null) return null;
  try {
    final date = _parseDate(map['date']);
    if (date == null) return null;
    final meals = ((map['meals'] as List?)
            ?.map((e) => _mapToMealSlot(e as Map?))
            .whereType<MealSlot>()
            .toList()) ??
        const [];
    return DayPlan(
      id: map['id'] as String? ?? '',
      date: date,
      meals: meals,
      proteinPowderGrams:
          (map['proteinPowderGrams'] as num?)?.toDouble() ?? 0.0,
      goal: DailyGoal(
        kcalTarget: (map['kcalTarget'] as num?)?.toDouble() ?? 1350.0,
        proteinTarget: (map['proteinTarget'] as num?)?.toDouble() ?? 120.0,
      ),
    );
  } catch (_) {
    return null;
  }
}

MealSlot? _mapToMealSlot(Map? map) {
  if (map == null) return null;
  try {
    final type = MealCategory.fromFirestore(map['type'] as String?);
    final recipeId = map['recipeId'] as String?;
    if (type == null || recipeId == null) return null;
    return MealSlot(type: type, recipeId: recipeId);
  } catch (_) {
    return null;
  }
}

/// Parses a Firestore date value — either a Timestamp or an ISO string.
DateTime? _parseDate(dynamic value) {
  try {
    if (value is Timestamp) {
      final ms = value.seconds * 1000 + value.nanoseconds ~/ 1000000;
      final dt = DateTime.fromMillisecondsSinceEpoch(ms, isUtc: true);
      // Return as midnight UTC date
      return DateTime.utc(dt.year, dt.month, dt.day);
    }
    if (value is String) {
      return DateTime.parse(value);
    }
    return null;
  } catch (_) {
    return null;
  }
}
