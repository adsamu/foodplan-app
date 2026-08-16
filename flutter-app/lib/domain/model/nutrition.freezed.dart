// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'nutrition.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$RecipeNutrition {
  double get kcal => throw _privateConstructorUsedError;
  double get protein => throw _privateConstructorUsedError;
  double get fat => throw _privateConstructorUsedError;
  double get carbs => throw _privateConstructorUsedError;

  /// Create a copy of RecipeNutrition
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $RecipeNutritionCopyWith<RecipeNutrition> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RecipeNutritionCopyWith<$Res> {
  factory $RecipeNutritionCopyWith(
          RecipeNutrition value, $Res Function(RecipeNutrition) then) =
      _$RecipeNutritionCopyWithImpl<$Res, RecipeNutrition>;
  @useResult
  $Res call({double kcal, double protein, double fat, double carbs});
}

/// @nodoc
class _$RecipeNutritionCopyWithImpl<$Res, $Val extends RecipeNutrition>
    implements $RecipeNutritionCopyWith<$Res> {
  _$RecipeNutritionCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of RecipeNutrition
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? kcal = null,
    Object? protein = null,
    Object? fat = null,
    Object? carbs = null,
  }) {
    return _then(_value.copyWith(
      kcal: null == kcal
          ? _value.kcal
          : kcal // ignore: cast_nullable_to_non_nullable
              as double,
      protein: null == protein
          ? _value.protein
          : protein // ignore: cast_nullable_to_non_nullable
              as double,
      fat: null == fat
          ? _value.fat
          : fat // ignore: cast_nullable_to_non_nullable
              as double,
      carbs: null == carbs
          ? _value.carbs
          : carbs // ignore: cast_nullable_to_non_nullable
              as double,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$RecipeNutritionImplCopyWith<$Res>
    implements $RecipeNutritionCopyWith<$Res> {
  factory _$$RecipeNutritionImplCopyWith(_$RecipeNutritionImpl value,
          $Res Function(_$RecipeNutritionImpl) then) =
      __$$RecipeNutritionImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({double kcal, double protein, double fat, double carbs});
}

/// @nodoc
class __$$RecipeNutritionImplCopyWithImpl<$Res>
    extends _$RecipeNutritionCopyWithImpl<$Res, _$RecipeNutritionImpl>
    implements _$$RecipeNutritionImplCopyWith<$Res> {
  __$$RecipeNutritionImplCopyWithImpl(
      _$RecipeNutritionImpl _value, $Res Function(_$RecipeNutritionImpl) _then)
      : super(_value, _then);

  /// Create a copy of RecipeNutrition
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? kcal = null,
    Object? protein = null,
    Object? fat = null,
    Object? carbs = null,
  }) {
    return _then(_$RecipeNutritionImpl(
      kcal: null == kcal
          ? _value.kcal
          : kcal // ignore: cast_nullable_to_non_nullable
              as double,
      protein: null == protein
          ? _value.protein
          : protein // ignore: cast_nullable_to_non_nullable
              as double,
      fat: null == fat
          ? _value.fat
          : fat // ignore: cast_nullable_to_non_nullable
              as double,
      carbs: null == carbs
          ? _value.carbs
          : carbs // ignore: cast_nullable_to_non_nullable
              as double,
    ));
  }
}

/// @nodoc

class _$RecipeNutritionImpl extends _RecipeNutrition {
  const _$RecipeNutritionImpl(
      {required this.kcal,
      required this.protein,
      required this.fat,
      required this.carbs})
      : super._();

  @override
  final double kcal;
  @override
  final double protein;
  @override
  final double fat;
  @override
  final double carbs;

  @override
  String toString() {
    return 'RecipeNutrition(kcal: $kcal, protein: $protein, fat: $fat, carbs: $carbs)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RecipeNutritionImpl &&
            (identical(other.kcal, kcal) || other.kcal == kcal) &&
            (identical(other.protein, protein) || other.protein == protein) &&
            (identical(other.fat, fat) || other.fat == fat) &&
            (identical(other.carbs, carbs) || other.carbs == carbs));
  }

  @override
  int get hashCode => Object.hash(runtimeType, kcal, protein, fat, carbs);

  /// Create a copy of RecipeNutrition
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$RecipeNutritionImplCopyWith<_$RecipeNutritionImpl> get copyWith =>
      __$$RecipeNutritionImplCopyWithImpl<_$RecipeNutritionImpl>(
          this, _$identity);
}

abstract class _RecipeNutrition extends RecipeNutrition {
  const factory _RecipeNutrition(
      {required final double kcal,
      required final double protein,
      required final double fat,
      required final double carbs}) = _$RecipeNutritionImpl;
  const _RecipeNutrition._() : super._();

  @override
  double get kcal;
  @override
  double get protein;
  @override
  double get fat;
  @override
  double get carbs;

  /// Create a copy of RecipeNutrition
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$RecipeNutritionImplCopyWith<_$RecipeNutritionImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
