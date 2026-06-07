# FoodPlan Optimizer — Implementation Brief

**Cloud Function (Python) & Android Integration**
*Two self-contained briefs for separate implementation assistants*

---

# Brief 1 — Python Cloud Function

A Firebase callable Cloud Function written in Python that reads a user's meal planning data from Firestore, solves a weekly meal assignment problem using OR-Tools CP-SAT, and writes the resulting meal plan back to Firestore.

## Project Structure

```
functions/
  main.py
  requirements.txt
```

### requirements.txt

```
firebase-functions
firebase-admin
ortools
```

---

## Firestore Schema

### Global collections (no user prefix)

#### `ingredients/{ingredientId}`

| Field | Type | Description |
|---|---|---|
| id | string | Unique identifier |
| name | string | Display name |
| kcalPer100g | number | |
| proteinPer100g | number | |
| fatPer100g | number | |
| carbsPer100g | number | |
| category | string | One of: `FRUIT_VEG`, `MEAT`, `FISH`, `DAIRY_EGGS`, `CHEESE`, `GRAINS`, `BREAD_BAKERY`, `DRY_GOODS`, `NUTS`, `CANNED`, `FROZEN`, `OILS_SAUCES`, `SPICES`, `DRINKS`, `SUPPLEMENT`, `OTHER` |
| source | string | One of: `LABEL`, `LIVSMEDELSVERKET`, `CALCULATED`, `BARCODE` |
| steps | list[string] | Optional preparation notes |

#### `recipes/{recipeId}`

| Field | Type | Description |
|---|---|---|
| id | string | Unique identifier |
| name | string | Display name |
| type | string | `MEAL` or `COMPONENT` |
| mealCategories | list[string] | `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK` — set on MEAL type, empty on COMPONENT |
| componentCategory | string\|null | `SAUCE`, `DRESSING`, `SALSA`, `SALAD`, `SIDE`, `OTHER` — only on COMPONENT type |
| steps | list[string] | Cooking steps |
| notes | string | Free-text notes |
| ingredients | list[map] | See sub-fields below |

Each ingredient entry has exactly one of `ingredientId` or `subRecipeId` set:

| Field | Type | Description |
|---|---|---|
| ingredientId | string\|null | References `ingredients` collection. If set, `grams` must be set. |
| subRecipeId | string\|null | References a recipe with `type=COMPONENT`. If set, `portions` must be set. |
| grams | number\|null | Amount in grams — used when `ingredientId` is set |
| portions | number\|null | Number of portions — used when `subRecipeId` is set |

### User collections

#### `users/{userId}/ratings/{recipeId}`

> ℹ️ Document ID is the `recipeId` — upserts are idempotent.
>
> ⚠️ `recipeId` is **not** stored as a document field — it **is** the document ID. The Python function must use the document ID as the key when building the ratings map.

| Field | Type | Description |
|---|---|---|
| stars | number\|null | 1–5; null if unrated |
| isExcluded | bool | Hard filter — recipe never appears in plans |
| isPinned | bool | Reserved for future use |
| lastScheduledDate | timestamp\|null | Most recent date the recipe was in a generated plan |
| timesScheduled | int | Total times scheduled |
| timesManuallyRemoved | int | Times user removed this recipe from a plan |

#### `users/{userId}/settings`

Single document. All sub-fields below.

**schedule:**

| Field | Type | Description |
|---|---|---|
| mealSlots | map | Keys: `MONDAY`…`SUNDAY`. Each value is a `DayMealConfig` map (see below). |
| batchGroups | list[map] | Each entry: `{ meal: string, days: list[int], batchNumber: int }`. `days` are ISO integers (1=Monday … 7=Sunday). |

`DayMealConfig` fields:

| Field | Type | Description |
|---|---|---|
| breakfast | bool | |
| lunch | bool | |
| dinner | bool | |
| snackCount | int | `0` = no snacks. `-1` = unlimited (optimizer decides how many, up to pool size — no hardcoded ceiling). `1/2/3` = exactly that many required slots; optimizer may add more optional ones. |

> ⚠️ `snackCount = -1` means **unlimited** — any number of snacks up to the full SNACK recipe pool size. There is no hardcoded upper bound. Optional slots left empty are omitted from output. Only `snackCount = 0` means no snacks.

**goals:**

| Field | Type | Description |
|---|---|---|
| kcalTarget | number | Daily kcal target |
| proteinTarget | number\|null | g/day — null if auto-derived |
| fatTarget | number\|null | g/day — null if auto-derived |
| carbsTarget | number\|null | g/day — null if auto-derived |
| autoField | string | `PROTEIN`, `FAT`, or `CARBS` — whichever is derived from the others |
| minKcalPerDay | number\|null | Hard lower bound per day |
| maxKcalPerDay | number\|null | Hard upper bound per day |
| minProteinPerDay | number\|null | |
| maxProteinPerDay | number\|null | |
| minFatPerDay | number\|null | |
| maxFatPerDay | number\|null | |
| minCarbsPerDay | number\|null | |
| maxCarbsPerDay | number\|null | |

**variety:**

| Field | Type | Description |
|---|---|---|
| level | string | `FLEXIBLE`, `BALANCED`, or `STRICT` |
| lunchDinnerSharedRecency | bool | LUNCH and DINNER share one recency index |
| breakfastSnackSharedRecency | bool | BREAKFAST and SNACK share one recency index |
| proteinSourceVariety | bool | Penalise same dominant protein source across lunch/dinner |
| perCategory | map | Keys: `LUNCH`, `DINNER`, `BREAKFAST`, `SNACK`. Each value: `{ maxTimesPerWeek: int\|null, maxConsecutiveDays: int\|null }`. null means unlimited. |

**diet:**

| Field | Type | Description |
|---|---|---|
| dietTypes | list[string] | `VEGETARIAN`, `VEGAN`, `PESCATARIAN`, `KETO` |
| allergies | list[string] | `GLUTEN`, `DAIRY`, `NUTS`, `SHELLFISH`, `EGGS`, `SOY`, `PORK` |
| excludedIngredientIds | list[string] | Hard filter — recipes containing these ingredients are excluded |
| preferredIngredientIds | list[string] | Soft scoring — future use |
| dislikedIngredientIds | list[string] | Soft scoring — future use |

**proteinPowder** (null if not configured):

| Field | Type | Description |
|---|---|---|
| ingredientId | string | References `ingredients` collection |
| name | string | Display name shown in UI |
| proteinPer100g | number | |
| kcalPer100g | number | |
| gramsInStock | number | UI stock warning only — not used by optimizer |
| autoFillGap | bool | If false, powder is never assigned |
| lowStockWarning | bool | UI only — not used by optimizer |

**rules:**

| Field | Type | Description |
|---|---|---|
| id | string | |
| type | string | `INGREDIENT` or `DIET_CATEGORY` |
| target | string | `ingredientId` or diet category name |
| targetName | string | Human-readable display name |
| constraint | string | `MIN_PER_WEEK` or `MAX_PER_WEEK` |
| value | int | The N in "min/max N per week" |

**shopping:**

| Field | Type | Description |
|---|---|---|
| shoppingDays | list[int] | ISO day numbers (1=Monday … 7=Sunday) |
| intervalWeeks | int | How often shopping occurs |

> ℹ️ `shopping` is not used by the optimizer but must be stored so the Android app can round-trip settings through Firestore.

#### `users/{userId}/mealPlans/{planId}`

Written by the Cloud Function after solving. Read by the Android app via its Firestore listener.

| Field | Type | Description |
|---|---|---|
| id | string | UUID |
| name | string | e.g. `Week 21 – 19 May 2025` |
| startDate | timestamp | Monday of the planned week |
| endDate | timestamp | Sunday of the planned week |
| days | list[map] | One entry per active day |

`DayPlan` fields:

