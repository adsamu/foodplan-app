// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'recipe.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$Recipe {
  String get id => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  RecipeType get type => throw _privateConstructorUsedError;
  Set<MealCategory> get mealCategories => throw _privateConstructorUsedError;
  ComponentCategory? get componentCategory =>
      throw _privateConstructorUsedError;
  List<RecipeIngredient> get ingredients => throw _privateConstructorUsedError;
  List<String> get steps => throw _privateConstructorUsedError;
  String get notes => throw _privateConstructorUsedError;

  /// Create a copy of Recipe
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $RecipeCopyWith<Recipe> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RecipeCopyWith<$Res> {
  factory $RecipeCopyWith(Recipe value, $Res Function(Recipe) then) =
      _$RecipeCopyWithImpl<$Res, Recipe>;
  @useResult
  $Res call(
      {String id,
      String name,
      RecipeType type,
      Set<MealCategory> mealCategories,
      ComponentCategory? componentCategory,
      List<RecipeIngredient> ingredients,
      List<String> steps,
      String notes});
}

/// @nodoc
class _$RecipeCopyWithImpl<$Res, $Val extends Recipe>
    implements $RecipeCopyWith<$Res> {
  _$RecipeCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of Recipe
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? type = null,
    Object? mealCategories = null,
    Object? componentCategory = freezed,
    Object? ingredients = null,
    Object? steps = null,
    Object? notes = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as RecipeType,
      mealCategories: null == mealCategories
          ? _value.mealCategories
          : mealCategories // ignore: cast_nullable_to_non_nullable
              as Set<MealCategory>,
      componentCategory: freezed == componentCategory
          ? _value.componentCategory
          : componentCategory // ignore: cast_nullable_to_non_nullable
              as ComponentCategory?,
      ingredients: null == ingredients
          ? _value.ingredients
          : ingredients // ignore: cast_nullable_to_non_nullable
              as List<RecipeIngredient>,
      steps: null == steps
          ? _value.steps
          : steps // ignore: cast_nullable_to_non_nullable
              as List<String>,
      notes: null == notes
          ? _value.notes
          : notes // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$RecipeImplCopyWith<$Res> implements $RecipeCopyWith<$Res> {
  factory _$$RecipeImplCopyWith(
          _$RecipeImpl value, $Res Function(_$RecipeImpl) then) =
      __$$RecipeImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String name,
      RecipeType type,
      Set<MealCategory> mealCategories,
      ComponentCategory? componentCategory,
      List<RecipeIngredient> ingredients,
      List<String> steps,
      String notes});
}

