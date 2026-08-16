# FoodPlan Optimizer — Firebase Cloud Function

Python Cloud Function that solves a weekly meal-assignment problem using
[OR-Tools CP-SAT](https://developers.google.com/optimization) and writes the
resulting plan back to Firestore.

Triggered via the callable function `optimise_meal_plan` with payload:

```json
{ "userId": "...", "startDate": "2025-05-19" }
```

## Layout

```
functions/
├── main.py              # all entry points, data classes, solver
├── requirements.txt
└── tests/
    ├── test_optimizer.py    # pytest suite (unit + integration)
    ├── fixtures/
    │   └── minimal.json     # canonical settings fixture
    ├── seed_emulator.py     # populate the local Firestore emulator
    ├── analyse_plan.py      # dump human-readable plan summary
    └── quality_three_weeks.py
```

## Setup

```bash
cd functions
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt pytest
```

## Running locally (Firebase emulators)

From the repo root:

```bash
firebase emulators:start --only functions,firestore
# Functions UI: http://localhost:4000   Firestore: localhost:8080   Functions: localhost:5001
python3 functions/tests/seed_emulator.py    # optional: seed sample data
```

## Deployment

```bash
firebase deploy --only functions:optimise_meal_plan
```

## How the solver is wired

1. `_read_settings_doc()` — reads `users/{userId}/settings/main` (falls back to
   the legacy subcollection).
2. `_read_firestore_data()` — pulls ingredients, recipes, ratings, settings and
   recent meal plans (recency window depends on `variety.level`).
3. `_parse_settings()`, `_parse_recipe()`, `_parse_rating()` — JSON → dataclasses.
4. `compute_recipe_nutrition()` — resolves leaf and sub-recipe nutrition.
5. `filter_recipes()` — drops excluded / inapplicable / out-of-window recipes.
6. `build_history_index()` — most-recent-use map across past weeks.
7. `solve()` — builds the CP-SAT model, minimises a weighted objective of
   macro deviation + recency + powder usage + custom rules +
   `proteinSourceVariety`.

# Testing

Tests live in `functions/tests/test_optimizer.py` and use pure pytest — no
emulator required. The suite covers nutrition pre-computation, filtering,
solver output structure, macro accuracy, recency penalties, protein-source
variety, and infeasibility edge cases.

## Run the suite

```bash
cd functions
source .venv/bin/activate
pytest tests/ -q                          # all tests
pytest tests/test_optimizer.py::TestSolverOutput -v
pytest -k "recency" -v                    # by keyword
```

A full run takes ≈3 min because each `solve()` call invokes CP-SAT. Unit
tests on pure helpers (`compute_recency_penalty`, `_dominant_protein_source`)
run in milliseconds.

## Writing new tests

The recommended structure is to group tests by behaviour in a class, reuse
the shared `INGREDIENTS_RAW` / `RECIPES_RAW` constants at the top of the file,
and call the solver through `_call_optimizer(fixture, ratings, pass_ingredients)`:

```python
class TestMyFeature:
    def test_x_does_y(self):
        data = _load_fixture()                       # deep-copyable JSON
        data["settings"]["variety"]["level"] = "STRICT"
        plan = _call_optimizer(data, pass_ingredients=True)
        assert plan["days"][0]["meals"]              # whatever invariant
```

### Available fixtures and helpers

- `fixture_data` — parsed `tests/fixtures/minimal.json`
- `recipes`, `nutrition`, `solved_plan`, `goals` — module-scoped pytest
  fixtures so the solver only runs once across the file
- `_load_fixture()` — returns a fresh dict (use this when mutating)
- `_make_recipes()`, `_make_nutrition()`, `_make_settings()`, `_make_eligible()`
- `_call_optimizer(fixture, ratings=None, pass_ingredients=False)` —
  one-call wrapper; pass `pass_ingredients=True` to exercise
  `proteinSourceVariety`

### Testing a pure helper

Prefer direct calls over going through `solve()` when possible — they're
two orders of magnitude faster:

```python
def test_within_week_index_drives_penalty():
    variety = VarietyConfig("BALANCED", True, False, False, {})
    pen = compute_recency_penalty(
        "rec_chicken_rice", "DINNER", variety,
        history_index={},
        within_week_index={("LUNCH_DINNER", "rec_chicken_rice"): date(2025, 5, 19)},
        reference_date=date(2025, 5, 22),
    )
    assert abs(pen - (1.0 - 3 / 28)) < 1e-9
```

### Testing the solver end-to-end

Build a minimal `Settings` and pass it directly — see `TestEdgeCases` for the
pattern. Always set `meal_slots` to only the day(s) you need active; smaller
schedules cut solve time dramatically.

### Importing from `main.py`

Tests prepend `functions/` to `sys.path`, so just import from `main`:

```python
from main import solve, filter_recipes, compute_recency_penalty
```

## Other tooling

- `tests/seed_emulator.py` — seeds the running Firestore emulator with the
  fixture data; handy for manual end-to-end runs from the Android app.
- `tests/analyse_plan.py` — pretty-prints a saved plan's nutrition breakdown.
- `tests/quality_three_weeks.py` — generates three consecutive weeks and
  checks variety/recency behaviour across runs.