| Field | Type | Description |
|---|---|---|
| id | string | UUID |
| date | timestamp | |
| meals | list[map] | `{ type: string, recipeId: string }`. Null snack slots omitted. |
| proteinPowderGrams | number | >= 0.0 |
| kcalTarget | int | Stored flat — **not** nested in a `goal` map |
| proteinTarget | int | Stored flat — **not** nested in a `goal` map |

> ⚠️ `kcalTarget` and `proteinTarget` are stored as flat fields on `DayPlan`, not nested inside a `goal` sub-map. This matches the Room `DayPlanEntity` schema the Android app uses.

---

## What the Function Reads from Firestore

- All documents from the `ingredients` collection
- All documents from the `recipes` collection — both `MEAL` and `COMPONENT` types are needed for sub-recipe nutrition resolution
- All documents from `users/{userId}/ratings`
- Document `users/{userId}/settings`
- Recent meal plans from `users/{userId}/mealPlans` — query by `startDate` descending, limit to enough weeks to cover the recency window: FLEXIBLE=2 weeks, BALANCED=4 weeks, STRICT=6 weeks

---

## Nutrition Pre-computation

Before building the CP-SAT model, compute `RecipeNutrition(kcal, protein, fat, carbs)` for every recipe. This requires iterative resolution because MEAL recipes can reference COMPONENT recipes via `subRecipeId`.

**Algorithm:**

1. `resolved = {}` (empty dict)
2. `remaining = all recipes` (MEAL and COMPONENT)
3. Loop until `remaining` is empty or no progress was made in a full pass:
   - For each recipe in `remaining`: if all `subRecipeId` references in its ingredients are already in `resolved`, compute its nutrition and add to `resolved`
     - Leaf ingredient: `(grams / 100) × ingredient macros` (using `kcalPer100g`, `proteinPer100g`, `fatPer100g`, `carbsPer100g`)
     - Sub-recipe reference: `portions × resolved[subRecipeId]` — where `resolved[subRecipeId]` is the component's **total** `RecipeNutrition`, not a per-100g value. Multiply `portions` by the full nutrition object.
4. If a recipe remains unresolved after the loop (circular reference): skip it — do not include in the optimizer pool

---

## Goals Resolution

One of `proteinTarget`, `fatTarget`, `carbsTarget` will be null (the `autoField`). Derive it:

- protein kcal = protein × 4
- fat kcal = fat × 9
- carbs kcal = carbs × 4
- auto field = `(kcalTarget − sum of the other two macro kcal totals) / its own kcal-per-gram coefficient`

---

## Recency

### Window by level

| Level | Window |
|---|---|
| FLEXIBLE | 14 days |
| BALANCED | 28 days |
| STRICT | 42 days |

### Recency groups

Two configurable groups control which categories share a recency index:

- If `lunchDinnerSharedRecency`: LUNCH and DINNER share one index — a recipe used in either counts against both
- If `breakfastSnackSharedRecency`: BREAKFAST and SNACK share one index
- Otherwise each category has its own independent index

Penalty for a recipe last used N days ago:

```
penalty = max(0.0,  1.0 - N / windowDays)
```

Continuous value 0.0–1.0 included in the objective as a soft cost.

### Within-week recency

The recency penalty must also account for earlier slots within the same week being planned, not just historical plans. If Monday's dinner is tuna, Tuesday's dinner tuna penalty must include Monday's assignment — not only the historical last-used date.

**Implementation:** maintain a running "last used this week" map keyed by `(recencyGroup, recipeId)` while processing slots in date order. For each slot, use the more recent of the historical date and the within-week date when computing the penalty. Update the map after scoring each slot.

---

## Recipe Filtering

Exclude a recipe from the optimizer pool if any of the following are true:

- `type != MEAL`
- `rating.isExcluded == true`
- `rating.stars == 1`
- `mealCategories` has no overlap with any active slot in the schedule
- Recipe contains an `ingredientId` listed in `diet.excludedIngredientIds`
- STRICT mode only: `lastScheduledDate` is within the recency window (hard pre-filter)

> ⚠️ When checking whether the SNACK category is "active", treat `snackCount = -1` as active (unlimited snacks). Only `snackCount = 0` means inactive. The condition is `snackCount != 0`, not `snackCount > 0`.

---

## CP-SAT Model

### Decision variables

**Meal assignment:**
```
x[day][meal_type][recipe_id]  in {0, 1}
```
`day` = 0..6 (Monday=0). Only create `x[d][m][r]` when `meal_type` is in `recipe.mealCategories`.

**Batch group assignment:**
```
y[batch_group_index][recipe_id]  in {0, 1}
```
One per batch group per eligible recipe for that group's meal category.

**Snack slots:**

- `snackCount = 0`: no snack variables for this day
- `snackCount = -1` (unlimited): create optional snack variables up to the size of the SNACK recipe pool — no hardcoded ceiling
- `snackCount = 1/2/3`: create that many required slots (must be filled) plus additional optional slots up to the pool size

Optional slots use a `NULL_SNACK` sentinel (contributes zero macros). Slots remaining as `NULL_SNACK` in the solution are omitted from the output `meals` list.

**Protein powder (continuous per day):**
```
powder_grams[day]  >= 0
```
CP-SAT requires integers. Scale by 10 (units of 0.1g) throughout the model. Divide by 10 when writing `proteinPowderGrams` to Firestore.

### Hard constraints

**H1 — One recipe per required slot:**
```
sum(x[d][m][r] for r in pool)  == 1
```
For each `(day, meal_type)` that is active and not a batch slot.

**H2 — Batch group consistency:**
```
x[d][meal_type][r]  ==  y[g][r]   for all d in group g, for all r
sum(y[g][r] for r in pool)  ==  1  for each group g
```
Different batch groups for the same meal category must get different recipes:
```
y[g1][r] + y[g2][r]  <=  1   for all r, all pairs g1 != g2 with same meal_type
```

**H3 — Recipe category eligibility:** enforced implicitly by only creating `x[d][m][r]` when `meal_type` is in `recipe.mealCategories`.

**H4 — Per-day hard bounds** (when set in goals):
```
sum(nutrition[r].kcal * x[d][m][r] for m,r) + powder_kcal[d]  >= minKcalPerDay
sum(nutrition[r].kcal * x[d][m][r] for m,r) + powder_kcal[d]  <= maxKcalPerDay
```
Same pattern for protein, fat, carbs.

**H5 — maxTimesPerWeek per category (batch-exempt):**
```
sum(x[d][meal_type][r] for d in week)  <= maxTimesPerWeek
```
For each recipe `r` and `meal_type` with a limit set. Batch slots not counted.

**H6 — maxConsecutiveDays per category (batch-exempt):** for each recipe `r` and `meal_type`, for each window of `(maxConsecutiveDays + 1)` consecutive active days:
```
sum(x[d][meal_type][r] for d in window)  <= maxConsecutiveDays
```

### Protein powder constraints

```
powder_protein[d] = powder_grams[d] * proteinPer100g / 100
powder_kcal[d]    = powder_grams[d] * kcalPer100g / 100
powder_grams[d]   <= 100 * 10   (max 100g, scaled by 10)
```

If `autoFillGap == false` or `proteinPowder` is null: `powder_grams[d] == 0` for all days.

### Objective function

Minimise (scale all float coefficients by 1000 for integer arithmetic):

```
  3.0 * (weekly_kcal_over + weekly_kcal_under)       / weeklyKcalTarget
+ 2.0 * (weekly_protein_over + weekly_protein_under) / weeklyProteinTarget
+ 1.0 * (weekly_fat_over + weekly_fat_under)         / weeklyFatTarget
+ 1.0 * (weekly_carbs_over + weekly_carbs_under)     / weeklyCarbsTarget
+ penalty_weight * sum(recency_penalty[d][m][r] * x[d][m][r] for d,m,r)
+ penalty_weight * sum(recency_penalty[g][r]    * y[g][r]    for g,r)
+ 0.5 * total_powder_grams / (100 * activeDays)
```