/// @nodoc
class __$$RecipeImplCopyWithImpl<$Res>
    extends _$RecipeCopyWithImpl<$Res, _$RecipeImpl>
    implements _$$RecipeImplCopyWith<$Res> {
  __$$RecipeImplCopyWithImpl(
      _$RecipeImpl _value, $Res Function(_$RecipeImpl) _then)
      : super(_value, _then);

  /// Create a copy of Recipe
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? type = null,
    Object? mealCategories = null,
    Object? componentCategory = freezed,
    Object? ingredients = null,
    Object? steps = null,
    Object? notes = null,
  }) {
    return _then(_$RecipeImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as RecipeType,
      mealCategories: null == mealCategories
          ? _value._mealCategories
          : mealCategories // ignore: cast_nullable_to_non_nullable
              as Set<MealCategory>,
      componentCategory: freezed == componentCategory
          ? _value.componentCategory
          : componentCategory // ignore: cast_nullable_to_non_nullable
              as ComponentCategory?,
      ingredients: null == ingredients
          ? _value._ingredients
          : ingredients // ignore: cast_nullable_to_non_nullable
              as List<RecipeIngredient>,
      steps: null == steps
          ? _value._steps
          : steps // ignore: cast_nullable_to_non_nullable
              as List<String>,
      notes: null == notes
          ? _value.notes
          : notes // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$RecipeImpl implements _Recipe {
  const _$RecipeImpl(
      {required this.id,
      required this.name,
      this.type = RecipeType.meal,
      final Set<MealCategory> mealCategories = const <MealCategory>{},
      this.componentCategory,
      final List<RecipeIngredient> ingredients = const <RecipeIngredient>[],
      final List<String> steps = const <String>[],
      this.notes = ''})
      : _mealCategories = mealCategories,
        _ingredients = ingredients,
        _steps = steps;

  @override
  final String id;
  @override
  final String name;
  @override
  @JsonKey()
  final RecipeType type;
  final Set<MealCategory> _mealCategories;
  @override
  @JsonKey()
  Set<MealCategory> get mealCategories {
    if (_mealCategories is EqualUnmodifiableSetView) return _mealCategories;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_mealCategories);
  }

  @override
  final ComponentCategory? componentCategory;
  final List<RecipeIngredient> _ingredients;
  @override
  @JsonKey()
  List<RecipeIngredient> get ingredients {
    if (_ingredients is EqualUnmodifiableListView) return _ingredients;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_ingredients);
  }

  final List<String> _steps;
  @override
  @JsonKey()
  List<String> get steps {
    if (_steps is EqualUnmodifiableListView) return _steps;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_steps);
  }

  @override
  @JsonKey()
  final String notes;

  @override
  String toString() {
    return 'Recipe(id: $id, name: $name, type: $type, mealCategories: $mealCategories, componentCategory: $componentCategory, ingredients: $ingredients, steps: $steps, notes: $notes)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RecipeImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.type, type) || other.type == type) &&
            const DeepCollectionEquality()
                .equals(other._mealCategories, _mealCategories) &&
            (identical(other.componentCategory, componentCategory) ||
                other.componentCategory == componentCategory) &&
            const DeepCollectionEquality()
                .equals(other._ingredients, _ingredients) &&
            const DeepCollectionEquality().equals(other._steps, _steps) &&
            (identical(other.notes, notes) || other.notes == notes));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      name,
      type,
      const DeepCollectionEquality().hash(_mealCategories),
      componentCategory,
      const DeepCollectionEquality().hash(_ingredients),
      const DeepCollectionEquality().hash(_steps),
      notes);

  /// Create a copy of Recipe
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$RecipeImplCopyWith<_$RecipeImpl> get copyWith =>
      __$$RecipeImplCopyWithImpl<_$RecipeImpl>(this, _$identity);
}

abstract class _Recipe implements Recipe {
  const factory _Recipe(
      {required final String id,
      required final String name,
      final RecipeType type,
      final Set<MealCategory> mealCategories,
      final ComponentCategory? componentCategory,
      final List<RecipeIngredient> ingredients,
      final List<String> steps,
      final String notes}) = _$RecipeImpl;

  @override
  String get id;
  @override
  String get name;
  @override
  RecipeType get type;
  @override
  Set<MealCategory> get mealCategories;
  @override
  ComponentCategory? get componentCategory;
  @override
  List<RecipeIngredient> get ingredients;
  @override
  List<String> get steps;
  @override
  String get notes;

