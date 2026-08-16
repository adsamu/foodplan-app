// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'meal_plan_config.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$MealPlanConfig {
  MealScheduleConfig get schedule => throw _privateConstructorUsedError;
  NutritionGoals get goals => throw _privateConstructorUsedError;
  DietPreferences get diet => throw _privateConstructorUsedError;
  VarietyConfig get variety => throw _privateConstructorUsedError;
  List<OptimizerRule> get rules => throw _privateConstructorUsedError;
  ShoppingConfig get shopping => throw _privateConstructorUsedError;
  ProteinPowder? get proteinPowder => throw _privateConstructorUsedError;

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $MealPlanConfigCopyWith<MealPlanConfig> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MealPlanConfigCopyWith<$Res> {
  factory $MealPlanConfigCopyWith(
          MealPlanConfig value, $Res Function(MealPlanConfig) then) =
      _$MealPlanConfigCopyWithImpl<$Res, MealPlanConfig>;
  @useResult
  $Res call(
      {MealScheduleConfig schedule,
      NutritionGoals goals,
      DietPreferences diet,
      VarietyConfig variety,
      List<OptimizerRule> rules,
      ShoppingConfig shopping,
      ProteinPowder? proteinPowder});

  $MealScheduleConfigCopyWith<$Res> get schedule;
  $NutritionGoalsCopyWith<$Res> get goals;
  $DietPreferencesCopyWith<$Res> get diet;
  $VarietyConfigCopyWith<$Res> get variety;
  $ShoppingConfigCopyWith<$Res> get shopping;
  $ProteinPowderCopyWith<$Res>? get proteinPowder;
}

/// @nodoc
class _$MealPlanConfigCopyWithImpl<$Res, $Val extends MealPlanConfig>
    implements $MealPlanConfigCopyWith<$Res> {
  _$MealPlanConfigCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? schedule = null,
    Object? goals = null,
    Object? diet = null,
    Object? variety = null,
    Object? rules = null,
    Object? shopping = null,
    Object? proteinPowder = freezed,
  }) {
    return _then(_value.copyWith(
      schedule: null == schedule
          ? _value.schedule
          : schedule // ignore: cast_nullable_to_non_nullable
              as MealScheduleConfig,
      goals: null == goals
          ? _value.goals
          : goals // ignore: cast_nullable_to_non_nullable
              as NutritionGoals,
      diet: null == diet
          ? _value.diet
          : diet // ignore: cast_nullable_to_non_nullable
              as DietPreferences,
      variety: null == variety
          ? _value.variety
          : variety // ignore: cast_nullable_to_non_nullable
              as VarietyConfig,
      rules: null == rules
          ? _value.rules
          : rules // ignore: cast_nullable_to_non_nullable
              as List<OptimizerRule>,
      shopping: null == shopping
          ? _value.shopping
          : shopping // ignore: cast_nullable_to_non_nullable
              as ShoppingConfig,
      proteinPowder: freezed == proteinPowder
          ? _value.proteinPowder
          : proteinPowder // ignore: cast_nullable_to_non_nullable
              as ProteinPowder?,
    ) as $Val);
  }

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $MealScheduleConfigCopyWith<$Res> get schedule {
    return $MealScheduleConfigCopyWith<$Res>(_value.schedule, (value) {
      return _then(_value.copyWith(schedule: value) as $Val);
    });
  }

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $NutritionGoalsCopyWith<$Res> get goals {
    return $NutritionGoalsCopyWith<$Res>(_value.goals, (value) {
      return _then(_value.copyWith(goals: value) as $Val);
    });
  }

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $DietPreferencesCopyWith<$Res> get diet {
    return $DietPreferencesCopyWith<$Res>(_value.diet, (value) {
      return _then(_value.copyWith(diet: value) as $Val);
    });
  }

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $VarietyConfigCopyWith<$Res> get variety {
    return $VarietyConfigCopyWith<$Res>(_value.variety, (value) {
      return _then(_value.copyWith(variety: value) as $Val);
    });
  }

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $ShoppingConfigCopyWith<$Res> get shopping {
    return $ShoppingConfigCopyWith<$Res>(_value.shopping, (value) {
      return _then(_value.copyWith(shopping: value) as $Val);
    });
  }

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $ProteinPowderCopyWith<$Res>? get proteinPowder {
    if (_value.proteinPowder == null) {
      return null;
    }

    return $ProteinPowderCopyWith<$Res>(_value.proteinPowder!, (value) {
      return _then(_value.copyWith(proteinPowder: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$MealPlanConfigImplCopyWith<$Res>
    implements $MealPlanConfigCopyWith<$Res> {
  factory _$$MealPlanConfigImplCopyWith(_$MealPlanConfigImpl value,
          $Res Function(_$MealPlanConfigImpl) then) =
      __$$MealPlanConfigImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {MealScheduleConfig schedule,
      NutritionGoals goals,
      DietPreferences diet,
      VarietyConfig variety,
      List<OptimizerRule> rules,
      ShoppingConfig shopping,
      ProteinPowder? proteinPowder});

  @override
  $MealScheduleConfigCopyWith<$Res> get schedule;
  @override
  $NutritionGoalsCopyWith<$Res> get goals;
  @override
  $DietPreferencesCopyWith<$Res> get diet;
  @override
  $VarietyConfigCopyWith<$Res> get variety;
  @override
  $ShoppingConfigCopyWith<$Res> get shopping;
  @override
  $ProteinPowderCopyWith<$Res>? get proteinPowder;
}

/// @nodoc
class __$$MealPlanConfigImplCopyWithImpl<$Res>
    extends _$MealPlanConfigCopyWithImpl<$Res, _$MealPlanConfigImpl>
    implements _$$MealPlanConfigImplCopyWith<$Res> {
  __$$MealPlanConfigImplCopyWithImpl(
      _$MealPlanConfigImpl _value, $Res Function(_$MealPlanConfigImpl) _then)
      : super(_value, _then);

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? schedule = null,
    Object? goals = null,
    Object? diet = null,
    Object? variety = null,
    Object? rules = null,
    Object? shopping = null,
    Object? proteinPowder = freezed,
  }) {
    return _then(_$MealPlanConfigImpl(
      schedule: null == schedule
          ? _value.schedule
          : schedule // ignore: cast_nullable_to_non_nullable
              as MealScheduleConfig,
      goals: null == goals
          ? _value.goals
          : goals // ignore: cast_nullable_to_non_nullable
              as NutritionGoals,
      diet: null == diet
          ? _value.diet
          : diet // ignore: cast_nullable_to_non_nullable
              as DietPreferences,
      variety: null == variety
          ? _value.variety
          : variety // ignore: cast_nullable_to_non_nullable
              as VarietyConfig,
      rules: null == rules
          ? _value._rules
          : rules // ignore: cast_nullable_to_non_nullable
              as List<OptimizerRule>,
      shopping: null == shopping
          ? _value.shopping
          : shopping // ignore: cast_nullable_to_non_nullable
              as ShoppingConfig,
      proteinPowder: freezed == proteinPowder
          ? _value.proteinPowder
          : proteinPowder // ignore: cast_nullable_to_non_nullable
              as ProteinPowder?,
    ));
  }
}

/// @nodoc

class _$MealPlanConfigImpl implements _MealPlanConfig {
  const _$MealPlanConfigImpl(
      {required this.schedule,
      required this.goals,
      required this.diet,
      required this.variety,
      required final List<OptimizerRule> rules,
      required this.shopping,
      this.proteinPowder})
      : _rules = rules;

  @override
  final MealScheduleConfig schedule;
  @override
  final NutritionGoals goals;
  @override
  final DietPreferences diet;
  @override
  final VarietyConfig variety;
  final List<OptimizerRule> _rules;
  @override
  List<OptimizerRule> get rules {
    if (_rules is EqualUnmodifiableListView) return _rules;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_rules);
  }

  @override
  final ShoppingConfig shopping;
  @override
  final ProteinPowder? proteinPowder;

  @override
  String toString() {
    return 'MealPlanConfig(schedule: $schedule, goals: $goals, diet: $diet, variety: $variety, rules: $rules, shopping: $shopping, proteinPowder: $proteinPowder)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MealPlanConfigImpl &&
            (identical(other.schedule, schedule) ||
                other.schedule == schedule) &&
            (identical(other.goals, goals) || other.goals == goals) &&
            (identical(other.diet, diet) || other.diet == diet) &&
            (identical(other.variety, variety) || other.variety == variety) &&
            const DeepCollectionEquality().equals(other._rules, _rules) &&
            (identical(other.shopping, shopping) ||
                other.shopping == shopping) &&
            (identical(other.proteinPowder, proteinPowder) ||
                other.proteinPowder == proteinPowder));
  }

  @override
  int get hashCode => Object.hash(runtimeType, schedule, goals, diet, variety,
      const DeepCollectionEquality().hash(_rules), shopping, proteinPowder);

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$MealPlanConfigImplCopyWith<_$MealPlanConfigImpl> get copyWith =>
      __$$MealPlanConfigImplCopyWithImpl<_$MealPlanConfigImpl>(
          this, _$identity);
}

abstract class _MealPlanConfig implements MealPlanConfig {
  const factory _MealPlanConfig(
      {required final MealScheduleConfig schedule,
      required final NutritionGoals goals,
      required final DietPreferences diet,
      required final VarietyConfig variety,
      required final List<OptimizerRule> rules,
      required final ShoppingConfig shopping,
      final ProteinPowder? proteinPowder}) = _$MealPlanConfigImpl;