Where:

- `weekly_kcal_over`, `weekly_kcal_under` are deviation variables: `weeklyKcal − target = over − under`, both >= 0
- `weeklyKcalTarget = kcalTarget × activeDays` (same pattern for protein, fat, carbs)
- `penalty_weight`: FLEXIBLE=0.2, BALANCED=0.6, STRICT=1.5
- `recency_penalty[d][m][r]` uses the within-week-aware calculation — not just the history index
- Batch group `y[g][r]` variables also carry recency penalties for the days in each group
- Powder penalised lightly to prefer food-based nutrition when macros are already met

**Custom rules soft penalty** (add to objective):

- For each `INGREDIENT` rule: count total appearances of recipes containing `target` ingredientId across all meal slots
- `MIN_PER_WEEK`: add penalty proportional to `(value − count)` when `count < value`
- `MAX_PER_WEEK`: add penalty proportional to `(count − value)` when `count > value`
- `DIET_CATEGORY` rules: no-op for now — recipe diet tags not yet implemented

### Solver settings

```python
solver = cp_model.CpSolver()
solver.parameters.max_time_in_seconds = 30.0
solver.parameters.num_search_workers  = 4
status = solver.Solve(model)
# Accept OPTIMAL or FEASIBLE. Return error if INFEASIBLE.
```

---

## Output — Writing to Firestore

After solving, write one document to `users/{userId}/mealPlans/{planId}`. See schema above.

`proteinPowderGrams` per day: compute analytically after fixing recipe assignments — close the daily protein gap up to the kcal ceiling and the 100g cap. If using scaled integers internally, divide by 10 before writing.

Only include days where `isActive == true` (`snackCount != 0` counts as active). Omit `NULL_SNACK` slots from the `meals` list.

---

## Function Signature

```python
@https_fn.on_call(timeout_sec=60)
def optimise_meal_plan(req: https_fn.CallableRequest) -> dict:
    user_id        = req.data.get("userId")
    start_date_str = req.data.get("startDate")  # ISO string e.g. "2025-05-19"
    # read Firestore → solve → write plan → return {"success": True, "planId": planId}
```

---
---

# Brief 2 — Android Integration

Replace the local `MealPlanOptimizer` with a call to the Firebase Cloud Function. Add Firestore repositories for recipes, ingredients, and ratings. Keep DataStore for settings. Keep Room as a local cache for meal plans, synced from Firestore.

---

## Step 0 — Apply Updated Source Files First

The codebase you receive has an older version of several files. Before doing any Firestore or function work, replace the following files with the versions provided alongside this brief:

| File | Package | What changed |
|---|---|---|
| `MealPlanConfig.kt` | `domain/model/` | New `VarietyConfig` model with `level`, recency groups, per-category limits |
| `MealPlanOptimizer.kt` | `domain/usecase/` | Full optimizer (will be replaced by remote call, but model types are needed) |
| `SettingsRepository.kt` | `data/repository/` | Updated DataStore keys for new `VarietyConfig` |
| `ScheduleTab.kt` | `ui/settings/` | Updated snack counter and variety card UI |
| `RulesTab.kt` | `ui/settings/` | Variety section removed |
| `GenerateMealPlanUseCase.kt` | `domain/usecase/` | Uses `config.variety.level.recencyWindowWeeks` instead of removed field |

> ⚠️ Do not skip this step. The Firestore schema in Brief 1 matches the new `MealPlanConfig`. If you proceed with the old codebase the settings sync and plan parsing will be wrong.

---

## Known Bugs to Fix

### 1. `isActive` with unlimited snacks

In `DayMealConfig`, the current code reads:

```kotlin
val isActive: Boolean get() = breakfast || lunch || dinner || snackCount > 0
```

This incorrectly treats `snackCount = -1` (unlimited) as inactive. Change to:

```kotlin
val isActive: Boolean get() = breakfast || lunch || dinner || snackCount != 0
```

### 2. `filterRecipes` SNACK category detection

In `GenerateMealPlanUseCase.filterRecipes`, the active categories check uses `snackCount > 0`. Change the snack condition to `snackCount != 0` so that unlimited days include SNACK in their active categories.

> ✎ Both occurrences of `snackCount > 0` must become `snackCount != 0` — one in `DayMealConfig.isActive` and one in `filterRecipes`.

### 3. Room schema cannot store multiple snack slots per day

`MealSlotEntity` uses a composite primary key of `(dayPlanId, type)`. This allows only one slot per `MealCategory` per day. Multiple snack slots on the same day will silently overwrite each other.

Fix: add a `slotIndex` column and change the primary key. This requires a Room migration.

```kotlin
@Entity(
    tableName = "meal_slots",
    primaryKeys = ["dayPlanId", "type", "slotIndex"],
    foreignKeys = [ForeignKey(
        entity = DayPlanEntity::class,
        parentColumns = ["id"],
        childColumns = ["dayPlanId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MealSlotEntity(
    val dayPlanId:  String,
    val type:       MealCategory,
    val slotIndex:  Int = 0,
    val recipeId:   String
)
```

---

## Dependencies

Add to `build.gradle`:

```kotlin
implementation("com.google.firebase:firebase-functions-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
```

---

## RemoteMealPlanOptimizer

Replace `MealPlanOptimizer.kt` with a thin wrapper after applying the Step 0 files:

```kotlin
class RemoteMealPlanOptimizer @Inject constructor(
    private val functions: FirebaseFunctions
) {
    suspend fun generate(userId: String, startDate: LocalDate): String {
        val result = functions
            .getHttpsCallable("optimise_meal_plan")
            .call(mapOf(
                "userId"    to userId,
                "startDate" to startDate.toString()
            ))
            .await()
        return (result.data as Map<*, *>)["planId"] as String
    }
}
```

---

## Updated GenerateMealPlanUseCase

Remove all local fetching and optimizer calls. Call `RemoteMealPlanOptimizer`, then fetch the plan from Room (which receives it via the Firestore listener):

```kotlin
class GenerateMealPlanUseCase @Inject constructor(
    private val optimizer:          RemoteMealPlanOptimizer,
    private val mealPlanRepository: MealPlanRepository
) {
    suspend operator fun invoke(
        userId:    String,
        startDate: LocalDate
    ): Result<MealPlan> = try {
        val planId = optimizer.generate(userId, startDate)
        val plan   = mealPlanRepository.getMealPlanWithDays(planId)
            ?: error("Plan not found after generation")
        Result.success(plan)
    } catch (e: Exception) { Result.failure(e) }
}
```

> ⚠️ Use `getMealPlanWithDays(planId)` on the repository — not `getMealPlanById` which only returns the top-level entity without days or slots.

---

## New Firestore Repositories

### FirestoreRecipeRepository

Collection: `recipes` (global, no user prefix).

Methods:
- `getAllRecipes(): Flow<List<Recipe>>` — snapshot listener on whole collection
- `getRecipeById(id: String): Recipe?`
- `saveRecipe(recipe: Recipe)`
- `deleteRecipe(id: String)`

Document mapping must include all `Recipe` fields: `id`, `name`, `type`, `mealCategories`, `componentCategory`, `steps`, `notes`, and the full `ingredients` list with `ingredientId`/`subRecipeId` and `grams`/`portions`.

### FirestoreIngredientRepository

Collection: `ingredients` (global, no user prefix).

Methods:
- `getAllIngredients(): Flow<List<Ingredient>>`
- `getIngredientById(id: String): Ingredient?`
- `saveIngredient(ingredient: Ingredient)`
- `deleteIngredient(id: String)`

Document mapping must include all `Ingredient` fields: `id`, `name`, `category`, `kcalPer100g`, `proteinPer100g`, `fatPer100g`, `carbsPer100g`, `source`, `steps`.

