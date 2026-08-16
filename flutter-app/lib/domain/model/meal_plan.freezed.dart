// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'meal_plan.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$MealPlan {
  String get id => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  DateTime get startDate => throw _privateConstructorUsedError;
  DateTime get endDate => throw _privateConstructorUsedError;
  List<DayPlan> get days => throw _privateConstructorUsedError;

  /// Create a copy of MealPlan
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $MealPlanCopyWith<MealPlan> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MealPlanCopyWith<$Res> {
  factory $MealPlanCopyWith(MealPlan value, $Res Function(MealPlan) then) =
      _$MealPlanCopyWithImpl<$Res, MealPlan>;
  @useResult
  $Res call(
      {String id,
      String name,
      DateTime startDate,
      DateTime endDate,
      List<DayPlan> days});
}

/// @nodoc
class _$MealPlanCopyWithImpl<$Res, $Val extends MealPlan>
    implements $MealPlanCopyWith<$Res> {
  _$MealPlanCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of MealPlan
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? startDate = null,
    Object? endDate = null,
    Object? days = null,
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
      startDate: null == startDate
          ? _value.startDate
          : startDate // ignore: cast_nullable_to_non_nullable
              as DateTime,
      endDate: null == endDate
          ? _value.endDate
          : endDate // ignore: cast_nullable_to_non_nullable
              as DateTime,
      days: null == days
          ? _value.days
          : days // ignore: cast_nullable_to_non_nullable
              as List<DayPlan>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$MealPlanImplCopyWith<$Res>
    implements $MealPlanCopyWith<$Res> {
  factory _$$MealPlanImplCopyWith(
          _$MealPlanImpl value, $Res Function(_$MealPlanImpl) then) =
      __$$MealPlanImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String name,
      DateTime startDate,
      DateTime endDate,
      List<DayPlan> days});
}

/// @nodoc
class __$$MealPlanImplCopyWithImpl<$Res>
    extends _$MealPlanCopyWithImpl<$Res, _$MealPlanImpl>
    implements _$$MealPlanImplCopyWith<$Res> {
  __$$MealPlanImplCopyWithImpl(
      _$MealPlanImpl _value, $Res Function(_$MealPlanImpl) _then)
      : super(_value, _then);

  /// Create a copy of MealPlan
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? startDate = null,
    Object? endDate = null,
    Object? days = null,
  }) {
    return _then(_$MealPlanImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      startDate: null == startDate
          ? _value.startDate
          : startDate // ignore: cast_nullable_to_non_nullable
              as DateTime,
      endDate: null == endDate
          ? _value.endDate
          : endDate // ignore: cast_nullable_to_non_nullable
              as DateTime,
      days: null == days
          ? _value._days
          : days // ignore: cast_nullable_to_non_nullable
              as List<DayPlan>,
    ));
  }
}

/// @nodoc

class _$MealPlanImpl implements _MealPlan {
  const _$MealPlanImpl(
      {required this.id,
      required this.name,
      required this.startDate,
      required this.endDate,
      final List<DayPlan> days = const <DayPlan>[]})
      : _days = days;

  @override
  final String id;
  @override
  final String name;
  @override
  final DateTime startDate;
  @override
  final DateTime endDate;
  final List<DayPlan> _days;
  @override
  @JsonKey()
  List<DayPlan> get days {
    if (_days is EqualUnmodifiableListView) return _days;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_days);
  }

  @override
  String toString() {
    return 'MealPlan(id: $id, name: $name, startDate: $startDate, endDate: $endDate, days: $days)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MealPlanImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.startDate, startDate) ||
                other.startDate == startDate) &&
            (identical(other.endDate, endDate) || other.endDate == endDate) &&
            const DeepCollectionEquality().equals(other._days, _days));
  }

  @override
  int get hashCode => Object.hash(runtimeType, id, name, startDate, endDate,
      const DeepCollectionEquality().hash(_days));

  /// Create a copy of MealPlan
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$MealPlanImplCopyWith<_$MealPlanImpl> get copyWith =>
      __$$MealPlanImplCopyWithImpl<_$MealPlanImpl>(this, _$identity);
}

abstract class _MealPlan implements MealPlan {
  const factory _MealPlan(
      {required final String id,
      required final String name,
      required final DateTime startDate,
      required final DateTime endDate,
      final List<DayPlan> days}) = _$MealPlanImpl;

