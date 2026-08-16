// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_database.dart';

// ignore_for_file: type=lint
class $RecipesTableTable extends RecipesTable
    with TableInfo<$RecipesTableTable, RecipesTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $RecipesTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  @override
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
      'name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _typeMeta = const VerificationMeta('type');
  @override
  late final GeneratedColumn<String> type = GeneratedColumn<String>(
      'type', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _mealCategoriesMeta =
      const VerificationMeta('mealCategories');
  @override
  late final GeneratedColumn<String> mealCategories = GeneratedColumn<String>(
      'meal_categories', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _componentCategoryMeta =
      const VerificationMeta('componentCategory');
  @override
  late final GeneratedColumn<String> componentCategory =
      GeneratedColumn<String>('component_category', aliasedName, true,
          type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _stepsMeta = const VerificationMeta('steps');
  @override
  late final GeneratedColumn<String> steps = GeneratedColumn<String>(
      'steps', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('[]'));
  static const VerificationMeta _notesMeta = const VerificationMeta('notes');
  @override
  late final GeneratedColumn<String> notes = GeneratedColumn<String>(
      'notes', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  @override
  List<GeneratedColumn> get $columns =>
      [id, name, type, mealCategories, componentCategory, steps, notes];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'recipes';
  @override
  VerificationContext validateIntegrity(Insertable<RecipesTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('name')) {
      context.handle(
          _nameMeta, name.isAcceptableOrUnknown(data['name']!, _nameMeta));
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('type')) {
      context.handle(
          _typeMeta, type.isAcceptableOrUnknown(data['type']!, _typeMeta));
    } else if (isInserting) {
      context.missing(_typeMeta);
    }
    if (data.containsKey('meal_categories')) {
      context.handle(
          _mealCategoriesMeta,
          mealCategories.isAcceptableOrUnknown(
              data['meal_categories']!, _mealCategoriesMeta));
    }
    if (data.containsKey('component_category')) {
      context.handle(
          _componentCategoryMeta,
          componentCategory.isAcceptableOrUnknown(
              data['component_category']!, _componentCategoryMeta));
    }
    if (data.containsKey('steps')) {
      context.handle(
          _stepsMeta, steps.isAcceptableOrUnknown(data['steps']!, _stepsMeta));
    }
    if (data.containsKey('notes')) {
      context.handle(
          _notesMeta, notes.isAcceptableOrUnknown(data['notes']!, _notesMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  RecipesTableData map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return RecipesTableData(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      name: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}name'])!,
      type: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}type'])!,
      mealCategories: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}meal_categories'])!,
      componentCategory: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}component_category']),
      steps: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}steps'])!,
      notes: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}notes'])!,
    );
  }

  @override
  $RecipesTableTable createAlias(String alias) {
    return $RecipesTableTable(attachedDatabase, alias);
  }
}

class RecipesTableData extends DataClass
    implements Insertable<RecipesTableData> {
  final String id;
  final String name;
  final String type;
  final String mealCategories;
  final String? componentCategory;
  final String steps;
  final String notes;
  const RecipesTableData(
      {required this.id,
      required this.name,
      required this.type,
      required this.mealCategories,
      this.componentCategory,
      required this.steps,
      required this.notes});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['name'] = Variable<String>(name);
    map['type'] = Variable<String>(type);
    map['meal_categories'] = Variable<String>(mealCategories);
    if (!nullToAbsent || componentCategory != null) {
      map['component_category'] = Variable<String>(componentCategory);
    }
    map['steps'] = Variable<String>(steps);
    map['notes'] = Variable<String>(notes);
    return map;
  }

  RecipesTableCompanion toCompanion(bool nullToAbsent) {
    return RecipesTableCompanion(
      id: Value(id),
      name: Value(name),
      type: Value(type),
      mealCategories: Value(mealCategories),
      componentCategory: componentCategory == null && nullToAbsent
          ? const Value.absent()
          : Value(componentCategory),
      steps: Value(steps),
      notes: Value(notes),
    );
  }

  factory RecipesTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return RecipesTableData(
      id: serializer.fromJson<String>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      type: serializer.fromJson<String>(json['type']),
      mealCategories: serializer.fromJson<String>(json['mealCategories']),
      componentCategory:
          serializer.fromJson<String?>(json['componentCategory']),
      steps: serializer.fromJson<String>(json['steps']),
      notes: serializer.fromJson<String>(json['notes']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'name': serializer.toJson<String>(name),
      'type': serializer.toJson<String>(type),
      'mealCategories': serializer.toJson<String>(mealCategories),
      'componentCategory': serializer.toJson<String?>(componentCategory),
      'steps': serializer.toJson<String>(steps),
      'notes': serializer.toJson<String>(notes),
    };
  }

  RecipesTableData copyWith(
          {String? id,
          String? name,
          String? type,
          String? mealCategories,
          Value<String?> componentCategory = const Value.absent(),
          String? steps,
          String? notes}) =>
      RecipesTableData(
        id: id ?? this.id,
        name: name ?? this.name,
        type: type ?? this.type,
        mealCategories: mealCategories ?? this.mealCategories,
        componentCategory: componentCategory.present
            ? componentCategory.value
            : this.componentCategory,
        steps: steps ?? this.steps,
        notes: notes ?? this.notes,
      );
  RecipesTableData copyWithCompanion(RecipesTableCompanion data) {
    return RecipesTableData(
      id: data.id.present ? data.id.value : this.id,
      name: data.name.present ? data.name.value : this.name,
      type: data.type.present ? data.type.value : this.type,
      mealCategories: data.mealCategories.present
          ? data.mealCategories.value
          : this.mealCategories,
      componentCategory: data.componentCategory.present
          ? data.componentCategory.value
          : this.componentCategory,
      steps: data.steps.present ? data.steps.value : this.steps,
      notes: data.notes.present ? data.notes.value : this.notes,
    );
  }

  @override
  String toString() {
    return (StringBuffer('RecipesTableData(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('type: $type, ')
          ..write('mealCategories: $mealCategories, ')
          ..write('componentCategory: $componentCategory, ')
          ..write('steps: $steps, ')
          ..write('notes: $notes')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id, name, type, mealCategories, componentCategory, steps, notes);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is RecipesTableData &&
          other.id == this.id &&
          other.name == this.name &&
          other.type == this.type &&
          other.mealCategories == this.mealCategories &&
          other.componentCategory == this.componentCategory &&
          other.steps == this.steps &&
          other.notes == this.notes);
}

