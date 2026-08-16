// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'shopping_list.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$ShoppingList {
  ShoppingPeriod get period => throw _privateConstructorUsedError;
  List<ShoppingCategory> get categories => throw _privateConstructorUsedError;

  /// Create a copy of ShoppingList
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ShoppingListCopyWith<ShoppingList> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ShoppingListCopyWith<$Res> {
  factory $ShoppingListCopyWith(
          ShoppingList value, $Res Function(ShoppingList) then) =
      _$ShoppingListCopyWithImpl<$Res, ShoppingList>;
  @useResult
  $Res call({ShoppingPeriod period, List<ShoppingCategory> categories});

  $ShoppingPeriodCopyWith<$Res> get period;
}

/// @nodoc
class _$ShoppingListCopyWithImpl<$Res, $Val extends ShoppingList>
    implements $ShoppingListCopyWith<$Res> {
  _$ShoppingListCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ShoppingList
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? period = null,
    Object? categories = null,
  }) {
    return _then(_value.copyWith(
      period: null == period
          ? _value.period
          : period // ignore: cast_nullable_to_non_nullable
              as ShoppingPeriod,
      categories: null == categories
          ? _value.categories
          : categories // ignore: cast_nullable_to_non_nullable
              as List<ShoppingCategory>,
    ) as $Val);
  }

  /// Create a copy of ShoppingList
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $ShoppingPeriodCopyWith<$Res> get period {
    return $ShoppingPeriodCopyWith<$Res>(_value.period, (value) {
      return _then(_value.copyWith(period: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$ShoppingListImplCopyWith<$Res>
    implements $ShoppingListCopyWith<$Res> {
  factory _$$ShoppingListImplCopyWith(
          _$ShoppingListImpl value, $Res Function(_$ShoppingListImpl) then) =
      __$$ShoppingListImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({ShoppingPeriod period, List<ShoppingCategory> categories});

  @override
  $ShoppingPeriodCopyWith<$Res> get period;
}

/// @nodoc
class __$$ShoppingListImplCopyWithImpl<$Res>
    extends _$ShoppingListCopyWithImpl<$Res, _$ShoppingListImpl>
    implements _$$ShoppingListImplCopyWith<$Res> {
  __$$ShoppingListImplCopyWithImpl(
      _$ShoppingListImpl _value, $Res Function(_$ShoppingListImpl) _then)
      : super(_value, _then);

  /// Create a copy of ShoppingList
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? period = null,
    Object? categories = null,
  }) {
    return _then(_$ShoppingListImpl(
      period: null == period
          ? _value.period
          : period // ignore: cast_nullable_to_non_nullable
              as ShoppingPeriod,
      categories: null == categories
          ? _value._categories
          : categories // ignore: cast_nullable_to_non_nullable
              as List<ShoppingCategory>,
    ));
  }
}

/// @nodoc

class _$ShoppingListImpl extends _ShoppingList {
  const _$ShoppingListImpl(
      {required this.period, required final List<ShoppingCategory> categories})
      : _categories = categories,
        super._();

  @override
  final ShoppingPeriod period;
  final List<ShoppingCategory> _categories;
  @override
  List<ShoppingCategory> get categories {
    if (_categories is EqualUnmodifiableListView) return _categories;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_categories);
  }

  @override
  String toString() {
    return 'ShoppingList(period: $period, categories: $categories)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ShoppingListImpl &&
            (identical(other.period, period) || other.period == period) &&
            const DeepCollectionEquality()
                .equals(other._categories, _categories));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, period, const DeepCollectionEquality().hash(_categories));

  /// Create a copy of ShoppingList
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ShoppingListImplCopyWith<_$ShoppingListImpl> get copyWith =>
      __$$ShoppingListImplCopyWithImpl<_$ShoppingListImpl>(this, _$identity);
}

abstract class _ShoppingList extends ShoppingList {
  const factory _ShoppingList(
      {required final ShoppingPeriod period,
      required final List<ShoppingCategory> categories}) = _$ShoppingListImpl;
  const _ShoppingList._() : super._();

  @override
  ShoppingPeriod get period;
  @override
  List<ShoppingCategory> get categories;

  /// Create a copy of ShoppingList
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ShoppingListImplCopyWith<_$ShoppingListImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ShoppingPeriod {
  DateTime get startDate => throw _privateConstructorUsedError;
  DateTime get endDate => throw _privateConstructorUsedError;
  List<SelectableRecipe> get recipes => throw _privateConstructorUsedError;

  /// Create a copy of ShoppingPeriod
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ShoppingPeriodCopyWith<ShoppingPeriod> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ShoppingPeriodCopyWith<$Res> {
  factory $ShoppingPeriodCopyWith(
          ShoppingPeriod value, $Res Function(ShoppingPeriod) then) =
      _$ShoppingPeriodCopyWithImpl<$Res, ShoppingPeriod>;
  @useResult
  $Res call(
      {DateTime startDate, DateTime endDate, List<SelectableRecipe> recipes});
}

/// @nodoc
class _$ShoppingPeriodCopyWithImpl<$Res, $Val extends ShoppingPeriod>
    implements $ShoppingPeriodCopyWith<$Res> {
  _$ShoppingPeriodCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ShoppingPeriod
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? startDate = null,
    Object? endDate = null,
    Object? recipes = null,
  }) {
    return _then(_value.copyWith(
      startDate: null == startDate
          ? _value.startDate
          : startDate // ignore: cast_nullable_to_non_nullable
              as DateTime,
      endDate: null == endDate
          ? _value.endDate
          : endDate // ignore: cast_nullable_to_non_nullable
              as DateTime,
      recipes: null == recipes
          ? _value.recipes
          : recipes // ignore: cast_nullable_to_non_nullable
              as List<SelectableRecipe>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ShoppingPeriodImplCopyWith<$Res>
    implements $ShoppingPeriodCopyWith<$Res> {
  factory _$$ShoppingPeriodImplCopyWith(_$ShoppingPeriodImpl value,
          $Res Function(_$ShoppingPeriodImpl) then) =
      __$$ShoppingPeriodImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {DateTime startDate, DateTime endDate, List<SelectableRecipe> recipes});
}

/// @nodoc
class __$$ShoppingPeriodImplCopyWithImpl<$Res>
    extends _$ShoppingPeriodCopyWithImpl<$Res, _$ShoppingPeriodImpl>
    implements _$$ShoppingPeriodImplCopyWith<$Res> {
  __$$ShoppingPeriodImplCopyWithImpl(
      _$ShoppingPeriodImpl _value, $Res Function(_$ShoppingPeriodImpl) _then)
      : super(_value, _then);

  /// Create a copy of ShoppingPeriod
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? startDate = null,
    Object? endDate = null,
    Object? recipes = null,
  }) {
    return _then(_$ShoppingPeriodImpl(
      startDate: null == startDate
          ? _value.startDate
          : startDate // ignore: cast_nullable_to_non_nullable
              as DateTime,
      endDate: null == endDate
          ? _value.endDate
          : endDate // ignore: cast_nullable_to_non_nullable
              as DateTime,
      recipes: null == recipes
          ? _value._recipes
          : recipes // ignore: cast_nullable_to_non_nullable
              as List<SelectableRecipe>,
    ));
  }
}

/// @nodoc

class _$ShoppingPeriodImpl extends _ShoppingPeriod {
  const _$ShoppingPeriodImpl(
      {required this.startDate,
      required this.endDate,
      final List<SelectableRecipe> recipes = const <SelectableRecipe>[]})
      : _recipes = recipes,
        super._();

  @override
  final DateTime startDate;
  @override
  final DateTime endDate;
  final List<SelectableRecipe> _recipes;
  @override
  @JsonKey()
  List<SelectableRecipe> get recipes {
    if (_recipes is EqualUnmodifiableListView) return _recipes;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_recipes);
  }

  @override
  String toString() {
    return 'ShoppingPeriod(startDate: $startDate, endDate: $endDate, recipes: $recipes)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ShoppingPeriodImpl &&
            (identical(other.startDate, startDate) ||
                other.startDate == startDate) &&
            (identical(other.endDate, endDate) || other.endDate == endDate) &&
            const DeepCollectionEquality().equals(other._recipes, _recipes));
  }

  @override
  int get hashCode => Object.hash(runtimeType, startDate, endDate,
      const DeepCollectionEquality().hash(_recipes));

  /// Create a copy of ShoppingPeriod
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ShoppingPeriodImplCopyWith<_$ShoppingPeriodImpl> get copyWith =>
      __$$ShoppingPeriodImplCopyWithImpl<_$ShoppingPeriodImpl>(
          this, _$identity);
}