  @override
  String get id;
  @override
  String get name;
  @override
  DateTime get startDate;
  @override
  DateTime get endDate;
  @override
  List<DayPlan> get days;

  /// Create a copy of MealPlan
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$MealPlanImplCopyWith<_$MealPlanImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$DayPlan {
  String get id => throw _privateConstructorUsedError;
  DateTime get date => throw _privateConstructorUsedError;
  List<MealSlot> get meals => throw _privateConstructorUsedError;
  double get proteinPowderGrams => throw _privateConstructorUsedError;
  DailyGoal get goal => throw _privateConstructorUsedError;

  /// Create a copy of DayPlan
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DayPlanCopyWith<DayPlan> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DayPlanCopyWith<$Res> {
  factory $DayPlanCopyWith(DayPlan value, $Res Function(DayPlan) then) =
      _$DayPlanCopyWithImpl<$Res, DayPlan>;
  @useResult
  $Res call(
      {String id,
      DateTime date,
      List<MealSlot> meals,
      double proteinPowderGrams,
      DailyGoal goal});

  $DailyGoalCopyWith<$Res> get goal;
}

/// @nodoc
class _$DayPlanCopyWithImpl<$Res, $Val extends DayPlan>
    implements $DayPlanCopyWith<$Res> {
  _$DayPlanCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DayPlan
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? date = null,
    Object? meals = null,
    Object? proteinPowderGrams = null,
    Object? goal = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      date: null == date
          ? _value.date
          : date // ignore: cast_nullable_to_non_nullable
              as DateTime,
      meals: null == meals
          ? _value.meals
          : meals // ignore: cast_nullable_to_non_nullable
              as List<MealSlot>,
      proteinPowderGrams: null == proteinPowderGrams
          ? _value.proteinPowderGrams
          : proteinPowderGrams // ignore: cast_nullable_to_non_nullable
              as double,
      goal: null == goal
          ? _value.goal
          : goal // ignore: cast_nullable_to_non_nullable
              as DailyGoal,
    ) as $Val);
  }

  /// Create a copy of DayPlan
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $DailyGoalCopyWith<$Res> get goal {
    return $DailyGoalCopyWith<$Res>(_value.goal, (value) {
      return _then(_value.copyWith(goal: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$DayPlanImplCopyWith<$Res> implements $DayPlanCopyWith<$Res> {
  factory _$$DayPlanImplCopyWith(
          _$DayPlanImpl value, $Res Function(_$DayPlanImpl) then) =
      __$$DayPlanImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      DateTime date,
      List<MealSlot> meals,
      double proteinPowderGrams,
      DailyGoal goal});

  @override
  $DailyGoalCopyWith<$Res> get goal;
}

/// @nodoc
class __$$DayPlanImplCopyWithImpl<$Res>
    extends _$DayPlanCopyWithImpl<$Res, _$DayPlanImpl>
    implements _$$DayPlanImplCopyWith<$Res> {
  __$$DayPlanImplCopyWithImpl(
      _$DayPlanImpl _value, $Res Function(_$DayPlanImpl) _then)
      : super(_value, _then);

  /// Create a copy of DayPlan
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? date = null,
    Object? meals = null,
    Object? proteinPowderGrams = null,
    Object? goal = null,
  }) {
    return _then(_$DayPlanImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      date: null == date
          ? _value.date
          : date // ignore: cast_nullable_to_non_nullable
              as DateTime,
      meals: null == meals
          ? _value._meals
          : meals // ignore: cast_nullable_to_non_nullable
              as List<MealSlot>,
      proteinPowderGrams: null == proteinPowderGrams
          ? _value.proteinPowderGrams
          : proteinPowderGrams // ignore: cast_nullable_to_non_nullable
              as double,
      goal: null == goal
          ? _value.goal
          : goal // ignore: cast_nullable_to_non_nullable
              as DailyGoal,
    ));
  }
}

/// @nodoc

class _$DayPlanImpl implements _DayPlan {
  const _$DayPlanImpl(
      {required this.id,
      required this.date,
      final List<MealSlot> meals = const <MealSlot>[],
      this.proteinPowderGrams = 0.0,
      this.goal = const DailyGoal()})
      : _meals = meals;