  @override
  MealScheduleConfig get schedule;
  @override
  NutritionGoals get goals;
  @override
  DietPreferences get diet;
  @override
  VarietyConfig get variety;
  @override
  List<OptimizerRule> get rules;
  @override
  ShoppingConfig get shopping;
  @override
  ProteinPowder? get proteinPowder;

  /// Create a copy of MealPlanConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$MealPlanConfigImplCopyWith<_$MealPlanConfigImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$MealScheduleConfig {
  /// Key is ISO weekday: 1=Monday … 7=Sunday
  Map<int, DaySlotConfig> get perDay => throw _privateConstructorUsedError;
  List<BatchCookingGroup> get batchCookingGroups =>
      throw _privateConstructorUsedError;
  bool get snackOptionalFill => throw _privateConstructorUsedError;

  /// Create a copy of MealScheduleConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $MealScheduleConfigCopyWith<MealScheduleConfig> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MealScheduleConfigCopyWith<$Res> {
  factory $MealScheduleConfigCopyWith(
          MealScheduleConfig value, $Res Function(MealScheduleConfig) then) =
      _$MealScheduleConfigCopyWithImpl<$Res, MealScheduleConfig>;
  @useResult
  $Res call(
      {Map<int, DaySlotConfig> perDay,
      List<BatchCookingGroup> batchCookingGroups,
      bool snackOptionalFill});
}