abstract class _ShoppingPeriod extends ShoppingPeriod {
  const factory _ShoppingPeriod(
      {required final DateTime startDate,
      required final DateTime endDate,
      final List<SelectableRecipe> recipes}) = _$ShoppingPeriodImpl;
  const _ShoppingPeriod._() : super._();

  @override
  DateTime get startDate;
  @override
  DateTime get endDate;
  @override
  List<SelectableRecipe> get recipes;

  /// Create a copy of ShoppingPeriod
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ShoppingPeriodImplCopyWith<_$ShoppingPeriodImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$SelectableRecipe {
  String get recipeId => throw _privateConstructorUsedError;
  String get recipeName => throw _privateConstructorUsedError;
  bool get isSelected => throw _privateConstructorUsedError;

  /// Create a copy of SelectableRecipe
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $SelectableRecipeCopyWith<SelectableRecipe> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SelectableRecipeCopyWith<$Res> {
  factory $SelectableRecipeCopyWith(
          SelectableRecipe value, $Res Function(SelectableRecipe) then) =
      _$SelectableRecipeCopyWithImpl<$Res, SelectableRecipe>;
  @useResult
  $Res call({String recipeId, String recipeName, bool isSelected});
}

/// @nodoc
class _$SelectableRecipeCopyWithImpl<$Res, $Val extends SelectableRecipe>
    implements $SelectableRecipeCopyWith<$Res> {
  _$SelectableRecipeCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of SelectableRecipe
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? recipeId = null,
    Object? recipeName = null,
    Object? isSelected = null,
  }) {
    return _then(_value.copyWith(
      recipeId: null == recipeId
          ? _value.recipeId
          : recipeId // ignore: cast_nullable_to_non_nullable
              as String,
      recipeName: null == recipeName
          ? _value.recipeName
          : recipeName // ignore: cast_nullable_to_non_nullable
              as String,
      isSelected: null == isSelected
          ? _value.isSelected
          : isSelected // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SelectableRecipeImplCopyWith<$Res>
    implements $SelectableRecipeCopyWith<$Res> {
  factory _$$SelectableRecipeImplCopyWith(_$SelectableRecipeImpl value,
          $Res Function(_$SelectableRecipeImpl) then) =
      __$$SelectableRecipeImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String recipeId, String recipeName, bool isSelected});
}

/// @nodoc
class __$$SelectableRecipeImplCopyWithImpl<$Res>
    extends _$SelectableRecipeCopyWithImpl<$Res, _$SelectableRecipeImpl>
    implements _$$SelectableRecipeImplCopyWith<$Res> {
  __$$SelectableRecipeImplCopyWithImpl(_$SelectableRecipeImpl _value,
      $Res Function(_$SelectableRecipeImpl) _then)
      : super(_value, _then);

  /// Create a copy of SelectableRecipe
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? recipeId = null,
    Object? recipeName = null,
    Object? isSelected = null,
  }) {
    return _then(_$SelectableRecipeImpl(
      recipeId: null == recipeId
          ? _value.recipeId
          : recipeId // ignore: cast_nullable_to_non_nullable
              as String,
      recipeName: null == recipeName
          ? _value.recipeName
          : recipeName // ignore: cast_nullable_to_non_nullable
              as String,
      isSelected: null == isSelected
          ? _value.isSelected
          : isSelected // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$SelectableRecipeImpl implements _SelectableRecipe {
  const _$SelectableRecipeImpl(
      {required this.recipeId,
      required this.recipeName,
      this.isSelected = true});

  @override
  final String recipeId;
  @override
  final String recipeName;
  @override
  @JsonKey()
  final bool isSelected;

  @override
  String toString() {
    return 'SelectableRecipe(recipeId: $recipeId, recipeName: $recipeName, isSelected: $isSelected)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SelectableRecipeImpl &&
            (identical(other.recipeId, recipeId) ||
                other.recipeId == recipeId) &&
            (identical(other.recipeName, recipeName) ||
                other.recipeName == recipeName) &&
            (identical(other.isSelected, isSelected) ||
                other.isSelected == isSelected));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, recipeId, recipeName, isSelected);

  /// Create a copy of SelectableRecipe
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$SelectableRecipeImplCopyWith<_$SelectableRecipeImpl> get copyWith =>
      __$$SelectableRecipeImplCopyWithImpl<_$SelectableRecipeImpl>(
          this, _$identity);
}

abstract class _SelectableRecipe implements SelectableRecipe {
  const factory _SelectableRecipe(
      {required final String recipeId,
      required final String recipeName,
      final bool isSelected}) = _$SelectableRecipeImpl;

  @override
  String get recipeId;
  @override
  String get recipeName;
  @override
  bool get isSelected;

  /// Create a copy of SelectableRecipe
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$SelectableRecipeImplCopyWith<_$SelectableRecipeImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ShoppingCategory {
  String get name => throw _privateConstructorUsedError;
  String get emoji => throw _privateConstructorUsedError;
  List<ShoppingItem> get items => throw _privateConstructorUsedError;

  /// Create a copy of ShoppingCategory
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ShoppingCategoryCopyWith<ShoppingCategory> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ShoppingCategoryCopyWith<$Res> {
  factory $ShoppingCategoryCopyWith(
          ShoppingCategory value, $Res Function(ShoppingCategory) then) =
      _$ShoppingCategoryCopyWithImpl<$Res, ShoppingCategory>;
  @useResult
  $Res call({String name, String emoji, List<ShoppingItem> items});
}

/// @nodoc
class _$ShoppingCategoryCopyWithImpl<$Res, $Val extends ShoppingCategory>
    implements $ShoppingCategoryCopyWith<$Res> {
  _$ShoppingCategoryCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ShoppingCategory
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? name = null,
    Object? emoji = null,
    Object? items = null,
  }) {
    return _then(_value.copyWith(
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      emoji: null == emoji
          ? _value.emoji
          : emoji // ignore: cast_nullable_to_non_nullable
              as String,
      items: null == items
          ? _value.items
          : items // ignore: cast_nullable_to_non_nullable
              as List<ShoppingItem>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ShoppingCategoryImplCopyWith<$Res>
    implements $ShoppingCategoryCopyWith<$Res> {
  factory _$$ShoppingCategoryImplCopyWith(_$ShoppingCategoryImpl value,
          $Res Function(_$ShoppingCategoryImpl) then) =
      __$$ShoppingCategoryImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String name, String emoji, List<ShoppingItem> items});
}

/// @nodoc
class __$$ShoppingCategoryImplCopyWithImpl<$Res>
    extends _$ShoppingCategoryCopyWithImpl<$Res, _$ShoppingCategoryImpl>
    implements _$$ShoppingCategoryImplCopyWith<$Res> {
  __$$ShoppingCategoryImplCopyWithImpl(_$ShoppingCategoryImpl _value,
      $Res Function(_$ShoppingCategoryImpl) _then)
      : super(_value, _then);

  /// Create a copy of ShoppingCategory
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? name = null,
    Object? emoji = null,
    Object? items = null,
  }) {
    return _then(_$ShoppingCategoryImpl(
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      emoji: null == emoji
          ? _value.emoji
          : emoji // ignore: cast_nullable_to_non_nullable
              as String,
      items: null == items
          ? _value._items
          : items // ignore: cast_nullable_to_non_nullable
              as List<ShoppingItem>,
    ));
  }
}

/// @nodoc

class _$ShoppingCategoryImpl implements _ShoppingCategory {
  const _$ShoppingCategoryImpl(
      {required this.name,
      required this.emoji,
      required final List<ShoppingItem> items})
      : _items = items;

  @override
  final String name;
  @override
  final String emoji;
  final List<ShoppingItem> _items;
  @override
  List<ShoppingItem> get items {
    if (_items is EqualUnmodifiableListView) return _items;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_items);
  }

  @override
  String toString() {
    return 'ShoppingCategory(name: $name, emoji: $emoji, items: $items)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ShoppingCategoryImpl &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.emoji, emoji) || other.emoji == emoji) &&
            const DeepCollectionEquality().equals(other._items, _items));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, name, emoji, const DeepCollectionEquality().hash(_items));

