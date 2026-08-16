import 'package:freezed_annotation/freezed_annotation.dart';

part 'recipe_rating.freezed.dart';

@freezed
class RecipeRating with _$RecipeRating {
  const factory RecipeRating({
    required String recipeId,
    int? stars, // 1–5, null = unrated
    @Default(0) int timesScheduled,
    @Default(0) int timesManuallyRemoved,
    @Default(false) bool isPinned,
    @Default(false) bool isExcluded,
    DateTime? lastScheduledDate,
  }) = _RecipeRating;
}