/// @nodoc
class _$MealScheduleConfigCopyWithImpl<$Res, $Val extends MealScheduleConfig>
    implements $MealScheduleConfigCopyWith<$Res> {
  _$MealScheduleConfigCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of MealScheduleConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? perDay = null,
    Object? batchCookingGroups = null,
    Object? snackOptionalFill = null,
  }) {
    return _then(_value.copyWith(
      perDay: null == perDay
          ? _value.perDay
          : perDay // ignore: cast_nullable_to_non_nullable
              as Map<int, DaySlotConfig>,
      batchCookingGroups: null == batchCookingGroups
          ? _value.batchCookingGroups
          : batchCookingGroups // ignore: cast_nullable_to_non_nullable
              as List<BatchCookingGroup>,
      snackOptionalFill: null == snackOptionalFill
          ? _value.snackOptionalFill
          : snackOptionalFill // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$MealScheduleConfigImplCopyWith<$Res>
    implements $MealScheduleConfigCopyWith<$Res> {
  factory _$$MealScheduleConfigImplCopyWith(_$MealScheduleConfigImpl value,
          $Res Function(_$MealScheduleConfigImpl) then) =
      __$$MealScheduleConfigImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {Map<int, DaySlotConfig> perDay,
      List<BatchCookingGroup> batchCookingGroups,
      bool snackOptionalFill});
}

/// @nodoc
class __$$MealScheduleConfigImplCopyWithImpl<$Res>
    extends _$MealScheduleConfigCopyWithImpl<$Res, _$MealScheduleConfigImpl>
    implements _$$MealScheduleConfigImplCopyWith<$Res> {
  __$$MealScheduleConfigImplCopyWithImpl(_$MealScheduleConfigImpl _value,
      $Res Function(_$MealScheduleConfigImpl) _then)
      : super(_value, _then);

  /// Create a copy of MealScheduleConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? perDay = null,
    Object? batchCookingGroups = null,
    Object? snackOptionalFill = null,
  }) {
    return _then(_$MealScheduleConfigImpl(
      perDay: null == perDay
          ? _value._perDay
          : perDay // ignore: cast_nullable_to_non_nullable
              as Map<int, DaySlotConfig>,
      batchCookingGroups: null == batchCookingGroups
          ? _value._batchCookingGroups
          : batchCookingGroups // ignore: cast_nullable_to_non_nullable
              as List<BatchCookingGroup>,
      snackOptionalFill: null == snackOptionalFill
          ? _value.snackOptionalFill
          : snackOptionalFill // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$MealScheduleConfigImpl implements _MealScheduleConfig {
  const _$MealScheduleConfigImpl(
      {required final Map<int, DaySlotConfig> perDay,
      final List<BatchCookingGroup> batchCookingGroups =
          const <BatchCookingGroup>[],
      this.snackOptionalFill = true})
      : _perDay = perDay,
        _batchCookingGroups = batchCookingGroups;

  /// Key is ISO weekday: 1=Monday … 7=Sunday
  final Map<int, DaySlotConfig> _perDay;

  /// Key is ISO weekday: 1=Monday … 7=Sunday
  @override
  Map<int, DaySlotConfig> get perDay {
    if (_perDay is EqualUnmodifiableMapView) return _perDay;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(_perDay);
  }

  final List<BatchCookingGroup> _batchCookingGroups;
  @override
  @JsonKey()
  List<BatchCookingGroup> get batchCookingGroups {
    if (_batchCookingGroups is EqualUnmodifiableListView)
      return _batchCookingGroups;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_batchCookingGroups);
  }

  @override
  @JsonKey()
  final bool snackOptionalFill;

  @override
  String toString() {
    return 'MealScheduleConfig(perDay: $perDay, batchCookingGroups: $batchCookingGroups, snackOptionalFill: $snackOptionalFill)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MealScheduleConfigImpl &&
            const DeepCollectionEquality().equals(other._perDay, _perDay) &&
            const DeepCollectionEquality()
                .equals(other._batchCookingGroups, _batchCookingGroups) &&
            (identical(other.snackOptionalFill, snackOptionalFill) ||
                other.snackOptionalFill == snackOptionalFill));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(_perDay),
      const DeepCollectionEquality().hash(_batchCookingGroups),
      snackOptionalFill);

  /// Create a copy of MealScheduleConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$MealScheduleConfigImplCopyWith<_$MealScheduleConfigImpl> get copyWith =>
      __$$MealScheduleConfigImplCopyWithImpl<_$MealScheduleConfigImpl>(
          this, _$identity);
}

abstract class _MealScheduleConfig implements MealScheduleConfig {
  const factory _MealScheduleConfig(
      {required final Map<int, DaySlotConfig> perDay,
      final List<BatchCookingGroup> batchCookingGroups,
      final bool snackOptionalFill}) = _$MealScheduleConfigImpl;

  /// Key is ISO weekday: 1=Monday … 7=Sunday
  @override
  Map<int, DaySlotConfig> get perDay;
  @override
  List<BatchCookingGroup> get batchCookingGroups;
  @override
  bool get snackOptionalFill;

  /// Create a copy of MealScheduleConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$MealScheduleConfigImplCopyWith<_$MealScheduleConfigImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$DaySlotConfig {
  bool get hasBreakfast => throw _privateConstructorUsedError;
  bool get hasLunch => throw _privateConstructorUsedError;
  bool get hasDinner => throw _privateConstructorUsedError;
  int get snackCount => throw _privateConstructorUsedError;

  /// Create a copy of DaySlotConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DaySlotConfigCopyWith<DaySlotConfig> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DaySlotConfigCopyWith<$Res> {
  factory $DaySlotConfigCopyWith(
          DaySlotConfig value, $Res Function(DaySlotConfig) then) =
      _$DaySlotConfigCopyWithImpl<$Res, DaySlotConfig>;
  @useResult
  $Res call({bool hasBreakfast, bool hasLunch, bool hasDinner, int snackCount});
}

/// @nodoc
class _$DaySlotConfigCopyWithImpl<$Res, $Val extends DaySlotConfig>
    implements $DaySlotConfigCopyWith<$Res> {
  _$DaySlotConfigCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DaySlotConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? hasBreakfast = null,
    Object? hasLunch = null,
    Object? hasDinner = null,
    Object? snackCount = null,
  }) {
    return _then(_value.copyWith(
      hasBreakfast: null == hasBreakfast
          ? _value.hasBreakfast
          : hasBreakfast // ignore: cast_nullable_to_non_nullable
              as bool,
      hasLunch: null == hasLunch
          ? _value.hasLunch
          : hasLunch // ignore: cast_nullable_to_non_nullable
              as bool,
      hasDinner: null == hasDinner
          ? _value.hasDinner
          : hasDinner // ignore: cast_nullable_to_non_nullable
              as bool,
      snackCount: null == snackCount
          ? _value.snackCount
          : snackCount // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$DaySlotConfigImplCopyWith<$Res>
    implements $DaySlotConfigCopyWith<$Res> {
  factory _$$DaySlotConfigImplCopyWith(
          _$DaySlotConfigImpl value, $Res Function(_$DaySlotConfigImpl) then) =
      __$$DaySlotConfigImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({bool hasBreakfast, bool hasLunch, bool hasDinner, int snackCount});
}

/// @nodoc
class __$$DaySlotConfigImplCopyWithImpl<$Res>
    extends _$DaySlotConfigCopyWithImpl<$Res, _$DaySlotConfigImpl>
    implements _$$DaySlotConfigImplCopyWith<$Res> {
  __$$DaySlotConfigImplCopyWithImpl(
      _$DaySlotConfigImpl _value, $Res Function(_$DaySlotConfigImpl) _then)
      : super(_value, _then);

  /// Create a copy of DaySlotConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? hasBreakfast = null,
    Object? hasLunch = null,
    Object? hasDinner = null,
    Object? snackCount = null,
  }) {
    return _then(_$DaySlotConfigImpl(
      hasBreakfast: null == hasBreakfast
          ? _value.hasBreakfast
          : hasBreakfast // ignore: cast_nullable_to_non_nullable
              as bool,
      hasLunch: null == hasLunch
          ? _value.hasLunch
          : hasLunch // ignore: cast_nullable_to_non_nullable
              as bool,
      hasDinner: null == hasDinner
          ? _value.hasDinner
          : hasDinner // ignore: cast_nullable_to_non_nullable
              as bool,
      snackCount: null == snackCount
          ? _value.snackCount
          : snackCount // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$DaySlotConfigImpl implements _DaySlotConfig {
  const _$DaySlotConfigImpl(
      {this.hasBreakfast = false,
      this.hasLunch = true,
      this.hasDinner = true,
      this.snackCount = 0});

  @override
  @JsonKey()
  final bool hasBreakfast;
  @override
  @JsonKey()
  final bool hasLunch;
  @override
  @JsonKey()
  final bool hasDinner;
  @override
  @JsonKey()
  final int snackCount;

  @override
  String toString() {
    return 'DaySlotConfig(hasBreakfast: $hasBreakfast, hasLunch: $hasLunch, hasDinner: $hasDinner, snackCount: $snackCount)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DaySlotConfigImpl &&
            (identical(other.hasBreakfast, hasBreakfast) ||
                other.hasBreakfast == hasBreakfast) &&
            (identical(other.hasLunch, hasLunch) ||
                other.hasLunch == hasLunch) &&
            (identical(other.hasDinner, hasDinner) ||
                other.hasDinner == hasDinner) &&
            (identical(other.snackCount, snackCount) ||
                other.snackCount == snackCount));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, hasBreakfast, hasLunch, hasDinner, snackCount);

  /// Create a copy of DaySlotConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DaySlotConfigImplCopyWith<_$DaySlotConfigImpl> get copyWith =>
      __$$DaySlotConfigImplCopyWithImpl<_$DaySlotConfigImpl>(this, _$identity);
}

abstract class _DaySlotConfig implements DaySlotConfig {
  const factory _DaySlotConfig(
      {final bool hasBreakfast,
      final bool hasLunch,
      final bool hasDinner,
      final int snackCount}) = _$DaySlotConfigImpl;

  @override
  bool get hasBreakfast;
  @override
  bool get hasLunch;
  @override
  bool get hasDinner;
  @override
  int get snackCount;

  /// Create a copy of DaySlotConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DaySlotConfigImplCopyWith<_$DaySlotConfigImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$BatchCookingGroup {
  MealCategory get category => throw _privateConstructorUsedError;

  /// ISO day numbers: 1=Monday … 7=Sunday
  Set<int> get days => throw _privateConstructorUsedError;
  int get batchNumber => throw _privateConstructorUsedError;

  /// Create a copy of BatchCookingGroup
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BatchCookingGroupCopyWith<BatchCookingGroup> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BatchCookingGroupCopyWith<$Res> {
  factory $BatchCookingGroupCopyWith(
          BatchCookingGroup value, $Res Function(BatchCookingGroup) then) =
      _$BatchCookingGroupCopyWithImpl<$Res, BatchCookingGroup>;
  @useResult
  $Res call({MealCategory category, Set<int> days, int batchNumber});
}

/// @nodoc
class _$BatchCookingGroupCopyWithImpl<$Res, $Val extends BatchCookingGroup>
    implements $BatchCookingGroupCopyWith<$Res> {
  _$BatchCookingGroupCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BatchCookingGroup
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? category = null,
    Object? days = null,
    Object? batchNumber = null,
  }) {
    return _then(_value.copyWith(
      category: null == category
          ? _value.category
          : category // ignore: cast_nullable_to_non_nullable
              as MealCategory,
      days: null == days
          ? _value.days
          : days // ignore: cast_nullable_to_non_nullable
              as Set<int>,
      batchNumber: null == batchNumber
          ? _value.batchNumber
          : batchNumber // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BatchCookingGroupImplCopyWith<$Res>
    implements $BatchCookingGroupCopyWith<$Res> {
  factory _$$BatchCookingGroupImplCopyWith(_$BatchCookingGroupImpl value,
          $Res Function(_$BatchCookingGroupImpl) then) =
      __$$BatchCookingGroupImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({MealCategory category, Set<int> days, int batchNumber});
}

/// @nodoc
class __$$BatchCookingGroupImplCopyWithImpl<$Res>
    extends _$BatchCookingGroupCopyWithImpl<$Res, _$BatchCookingGroupImpl>
    implements _$$BatchCookingGroupImplCopyWith<$Res> {
  __$$BatchCookingGroupImplCopyWithImpl(_$BatchCookingGroupImpl _value,
      $Res Function(_$BatchCookingGroupImpl) _then)
      : super(_value, _then);

  /// Create a copy of BatchCookingGroup
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? category = null,
    Object? days = null,
    Object? batchNumber = null,
  }) {
    return _then(_$BatchCookingGroupImpl(
      category: null == category
          ? _value.category
          : category // ignore: cast_nullable_to_non_nullable
              as MealCategory,
      days: null == days
          ? _value._days
          : days // ignore: cast_nullable_to_non_nullable
              as Set<int>,
      batchNumber: null == batchNumber
          ? _value.batchNumber
          : batchNumber // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$BatchCookingGroupImpl implements _BatchCookingGroup {
  const _$BatchCookingGroupImpl(
      {required this.category,
      required final Set<int> days,
      required this.batchNumber})
      : _days = days;

  @override
  final MealCategory category;

  /// ISO day numbers: 1=Monday … 7=Sunday
  final Set<int> _days;

  /// ISO day numbers: 1=Monday … 7=Sunday
  @override
  Set<int> get days {
    if (_days is EqualUnmodifiableSetView) return _days;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_days);
  }

  @override
  final int batchNumber;

  @override
  String toString() {
    return 'BatchCookingGroup(category: $category, days: $days, batchNumber: $batchNumber)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BatchCookingGroupImpl &&
            (identical(other.category, category) ||
                other.category == category) &&
            const DeepCollectionEquality().equals(other._days, _days) &&
            (identical(other.batchNumber, batchNumber) ||
                other.batchNumber == batchNumber));
  }

  @override
  int get hashCode => Object.hash(runtimeType, category,
      const DeepCollectionEquality().hash(_days), batchNumber);

  /// Create a copy of BatchCookingGroup
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BatchCookingGroupImplCopyWith<_$BatchCookingGroupImpl> get copyWith =>
      __$$BatchCookingGroupImplCopyWithImpl<_$BatchCookingGroupImpl>(
          this, _$identity);
}

abstract class _BatchCookingGroup implements BatchCookingGroup {
  const factory _BatchCookingGroup(
      {required final MealCategory category,
      required final Set<int> days,
      required final int batchNumber}) = _$BatchCookingGroupImpl;

  @override
  MealCategory get category;

  /// ISO day numbers: 1=Monday … 7=Sunday
  @override
  Set<int> get days;
  @override
  int get batchNumber;

  /// Create a copy of BatchCookingGroup
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BatchCookingGroupImplCopyWith<_$BatchCookingGroupImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$NutritionGoals {
  double get kcalTarget => throw _privateConstructorUsedError;
  double? get proteinTarget => throw _privateConstructorUsedError;
  double? get fatTarget => throw _privateConstructorUsedError;
  double? get carbsTarget => throw _privateConstructorUsedError;
  MacroField get autoField => throw _privateConstructorUsedError;
  double? get minKcalPerDay => throw _privateConstructorUsedError;
  double? get maxKcalPerDay => throw _privateConstructorUsedError;
  double? get minProteinPerDay => throw _privateConstructorUsedError;
  double? get maxProteinPerDay => throw _privateConstructorUsedError;
  double? get minFatPerDay => throw _privateConstructorUsedError;
  double? get maxFatPerDay => throw _privateConstructorUsedError;
  double? get minCarbsPerDay => throw _privateConstructorUsedError;
  double? get maxCarbsPerDay => throw _privateConstructorUsedError;

  /// Create a copy of NutritionGoals
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $NutritionGoalsCopyWith<NutritionGoals> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $NutritionGoalsCopyWith<$Res> {
  factory $NutritionGoalsCopyWith(
          NutritionGoals value, $Res Function(NutritionGoals) then) =
      _$NutritionGoalsCopyWithImpl<$Res, NutritionGoals>;
  @useResult
  $Res call(
      {double kcalTarget,
      double? proteinTarget,
      double? fatTarget,
      double? carbsTarget,
      MacroField autoField,
      double? minKcalPerDay,
      double? maxKcalPerDay,
      double? minProteinPerDay,
      double? maxProteinPerDay,
      double? minFatPerDay,
      double? maxFatPerDay,
      double? minCarbsPerDay,
      double? maxCarbsPerDay});
}

/// @nodoc
class _$NutritionGoalsCopyWithImpl<$Res, $Val extends NutritionGoals>
    implements $NutritionGoalsCopyWith<$Res> {
  _$NutritionGoalsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of NutritionGoals
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? kcalTarget = null,
    Object? proteinTarget = freezed,
    Object? fatTarget = freezed,
    Object? carbsTarget = freezed,
    Object? autoField = null,
    Object? minKcalPerDay = freezed,
    Object? maxKcalPerDay = freezed,
    Object? minProteinPerDay = freezed,
    Object? maxProteinPerDay = freezed,
    Object? minFatPerDay = freezed,
    Object? maxFatPerDay = freezed,
    Object? minCarbsPerDay = freezed,
    Object? maxCarbsPerDay = freezed,
  }) {
    return _then(_value.copyWith(
      kcalTarget: null == kcalTarget
          ? _value.kcalTarget
          : kcalTarget // ignore: cast_nullable_to_non_nullable
              as double,
      proteinTarget: freezed == proteinTarget
          ? _value.proteinTarget
          : proteinTarget // ignore: cast_nullable_to_non_nullable
              as double?,
      fatTarget: freezed == fatTarget
          ? _value.fatTarget
          : fatTarget // ignore: cast_nullable_to_non_nullable
              as double?,
      carbsTarget: freezed == carbsTarget
          ? _value.carbsTarget
          : carbsTarget // ignore: cast_nullable_to_non_nullable
              as double?,
      autoField: null == autoField
          ? _value.autoField
          : autoField // ignore: cast_nullable_to_non_nullable
              as MacroField,
      minKcalPerDay: freezed == minKcalPerDay
          ? _value.minKcalPerDay
          : minKcalPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      maxKcalPerDay: freezed == maxKcalPerDay
          ? _value.maxKcalPerDay
          : maxKcalPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      minProteinPerDay: freezed == minProteinPerDay
          ? _value.minProteinPerDay
          : minProteinPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      maxProteinPerDay: freezed == maxProteinPerDay
          ? _value.maxProteinPerDay
          : maxProteinPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      minFatPerDay: freezed == minFatPerDay
          ? _value.minFatPerDay
          : minFatPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      maxFatPerDay: freezed == maxFatPerDay
          ? _value.maxFatPerDay
          : maxFatPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      minCarbsPerDay: freezed == minCarbsPerDay
          ? _value.minCarbsPerDay
          : minCarbsPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      maxCarbsPerDay: freezed == maxCarbsPerDay
          ? _value.maxCarbsPerDay
          : maxCarbsPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$NutritionGoalsImplCopyWith<$Res>
    implements $NutritionGoalsCopyWith<$Res> {
  factory _$$NutritionGoalsImplCopyWith(_$NutritionGoalsImpl value,
          $Res Function(_$NutritionGoalsImpl) then) =
      __$$NutritionGoalsImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {double kcalTarget,
      double? proteinTarget,
      double? fatTarget,
      double? carbsTarget,
      MacroField autoField,
      double? minKcalPerDay,
      double? maxKcalPerDay,
      double? minProteinPerDay,
      double? maxProteinPerDay,
      double? minFatPerDay,
      double? maxFatPerDay,
      double? minCarbsPerDay,
      double? maxCarbsPerDay});
}

/// @nodoc
class __$$NutritionGoalsImplCopyWithImpl<$Res>
    extends _$NutritionGoalsCopyWithImpl<$Res, _$NutritionGoalsImpl>
    implements _$$NutritionGoalsImplCopyWith<$Res> {
  __$$NutritionGoalsImplCopyWithImpl(
      _$NutritionGoalsImpl _value, $Res Function(_$NutritionGoalsImpl) _then)
      : super(_value, _then);

  /// Create a copy of NutritionGoals
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? kcalTarget = null,
    Object? proteinTarget = freezed,
    Object? fatTarget = freezed,
    Object? carbsTarget = freezed,
    Object? autoField = null,
    Object? minKcalPerDay = freezed,
    Object? maxKcalPerDay = freezed,
    Object? minProteinPerDay = freezed,
    Object? maxProteinPerDay = freezed,
    Object? minFatPerDay = freezed,
    Object? maxFatPerDay = freezed,
    Object? minCarbsPerDay = freezed,
    Object? maxCarbsPerDay = freezed,
  }) {
    return _then(_$NutritionGoalsImpl(
      kcalTarget: null == kcalTarget
          ? _value.kcalTarget
          : kcalTarget // ignore: cast_nullable_to_non_nullable
              as double,
      proteinTarget: freezed == proteinTarget
          ? _value.proteinTarget
          : proteinTarget // ignore: cast_nullable_to_non_nullable
              as double?,
      fatTarget: freezed == fatTarget
          ? _value.fatTarget
          : fatTarget // ignore: cast_nullable_to_non_nullable
              as double?,
      carbsTarget: freezed == carbsTarget
          ? _value.carbsTarget
          : carbsTarget // ignore: cast_nullable_to_non_nullable
              as double?,
      autoField: null == autoField
          ? _value.autoField
          : autoField // ignore: cast_nullable_to_non_nullable
              as MacroField,
      minKcalPerDay: freezed == minKcalPerDay
          ? _value.minKcalPerDay
          : minKcalPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      maxKcalPerDay: freezed == maxKcalPerDay
          ? _value.maxKcalPerDay
          : maxKcalPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      minProteinPerDay: freezed == minProteinPerDay
          ? _value.minProteinPerDay
          : minProteinPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      maxProteinPerDay: freezed == maxProteinPerDay
          ? _value.maxProteinPerDay
          : maxProteinPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      minFatPerDay: freezed == minFatPerDay
          ? _value.minFatPerDay
          : minFatPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      maxFatPerDay: freezed == maxFatPerDay
          ? _value.maxFatPerDay
          : maxFatPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      minCarbsPerDay: freezed == minCarbsPerDay
          ? _value.minCarbsPerDay
          : minCarbsPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
      maxCarbsPerDay: freezed == maxCarbsPerDay
          ? _value.maxCarbsPerDay
          : maxCarbsPerDay // ignore: cast_nullable_to_non_nullable
              as double?,
    ));
  }
}

/// @nodoc

class _$NutritionGoalsImpl extends _NutritionGoals {
  const _$NutritionGoalsImpl(
      {required this.kcalTarget,
      this.proteinTarget,
      this.fatTarget,
      this.carbsTarget,
      this.autoField = MacroField.protein,
      this.minKcalPerDay,
      this.maxKcalPerDay,
      this.minProteinPerDay,
      this.maxProteinPerDay,
      this.minFatPerDay,
      this.maxFatPerDay,
      this.minCarbsPerDay,
      this.maxCarbsPerDay})
      : super._();

  @override
  final double kcalTarget;
  @override
  final double? proteinTarget;
  @override
  final double? fatTarget;
  @override
  final double? carbsTarget;
  @override
  @JsonKey()
  final MacroField autoField;
  @override
  final double? minKcalPerDay;
  @override
  final double? maxKcalPerDay;
  @override
  final double? minProteinPerDay;
  @override
  final double? maxProteinPerDay;
  @override
  final double? minFatPerDay;
  @override
  final double? maxFatPerDay;
  @override
  final double? minCarbsPerDay;
  @override
  final double? maxCarbsPerDay;

  @override
  String toString() {
    return 'NutritionGoals(kcalTarget: $kcalTarget, proteinTarget: $proteinTarget, fatTarget: $fatTarget, carbsTarget: $carbsTarget, autoField: $autoField, minKcalPerDay: $minKcalPerDay, maxKcalPerDay: $maxKcalPerDay, minProteinPerDay: $minProteinPerDay, maxProteinPerDay: $maxProteinPerDay, minFatPerDay: $minFatPerDay, maxFatPerDay: $maxFatPerDay, minCarbsPerDay: $minCarbsPerDay, maxCarbsPerDay: $maxCarbsPerDay)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$NutritionGoalsImpl &&
            (identical(other.kcalTarget, kcalTarget) ||
                other.kcalTarget == kcalTarget) &&
            (identical(other.proteinTarget, proteinTarget) ||
                other.proteinTarget == proteinTarget) &&
            (identical(other.fatTarget, fatTarget) ||
                other.fatTarget == fatTarget) &&
            (identical(other.carbsTarget, carbsTarget) ||
                other.carbsTarget == carbsTarget) &&
            (identical(other.autoField, autoField) ||
                other.autoField == autoField) &&
            (identical(other.minKcalPerDay, minKcalPerDay) ||
                other.minKcalPerDay == minKcalPerDay) &&
            (identical(other.maxKcalPerDay, maxKcalPerDay) ||
                other.maxKcalPerDay == maxKcalPerDay) &&
            (identical(other.minProteinPerDay, minProteinPerDay) ||
                other.minProteinPerDay == minProteinPerDay) &&
            (identical(other.maxProteinPerDay, maxProteinPerDay) ||
                other.maxProteinPerDay == maxProteinPerDay) &&
            (identical(other.minFatPerDay, minFatPerDay) ||
                other.minFatPerDay == minFatPerDay) &&
            (identical(other.maxFatPerDay, maxFatPerDay) ||
                other.maxFatPerDay == maxFatPerDay) &&
            (identical(other.minCarbsPerDay, minCarbsPerDay) ||
                other.minCarbsPerDay == minCarbsPerDay) &&
            (identical(other.maxCarbsPerDay, maxCarbsPerDay) ||
                other.maxCarbsPerDay == maxCarbsPerDay));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      kcalTarget,
      proteinTarget,
      fatTarget,
      carbsTarget,
      autoField,
      minKcalPerDay,
      maxKcalPerDay,
      minProteinPerDay,
      maxProteinPerDay,
      minFatPerDay,
      maxFatPerDay,
      minCarbsPerDay,
      maxCarbsPerDay);

  /// Create a copy of NutritionGoals
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$NutritionGoalsImplCopyWith<_$NutritionGoalsImpl> get copyWith =>
      __$$NutritionGoalsImplCopyWithImpl<_$NutritionGoalsImpl>(
          this, _$identity);
}

abstract class _NutritionGoals extends NutritionGoals {
  const factory _NutritionGoals(
      {required final double kcalTarget,
      final double? proteinTarget,
      final double? fatTarget,
      final double? carbsTarget,
      final MacroField autoField,
      final double? minKcalPerDay,
      final double? maxKcalPerDay,
      final double? minProteinPerDay,
      final double? maxProteinPerDay,
      final double? minFatPerDay,
      final double? maxFatPerDay,
      final double? minCarbsPerDay,
      final double? maxCarbsPerDay}) = _$NutritionGoalsImpl;
  const _NutritionGoals._() : super._();

  @override
  double get kcalTarget;
  @override
  double? get proteinTarget;
  @override
  double? get fatTarget;
  @override
  double? get carbsTarget;
  @override
  MacroField get autoField;
  @override
  double? get minKcalPerDay;
  @override
  double? get maxKcalPerDay;
  @override
  double? get minProteinPerDay;
  @override
  double? get maxProteinPerDay;
  @override
  double? get minFatPerDay;
  @override
  double? get maxFatPerDay;
  @override
  double? get minCarbsPerDay;
  @override
  double? get maxCarbsPerDay;

  /// Create a copy of NutritionGoals
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$NutritionGoalsImplCopyWith<_$NutritionGoalsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$DietPreferences {
  Set<DietType> get dietTypes => throw _privateConstructorUsedError;
  Set<AllergyType> get allergies => throw _privateConstructorUsedError;
  Set<String> get excludedIngredientIds => throw _privateConstructorUsedError;
  Set<String> get preferredIngredientIds => throw _privateConstructorUsedError;
  Set<String> get dislikedIngredientIds => throw _privateConstructorUsedError;

  /// Create a copy of DietPreferences
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DietPreferencesCopyWith<DietPreferences> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DietPreferencesCopyWith<$Res> {
  factory $DietPreferencesCopyWith(
          DietPreferences value, $Res Function(DietPreferences) then) =
      _$DietPreferencesCopyWithImpl<$Res, DietPreferences>;
  @useResult
  $Res call(
      {Set<DietType> dietTypes,
      Set<AllergyType> allergies,
      Set<String> excludedIngredientIds,
      Set<String> preferredIngredientIds,
      Set<String> dislikedIngredientIds});
}

/// @nodoc
class _$DietPreferencesCopyWithImpl<$Res, $Val extends DietPreferences>
    implements $DietPreferencesCopyWith<$Res> {
  _$DietPreferencesCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DietPreferences
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? dietTypes = null,
    Object? allergies = null,
    Object? excludedIngredientIds = null,
    Object? preferredIngredientIds = null,
    Object? dislikedIngredientIds = null,
  }) {
    return _then(_value.copyWith(
      dietTypes: null == dietTypes
          ? _value.dietTypes
          : dietTypes // ignore: cast_nullable_to_non_nullable
              as Set<DietType>,
      allergies: null == allergies
          ? _value.allergies
          : allergies // ignore: cast_nullable_to_non_nullable
              as Set<AllergyType>,
      excludedIngredientIds: null == excludedIngredientIds
          ? _value.excludedIngredientIds
          : excludedIngredientIds // ignore: cast_nullable_to_non_nullable
              as Set<String>,
      preferredIngredientIds: null == preferredIngredientIds
          ? _value.preferredIngredientIds
          : preferredIngredientIds // ignore: cast_nullable_to_non_nullable
              as Set<String>,
      dislikedIngredientIds: null == dislikedIngredientIds
          ? _value.dislikedIngredientIds
          : dislikedIngredientIds // ignore: cast_nullable_to_non_nullable
              as Set<String>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$DietPreferencesImplCopyWith<$Res>
    implements $DietPreferencesCopyWith<$Res> {
  factory _$$DietPreferencesImplCopyWith(_$DietPreferencesImpl value,
          $Res Function(_$DietPreferencesImpl) then) =
      __$$DietPreferencesImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {Set<DietType> dietTypes,
      Set<AllergyType> allergies,
      Set<String> excludedIngredientIds,
      Set<String> preferredIngredientIds,
      Set<String> dislikedIngredientIds});
}

/// @nodoc
class __$$DietPreferencesImplCopyWithImpl<$Res>
    extends _$DietPreferencesCopyWithImpl<$Res, _$DietPreferencesImpl>
    implements _$$DietPreferencesImplCopyWith<$Res> {
  __$$DietPreferencesImplCopyWithImpl(
      _$DietPreferencesImpl _value, $Res Function(_$DietPreferencesImpl) _then)
      : super(_value, _then);

  /// Create a copy of DietPreferences
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? dietTypes = null,
    Object? allergies = null,
    Object? excludedIngredientIds = null,
    Object? preferredIngredientIds = null,
    Object? dislikedIngredientIds = null,
  }) {
    return _then(_$DietPreferencesImpl(
      dietTypes: null == dietTypes
          ? _value._dietTypes
          : dietTypes // ignore: cast_nullable_to_non_nullable
              as Set<DietType>,
      allergies: null == allergies
          ? _value._allergies
          : allergies // ignore: cast_nullable_to_non_nullable
              as Set<AllergyType>,
      excludedIngredientIds: null == excludedIngredientIds
          ? _value._excludedIngredientIds
          : excludedIngredientIds // ignore: cast_nullable_to_non_nullable
              as Set<String>,
      preferredIngredientIds: null == preferredIngredientIds
          ? _value._preferredIngredientIds
          : preferredIngredientIds // ignore: cast_nullable_to_non_nullable
              as Set<String>,
      dislikedIngredientIds: null == dislikedIngredientIds
          ? _value._dislikedIngredientIds
          : dislikedIngredientIds // ignore: cast_nullable_to_non_nullable
              as Set<String>,
    ));
  }
}

/// @nodoc

class _$DietPreferencesImpl implements _DietPreferences {
  const _$DietPreferencesImpl(
      {final Set<DietType> dietTypes = const <DietType>{},
      final Set<AllergyType> allergies = const <AllergyType>{},
      final Set<String> excludedIngredientIds = const <String>{},
      final Set<String> preferredIngredientIds = const <String>{},
      final Set<String> dislikedIngredientIds = const <String>{}})
      : _dietTypes = dietTypes,
        _allergies = allergies,
        _excludedIngredientIds = excludedIngredientIds,
        _preferredIngredientIds = preferredIngredientIds,
        _dislikedIngredientIds = dislikedIngredientIds;

  final Set<DietType> _dietTypes;
  @override
  @JsonKey()
  Set<DietType> get dietTypes {
    if (_dietTypes is EqualUnmodifiableSetView) return _dietTypes;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_dietTypes);
  }

  final Set<AllergyType> _allergies;
  @override
  @JsonKey()
  Set<AllergyType> get allergies {
    if (_allergies is EqualUnmodifiableSetView) return _allergies;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_allergies);
  }

  final Set<String> _excludedIngredientIds;
  @override
  @JsonKey()
  Set<String> get excludedIngredientIds {
    if (_excludedIngredientIds is EqualUnmodifiableSetView)
      return _excludedIngredientIds;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_excludedIngredientIds);
  }

  final Set<String> _preferredIngredientIds;
  @override
  @JsonKey()
  Set<String> get preferredIngredientIds {
    if (_preferredIngredientIds is EqualUnmodifiableSetView)
      return _preferredIngredientIds;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_preferredIngredientIds);
  }

  final Set<String> _dislikedIngredientIds;
  @override
  @JsonKey()
  Set<String> get dislikedIngredientIds {
    if (_dislikedIngredientIds is EqualUnmodifiableSetView)
      return _dislikedIngredientIds;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_dislikedIngredientIds);
  }

  @override
  String toString() {
    return 'DietPreferences(dietTypes: $dietTypes, allergies: $allergies, excludedIngredientIds: $excludedIngredientIds, preferredIngredientIds: $preferredIngredientIds, dislikedIngredientIds: $dislikedIngredientIds)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DietPreferencesImpl &&
            const DeepCollectionEquality()
                .equals(other._dietTypes, _dietTypes) &&
            const DeepCollectionEquality()
                .equals(other._allergies, _allergies) &&
            const DeepCollectionEquality()
                .equals(other._excludedIngredientIds, _excludedIngredientIds) &&
            const DeepCollectionEquality().equals(
                other._preferredIngredientIds, _preferredIngredientIds) &&
            const DeepCollectionEquality()
                .equals(other._dislikedIngredientIds, _dislikedIngredientIds));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(_dietTypes),
      const DeepCollectionEquality().hash(_allergies),
      const DeepCollectionEquality().hash(_excludedIngredientIds),
      const DeepCollectionEquality().hash(_preferredIngredientIds),
      const DeepCollectionEquality().hash(_dislikedIngredientIds));

  /// Create a copy of DietPreferences
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DietPreferencesImplCopyWith<_$DietPreferencesImpl> get copyWith =>
      __$$DietPreferencesImplCopyWithImpl<_$DietPreferencesImpl>(
          this, _$identity);
}