  /// Create a copy of ShoppingCategory
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ShoppingCategoryImplCopyWith<_$ShoppingCategoryImpl> get copyWith =>
      __$$ShoppingCategoryImplCopyWithImpl<_$ShoppingCategoryImpl>(
          this, _$identity);
}

abstract class _ShoppingCategory implements ShoppingCategory {
  const factory _ShoppingCategory(
      {required final String name,
      required final String emoji,
      required final List<ShoppingItem> items}) = _$ShoppingCategoryImpl;

  @override
  String get name;
  @override
  String get emoji;
  @override
  List<ShoppingItem> get items;

  /// Create a copy of ShoppingCategory
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ShoppingCategoryImplCopyWith<_$ShoppingCategoryImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ShoppingItem {
  String get ingredientId => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  double get totalGrams => throw _privateConstructorUsedError;
  ShoppingUnit get unit => throw _privateConstructorUsedError;
  List<RecipeContribution> get contributions =>
      throw _privateConstructorUsedError;

  /// Create a copy of ShoppingItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ShoppingItemCopyWith<ShoppingItem> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ShoppingItemCopyWith<$Res> {
  factory $ShoppingItemCopyWith(
          ShoppingItem value, $Res Function(ShoppingItem) then) =
      _$ShoppingItemCopyWithImpl<$Res, ShoppingItem>;
  @useResult
  $Res call(
      {String ingredientId,
      String name,
      double totalGrams,
      ShoppingUnit unit,
      List<RecipeContribution> contributions});
}

/// @nodoc
class _$ShoppingItemCopyWithImpl<$Res, $Val extends ShoppingItem>
    implements $ShoppingItemCopyWith<$Res> {
  _$ShoppingItemCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ShoppingItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ingredientId = null,
    Object? name = null,
    Object? totalGrams = null,
    Object? unit = null,
    Object? contributions = null,
  }) {
    return _then(_value.copyWith(
      ingredientId: null == ingredientId
          ? _value.ingredientId
          : ingredientId // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      totalGrams: null == totalGrams
          ? _value.totalGrams
          : totalGrams // ignore: cast_nullable_to_non_nullable
              as double,
      unit: null == unit
          ? _value.unit
          : unit // ignore: cast_nullable_to_non_nullable
              as ShoppingUnit,
      contributions: null == contributions
          ? _value.contributions
          : contributions // ignore: cast_nullable_to_non_nullable
              as List<RecipeContribution>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ShoppingItemImplCopyWith<$Res>
    implements $ShoppingItemCopyWith<$Res> {
  factory _$$ShoppingItemImplCopyWith(
          _$ShoppingItemImpl value, $Res Function(_$ShoppingItemImpl) then) =
      __$$ShoppingItemImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String ingredientId,
      String name,
      double totalGrams,
      ShoppingUnit unit,
      List<RecipeContribution> contributions});
}

/// @nodoc
class __$$ShoppingItemImplCopyWithImpl<$Res>
    extends _$ShoppingItemCopyWithImpl<$Res, _$ShoppingItemImpl>
    implements _$$ShoppingItemImplCopyWith<$Res> {
  __$$ShoppingItemImplCopyWithImpl(
      _$ShoppingItemImpl _value, $Res Function(_$ShoppingItemImpl) _then)
      : super(_value, _then);

  /// Create a copy of ShoppingItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ingredientId = null,
    Object? name = null,
    Object? totalGrams = null,
    Object? unit = null,
    Object? contributions = null,
  }) {
    return _then(_$ShoppingItemImpl(
      ingredientId: null == ingredientId
          ? _value.ingredientId
          : ingredientId // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      totalGrams: null == totalGrams
          ? _value.totalGrams
          : totalGrams // ignore: cast_nullable_to_non_nullable
              as double,
      unit: null == unit
          ? _value.unit
          : unit // ignore: cast_nullable_to_non_nullable
              as ShoppingUnit,
      contributions: null == contributions
          ? _value._contributions
          : contributions // ignore: cast_nullable_to_non_nullable
              as List<RecipeContribution>,
    ));
  }
}