### FirestoreRatingRepository

Collection: `users/{userId}/ratings`. Document ID is the `recipeId`.

Methods:
- `getAllRatings(userId: String): Flow<List<RecipeRating>>`
- `saveRating(userId: String, rating: RecipeRating)`

Document mapping must include all `RecipeRating` fields: `stars`, `isExcluded`, `isPinned`, `lastScheduledDate`, `timesScheduled`, `timesManuallyRemoved`.

> ⚠️ `recipeId` is **not** stored as a document field — it **is** the document ID. When reading a rating from Firestore, populate `RecipeRating.recipeId` from `document.id`, not from a field inside the document.

### Updated MealPlanRepository

Keep all existing Room-backed methods unchanged. Add a Firestore snapshot listener that mirrors incoming plan documents to Room:

```kotlin
firestore.collection("users")
    .document(userId)
    .collection("mealPlans")
    .addSnapshotListener { snapshot, _ ->
        snapshot?.documents?.forEach { doc ->
            scope.launch {
                val plan = doc.toMealPlan()
                dao.upsertMealPlan(plan.toEntity())
                plan.days.forEach { day ->
                    dao.upsertDayPlan(day.toEntity(plan.id))
                    day.meals.forEachIndexed { i, slot ->
                        dao.upsertMealSlot(slot.toEntity(day.id, slotIndex = i))
                    }
                }
            }
        }
    }
```

> ⚠️ `DayPlan` stores `kcalTarget` and `proteinTarget` as **flat fields** — not nested in a `goal` map. Map them directly to `DayPlanEntity.kcalTarget` and `DayPlanEntity.proteinTarget` when deserialising from Firestore.

> ⚠️ Room stores `LocalDate` as epoch days (`Long`) in range queries. When writing `DayPlanEntity.date` from a Firestore timestamp, convert using `date.toEpochDays().toLong()`. The DAO method `getDayPlanByDate` takes a `Long` parameter, not a `LocalDate` directly.

> ℹ️ The UI always reads from Room via the DAO. Firestore writes from the Cloud Function propagate to Room via this listener automatically — no manual fetch needed after generation.

---

## Settings Sync — DataStore to Firestore

The Cloud Function reads settings from Firestore. Add a one-way sync in `SettingsRepository` so every DataStore change is mirrored to Firestore. Run in a background coroutine, debounced to avoid a write per slider movement:

```kotlin
settingsFlow
    .debounce(800)
    .collect { config ->
        firestore.document("users/$userId/settings")
            .set(config.toFirestoreMap())
    }
```

`GenerateMealPlanUseCase` should verify the settings document exists in Firestore before calling the function — or the sync should be awaited on first launch before any plan generation is triggered.

> ⚠️ Use `"default_user"` as the hardcoded `userId` everywhere until Firebase Auth is added — in `RemoteMealPlanOptimizer`, `MealPlanRepository`, `FirestoreRatingRepository`, and the settings sync.

---

## MealPlanViewModel Change

Update `generatePlan()` to pass `userId`:

```kotlin
fun generatePlan() {
    viewModelScope.launch {
        _optimizerState.value = OptimizerState.Running
        val result = generateMealPlan(
            userId    = "default_user",
            startDate = weekStart(selectedDate.value)
        )
        _optimizerState.value = result.fold(
            onSuccess = { OptimizerState.Success(it.name) },
            onFailure = { OptimizerState.Error(it.message ?: "Failed") }
        )
    }
}
```

---

## Hilt Module Changes

Add bindings for:
- `FirebaseFunctions` — `FirebaseFunctions.getInstance()`
- `FirebaseFirestore` — `FirebaseFirestore.getInstance()`
- `RemoteMealPlanOptimizer`
- `FirestoreRecipeRepository`
- `FirestoreIngredientRepository`
- `FirestoreRatingRepository`

Replace the existing local `MealPlanOptimizer` binding with `RemoteMealPlanOptimizer`.

---

## Local Emulator Setup

In your `Application` class or a debug initialiser:

```kotlin
if (BuildConfig.DEBUG) {
    FirebaseFunctions.getInstance().useEmulator("10.0.2.2", 5001)
    FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
}
```

Start both emulators:

```bash
firebase emulators:start --only functions,firestore
```

> ⚠️ Both `useEmulator` calls are required — Functions on port 5001, Firestore on port 8080. Missing either one causes silent failures where the app talks to production instead of the emulator.

---

## What Stays Unchanged

- `SettingsRepository` DataStore logic — continues reading/writing locally for instant response
- All settings UI — `ScheduleTab` (after Step 0 replacement), `RulesTab`, nutrition goals
- `MealPlanScreen` and `MealPlanViewModel` UI logic (other than the `userId` change above)
- Room schema for `meal_plans`, `day_plans`, `meal_slot_configs`, `batch_cooking_groups`, `recipe_ratings` tables (except the `MealSlotEntity` migration noted above)
- Shopping list, recipe detail, and all other screens

---
---

# Testing Guide — Python Cloud Function

## Unit Testing the CP-SAT Model

Install dependencies and run with `pytest`:

```bash
cd functions
pip install -r requirements.txt pytest
pytest tests/
```

### Minimal valid input fixture

Create `tests/fixtures/minimal.json` — a small but complete input the solver can run in under a second:

```json
{
  "userId": "test_user",
  "startDate": "2025-05-19",
  "settings": {
    "schedule": {
      "mealSlots": {
        "MONDAY":    { "breakfast": false, "lunch": true, "dinner": true, "snackCount": 0 },
        "TUESDAY":   { "breakfast": false, "lunch": true, "dinner": true, "snackCount": 0 },
        "WEDNESDAY": { "breakfast": false, "lunch": true, "dinner": true, "snackCount": 0 },
        "THURSDAY":  { "breakfast": false, "lunch": true, "dinner": true, "snackCount": 0 },
        "FRIDAY":    { "breakfast": false, "lunch": true, "dinner": true, "snackCount": 1 },
        "SATURDAY":  { "breakfast": false, "lunch": true, "dinner": true, "snackCount": 1 },
        "SUNDAY":    { "breakfast": false, "lunch": false, "dinner": true, "snackCount": 0 }
      },
      "batchGroups": [
        { "meal": "LUNCH", "days": [1,2,3,4,5], "batchNumber": 1 },
        { "meal": "LUNCH", "days": [6],          "batchNumber": 2 }
      ]
    },
    "goals": {
      "kcalTarget": 1800,
      "proteinTarget": 150,
      "fatTarget": null,
      "carbsTarget": 180,
      "autoField": "FAT",
      "maxKcalPerDay": 2200
    },
    "variety": {
      "level": "BALANCED",
      "lunchDinnerSharedRecency": true,
      "breakfastSnackSharedRecency": false,
      "proteinSourceVariety": true,
      "perCategory": {
        "LUNCH":  { "maxTimesPerWeek": null, "maxConsecutiveDays": null },
        "DINNER": { "maxTimesPerWeek": 2,    "maxConsecutiveDays": 2 },
        "BREAKFAST": { "maxTimesPerWeek": null, "maxConsecutiveDays": null },
        "SNACK":  { "maxTimesPerWeek": 3,    "maxConsecutiveDays": null }
      }
    },
    "proteinPowder": {
      "ingredientId": "ing_protein_powder",
      "name": "Whey Protein",
      "proteinPer100g": 72,
      "kcalPer100g": 354,
      "autoFillGap": true
    },
    "diet": { "dietTypes": [], "allergies": [], "excludedIngredientIds": [], "preferredIngredientIds": [], "dislikedIngredientIds": [] },
    "rules": [],
    "shopping": { "shoppingDays": [7], "intervalWeeks": 1 }
  }
}
```

### Suggested test cases