abstract class _DietPreferences implements DietPreferences {
  const factory _DietPreferences(
      {final Set<DietType> dietTypes,
      final Set<AllergyType> allergies,
      final Set<String> excludedIngredientIds,
      final Set<String> preferredIngredientIds,
      final Set<String> dislikedIngredientIds}) = _$DietPreferencesImpl;

  @override
  Set<DietType> get dietTypes;
  @override
  Set<AllergyType> get allergies;
  @override
  Set<String> get excludedIngredientIds;
  @override
  Set<String> get preferredIngredientIds;
  @override
  Set<String> get dislikedIngredientIds;

  /// Create a copy of DietPreferences
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DietPreferencesImplCopyWith<_$DietPreferencesImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$MealCategoryVariety {
  int? get maxTimesPerWeek => throw _privateConstructorUsedError;
  int? get maxConsecutiveDays => throw _privateConstructorUsedError;

  /// Create a copy of MealCategoryVariety
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $MealCategoryVarietyCopyWith<MealCategoryVariety> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MealCategoryVarietyCopyWith<$Res> {
  factory $MealCategoryVarietyCopyWith(
          MealCategoryVariety value, $Res Function(MealCategoryVariety) then) =
      _$MealCategoryVarietyCopyWithImpl<$Res, MealCategoryVariety>;
  @useResult
  $Res call({int? maxTimesPerWeek, int? maxConsecutiveDays});
}

/// @nodoc
class _$MealCategoryVarietyCopyWithImpl<$Res, $Val extends MealCategoryVariety>
    implements $MealCategoryVarietyCopyWith<$Res> {
  _$MealCategoryVarietyCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of MealCategoryVariety
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? maxTimesPerWeek = freezed,
    Object? maxConsecutiveDays = freezed,
  }) {
    return _then(_value.copyWith(
      maxTimesPerWeek: freezed == maxTimesPerWeek
          ? _value.maxTimesPerWeek
          : maxTimesPerWeek // ignore: cast_nullable_to_non_nullable
              as int?,
      maxConsecutiveDays: freezed == maxConsecutiveDays
          ? _value.maxConsecutiveDays
          : maxConsecutiveDays // ignore: cast_nullable_to_non_nullable
              as int?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$MealCategoryVarietyImplCopyWith<$Res>
    implements $MealCategoryVarietyCopyWith<$Res> {
  factory _$$MealCategoryVarietyImplCopyWith(_$MealCategoryVarietyImpl value,
          $Res Function(_$MealCategoryVarietyImpl) then) =
      __$$MealCategoryVarietyImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int? maxTimesPerWeek, int? maxConsecutiveDays});
}

/// @nodoc
class __$$MealCategoryVarietyImplCopyWithImpl<$Res>
    extends _$MealCategoryVarietyCopyWithImpl<$Res, _$MealCategoryVarietyImpl>
    implements _$$MealCategoryVarietyImplCopyWith<$Res> {
  __$$MealCategoryVarietyImplCopyWithImpl(_$MealCategoryVarietyImpl _value,
      $Res Function(_$MealCategoryVarietyImpl) _then)
      : super(_value, _then);

  /// Create a copy of MealCategoryVariety
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? maxTimesPerWeek = freezed,
    Object? maxConsecutiveDays = freezed,
  }) {
    return _then(_$MealCategoryVarietyImpl(
      maxTimesPerWeek: freezed == maxTimesPerWeek
          ? _value.maxTimesPerWeek
          : maxTimesPerWeek // ignore: cast_nullable_to_non_nullable
              as int?,
      maxConsecutiveDays: freezed == maxConsecutiveDays
          ? _value.maxConsecutiveDays
          : maxConsecutiveDays // ignore: cast_nullable_to_non_nullable
              as int?,
    ));
  }
}