/// @nodoc

class _$ShoppingItemImpl extends _ShoppingItem {
  const _$ShoppingItemImpl(
      {required this.ingredientId,
      required this.name,
      required this.totalGrams,
      required this.unit,
      required final List<RecipeContribution> contributions})
      : _contributions = contributions,
        super._();

  @override
  final String ingredientId;
  @override
  final String name;
  @override
  final double totalGrams;
  @override
  final ShoppingUnit unit;
  final List<RecipeContribution> _contributions;
  @override
  List<RecipeContribution> get contributions {
    if (_contributions is EqualUnmodifiableListView) return _contributions;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_contributions);
  }

  @override
  String toString() {
    return 'ShoppingItem(ingredientId: $ingredientId, name: $name, totalGrams: $totalGrams, unit: $unit, contributions: $contributions)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ShoppingItemImpl &&
            (identical(other.ingredientId, ingredientId) ||
                other.ingredientId == ingredientId) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.totalGrams, totalGrams) ||
                other.totalGrams == totalGrams) &&
            (identical(other.unit, unit) || other.unit == unit) &&
            const DeepCollectionEquality()
                .equals(other._contributions, _contributions));
  }

  @override
  int get hashCode => Object.hash(runtimeType, ingredientId, name, totalGrams,
      unit, const DeepCollectionEquality().hash(_contributions));

  /// Create a copy of ShoppingItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ShoppingItemImplCopyWith<_$ShoppingItemImpl> get copyWith =>
      __$$ShoppingItemImplCopyWithImpl<_$ShoppingItemImpl>(this, _$identity);
}

