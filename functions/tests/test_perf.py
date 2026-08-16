"""
Performance and memory benchmark tests for the CP-SAT optimizer.

Each test prints its measured metrics (time and/or peak memory) so results
are always visible with -s.  Time limits are set to catch catastrophic
regressions, not to enforce micro-benchmark targets.

Run with:
    cd functions
    pytest tests/test_perf.py -v -s

Performance profile (reference numbers, dev machine, 20 MEAL / 4 SNACK recipes):

  FAST (OPTIMAL, < 3 s)
    no snacks + no powder + no batch   ~100 ms
    no snacks + no powder + batch      ~2 000 ms
    single day, any config             ~20 ms

  HITS SOLVER TIMEOUT (FEASIBLE at ~30 s)
    protein powder    — powder_vars[di] are IntVar (0..1000), introducing
                        mixed-integer structure that CP-SAT struggles with
                        in ~30 s on the current recipe pool.

Tests in TestSolveHitsTimeout document this known behaviour and only assert
that the solver returns within max_time_in_seconds + a small overhead budget
and that the returned plan is still structurally valid.
"""

from __future__ import annotations

import datetime
import pathlib
import sys
import time
import tracemalloc
from dataclasses import dataclass

import pytest

sys.path.insert(0, str(pathlib.Path(__file__).parent.parent))

from main import (
    BatchGroup,
    DayMealConfig,
    Goals,
    ProteinPowder,
    Recipe,
    Settings,
    VarietyConfig,
    VarietyPerCategory,
    build_history_index,
    compute_recipe_nutrition,
    filter_recipes,
    solve,
    _parse_recipe,
    _parse_settings,
)
from tests.test_optimizer import INGREDIENTS_RAW, RECIPES_RAW

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

START_DATE = datetime.date(2025, 5, 19)
SOLVER_MAX_S = 30.0  # must match solve()'s solver.parameters.max_time_in_seconds


@dataclass
class PerfResult:
    label: str
    elapsed_ms: float
    peak_mb: float

    def __str__(self) -> str:
        return (
            f"{self.label}: elapsed={self.elapsed_ms:.0f}ms  peak_mem={self.peak_mb:.1f}MB"
        )


def _timed_call(fn, *args, **kwargs) -> tuple[object, PerfResult]:
    """Run fn, measuring wall time and peak Python-heap allocations."""
    tracemalloc.start()
    t0 = time.perf_counter()
    result = fn(*args, **kwargs)
    elapsed = time.perf_counter() - t0
    _, peak_bytes = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    perf = PerfResult(
        label=fn.__name__,
        elapsed_ms=elapsed * 1000,
        peak_mb=peak_bytes / 1024 / 1024,
    )
    return result, perf


def _make_all_recipes() -> dict[str, Recipe]:
    return {rid: _parse_recipe(rid, data) for rid, data in RECIPES_RAW.items()}


def _make_settings(
    *,
    snack_count: int = 0,
    batch: bool = False,
    variety_level: str = "BALANCED",
    protein_powder: bool = False,
    days: list[str] | None = None,
    max_dinner_per_week: int | None = 2,
    max_kcal_per_day: float | None = None,
) -> Settings:
    if days is None:
        days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]

    slots = {
        day: DayMealConfig(False, True, True, snack_count)
        for day in days
    }
    # Sunday typically has no lunch
    if "SUNDAY" in slots:
        slots["SUNDAY"] = DayMealConfig(False, False, True, snack_count)

    batch_groups = (
        [
            BatchGroup(meal="LUNCH", days=[1, 2, 3, 4, 5], batch_number=1),
            BatchGroup(meal="LUNCH", days=[6], batch_number=2),
        ]
        if batch
        else []
    )
    pp = (
        ProteinPowder(
            ingredient_id="ing_protein_powder",
            name="Whey",
            protein_per_100g=72.0,
            kcal_per_100g=354.0,
            auto_fill_gap=True,
        )
        if protein_powder
        else None
    )
    per_cat: dict[str, VarietyPerCategory] = {
        "LUNCH":  VarietyPerCategory(None, None),
        "DINNER": VarietyPerCategory(max_dinner_per_week, None),
        "SNACK":  VarietyPerCategory(None, None),
    }
    return Settings(
        meal_slots=slots,
        batch_groups=batch_groups,
        goals=Goals(
            kcal_target=1800,
            protein_target=150,
            fat_target=50,
            carbs_target=180,
            max_kcal=max_kcal_per_day,
        ),
        variety=VarietyConfig(
            level=variety_level,
            lunch_dinner_shared_recency=True,
            breakfast_snack_shared_recency=False,
            protein_source_variety=True,
            per_category=per_cat,
        ),
        protein_powder=pp,
        diet_excluded_ingredient_ids=[],
        excluded_recipe_ids=set(),
        rules=[],
    )


# ---------------------------------------------------------------------------
# Shared fixtures
# ---------------------------------------------------------------------------


@pytest.fixture(scope="module")
def all_recipes():
    return _make_all_recipes()