/// @nodoc

class _$MealCategoryVarietyImpl implements _MealCategoryVariety {
  const _$MealCategoryVarietyImpl(
      {this.maxTimesPerWeek, this.maxConsecutiveDays});

  @override
  final int? maxTimesPerWeek;
  @override
  final int? maxConsecutiveDays;

  @override
  String toString() {
    return 'MealCategoryVariety(maxTimesPerWeek: $maxTimesPerWeek, maxConsecutiveDays: $maxConsecutiveDays)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MealCategoryVarietyImpl &&
            (identical(other.maxTimesPerWeek, maxTimesPerWeek) ||
                other.maxTimesPerWeek == maxTimesPerWeek) &&
            (identical(other.maxConsecutiveDays, maxConsecutiveDays) ||
                other.maxConsecutiveDays == maxConsecutiveDays));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, maxTimesPerWeek, maxConsecutiveDays);

  /// Create a copy of MealCategoryVariety
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$MealCategoryVarietyImplCopyWith<_$MealCategoryVarietyImpl> get copyWith =>
      __$$MealCategoryVarietyImplCopyWithImpl<_$MealCategoryVarietyImpl>(
          this, _$identity);
}

abstract class _MealCategoryVariety implements MealCategoryVariety {
  const factory _MealCategoryVariety(
      {final int? maxTimesPerWeek,
      final int? maxConsecutiveDays}) = _$MealCategoryVarietyImpl;