```python
# tests/test_optimizer.py

def test_all_slots_filled(solved_plan):
    for day in solved_plan["days"]:
        types = [m["type"] for m in day["meals"]]
        assert "LUNCH" in types
        assert "DINNER" in types

def test_batch_consistency(solved_plan):
    # Mon–Fri lunch must all share the same recipe
    mon_fri = [d for d in solved_plan["days"] if d["dayOfWeek"] in ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]]
    lunch_ids = [next(m["recipeId"] for m in d["meals"] if m["type"] == "LUNCH") for d in mon_fri]
    assert len(set(lunch_ids)) == 1

def test_weekly_kcal_within_10_pct(solved_plan, goals):
    total = sum(d["kcalActual"] for d in solved_plan["days"])
    target = goals["kcalTarget"] * len(solved_plan["days"])
    assert abs(total - target) / target < 0.10

def test_weekly_protein_within_10_pct(solved_plan, goals):
    total = sum(d["proteinActual"] for d in solved_plan["days"])
    target = goals["proteinTarget"] * len(solved_plan["days"])
    assert abs(total - target) / target < 0.10

def test_dinner_max_times_per_week(solved_plan):
    from collections import Counter
    dinner_ids = [m["recipeId"] for d in solved_plan["days"] for m in d["meals"] if m["type"] == "DINNER"]
    counts = Counter(dinner_ids)
    assert max(counts.values()) <= 2  # maxTimesPerWeek=2

def test_no_null_snacks_in_output(solved_plan):
    for day in solved_plan["days"]:
        for meal in day["meals"]:
            assert meal["recipeId"] != "null_snack"

def test_infeasible_returns_error(empty_recipe_pool):
    result = call_optimizer(empty_recipe_pool)
    assert result["success"] == False
    assert "INFEASIBLE" in result["error"]
```

## Integration Testing Against the Firestore Emulator

Start the emulator:

```bash
firebase emulators:start --only functions,firestore
```

Seed test data and call the function:

```bash
# Seed Firestore emulator with the quality test dataset (see section below)
python tests/seed_emulator.py

# Call the function via the emulator REST endpoint
curl -X POST \
  "http://localhost:5001/YOUR_PROJECT_ID/us-central1/optimise_meal_plan" \
  -H "Content-Type: application/json" \
  -d '{"data": {"userId": "test_user", "startDate": "2025-05-19"}}'
```

For CI, use `firebase emulators:exec` to run tests against a fresh emulator and exit:

```bash
firebase emulators:exec --only functions,firestore "pytest tests/integration/"
```

---

# Testing Guide — Android

## Stubbing RemoteMealPlanOptimizer

Before the Cloud Function exists, replace the real optimizer with a fake that writes a hardcoded plan to the Firestore emulator. This lets you test the full Android flow — button press → Firestore listener → Room → UI — without a working backend.

```kotlin
class FakeRemoteMealPlanOptimizer @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteMealPlanOptimizer {

    override suspend fun generate(userId: String, startDate: LocalDate): String {
        val planId = UUID.randomUUID().toString()
        val plan   = buildHardcodedPlan(planId, startDate)
        firestore.collection("users").document(userId)
            .collection("mealPlans").document(planId)
            .set(plan)
            .await()
        return planId
    }
}
```

Bind `FakeRemoteMealPlanOptimizer` in your debug Hilt module. The Firestore listener in `MealPlanRepository` will pick up the write and push it to Room exactly as the real function would.

## Room Migration Test

Add a migration test to verify the `MealSlotEntity` schema change doesn't drop data:

```kotlin
@RunWith(AndroidJUnit4::class)
class MealSlotMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate_adds_slot_index() {
        // Create DB at old version, insert a meal slot without slotIndex
        helper.createDatabase(TEST_DB, OLD_VERSION).apply {
            execSQL("INSERT INTO meal_slots VALUES ('day1', 'LUNCH', 'recipe1')")
            close()
        }
        // Migrate and verify slotIndex defaulted to 0
        helper.runMigrationsAndValidate(TEST_DB, NEW_VERSION, true, MIGRATION).apply {
            val cursor = query("SELECT slotIndex FROM meal_slots WHERE dayPlanId = 'day1'")
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
            close()
        }
    }
}
```

## Seeding the Emulator UI Manually

1. Start emulators: `firebase emulators:start --only functions,firestore`
2. Open `http://localhost:4000` in a browser
3. Navigate to Firestore → Start collection → `ingredients`
4. Add a few ingredient documents matching the schema in Brief 1
5. Add recipe documents referencing those ingredient IDs
6. Add `users/default_user/settings` with a valid settings document
7. Run the app pointing at the emulator and tap Generate Plan

---

# Quality Test Dataset

A realistic 30-recipe dataset for evaluating optimizer quality. Run `tests/seed_emulator.py` to load it into the Firestore emulator, then call the optimizer and run `tests/analyse_plan.py` on the result.

## Ingredients

```python
INGREDIENTS = [
  # id, name, category, kcal/100g, protein/100g, fat/100g, carbs/100g
  ("ing_chicken_breast",  "Chicken Breast",        "MEAT",      110, 23.0,  1.5,  0.0),
  ("ing_salmon",          "Salmon Fillet",          "FISH",      208, 20.0, 13.0,  0.0),
  ("ing_tuna_canned",     "Canned Tuna",            "FISH",      116, 26.0,  1.0,  0.0),
  ("ing_ground_beef",     "Ground Beef 15%",        "MEAT",      215, 17.0, 16.0,  0.0),
  ("ing_cod",             "Cod Fillet",             "FISH",       82, 18.0,  0.7,  0.0),
  ("ing_eggs",            "Eggs",                   "DAIRY_EGGS", 155, 13.0, 11.0,  1.0),
  ("ing_greek_yogurt",    "Greek Yogurt 0%",        "DAIRY_EGGS",  59, 10.0,  0.4,  3.6),
  ("ing_cottage_cheese",  "Cottage Cheese",         "CHEESE",     98, 11.0,  4.3,  3.4),
  ("ing_rice",            "White Rice (dry)",       "GRAINS",    360,  6.5,  0.5, 79.0),
  ("ing_pasta",           "Pasta (dry)",            "GRAINS",    350, 12.0,  1.5, 71.0),
  ("ing_oats",            "Rolled Oats",            "GRAINS",    389, 17.0,  7.0, 66.0),
  ("ing_sweet_potato",    "Sweet Potato",           "FRUIT_VEG",  86,  1.6,  0.1, 20.0),
  ("ing_broccoli",        "Broccoli",               "FRUIT_VEG",  34,  2.8,  0.4,  6.6),
  ("ing_spinach",         "Spinach",                "FRUIT_VEG",  23,  2.9,  0.4,  3.6),
  ("ing_tomato",          "Tomato",                 "FRUIT_VEG",  18,  0.9,  0.2,  3.9),
  ("ing_onion",           "Onion",                  "FRUIT_VEG",  40,  1.1,  0.1,  9.3),
  ("ing_garlic",          "Garlic",                 "SPICES",    149,  6.4,  0.5, 33.0),
  ("ing_olive_oil",       "Olive Oil",              "OILS_SAUCES",884,  0.0,100.0,  0.0),
  ("ing_coconut_milk",    "Coconut Milk",           "CANNED",    197,  2.0, 21.0,  2.8),
  ("ing_chickpeas",       "Chickpeas (canned)",     "CANNED",    164,  8.9,  2.6, 27.0),
  ("ing_lentils",         "Red Lentils (dry)",      "DRY_GOODS", 353, 26.0,  1.1, 60.0),
  ("ing_kidney_beans",    "Kidney Beans (canned)",  "CANNED",    127,  8.7,  0.5, 22.0),
  ("ing_bread_whole",     "Wholegrain Bread",       "BREAD_BAKERY",247, 9.0,  3.5, 41.0),
  ("ing_banana",          "Banana",                 "FRUIT_VEG",  89,  1.1,  0.3, 23.0),
  ("ing_blueberries",     "Blueberries",            "FRUIT_VEG",  57,  0.7,  0.3, 14.0),
  ("ing_almonds",         "Almonds",                "NUTS",      579, 21.0, 50.0, 22.0),
  ("ing_protein_powder",  "Whey Protein Powder",    "SUPPLEMENT",354, 72.0,  4.0, 12.0),
  ("ing_curry_paste",     "Red Curry Paste",        "OILS_SAUCES",100, 2.0,  4.0, 14.0),
  ("ing_soy_sauce",       "Soy Sauce",              "OILS_SAUCES", 53, 8.1,  0.1,  4.9),
  ("ing_feta",            "Feta Cheese",            "CHEESE",    264, 14.0, 21.0,  4.1),
]
```

