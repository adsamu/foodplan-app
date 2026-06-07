package com.adasa.foodplan.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the v5 → v6 migration that introduces a `slotIndex` column on
 * `meal_slots` and widens the primary key to (dayPlanId, type, slotIndex),
 * allowing multiple snacks per day to coexist.
 */
@RunWith(AndroidJUnit4::class)
class Migration5To6Test {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() { context.deleteDatabase(TEST_DB) }

    @After
    fun tearDown() { context.deleteDatabase(TEST_DB) }

    @Test
    fun migration_preservesExistingRows_andAssignsSlotIndexZero() {
        openV5Helper().use { helper ->
            val db = helper.writableDatabase
            seedV5Data(db)

            FoodPlanDatabase.MIGRATION_5_6.migrate(db)

            db.query(
                "SELECT dayPlanId, type, slotIndex, recipeId FROM meal_slots ORDER BY type"
            ).use { c ->
                assertEquals(2, c.count)
                c.moveToFirst()
                assertEquals("d1", c.getString(0))
                assertEquals("DINNER", c.getString(1))
                assertEquals(0, c.getInt(2))
                assertEquals("rec_dinner", c.getString(3))
                c.moveToNext()
                assertEquals("LUNCH", c.getString(1))
                assertEquals(0, c.getInt(2))
                assertEquals("rec_lunch", c.getString(3))
            }
        }
    }

    @Test
    fun migration_allowsMultipleSnacksPerDay() {
        openV5Helper().use { helper ->
            val db = helper.writableDatabase
            seedV5Data(db)
            FoodPlanDatabase.MIGRATION_5_6.migrate(db)

            db.execSQL("INSERT INTO meal_slots VALUES('d1','SNACK',0,'snack_a')")
            db.execSQL("INSERT INTO meal_slots VALUES('d1','SNACK',1,'snack_b')")

            db.query("SELECT COUNT(*) FROM meal_slots WHERE type='SNACK'").use { c ->
                c.moveToFirst()
                assertEquals(2, c.getInt(0))
            }
        }
    }

    @Test
    fun migration_keepsCompositePkUniqueOnSlotIndex() {
        openV5Helper().use { helper ->
            val db = helper.writableDatabase
            seedV5Data(db)
            FoodPlanDatabase.MIGRATION_5_6.migrate(db)

            db.execSQL("INSERT INTO meal_slots VALUES('d1','SNACK',0,'snack_a')")
            try {
                db.execSQL("INSERT INTO meal_slots VALUES('d1','SNACK',0,'snack_dup')")
                fail("Expected SQLiteConstraintException for duplicate (dayPlanId,type,slotIndex)")
            } catch (expected: SQLiteConstraintException) {
                // expected
            }
        }
    }

    // ------------------------------------------------------------------

    private fun openV5Helper(): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(TEST_DB)
                .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                    override fun onCreate(db: SupportSQLiteDatabase) = createV5Schema(db)
                    override fun onUpgrade(db: SupportSQLiteDatabase, old: Int, new: Int) = Unit
                })
                .build()
        )

    private fun createV5Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE meal_plans (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                startDate INTEGER NOT NULL,
                endDate INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE day_plans (
                id TEXT NOT NULL PRIMARY KEY,
                mealPlanId TEXT NOT NULL,
                date INTEGER NOT NULL,
                proteinPowderGrams REAL NOT NULL,
                kcalTarget INTEGER NOT NULL,
                proteinTarget INTEGER NOT NULL,
                FOREIGN KEY(mealPlanId) REFERENCES meal_plans(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE meal_slots (
                dayPlanId TEXT NOT NULL,
                type TEXT NOT NULL,
                recipeId TEXT NOT NULL,
                PRIMARY KEY(dayPlanId, type),
                FOREIGN KEY(dayPlanId) REFERENCES day_plans(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    private fun seedV5Data(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO meal_plans VALUES('p1','wk1',0,6)")
        db.execSQL("INSERT INTO day_plans VALUES('d1','p1',0,0.0,2000,150)")
        db.execSQL("INSERT INTO meal_slots VALUES('d1','LUNCH','rec_lunch')")
        db.execSQL("INSERT INTO meal_slots VALUES('d1','DINNER','rec_dinner')")
    }

    private companion object {
        const val TEST_DB = "migration-5-to-6-test.db"
    }
}