abstract class _ShoppingItem extends ShoppingItem {
  const factory _ShoppingItem(
          {required final String ingredientId,
          required final String name,
          required final double totalGrams,
          required final ShoppingUnit unit,
          required final List<RecipeContribution> contributions}) =
      _$ShoppingItemImpl;
  const _ShoppingItem._() : super._();

  @override
  String get ingredientId;
  @override
  String get name;
  @override
  double get totalGrams;
  @override
  ShoppingUnit get unit;
  @override
  List<RecipeContribution> get contributions;

  /// Create a copy of ShoppingItem
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ShoppingItemImplCopyWith<_$ShoppingItemImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$RecipeContribution {
  String get recipeName => throw _privateConstructorUsedError;
  double get grams => throw _privateConstructorUsedError;

  /// Create a copy of RecipeContribution
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $RecipeContributionCopyWith<RecipeContribution> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RecipeContributionCopyWith<$Res> {
  factory $RecipeContributionCopyWith(
          RecipeContribution value, $Res Function(RecipeContribution) then) =
      _$RecipeContributionCopyWithImpl<$Res, RecipeContribution>;
  @useResult
  $Res call({String recipeName, double grams});
}

/// @nodoc
class _$RecipeContributionCopyWithImpl<$Res, $Val extends RecipeContribution>
    implements $RecipeContributionCopyWith<$Res> {
  _$RecipeContributionCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of RecipeContribution
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? recipeName = null,
    Object? grams = null,
  }) {
    return _then(_value.copyWith(
      recipeName: null == recipeName
          ? _value.recipeName
          : recipeName // ignore: cast_nullable_to_non_nullable
              as String,
      grams: null == grams
          ? _value.grams
          : grams // ignore: cast_nullable_to_non_nullable
              as double,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$RecipeContributionImplCopyWith<$Res>
    implements $RecipeContributionCopyWith<$Res> {
  factory _$$RecipeContributionImplCopyWith(_$RecipeContributionImpl value,
          $Res Function(_$RecipeContributionImpl) then) =
      __$$RecipeContributionImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String recipeName, double grams});
}

/// @nodoc
class __$$RecipeContributionImplCopyWithImpl<$Res>
    extends _$RecipeContributionCopyWithImpl<$Res, _$RecipeContributionImpl>
    implements _$$RecipeContributionImplCopyWith<$Res> {
  __$$RecipeContributionImplCopyWithImpl(_$RecipeContributionImpl _value,
      $Res Function(_$RecipeContributionImpl) _then)
      : super(_value, _then);

  /// Create a copy of RecipeContribution
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? recipeName = null,
    Object? grams = null,
  }) {
    return _then(_$RecipeContributionImpl(
      recipeName: null == recipeName
          ? _value.recipeName
          : recipeName // ignore: cast_nullable_to_non_nullable
              as String,
      grams: null == grams
          ? _value.grams
          : grams // ignore: cast_nullable_to_non_nullable
              as double,
    ));
  }
}

/// @nodoc

class _$RecipeContributionImpl implements _RecipeContribution {
  const _$RecipeContributionImpl(
      {required this.recipeName, required this.grams});

  @override
  final String recipeName;
  @override
  final double grams;

  @override
  String toString() {
    return 'RecipeContribution(recipeName: $recipeName, grams: $grams)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RecipeContributionImpl &&
            (identical(other.recipeName, recipeName) ||
                other.recipeName == recipeName) &&
            (identical(other.grams, grams) || other.grams == grams));
  }

  @override
  int get hashCode => Object.hash(runtimeType, recipeName, grams);

  /// Create a copy of RecipeContribution
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$RecipeContributionImplCopyWith<_$RecipeContributionImpl> get copyWith =>
      __$$RecipeContributionImplCopyWithImpl<_$RecipeContributionImpl>(
          this, _$identity);
}

abstract class _RecipeContribution implements RecipeContribution {
  const factory _RecipeContribution(
      {required final String recipeName,
      required final double grams}) = _$RecipeContributionImpl;

  @override
  String get recipeName;
  @override
  double get grams;

  /// Create a copy of RecipeContribution
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$RecipeContributionImplCopyWith<_$RecipeContributionImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
