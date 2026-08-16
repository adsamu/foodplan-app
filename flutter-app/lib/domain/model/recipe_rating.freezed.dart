// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'recipe_rating.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$RecipeRating {
  String get recipeId => throw _privateConstructorUsedError;
  int? get stars => throw _privateConstructorUsedError; // 1–5, null = unrated
  int get timesScheduled => throw _privateConstructorUsedError;
  int get timesManuallyRemoved => throw _privateConstructorUsedError;
  bool get isPinned => throw _privateConstructorUsedError;
  bool get isExcluded => throw _privateConstructorUsedError;
  DateTime? get lastScheduledDate => throw _privateConstructorUsedError;

  /// Create a copy of RecipeRating
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $RecipeRatingCopyWith<RecipeRating> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RecipeRatingCopyWith<$Res> {
  factory $RecipeRatingCopyWith(
          RecipeRating value, $Res Function(RecipeRating) then) =
      _$RecipeRatingCopyWithImpl<$Res, RecipeRating>;
  @useResult
  $Res call(
      {String recipeId,
      int? stars,
      int timesScheduled,
      int timesManuallyRemoved,
      bool isPinned,
      bool isExcluded,
      DateTime? lastScheduledDate});
}

/// @nodoc
class _$RecipeRatingCopyWithImpl<$Res, $Val extends RecipeRating>
    implements $RecipeRatingCopyWith<$Res> {
  _$RecipeRatingCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of RecipeRating
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? recipeId = null,
    Object? stars = freezed,
    Object? timesScheduled = null,
    Object? timesManuallyRemoved = null,
    Object? isPinned = null,
    Object? isExcluded = null,
    Object? lastScheduledDate = freezed,
  }) {
    return _then(_value.copyWith(
      recipeId: null == recipeId
          ? _value.recipeId
          : recipeId // ignore: cast_nullable_to_non_nullable
              as String,
      stars: freezed == stars
          ? _value.stars
          : stars // ignore: cast_nullable_to_non_nullable
              as int?,
      timesScheduled: null == timesScheduled
          ? _value.timesScheduled
          : timesScheduled // ignore: cast_nullable_to_non_nullable
              as int,
      timesManuallyRemoved: null == timesManuallyRemoved
          ? _value.timesManuallyRemoved
          : timesManuallyRemoved // ignore: cast_nullable_to_non_nullable
              as int,
      isPinned: null == isPinned
          ? _value.isPinned
          : isPinned // ignore: cast_nullable_to_non_nullable
              as bool,
      isExcluded: null == isExcluded
          ? _value.isExcluded
          : isExcluded // ignore: cast_nullable_to_non_nullable
              as bool,
      lastScheduledDate: freezed == lastScheduledDate
          ? _value.lastScheduledDate
          : lastScheduledDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$RecipeRatingImplCopyWith<$Res>
    implements $RecipeRatingCopyWith<$Res> {
  factory _$$RecipeRatingImplCopyWith(
          _$RecipeRatingImpl value, $Res Function(_$RecipeRatingImpl) then) =
      __$$RecipeRatingImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String recipeId,
      int? stars,
      int timesScheduled,
      int timesManuallyRemoved,
      bool isPinned,
      bool isExcluded,
      DateTime? lastScheduledDate});
}

/// @nodoc
class __$$RecipeRatingImplCopyWithImpl<$Res>
    extends _$RecipeRatingCopyWithImpl<$Res, _$RecipeRatingImpl>
    implements _$$RecipeRatingImplCopyWith<$Res> {
  __$$RecipeRatingImplCopyWithImpl(
      _$RecipeRatingImpl _value, $Res Function(_$RecipeRatingImpl) _then)
      : super(_value, _then);

  /// Create a copy of RecipeRating
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? recipeId = null,
    Object? stars = freezed,
    Object? timesScheduled = null,
    Object? timesManuallyRemoved = null,
    Object? isPinned = null,
    Object? isExcluded = null,
    Object? lastScheduledDate = freezed,
  }) {
    return _then(_$RecipeRatingImpl(
      recipeId: null == recipeId
          ? _value.recipeId
          : recipeId // ignore: cast_nullable_to_non_nullable
              as String,
      stars: freezed == stars
          ? _value.stars
          : stars // ignore: cast_nullable_to_non_nullable
              as int?,
      timesScheduled: null == timesScheduled
          ? _value.timesScheduled
          : timesScheduled // ignore: cast_nullable_to_non_nullable
              as int,
      timesManuallyRemoved: null == timesManuallyRemoved
          ? _value.timesManuallyRemoved
          : timesManuallyRemoved // ignore: cast_nullable_to_non_nullable
              as int,
      isPinned: null == isPinned
          ? _value.isPinned
          : isPinned // ignore: cast_nullable_to_non_nullable
              as bool,
      isExcluded: null == isExcluded
          ? _value.isExcluded
          : isExcluded // ignore: cast_nullable_to_non_nullable
              as bool,
      lastScheduledDate: freezed == lastScheduledDate
          ? _value.lastScheduledDate
          : lastScheduledDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }
}

/// @nodoc

class _$RecipeRatingImpl implements _RecipeRating {
  const _$RecipeRatingImpl(
      {required this.recipeId,
      this.stars,
      this.timesScheduled = 0,
      this.timesManuallyRemoved = 0,
      this.isPinned = false,
      this.isExcluded = false,
      this.lastScheduledDate});

  @override
  final String recipeId;
  @override
  final int? stars;
// 1–5, null = unrated
  @override
  @JsonKey()
  final int timesScheduled;
  @override
  @JsonKey()
  final int timesManuallyRemoved;
  @override
  @JsonKey()
  final bool isPinned;
  @override
  @JsonKey()
  final bool isExcluded;
  @override
  final DateTime? lastScheduledDate;

  @override
  String toString() {
    return 'RecipeRating(recipeId: $recipeId, stars: $stars, timesScheduled: $timesScheduled, timesManuallyRemoved: $timesManuallyRemoved, isPinned: $isPinned, isExcluded: $isExcluded, lastScheduledDate: $lastScheduledDate)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RecipeRatingImpl &&
            (identical(other.recipeId, recipeId) ||
                other.recipeId == recipeId) &&
            (identical(other.stars, stars) || other.stars == stars) &&
            (identical(other.timesScheduled, timesScheduled) ||
                other.timesScheduled == timesScheduled) &&
            (identical(other.timesManuallyRemoved, timesManuallyRemoved) ||
                other.timesManuallyRemoved == timesManuallyRemoved) &&
            (identical(other.isPinned, isPinned) ||
                other.isPinned == isPinned) &&
            (identical(other.isExcluded, isExcluded) ||
                other.isExcluded == isExcluded) &&
            (identical(other.lastScheduledDate, lastScheduledDate) ||
                other.lastScheduledDate == lastScheduledDate));
  }

  @override
  int get hashCode => Object.hash(runtimeType, recipeId, stars, timesScheduled,
      timesManuallyRemoved, isPinned, isExcluded, lastScheduledDate);

  /// Create a copy of RecipeRating
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$RecipeRatingImplCopyWith<_$RecipeRatingImpl> get copyWith =>
      __$$RecipeRatingImplCopyWithImpl<_$RecipeRatingImpl>(this, _$identity);
}

abstract class _RecipeRating implements RecipeRating {
  const factory _RecipeRating(
      {required final String recipeId,
      final int? stars,
      final int timesScheduled,
      final int timesManuallyRemoved,
      final bool isPinned,
      final bool isExcluded,
      final DateTime? lastScheduledDate}) = _$RecipeRatingImpl;

  @override
  String get recipeId;
  @override
  int? get stars; // 1–5, null = unrated
  @override
  int get timesScheduled;
  @override
  int get timesManuallyRemoved;
  @override
  bool get isPinned;
  @override
  bool get isExcluded;
  @override
  DateTime? get lastScheduledDate;

  /// Create a copy of RecipeRating
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$RecipeRatingImplCopyWith<_$RecipeRatingImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