  @override
  final String id;
  @override
  final DateTime date;
  final List<MealSlot> _meals;
  @override
  @JsonKey()
  List<MealSlot> get meals {
    if (_meals is EqualUnmodifiableListView) return _meals;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_meals);
  }

  @override
  @JsonKey()
  final double proteinPowderGrams;
  @override
  @JsonKey()
  final DailyGoal goal;

  @override
  String toString() {
    return 'DayPlan(id: $id, date: $date, meals: $meals, proteinPowderGrams: $proteinPowderGrams, goal: $goal)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DayPlanImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.date, date) || other.date == date) &&
            const DeepCollectionEquality().equals(other._meals, _meals) &&
            (identical(other.proteinPowderGrams, proteinPowderGrams) ||
                other.proteinPowderGrams == proteinPowderGrams) &&
            (identical(other.goal, goal) || other.goal == goal));
  }

  @override
  int get hashCode => Object.hash(runtimeType, id, date,
      const DeepCollectionEquality().hash(_meals), proteinPowderGrams, goal);

  /// Create a copy of DayPlan
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DayPlanImplCopyWith<_$DayPlanImpl> get copyWith =>
      __$$DayPlanImplCopyWithImpl<_$DayPlanImpl>(this, _$identity);
}

abstract class _DayPlan implements DayPlan {
  const factory _DayPlan(
      {required final String id,
      required final DateTime date,
      final List<MealSlot> meals,
      final double proteinPowderGrams,
      final DailyGoal goal}) = _$DayPlanImpl;

  @override
  String get id;
  @override
  DateTime get date;
  @override
  List<MealSlot> get meals;
  @override
  double get proteinPowderGrams;
  @override
  DailyGoal get goal;