  /// Create a copy of Recipe
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$RecipeImplCopyWith<_$RecipeImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$RecipeIngredient {
  String? get ingredientId => throw _privateConstructorUsedError;
  String? get subRecipeId => throw _privateConstructorUsedError;
  double? get grams => throw _privateConstructorUsedError;
  double? get portions => throw _privateConstructorUsedError;

  /// Create a copy of RecipeIngredient
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $RecipeIngredientCopyWith<RecipeIngredient> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RecipeIngredientCopyWith<$Res> {
  factory $RecipeIngredientCopyWith(
          RecipeIngredient value, $Res Function(RecipeIngredient) then) =
      _$RecipeIngredientCopyWithImpl<$Res, RecipeIngredient>;
  @useResult
  $Res call(
      {String? ingredientId,
      String? subRecipeId,
      double? grams,
      double? portions});
}

/// @nodoc
class _$RecipeIngredientCopyWithImpl<$Res, $Val extends RecipeIngredient>
    implements $RecipeIngredientCopyWith<$Res> {
  _$RecipeIngredientCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of RecipeIngredient
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ingredientId = freezed,
    Object? subRecipeId = freezed,
    Object? grams = freezed,
    Object? portions = freezed,
  }) {
    return _then(_value.copyWith(
      ingredientId: freezed == ingredientId
          ? _value.ingredientId
          : ingredientId // ignore: cast_nullable_to_non_nullable
              as String?,
      subRecipeId: freezed == subRecipeId
          ? _value.subRecipeId
          : subRecipeId // ignore: cast_nullable_to_non_nullable
              as String?,
      grams: freezed == grams
          ? _value.grams
          : grams // ignore: cast_nullable_to_non_nullable
              as double?,
      portions: freezed == portions
          ? _value.portions
          : portions // ignore: cast_nullable_to_non_nullable
              as double?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$RecipeIngredientImplCopyWith<$Res>
    implements $RecipeIngredientCopyWith<$Res> {
  factory _$$RecipeIngredientImplCopyWith(_$RecipeIngredientImpl value,
          $Res Function(_$RecipeIngredientImpl) then) =
      __$$RecipeIngredientImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? ingredientId,
      String? subRecipeId,
      double? grams,
      double? portions});
}

/// @nodoc
class __$$RecipeIngredientImplCopyWithImpl<$Res>
    extends _$RecipeIngredientCopyWithImpl<$Res, _$RecipeIngredientImpl>
    implements _$$RecipeIngredientImplCopyWith<$Res> {
  __$$RecipeIngredientImplCopyWithImpl(_$RecipeIngredientImpl _value,
      $Res Function(_$RecipeIngredientImpl) _then)
      : super(_value, _then);

  /// Create a copy of RecipeIngredient
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ingredientId = freezed,
    Object? subRecipeId = freezed,
    Object? grams = freezed,
    Object? portions = freezed,
  }) {
    return _then(_$RecipeIngredientImpl(
      ingredientId: freezed == ingredientId
          ? _value.ingredientId
          : ingredientId // ignore: cast_nullable_to_non_nullable
              as String?,
      subRecipeId: freezed == subRecipeId
          ? _value.subRecipeId
          : subRecipeId // ignore: cast_nullable_to_non_nullable
              as String?,
      grams: freezed == grams
          ? _value.grams
          : grams // ignore: cast_nullable_to_non_nullable
              as double?,
      portions: freezed == portions
          ? _value.portions
          : portions // ignore: cast_nullable_to_non_nullable
              as double?,
    ));
  }
}

/// @nodoc

class _$RecipeIngredientImpl extends _RecipeIngredient {
  const _$RecipeIngredientImpl(
      {this.ingredientId, this.subRecipeId, this.grams, this.portions})
      : super._();

  @override
  final String? ingredientId;
  @override
  final String? subRecipeId;
  @override
  final double? grams;
  @override
  final double? portions;

  @override
  String toString() {
    return 'RecipeIngredient(ingredientId: $ingredientId, subRecipeId: $subRecipeId, grams: $grams, portions: $portions)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RecipeIngredientImpl &&
            (identical(other.ingredientId, ingredientId) ||
                other.ingredientId == ingredientId) &&
            (identical(other.subRecipeId, subRecipeId) ||
                other.subRecipeId == subRecipeId) &&
            (identical(other.grams, grams) || other.grams == grams) &&
            (identical(other.portions, portions) ||
                other.portions == portions));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, ingredientId, subRecipeId, grams, portions);

  /// Create a copy of RecipeIngredient
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$RecipeIngredientImplCopyWith<_$RecipeIngredientImpl> get copyWith =>
      __$$RecipeIngredientImplCopyWithImpl<_$RecipeIngredientImpl>(
          this, _$identity);
}

abstract class _RecipeIngredient extends RecipeIngredient {
  const factory _RecipeIngredient(
      {final String? ingredientId,
      final String? subRecipeId,
      final double? grams,
      final double? portions}) = _$RecipeIngredientImpl;
  const _RecipeIngredient._() : super._();

  @override
  String? get ingredientId;
  @override
  String? get subRecipeId;
  @override
  double? get grams;
  @override
  double? get portions;

  /// Create a copy of RecipeIngredient
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$RecipeIngredientImplCopyWith<_$RecipeIngredientImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