## Recipes

```python
RECIPES = [
  # ── LUNCH / DINNER ──────────────────────────────────────────────────────

  {
    "id": "rec_chicken_rice",
    "name": "Chicken & Rice",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_chicken_breast", "grams": 180},
      {"ingredientId": "ing_rice",           "grams": 80},
      {"ingredientId": "ing_broccoli",       "grams": 150},
      {"ingredientId": "ing_olive_oil",      "grams": 10},
    ]
    # kcal≈560, protein≈55g, fat≈15g, carbs≈65g
  },
  {
    "id": "rec_salmon_sweet_potato",
    "name": "Baked Salmon & Sweet Potato",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_salmon",       "grams": 180},
      {"ingredientId": "ing_sweet_potato", "grams": 200},
      {"ingredientId": "ing_spinach",      "grams": 100},
      {"ingredientId": "ing_olive_oil",    "grams": 10},
    ]
    # kcal≈620, protein≈44g, fat≈25g, carbs≈48g
  },
  {
    "id": "rec_beef_pasta",
    "name": "Beef Bolognese Pasta",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_ground_beef", "grams": 150},
      {"ingredientId": "ing_pasta",       "grams": 80},
      {"ingredientId": "ing_tomato",      "grams": 200},
      {"ingredientId": "ing_onion",       "grams": 80},
      {"ingredientId": "ing_olive_oil",   "grams": 10},
    ]
    # kcal≈640, protein≈38g, fat≈22g, carbs≈68g
  },
  {
    "id": "rec_tuna_rice",
    "name": "Tuna & Rice Bowl",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_tuna_canned", "grams": 160},
      {"ingredientId": "ing_rice",        "grams": 80},
      {"ingredientId": "ing_spinach",     "grams": 100},
      {"ingredientId": "ing_soy_sauce",   "grams": 15},
    ]
    # kcal≈440, protein≈52g, fat≈3g, carbs≈66g
  },
  {
    "id": "rec_chicken_curry",
    "name": "Chicken Coconut Curry",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_chicken_breast", "grams": 200},
      {"ingredientId": "ing_coconut_milk",   "grams": 100},
      {"ingredientId": "ing_curry_paste",    "grams": 30},
      {"ingredientId": "ing_rice",           "grams": 80},
      {"ingredientId": "ing_onion",          "grams": 80},
    ]
    # kcal≈640, protein≈53g, fat≈25g, carbs≈68g
  },
  {
    "id": "rec_cod_lentils",
    "name": "Baked Cod & Lentils",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_cod",     "grams": 200},
      {"ingredientId": "ing_lentils", "grams": 80},
      {"ingredientId": "ing_tomato",  "grams": 150},
      {"ingredientId": "ing_spinach", "grams": 80},
      {"ingredientId": "ing_olive_oil","grams": 10},
    ]
    # kcal≈490, protein≈50g, fat≈10g, carbs≈55g
  },
  {
    "id": "rec_beef_sweet_potato",
    "name": "Ground Beef & Sweet Potato",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_ground_beef",  "grams": 150},
      {"ingredientId": "ing_sweet_potato", "grams": 200},
      {"ingredientId": "ing_broccoli",     "grams": 150},
      {"ingredientId": "ing_olive_oil",    "grams": 10},
    ]
    # kcal≈600, protein≈35g, fat≈22g, carbs≈55g
  },
  {
    "id": "rec_chickpea_curry",
    "name": "Chickpea Curry",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_chickpeas",   "grams": 240},
      {"ingredientId": "ing_coconut_milk","grams": 100},
      {"ingredientId": "ing_curry_paste", "grams": 30},
      {"ingredientId": "ing_rice",        "grams": 80},
      {"ingredientId": "ing_spinach",     "grams": 100},
    ]
    # kcal≈640, protein≈22g, fat≈26g, carbs≈85g
  },
  {
    "id": "rec_salmon_pasta",
    "name": "Salmon & Pasta",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_salmon",    "grams": 160},
      {"ingredientId": "ing_pasta",     "grams": 80},
      {"ingredientId": "ing_spinach",   "grams": 100},
      {"ingredientId": "ing_olive_oil", "grams": 10},
    ]
    # kcal≈570, protein≈40g, fat≈22g, carbs≈57g
  },
  {
    "id": "rec_lentil_soup",
    "name": "Red Lentil Soup",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_lentils",   "grams": 100},
      {"ingredientId": "ing_tomato",    "grams": 200},
      {"ingredientId": "ing_onion",     "grams": 80},
      {"ingredientId": "ing_garlic",    "grams": 10},
      {"ingredientId": "ing_olive_oil", "grams": 10},
    ]
    # kcal≈430, protein≈26g, fat≈10g, carbs≈65g
  },
  {
    "id": "rec_tuna_pasta",
    "name": "Tuna Pasta",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_tuna_canned","grams": 160},
      {"ingredientId": "ing_pasta",      "grams": 80},
      {"ingredientId": "ing_tomato",     "grams": 150},
      {"ingredientId": "ing_olive_oil",  "grams": 10},
    ]
    # kcal≈490, protein≈48g, fat≈10g, carbs≈60g
  },
  {
    "id": "rec_chicken_feta_salad",
    "name": "Chicken & Feta Salad",
    "type": "MEAL",
    "mealCategories": ["LUNCH"],
    "ingredients": [
      {"ingredientId": "ing_chicken_breast","grams": 180},
      {"ingredientId": "ing_feta",          "grams": 60},
      {"ingredientId": "ing_spinach",       "grams": 150},
      {"ingredientId": "ing_tomato",        "grams": 150},
      {"ingredientId": "ing_olive_oil",     "grams": 10},
    ]
    # kcal≈470, protein≈50g, fat≈24g, carbs≈10g
  },
  {
    "id": "rec_kidney_bean_chili",
    "name": "Kidney Bean Chili",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_kidney_beans","grams": 240},
      {"ingredientId": "ing_ground_beef", "grams": 100},
      {"ingredientId": "ing_tomato",      "grams": 200},
      {"ingredientId": "ing_onion",       "grams": 80},
      {"ingredientId": "ing_olive_oil",   "grams": 10},
    ]
    # kcal≈530, protein≈36g, fat≈18g, carbs≈50g
  },
  {
    "id": "rec_egg_fried_rice",
    "name": "Egg Fried Rice",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_eggs",      "grams": 150},
      {"ingredientId": "ing_rice",      "grams": 100},
      {"ingredientId": "ing_broccoli",  "grams": 150},
      {"ingredientId": "ing_soy_sauce", "grams": 15},
      {"ingredientId": "ing_olive_oil", "grams": 10},
    ]
    # kcal≈520, protein≈30g, fat≈18g, carbs≈65g
  },
  {
    "id": "rec_cod_sweet_potato",
    "name": "Cod & Sweet Potato Mash",
    "type": "MEAL",
    "mealCategories": ["DINNER"],
    "ingredients": [
      {"ingredientId": "ing_cod",          "grams": 200},
      {"ingredientId": "ing_sweet_potato", "grams": 250},
      {"ingredientId": "ing_broccoli",     "grams": 150},
      {"ingredientId": "ing_olive_oil",    "grams": 10},
    ]
    # kcal≈490, protein≈40g, fat≈10g, carbs≈60g
  },

  # ── BREAKFAST ───────────────────────────────────────────────────────────

  {
    "id": "rec_oat_porridge",
    "name": "Oat Porridge with Banana",
    "type": "MEAL",
    "mealCategories": ["BREAKFAST"],
    "ingredients": [
      {"ingredientId": "ing_oats",   "grams": 80},
      {"ingredientId": "ing_banana", "grams": 120},
      {"ingredientId": "ing_eggs",   "grams": 50},
    ]
    # kcal≈440, protein≈18g, fat≈9g, carbs≈72g
  },
  {
    "id": "rec_greek_yogurt_berries",
    "name": "Greek Yogurt & Blueberries",
    "type": "MEAL",
    "mealCategories": ["BREAKFAST", "SNACK"],
    "ingredients": [
      {"ingredientId": "ing_greek_yogurt", "grams": 200},
      {"ingredientId": "ing_blueberries",  "grams": 100},
      {"ingredientId": "ing_oats",         "grams": 30},
    ]
    # kcal≈270, protein≈23g, fat≈2g, carbs≈38g
  },
  {
    "id": "rec_scrambled_eggs_toast",
    "name": "Scrambled Eggs on Toast",
    "type": "MEAL",
    "mealCategories": ["BREAKFAST"],
    "ingredients": [
      {"ingredientId": "ing_eggs",       "grams": 200},
      {"ingredientId": "ing_bread_whole","grams": 80},
      {"ingredientId": "ing_olive_oil",  "grams": 10},
      {"ingredientId": "ing_spinach",    "grams": 50},
    ]
    # kcal≈490, protein≈28g, fat≈22g, carbs≈44g
  },
  {
    "id": "rec_cottage_cheese_toast",
    "name": "Cottage Cheese on Toast",
    "type": "MEAL",
    "mealCategories": ["BREAKFAST", "SNACK"],
    "ingredients": [
      {"ingredientId": "ing_cottage_cheese","grams": 200},
      {"ingredientId": "ing_bread_whole",   "grams": 80},
      {"ingredientId": "ing_blueberries",   "grams": 80},
    ]
    # kcal≈370, protein≈28g, fat≈7g, carbs≈50g
  },
  {
    "id": "rec_oat_banana_eggs",
    "name": "Oat & Egg Pancakes",
    "type": "MEAL",
    "mealCategories": ["BREAKFAST"],
    "ingredients": [
      {"ingredientId": "ing_oats",   "grams": 80},
      {"ingredientId": "ing_eggs",   "grams": 150},
      {"ingredientId": "ing_banana", "grams": 100},
    ]
    # kcal≈470, protein≈25g, fat≈12g, carbs≈65g
  },

  # ── SNACKS ──────────────────────────────────────────────────────────────

  {
    "id": "rec_almonds_yogurt",
    "name": "Almonds & Yogurt",
    "type": "MEAL",
    "mealCategories": ["SNACK"],
    "ingredients": [
      {"ingredientId": "ing_almonds",      "grams": 30},
      {"ingredientId": "ing_greek_yogurt", "grams": 150},
    ]
    # kcal≈260, protein≈18g, fat≈18g, carbs≈10g
  },
  {
    "id": "rec_banana_almonds",
    "name": "Banana & Almonds",
    "type": "MEAL",
    "mealCategories": ["SNACK"],
    "ingredients": [
      {"ingredientId": "ing_banana",  "grams": 120},
      {"ingredientId": "ing_almonds", "grams": 25},
    ]
    # kcal≈225, protein≈6g, fat≈14g, carbs≈24g
  },
  {
    "id": "rec_cottage_cheese_berries",
    "name": "Cottage Cheese & Berries",
    "type": "MEAL",
    "mealCategories": ["SNACK"],
    "ingredients": [
      {"ingredientId": "ing_cottage_cheese","grams": 150},
      {"ingredientId": "ing_blueberries",   "grams": 100},
    ]
    # kcal≈205, protein≈18g, fat≈6g, carbs≈21g
  },
  {
    "id": "rec_eggs_spinach",
    "name": "Boiled Eggs & Spinach",
    "type": "MEAL",
    "mealCategories": ["SNACK"],
    "ingredients": [
      {"ingredientId": "ing_eggs",    "grams": 150},
      {"ingredientId": "ing_spinach", "grams": 80},
    ]
    # kcal≈255, protein≈22g, fat≈17g, carbs≈4g
  },
  {
    "id": "rec_oat_blueberry",
    "name": "Oat & Blueberry Snack",
    "type": "MEAL",
    "mealCategories": ["SNACK", "BREAKFAST"],
    "ingredients": [
      {"ingredientId": "ing_oats",        "grams": 50},
      {"ingredientId": "ing_blueberries", "grams": 80},
      {"ingredientId": "ing_greek_yogurt","grams": 100},
    ]
    # kcal≈270, protein≈13g, fat≈5g, carbs≈42g
  },

  # ── COMPONENT (sub-recipe example) ──────────────────────────────────────

  {
    "id": "comp_curry_sauce",
    "name": "Red Curry Sauce",
    "type": "COMPONENT",
    "componentCategory": "SAUCE",
    "mealCategories": [],
    "ingredients": [
      {"ingredientId": "ing_coconut_milk", "grams": 200},
      {"ingredientId": "ing_curry_paste",  "grams": 40},
      {"ingredientId": "ing_garlic",       "grams": 10},
      {"ingredientId": "ing_onion",        "grams": 80},
    ]
    # Total: kcal≈450, protein≈6g, fat≈44g, carbs≈22g
  },
  {
    "id": "rec_chicken_curry_component",
    "name": "Chicken Curry (with sauce component)",
    "type": "MEAL",
    "mealCategories": ["LUNCH", "DINNER"],
    "ingredients": [
      {"ingredientId": "ing_chicken_breast", "grams": 200},
      {"subRecipeId":  "comp_curry_sauce",   "portions": 0.5},
      {"ingredientId": "ing_rice",           "grams": 80},
    ]
    # Total: kcal≈730, protein≈55g, fat≈26g, carbs≈80g
  },
]
```