  @override
  int? get maxTimesPerWeek;
  @override
  int? get maxConsecutiveDays;

  /// Create a copy of MealCategoryVariety
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$MealCategoryVarietyImplCopyWith<_$MealCategoryVarietyImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$VarietyConfig {
  VarietyLevel get level => throw _privateConstructorUsedError;
  Map<MealCategory, MealCategoryVariety>? get perCategory =>
      throw _privateConstructorUsedError;
  bool get lunchDinnerSharedRecency => throw _privateConstructorUsedError;
  bool get breakfastSnackSharedRecency => throw _privateConstructorUsedError;
  bool get lunchDinnerMustDiffer => throw _privateConstructorUsedError;
  bool get proteinSourceVariety => throw _privateConstructorUsedError;

  /// Create a copy of VarietyConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $VarietyConfigCopyWith<VarietyConfig> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $VarietyConfigCopyWith<$Res> {
  factory $VarietyConfigCopyWith(
          VarietyConfig value, $Res Function(VarietyConfig) then) =
      _$VarietyConfigCopyWithImpl<$Res, VarietyConfig>;
  @useResult
  $Res call(
      {VarietyLevel level,
      Map<MealCategory, MealCategoryVariety>? perCategory,
      bool lunchDinnerSharedRecency,
      bool breakfastSnackSharedRecency,
      bool lunchDinnerMustDiffer,
      bool proteinSourceVariety});
}

/// @nodoc
class _$VarietyConfigCopyWithImpl<$Res, $Val extends VarietyConfig>
    implements $VarietyConfigCopyWith<$Res> {
  _$VarietyConfigCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of VarietyConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? level = null,
    Object? perCategory = freezed,
    Object? lunchDinnerSharedRecency = null,
    Object? breakfastSnackSharedRecency = null,
    Object? lunchDinnerMustDiffer = null,
    Object? proteinSourceVariety = null,
  }) {
    return _then(_value.copyWith(
      level: null == level
          ? _value.level
          : level // ignore: cast_nullable_to_non_nullable
              as VarietyLevel,
      perCategory: freezed == perCategory
          ? _value.perCategory
          : perCategory // ignore: cast_nullable_to_non_nullable
              as Map<MealCategory, MealCategoryVariety>?,
      lunchDinnerSharedRecency: null == lunchDinnerSharedRecency
          ? _value.lunchDinnerSharedRecency
          : lunchDinnerSharedRecency // ignore: cast_nullable_to_non_nullable
              as bool,
      breakfastSnackSharedRecency: null == breakfastSnackSharedRecency
          ? _value.breakfastSnackSharedRecency
          : breakfastSnackSharedRecency // ignore: cast_nullable_to_non_nullable
              as bool,
      lunchDinnerMustDiffer: null == lunchDinnerMustDiffer
          ? _value.lunchDinnerMustDiffer
          : lunchDinnerMustDiffer // ignore: cast_nullable_to_non_nullable
              as bool,
      proteinSourceVariety: null == proteinSourceVariety
          ? _value.proteinSourceVariety
          : proteinSourceVariety // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$VarietyConfigImplCopyWith<$Res>
    implements $VarietyConfigCopyWith<$Res> {
  factory _$$VarietyConfigImplCopyWith(
          _$VarietyConfigImpl value, $Res Function(_$VarietyConfigImpl) then) =
      __$$VarietyConfigImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {VarietyLevel level,
      Map<MealCategory, MealCategoryVariety>? perCategory,
      bool lunchDinnerSharedRecency,
      bool breakfastSnackSharedRecency,
      bool lunchDinnerMustDiffer,
      bool proteinSourceVariety});
}

/// @nodoc
class __$$VarietyConfigImplCopyWithImpl<$Res>
    extends _$VarietyConfigCopyWithImpl<$Res, _$VarietyConfigImpl>
    implements _$$VarietyConfigImplCopyWith<$Res> {
  __$$VarietyConfigImplCopyWithImpl(
      _$VarietyConfigImpl _value, $Res Function(_$VarietyConfigImpl) _then)
      : super(_value, _then);

  /// Create a copy of VarietyConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? level = null,
    Object? perCategory = freezed,
    Object? lunchDinnerSharedRecency = null,
    Object? breakfastSnackSharedRecency = null,
    Object? lunchDinnerMustDiffer = null,
    Object? proteinSourceVariety = null,
  }) {
    return _then(_$VarietyConfigImpl(
      level: null == level
          ? _value.level
          : level // ignore: cast_nullable_to_non_nullable
              as VarietyLevel,
      perCategory: freezed == perCategory
          ? _value._perCategory
          : perCategory // ignore: cast_nullable_to_non_nullable
              as Map<MealCategory, MealCategoryVariety>?,
      lunchDinnerSharedRecency: null == lunchDinnerSharedRecency
          ? _value.lunchDinnerSharedRecency
          : lunchDinnerSharedRecency // ignore: cast_nullable_to_non_nullable
              as bool,
      breakfastSnackSharedRecency: null == breakfastSnackSharedRecency
          ? _value.breakfastSnackSharedRecency
          : breakfastSnackSharedRecency // ignore: cast_nullable_to_non_nullable
              as bool,
      lunchDinnerMustDiffer: null == lunchDinnerMustDiffer
          ? _value.lunchDinnerMustDiffer
          : lunchDinnerMustDiffer // ignore: cast_nullable_to_non_nullable
              as bool,
      proteinSourceVariety: null == proteinSourceVariety
          ? _value.proteinSourceVariety
          : proteinSourceVariety // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$VarietyConfigImpl extends _VarietyConfig {
  const _$VarietyConfigImpl(
      {this.level = VarietyLevel.balanced,
      final Map<MealCategory, MealCategoryVariety>? perCategory,
      this.lunchDinnerSharedRecency = true,
      this.breakfastSnackSharedRecency = false,
      this.lunchDinnerMustDiffer = true,
      this.proteinSourceVariety = true})
      : _perCategory = perCategory,
        super._();

  @override
  @JsonKey()
  final VarietyLevel level;
  final Map<MealCategory, MealCategoryVariety>? _perCategory;
  @override
  Map<MealCategory, MealCategoryVariety>? get perCategory {
    final value = _perCategory;
    if (value == null) return null;
    if (_perCategory is EqualUnmodifiableMapView) return _perCategory;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  @override
  @JsonKey()
  final bool lunchDinnerSharedRecency;
  @override
  @JsonKey()
  final bool breakfastSnackSharedRecency;
  @override
  @JsonKey()
  final bool lunchDinnerMustDiffer;
  @override
  @JsonKey()
  final bool proteinSourceVariety;

  @override
  String toString() {
    return 'VarietyConfig(level: $level, perCategory: $perCategory, lunchDinnerSharedRecency: $lunchDinnerSharedRecency, breakfastSnackSharedRecency: $breakfastSnackSharedRecency, lunchDinnerMustDiffer: $lunchDinnerMustDiffer, proteinSourceVariety: $proteinSourceVariety)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$VarietyConfigImpl &&
            (identical(other.level, level) || other.level == level) &&
            const DeepCollectionEquality()
                .equals(other._perCategory, _perCategory) &&
            (identical(
                    other.lunchDinnerSharedRecency, lunchDinnerSharedRecency) ||
                other.lunchDinnerSharedRecency == lunchDinnerSharedRecency) &&
            (identical(other.breakfastSnackSharedRecency,
                    breakfastSnackSharedRecency) ||
                other.breakfastSnackSharedRecency ==
                    breakfastSnackSharedRecency) &&
            (identical(other.lunchDinnerMustDiffer, lunchDinnerMustDiffer) ||
                other.lunchDinnerMustDiffer == lunchDinnerMustDiffer) &&
            (identical(other.proteinSourceVariety, proteinSourceVariety) ||
                other.proteinSourceVariety == proteinSourceVariety));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      level,
      const DeepCollectionEquality().hash(_perCategory),
      lunchDinnerSharedRecency,
      breakfastSnackSharedRecency,
      lunchDinnerMustDiffer,
      proteinSourceVariety);

  /// Create a copy of VarietyConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$VarietyConfigImplCopyWith<_$VarietyConfigImpl> get copyWith =>
      __$$VarietyConfigImplCopyWithImpl<_$VarietyConfigImpl>(this, _$identity);
}

abstract class _VarietyConfig extends VarietyConfig {
  const factory _VarietyConfig(
      {final VarietyLevel level,
      final Map<MealCategory, MealCategoryVariety>? perCategory,
      final bool lunchDinnerSharedRecency,
      final bool breakfastSnackSharedRecency,
      final bool lunchDinnerMustDiffer,
      final bool proteinSourceVariety}) = _$VarietyConfigImpl;
  const _VarietyConfig._() : super._();

  @override
  VarietyLevel get level;
  @override
  Map<MealCategory, MealCategoryVariety>? get perCategory;
  @override
  bool get lunchDinnerSharedRecency;
  @override
  bool get breakfastSnackSharedRecency;
  @override
  bool get lunchDinnerMustDiffer;
  @override
  bool get proteinSourceVariety;

  /// Create a copy of VarietyConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$VarietyConfigImplCopyWith<_$VarietyConfigImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$OptimizerRule {
  String get id => throw _privateConstructorUsedError;
  RuleTargetType get type => throw _privateConstructorUsedError;
  String get target => throw _privateConstructorUsedError;
  String get targetName => throw _privateConstructorUsedError;
  ConstraintType get constraint => throw _privateConstructorUsedError;
  int get value => throw _privateConstructorUsedError;

  /// Create a copy of OptimizerRule
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $OptimizerRuleCopyWith<OptimizerRule> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $OptimizerRuleCopyWith<$Res> {
  factory $OptimizerRuleCopyWith(
          OptimizerRule value, $Res Function(OptimizerRule) then) =
      _$OptimizerRuleCopyWithImpl<$Res, OptimizerRule>;
  @useResult
  $Res call(
      {String id,
      RuleTargetType type,
      String target,
      String targetName,
      ConstraintType constraint,
      int value});
}

/// @nodoc
class _$OptimizerRuleCopyWithImpl<$Res, $Val extends OptimizerRule>
    implements $OptimizerRuleCopyWith<$Res> {
  _$OptimizerRuleCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of OptimizerRule
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? type = null,
    Object? target = null,
    Object? targetName = null,
    Object? constraint = null,
    Object? value = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as RuleTargetType,
      target: null == target
          ? _value.target
          : target // ignore: cast_nullable_to_non_nullable
              as String,
      targetName: null == targetName
          ? _value.targetName
          : targetName // ignore: cast_nullable_to_non_nullable
              as String,
      constraint: null == constraint
          ? _value.constraint
          : constraint // ignore: cast_nullable_to_non_nullable
              as ConstraintType,
      value: null == value
          ? _value.value
          : value // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$OptimizerRuleImplCopyWith<$Res>
    implements $OptimizerRuleCopyWith<$Res> {
  factory _$$OptimizerRuleImplCopyWith(
          _$OptimizerRuleImpl value, $Res Function(_$OptimizerRuleImpl) then) =
      __$$OptimizerRuleImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      RuleTargetType type,
      String target,
      String targetName,
      ConstraintType constraint,
      int value});
}

/// @nodoc
class __$$OptimizerRuleImplCopyWithImpl<$Res>
    extends _$OptimizerRuleCopyWithImpl<$Res, _$OptimizerRuleImpl>
    implements _$$OptimizerRuleImplCopyWith<$Res> {
  __$$OptimizerRuleImplCopyWithImpl(
      _$OptimizerRuleImpl _value, $Res Function(_$OptimizerRuleImpl) _then)
      : super(_value, _then);

  /// Create a copy of OptimizerRule
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? type = null,
    Object? target = null,
    Object? targetName = null,
    Object? constraint = null,
    Object? value = null,
  }) {
    return _then(_$OptimizerRuleImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as RuleTargetType,
      target: null == target
          ? _value.target
          : target // ignore: cast_nullable_to_non_nullable
              as String,
      targetName: null == targetName
          ? _value.targetName
          : targetName // ignore: cast_nullable_to_non_nullable
              as String,
      constraint: null == constraint
          ? _value.constraint
          : constraint // ignore: cast_nullable_to_non_nullable
              as ConstraintType,
      value: null == value
          ? _value.value
          : value // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$OptimizerRuleImpl implements _OptimizerRule {
  const _$OptimizerRuleImpl(
      {this.id = '',
      required this.type,
      required this.target,
      required this.targetName,
      required this.constraint,
      required this.value});

  @override
  @JsonKey()
  final String id;
  @override
  final RuleTargetType type;
  @override
  final String target;
  @override
  final String targetName;
  @override
  final ConstraintType constraint;
  @override
  final int value;

  @override
  String toString() {
    return 'OptimizerRule(id: $id, type: $type, target: $target, targetName: $targetName, constraint: $constraint, value: $value)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$OptimizerRuleImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.type, type) || other.type == type) &&
            (identical(other.target, target) || other.target == target) &&
            (identical(other.targetName, targetName) ||
                other.targetName == targetName) &&
            (identical(other.constraint, constraint) ||
                other.constraint == constraint) &&
            (identical(other.value, value) || other.value == value));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, id, type, target, targetName, constraint, value);

  /// Create a copy of OptimizerRule
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$OptimizerRuleImplCopyWith<_$OptimizerRuleImpl> get copyWith =>
      __$$OptimizerRuleImplCopyWithImpl<_$OptimizerRuleImpl>(this, _$identity);
}

abstract class _OptimizerRule implements OptimizerRule {
  const factory _OptimizerRule(
      {final String id,
      required final RuleTargetType type,
      required final String target,
      required final String targetName,
      required final ConstraintType constraint,
      required final int value}) = _$OptimizerRuleImpl;

  @override
  String get id;
  @override
  RuleTargetType get type;
  @override
  String get target;
  @override
  String get targetName;
  @override
  ConstraintType get constraint;
  @override
  int get value;

  /// Create a copy of OptimizerRule
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$OptimizerRuleImplCopyWith<_$OptimizerRuleImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ShoppingConfig {
  Set<int> get shoppingDays => throw _privateConstructorUsedError;
  int get intervalWeeks => throw _privateConstructorUsedError;

  /// Create a copy of ShoppingConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ShoppingConfigCopyWith<ShoppingConfig> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ShoppingConfigCopyWith<$Res> {
  factory $ShoppingConfigCopyWith(
          ShoppingConfig value, $Res Function(ShoppingConfig) then) =
      _$ShoppingConfigCopyWithImpl<$Res, ShoppingConfig>;
  @useResult
  $Res call({Set<int> shoppingDays, int intervalWeeks});
}

/// @nodoc
class _$ShoppingConfigCopyWithImpl<$Res, $Val extends ShoppingConfig>
    implements $ShoppingConfigCopyWith<$Res> {
  _$ShoppingConfigCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ShoppingConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? shoppingDays = null,
    Object? intervalWeeks = null,
  }) {
    return _then(_value.copyWith(
      shoppingDays: null == shoppingDays
          ? _value.shoppingDays
          : shoppingDays // ignore: cast_nullable_to_non_nullable
              as Set<int>,
      intervalWeeks: null == intervalWeeks
          ? _value.intervalWeeks
          : intervalWeeks // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ShoppingConfigImplCopyWith<$Res>
    implements $ShoppingConfigCopyWith<$Res> {
  factory _$$ShoppingConfigImplCopyWith(_$ShoppingConfigImpl value,
          $Res Function(_$ShoppingConfigImpl) then) =
      __$$ShoppingConfigImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({Set<int> shoppingDays, int intervalWeeks});
}

/// @nodoc
class __$$ShoppingConfigImplCopyWithImpl<$Res>
    extends _$ShoppingConfigCopyWithImpl<$Res, _$ShoppingConfigImpl>
    implements _$$ShoppingConfigImplCopyWith<$Res> {
  __$$ShoppingConfigImplCopyWithImpl(
      _$ShoppingConfigImpl _value, $Res Function(_$ShoppingConfigImpl) _then)
      : super(_value, _then);

  /// Create a copy of ShoppingConfig
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? shoppingDays = null,
    Object? intervalWeeks = null,
  }) {
    return _then(_$ShoppingConfigImpl(
      shoppingDays: null == shoppingDays
          ? _value._shoppingDays
          : shoppingDays // ignore: cast_nullable_to_non_nullable
              as Set<int>,
      intervalWeeks: null == intervalWeeks
          ? _value.intervalWeeks
          : intervalWeeks // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$ShoppingConfigImpl implements _ShoppingConfig {
  const _$ShoppingConfigImpl(
      {required final Set<int> shoppingDays, this.intervalWeeks = 1})
      : _shoppingDays = shoppingDays;

  final Set<int> _shoppingDays;
  @override
  Set<int> get shoppingDays {
    if (_shoppingDays is EqualUnmodifiableSetView) return _shoppingDays;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_shoppingDays);
  }

  @override
  @JsonKey()
  final int intervalWeeks;

  @override
  String toString() {
    return 'ShoppingConfig(shoppingDays: $shoppingDays, intervalWeeks: $intervalWeeks)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ShoppingConfigImpl &&
            const DeepCollectionEquality()
                .equals(other._shoppingDays, _shoppingDays) &&
            (identical(other.intervalWeeks, intervalWeeks) ||
                other.intervalWeeks == intervalWeeks));
  }

  @override
  int get hashCode => Object.hash(runtimeType,
      const DeepCollectionEquality().hash(_shoppingDays), intervalWeeks);

  /// Create a copy of ShoppingConfig
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ShoppingConfigImplCopyWith<_$ShoppingConfigImpl> get copyWith =>
      __$$ShoppingConfigImplCopyWithImpl<_$ShoppingConfigImpl>(
          this, _$identity);
}

abstract class _ShoppingConfig implements ShoppingConfig {
  const factory _ShoppingConfig(
      {required final Set<int> shoppingDays,
      final int intervalWeeks}) = _$ShoppingConfigImpl;

  @override
  Set<int> get shoppingDays;
  @override
  int get intervalWeeks;

  /// Create a copy of ShoppingConfig
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ShoppingConfigImplCopyWith<_$ShoppingConfigImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ProteinPowder {
  String get ingredientId => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  double get proteinPer100g => throw _privateConstructorUsedError;
  double get kcalPer100g => throw _privateConstructorUsedError;
  double get gramsInStock => throw _privateConstructorUsedError;
  bool get autoFillGap => throw _privateConstructorUsedError;
  bool get lowStockWarning => throw _privateConstructorUsedError;

  /// Create a copy of ProteinPowder
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ProteinPowderCopyWith<ProteinPowder> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ProteinPowderCopyWith<$Res> {
  factory $ProteinPowderCopyWith(
          ProteinPowder value, $Res Function(ProteinPowder) then) =
      _$ProteinPowderCopyWithImpl<$Res, ProteinPowder>;
  @useResult
  $Res call(
      {String ingredientId,
      String name,
      double proteinPer100g,
      double kcalPer100g,
      double gramsInStock,
      bool autoFillGap,
      bool lowStockWarning});
}

/// @nodoc
class _$ProteinPowderCopyWithImpl<$Res, $Val extends ProteinPowder>
    implements $ProteinPowderCopyWith<$Res> {
  _$ProteinPowderCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ProteinPowder
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ingredientId = null,
    Object? name = null,
    Object? proteinPer100g = null,
    Object? kcalPer100g = null,
    Object? gramsInStock = null,
    Object? autoFillGap = null,
    Object? lowStockWarning = null,
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
      proteinPer100g: null == proteinPer100g
          ? _value.proteinPer100g
          : proteinPer100g // ignore: cast_nullable_to_non_nullable
              as double,
      kcalPer100g: null == kcalPer100g
          ? _value.kcalPer100g
          : kcalPer100g // ignore: cast_nullable_to_non_nullable
              as double,
      gramsInStock: null == gramsInStock
          ? _value.gramsInStock
          : gramsInStock // ignore: cast_nullable_to_non_nullable
              as double,
      autoFillGap: null == autoFillGap
          ? _value.autoFillGap
          : autoFillGap // ignore: cast_nullable_to_non_nullable
              as bool,
      lowStockWarning: null == lowStockWarning
          ? _value.lowStockWarning
          : lowStockWarning // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ProteinPowderImplCopyWith<$Res>
    implements $ProteinPowderCopyWith<$Res> {
  factory _$$ProteinPowderImplCopyWith(
          _$ProteinPowderImpl value, $Res Function(_$ProteinPowderImpl) then) =
      __$$ProteinPowderImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String ingredientId,
      String name,
      double proteinPer100g,
      double kcalPer100g,
      double gramsInStock,
      bool autoFillGap,
      bool lowStockWarning});
}

/// @nodoc
class __$$ProteinPowderImplCopyWithImpl<$Res>
    extends _$ProteinPowderCopyWithImpl<$Res, _$ProteinPowderImpl>
    implements _$$ProteinPowderImplCopyWith<$Res> {
  __$$ProteinPowderImplCopyWithImpl(
      _$ProteinPowderImpl _value, $Res Function(_$ProteinPowderImpl) _then)
      : super(_value, _then);

  /// Create a copy of ProteinPowder
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ingredientId = null,
    Object? name = null,
    Object? proteinPer100g = null,
    Object? kcalPer100g = null,
    Object? gramsInStock = null,
    Object? autoFillGap = null,
    Object? lowStockWarning = null,
  }) {
    return _then(_$ProteinPowderImpl(
      ingredientId: null == ingredientId
          ? _value.ingredientId
          : ingredientId // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      proteinPer100g: null == proteinPer100g
          ? _value.proteinPer100g
          : proteinPer100g // ignore: cast_nullable_to_non_nullable
              as double,
      kcalPer100g: null == kcalPer100g
          ? _value.kcalPer100g
          : kcalPer100g // ignore: cast_nullable_to_non_nullable
              as double,
      gramsInStock: null == gramsInStock
          ? _value.gramsInStock
          : gramsInStock // ignore: cast_nullable_to_non_nullable
              as double,
      autoFillGap: null == autoFillGap
          ? _value.autoFillGap
          : autoFillGap // ignore: cast_nullable_to_non_nullable
              as bool,
      lowStockWarning: null == lowStockWarning
          ? _value.lowStockWarning
          : lowStockWarning // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$ProteinPowderImpl extends _ProteinPowder {
  const _$ProteinPowderImpl(
      {required this.ingredientId,
      required this.name,
      required this.proteinPer100g,
      required this.kcalPer100g,
      required this.gramsInStock,
      this.autoFillGap = true,
      this.lowStockWarning = true})
      : super._();

  @override
  final String ingredientId;
  @override
  final String name;
  @override
  final double proteinPer100g;
  @override
  final double kcalPer100g;
  @override
  final double gramsInStock;
  @override
  @JsonKey()
  final bool autoFillGap;
  @override
  @JsonKey()
  final bool lowStockWarning;

  @override
  String toString() {
    return 'ProteinPowder(ingredientId: $ingredientId, name: $name, proteinPer100g: $proteinPer100g, kcalPer100g: $kcalPer100g, gramsInStock: $gramsInStock, autoFillGap: $autoFillGap, lowStockWarning: $lowStockWarning)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProteinPowderImpl &&
            (identical(other.ingredientId, ingredientId) ||
                other.ingredientId == ingredientId) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.proteinPer100g, proteinPer100g) ||
                other.proteinPer100g == proteinPer100g) &&
            (identical(other.kcalPer100g, kcalPer100g) ||
                other.kcalPer100g == kcalPer100g) &&
            (identical(other.gramsInStock, gramsInStock) ||
                other.gramsInStock == gramsInStock) &&
            (identical(other.autoFillGap, autoFillGap) ||
                other.autoFillGap == autoFillGap) &&
            (identical(other.lowStockWarning, lowStockWarning) ||
                other.lowStockWarning == lowStockWarning));
  }

  @override
  int get hashCode => Object.hash(runtimeType, ingredientId, name,
      proteinPer100g, kcalPer100g, gramsInStock, autoFillGap, lowStockWarning);

  /// Create a copy of ProteinPowder
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ProteinPowderImplCopyWith<_$ProteinPowderImpl> get copyWith =>
      __$$ProteinPowderImplCopyWithImpl<_$ProteinPowderImpl>(this, _$identity);
}

abstract class _ProteinPowder extends ProteinPowder {
  const factory _ProteinPowder(
      {required final String ingredientId,
      required final String name,
      required final double proteinPer100g,
      required final double kcalPer100g,
      required final double gramsInStock,
      final bool autoFillGap,
      final bool lowStockWarning}) = _$ProteinPowderImpl;
  const _ProteinPowder._() : super._();

  @override
  String get ingredientId;
  @override
  String get name;
  @override
  double get proteinPer100g;
  @override
  double get kcalPer100g;
  @override
  double get gramsInStock;
  @override
  bool get autoFillGap;
  @override
  bool get lowStockWarning;

  /// Create a copy of ProteinPowder
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ProteinPowderImplCopyWith<_$ProteinPowderImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