class RecipesTableCompanion extends UpdateCompanion<RecipesTableData> {
  final Value<String> id;
  final Value<String> name;
  final Value<String> type;
  final Value<String> mealCategories;
  final Value<String?> componentCategory;
  final Value<String> steps;
  final Value<String> notes;
  final Value<int> rowid;
  const RecipesTableCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.type = const Value.absent(),
    this.mealCategories = const Value.absent(),
    this.componentCategory = const Value.absent(),
    this.steps = const Value.absent(),
    this.notes = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  RecipesTableCompanion.insert({
    required String id,
    required String name,
    required String type,
    this.mealCategories = const Value.absent(),
    this.componentCategory = const Value.absent(),
    this.steps = const Value.absent(),
    this.notes = const Value.absent(),
    this.rowid = const Value.absent(),
  })  : id = Value(id),
        name = Value(name),
        type = Value(type);
  static Insertable<RecipesTableData> custom({
    Expression<String>? id,
    Expression<String>? name,
    Expression<String>? type,
    Expression<String>? mealCategories,
    Expression<String>? componentCategory,
    Expression<String>? steps,
    Expression<String>? notes,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (type != null) 'type': type,
      if (mealCategories != null) 'meal_categories': mealCategories,
      if (componentCategory != null) 'component_category': componentCategory,
      if (steps != null) 'steps': steps,
      if (notes != null) 'notes': notes,
      if (rowid != null) 'rowid': rowid,
    });
  }

  RecipesTableCompanion copyWith(
      {Value<String>? id,
      Value<String>? name,
      Value<String>? type,
      Value<String>? mealCategories,
      Value<String?>? componentCategory,
      Value<String>? steps,
      Value<String>? notes,
      Value<int>? rowid}) {
    return RecipesTableCompanion(
      id: id ?? this.id,
      name: name ?? this.name,
      type: type ?? this.type,
      mealCategories: mealCategories ?? this.mealCategories,
      componentCategory: componentCategory ?? this.componentCategory,
      steps: steps ?? this.steps,
      notes: notes ?? this.notes,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (type.present) {
      map['type'] = Variable<String>(type.value);
    }
    if (mealCategories.present) {
      map['meal_categories'] = Variable<String>(mealCategories.value);
    }
    if (componentCategory.present) {
      map['component_category'] = Variable<String>(componentCategory.value);
    }
    if (steps.present) {
      map['steps'] = Variable<String>(steps.value);
    }
    if (notes.present) {
      map['notes'] = Variable<String>(notes.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('RecipesTableCompanion(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('type: $type, ')
          ..write('mealCategories: $mealCategories, ')
          ..write('componentCategory: $componentCategory, ')
          ..write('steps: $steps, ')
          ..write('notes: $notes, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $RecipeIngredientsTableTable extends RecipeIngredientsTable
    with TableInfo<$RecipeIngredientsTableTable, RecipeIngredientsTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $RecipeIngredientsTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _recipeIdMeta =
      const VerificationMeta('recipeId');
  @override
  late final GeneratedColumn<String> recipeId = GeneratedColumn<String>(
      'recipe_id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: true,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES recipes (id) ON DELETE CASCADE'));
  static const VerificationMeta _ingredientIdMeta =
      const VerificationMeta('ingredientId');
  @override
  late final GeneratedColumn<String> ingredientId = GeneratedColumn<String>(
      'ingredient_id', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _subRecipeIdMeta =
      const VerificationMeta('subRecipeId');
  @override
  late final GeneratedColumn<String> subRecipeId = GeneratedColumn<String>(
      'sub_recipe_id', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _gramsMeta = const VerificationMeta('grams');
  @override
  late final GeneratedColumn<double> grams = GeneratedColumn<double>(
      'grams', aliasedName, true,
      type: DriftSqlType.double, requiredDuringInsert: false);
  static const VerificationMeta _portionsMeta =
      const VerificationMeta('portions');
  @override
  late final GeneratedColumn<double> portions = GeneratedColumn<double>(
      'portions', aliasedName, true,
      type: DriftSqlType.double, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns =>
      [id, recipeId, ingredientId, subRecipeId, grams, portions];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'recipe_ingredients';
  @override
  VerificationContext validateIntegrity(
      Insertable<RecipeIngredientsTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('recipe_id')) {
      context.handle(_recipeIdMeta,
          recipeId.isAcceptableOrUnknown(data['recipe_id']!, _recipeIdMeta));
    } else if (isInserting) {
      context.missing(_recipeIdMeta);
    }
    if (data.containsKey('ingredient_id')) {
      context.handle(
          _ingredientIdMeta,
          ingredientId.isAcceptableOrUnknown(
              data['ingredient_id']!, _ingredientIdMeta));
    }
    if (data.containsKey('sub_recipe_id')) {
      context.handle(
          _subRecipeIdMeta,
          subRecipeId.isAcceptableOrUnknown(
              data['sub_recipe_id']!, _subRecipeIdMeta));
    }
    if (data.containsKey('grams')) {
      context.handle(
          _gramsMeta, grams.isAcceptableOrUnknown(data['grams']!, _gramsMeta));
    }
    if (data.containsKey('portions')) {
      context.handle(_portionsMeta,
          portions.isAcceptableOrUnknown(data['portions']!, _portionsMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  RecipeIngredientsTableData map(Map<String, dynamic> data,
      {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return RecipeIngredientsTableData(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      recipeId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}recipe_id'])!,
      ingredientId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}ingredient_id']),
      subRecipeId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}sub_recipe_id']),
      grams: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}grams']),
      portions: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}portions']),
    );
  }

  @override
  $RecipeIngredientsTableTable createAlias(String alias) {
    return $RecipeIngredientsTableTable(attachedDatabase, alias);
  }
}

class RecipeIngredientsTableData extends DataClass
    implements Insertable<RecipeIngredientsTableData> {
  final String id;
  final String recipeId;
  final String? ingredientId;
  final String? subRecipeId;
  final double? grams;
  final double? portions;
  const RecipeIngredientsTableData(
      {required this.id,
      required this.recipeId,
      this.ingredientId,
      this.subRecipeId,
      this.grams,
      this.portions});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['recipe_id'] = Variable<String>(recipeId);
    if (!nullToAbsent || ingredientId != null) {
      map['ingredient_id'] = Variable<String>(ingredientId);
    }
    if (!nullToAbsent || subRecipeId != null) {
      map['sub_recipe_id'] = Variable<String>(subRecipeId);
    }
    if (!nullToAbsent || grams != null) {
      map['grams'] = Variable<double>(grams);
    }
    if (!nullToAbsent || portions != null) {
      map['portions'] = Variable<double>(portions);
    }
    return map;
  }

  RecipeIngredientsTableCompanion toCompanion(bool nullToAbsent) {
    return RecipeIngredientsTableCompanion(
      id: Value(id),
      recipeId: Value(recipeId),
      ingredientId: ingredientId == null && nullToAbsent
          ? const Value.absent()
          : Value(ingredientId),
      subRecipeId: subRecipeId == null && nullToAbsent
          ? const Value.absent()
          : Value(subRecipeId),
      grams:
          grams == null && nullToAbsent ? const Value.absent() : Value(grams),
      portions: portions == null && nullToAbsent
          ? const Value.absent()
          : Value(portions),
    );
  }

  factory RecipeIngredientsTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return RecipeIngredientsTableData(
      id: serializer.fromJson<String>(json['id']),
      recipeId: serializer.fromJson<String>(json['recipeId']),
      ingredientId: serializer.fromJson<String?>(json['ingredientId']),
      subRecipeId: serializer.fromJson<String?>(json['subRecipeId']),
      grams: serializer.fromJson<double?>(json['grams']),
      portions: serializer.fromJson<double?>(json['portions']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'recipeId': serializer.toJson<String>(recipeId),
      'ingredientId': serializer.toJson<String?>(ingredientId),
      'subRecipeId': serializer.toJson<String?>(subRecipeId),
      'grams': serializer.toJson<double?>(grams),
      'portions': serializer.toJson<double?>(portions),
    };
  }

  RecipeIngredientsTableData copyWith(
          {String? id,
          String? recipeId,
          Value<String?> ingredientId = const Value.absent(),
          Value<String?> subRecipeId = const Value.absent(),
          Value<double?> grams = const Value.absent(),
          Value<double?> portions = const Value.absent()}) =>
      RecipeIngredientsTableData(
        id: id ?? this.id,
        recipeId: recipeId ?? this.recipeId,
        ingredientId:
            ingredientId.present ? ingredientId.value : this.ingredientId,
        subRecipeId: subRecipeId.present ? subRecipeId.value : this.subRecipeId,
        grams: grams.present ? grams.value : this.grams,
        portions: portions.present ? portions.value : this.portions,
      );
  RecipeIngredientsTableData copyWithCompanion(
      RecipeIngredientsTableCompanion data) {
    return RecipeIngredientsTableData(
      id: data.id.present ? data.id.value : this.id,
      recipeId: data.recipeId.present ? data.recipeId.value : this.recipeId,
      ingredientId: data.ingredientId.present
          ? data.ingredientId.value
          : this.ingredientId,
      subRecipeId:
          data.subRecipeId.present ? data.subRecipeId.value : this.subRecipeId,
      grams: data.grams.present ? data.grams.value : this.grams,
      portions: data.portions.present ? data.portions.value : this.portions,
    );
  }

  @override
  String toString() {
    return (StringBuffer('RecipeIngredientsTableData(')
          ..write('id: $id, ')
          ..write('recipeId: $recipeId, ')
          ..write('ingredientId: $ingredientId, ')
          ..write('subRecipeId: $subRecipeId, ')
          ..write('grams: $grams, ')
          ..write('portions: $portions')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, recipeId, ingredientId, subRecipeId, grams, portions);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is RecipeIngredientsTableData &&
          other.id == this.id &&
          other.recipeId == this.recipeId &&
          other.ingredientId == this.ingredientId &&
          other.subRecipeId == this.subRecipeId &&
          other.grams == this.grams &&
          other.portions == this.portions);
}

class RecipeIngredientsTableCompanion
    extends UpdateCompanion<RecipeIngredientsTableData> {
  final Value<String> id;
  final Value<String> recipeId;
  final Value<String?> ingredientId;
  final Value<String?> subRecipeId;
  final Value<double?> grams;
  final Value<double?> portions;
  final Value<int> rowid;
  const RecipeIngredientsTableCompanion({
    this.id = const Value.absent(),
    this.recipeId = const Value.absent(),
    this.ingredientId = const Value.absent(),
    this.subRecipeId = const Value.absent(),
    this.grams = const Value.absent(),
    this.portions = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  RecipeIngredientsTableCompanion.insert({
    required String id,
    required String recipeId,
    this.ingredientId = const Value.absent(),
    this.subRecipeId = const Value.absent(),
    this.grams = const Value.absent(),
    this.portions = const Value.absent(),
    this.rowid = const Value.absent(),
  })  : id = Value(id),
        recipeId = Value(recipeId);
  static Insertable<RecipeIngredientsTableData> custom({
    Expression<String>? id,
    Expression<String>? recipeId,
    Expression<String>? ingredientId,
    Expression<String>? subRecipeId,
    Expression<double>? grams,
    Expression<double>? portions,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (recipeId != null) 'recipe_id': recipeId,
      if (ingredientId != null) 'ingredient_id': ingredientId,
      if (subRecipeId != null) 'sub_recipe_id': subRecipeId,
      if (grams != null) 'grams': grams,
      if (portions != null) 'portions': portions,
      if (rowid != null) 'rowid': rowid,
    });
  }

  RecipeIngredientsTableCompanion copyWith(
      {Value<String>? id,
      Value<String>? recipeId,
      Value<String?>? ingredientId,
      Value<String?>? subRecipeId,
      Value<double?>? grams,
      Value<double?>? portions,
      Value<int>? rowid}) {
    return RecipeIngredientsTableCompanion(
      id: id ?? this.id,
      recipeId: recipeId ?? this.recipeId,
      ingredientId: ingredientId ?? this.ingredientId,
      subRecipeId: subRecipeId ?? this.subRecipeId,
      grams: grams ?? this.grams,
      portions: portions ?? this.portions,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (recipeId.present) {
      map['recipe_id'] = Variable<String>(recipeId.value);
    }
    if (ingredientId.present) {
      map['ingredient_id'] = Variable<String>(ingredientId.value);
    }
    if (subRecipeId.present) {
      map['sub_recipe_id'] = Variable<String>(subRecipeId.value);
    }
    if (grams.present) {
      map['grams'] = Variable<double>(grams.value);
    }
    if (portions.present) {
      map['portions'] = Variable<double>(portions.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('RecipeIngredientsTableCompanion(')
          ..write('id: $id, ')
          ..write('recipeId: $recipeId, ')
          ..write('ingredientId: $ingredientId, ')
          ..write('subRecipeId: $subRecipeId, ')
          ..write('grams: $grams, ')
          ..write('portions: $portions, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $IngredientsTableTable extends IngredientsTable
    with TableInfo<$IngredientsTableTable, IngredientsTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $IngredientsTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  @override
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
      'name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _categoryMeta =
      const VerificationMeta('category');
  @override
  late final GeneratedColumn<String> category = GeneratedColumn<String>(
      'category', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _kcalPer100gMeta =
      const VerificationMeta('kcalPer100g');
  @override
  late final GeneratedColumn<double> kcalPer100g = GeneratedColumn<double>(
      'kcal_per100g', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _proteinPer100gMeta =
      const VerificationMeta('proteinPer100g');
  @override
  late final GeneratedColumn<double> proteinPer100g = GeneratedColumn<double>(
      'protein_per100g', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _fatPer100gMeta =
      const VerificationMeta('fatPer100g');
  @override
  late final GeneratedColumn<double> fatPer100g = GeneratedColumn<double>(
      'fat_per100g', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _carbsPer100gMeta =
      const VerificationMeta('carbsPer100g');
  @override
  late final GeneratedColumn<double> carbsPer100g = GeneratedColumn<double>(
      'carbs_per100g', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _sourceMeta = const VerificationMeta('source');
  @override
  late final GeneratedColumn<String> source = GeneratedColumn<String>(
      'source', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _stepsMeta = const VerificationMeta('steps');
  @override
  late final GeneratedColumn<String> steps = GeneratedColumn<String>(
      'steps', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  @override
  List<GeneratedColumn> get $columns => [
        id,
        name,
        category,
        kcalPer100g,
        proteinPer100g,
        fatPer100g,
        carbsPer100g,
        source,
        steps
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'ingredients';
  @override
  VerificationContext validateIntegrity(
      Insertable<IngredientsTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('name')) {
      context.handle(
          _nameMeta, name.isAcceptableOrUnknown(data['name']!, _nameMeta));
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('category')) {
      context.handle(_categoryMeta,
          category.isAcceptableOrUnknown(data['category']!, _categoryMeta));
    } else if (isInserting) {
      context.missing(_categoryMeta);
    }
    if (data.containsKey('kcal_per100g')) {
      context.handle(
          _kcalPer100gMeta,
          kcalPer100g.isAcceptableOrUnknown(
              data['kcal_per100g']!, _kcalPer100gMeta));
    }
    if (data.containsKey('protein_per100g')) {
      context.handle(
          _proteinPer100gMeta,
          proteinPer100g.isAcceptableOrUnknown(
              data['protein_per100g']!, _proteinPer100gMeta));
    }
    if (data.containsKey('fat_per100g')) {
      context.handle(
          _fatPer100gMeta,
          fatPer100g.isAcceptableOrUnknown(
              data['fat_per100g']!, _fatPer100gMeta));
    }
    if (data.containsKey('carbs_per100g')) {
      context.handle(
          _carbsPer100gMeta,
          carbsPer100g.isAcceptableOrUnknown(
              data['carbs_per100g']!, _carbsPer100gMeta));
    }
    if (data.containsKey('source')) {
      context.handle(_sourceMeta,
          source.isAcceptableOrUnknown(data['source']!, _sourceMeta));
    } else if (isInserting) {
      context.missing(_sourceMeta);
    }
    if (data.containsKey('steps')) {
      context.handle(
          _stepsMeta, steps.isAcceptableOrUnknown(data['steps']!, _stepsMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  IngredientsTableData map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return IngredientsTableData(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      name: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}name'])!,
      category: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}category'])!,
      kcalPer100g: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}kcal_per100g'])!,
      proteinPer100g: attachedDatabase.typeMapping.read(
          DriftSqlType.double, data['${effectivePrefix}protein_per100g'])!,
      fatPer100g: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}fat_per100g'])!,
      carbsPer100g: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}carbs_per100g'])!,
      source: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}source'])!,
      steps: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}steps'])!,
    );
  }

  @override
  $IngredientsTableTable createAlias(String alias) {
    return $IngredientsTableTable(attachedDatabase, alias);
  }
}

class IngredientsTableData extends DataClass
    implements Insertable<IngredientsTableData> {
  final String id;
  final String name;
  final String category;
  final double kcalPer100g;
  final double proteinPer100g;
  final double fatPer100g;
  final double carbsPer100g;
  final String source;
  final String steps;
  const IngredientsTableData(
      {required this.id,
      required this.name,
      required this.category,
      required this.kcalPer100g,
      required this.proteinPer100g,
      required this.fatPer100g,
      required this.carbsPer100g,
      required this.source,
      required this.steps});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['name'] = Variable<String>(name);
    map['category'] = Variable<String>(category);
    map['kcal_per100g'] = Variable<double>(kcalPer100g);
    map['protein_per100g'] = Variable<double>(proteinPer100g);
    map['fat_per100g'] = Variable<double>(fatPer100g);
    map['carbs_per100g'] = Variable<double>(carbsPer100g);
    map['source'] = Variable<String>(source);
    map['steps'] = Variable<String>(steps);
    return map;
  }

  IngredientsTableCompanion toCompanion(bool nullToAbsent) {
    return IngredientsTableCompanion(
      id: Value(id),
      name: Value(name),
      category: Value(category),
      kcalPer100g: Value(kcalPer100g),
      proteinPer100g: Value(proteinPer100g),
      fatPer100g: Value(fatPer100g),
      carbsPer100g: Value(carbsPer100g),
      source: Value(source),
      steps: Value(steps),
    );
  }

  factory IngredientsTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return IngredientsTableData(
      id: serializer.fromJson<String>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      category: serializer.fromJson<String>(json['category']),
      kcalPer100g: serializer.fromJson<double>(json['kcalPer100g']),
      proteinPer100g: serializer.fromJson<double>(json['proteinPer100g']),
      fatPer100g: serializer.fromJson<double>(json['fatPer100g']),
      carbsPer100g: serializer.fromJson<double>(json['carbsPer100g']),
      source: serializer.fromJson<String>(json['source']),
      steps: serializer.fromJson<String>(json['steps']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'name': serializer.toJson<String>(name),
      'category': serializer.toJson<String>(category),
      'kcalPer100g': serializer.toJson<double>(kcalPer100g),
      'proteinPer100g': serializer.toJson<double>(proteinPer100g),
      'fatPer100g': serializer.toJson<double>(fatPer100g),
      'carbsPer100g': serializer.toJson<double>(carbsPer100g),
      'source': serializer.toJson<String>(source),
      'steps': serializer.toJson<String>(steps),
    };
  }

  IngredientsTableData copyWith(
          {String? id,
          String? name,
          String? category,
          double? kcalPer100g,
          double? proteinPer100g,
          double? fatPer100g,
          double? carbsPer100g,
          String? source,
          String? steps}) =>
      IngredientsTableData(
        id: id ?? this.id,
        name: name ?? this.name,
        category: category ?? this.category,
        kcalPer100g: kcalPer100g ?? this.kcalPer100g,
        proteinPer100g: proteinPer100g ?? this.proteinPer100g,
        fatPer100g: fatPer100g ?? this.fatPer100g,
        carbsPer100g: carbsPer100g ?? this.carbsPer100g,
        source: source ?? this.source,
        steps: steps ?? this.steps,
      );
  IngredientsTableData copyWithCompanion(IngredientsTableCompanion data) {
    return IngredientsTableData(
      id: data.id.present ? data.id.value : this.id,
      name: data.name.present ? data.name.value : this.name,
      category: data.category.present ? data.category.value : this.category,
      kcalPer100g:
          data.kcalPer100g.present ? data.kcalPer100g.value : this.kcalPer100g,
      proteinPer100g: data.proteinPer100g.present
          ? data.proteinPer100g.value
          : this.proteinPer100g,
      fatPer100g:
          data.fatPer100g.present ? data.fatPer100g.value : this.fatPer100g,
      carbsPer100g: data.carbsPer100g.present
          ? data.carbsPer100g.value
          : this.carbsPer100g,
      source: data.source.present ? data.source.value : this.source,
      steps: data.steps.present ? data.steps.value : this.steps,
    );
  }

  @override
  String toString() {
    return (StringBuffer('IngredientsTableData(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('category: $category, ')
          ..write('kcalPer100g: $kcalPer100g, ')
          ..write('proteinPer100g: $proteinPer100g, ')
          ..write('fatPer100g: $fatPer100g, ')
          ..write('carbsPer100g: $carbsPer100g, ')
          ..write('source: $source, ')
          ..write('steps: $steps')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, name, category, kcalPer100g,
      proteinPer100g, fatPer100g, carbsPer100g, source, steps);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is IngredientsTableData &&
          other.id == this.id &&
          other.name == this.name &&
          other.category == this.category &&
          other.kcalPer100g == this.kcalPer100g &&
          other.proteinPer100g == this.proteinPer100g &&
          other.fatPer100g == this.fatPer100g &&
          other.carbsPer100g == this.carbsPer100g &&
          other.source == this.source &&
          other.steps == this.steps);
}

class IngredientsTableCompanion extends UpdateCompanion<IngredientsTableData> {
  final Value<String> id;
  final Value<String> name;
  final Value<String> category;
  final Value<double> kcalPer100g;
  final Value<double> proteinPer100g;
  final Value<double> fatPer100g;
  final Value<double> carbsPer100g;
  final Value<String> source;
  final Value<String> steps;
  final Value<int> rowid;
  const IngredientsTableCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.category = const Value.absent(),
    this.kcalPer100g = const Value.absent(),
    this.proteinPer100g = const Value.absent(),
    this.fatPer100g = const Value.absent(),
    this.carbsPer100g = const Value.absent(),
    this.source = const Value.absent(),
    this.steps = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  IngredientsTableCompanion.insert({
    required String id,
    required String name,
    required String category,
    this.kcalPer100g = const Value.absent(),
    this.proteinPer100g = const Value.absent(),
    this.fatPer100g = const Value.absent(),
    this.carbsPer100g = const Value.absent(),
    required String source,
    this.steps = const Value.absent(),
    this.rowid = const Value.absent(),
  })  : id = Value(id),
        name = Value(name),
        category = Value(category),
        source = Value(source);
  static Insertable<IngredientsTableData> custom({
    Expression<String>? id,
    Expression<String>? name,
    Expression<String>? category,
    Expression<double>? kcalPer100g,
    Expression<double>? proteinPer100g,
    Expression<double>? fatPer100g,
    Expression<double>? carbsPer100g,
    Expression<String>? source,
    Expression<String>? steps,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (category != null) 'category': category,
      if (kcalPer100g != null) 'kcal_per100g': kcalPer100g,
      if (proteinPer100g != null) 'protein_per100g': proteinPer100g,
      if (fatPer100g != null) 'fat_per100g': fatPer100g,
      if (carbsPer100g != null) 'carbs_per100g': carbsPer100g,
      if (source != null) 'source': source,
      if (steps != null) 'steps': steps,
      if (rowid != null) 'rowid': rowid,
    });
  }

  IngredientsTableCompanion copyWith(
      {Value<String>? id,
      Value<String>? name,
      Value<String>? category,
      Value<double>? kcalPer100g,
      Value<double>? proteinPer100g,
      Value<double>? fatPer100g,
      Value<double>? carbsPer100g,
      Value<String>? source,
      Value<String>? steps,
      Value<int>? rowid}) {
    return IngredientsTableCompanion(
      id: id ?? this.id,
      name: name ?? this.name,
      category: category ?? this.category,
      kcalPer100g: kcalPer100g ?? this.kcalPer100g,
      proteinPer100g: proteinPer100g ?? this.proteinPer100g,
      fatPer100g: fatPer100g ?? this.fatPer100g,
      carbsPer100g: carbsPer100g ?? this.carbsPer100g,
      source: source ?? this.source,
      steps: steps ?? this.steps,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (category.present) {
      map['category'] = Variable<String>(category.value);
    }
    if (kcalPer100g.present) {
      map['kcal_per100g'] = Variable<double>(kcalPer100g.value);
    }
    if (proteinPer100g.present) {
      map['protein_per100g'] = Variable<double>(proteinPer100g.value);
    }
    if (fatPer100g.present) {
      map['fat_per100g'] = Variable<double>(fatPer100g.value);
    }
    if (carbsPer100g.present) {
      map['carbs_per100g'] = Variable<double>(carbsPer100g.value);
    }
    if (source.present) {
      map['source'] = Variable<String>(source.value);
    }
    if (steps.present) {
      map['steps'] = Variable<String>(steps.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('IngredientsTableCompanion(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('category: $category, ')
          ..write('kcalPer100g: $kcalPer100g, ')
          ..write('proteinPer100g: $proteinPer100g, ')
          ..write('fatPer100g: $fatPer100g, ')
          ..write('carbsPer100g: $carbsPer100g, ')
          ..write('source: $source, ')
          ..write('steps: $steps, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $MealPlansTableTable extends MealPlansTable
    with TableInfo<$MealPlansTableTable, MealPlansTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $MealPlansTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  @override
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
      'name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _startDateMeta =
      const VerificationMeta('startDate');
  @override
  late final GeneratedColumn<int> startDate = GeneratedColumn<int>(
      'start_date', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _endDateMeta =
      const VerificationMeta('endDate');
  @override
  late final GeneratedColumn<int> endDate = GeneratedColumn<int>(
      'end_date', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  @override
  List<GeneratedColumn> get $columns => [id, name, startDate, endDate];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'meal_plans';
  @override
  VerificationContext validateIntegrity(Insertable<MealPlansTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('name')) {
      context.handle(
          _nameMeta, name.isAcceptableOrUnknown(data['name']!, _nameMeta));
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('start_date')) {
      context.handle(_startDateMeta,
          startDate.isAcceptableOrUnknown(data['start_date']!, _startDateMeta));
    } else if (isInserting) {
      context.missing(_startDateMeta);
    }
    if (data.containsKey('end_date')) {
      context.handle(_endDateMeta,
          endDate.isAcceptableOrUnknown(data['end_date']!, _endDateMeta));
    } else if (isInserting) {
      context.missing(_endDateMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  MealPlansTableData map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return MealPlansTableData(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      name: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}name'])!,
      startDate: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}start_date'])!,
      endDate: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}end_date'])!,
    );
  }

  @override
  $MealPlansTableTable createAlias(String alias) {
    return $MealPlansTableTable(attachedDatabase, alias);
  }
}

class MealPlansTableData extends DataClass
    implements Insertable<MealPlansTableData> {
  final String id;
  final String name;
  final int startDate;
  final int endDate;
  const MealPlansTableData(
      {required this.id,
      required this.name,
      required this.startDate,
      required this.endDate});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['name'] = Variable<String>(name);
    map['start_date'] = Variable<int>(startDate);
    map['end_date'] = Variable<int>(endDate);
    return map;
  }

  MealPlansTableCompanion toCompanion(bool nullToAbsent) {
    return MealPlansTableCompanion(
      id: Value(id),
      name: Value(name),
      startDate: Value(startDate),
      endDate: Value(endDate),
    );
  }

  factory MealPlansTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return MealPlansTableData(
      id: serializer.fromJson<String>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      startDate: serializer.fromJson<int>(json['startDate']),
      endDate: serializer.fromJson<int>(json['endDate']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'name': serializer.toJson<String>(name),
      'startDate': serializer.toJson<int>(startDate),
      'endDate': serializer.toJson<int>(endDate),
    };
  }

  MealPlansTableData copyWith(
          {String? id, String? name, int? startDate, int? endDate}) =>
      MealPlansTableData(
        id: id ?? this.id,
        name: name ?? this.name,
        startDate: startDate ?? this.startDate,
        endDate: endDate ?? this.endDate,
      );
  MealPlansTableData copyWithCompanion(MealPlansTableCompanion data) {
    return MealPlansTableData(
      id: data.id.present ? data.id.value : this.id,
      name: data.name.present ? data.name.value : this.name,
      startDate: data.startDate.present ? data.startDate.value : this.startDate,
      endDate: data.endDate.present ? data.endDate.value : this.endDate,
    );
  }

  @override
  String toString() {
    return (StringBuffer('MealPlansTableData(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('startDate: $startDate, ')
          ..write('endDate: $endDate')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, name, startDate, endDate);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is MealPlansTableData &&
          other.id == this.id &&
          other.name == this.name &&
          other.startDate == this.startDate &&
          other.endDate == this.endDate);
}

class MealPlansTableCompanion extends UpdateCompanion<MealPlansTableData> {
  final Value<String> id;
  final Value<String> name;
  final Value<int> startDate;
  final Value<int> endDate;
  final Value<int> rowid;
  const MealPlansTableCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.startDate = const Value.absent(),
    this.endDate = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  MealPlansTableCompanion.insert({
    required String id,
    required String name,
    required int startDate,
    required int endDate,
    this.rowid = const Value.absent(),
  })  : id = Value(id),
        name = Value(name),
        startDate = Value(startDate),
        endDate = Value(endDate);
  static Insertable<MealPlansTableData> custom({
    Expression<String>? id,
    Expression<String>? name,
    Expression<int>? startDate,
    Expression<int>? endDate,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (startDate != null) 'start_date': startDate,
      if (endDate != null) 'end_date': endDate,
      if (rowid != null) 'rowid': rowid,
    });
  }

  MealPlansTableCompanion copyWith(
      {Value<String>? id,
      Value<String>? name,
      Value<int>? startDate,
      Value<int>? endDate,
      Value<int>? rowid}) {
    return MealPlansTableCompanion(
      id: id ?? this.id,
      name: name ?? this.name,
      startDate: startDate ?? this.startDate,
      endDate: endDate ?? this.endDate,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (startDate.present) {
      map['start_date'] = Variable<int>(startDate.value);
    }
    if (endDate.present) {
      map['end_date'] = Variable<int>(endDate.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('MealPlansTableCompanion(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('startDate: $startDate, ')
          ..write('endDate: $endDate, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $DayPlansTableTable extends DayPlansTable
    with TableInfo<$DayPlansTableTable, DayPlansTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $DayPlansTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _mealPlanIdMeta =
      const VerificationMeta('mealPlanId');
  @override
  late final GeneratedColumn<String> mealPlanId = GeneratedColumn<String>(
      'meal_plan_id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: true,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES meal_plans (id) ON DELETE CASCADE'));
  static const VerificationMeta _dateMeta = const VerificationMeta('date');
  @override
  late final GeneratedColumn<int> date = GeneratedColumn<int>(
      'date', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _proteinPowderGramsMeta =
      const VerificationMeta('proteinPowderGrams');
  @override
  late final GeneratedColumn<double> proteinPowderGrams =
      GeneratedColumn<double>('protein_powder_grams', aliasedName, false,
          type: DriftSqlType.double,
          requiredDuringInsert: false,
          defaultValue: const Constant(0.0));
  static const VerificationMeta _kcalTargetMeta =
      const VerificationMeta('kcalTarget');
  @override
  late final GeneratedColumn<double> kcalTarget = GeneratedColumn<double>(
      'kcal_target', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(1350.0));
  static const VerificationMeta _proteinTargetMeta =
      const VerificationMeta('proteinTarget');
  @override
  late final GeneratedColumn<double> proteinTarget = GeneratedColumn<double>(
      'protein_target', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(120.0));
  @override
  List<GeneratedColumn> get $columns =>
      [id, mealPlanId, date, proteinPowderGrams, kcalTarget, proteinTarget];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'day_plans';
  @override
  VerificationContext validateIntegrity(Insertable<DayPlansTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('meal_plan_id')) {
      context.handle(
          _mealPlanIdMeta,
          mealPlanId.isAcceptableOrUnknown(
              data['meal_plan_id']!, _mealPlanIdMeta));
    } else if (isInserting) {
      context.missing(_mealPlanIdMeta);
    }
    if (data.containsKey('date')) {
      context.handle(
          _dateMeta, date.isAcceptableOrUnknown(data['date']!, _dateMeta));
    } else if (isInserting) {
      context.missing(_dateMeta);
    }
    if (data.containsKey('protein_powder_grams')) {
      context.handle(
          _proteinPowderGramsMeta,
          proteinPowderGrams.isAcceptableOrUnknown(
              data['protein_powder_grams']!, _proteinPowderGramsMeta));
    }
    if (data.containsKey('kcal_target')) {
      context.handle(
          _kcalTargetMeta,
          kcalTarget.isAcceptableOrUnknown(
              data['kcal_target']!, _kcalTargetMeta));
    }
    if (data.containsKey('protein_target')) {
      context.handle(
          _proteinTargetMeta,
          proteinTarget.isAcceptableOrUnknown(
              data['protein_target']!, _proteinTargetMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  DayPlansTableData map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return DayPlansTableData(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      mealPlanId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}meal_plan_id'])!,
      date: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}date'])!,
      proteinPowderGrams: attachedDatabase.typeMapping.read(
          DriftSqlType.double, data['${effectivePrefix}protein_powder_grams'])!,
      kcalTarget: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}kcal_target'])!,
      proteinTarget: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}protein_target'])!,
    );
  }

  @override
  $DayPlansTableTable createAlias(String alias) {
    return $DayPlansTableTable(attachedDatabase, alias);
  }
}

class DayPlansTableData extends DataClass
    implements Insertable<DayPlansTableData> {
  final String id;
  final String mealPlanId;
  final int date;
  final double proteinPowderGrams;
  final double kcalTarget;
  final double proteinTarget;
  const DayPlansTableData(
      {required this.id,
      required this.mealPlanId,
      required this.date,
      required this.proteinPowderGrams,
      required this.kcalTarget,
      required this.proteinTarget});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['meal_plan_id'] = Variable<String>(mealPlanId);
    map['date'] = Variable<int>(date);
    map['protein_powder_grams'] = Variable<double>(proteinPowderGrams);
    map['kcal_target'] = Variable<double>(kcalTarget);
    map['protein_target'] = Variable<double>(proteinTarget);
    return map;
  }

  DayPlansTableCompanion toCompanion(bool nullToAbsent) {
    return DayPlansTableCompanion(
      id: Value(id),
      mealPlanId: Value(mealPlanId),
      date: Value(date),
      proteinPowderGrams: Value(proteinPowderGrams),
      kcalTarget: Value(kcalTarget),
      proteinTarget: Value(proteinTarget),
    );
  }

  factory DayPlansTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return DayPlansTableData(
      id: serializer.fromJson<String>(json['id']),
      mealPlanId: serializer.fromJson<String>(json['mealPlanId']),
      date: serializer.fromJson<int>(json['date']),
      proteinPowderGrams:
          serializer.fromJson<double>(json['proteinPowderGrams']),
      kcalTarget: serializer.fromJson<double>(json['kcalTarget']),
      proteinTarget: serializer.fromJson<double>(json['proteinTarget']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'mealPlanId': serializer.toJson<String>(mealPlanId),
      'date': serializer.toJson<int>(date),
      'proteinPowderGrams': serializer.toJson<double>(proteinPowderGrams),
      'kcalTarget': serializer.toJson<double>(kcalTarget),
      'proteinTarget': serializer.toJson<double>(proteinTarget),
    };
  }

  DayPlansTableData copyWith(
          {String? id,
          String? mealPlanId,
          int? date,
          double? proteinPowderGrams,
          double? kcalTarget,
          double? proteinTarget}) =>
      DayPlansTableData(
        id: id ?? this.id,
        mealPlanId: mealPlanId ?? this.mealPlanId,
        date: date ?? this.date,
        proteinPowderGrams: proteinPowderGrams ?? this.proteinPowderGrams,
        kcalTarget: kcalTarget ?? this.kcalTarget,
        proteinTarget: proteinTarget ?? this.proteinTarget,
      );
  DayPlansTableData copyWithCompanion(DayPlansTableCompanion data) {
    return DayPlansTableData(
      id: data.id.present ? data.id.value : this.id,
      mealPlanId:
          data.mealPlanId.present ? data.mealPlanId.value : this.mealPlanId,
      date: data.date.present ? data.date.value : this.date,
      proteinPowderGrams: data.proteinPowderGrams.present
          ? data.proteinPowderGrams.value
          : this.proteinPowderGrams,
      kcalTarget:
          data.kcalTarget.present ? data.kcalTarget.value : this.kcalTarget,
      proteinTarget: data.proteinTarget.present
          ? data.proteinTarget.value
          : this.proteinTarget,
    );
  }

  @override
  String toString() {
    return (StringBuffer('DayPlansTableData(')
          ..write('id: $id, ')
          ..write('mealPlanId: $mealPlanId, ')
          ..write('date: $date, ')
          ..write('proteinPowderGrams: $proteinPowderGrams, ')
          ..write('kcalTarget: $kcalTarget, ')
          ..write('proteinTarget: $proteinTarget')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id, mealPlanId, date, proteinPowderGrams, kcalTarget, proteinTarget);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is DayPlansTableData &&
          other.id == this.id &&
          other.mealPlanId == this.mealPlanId &&
          other.date == this.date &&
          other.proteinPowderGrams == this.proteinPowderGrams &&
          other.kcalTarget == this.kcalTarget &&
          other.proteinTarget == this.proteinTarget);
}

class DayPlansTableCompanion extends UpdateCompanion<DayPlansTableData> {
  final Value<String> id;
  final Value<String> mealPlanId;
  final Value<int> date;
  final Value<double> proteinPowderGrams;
  final Value<double> kcalTarget;
  final Value<double> proteinTarget;
  final Value<int> rowid;
  const DayPlansTableCompanion({
    this.id = const Value.absent(),
    this.mealPlanId = const Value.absent(),
    this.date = const Value.absent(),
    this.proteinPowderGrams = const Value.absent(),
    this.kcalTarget = const Value.absent(),
    this.proteinTarget = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  DayPlansTableCompanion.insert({
    required String id,
    required String mealPlanId,
    required int date,
    this.proteinPowderGrams = const Value.absent(),
    this.kcalTarget = const Value.absent(),
    this.proteinTarget = const Value.absent(),
    this.rowid = const Value.absent(),
  })  : id = Value(id),
        mealPlanId = Value(mealPlanId),
        date = Value(date);
  static Insertable<DayPlansTableData> custom({
    Expression<String>? id,
    Expression<String>? mealPlanId,
    Expression<int>? date,
    Expression<double>? proteinPowderGrams,
    Expression<double>? kcalTarget,
    Expression<double>? proteinTarget,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (mealPlanId != null) 'meal_plan_id': mealPlanId,
      if (date != null) 'date': date,
      if (proteinPowderGrams != null)
        'protein_powder_grams': proteinPowderGrams,
      if (kcalTarget != null) 'kcal_target': kcalTarget,
      if (proteinTarget != null) 'protein_target': proteinTarget,
      if (rowid != null) 'rowid': rowid,
    });
  }

  DayPlansTableCompanion copyWith(
      {Value<String>? id,
      Value<String>? mealPlanId,
      Value<int>? date,
      Value<double>? proteinPowderGrams,
      Value<double>? kcalTarget,
      Value<double>? proteinTarget,
      Value<int>? rowid}) {
    return DayPlansTableCompanion(
      id: id ?? this.id,
      mealPlanId: mealPlanId ?? this.mealPlanId,
      date: date ?? this.date,
      proteinPowderGrams: proteinPowderGrams ?? this.proteinPowderGrams,
      kcalTarget: kcalTarget ?? this.kcalTarget,
      proteinTarget: proteinTarget ?? this.proteinTarget,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (mealPlanId.present) {
      map['meal_plan_id'] = Variable<String>(mealPlanId.value);
    }
    if (date.present) {
      map['date'] = Variable<int>(date.value);
    }
    if (proteinPowderGrams.present) {
      map['protein_powder_grams'] = Variable<double>(proteinPowderGrams.value);
    }
    if (kcalTarget.present) {
      map['kcal_target'] = Variable<double>(kcalTarget.value);
    }
    if (proteinTarget.present) {
      map['protein_target'] = Variable<double>(proteinTarget.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('DayPlansTableCompanion(')
          ..write('id: $id, ')
          ..write('mealPlanId: $mealPlanId, ')
          ..write('date: $date, ')
          ..write('proteinPowderGrams: $proteinPowderGrams, ')
          ..write('kcalTarget: $kcalTarget, ')
          ..write('proteinTarget: $proteinTarget, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $MealSlotsTableTable extends MealSlotsTable
    with TableInfo<$MealSlotsTableTable, MealSlotsTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $MealSlotsTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _dayPlanIdMeta =
      const VerificationMeta('dayPlanId');
  @override
  late final GeneratedColumn<String> dayPlanId = GeneratedColumn<String>(
      'day_plan_id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: true,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES day_plans (id) ON DELETE CASCADE'));
  static const VerificationMeta _typeMeta = const VerificationMeta('type');
  @override
  late final GeneratedColumn<String> type = GeneratedColumn<String>(
      'type', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _slotIndexMeta =
      const VerificationMeta('slotIndex');
  @override
  late final GeneratedColumn<int> slotIndex = GeneratedColumn<int>(
      'slot_index', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _recipeIdMeta =
      const VerificationMeta('recipeId');
  @override
  late final GeneratedColumn<String> recipeId = GeneratedColumn<String>(
      'recipe_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  @override
  List<GeneratedColumn> get $columns =>
      [id, dayPlanId, type, slotIndex, recipeId];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'meal_slots';
  @override
  VerificationContext validateIntegrity(Insertable<MealSlotsTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('day_plan_id')) {
      context.handle(
          _dayPlanIdMeta,
          dayPlanId.isAcceptableOrUnknown(
              data['day_plan_id']!, _dayPlanIdMeta));
    } else if (isInserting) {
      context.missing(_dayPlanIdMeta);
    }
    if (data.containsKey('type')) {
      context.handle(
          _typeMeta, type.isAcceptableOrUnknown(data['type']!, _typeMeta));
    } else if (isInserting) {
      context.missing(_typeMeta);
    }
    if (data.containsKey('slot_index')) {
      context.handle(_slotIndexMeta,
          slotIndex.isAcceptableOrUnknown(data['slot_index']!, _slotIndexMeta));
    }
    if (data.containsKey('recipe_id')) {
      context.handle(_recipeIdMeta,
          recipeId.isAcceptableOrUnknown(data['recipe_id']!, _recipeIdMeta));
    } else if (isInserting) {
      context.missing(_recipeIdMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  MealSlotsTableData map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return MealSlotsTableData(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      dayPlanId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}day_plan_id'])!,
      type: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}type'])!,
      slotIndex: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}slot_index'])!,
      recipeId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}recipe_id'])!,
    );
  }

  @override
  $MealSlotsTableTable createAlias(String alias) {
    return $MealSlotsTableTable(attachedDatabase, alias);
  }
}

class MealSlotsTableData extends DataClass
    implements Insertable<MealSlotsTableData> {
  final String id;
  final String dayPlanId;
  final String type;
  final int slotIndex;
  final String recipeId;
  const MealSlotsTableData(
      {required this.id,
      required this.dayPlanId,
      required this.type,
      required this.slotIndex,
      required this.recipeId});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['day_plan_id'] = Variable<String>(dayPlanId);
    map['type'] = Variable<String>(type);
    map['slot_index'] = Variable<int>(slotIndex);
    map['recipe_id'] = Variable<String>(recipeId);
    return map;
  }

  MealSlotsTableCompanion toCompanion(bool nullToAbsent) {
    return MealSlotsTableCompanion(
      id: Value(id),
      dayPlanId: Value(dayPlanId),
      type: Value(type),
      slotIndex: Value(slotIndex),
      recipeId: Value(recipeId),
    );
  }

  factory MealSlotsTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return MealSlotsTableData(
      id: serializer.fromJson<String>(json['id']),
      dayPlanId: serializer.fromJson<String>(json['dayPlanId']),
      type: serializer.fromJson<String>(json['type']),
      slotIndex: serializer.fromJson<int>(json['slotIndex']),
      recipeId: serializer.fromJson<String>(json['recipeId']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'dayPlanId': serializer.toJson<String>(dayPlanId),
      'type': serializer.toJson<String>(type),
      'slotIndex': serializer.toJson<int>(slotIndex),
      'recipeId': serializer.toJson<String>(recipeId),
    };
  }

  MealSlotsTableData copyWith(
          {String? id,
          String? dayPlanId,
          String? type,
          int? slotIndex,
          String? recipeId}) =>
      MealSlotsTableData(
        id: id ?? this.id,
        dayPlanId: dayPlanId ?? this.dayPlanId,
        type: type ?? this.type,
        slotIndex: slotIndex ?? this.slotIndex,
        recipeId: recipeId ?? this.recipeId,
      );
  MealSlotsTableData copyWithCompanion(MealSlotsTableCompanion data) {
    return MealSlotsTableData(
      id: data.id.present ? data.id.value : this.id,
      dayPlanId: data.dayPlanId.present ? data.dayPlanId.value : this.dayPlanId,
      type: data.type.present ? data.type.value : this.type,
      slotIndex: data.slotIndex.present ? data.slotIndex.value : this.slotIndex,
      recipeId: data.recipeId.present ? data.recipeId.value : this.recipeId,
    );
  }

  @override
  String toString() {
    return (StringBuffer('MealSlotsTableData(')
          ..write('id: $id, ')
          ..write('dayPlanId: $dayPlanId, ')
          ..write('type: $type, ')
          ..write('slotIndex: $slotIndex, ')
          ..write('recipeId: $recipeId')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, dayPlanId, type, slotIndex, recipeId);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is MealSlotsTableData &&
          other.id == this.id &&
          other.dayPlanId == this.dayPlanId &&
          other.type == this.type &&
          other.slotIndex == this.slotIndex &&
          other.recipeId == this.recipeId);
}

class MealSlotsTableCompanion extends UpdateCompanion<MealSlotsTableData> {
  final Value<String> id;
  final Value<String> dayPlanId;
  final Value<String> type;
  final Value<int> slotIndex;
  final Value<String> recipeId;
  final Value<int> rowid;
  const MealSlotsTableCompanion({
    this.id = const Value.absent(),
    this.dayPlanId = const Value.absent(),
    this.type = const Value.absent(),
    this.slotIndex = const Value.absent(),
    this.recipeId = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  MealSlotsTableCompanion.insert({
    required String id,
    required String dayPlanId,
    required String type,
    this.slotIndex = const Value.absent(),
    required String recipeId,
    this.rowid = const Value.absent(),
  })  : id = Value(id),
        dayPlanId = Value(dayPlanId),
        type = Value(type),
        recipeId = Value(recipeId);
  static Insertable<MealSlotsTableData> custom({
    Expression<String>? id,
    Expression<String>? dayPlanId,
    Expression<String>? type,
    Expression<int>? slotIndex,
    Expression<String>? recipeId,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (dayPlanId != null) 'day_plan_id': dayPlanId,
      if (type != null) 'type': type,
      if (slotIndex != null) 'slot_index': slotIndex,
      if (recipeId != null) 'recipe_id': recipeId,
      if (rowid != null) 'rowid': rowid,
    });
  }

  MealSlotsTableCompanion copyWith(
      {Value<String>? id,
      Value<String>? dayPlanId,
      Value<String>? type,
      Value<int>? slotIndex,
      Value<String>? recipeId,
      Value<int>? rowid}) {
    return MealSlotsTableCompanion(
      id: id ?? this.id,
      dayPlanId: dayPlanId ?? this.dayPlanId,
      type: type ?? this.type,
      slotIndex: slotIndex ?? this.slotIndex,
      recipeId: recipeId ?? this.recipeId,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (dayPlanId.present) {
      map['day_plan_id'] = Variable<String>(dayPlanId.value);
    }
    if (type.present) {
      map['type'] = Variable<String>(type.value);
    }
    if (slotIndex.present) {
      map['slot_index'] = Variable<int>(slotIndex.value);
    }
    if (recipeId.present) {
      map['recipe_id'] = Variable<String>(recipeId.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('MealSlotsTableCompanion(')
          ..write('id: $id, ')
          ..write('dayPlanId: $dayPlanId, ')
          ..write('type: $type, ')
          ..write('slotIndex: $slotIndex, ')
          ..write('recipeId: $recipeId, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $RecipeRatingsTableTable extends RecipeRatingsTable
    with TableInfo<$RecipeRatingsTableTable, RecipeRatingsTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $RecipeRatingsTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _recipeIdMeta =
      const VerificationMeta('recipeId');
  @override
  late final GeneratedColumn<String> recipeId = GeneratedColumn<String>(
      'recipe_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _starsMeta = const VerificationMeta('stars');
  @override
  late final GeneratedColumn<int> stars = GeneratedColumn<int>(
      'stars', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _timesScheduledMeta =
      const VerificationMeta('timesScheduled');
  @override
  late final GeneratedColumn<int> timesScheduled = GeneratedColumn<int>(
      'times_scheduled', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _timesManuallyRemovedMeta =
      const VerificationMeta('timesManuallyRemoved');
  @override
  late final GeneratedColumn<int> timesManuallyRemoved = GeneratedColumn<int>(
      'times_manually_removed', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _isPinnedMeta =
      const VerificationMeta('isPinned');
  @override
  late final GeneratedColumn<bool> isPinned = GeneratedColumn<bool>(
      'is_pinned', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_pinned" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _isExcludedMeta =
      const VerificationMeta('isExcluded');
  @override
  late final GeneratedColumn<bool> isExcluded = GeneratedColumn<bool>(
      'is_excluded', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_excluded" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _lastScheduledDateMeta =
      const VerificationMeta('lastScheduledDate');
  @override
  late final GeneratedColumn<int> lastScheduledDate = GeneratedColumn<int>(
      'last_scheduled_date', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        recipeId,
        stars,
        timesScheduled,
        timesManuallyRemoved,
        isPinned,
        isExcluded,
        lastScheduledDate
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'recipe_ratings';
  @override
  VerificationContext validateIntegrity(
      Insertable<RecipeRatingsTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('recipe_id')) {
      context.handle(_recipeIdMeta,
          recipeId.isAcceptableOrUnknown(data['recipe_id']!, _recipeIdMeta));
    } else if (isInserting) {
      context.missing(_recipeIdMeta);
    }
    if (data.containsKey('stars')) {
      context.handle(
          _starsMeta, stars.isAcceptableOrUnknown(data['stars']!, _starsMeta));
    }
    if (data.containsKey('times_scheduled')) {
      context.handle(
          _timesScheduledMeta,
          timesScheduled.isAcceptableOrUnknown(
              data['times_scheduled']!, _timesScheduledMeta));
    }
    if (data.containsKey('times_manually_removed')) {
      context.handle(
          _timesManuallyRemovedMeta,
          timesManuallyRemoved.isAcceptableOrUnknown(
              data['times_manually_removed']!, _timesManuallyRemovedMeta));
    }
    if (data.containsKey('is_pinned')) {
      context.handle(_isPinnedMeta,
          isPinned.isAcceptableOrUnknown(data['is_pinned']!, _isPinnedMeta));
    }
    if (data.containsKey('is_excluded')) {
      context.handle(
          _isExcludedMeta,
          isExcluded.isAcceptableOrUnknown(
              data['is_excluded']!, _isExcludedMeta));
    }
    if (data.containsKey('last_scheduled_date')) {
      context.handle(
          _lastScheduledDateMeta,
          lastScheduledDate.isAcceptableOrUnknown(
              data['last_scheduled_date']!, _lastScheduledDateMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {recipeId};
  @override
  RecipeRatingsTableData map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return RecipeRatingsTableData(
      recipeId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}recipe_id'])!,
      stars: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}stars']),
      timesScheduled: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}times_scheduled'])!,
      timesManuallyRemoved: attachedDatabase.typeMapping.read(
          DriftSqlType.int, data['${effectivePrefix}times_manually_removed'])!,
      isPinned: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_pinned'])!,
      isExcluded: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_excluded'])!,
      lastScheduledDate: attachedDatabase.typeMapping.read(
          DriftSqlType.int, data['${effectivePrefix}last_scheduled_date']),
    );
  }

  @override
  $RecipeRatingsTableTable createAlias(String alias) {
    return $RecipeRatingsTableTable(attachedDatabase, alias);
  }
}

class RecipeRatingsTableData extends DataClass
    implements Insertable<RecipeRatingsTableData> {
  final String recipeId;
  final int? stars;
  final int timesScheduled;
  final int timesManuallyRemoved;
  final bool isPinned;
  final bool isExcluded;
  final int? lastScheduledDate;
  const RecipeRatingsTableData(
      {required this.recipeId,
      this.stars,
      required this.timesScheduled,
      required this.timesManuallyRemoved,
      required this.isPinned,
      required this.isExcluded,
      this.lastScheduledDate});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['recipe_id'] = Variable<String>(recipeId);
    if (!nullToAbsent || stars != null) {
      map['stars'] = Variable<int>(stars);
    }
    map['times_scheduled'] = Variable<int>(timesScheduled);
    map['times_manually_removed'] = Variable<int>(timesManuallyRemoved);
    map['is_pinned'] = Variable<bool>(isPinned);
    map['is_excluded'] = Variable<bool>(isExcluded);
    if (!nullToAbsent || lastScheduledDate != null) {
      map['last_scheduled_date'] = Variable<int>(lastScheduledDate);
    }
    return map;
  }

  RecipeRatingsTableCompanion toCompanion(bool nullToAbsent) {
    return RecipeRatingsTableCompanion(
      recipeId: Value(recipeId),
      stars:
          stars == null && nullToAbsent ? const Value.absent() : Value(stars),
      timesScheduled: Value(timesScheduled),
      timesManuallyRemoved: Value(timesManuallyRemoved),
      isPinned: Value(isPinned),
      isExcluded: Value(isExcluded),
      lastScheduledDate: lastScheduledDate == null && nullToAbsent
          ? const Value.absent()
          : Value(lastScheduledDate),
    );
  }

  factory RecipeRatingsTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return RecipeRatingsTableData(
      recipeId: serializer.fromJson<String>(json['recipeId']),
      stars: serializer.fromJson<int?>(json['stars']),
      timesScheduled: serializer.fromJson<int>(json['timesScheduled']),
      timesManuallyRemoved:
          serializer.fromJson<int>(json['timesManuallyRemoved']),
      isPinned: serializer.fromJson<bool>(json['isPinned']),
      isExcluded: serializer.fromJson<bool>(json['isExcluded']),
      lastScheduledDate: serializer.fromJson<int?>(json['lastScheduledDate']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'recipeId': serializer.toJson<String>(recipeId),
      'stars': serializer.toJson<int?>(stars),
      'timesScheduled': serializer.toJson<int>(timesScheduled),
      'timesManuallyRemoved': serializer.toJson<int>(timesManuallyRemoved),
      'isPinned': serializer.toJson<bool>(isPinned),
      'isExcluded': serializer.toJson<bool>(isExcluded),
      'lastScheduledDate': serializer.toJson<int?>(lastScheduledDate),
    };
  }

  RecipeRatingsTableData copyWith(
          {String? recipeId,
          Value<int?> stars = const Value.absent(),
          int? timesScheduled,
          int? timesManuallyRemoved,
          bool? isPinned,
          bool? isExcluded,
          Value<int?> lastScheduledDate = const Value.absent()}) =>
      RecipeRatingsTableData(
        recipeId: recipeId ?? this.recipeId,
        stars: stars.present ? stars.value : this.stars,
        timesScheduled: timesScheduled ?? this.timesScheduled,
        timesManuallyRemoved: timesManuallyRemoved ?? this.timesManuallyRemoved,
        isPinned: isPinned ?? this.isPinned,
        isExcluded: isExcluded ?? this.isExcluded,
        lastScheduledDate: lastScheduledDate.present
            ? lastScheduledDate.value
            : this.lastScheduledDate,
      );
  RecipeRatingsTableData copyWithCompanion(RecipeRatingsTableCompanion data) {
    return RecipeRatingsTableData(
      recipeId: data.recipeId.present ? data.recipeId.value : this.recipeId,
      stars: data.stars.present ? data.stars.value : this.stars,
      timesScheduled: data.timesScheduled.present
          ? data.timesScheduled.value
          : this.timesScheduled,
      timesManuallyRemoved: data.timesManuallyRemoved.present
          ? data.timesManuallyRemoved.value
          : this.timesManuallyRemoved,
      isPinned: data.isPinned.present ? data.isPinned.value : this.isPinned,
      isExcluded:
          data.isExcluded.present ? data.isExcluded.value : this.isExcluded,
      lastScheduledDate: data.lastScheduledDate.present
          ? data.lastScheduledDate.value
          : this.lastScheduledDate,
    );
  }

  @override
  String toString() {
    return (StringBuffer('RecipeRatingsTableData(')
          ..write('recipeId: $recipeId, ')
          ..write('stars: $stars, ')
          ..write('timesScheduled: $timesScheduled, ')
          ..write('timesManuallyRemoved: $timesManuallyRemoved, ')
          ..write('isPinned: $isPinned, ')
          ..write('isExcluded: $isExcluded, ')
          ..write('lastScheduledDate: $lastScheduledDate')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(recipeId, stars, timesScheduled,
      timesManuallyRemoved, isPinned, isExcluded, lastScheduledDate);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is RecipeRatingsTableData &&
          other.recipeId == this.recipeId &&
          other.stars == this.stars &&
          other.timesScheduled == this.timesScheduled &&
          other.timesManuallyRemoved == this.timesManuallyRemoved &&
          other.isPinned == this.isPinned &&
          other.isExcluded == this.isExcluded &&
          other.lastScheduledDate == this.lastScheduledDate);
}

class RecipeRatingsTableCompanion
    extends UpdateCompanion<RecipeRatingsTableData> {
  final Value<String> recipeId;
  final Value<int?> stars;
  final Value<int> timesScheduled;
  final Value<int> timesManuallyRemoved;
  final Value<bool> isPinned;
  final Value<bool> isExcluded;
  final Value<int?> lastScheduledDate;
  final Value<int> rowid;
  const RecipeRatingsTableCompanion({
    this.recipeId = const Value.absent(),
    this.stars = const Value.absent(),
    this.timesScheduled = const Value.absent(),
    this.timesManuallyRemoved = const Value.absent(),
    this.isPinned = const Value.absent(),
    this.isExcluded = const Value.absent(),
    this.lastScheduledDate = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  RecipeRatingsTableCompanion.insert({
    required String recipeId,
    this.stars = const Value.absent(),
    this.timesScheduled = const Value.absent(),
    this.timesManuallyRemoved = const Value.absent(),
    this.isPinned = const Value.absent(),
    this.isExcluded = const Value.absent(),
    this.lastScheduledDate = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : recipeId = Value(recipeId);
  static Insertable<RecipeRatingsTableData> custom({
    Expression<String>? recipeId,
    Expression<int>? stars,
    Expression<int>? timesScheduled,
    Expression<int>? timesManuallyRemoved,
    Expression<bool>? isPinned,
    Expression<bool>? isExcluded,
    Expression<int>? lastScheduledDate,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (recipeId != null) 'recipe_id': recipeId,
      if (stars != null) 'stars': stars,
      if (timesScheduled != null) 'times_scheduled': timesScheduled,
      if (timesManuallyRemoved != null)
        'times_manually_removed': timesManuallyRemoved,
      if (isPinned != null) 'is_pinned': isPinned,
      if (isExcluded != null) 'is_excluded': isExcluded,
      if (lastScheduledDate != null) 'last_scheduled_date': lastScheduledDate,
      if (rowid != null) 'rowid': rowid,
    });
  }

  RecipeRatingsTableCompanion copyWith(
      {Value<String>? recipeId,
      Value<int?>? stars,
      Value<int>? timesScheduled,
      Value<int>? timesManuallyRemoved,
      Value<bool>? isPinned,
      Value<bool>? isExcluded,
      Value<int?>? lastScheduledDate,
      Value<int>? rowid}) {
    return RecipeRatingsTableCompanion(
      recipeId: recipeId ?? this.recipeId,
      stars: stars ?? this.stars,
      timesScheduled: timesScheduled ?? this.timesScheduled,
      timesManuallyRemoved: timesManuallyRemoved ?? this.timesManuallyRemoved,
      isPinned: isPinned ?? this.isPinned,
      isExcluded: isExcluded ?? this.isExcluded,
      lastScheduledDate: lastScheduledDate ?? this.lastScheduledDate,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (recipeId.present) {
      map['recipe_id'] = Variable<String>(recipeId.value);
    }
    if (stars.present) {
      map['stars'] = Variable<int>(stars.value);
    }
    if (timesScheduled.present) {
      map['times_scheduled'] = Variable<int>(timesScheduled.value);
    }
    if (timesManuallyRemoved.present) {
      map['times_manually_removed'] = Variable<int>(timesManuallyRemoved.value);
    }
    if (isPinned.present) {
      map['is_pinned'] = Variable<bool>(isPinned.value);
    }
    if (isExcluded.present) {
      map['is_excluded'] = Variable<bool>(isExcluded.value);
    }
    if (lastScheduledDate.present) {
      map['last_scheduled_date'] = Variable<int>(lastScheduledDate.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('RecipeRatingsTableCompanion(')
          ..write('recipeId: $recipeId, ')
          ..write('stars: $stars, ')
          ..write('timesScheduled: $timesScheduled, ')
          ..write('timesManuallyRemoved: $timesManuallyRemoved, ')
          ..write('isPinned: $isPinned, ')
          ..write('isExcluded: $isExcluded, ')
          ..write('lastScheduledDate: $lastScheduledDate, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $MealSlotConfigsTableTable extends MealSlotConfigsTable
    with TableInfo<$MealSlotConfigsTableTable, MealSlotConfigsTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $MealSlotConfigsTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _dayOfWeekMeta =
      const VerificationMeta('dayOfWeek');
  @override
  late final GeneratedColumn<int> dayOfWeek = GeneratedColumn<int>(
      'day_of_week', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _hasBreakfastMeta =
      const VerificationMeta('hasBreakfast');
  @override
  late final GeneratedColumn<bool> hasBreakfast = GeneratedColumn<bool>(
      'has_breakfast', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'CHECK ("has_breakfast" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _hasLunchMeta =
      const VerificationMeta('hasLunch');
  @override
  late final GeneratedColumn<bool> hasLunch = GeneratedColumn<bool>(
      'has_lunch', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("has_lunch" IN (0, 1))'),
      defaultValue: const Constant(true));
  static const VerificationMeta _hasDinnerMeta =
      const VerificationMeta('hasDinner');
  @override
  late final GeneratedColumn<bool> hasDinner = GeneratedColumn<bool>(
      'has_dinner', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("has_dinner" IN (0, 1))'),
      defaultValue: const Constant(true));
  static const VerificationMeta _snackCountMeta =
      const VerificationMeta('snackCount');
  @override
  late final GeneratedColumn<int> snackCount = GeneratedColumn<int>(
      'snack_count', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  @override
  List<GeneratedColumn> get $columns =>
      [dayOfWeek, hasBreakfast, hasLunch, hasDinner, snackCount];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'meal_slot_configs';
  @override
  VerificationContext validateIntegrity(
      Insertable<MealSlotConfigsTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('day_of_week')) {
      context.handle(
          _dayOfWeekMeta,
          dayOfWeek.isAcceptableOrUnknown(
              data['day_of_week']!, _dayOfWeekMeta));
    }
    if (data.containsKey('has_breakfast')) {
      context.handle(
          _hasBreakfastMeta,
          hasBreakfast.isAcceptableOrUnknown(
              data['has_breakfast']!, _hasBreakfastMeta));
    }
    if (data.containsKey('has_lunch')) {
      context.handle(_hasLunchMeta,
          hasLunch.isAcceptableOrUnknown(data['has_lunch']!, _hasLunchMeta));
    }
    if (data.containsKey('has_dinner')) {
      context.handle(_hasDinnerMeta,
          hasDinner.isAcceptableOrUnknown(data['has_dinner']!, _hasDinnerMeta));
    }
    if (data.containsKey('snack_count')) {
      context.handle(
          _snackCountMeta,
          snackCount.isAcceptableOrUnknown(
              data['snack_count']!, _snackCountMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {dayOfWeek};
  @override
  MealSlotConfigsTableData map(Map<String, dynamic> data,
      {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return MealSlotConfigsTableData(
      dayOfWeek: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}day_of_week'])!,
      hasBreakfast: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}has_breakfast'])!,
      hasLunch: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}has_lunch'])!,
      hasDinner: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}has_dinner'])!,
      snackCount: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}snack_count'])!,
    );
  }

  @override
  $MealSlotConfigsTableTable createAlias(String alias) {
    return $MealSlotConfigsTableTable(attachedDatabase, alias);
  }
}

class MealSlotConfigsTableData extends DataClass
    implements Insertable<MealSlotConfigsTableData> {
  final int dayOfWeek;
  final bool hasBreakfast;
  final bool hasLunch;
  final bool hasDinner;
  final int snackCount;
  const MealSlotConfigsTableData(
      {required this.dayOfWeek,
      required this.hasBreakfast,
      required this.hasLunch,
      required this.hasDinner,
      required this.snackCount});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['day_of_week'] = Variable<int>(dayOfWeek);
    map['has_breakfast'] = Variable<bool>(hasBreakfast);
    map['has_lunch'] = Variable<bool>(hasLunch);
    map['has_dinner'] = Variable<bool>(hasDinner);
    map['snack_count'] = Variable<int>(snackCount);
    return map;
  }

  MealSlotConfigsTableCompanion toCompanion(bool nullToAbsent) {
    return MealSlotConfigsTableCompanion(
      dayOfWeek: Value(dayOfWeek),
      hasBreakfast: Value(hasBreakfast),
      hasLunch: Value(hasLunch),
      hasDinner: Value(hasDinner),
      snackCount: Value(snackCount),
    );
  }

  factory MealSlotConfigsTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return MealSlotConfigsTableData(
      dayOfWeek: serializer.fromJson<int>(json['dayOfWeek']),
      hasBreakfast: serializer.fromJson<bool>(json['hasBreakfast']),
      hasLunch: serializer.fromJson<bool>(json['hasLunch']),
      hasDinner: serializer.fromJson<bool>(json['hasDinner']),
      snackCount: serializer.fromJson<int>(json['snackCount']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'dayOfWeek': serializer.toJson<int>(dayOfWeek),
      'hasBreakfast': serializer.toJson<bool>(hasBreakfast),
      'hasLunch': serializer.toJson<bool>(hasLunch),
      'hasDinner': serializer.toJson<bool>(hasDinner),
      'snackCount': serializer.toJson<int>(snackCount),
    };
  }

  MealSlotConfigsTableData copyWith(
          {int? dayOfWeek,
          bool? hasBreakfast,
          bool? hasLunch,
          bool? hasDinner,
          int? snackCount}) =>
      MealSlotConfigsTableData(
        dayOfWeek: dayOfWeek ?? this.dayOfWeek,
        hasBreakfast: hasBreakfast ?? this.hasBreakfast,
        hasLunch: hasLunch ?? this.hasLunch,
        hasDinner: hasDinner ?? this.hasDinner,
        snackCount: snackCount ?? this.snackCount,
      );
  MealSlotConfigsTableData copyWithCompanion(
      MealSlotConfigsTableCompanion data) {
    return MealSlotConfigsTableData(
      dayOfWeek: data.dayOfWeek.present ? data.dayOfWeek.value : this.dayOfWeek,
      hasBreakfast: data.hasBreakfast.present
          ? data.hasBreakfast.value
          : this.hasBreakfast,
      hasLunch: data.hasLunch.present ? data.hasLunch.value : this.hasLunch,
      hasDinner: data.hasDinner.present ? data.hasDinner.value : this.hasDinner,
      snackCount:
          data.snackCount.present ? data.snackCount.value : this.snackCount,
    );
  }

  @override
  String toString() {
    return (StringBuffer('MealSlotConfigsTableData(')
          ..write('dayOfWeek: $dayOfWeek, ')
          ..write('hasBreakfast: $hasBreakfast, ')
          ..write('hasLunch: $hasLunch, ')
          ..write('hasDinner: $hasDinner, ')
          ..write('snackCount: $snackCount')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(dayOfWeek, hasBreakfast, hasLunch, hasDinner, snackCount);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is MealSlotConfigsTableData &&
          other.dayOfWeek == this.dayOfWeek &&
          other.hasBreakfast == this.hasBreakfast &&
          other.hasLunch == this.hasLunch &&
          other.hasDinner == this.hasDinner &&
          other.snackCount == this.snackCount);
}

class MealSlotConfigsTableCompanion
    extends UpdateCompanion<MealSlotConfigsTableData> {
  final Value<int> dayOfWeek;
  final Value<bool> hasBreakfast;
  final Value<bool> hasLunch;
  final Value<bool> hasDinner;
  final Value<int> snackCount;
  const MealSlotConfigsTableCompanion({
    this.dayOfWeek = const Value.absent(),
    this.hasBreakfast = const Value.absent(),
    this.hasLunch = const Value.absent(),
    this.hasDinner = const Value.absent(),
    this.snackCount = const Value.absent(),
  });
  MealSlotConfigsTableCompanion.insert({
    this.dayOfWeek = const Value.absent(),
    this.hasBreakfast = const Value.absent(),
    this.hasLunch = const Value.absent(),
    this.hasDinner = const Value.absent(),
    this.snackCount = const Value.absent(),
  });
  static Insertable<MealSlotConfigsTableData> custom({
    Expression<int>? dayOfWeek,
    Expression<bool>? hasBreakfast,
    Expression<bool>? hasLunch,
    Expression<bool>? hasDinner,
    Expression<int>? snackCount,
  }) {
    return RawValuesInsertable({
      if (dayOfWeek != null) 'day_of_week': dayOfWeek,
      if (hasBreakfast != null) 'has_breakfast': hasBreakfast,
      if (hasLunch != null) 'has_lunch': hasLunch,
      if (hasDinner != null) 'has_dinner': hasDinner,
      if (snackCount != null) 'snack_count': snackCount,
    });
  }

  MealSlotConfigsTableCompanion copyWith(
      {Value<int>? dayOfWeek,
      Value<bool>? hasBreakfast,
      Value<bool>? hasLunch,
      Value<bool>? hasDinner,
      Value<int>? snackCount}) {
    return MealSlotConfigsTableCompanion(
      dayOfWeek: dayOfWeek ?? this.dayOfWeek,
      hasBreakfast: hasBreakfast ?? this.hasBreakfast,
      hasLunch: hasLunch ?? this.hasLunch,
      hasDinner: hasDinner ?? this.hasDinner,
      snackCount: snackCount ?? this.snackCount,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (dayOfWeek.present) {
      map['day_of_week'] = Variable<int>(dayOfWeek.value);
    }
    if (hasBreakfast.present) {
      map['has_breakfast'] = Variable<bool>(hasBreakfast.value);
    }
    if (hasLunch.present) {
      map['has_lunch'] = Variable<bool>(hasLunch.value);
    }
    if (hasDinner.present) {
      map['has_dinner'] = Variable<bool>(hasDinner.value);
    }
    if (snackCount.present) {
      map['snack_count'] = Variable<int>(snackCount.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('MealSlotConfigsTableCompanion(')
          ..write('dayOfWeek: $dayOfWeek, ')
          ..write('hasBreakfast: $hasBreakfast, ')
          ..write('hasLunch: $hasLunch, ')
          ..write('hasDinner: $hasDinner, ')
          ..write('snackCount: $snackCount')
          ..write(')'))
        .toString();
  }
}

class $BatchCookingGroupsTableTable extends BatchCookingGroupsTable
    with TableInfo<$BatchCookingGroupsTableTable, BatchCookingGroupsTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $BatchCookingGroupsTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _categoryMeta =
      const VerificationMeta('category');
  @override
  late final GeneratedColumn<String> category = GeneratedColumn<String>(
      'category', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _daysMeta = const VerificationMeta('days');
  @override
  late final GeneratedColumn<String> days = GeneratedColumn<String>(
      'days', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _batchNumberMeta =
      const VerificationMeta('batchNumber');
  @override
  late final GeneratedColumn<int> batchNumber = GeneratedColumn<int>(
      'batch_number', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  @override
  List<GeneratedColumn> get $columns => [id, category, days, batchNumber];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'batch_cooking_groups';
  @override
  VerificationContext validateIntegrity(
      Insertable<BatchCookingGroupsTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('category')) {
      context.handle(_categoryMeta,
          category.isAcceptableOrUnknown(data['category']!, _categoryMeta));
    } else if (isInserting) {
      context.missing(_categoryMeta);
    }
    if (data.containsKey('days')) {
      context.handle(
          _daysMeta, days.isAcceptableOrUnknown(data['days']!, _daysMeta));
    } else if (isInserting) {
      context.missing(_daysMeta);
    }
    if (data.containsKey('batch_number')) {
      context.handle(
          _batchNumberMeta,
          batchNumber.isAcceptableOrUnknown(
              data['batch_number']!, _batchNumberMeta));
    } else if (isInserting) {
      context.missing(_batchNumberMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  BatchCookingGroupsTableData map(Map<String, dynamic> data,
      {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return BatchCookingGroupsTableData(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      category: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}category'])!,
      days: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}days'])!,
      batchNumber: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}batch_number'])!,
    );
  }

  @override
  $BatchCookingGroupsTableTable createAlias(String alias) {
    return $BatchCookingGroupsTableTable(attachedDatabase, alias);
  }
}

class BatchCookingGroupsTableData extends DataClass
    implements Insertable<BatchCookingGroupsTableData> {
  final String id;
  final String category;
  final String days;
  final int batchNumber;
  const BatchCookingGroupsTableData(
      {required this.id,
      required this.category,
      required this.days,
      required this.batchNumber});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['category'] = Variable<String>(category);
    map['days'] = Variable<String>(days);
    map['batch_number'] = Variable<int>(batchNumber);
    return map;
  }

  BatchCookingGroupsTableCompanion toCompanion(bool nullToAbsent) {
    return BatchCookingGroupsTableCompanion(
      id: Value(id),
      category: Value(category),
      days: Value(days),
      batchNumber: Value(batchNumber),
    );
  }

  factory BatchCookingGroupsTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return BatchCookingGroupsTableData(
      id: serializer.fromJson<String>(json['id']),
      category: serializer.fromJson<String>(json['category']),
      days: serializer.fromJson<String>(json['days']),
      batchNumber: serializer.fromJson<int>(json['batchNumber']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'category': serializer.toJson<String>(category),
      'days': serializer.toJson<String>(days),
      'batchNumber': serializer.toJson<int>(batchNumber),
    };
  }

  BatchCookingGroupsTableData copyWith(
          {String? id, String? category, String? days, int? batchNumber}) =>
      BatchCookingGroupsTableData(
        id: id ?? this.id,
        category: category ?? this.category,
        days: days ?? this.days,
        batchNumber: batchNumber ?? this.batchNumber,
      );
  BatchCookingGroupsTableData copyWithCompanion(
      BatchCookingGroupsTableCompanion data) {
    return BatchCookingGroupsTableData(
      id: data.id.present ? data.id.value : this.id,
      category: data.category.present ? data.category.value : this.category,
      days: data.days.present ? data.days.value : this.days,
      batchNumber:
          data.batchNumber.present ? data.batchNumber.value : this.batchNumber,
    );
  }

  @override
  String toString() {
    return (StringBuffer('BatchCookingGroupsTableData(')
          ..write('id: $id, ')
          ..write('category: $category, ')
          ..write('days: $days, ')
          ..write('batchNumber: $batchNumber')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, category, days, batchNumber);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is BatchCookingGroupsTableData &&
          other.id == this.id &&
          other.category == this.category &&
          other.days == this.days &&
          other.batchNumber == this.batchNumber);
}

class BatchCookingGroupsTableCompanion
    extends UpdateCompanion<BatchCookingGroupsTableData> {
  final Value<String> id;
  final Value<String> category;
  final Value<String> days;
  final Value<int> batchNumber;
  final Value<int> rowid;
  const BatchCookingGroupsTableCompanion({
    this.id = const Value.absent(),
    this.category = const Value.absent(),
    this.days = const Value.absent(),
    this.batchNumber = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  BatchCookingGroupsTableCompanion.insert({
    required String id,
    required String category,
    required String days,
    required int batchNumber,
    this.rowid = const Value.absent(),
  })  : id = Value(id),
        category = Value(category),
        days = Value(days),
        batchNumber = Value(batchNumber);
  static Insertable<BatchCookingGroupsTableData> custom({
    Expression<String>? id,
    Expression<String>? category,
    Expression<String>? days,
    Expression<int>? batchNumber,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (category != null) 'category': category,
      if (days != null) 'days': days,
      if (batchNumber != null) 'batch_number': batchNumber,
      if (rowid != null) 'rowid': rowid,
    });
  }

  BatchCookingGroupsTableCompanion copyWith(
      {Value<String>? id,
      Value<String>? category,
      Value<String>? days,
      Value<int>? batchNumber,
      Value<int>? rowid}) {
    return BatchCookingGroupsTableCompanion(
      id: id ?? this.id,
      category: category ?? this.category,
      days: days ?? this.days,
      batchNumber: batchNumber ?? this.batchNumber,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (category.present) {
      map['category'] = Variable<String>(category.value);
    }
    if (days.present) {
      map['days'] = Variable<String>(days.value);
    }
    if (batchNumber.present) {
      map['batch_number'] = Variable<int>(batchNumber.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('BatchCookingGroupsTableCompanion(')
          ..write('id: $id, ')
          ..write('category: $category, ')
          ..write('days: $days, ')
          ..write('batchNumber: $batchNumber, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $OptimizerRulesTableTable extends OptimizerRulesTable
    with TableInfo<$OptimizerRulesTableTable, OptimizerRulesTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $OptimizerRulesTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _typeMeta = const VerificationMeta('type');
  @override
  late final GeneratedColumn<String> type = GeneratedColumn<String>(
      'type', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _targetMeta = const VerificationMeta('target');
  @override
  late final GeneratedColumn<String> target = GeneratedColumn<String>(
      'target', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _targetNameMeta =
      const VerificationMeta('targetName');
  @override
  late final GeneratedColumn<String> targetName = GeneratedColumn<String>(
      'target_name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _constraintMeta =
      const VerificationMeta('constraint');
  @override
  late final GeneratedColumn<String> constraint = GeneratedColumn<String>(
      'constraint', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _valueMeta = const VerificationMeta('value');
  @override
  late final GeneratedColumn<int> value = GeneratedColumn<int>(
      'value', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  @override
  List<GeneratedColumn> get $columns =>
      [id, type, target, targetName, constraint, value];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'optimizer_rules';
  @override
  VerificationContext validateIntegrity(
      Insertable<OptimizerRulesTableData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('type')) {
      context.handle(
          _typeMeta, type.isAcceptableOrUnknown(data['type']!, _typeMeta));
    } else if (isInserting) {
      context.missing(_typeMeta);
    }
    if (data.containsKey('target')) {
      context.handle(_targetMeta,
          target.isAcceptableOrUnknown(data['target']!, _targetMeta));
    } else if (isInserting) {
      context.missing(_targetMeta);
    }
    if (data.containsKey('target_name')) {
      context.handle(
          _targetNameMeta,
          targetName.isAcceptableOrUnknown(
              data['target_name']!, _targetNameMeta));
    } else if (isInserting) {
      context.missing(_targetNameMeta);
    }
    if (data.containsKey('constraint')) {
      context.handle(
          _constraintMeta,
          constraint.isAcceptableOrUnknown(
              data['constraint']!, _constraintMeta));
    } else if (isInserting) {
      context.missing(_constraintMeta);
    }
    if (data.containsKey('value')) {
      context.handle(
          _valueMeta, value.isAcceptableOrUnknown(data['value']!, _valueMeta));
    } else if (isInserting) {
      context.missing(_valueMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  OptimizerRulesTableData map(Map<String, dynamic> data,
      {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return OptimizerRulesTableData(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      type: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}type'])!,
      target: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}target'])!,
      targetName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}target_name'])!,
      constraint: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}constraint'])!,
      value: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}value'])!,
    );
  }

  @override
  $OptimizerRulesTableTable createAlias(String alias) {
    return $OptimizerRulesTableTable(attachedDatabase, alias);
  }
}

class OptimizerRulesTableData extends DataClass
    implements Insertable<OptimizerRulesTableData> {
  final String id;
  final String type;
  final String target;
  final String targetName;
  final String constraint;
  final int value;
  const OptimizerRulesTableData(
      {required this.id,
      required this.type,
      required this.target,
      required this.targetName,
      required this.constraint,
      required this.value});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['type'] = Variable<String>(type);
    map['target'] = Variable<String>(target);
    map['target_name'] = Variable<String>(targetName);
    map['constraint'] = Variable<String>(constraint);
    map['value'] = Variable<int>(value);
    return map;
  }

  OptimizerRulesTableCompanion toCompanion(bool nullToAbsent) {
    return OptimizerRulesTableCompanion(
      id: Value(id),
      type: Value(type),
      target: Value(target),
      targetName: Value(targetName),
      constraint: Value(constraint),
      value: Value(value),
    );
  }

  factory OptimizerRulesTableData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return OptimizerRulesTableData(
      id: serializer.fromJson<String>(json['id']),
      type: serializer.fromJson<String>(json['type']),
      target: serializer.fromJson<String>(json['target']),
      targetName: serializer.fromJson<String>(json['targetName']),
      constraint: serializer.fromJson<String>(json['constraint']),
      value: serializer.fromJson<int>(json['value']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'type': serializer.toJson<String>(type),
      'target': serializer.toJson<String>(target),
      'targetName': serializer.toJson<String>(targetName),
      'constraint': serializer.toJson<String>(constraint),
      'value': serializer.toJson<int>(value),
    };
  }

  OptimizerRulesTableData copyWith(
          {String? id,
          String? type,
          String? target,
          String? targetName,
          String? constraint,
          int? value}) =>
      OptimizerRulesTableData(
        id: id ?? this.id,
        type: type ?? this.type,
        target: target ?? this.target,
        targetName: targetName ?? this.targetName,
        constraint: constraint ?? this.constraint,
        value: value ?? this.value,
      );
  OptimizerRulesTableData copyWithCompanion(OptimizerRulesTableCompanion data) {
    return OptimizerRulesTableData(
      id: data.id.present ? data.id.value : this.id,
      type: data.type.present ? data.type.value : this.type,
      target: data.target.present ? data.target.value : this.target,
      targetName:
          data.targetName.present ? data.targetName.value : this.targetName,
      constraint:
          data.constraint.present ? data.constraint.value : this.constraint,
      value: data.value.present ? data.value.value : this.value,
    );
  }

  @override
  String toString() {
    return (StringBuffer('OptimizerRulesTableData(')
          ..write('id: $id, ')
          ..write('type: $type, ')
          ..write('target: $target, ')
          ..write('targetName: $targetName, ')
          ..write('constraint: $constraint, ')
          ..write('value: $value')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, type, target, targetName, constraint, value);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is OptimizerRulesTableData &&
          other.id == this.id &&
          other.type == this.type &&
          other.target == this.target &&
          other.targetName == this.targetName &&
          other.constraint == this.constraint &&
          other.value == this.value);
}

class OptimizerRulesTableCompanion
    extends UpdateCompanion<OptimizerRulesTableData> {
  final Value<String> id;
  final Value<String> type;
  final Value<String> target;
  final Value<String> targetName;
  final Value<String> constraint;
  final Value<int> value;
  final Value<int> rowid;
  const OptimizerRulesTableCompanion({
    this.id = const Value.absent(),
    this.type = const Value.absent(),
    this.target = const Value.absent(),
    this.targetName = const Value.absent(),
    this.constraint = const Value.absent(),
    this.value = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  OptimizerRulesTableCompanion.insert({
    required String id,
    required String type,
    required String target,
    required String targetName,
    required String constraint,
    required int value,
    this.rowid = const Value.absent(),
  })  : id = Value(id),
        type = Value(type),
        target = Value(target),
        targetName = Value(targetName),
        constraint = Value(constraint),
        value = Value(value);
  static Insertable<OptimizerRulesTableData> custom({
    Expression<String>? id,
    Expression<String>? type,
    Expression<String>? target,
    Expression<String>? targetName,
    Expression<String>? constraint,
    Expression<int>? value,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (type != null) 'type': type,
      if (target != null) 'target': target,
      if (targetName != null) 'target_name': targetName,
      if (constraint != null) 'constraint': constraint,
      if (value != null) 'value': value,
      if (rowid != null) 'rowid': rowid,
    });
  }

  OptimizerRulesTableCompanion copyWith(
      {Value<String>? id,
      Value<String>? type,
      Value<String>? target,
      Value<String>? targetName,
      Value<String>? constraint,
      Value<int>? value,
      Value<int>? rowid}) {
    return OptimizerRulesTableCompanion(
      id: id ?? this.id,
      type: type ?? this.type,
      target: target ?? this.target,
      targetName: targetName ?? this.targetName,
      constraint: constraint ?? this.constraint,
      value: value ?? this.value,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (type.present) {
      map['type'] = Variable<String>(type.value);
    }
    if (target.present) {
      map['target'] = Variable<String>(target.value);
    }
    if (targetName.present) {
      map['target_name'] = Variable<String>(targetName.value);
    }
    if (constraint.present) {
      map['constraint'] = Variable<String>(constraint.value);
    }
    if (value.present) {
      map['value'] = Variable<int>(value.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('OptimizerRulesTableCompanion(')
          ..write('id: $id, ')
          ..write('type: $type, ')
          ..write('target: $target, ')
          ..write('targetName: $targetName, ')
          ..write('constraint: $constraint, ')
          ..write('value: $value, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $RecipesTableTable recipesTable = $RecipesTableTable(this);
  late final $RecipeIngredientsTableTable recipeIngredientsTable =
      $RecipeIngredientsTableTable(this);
  late final $IngredientsTableTable ingredientsTable =
      $IngredientsTableTable(this);
  late final $MealPlansTableTable mealPlansTable = $MealPlansTableTable(this);
  late final $DayPlansTableTable dayPlansTable = $DayPlansTableTable(this);
  late final $MealSlotsTableTable mealSlotsTable = $MealSlotsTableTable(this);
  late final $RecipeRatingsTableTable recipeRatingsTable =
      $RecipeRatingsTableTable(this);
  late final $MealSlotConfigsTableTable mealSlotConfigsTable =
      $MealSlotConfigsTableTable(this);
  late final $BatchCookingGroupsTableTable batchCookingGroupsTable =
      $BatchCookingGroupsTableTable(this);
  late final $OptimizerRulesTableTable optimizerRulesTable =
      $OptimizerRulesTableTable(this);
  late final RecipeDao recipeDao = RecipeDao(this as AppDatabase);
  late final IngredientDao ingredientDao = IngredientDao(this as AppDatabase);
  late final MealPlanDao mealPlanDao = MealPlanDao(this as AppDatabase);
  late final SettingsDao settingsDao = SettingsDao(this as AppDatabase);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
        recipesTable,
        recipeIngredientsTable,
        ingredientsTable,
        mealPlansTable,
        dayPlansTable,
        mealSlotsTable,
        recipeRatingsTable,
        mealSlotConfigsTable,
        batchCookingGroupsTable,
        optimizerRulesTable
      ];
  @override
  StreamQueryUpdateRules get streamUpdateRules => const StreamQueryUpdateRules(
        [
          WritePropagation(
            on: TableUpdateQuery.onTableName('recipes',
                limitUpdateKind: UpdateKind.delete),
            result: [
              TableUpdate('recipe_ingredients', kind: UpdateKind.delete),
            ],
          ),
          WritePropagation(
            on: TableUpdateQuery.onTableName('meal_plans',
                limitUpdateKind: UpdateKind.delete),
            result: [
              TableUpdate('day_plans', kind: UpdateKind.delete),
            ],
          ),
          WritePropagation(
            on: TableUpdateQuery.onTableName('day_plans',
                limitUpdateKind: UpdateKind.delete),
            result: [
              TableUpdate('meal_slots', kind: UpdateKind.delete),
            ],
          ),
        ],
      );
}

typedef $$RecipesTableTableCreateCompanionBuilder = RecipesTableCompanion
    Function({
  required String id,
  required String name,
  required String type,
  Value<String> mealCategories,
  Value<String?> componentCategory,
  Value<String> steps,
  Value<String> notes,
  Value<int> rowid,
});
typedef $$RecipesTableTableUpdateCompanionBuilder = RecipesTableCompanion
    Function({
  Value<String> id,
  Value<String> name,
  Value<String> type,
  Value<String> mealCategories,
  Value<String?> componentCategory,
  Value<String> steps,
  Value<String> notes,
  Value<int> rowid,
});

final class $$RecipesTableTableReferences extends BaseReferences<_$AppDatabase,
    $RecipesTableTable, RecipesTableData> {
  $$RecipesTableTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static MultiTypedResultKey<$RecipeIngredientsTableTable,
      List<RecipeIngredientsTableData>> _recipeIngredientsTableRefsTable(
          _$AppDatabase db) =>
      MultiTypedResultKey.fromTable(db.recipeIngredientsTable,
          aliasName: $_aliasNameGenerator(
              db.recipesTable.id, db.recipeIngredientsTable.recipeId));

  $$RecipeIngredientsTableTableProcessedTableManager
      get recipeIngredientsTableRefs {
    final manager = $$RecipeIngredientsTableTableTableManager(
            $_db, $_db.recipeIngredientsTable)
        .filter((f) => f.recipeId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache =
        $_typedResult.readTableOrNull(_recipeIngredientsTableRefsTable($_db));
    return ProcessedTableManager(
        manager.$state.copyWith(prefetchedData: cache));
  }
}

class $$RecipesTableTableFilterComposer
    extends Composer<_$AppDatabase, $RecipesTableTable> {
  $$RecipesTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get type => $composableBuilder(
      column: $table.type, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get mealCategories => $composableBuilder(
      column: $table.mealCategories,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get componentCategory => $composableBuilder(
      column: $table.componentCategory,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get steps => $composableBuilder(
      column: $table.steps, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get notes => $composableBuilder(
      column: $table.notes, builder: (column) => ColumnFilters(column));

  Expression<bool> recipeIngredientsTableRefs(
      Expression<bool> Function($$RecipeIngredientsTableTableFilterComposer f)
          f) {
    final $$RecipeIngredientsTableTableFilterComposer composer =
        $composerBuilder(
            composer: this,
            getCurrentColumn: (t) => t.id,
            referencedTable: $db.recipeIngredientsTable,
            getReferencedColumn: (t) => t.recipeId,
            builder: (joinBuilder,
                    {$addJoinBuilderToRootComposer,
                    $removeJoinBuilderFromRootComposer}) =>
                $$RecipeIngredientsTableTableFilterComposer(
                  $db: $db,
                  $table: $db.recipeIngredientsTable,
                  $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                  joinBuilder: joinBuilder,
                  $removeJoinBuilderFromRootComposer:
                      $removeJoinBuilderFromRootComposer,
                ));
    return f(composer);
  }
}

class $$RecipesTableTableOrderingComposer
    extends Composer<_$AppDatabase, $RecipesTableTable> {
  $$RecipesTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get type => $composableBuilder(
      column: $table.type, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get mealCategories => $composableBuilder(
      column: $table.mealCategories,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get componentCategory => $composableBuilder(
      column: $table.componentCategory,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get steps => $composableBuilder(
      column: $table.steps, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get notes => $composableBuilder(
      column: $table.notes, builder: (column) => ColumnOrderings(column));
}

class $$RecipesTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $RecipesTableTable> {
  $$RecipesTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<String> get type =>
      $composableBuilder(column: $table.type, builder: (column) => column);

  GeneratedColumn<String> get mealCategories => $composableBuilder(
      column: $table.mealCategories, builder: (column) => column);

  GeneratedColumn<String> get componentCategory => $composableBuilder(
      column: $table.componentCategory, builder: (column) => column);

  GeneratedColumn<String> get steps =>
      $composableBuilder(column: $table.steps, builder: (column) => column);

  GeneratedColumn<String> get notes =>
      $composableBuilder(column: $table.notes, builder: (column) => column);

  Expression<T> recipeIngredientsTableRefs<T extends Object>(
      Expression<T> Function($$RecipeIngredientsTableTableAnnotationComposer a)
          f) {
    final $$RecipeIngredientsTableTableAnnotationComposer composer =
        $composerBuilder(
            composer: this,
            getCurrentColumn: (t) => t.id,
            referencedTable: $db.recipeIngredientsTable,
            getReferencedColumn: (t) => t.recipeId,
            builder: (joinBuilder,
                    {$addJoinBuilderToRootComposer,
                    $removeJoinBuilderFromRootComposer}) =>
                $$RecipeIngredientsTableTableAnnotationComposer(
                  $db: $db,
                  $table: $db.recipeIngredientsTable,
                  $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                  joinBuilder: joinBuilder,
                  $removeJoinBuilderFromRootComposer:
                      $removeJoinBuilderFromRootComposer,
                ));
    return f(composer);
  }
}

class $$RecipesTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $RecipesTableTable,
    RecipesTableData,
    $$RecipesTableTableFilterComposer,
    $$RecipesTableTableOrderingComposer,
    $$RecipesTableTableAnnotationComposer,
    $$RecipesTableTableCreateCompanionBuilder,
    $$RecipesTableTableUpdateCompanionBuilder,
    (RecipesTableData, $$RecipesTableTableReferences),
    RecipesTableData,
    PrefetchHooks Function({bool recipeIngredientsTableRefs})> {
  $$RecipesTableTableTableManager(_$AppDatabase db, $RecipesTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$RecipesTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$RecipesTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$RecipesTableTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> name = const Value.absent(),
            Value<String> type = const Value.absent(),
            Value<String> mealCategories = const Value.absent(),
            Value<String?> componentCategory = const Value.absent(),
            Value<String> steps = const Value.absent(),
            Value<String> notes = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              RecipesTableCompanion(
            id: id,
            name: name,
            type: type,
            mealCategories: mealCategories,
            componentCategory: componentCategory,
            steps: steps,
            notes: notes,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String id,
            required String name,
            required String type,
            Value<String> mealCategories = const Value.absent(),
            Value<String?> componentCategory = const Value.absent(),
            Value<String> steps = const Value.absent(),
            Value<String> notes = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              RecipesTableCompanion.insert(
            id: id,
            name: name,
            type: type,
            mealCategories: mealCategories,
            componentCategory: componentCategory,
            steps: steps,
            notes: notes,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (
                    e.readTable(table),
                    $$RecipesTableTableReferences(db, table, e)
                  ))
              .toList(),
          prefetchHooksCallback: ({recipeIngredientsTableRefs = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [
                if (recipeIngredientsTableRefs) db.recipeIngredientsTable
              ],
              addJoins: null,
              getPrefetchedDataCallback: (items) async {
                return [
                  if (recipeIngredientsTableRefs)
                    await $_getPrefetchedData<RecipesTableData,
                            $RecipesTableTable, RecipeIngredientsTableData>(
                        currentTable: table,
                        referencedTable: $$RecipesTableTableReferences
                            ._recipeIngredientsTableRefsTable(db),
                        managerFromTypedResult: (p0) =>
                            $$RecipesTableTableReferences(db, table, p0)
                                .recipeIngredientsTableRefs,
                        referencedItemsForCurrentItem: (item,
                                referencedItems) =>
                            referencedItems.where((e) => e.recipeId == item.id),
                        typedResults: items)
                ];
              },
            );
          },
        ));
}

typedef $$RecipesTableTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $RecipesTableTable,
    RecipesTableData,
    $$RecipesTableTableFilterComposer,
    $$RecipesTableTableOrderingComposer,
    $$RecipesTableTableAnnotationComposer,
    $$RecipesTableTableCreateCompanionBuilder,
    $$RecipesTableTableUpdateCompanionBuilder,
    (RecipesTableData, $$RecipesTableTableReferences),
    RecipesTableData,
    PrefetchHooks Function({bool recipeIngredientsTableRefs})>;
typedef $$RecipeIngredientsTableTableCreateCompanionBuilder
    = RecipeIngredientsTableCompanion Function({
  required String id,
  required String recipeId,
  Value<String?> ingredientId,
  Value<String?> subRecipeId,
  Value<double?> grams,
  Value<double?> portions,
  Value<int> rowid,
});
typedef $$RecipeIngredientsTableTableUpdateCompanionBuilder
    = RecipeIngredientsTableCompanion Function({
  Value<String> id,
  Value<String> recipeId,
  Value<String?> ingredientId,
  Value<String?> subRecipeId,
  Value<double?> grams,
  Value<double?> portions,
  Value<int> rowid,
});

final class $$RecipeIngredientsTableTableReferences extends BaseReferences<
    _$AppDatabase, $RecipeIngredientsTableTable, RecipeIngredientsTableData> {
  $$RecipeIngredientsTableTableReferences(
      super.$_db, super.$_table, super.$_typedResult);

  static $RecipesTableTable _recipeIdTable(_$AppDatabase db) =>
      db.recipesTable.createAlias($_aliasNameGenerator(
          db.recipeIngredientsTable.recipeId, db.recipesTable.id));

  $$RecipesTableTableProcessedTableManager get recipeId {
    final $_column = $_itemColumn<String>('recipe_id')!;

    final manager = $$RecipesTableTableTableManager($_db, $_db.recipesTable)
        .filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_recipeIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
        manager.$state.copyWith(prefetchedData: [item]));
  }
}

class $$RecipeIngredientsTableTableFilterComposer
    extends Composer<_$AppDatabase, $RecipeIngredientsTableTable> {
  $$RecipeIngredientsTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get ingredientId => $composableBuilder(
      column: $table.ingredientId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get subRecipeId => $composableBuilder(
      column: $table.subRecipeId, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get grams => $composableBuilder(
      column: $table.grams, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get portions => $composableBuilder(
      column: $table.portions, builder: (column) => ColumnFilters(column));

  $$RecipesTableTableFilterComposer get recipeId {
    final $$RecipesTableTableFilterComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.recipeId,
        referencedTable: $db.recipesTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$RecipesTableTableFilterComposer(
              $db: $db,
              $table: $db.recipesTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }
}

class $$RecipeIngredientsTableTableOrderingComposer
    extends Composer<_$AppDatabase, $RecipeIngredientsTableTable> {
  $$RecipeIngredientsTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get ingredientId => $composableBuilder(
      column: $table.ingredientId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get subRecipeId => $composableBuilder(
      column: $table.subRecipeId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get grams => $composableBuilder(
      column: $table.grams, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get portions => $composableBuilder(
      column: $table.portions, builder: (column) => ColumnOrderings(column));

  $$RecipesTableTableOrderingComposer get recipeId {
    final $$RecipesTableTableOrderingComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.recipeId,
        referencedTable: $db.recipesTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$RecipesTableTableOrderingComposer(
              $db: $db,
              $table: $db.recipesTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }
}

class $$RecipeIngredientsTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $RecipeIngredientsTableTable> {
  $$RecipeIngredientsTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get ingredientId => $composableBuilder(
      column: $table.ingredientId, builder: (column) => column);

  GeneratedColumn<String> get subRecipeId => $composableBuilder(
      column: $table.subRecipeId, builder: (column) => column);

  GeneratedColumn<double> get grams =>
      $composableBuilder(column: $table.grams, builder: (column) => column);

  GeneratedColumn<double> get portions =>
      $composableBuilder(column: $table.portions, builder: (column) => column);

  $$RecipesTableTableAnnotationComposer get recipeId {
    final $$RecipesTableTableAnnotationComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.recipeId,
        referencedTable: $db.recipesTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$RecipesTableTableAnnotationComposer(
              $db: $db,
              $table: $db.recipesTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }
}

class $$RecipeIngredientsTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $RecipeIngredientsTableTable,
    RecipeIngredientsTableData,
    $$RecipeIngredientsTableTableFilterComposer,
    $$RecipeIngredientsTableTableOrderingComposer,
    $$RecipeIngredientsTableTableAnnotationComposer,
    $$RecipeIngredientsTableTableCreateCompanionBuilder,
    $$RecipeIngredientsTableTableUpdateCompanionBuilder,
    (RecipeIngredientsTableData, $$RecipeIngredientsTableTableReferences),
    RecipeIngredientsTableData,
    PrefetchHooks Function({bool recipeId})> {
  $$RecipeIngredientsTableTableTableManager(
      _$AppDatabase db, $RecipeIngredientsTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$RecipeIngredientsTableTableFilterComposer(
                  $db: db, $table: table),
          createOrderingComposer: () =>
              $$RecipeIngredientsTableTableOrderingComposer(
                  $db: db, $table: table),
          createComputedFieldComposer: () =>
              $$RecipeIngredientsTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> recipeId = const Value.absent(),
            Value<String?> ingredientId = const Value.absent(),
            Value<String?> subRecipeId = const Value.absent(),
            Value<double?> grams = const Value.absent(),
            Value<double?> portions = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              RecipeIngredientsTableCompanion(
            id: id,
            recipeId: recipeId,
            ingredientId: ingredientId,
            subRecipeId: subRecipeId,
            grams: grams,
            portions: portions,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String id,
            required String recipeId,
            Value<String?> ingredientId = const Value.absent(),
            Value<String?> subRecipeId = const Value.absent(),
            Value<double?> grams = const Value.absent(),
            Value<double?> portions = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              RecipeIngredientsTableCompanion.insert(
            id: id,
            recipeId: recipeId,
            ingredientId: ingredientId,
            subRecipeId: subRecipeId,
            grams: grams,
            portions: portions,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (
                    e.readTable(table),
                    $$RecipeIngredientsTableTableReferences(db, table, e)
                  ))
              .toList(),
          prefetchHooksCallback: ({recipeId = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [],
              addJoins: <
                  T extends TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic>>(state) {
                if (recipeId) {
                  state = state.withJoin(
                    currentTable: table,
                    currentColumn: table.recipeId,
                    referencedTable: $$RecipeIngredientsTableTableReferences
                        ._recipeIdTable(db),
                    referencedColumn: $$RecipeIngredientsTableTableReferences
                        ._recipeIdTable(db)
                        .id,
                  ) as T;
                }

                return state;
              },
              getPrefetchedDataCallback: (items) async {
                return [];
              },
            );
          },
        ));
}

typedef $$RecipeIngredientsTableTableProcessedTableManager
    = ProcessedTableManager<
        _$AppDatabase,
        $RecipeIngredientsTableTable,
        RecipeIngredientsTableData,
        $$RecipeIngredientsTableTableFilterComposer,
        $$RecipeIngredientsTableTableOrderingComposer,
        $$RecipeIngredientsTableTableAnnotationComposer,
        $$RecipeIngredientsTableTableCreateCompanionBuilder,
        $$RecipeIngredientsTableTableUpdateCompanionBuilder,
        (RecipeIngredientsTableData, $$RecipeIngredientsTableTableReferences),
        RecipeIngredientsTableData,
        PrefetchHooks Function({bool recipeId})>;
typedef $$IngredientsTableTableCreateCompanionBuilder
    = IngredientsTableCompanion Function({
  required String id,
  required String name,
  required String category,
  Value<double> kcalPer100g,
  Value<double> proteinPer100g,
  Value<double> fatPer100g,
  Value<double> carbsPer100g,
  required String source,
  Value<String> steps,
  Value<int> rowid,
});
typedef $$IngredientsTableTableUpdateCompanionBuilder
    = IngredientsTableCompanion Function({
  Value<String> id,
  Value<String> name,
  Value<String> category,
  Value<double> kcalPer100g,
  Value<double> proteinPer100g,
  Value<double> fatPer100g,
  Value<double> carbsPer100g,
  Value<String> source,
  Value<String> steps,
  Value<int> rowid,
});

class $$IngredientsTableTableFilterComposer
    extends Composer<_$AppDatabase, $IngredientsTableTable> {
  $$IngredientsTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get category => $composableBuilder(
      column: $table.category, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get kcalPer100g => $composableBuilder(
      column: $table.kcalPer100g, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get proteinPer100g => $composableBuilder(
      column: $table.proteinPer100g,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get fatPer100g => $composableBuilder(
      column: $table.fatPer100g, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get carbsPer100g => $composableBuilder(
      column: $table.carbsPer100g, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get source => $composableBuilder(
      column: $table.source, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get steps => $composableBuilder(
      column: $table.steps, builder: (column) => ColumnFilters(column));
}

class $$IngredientsTableTableOrderingComposer
    extends Composer<_$AppDatabase, $IngredientsTableTable> {
  $$IngredientsTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get category => $composableBuilder(
      column: $table.category, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get kcalPer100g => $composableBuilder(
      column: $table.kcalPer100g, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get proteinPer100g => $composableBuilder(
      column: $table.proteinPer100g,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get fatPer100g => $composableBuilder(
      column: $table.fatPer100g, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get carbsPer100g => $composableBuilder(
      column: $table.carbsPer100g,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get source => $composableBuilder(
      column: $table.source, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get steps => $composableBuilder(
      column: $table.steps, builder: (column) => ColumnOrderings(column));
}

class $$IngredientsTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $IngredientsTableTable> {
  $$IngredientsTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<String> get category =>
      $composableBuilder(column: $table.category, builder: (column) => column);

  GeneratedColumn<double> get kcalPer100g => $composableBuilder(
      column: $table.kcalPer100g, builder: (column) => column);

  GeneratedColumn<double> get proteinPer100g => $composableBuilder(
      column: $table.proteinPer100g, builder: (column) => column);

  GeneratedColumn<double> get fatPer100g => $composableBuilder(
      column: $table.fatPer100g, builder: (column) => column);

  GeneratedColumn<double> get carbsPer100g => $composableBuilder(
      column: $table.carbsPer100g, builder: (column) => column);

  GeneratedColumn<String> get source =>
      $composableBuilder(column: $table.source, builder: (column) => column);

  GeneratedColumn<String> get steps =>
      $composableBuilder(column: $table.steps, builder: (column) => column);
}

class $$IngredientsTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $IngredientsTableTable,
    IngredientsTableData,
    $$IngredientsTableTableFilterComposer,
    $$IngredientsTableTableOrderingComposer,
    $$IngredientsTableTableAnnotationComposer,
    $$IngredientsTableTableCreateCompanionBuilder,
    $$IngredientsTableTableUpdateCompanionBuilder,
    (
      IngredientsTableData,
      BaseReferences<_$AppDatabase, $IngredientsTableTable,
          IngredientsTableData>
    ),
    IngredientsTableData,
    PrefetchHooks Function()> {
  $$IngredientsTableTableTableManager(
      _$AppDatabase db, $IngredientsTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$IngredientsTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$IngredientsTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$IngredientsTableTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> name = const Value.absent(),
            Value<String> category = const Value.absent(),
            Value<double> kcalPer100g = const Value.absent(),
            Value<double> proteinPer100g = const Value.absent(),
            Value<double> fatPer100g = const Value.absent(),
            Value<double> carbsPer100g = const Value.absent(),
            Value<String> source = const Value.absent(),
            Value<String> steps = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              IngredientsTableCompanion(
            id: id,
            name: name,
            category: category,
            kcalPer100g: kcalPer100g,
            proteinPer100g: proteinPer100g,
            fatPer100g: fatPer100g,
            carbsPer100g: carbsPer100g,
            source: source,
            steps: steps,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String id,
            required String name,
            required String category,
            Value<double> kcalPer100g = const Value.absent(),
            Value<double> proteinPer100g = const Value.absent(),
            Value<double> fatPer100g = const Value.absent(),
            Value<double> carbsPer100g = const Value.absent(),
            required String source,
            Value<String> steps = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              IngredientsTableCompanion.insert(
            id: id,
            name: name,
            category: category,
            kcalPer100g: kcalPer100g,
            proteinPer100g: proteinPer100g,
            fatPer100g: fatPer100g,
            carbsPer100g: carbsPer100g,
            source: source,
            steps: steps,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$IngredientsTableTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $IngredientsTableTable,
    IngredientsTableData,
    $$IngredientsTableTableFilterComposer,
    $$IngredientsTableTableOrderingComposer,
    $$IngredientsTableTableAnnotationComposer,
    $$IngredientsTableTableCreateCompanionBuilder,
    $$IngredientsTableTableUpdateCompanionBuilder,
    (
      IngredientsTableData,
      BaseReferences<_$AppDatabase, $IngredientsTableTable,
          IngredientsTableData>
    ),
    IngredientsTableData,
    PrefetchHooks Function()>;
typedef $$MealPlansTableTableCreateCompanionBuilder = MealPlansTableCompanion
    Function({
  required String id,
  required String name,
  required int startDate,
  required int endDate,
  Value<int> rowid,
});
typedef $$MealPlansTableTableUpdateCompanionBuilder = MealPlansTableCompanion
    Function({
  Value<String> id,
  Value<String> name,
  Value<int> startDate,
  Value<int> endDate,
  Value<int> rowid,
});

final class $$MealPlansTableTableReferences extends BaseReferences<
    _$AppDatabase, $MealPlansTableTable, MealPlansTableData> {
  $$MealPlansTableTableReferences(
      super.$_db, super.$_table, super.$_typedResult);

  static MultiTypedResultKey<$DayPlansTableTable, List<DayPlansTableData>>
      _dayPlansTableRefsTable(_$AppDatabase db) =>
          MultiTypedResultKey.fromTable(db.dayPlansTable,
              aliasName: $_aliasNameGenerator(
                  db.mealPlansTable.id, db.dayPlansTable.mealPlanId));

  $$DayPlansTableTableProcessedTableManager get dayPlansTableRefs {
    final manager = $$DayPlansTableTableTableManager($_db, $_db.dayPlansTable)
        .filter((f) => f.mealPlanId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache = $_typedResult.readTableOrNull(_dayPlansTableRefsTable($_db));
    return ProcessedTableManager(
        manager.$state.copyWith(prefetchedData: cache));
  }
}

class $$MealPlansTableTableFilterComposer
    extends Composer<_$AppDatabase, $MealPlansTableTable> {
  $$MealPlansTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get startDate => $composableBuilder(
      column: $table.startDate, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get endDate => $composableBuilder(
      column: $table.endDate, builder: (column) => ColumnFilters(column));

  Expression<bool> dayPlansTableRefs(
      Expression<bool> Function($$DayPlansTableTableFilterComposer f) f) {
    final $$DayPlansTableTableFilterComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.id,
        referencedTable: $db.dayPlansTable,
        getReferencedColumn: (t) => t.mealPlanId,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$DayPlansTableTableFilterComposer(
              $db: $db,
              $table: $db.dayPlansTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return f(composer);
  }
}

class $$MealPlansTableTableOrderingComposer
    extends Composer<_$AppDatabase, $MealPlansTableTable> {
  $$MealPlansTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get startDate => $composableBuilder(
      column: $table.startDate, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get endDate => $composableBuilder(
      column: $table.endDate, builder: (column) => ColumnOrderings(column));
}

class $$MealPlansTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $MealPlansTableTable> {
  $$MealPlansTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<int> get startDate =>
      $composableBuilder(column: $table.startDate, builder: (column) => column);

  GeneratedColumn<int> get endDate =>
      $composableBuilder(column: $table.endDate, builder: (column) => column);

  Expression<T> dayPlansTableRefs<T extends Object>(
      Expression<T> Function($$DayPlansTableTableAnnotationComposer a) f) {
    final $$DayPlansTableTableAnnotationComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.id,
        referencedTable: $db.dayPlansTable,
        getReferencedColumn: (t) => t.mealPlanId,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$DayPlansTableTableAnnotationComposer(
              $db: $db,
              $table: $db.dayPlansTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return f(composer);
  }
}

class $$MealPlansTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $MealPlansTableTable,
    MealPlansTableData,
    $$MealPlansTableTableFilterComposer,
    $$MealPlansTableTableOrderingComposer,
    $$MealPlansTableTableAnnotationComposer,
    $$MealPlansTableTableCreateCompanionBuilder,
    $$MealPlansTableTableUpdateCompanionBuilder,
    (MealPlansTableData, $$MealPlansTableTableReferences),
    MealPlansTableData,
    PrefetchHooks Function({bool dayPlansTableRefs})> {
  $$MealPlansTableTableTableManager(
      _$AppDatabase db, $MealPlansTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$MealPlansTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$MealPlansTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$MealPlansTableTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> name = const Value.absent(),
            Value<int> startDate = const Value.absent(),
            Value<int> endDate = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              MealPlansTableCompanion(
            id: id,
            name: name,
            startDate: startDate,
            endDate: endDate,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String id,
            required String name,
            required int startDate,
            required int endDate,
            Value<int> rowid = const Value.absent(),
          }) =>
              MealPlansTableCompanion.insert(
            id: id,
            name: name,
            startDate: startDate,
            endDate: endDate,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (
                    e.readTable(table),
                    $$MealPlansTableTableReferences(db, table, e)
                  ))
              .toList(),
          prefetchHooksCallback: ({dayPlansTableRefs = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [
                if (dayPlansTableRefs) db.dayPlansTable
              ],
              addJoins: null,
              getPrefetchedDataCallback: (items) async {
                return [
                  if (dayPlansTableRefs)
                    await $_getPrefetchedData<MealPlansTableData,
                            $MealPlansTableTable, DayPlansTableData>(
                        currentTable: table,
                        referencedTable: $$MealPlansTableTableReferences
                            ._dayPlansTableRefsTable(db),
                        managerFromTypedResult: (p0) =>
                            $$MealPlansTableTableReferences(db, table, p0)
                                .dayPlansTableRefs,
                        referencedItemsForCurrentItem:
                            (item, referencedItems) => referencedItems
                                .where((e) => e.mealPlanId == item.id),
                        typedResults: items)
                ];
              },
            );
          },
        ));
}

typedef $$MealPlansTableTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $MealPlansTableTable,
    MealPlansTableData,
    $$MealPlansTableTableFilterComposer,
    $$MealPlansTableTableOrderingComposer,
    $$MealPlansTableTableAnnotationComposer,
    $$MealPlansTableTableCreateCompanionBuilder,
    $$MealPlansTableTableUpdateCompanionBuilder,
    (MealPlansTableData, $$MealPlansTableTableReferences),
    MealPlansTableData,
    PrefetchHooks Function({bool dayPlansTableRefs})>;
typedef $$DayPlansTableTableCreateCompanionBuilder = DayPlansTableCompanion
    Function({
  required String id,
  required String mealPlanId,
  required int date,
  Value<double> proteinPowderGrams,
  Value<double> kcalTarget,
  Value<double> proteinTarget,
  Value<int> rowid,
});
typedef $$DayPlansTableTableUpdateCompanionBuilder = DayPlansTableCompanion
    Function({
  Value<String> id,
  Value<String> mealPlanId,
  Value<int> date,
  Value<double> proteinPowderGrams,
  Value<double> kcalTarget,
  Value<double> proteinTarget,
  Value<int> rowid,
});

final class $$DayPlansTableTableReferences extends BaseReferences<_$AppDatabase,
    $DayPlansTableTable, DayPlansTableData> {
  $$DayPlansTableTableReferences(
      super.$_db, super.$_table, super.$_typedResult);

  static $MealPlansTableTable _mealPlanIdTable(_$AppDatabase db) =>
      db.mealPlansTable.createAlias($_aliasNameGenerator(
          db.dayPlansTable.mealPlanId, db.mealPlansTable.id));

  $$MealPlansTableTableProcessedTableManager get mealPlanId {
    final $_column = $_itemColumn<String>('meal_plan_id')!;

    final manager = $$MealPlansTableTableTableManager($_db, $_db.mealPlansTable)
        .filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_mealPlanIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
        manager.$state.copyWith(prefetchedData: [item]));
  }

  static MultiTypedResultKey<$MealSlotsTableTable, List<MealSlotsTableData>>
      _mealSlotsTableRefsTable(_$AppDatabase db) =>
          MultiTypedResultKey.fromTable(db.mealSlotsTable,
              aliasName: $_aliasNameGenerator(
                  db.dayPlansTable.id, db.mealSlotsTable.dayPlanId));

  $$MealSlotsTableTableProcessedTableManager get mealSlotsTableRefs {
    final manager = $$MealSlotsTableTableTableManager($_db, $_db.mealSlotsTable)
        .filter((f) => f.dayPlanId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache = $_typedResult.readTableOrNull(_mealSlotsTableRefsTable($_db));
    return ProcessedTableManager(
        manager.$state.copyWith(prefetchedData: cache));
  }
}

class $$DayPlansTableTableFilterComposer
    extends Composer<_$AppDatabase, $DayPlansTableTable> {
  $$DayPlansTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get date => $composableBuilder(
      column: $table.date, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get proteinPowderGrams => $composableBuilder(
      column: $table.proteinPowderGrams,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get kcalTarget => $composableBuilder(
      column: $table.kcalTarget, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get proteinTarget => $composableBuilder(
      column: $table.proteinTarget, builder: (column) => ColumnFilters(column));

  $$MealPlansTableTableFilterComposer get mealPlanId {
    final $$MealPlansTableTableFilterComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.mealPlanId,
        referencedTable: $db.mealPlansTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$MealPlansTableTableFilterComposer(
              $db: $db,
              $table: $db.mealPlansTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }

  Expression<bool> mealSlotsTableRefs(
      Expression<bool> Function($$MealSlotsTableTableFilterComposer f) f) {
    final $$MealSlotsTableTableFilterComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.id,
        referencedTable: $db.mealSlotsTable,
        getReferencedColumn: (t) => t.dayPlanId,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$MealSlotsTableTableFilterComposer(
              $db: $db,
              $table: $db.mealSlotsTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return f(composer);
  }
}

class $$DayPlansTableTableOrderingComposer
    extends Composer<_$AppDatabase, $DayPlansTableTable> {
  $$DayPlansTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get date => $composableBuilder(
      column: $table.date, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get proteinPowderGrams => $composableBuilder(
      column: $table.proteinPowderGrams,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get kcalTarget => $composableBuilder(
      column: $table.kcalTarget, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get proteinTarget => $composableBuilder(
      column: $table.proteinTarget,
      builder: (column) => ColumnOrderings(column));

  $$MealPlansTableTableOrderingComposer get mealPlanId {
    final $$MealPlansTableTableOrderingComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.mealPlanId,
        referencedTable: $db.mealPlansTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$MealPlansTableTableOrderingComposer(
              $db: $db,
              $table: $db.mealPlansTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }
}

class $$DayPlansTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $DayPlansTableTable> {
  $$DayPlansTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get date =>
      $composableBuilder(column: $table.date, builder: (column) => column);

  GeneratedColumn<double> get proteinPowderGrams => $composableBuilder(
      column: $table.proteinPowderGrams, builder: (column) => column);

  GeneratedColumn<double> get kcalTarget => $composableBuilder(
      column: $table.kcalTarget, builder: (column) => column);

  GeneratedColumn<double> get proteinTarget => $composableBuilder(
      column: $table.proteinTarget, builder: (column) => column);

  $$MealPlansTableTableAnnotationComposer get mealPlanId {
    final $$MealPlansTableTableAnnotationComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.mealPlanId,
        referencedTable: $db.mealPlansTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$MealPlansTableTableAnnotationComposer(
              $db: $db,
              $table: $db.mealPlansTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }

  Expression<T> mealSlotsTableRefs<T extends Object>(
      Expression<T> Function($$MealSlotsTableTableAnnotationComposer a) f) {
    final $$MealSlotsTableTableAnnotationComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.id,
        referencedTable: $db.mealSlotsTable,
        getReferencedColumn: (t) => t.dayPlanId,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$MealSlotsTableTableAnnotationComposer(
              $db: $db,
              $table: $db.mealSlotsTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return f(composer);
  }
}

class $$DayPlansTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $DayPlansTableTable,
    DayPlansTableData,
    $$DayPlansTableTableFilterComposer,
    $$DayPlansTableTableOrderingComposer,
    $$DayPlansTableTableAnnotationComposer,
    $$DayPlansTableTableCreateCompanionBuilder,
    $$DayPlansTableTableUpdateCompanionBuilder,
    (DayPlansTableData, $$DayPlansTableTableReferences),
    DayPlansTableData,
    PrefetchHooks Function({bool mealPlanId, bool mealSlotsTableRefs})> {
  $$DayPlansTableTableTableManager(_$AppDatabase db, $DayPlansTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$DayPlansTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$DayPlansTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$DayPlansTableTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> mealPlanId = const Value.absent(),
            Value<int> date = const Value.absent(),
            Value<double> proteinPowderGrams = const Value.absent(),
            Value<double> kcalTarget = const Value.absent(),
            Value<double> proteinTarget = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              DayPlansTableCompanion(
            id: id,
            mealPlanId: mealPlanId,
            date: date,
            proteinPowderGrams: proteinPowderGrams,
            kcalTarget: kcalTarget,
            proteinTarget: proteinTarget,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String id,
            required String mealPlanId,
            required int date,
            Value<double> proteinPowderGrams = const Value.absent(),
            Value<double> kcalTarget = const Value.absent(),
            Value<double> proteinTarget = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              DayPlansTableCompanion.insert(
            id: id,
            mealPlanId: mealPlanId,
            date: date,
            proteinPowderGrams: proteinPowderGrams,
            kcalTarget: kcalTarget,
            proteinTarget: proteinTarget,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (
                    e.readTable(table),
                    $$DayPlansTableTableReferences(db, table, e)
                  ))
              .toList(),
          prefetchHooksCallback: (
              {mealPlanId = false, mealSlotsTableRefs = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [
                if (mealSlotsTableRefs) db.mealSlotsTable
              ],
              addJoins: <
                  T extends TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic>>(state) {
                if (mealPlanId) {
                  state = state.withJoin(
                    currentTable: table,
                    currentColumn: table.mealPlanId,
                    referencedTable:
                        $$DayPlansTableTableReferences._mealPlanIdTable(db),
                    referencedColumn:
                        $$DayPlansTableTableReferences._mealPlanIdTable(db).id,
                  ) as T;
                }

                return state;
              },
              getPrefetchedDataCallback: (items) async {
                return [
                  if (mealSlotsTableRefs)
                    await $_getPrefetchedData<DayPlansTableData,
                            $DayPlansTableTable, MealSlotsTableData>(
                        currentTable: table,
                        referencedTable: $$DayPlansTableTableReferences
                            ._mealSlotsTableRefsTable(db),
                        managerFromTypedResult: (p0) =>
                            $$DayPlansTableTableReferences(db, table, p0)
                                .mealSlotsTableRefs,
                        referencedItemsForCurrentItem:
                            (item, referencedItems) => referencedItems
                                .where((e) => e.dayPlanId == item.id),
                        typedResults: items)
                ];
              },
            );
          },
        ));
}

typedef $$DayPlansTableTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $DayPlansTableTable,
    DayPlansTableData,
    $$DayPlansTableTableFilterComposer,
    $$DayPlansTableTableOrderingComposer,
    $$DayPlansTableTableAnnotationComposer,
    $$DayPlansTableTableCreateCompanionBuilder,
    $$DayPlansTableTableUpdateCompanionBuilder,
    (DayPlansTableData, $$DayPlansTableTableReferences),
    DayPlansTableData,
    PrefetchHooks Function({bool mealPlanId, bool mealSlotsTableRefs})>;
typedef $$MealSlotsTableTableCreateCompanionBuilder = MealSlotsTableCompanion
    Function({
  required String id,
  required String dayPlanId,
  required String type,
  Value<int> slotIndex,
  required String recipeId,
  Value<int> rowid,
});
typedef $$MealSlotsTableTableUpdateCompanionBuilder = MealSlotsTableCompanion
    Function({
  Value<String> id,
  Value<String> dayPlanId,
  Value<String> type,
  Value<int> slotIndex,
  Value<String> recipeId,
  Value<int> rowid,
});

final class $$MealSlotsTableTableReferences extends BaseReferences<
    _$AppDatabase, $MealSlotsTableTable, MealSlotsTableData> {
  $$MealSlotsTableTableReferences(
      super.$_db, super.$_table, super.$_typedResult);

  static $DayPlansTableTable _dayPlanIdTable(_$AppDatabase db) =>
      db.dayPlansTable.createAlias($_aliasNameGenerator(
          db.mealSlotsTable.dayPlanId, db.dayPlansTable.id));

  $$DayPlansTableTableProcessedTableManager get dayPlanId {
    final $_column = $_itemColumn<String>('day_plan_id')!;

    final manager = $$DayPlansTableTableTableManager($_db, $_db.dayPlansTable)
        .filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_dayPlanIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
        manager.$state.copyWith(prefetchedData: [item]));
  }
}

class $$MealSlotsTableTableFilterComposer
    extends Composer<_$AppDatabase, $MealSlotsTableTable> {
  $$MealSlotsTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get type => $composableBuilder(
      column: $table.type, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get slotIndex => $composableBuilder(
      column: $table.slotIndex, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get recipeId => $composableBuilder(
      column: $table.recipeId, builder: (column) => ColumnFilters(column));

  $$DayPlansTableTableFilterComposer get dayPlanId {
    final $$DayPlansTableTableFilterComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.dayPlanId,
        referencedTable: $db.dayPlansTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$DayPlansTableTableFilterComposer(
              $db: $db,
              $table: $db.dayPlansTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }
}

class $$MealSlotsTableTableOrderingComposer
    extends Composer<_$AppDatabase, $MealSlotsTableTable> {
  $$MealSlotsTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get type => $composableBuilder(
      column: $table.type, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get slotIndex => $composableBuilder(
      column: $table.slotIndex, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get recipeId => $composableBuilder(
      column: $table.recipeId, builder: (column) => ColumnOrderings(column));

  $$DayPlansTableTableOrderingComposer get dayPlanId {
    final $$DayPlansTableTableOrderingComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.dayPlanId,
        referencedTable: $db.dayPlansTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$DayPlansTableTableOrderingComposer(
              $db: $db,
              $table: $db.dayPlansTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }
}

class $$MealSlotsTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $MealSlotsTableTable> {
  $$MealSlotsTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get type =>
      $composableBuilder(column: $table.type, builder: (column) => column);

  GeneratedColumn<int> get slotIndex =>
      $composableBuilder(column: $table.slotIndex, builder: (column) => column);

  GeneratedColumn<String> get recipeId =>
      $composableBuilder(column: $table.recipeId, builder: (column) => column);

  $$DayPlansTableTableAnnotationComposer get dayPlanId {
    final $$DayPlansTableTableAnnotationComposer composer = $composerBuilder(
        composer: this,
        getCurrentColumn: (t) => t.dayPlanId,
        referencedTable: $db.dayPlansTable,
        getReferencedColumn: (t) => t.id,
        builder: (joinBuilder,
                {$addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer}) =>
            $$DayPlansTableTableAnnotationComposer(
              $db: $db,
              $table: $db.dayPlansTable,
              $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
              joinBuilder: joinBuilder,
              $removeJoinBuilderFromRootComposer:
                  $removeJoinBuilderFromRootComposer,
            ));
    return composer;
  }
}

class $$MealSlotsTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $MealSlotsTableTable,
    MealSlotsTableData,
    $$MealSlotsTableTableFilterComposer,
    $$MealSlotsTableTableOrderingComposer,
    $$MealSlotsTableTableAnnotationComposer,
    $$MealSlotsTableTableCreateCompanionBuilder,
    $$MealSlotsTableTableUpdateCompanionBuilder,
    (MealSlotsTableData, $$MealSlotsTableTableReferences),
    MealSlotsTableData,
    PrefetchHooks Function({bool dayPlanId})> {
  $$MealSlotsTableTableTableManager(
      _$AppDatabase db, $MealSlotsTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$MealSlotsTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$MealSlotsTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$MealSlotsTableTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> dayPlanId = const Value.absent(),
            Value<String> type = const Value.absent(),
            Value<int> slotIndex = const Value.absent(),
            Value<String> recipeId = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              MealSlotsTableCompanion(
            id: id,
            dayPlanId: dayPlanId,
            type: type,
            slotIndex: slotIndex,
            recipeId: recipeId,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String id,
            required String dayPlanId,
            required String type,
            Value<int> slotIndex = const Value.absent(),
            required String recipeId,
            Value<int> rowid = const Value.absent(),
          }) =>
              MealSlotsTableCompanion.insert(
            id: id,
            dayPlanId: dayPlanId,
            type: type,
            slotIndex: slotIndex,
            recipeId: recipeId,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (
                    e.readTable(table),
                    $$MealSlotsTableTableReferences(db, table, e)
                  ))
              .toList(),
          prefetchHooksCallback: ({dayPlanId = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [],
              addJoins: <
                  T extends TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic>>(state) {
                if (dayPlanId) {
                  state = state.withJoin(
                    currentTable: table,
                    currentColumn: table.dayPlanId,
                    referencedTable:
                        $$MealSlotsTableTableReferences._dayPlanIdTable(db),
                    referencedColumn:
                        $$MealSlotsTableTableReferences._dayPlanIdTable(db).id,
                  ) as T;
                }

                return state;
              },
              getPrefetchedDataCallback: (items) async {
                return [];
              },
            );
          },
        ));
}

typedef $$MealSlotsTableTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $MealSlotsTableTable,
    MealSlotsTableData,
    $$MealSlotsTableTableFilterComposer,
    $$MealSlotsTableTableOrderingComposer,
    $$MealSlotsTableTableAnnotationComposer,
    $$MealSlotsTableTableCreateCompanionBuilder,
    $$MealSlotsTableTableUpdateCompanionBuilder,
    (MealSlotsTableData, $$MealSlotsTableTableReferences),
    MealSlotsTableData,
    PrefetchHooks Function({bool dayPlanId})>;
typedef $$RecipeRatingsTableTableCreateCompanionBuilder
    = RecipeRatingsTableCompanion Function({
  required String recipeId,
  Value<int?> stars,
  Value<int> timesScheduled,
  Value<int> timesManuallyRemoved,
  Value<bool> isPinned,
  Value<bool> isExcluded,
  Value<int?> lastScheduledDate,
  Value<int> rowid,
});
typedef $$RecipeRatingsTableTableUpdateCompanionBuilder
    = RecipeRatingsTableCompanion Function({
  Value<String> recipeId,
  Value<int?> stars,
  Value<int> timesScheduled,
  Value<int> timesManuallyRemoved,
  Value<bool> isPinned,
  Value<bool> isExcluded,
  Value<int?> lastScheduledDate,
  Value<int> rowid,
});

class $$RecipeRatingsTableTableFilterComposer
    extends Composer<_$AppDatabase, $RecipeRatingsTableTable> {
  $$RecipeRatingsTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get recipeId => $composableBuilder(
      column: $table.recipeId, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get stars => $composableBuilder(
      column: $table.stars, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get timesScheduled => $composableBuilder(
      column: $table.timesScheduled,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get timesManuallyRemoved => $composableBuilder(
      column: $table.timesManuallyRemoved,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isPinned => $composableBuilder(
      column: $table.isPinned, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isExcluded => $composableBuilder(
      column: $table.isExcluded, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get lastScheduledDate => $composableBuilder(
      column: $table.lastScheduledDate,
      builder: (column) => ColumnFilters(column));
}

class $$RecipeRatingsTableTableOrderingComposer
    extends Composer<_$AppDatabase, $RecipeRatingsTableTable> {
  $$RecipeRatingsTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get recipeId => $composableBuilder(
      column: $table.recipeId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get stars => $composableBuilder(
      column: $table.stars, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get timesScheduled => $composableBuilder(
      column: $table.timesScheduled,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get timesManuallyRemoved => $composableBuilder(
      column: $table.timesManuallyRemoved,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isPinned => $composableBuilder(
      column: $table.isPinned, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isExcluded => $composableBuilder(
      column: $table.isExcluded, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get lastScheduledDate => $composableBuilder(
      column: $table.lastScheduledDate,
      builder: (column) => ColumnOrderings(column));
}

class $$RecipeRatingsTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $RecipeRatingsTableTable> {
  $$RecipeRatingsTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get recipeId =>
      $composableBuilder(column: $table.recipeId, builder: (column) => column);

  GeneratedColumn<int> get stars =>
      $composableBuilder(column: $table.stars, builder: (column) => column);

  GeneratedColumn<int> get timesScheduled => $composableBuilder(
      column: $table.timesScheduled, builder: (column) => column);

  GeneratedColumn<int> get timesManuallyRemoved => $composableBuilder(
      column: $table.timesManuallyRemoved, builder: (column) => column);

  GeneratedColumn<bool> get isPinned =>
      $composableBuilder(column: $table.isPinned, builder: (column) => column);

  GeneratedColumn<bool> get isExcluded => $composableBuilder(
      column: $table.isExcluded, builder: (column) => column);

  GeneratedColumn<int> get lastScheduledDate => $composableBuilder(
      column: $table.lastScheduledDate, builder: (column) => column);
}

class $$RecipeRatingsTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $RecipeRatingsTableTable,
    RecipeRatingsTableData,
    $$RecipeRatingsTableTableFilterComposer,
    $$RecipeRatingsTableTableOrderingComposer,
    $$RecipeRatingsTableTableAnnotationComposer,
    $$RecipeRatingsTableTableCreateCompanionBuilder,
    $$RecipeRatingsTableTableUpdateCompanionBuilder,
    (
      RecipeRatingsTableData,
      BaseReferences<_$AppDatabase, $RecipeRatingsTableTable,
          RecipeRatingsTableData>
    ),
    RecipeRatingsTableData,
    PrefetchHooks Function()> {
  $$RecipeRatingsTableTableTableManager(
      _$AppDatabase db, $RecipeRatingsTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$RecipeRatingsTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$RecipeRatingsTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$RecipeRatingsTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> recipeId = const Value.absent(),
            Value<int?> stars = const Value.absent(),
            Value<int> timesScheduled = const Value.absent(),
            Value<int> timesManuallyRemoved = const Value.absent(),
            Value<bool> isPinned = const Value.absent(),
            Value<bool> isExcluded = const Value.absent(),
            Value<int?> lastScheduledDate = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              RecipeRatingsTableCompanion(
            recipeId: recipeId,
            stars: stars,
            timesScheduled: timesScheduled,
            timesManuallyRemoved: timesManuallyRemoved,
            isPinned: isPinned,
            isExcluded: isExcluded,
            lastScheduledDate: lastScheduledDate,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String recipeId,
            Value<int?> stars = const Value.absent(),
            Value<int> timesScheduled = const Value.absent(),
            Value<int> timesManuallyRemoved = const Value.absent(),
            Value<bool> isPinned = const Value.absent(),
            Value<bool> isExcluded = const Value.absent(),
            Value<int?> lastScheduledDate = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              RecipeRatingsTableCompanion.insert(
            recipeId: recipeId,
            stars: stars,
            timesScheduled: timesScheduled,
            timesManuallyRemoved: timesManuallyRemoved,
            isPinned: isPinned,
            isExcluded: isExcluded,
            lastScheduledDate: lastScheduledDate,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$RecipeRatingsTableTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $RecipeRatingsTableTable,
    RecipeRatingsTableData,
    $$RecipeRatingsTableTableFilterComposer,
    $$RecipeRatingsTableTableOrderingComposer,
    $$RecipeRatingsTableTableAnnotationComposer,
    $$RecipeRatingsTableTableCreateCompanionBuilder,
    $$RecipeRatingsTableTableUpdateCompanionBuilder,
    (
      RecipeRatingsTableData,
      BaseReferences<_$AppDatabase, $RecipeRatingsTableTable,
          RecipeRatingsTableData>
    ),
    RecipeRatingsTableData,
    PrefetchHooks Function()>;
typedef $$MealSlotConfigsTableTableCreateCompanionBuilder
    = MealSlotConfigsTableCompanion Function({
  Value<int> dayOfWeek,
  Value<bool> hasBreakfast,
  Value<bool> hasLunch,
  Value<bool> hasDinner,
  Value<int> snackCount,
});
typedef $$MealSlotConfigsTableTableUpdateCompanionBuilder
    = MealSlotConfigsTableCompanion Function({
  Value<int> dayOfWeek,
  Value<bool> hasBreakfast,
  Value<bool> hasLunch,
  Value<bool> hasDinner,
  Value<int> snackCount,
});

class $$MealSlotConfigsTableTableFilterComposer
    extends Composer<_$AppDatabase, $MealSlotConfigsTableTable> {
  $$MealSlotConfigsTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get dayOfWeek => $composableBuilder(
      column: $table.dayOfWeek, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get hasBreakfast => $composableBuilder(
      column: $table.hasBreakfast, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get hasLunch => $composableBuilder(
      column: $table.hasLunch, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get hasDinner => $composableBuilder(
      column: $table.hasDinner, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get snackCount => $composableBuilder(
      column: $table.snackCount, builder: (column) => ColumnFilters(column));
}

class $$MealSlotConfigsTableTableOrderingComposer
    extends Composer<_$AppDatabase, $MealSlotConfigsTableTable> {
  $$MealSlotConfigsTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get dayOfWeek => $composableBuilder(
      column: $table.dayOfWeek, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get hasBreakfast => $composableBuilder(
      column: $table.hasBreakfast,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get hasLunch => $composableBuilder(
      column: $table.hasLunch, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get hasDinner => $composableBuilder(
      column: $table.hasDinner, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get snackCount => $composableBuilder(
      column: $table.snackCount, builder: (column) => ColumnOrderings(column));
}

class $$MealSlotConfigsTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $MealSlotConfigsTableTable> {
  $$MealSlotConfigsTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get dayOfWeek =>
      $composableBuilder(column: $table.dayOfWeek, builder: (column) => column);

  GeneratedColumn<bool> get hasBreakfast => $composableBuilder(
      column: $table.hasBreakfast, builder: (column) => column);

  GeneratedColumn<bool> get hasLunch =>
      $composableBuilder(column: $table.hasLunch, builder: (column) => column);

  GeneratedColumn<bool> get hasDinner =>
      $composableBuilder(column: $table.hasDinner, builder: (column) => column);

  GeneratedColumn<int> get snackCount => $composableBuilder(
      column: $table.snackCount, builder: (column) => column);
}

class $$MealSlotConfigsTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $MealSlotConfigsTableTable,
    MealSlotConfigsTableData,
    $$MealSlotConfigsTableTableFilterComposer,
    $$MealSlotConfigsTableTableOrderingComposer,
    $$MealSlotConfigsTableTableAnnotationComposer,
    $$MealSlotConfigsTableTableCreateCompanionBuilder,
    $$MealSlotConfigsTableTableUpdateCompanionBuilder,
    (
      MealSlotConfigsTableData,
      BaseReferences<_$AppDatabase, $MealSlotConfigsTableTable,
          MealSlotConfigsTableData>
    ),
    MealSlotConfigsTableData,
    PrefetchHooks Function()> {
  $$MealSlotConfigsTableTableTableManager(
      _$AppDatabase db, $MealSlotConfigsTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$MealSlotConfigsTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$MealSlotConfigsTableTableOrderingComposer(
                  $db: db, $table: table),
          createComputedFieldComposer: () =>
              $$MealSlotConfigsTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> dayOfWeek = const Value.absent(),
            Value<bool> hasBreakfast = const Value.absent(),
            Value<bool> hasLunch = const Value.absent(),
            Value<bool> hasDinner = const Value.absent(),
            Value<int> snackCount = const Value.absent(),
          }) =>
              MealSlotConfigsTableCompanion(
            dayOfWeek: dayOfWeek,
            hasBreakfast: hasBreakfast,
            hasLunch: hasLunch,
            hasDinner: hasDinner,
            snackCount: snackCount,
          ),
          createCompanionCallback: ({
            Value<int> dayOfWeek = const Value.absent(),
            Value<bool> hasBreakfast = const Value.absent(),
            Value<bool> hasLunch = const Value.absent(),
            Value<bool> hasDinner = const Value.absent(),
            Value<int> snackCount = const Value.absent(),
          }) =>
              MealSlotConfigsTableCompanion.insert(
            dayOfWeek: dayOfWeek,
            hasBreakfast: hasBreakfast,
            hasLunch: hasLunch,
            hasDinner: hasDinner,
            snackCount: snackCount,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$MealSlotConfigsTableTableProcessedTableManager
    = ProcessedTableManager<
        _$AppDatabase,
        $MealSlotConfigsTableTable,
        MealSlotConfigsTableData,
        $$MealSlotConfigsTableTableFilterComposer,
        $$MealSlotConfigsTableTableOrderingComposer,
        $$MealSlotConfigsTableTableAnnotationComposer,
        $$MealSlotConfigsTableTableCreateCompanionBuilder,
        $$MealSlotConfigsTableTableUpdateCompanionBuilder,
        (
          MealSlotConfigsTableData,
          BaseReferences<_$AppDatabase, $MealSlotConfigsTableTable,
              MealSlotConfigsTableData>
        ),
        MealSlotConfigsTableData,
        PrefetchHooks Function()>;
typedef $$BatchCookingGroupsTableTableCreateCompanionBuilder
    = BatchCookingGroupsTableCompanion Function({
  required String id,
  required String category,
  required String days,
  required int batchNumber,
  Value<int> rowid,
});
typedef $$BatchCookingGroupsTableTableUpdateCompanionBuilder
    = BatchCookingGroupsTableCompanion Function({
  Value<String> id,
  Value<String> category,
  Value<String> days,
  Value<int> batchNumber,
  Value<int> rowid,
});

class $$BatchCookingGroupsTableTableFilterComposer
    extends Composer<_$AppDatabase, $BatchCookingGroupsTableTable> {
  $$BatchCookingGroupsTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get category => $composableBuilder(
      column: $table.category, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get days => $composableBuilder(
      column: $table.days, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get batchNumber => $composableBuilder(
      column: $table.batchNumber, builder: (column) => ColumnFilters(column));
}

class $$BatchCookingGroupsTableTableOrderingComposer
    extends Composer<_$AppDatabase, $BatchCookingGroupsTableTable> {
  $$BatchCookingGroupsTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get category => $composableBuilder(
      column: $table.category, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get days => $composableBuilder(
      column: $table.days, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get batchNumber => $composableBuilder(
      column: $table.batchNumber, builder: (column) => ColumnOrderings(column));
}

class $$BatchCookingGroupsTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $BatchCookingGroupsTableTable> {
  $$BatchCookingGroupsTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get category =>
      $composableBuilder(column: $table.category, builder: (column) => column);

  GeneratedColumn<String> get days =>
      $composableBuilder(column: $table.days, builder: (column) => column);

  GeneratedColumn<int> get batchNumber => $composableBuilder(
      column: $table.batchNumber, builder: (column) => column);
}

class $$BatchCookingGroupsTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $BatchCookingGroupsTableTable,
    BatchCookingGroupsTableData,
    $$BatchCookingGroupsTableTableFilterComposer,
    $$BatchCookingGroupsTableTableOrderingComposer,
    $$BatchCookingGroupsTableTableAnnotationComposer,
    $$BatchCookingGroupsTableTableCreateCompanionBuilder,
    $$BatchCookingGroupsTableTableUpdateCompanionBuilder,
    (
      BatchCookingGroupsTableData,
      BaseReferences<_$AppDatabase, $BatchCookingGroupsTableTable,
          BatchCookingGroupsTableData>
    ),
    BatchCookingGroupsTableData,
    PrefetchHooks Function()> {
  $$BatchCookingGroupsTableTableTableManager(
      _$AppDatabase db, $BatchCookingGroupsTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$BatchCookingGroupsTableTableFilterComposer(
                  $db: db, $table: table),
          createOrderingComposer: () =>
              $$BatchCookingGroupsTableTableOrderingComposer(
                  $db: db, $table: table),
          createComputedFieldComposer: () =>
              $$BatchCookingGroupsTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> category = const Value.absent(),
            Value<String> days = const Value.absent(),
            Value<int> batchNumber = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              BatchCookingGroupsTableCompanion(
            id: id,
            category: category,
            days: days,
            batchNumber: batchNumber,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String id,
            required String category,
            required String days,
            required int batchNumber,
            Value<int> rowid = const Value.absent(),
          }) =>
              BatchCookingGroupsTableCompanion.insert(
            id: id,
            category: category,
            days: days,
            batchNumber: batchNumber,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$BatchCookingGroupsTableTableProcessedTableManager
    = ProcessedTableManager<
        _$AppDatabase,
        $BatchCookingGroupsTableTable,
        BatchCookingGroupsTableData,
        $$BatchCookingGroupsTableTableFilterComposer,
        $$BatchCookingGroupsTableTableOrderingComposer,
        $$BatchCookingGroupsTableTableAnnotationComposer,
        $$BatchCookingGroupsTableTableCreateCompanionBuilder,
        $$BatchCookingGroupsTableTableUpdateCompanionBuilder,
        (
          BatchCookingGroupsTableData,
          BaseReferences<_$AppDatabase, $BatchCookingGroupsTableTable,
              BatchCookingGroupsTableData>
        ),
        BatchCookingGroupsTableData,
        PrefetchHooks Function()>;
typedef $$OptimizerRulesTableTableCreateCompanionBuilder
    = OptimizerRulesTableCompanion Function({
  required String id,
  required String type,
  required String target,
  required String targetName,
  required String constraint,
  required int value,
  Value<int> rowid,
});
typedef $$OptimizerRulesTableTableUpdateCompanionBuilder
    = OptimizerRulesTableCompanion Function({
  Value<String> id,
  Value<String> type,
  Value<String> target,
  Value<String> targetName,
  Value<String> constraint,
  Value<int> value,
  Value<int> rowid,
});

class $$OptimizerRulesTableTableFilterComposer
    extends Composer<_$AppDatabase, $OptimizerRulesTableTable> {
  $$OptimizerRulesTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get type => $composableBuilder(
      column: $table.type, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get target => $composableBuilder(
      column: $table.target, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get targetName => $composableBuilder(
      column: $table.targetName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get constraint => $composableBuilder(
      column: $table.constraint, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get value => $composableBuilder(
      column: $table.value, builder: (column) => ColumnFilters(column));
}

class $$OptimizerRulesTableTableOrderingComposer
    extends Composer<_$AppDatabase, $OptimizerRulesTableTable> {
  $$OptimizerRulesTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get type => $composableBuilder(
      column: $table.type, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get target => $composableBuilder(
      column: $table.target, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get targetName => $composableBuilder(
      column: $table.targetName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get constraint => $composableBuilder(
      column: $table.constraint, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get value => $composableBuilder(
      column: $table.value, builder: (column) => ColumnOrderings(column));
}

class $$OptimizerRulesTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $OptimizerRulesTableTable> {
  $$OptimizerRulesTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get type =>
      $composableBuilder(column: $table.type, builder: (column) => column);

  GeneratedColumn<String> get target =>
      $composableBuilder(column: $table.target, builder: (column) => column);

  GeneratedColumn<String> get targetName => $composableBuilder(
      column: $table.targetName, builder: (column) => column);

  GeneratedColumn<String> get constraint => $composableBuilder(
      column: $table.constraint, builder: (column) => column);

  GeneratedColumn<int> get value =>
      $composableBuilder(column: $table.value, builder: (column) => column);
}

class $$OptimizerRulesTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $OptimizerRulesTableTable,
    OptimizerRulesTableData,
    $$OptimizerRulesTableTableFilterComposer,
    $$OptimizerRulesTableTableOrderingComposer,
    $$OptimizerRulesTableTableAnnotationComposer,
    $$OptimizerRulesTableTableCreateCompanionBuilder,
    $$OptimizerRulesTableTableUpdateCompanionBuilder,
    (
      OptimizerRulesTableData,
      BaseReferences<_$AppDatabase, $OptimizerRulesTableTable,
          OptimizerRulesTableData>
    ),
    OptimizerRulesTableData,
    PrefetchHooks Function()> {
  $$OptimizerRulesTableTableTableManager(
      _$AppDatabase db, $OptimizerRulesTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$OptimizerRulesTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$OptimizerRulesTableTableOrderingComposer(
                  $db: db, $table: table),
          createComputedFieldComposer: () =>
              $$OptimizerRulesTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> type = const Value.absent(),
            Value<String> target = const Value.absent(),
            Value<String> targetName = const Value.absent(),
            Value<String> constraint = const Value.absent(),
            Value<int> value = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              OptimizerRulesTableCompanion(
            id: id,
            type: type,
            target: target,
            targetName: targetName,
            constraint: constraint,
            value: value,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String id,
            required String type,
            required String target,
            required String targetName,
            required String constraint,
            required int value,
            Value<int> rowid = const Value.absent(),
          }) =>
              OptimizerRulesTableCompanion.insert(
            id: id,
            type: type,
            target: target,
            targetName: targetName,
            constraint: constraint,
            value: value,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$OptimizerRulesTableTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $OptimizerRulesTableTable,
    OptimizerRulesTableData,
    $$OptimizerRulesTableTableFilterComposer,
    $$OptimizerRulesTableTableOrderingComposer,
    $$OptimizerRulesTableTableAnnotationComposer,
    $$OptimizerRulesTableTableCreateCompanionBuilder,
    $$OptimizerRulesTableTableUpdateCompanionBuilder,
    (
      OptimizerRulesTableData,
      BaseReferences<_$AppDatabase, $OptimizerRulesTableTable,
          OptimizerRulesTableData>
    ),
    OptimizerRulesTableData,
    PrefetchHooks Function()>;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$RecipesTableTableTableManager get recipesTable =>
      $$RecipesTableTableTableManager(_db, _db.recipesTable);
  $$RecipeIngredientsTableTableTableManager get recipeIngredientsTable =>
      $$RecipeIngredientsTableTableTableManager(
          _db, _db.recipeIngredientsTable);
  $$IngredientsTableTableTableManager get ingredientsTable =>
      $$IngredientsTableTableTableManager(_db, _db.ingredientsTable);
  $$MealPlansTableTableTableManager get mealPlansTable =>
      $$MealPlansTableTableTableManager(_db, _db.mealPlansTable);
  $$DayPlansTableTableTableManager get dayPlansTable =>
      $$DayPlansTableTableTableManager(_db, _db.dayPlansTable);
  $$MealSlotsTableTableTableManager get mealSlotsTable =>
      $$MealSlotsTableTableTableManager(_db, _db.mealSlotsTable);
  $$RecipeRatingsTableTableTableManager get recipeRatingsTable =>
      $$RecipeRatingsTableTableTableManager(_db, _db.recipeRatingsTable);
  $$MealSlotConfigsTableTableTableManager get mealSlotConfigsTable =>
      $$MealSlotConfigsTableTableTableManager(_db, _db.mealSlotConfigsTable);
  $$BatchCookingGroupsTableTableTableManager get batchCookingGroupsTable =>
      $$BatchCookingGroupsTableTableTableManager(
          _db, _db.batchCookingGroupsTable);
  $$OptimizerRulesTableTableTableManager get optimizerRulesTable =>
      $$OptimizerRulesTableTableTableManager(_db, _db.optimizerRulesTable);
}