## Seed Script

```python
# tests/seed_emulator.py
import firebase_admin
from firebase_admin import credentials, firestore
import os

os.environ["FIRESTORE_EMULATOR_HOST"] = "localhost:8080"
firebase_admin.initialize_app(credentials.ApplicationDefault(), {"projectId": "your-project-id"})
db = firestore.client()

USER_ID = "test_user"

# Write ingredients
for ing in INGREDIENTS:
    fields = {
        "id": ing[0], "name": ing[1], "category": ing[2],
        "kcalPer100g": ing[3], "proteinPer100g": ing[4],
        "fatPer100g": ing[5], "carbsPer100g": ing[6],
        "source": "LABEL", "steps": []
    }
    db.collection("ingredients").document(ing[0]).set(fields)

# Write recipes
for recipe in RECIPES:
    db.collection("recipes").document(recipe["id"]).set({
        k: v for k, v in recipe.items() if k != "id"
    } | {"id": recipe["id"], "notes": "", "steps": [], "componentCategory": recipe.get("componentCategory")})

# Write settings
db.collection("users").document(USER_ID).collection("settings_doc").document("main").set(SETTINGS)

# Write a fake history plan (2 weeks ago) to test recency
import datetime
two_weeks_ago = datetime.date.today() - datetime.timedelta(weeks=2)
db.collection("users").document(USER_ID).collection("mealPlans").document("history_plan_1").set({
    "id": "history_plan_1",
    "name": "Previous Week",
    "startDate": two_weeks_ago.isoformat(),
    "endDate": (two_weeks_ago + datetime.timedelta(days=6)).isoformat(),
    "days": [
        {
            "id": "hist_day_1", "date": two_weeks_ago.isoformat(),
            "meals": [
                {"type": "LUNCH",  "recipeId": "rec_chicken_rice"},
                {"type": "DINNER", "recipeId": "rec_salmon_sweet_potato"},
            ],
            "proteinPowderGrams": 25, "kcalTarget": 1800, "proteinTarget": 150
        }
    ]
})

print("Seeded emulator successfully")
```