@pytest.fixture(scope="module")
def all_nutrition(all_recipes):
    return compute_recipe_nutrition(all_recipes, INGREDIENTS_RAW)


# ---------------------------------------------------------------------------
# Phase: nutrition computation
# ---------------------------------------------------------------------------


class TestNutritionComputationPerf:
    def test_time_and_memory(self, all_recipes):
        _, perf = _timed_call(compute_recipe_nutrition, all_recipes, INGREDIENTS_RAW)
        print(f"\n  {perf}")
        assert perf.elapsed_ms < 500, f"nutrition compute too slow: {perf.elapsed_ms:.0f}ms"
        assert perf.peak_mb < 20, f"nutrition compute uses too much memory: {perf.peak_mb:.1f}MB"


# ---------------------------------------------------------------------------
# Phase: recipe filtering
# ---------------------------------------------------------------------------


class TestFilteringPerf:
    def test_time_and_memory(self, all_recipes, all_nutrition):
        settings = _make_settings()
        _, perf = _timed_call(filter_recipes, all_recipes, settings, {}, all_nutrition)
        print(f"\n  {perf}")
        assert perf.elapsed_ms < 200, f"filter too slow: {perf.elapsed_ms:.0f}ms"
        assert perf.peak_mb < 5, f"filter uses too much memory: {perf.peak_mb:.1f}MB"


# ---------------------------------------------------------------------------
# Solve — fast configurations (no unlimited snacks)
# ---------------------------------------------------------------------------


class TestSolveFastPerf:
    """Configurations that should solve well within 2 seconds."""

    def _run(self, settings, nutrition, recipes, label) -> PerfResult:
        eligible = filter_recipes(recipes, settings, {}, nutrition)
        _, perf = _timed_call(
            solve, eligible, settings, nutrition, {}, {}, START_DATE, INGREDIENTS_RAW
        )
        perf.label = label
        print(f"\n  {perf}  eligible={len(eligible)}")
        return perf

    def test_single_day_no_snack(self, all_recipes, all_nutrition):
        settings = _make_settings(days=["MONDAY"], snack_count=0)
        perf = self._run(settings, all_nutrition, all_recipes, "single_day_no_snack")
        assert perf.elapsed_ms < 2_000
        assert perf.peak_mb < 100

    def test_full_week_no_snack(self, all_recipes, all_nutrition):
        settings = _make_settings(snack_count=0)
        perf = self._run(settings, all_nutrition, all_recipes, "full_week_no_snack")
        assert perf.elapsed_ms < 2_000

    def test_full_week_with_batch(self, all_recipes, all_nutrition):
        settings = _make_settings(snack_count=0, batch=True)
        perf = self._run(settings, all_nutrition, all_recipes, "full_week_batch_no_snack")
        assert perf.elapsed_ms < 5_000

    def test_full_week_fixed_one_snack(self, all_recipes, all_nutrition):
        """Selection model: snack_count=1 now solves in ms, not 30 s."""
        settings = _make_settings(snack_count=1)
        perf = self._run(settings, all_nutrition, all_recipes, "full_week_fixed_1_snack")
        assert perf.elapsed_ms < 5_000


    def test_memory_peak_full_week_no_snack(self, all_recipes, all_nutrition):
        settings = _make_settings(snack_count=0)
        eligible = filter_recipes(all_recipes, settings, {}, all_nutrition)
        _, perf = _timed_call(
            solve, eligible, settings, all_nutrition, {}, {}, START_DATE
        )
        perf.label = "memory_full_week_no_snack"
        print(f"\n  {perf}")
        assert perf.peak_mb < 150, f"peak memory too high: {perf.peak_mb:.1f}MB"


# ---------------------------------------------------------------------------
# Solve — configurations that hit the solver timeout
# ---------------------------------------------------------------------------


class TestSolveHitsTimeout:
    """
    Configurations that are still too hard to prove OPTIMAL within 30 s on the
    small test recipe pool:

    - Protein powder: IntVar per day introduces mixed-integer structure.
    - Variable snacks (snack_count=-1): implicit per-day kcal bounds prune the
      search but the tiny pool (5 meal recipes) still exhausts 30 s proving
      optimality across 4 macro targets.  With a large production pool the same
      bounds make the solve finish in < 5 s; this test only checks structural
      validity to avoid a flaky wall-clock assertion.
    """

    TIMEOUT_BUDGET_MS = (SOLVER_MAX_S + 5) * 1000  # 35 000 ms

    def _run(self, settings, nutrition, recipes, label) -> tuple[dict, PerfResult]:
        eligible = filter_recipes(recipes, settings, {}, nutrition)
        plan, perf = _timed_call(
            solve, eligible, settings, nutrition, {}, {}, START_DATE, INGREDIENTS_RAW
        )
        perf.label = label
        print(f"\n  {perf}  eligible={len(eligible)}")
        return plan, perf

    def test_with_protein_powder(self, all_recipes, all_nutrition):
        """Powder introduces IntVar per day; mixed-integer makes proof intractable."""
        settings = _make_settings(snack_count=0, protein_powder=True)
        plan, perf = self._run(settings, all_nutrition, all_recipes, "powder_no_snack")
        assert perf.elapsed_ms < self.TIMEOUT_BUDGET_MS
        assert plan["days"]

    def test_full_week_unlimited_snacks(self, all_recipes, all_nutrition):
        """Structural smoke-test for variable snacks.  Production performance
        (typically < 5 s with a large recipe pool) cannot be validated here."""
        settings = _make_settings(snack_count=-1)
        plan, perf = self._run(settings, all_nutrition, all_recipes, "unlimited_snacks")
        assert perf.elapsed_ms < self.TIMEOUT_BUDGET_MS
        assert plan["days"]
        for day_data in plan["days"]:
            snacks = [m for m in day_data["meals"] if m["type"] == "SNACK"]
            assert len(snacks) <= 3, f"Snack cap violated on {day_data['dayOfWeek']}"
        assert plan["days"]