  /// Create a copy of DayPlan
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DayPlanImplCopyWith<_$DayPlanImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$MealSlot {
  MealCategory get type => throw _privateConstructorUsedError;
  String get recipeId => throw _privateConstructorUsedError;

  /// Create a copy of MealSlot
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $MealSlotCopyWith<MealSlot> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MealSlotCopyWith<$Res> {
  factory $MealSlotCopyWith(MealSlot value, $Res Function(MealSlot) then) =
      _$MealSlotCopyWithImpl<$Res, MealSlot>;
  @useResult
  $Res call({MealCategory type, String recipeId});
}

/// @nodoc
class _$MealSlotCopyWithImpl<$Res, $Val extends MealSlot>
    implements $MealSlotCopyWith<$Res> {
  _$MealSlotCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of MealSlot
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? type = null,
    Object? recipeId = null,
  }) {
    return _then(_value.copyWith(
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as MealCategory,
      recipeId: null == recipeId
          ? _value.recipeId
          : recipeId // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$MealSlotImplCopyWith<$Res>
    implements $MealSlotCopyWith<$Res> {
  factory _$$MealSlotImplCopyWith(
          _$MealSlotImpl value, $Res Function(_$MealSlotImpl) then) =
      __$$MealSlotImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({MealCategory type, String recipeId});
}

/// @nodoc
class __$$MealSlotImplCopyWithImpl<$Res>
    extends _$MealSlotCopyWithImpl<$Res, _$MealSlotImpl>
    implements _$$MealSlotImplCopyWith<$Res> {
  __$$MealSlotImplCopyWithImpl(
      _$MealSlotImpl _value, $Res Function(_$MealSlotImpl) _then)
      : super(_value, _then);

  /// Create a copy of MealSlot
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? type = null,
    Object? recipeId = null,
  }) {
    return _then(_$MealSlotImpl(
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as MealCategory,
      recipeId: null == recipeId
          ? _value.recipeId
          : recipeId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$MealSlotImpl implements _MealSlot {
  const _$MealSlotImpl({required this.type, required this.recipeId});

  @override
  final MealCategory type;
  @override
  final String recipeId;

  @override
  String toString() {
    return 'MealSlot(type: $type, recipeId: $recipeId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MealSlotImpl &&
            (identical(other.type, type) || other.type == type) &&
            (identical(other.recipeId, recipeId) ||
                other.recipeId == recipeId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, type, recipeId);

  /// Create a copy of MealSlot
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$MealSlotImplCopyWith<_$MealSlotImpl> get copyWith =>
      __$$MealSlotImplCopyWithImpl<_$MealSlotImpl>(this, _$identity);
}

abstract class _MealSlot implements MealSlot {
  const factory _MealSlot(
      {required final MealCategory type,
      required final String recipeId}) = _$MealSlotImpl;

  @override
  MealCategory get type;
  @override
  String get recipeId;

  /// Create a copy of MealSlot
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$MealSlotImplCopyWith<_$MealSlotImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$DailyGoal {
  double get kcalTarget => throw _privateConstructorUsedError;
  double get proteinTarget => throw _privateConstructorUsedError;

  /// Create a copy of DailyGoal
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DailyGoalCopyWith<DailyGoal> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DailyGoalCopyWith<$Res> {
  factory $DailyGoalCopyWith(DailyGoal value, $Res Function(DailyGoal) then) =
      _$DailyGoalCopyWithImpl<$Res, DailyGoal>;
  @useResult
  $Res call({double kcalTarget, double proteinTarget});
}

/// @nodoc
class _$DailyGoalCopyWithImpl<$Res, $Val extends DailyGoal>
    implements $DailyGoalCopyWith<$Res> {
  _$DailyGoalCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DailyGoal
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? kcalTarget = null,
    Object? proteinTarget = null,
  }) {
    return _then(_value.copyWith(
      kcalTarget: null == kcalTarget
          ? _value.kcalTarget
          : kcalTarget // ignore: cast_nullable_to_non_nullable
              as double,
      proteinTarget: null == proteinTarget
          ? _value.proteinTarget
          : proteinTarget // ignore: cast_nullable_to_non_nullable
              as double,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$DailyGoalImplCopyWith<$Res>
    implements $DailyGoalCopyWith<$Res> {
  factory _$$DailyGoalImplCopyWith(
          _$DailyGoalImpl value, $Res Function(_$DailyGoalImpl) then) =
      __$$DailyGoalImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({double kcalTarget, double proteinTarget});
}

/// @nodoc
class __$$DailyGoalImplCopyWithImpl<$Res>
    extends _$DailyGoalCopyWithImpl<$Res, _$DailyGoalImpl>
    implements _$$DailyGoalImplCopyWith<$Res> {
  __$$DailyGoalImplCopyWithImpl(
      _$DailyGoalImpl _value, $Res Function(_$DailyGoalImpl) _then)
      : super(_value, _then);

  /// Create a copy of DailyGoal
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? kcalTarget = null,
    Object? proteinTarget = null,
  }) {
    return _then(_$DailyGoalImpl(
      kcalTarget: null == kcalTarget
          ? _value.kcalTarget
          : kcalTarget // ignore: cast_nullable_to_non_nullable
              as double,
      proteinTarget: null == proteinTarget
          ? _value.proteinTarget
          : proteinTarget // ignore: cast_nullable_to_non_nullable
              as double,
    ));
  }
}

/// @nodoc

class _$DailyGoalImpl implements _DailyGoal {
  const _$DailyGoalImpl({this.kcalTarget = 1350.0, this.proteinTarget = 120.0});

  @override
  @JsonKey()
  final double kcalTarget;
  @override
  @JsonKey()
  final double proteinTarget;

  @override
  String toString() {
    return 'DailyGoal(kcalTarget: $kcalTarget, proteinTarget: $proteinTarget)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DailyGoalImpl &&
            (identical(other.kcalTarget, kcalTarget) ||
                other.kcalTarget == kcalTarget) &&
            (identical(other.proteinTarget, proteinTarget) ||
                other.proteinTarget == proteinTarget));
  }

  @override
  int get hashCode => Object.hash(runtimeType, kcalTarget, proteinTarget);

  /// Create a copy of DailyGoal
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DailyGoalImplCopyWith<_$DailyGoalImpl> get copyWith =>
      __$$DailyGoalImplCopyWithImpl<_$DailyGoalImpl>(this, _$identity);
}

abstract class _DailyGoal implements DailyGoal {
  const factory _DailyGoal(
      {final double kcalTarget, final double proteinTarget}) = _$DailyGoalImpl;

  @override
  double get kcalTarget;
  @override
  double get proteinTarget;

  /// Create a copy of DailyGoal
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DailyGoalImplCopyWith<_$DailyGoalImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