## Quality Analysis Script

Run this after calling the optimizer to evaluate plan quality:

```python
# tests/analyse_plan.py
"""
Usage:
    python tests/analyse_plan.py --userId test_user --planId <planId>

Fetches the generated plan from the Firestore emulator and prints a
quality report covering macro accuracy, recipe variety, recency, and
batch consistency.
"""
import argparse, os
import firebase_admin
from firebase_admin import credentials, firestore
from collections import Counter

os.environ["FIRESTORE_EMULATOR_HOST"] = "localhost:8080"
firebase_admin.initialize_app(credentials.ApplicationDefault(), {"projectId": "your-project-id"})
db = firestore.client()

def analyse(user_id: str, plan_id: str):
    plan   = db.collection("users").document(user_id).collection("mealPlans").document(plan_id).get().to_dict()
    days   = plan["days"]
    config = db.collection("users").document(user_id).collection("settings_doc").document("main").get().to_dict()

    # Pre-load recipe nutrition
    recipes  = {r.id: r.to_dict() for r in db.collection("recipes").stream()}
    ingreds  = {i.id: i.to_dict() for i in db.collection("ingredients").stream()}

    def recipe_nutrition(recipe_id):
        r = recipes.get(recipe_id, {})
        kcal = protein = fat = carbs = 0
        for ing in r.get("ingredients", []):
            if "ingredientId" in ing and ing["ingredientId"]:
                i = ingreds.get(ing["ingredientId"], {})
                g = ing.get("grams", 0) / 100
                kcal    += g * i.get("kcalPer100g", 0)
                protein += g * i.get("proteinPer100g", 0)
                fat     += g * i.get("fatPer100g", 0)
                carbs   += g * i.get("carbsPer100g", 0)
        return kcal, protein, fat, carbs

    # Aggregate weekly totals
    total_kcal = total_protein = total_fat = total_carbs = total_powder_kcal = total_powder_protein = 0
    all_recipe_ids = []
    lunch_ids = []
    dinner_ids = []

    for day in days:
        powder_g = day.get("proteinPowderGrams", 0)
        powder_kcal_per_g    = 354 / 100
        powder_protein_per_g = 72 / 100
        total_powder_kcal    += powder_g * powder_kcal_per_g
        total_powder_protein += powder_g * powder_protein_per_g

        for meal in day.get("meals", []):
            rid = meal["recipeId"]
            all_recipe_ids.append(rid)
            if meal["type"] == "LUNCH":  lunch_ids.append(rid)
            if meal["type"] == "DINNER": dinner_ids.append(rid)
            k, p, f, c = recipe_nutrition(rid)
            total_kcal    += k
            total_protein += p
            total_fat     += f
            total_carbs   += c

    total_kcal    += total_powder_kcal
    total_protein += total_powder_protein

    n_days        = len(days)
    kcal_target   = config["goals"]["kcalTarget"] * n_days
    protein_target= config["goals"]["proteinTarget"] * n_days

    print("\n" + "="*55)
    print("  OPTIMIZER QUALITY REPORT")
    print("="*55)

    print(f"\n📅 Days planned: {n_days}")
    print(f"🍽  Total meals:  {len(all_recipe_ids)}")

    print("\n── MACRO ACCURACY ─────────────────────────────────────")
    kcal_err    = (total_kcal    - kcal_target)    / kcal_target    * 100
    protein_err = (total_protein - protein_target) / protein_target * 100
    print(f"  Weekly kcal:    {total_kcal:,.0f} / {kcal_target:,.0f}  ({kcal_err:+.1f}%)")
    print(f"  Weekly protein: {total_protein:,.0f}g / {protein_target:,.0f}g  ({protein_err:+.1f}%)")
    print(f"  Powder used:    {sum(d.get('proteinPowderGrams',0) for d in days):.0f}g total")

    print("\n── RECIPE VARIETY ─────────────────────────────────────")
    recipe_counts = Counter(all_recipe_ids)
    unique        = len(recipe_counts)
    most_common   = recipe_counts.most_common(3)
    print(f"  Unique recipes: {unique}")
    print(f"  Most repeated:  {[(recipes[r]['name'], c) for r, c in most_common]}")

    print("\n── LUNCH VARIETY ──────────────────────────────────────")
    for rid, count in Counter(lunch_ids).most_common():
        print(f"  {recipes[rid]['name']:<35} ×{count}")

    print("\n── DINNER VARIETY ─────────────────────────────────────")
    for rid, count in Counter(dinner_ids).most_common():
        print(f"  {recipes[rid]['name']:<35} ×{count}")

    print("\n── BATCH CONSISTENCY ──────────────────────────────────")
    batch_groups = config["schedule"].get("batchGroups", [])
    for g in batch_groups:
        meal = g["meal"]
        iso_days = set(g["days"])
        group_days = [d for d in days if _iso_day(d["date"]) in iso_days]
        group_ids  = set()
        for d in group_days:
            for m in d["meals"]:
                if m["type"] == meal:
                    group_ids.add(m["recipeId"])
        status = "✓ consistent" if len(group_ids) == 1 else f"✗ INCONSISTENT ({group_ids})"
        print(f"  Batch {g['batchNumber']} ({meal}, {len(group_days)} days): {status}")

    print("\n── PROTEIN POWDER ─────────────────────────────────────")
    for day in days:
        g = day.get("proteinPowderGrams", 0)
        if g > 0:
            print(f"  {day['date']}: {g:.0f}g powder")

    print("\n" + "="*55 + "\n")

def _iso_day(date_str):
    import datetime
    return datetime.date.fromisoformat(date_str).isoweekday()

if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--userId", default="test_user")
    p.add_argument("--planId", required=True)
    args = p.parse_args()
    analyse(args.userId, args.planId)
```

### Example output

```
=======================================================
  OPTIMIZER QUALITY REPORT
=======================================================

📅 Days planned: 7
🍽  Total meals:  16

── MACRO ACCURACY ─────────────────────────────────────
  Weekly kcal:    12,580 / 12,600  (-0.2%)
  Weekly protein: 1,043g / 1,050g  (-0.7%)
  Powder used:    85g total

── RECIPE VARIETY ─────────────────────────────────────
  Unique recipes: 9
  Most repeated:  [('Chicken & Rice', 5), ('Tuna & Rice Bowl', 2), ('Salmon & Pasta', 2)]

── LUNCH VARIETY ──────────────────────────────────────
  Chicken & Rice                      ×5   ← batch Mon-Fri
  Greek Yogurt & Blueberries          ×1   ← batch Sat
  Tuna & Rice Bowl                    ×1   ← batch Sun

── DINNER VARIETY ─────────────────────────────────────
  Salmon & Pasta                      ×2
  Baked Cod & Lentils                 ×2
  Kidney Bean Chili                   ×1
  Chicken Coconut Curry               ×1
  Ground Beef & Sweet Potato          ×1

── BATCH CONSISTENCY ──────────────────────────────────
  Batch 1 (LUNCH, 5 days): ✓ consistent
  Batch 2 (LUNCH, 1 days): ✓ consistent

── PROTEIN POWDER ─────────────────────────────────────
  2025-05-19: 20g powder
  2025-05-21: 15g powder
  2025-05-24: 25g powder
  2025-05-25: 25g powder

=======================================================
```

The analysis script can also be run by a code assistant to evaluate whether optimizer changes improved or degraded plan quality — compare the macro accuracy percentages, unique recipe count, and max repeat count before and after any change.