# ---------------------------------------------------------------------------
# Solve metrics verified via logs
# ---------------------------------------------------------------------------


class TestModelMetrics:
    """Verify the solver emits the expected log lines for observability."""

    def test_pool_sizes_logged(self, all_recipes, all_nutrition, caplog):
        import logging
        settings = _make_settings(snack_count=0)
        eligible = filter_recipes(all_recipes, settings, {}, all_nutrition)
        with caplog.at_level(logging.INFO, logger="main"):
            solve(eligible, settings, all_nutrition, {}, {}, START_DATE)
        lines = [r.message for r in caplog.records if "optimizer.solve pool_sizes=" in r.message]
        assert lines, "Expected pool_sizes log line not found"

    def test_model_built_logged(self, all_recipes, all_nutrition, caplog):
        import logging
        settings = _make_settings(snack_count=0)
        eligible = filter_recipes(all_recipes, settings, {}, all_nutrition)
        with caplog.at_level(logging.INFO, logger="main"):
            solve(eligible, settings, all_nutrition, {}, {}, START_DATE)
        lines = [r.message for r in caplog.records if "model_built" in r.message]
        assert lines, "Expected model_built log line not found"
        line = lines[0]
        assert "vars=" in line and "constraints=" in line
        # Extract and sanity-check variable count
        for part in line.split():
            if part.startswith("vars="):
                n_vars = int(part.split("=")[1])
                assert n_vars > 0, "model has no variables"

    def test_solve_done_logged_with_status(self, all_recipes, all_nutrition, caplog):
        import logging
        settings = _make_settings(snack_count=0)
        eligible = filter_recipes(all_recipes, settings, {}, all_nutrition)
        with caplog.at_level(logging.INFO, logger="main"):
            solve(eligible, settings, all_nutrition, {}, {}, START_DATE)
        lines = [r.message for r in caplog.records if "optimizer.solve done" in r.message]
        assert lines, "Expected 'optimizer.solve done' log line not found"
        line = lines[0]
        assert "OPTIMAL" in line or "FEASIBLE" in line
        assert "solve_ms=" in line
        assert "objective=" in line

    def test_build_time_reported_in_ms(self, all_recipes, all_nutrition, caplog):
        """build_ms= in the model_built log should be a non-negative integer."""
        import logging
        settings = _make_settings(snack_count=0)
        eligible = filter_recipes(all_recipes, settings, {}, all_nutrition)
        with caplog.at_level(logging.INFO, logger="main"):
            solve(eligible, settings, all_nutrition, {}, {}, START_DATE)
        lines = [r.message for r in caplog.records if "model_built" in r.message]
        assert lines
        line = lines[0]
        for part in line.split():
            if part.startswith("build_ms="):
                val = float(part.split("=")[1])
                assert val >= 0


# ---------------------------------------------------------------------------
# Multi-week solve
# ---------------------------------------------------------------------------


class TestMultiWeekPerf:
    """Solve three consecutive weeks (each feeds history to the next)
    without unlimited snacks so the test completes in a reasonable time."""

    def test_three_weeks_no_snack(self, all_recipes, all_nutrition):
        settings = _make_settings(snack_count=0, batch=True)
        week_starts = [
            datetime.date(2025, 5, 19),
            datetime.date(2025, 5, 26),
            datetime.date(2025, 6, 2),
        ]
        history_plans: list[dict] = []
        total_ms = 0.0

        for week_start in week_starts:
            eligible = filter_recipes(all_recipes, settings, {}, all_nutrition)
            history_index = build_history_index(history_plans, settings.variety)
            plan, perf = _timed_call(
                solve, eligible, settings, all_nutrition, {}, history_index, week_start
            )
            total_ms += perf.elapsed_ms
            history_plans.append({
                "id": plan["id"],
                "startDate": plan["startDate"],
                "days": [{"date": d["date"], "meals": d["meals"]} for d in plan["days"]],
            })
            print(f"\n  week {week_start}: {perf.elapsed_ms:.0f}ms")

        print(f"\n  3-week total: {total_ms:.0f}ms")
        assert total_ms < 10_000, f"3-week solve (no snacks) took {total_ms:.0f}ms"
