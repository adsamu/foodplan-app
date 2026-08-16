package com.adasa.foodplan.data.local

import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.MealPlanRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.adasa.foodplan.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val firestore: FirebaseFirestore
) {
    /**
     * Seeds the shared Firestore recipes/ingredients collections exactly once —
     * checked directly against Firestore (not the Room cache, which starts empty
     * on every fresh install regardless of whether the cloud data already exists).
     */
    suspend fun seedIfEmpty() {
        val existing = firestore.collection("ingredients").limit(1).get().await()
        if (!existing.isEmpty) return
        seedIngredients()
        seedRecipes()
        seedMealPlan()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ingredients — full Ingrediensdatabas from spreadsheet
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun seedIngredients() {
        val all = listOf(

            // Protein – kyckling
            i("ing_kyckling_kronfagel", "Kycklingbröstfilé (Kronfågel)", IngredientCategory.MEAT,
                90.0, 19.0, 2.0, 0.0, IngredientSource.LABEL),
            i("ing_kyckling_lindstroms", "Kyckling (Lindströms)", IngredientCategory.MEAT,
                104.0, 22.0, 2.0, 0.0, IngredientSource.LABEL),
            i("ing_kyckling_guldfagel", "Kycklingbröstfilé (Guldfågel)", IngredientCategory.MEAT,
                120.0, 23.0, 2.0, 0.0, IngredientSource.LABEL),

            // Protein – fisk
            i("ing_lax", "Lax (rå)", IngredientCategory.FISH,
                188.0, 20.0, 13.0, 0.0, IngredientSource.LIVSMEDELSVERKET),
            i("ing_tonfisk", "Tonfisk i vatten (ICA)", IngredientCategory.FISH,
                110.0, 24.0, 1.0, 0.0, IngredientSource.LABEL),

            // Protein – kött
            i("ing_nottfars_12", "Nöttfärs 12% (rå)", IngredientCategory.MEAT,
                184.0, 19.0, 12.0, 0.0, IngredientSource.LABEL),
            i("ing_nottfars_5", "Nöttfärs 5% (rå)", IngredientCategory.MEAT,
                107.0, 21.0, 3.0, 0.0, IngredientSource.CALCULATED),
            i("ing_biff_ryggbiff", "Biff ryggbiff nöt", IngredientCategory.MEAT,
                120.0, 22.0, 4.0, 0.0, IngredientSource.LIVSMEDELSVERKET),

            // Protein – ägg
            i("ing_agg", "Ägg", IngredientCategory.DAIRY_EGGS,
                143.0, 13.0, 10.0, 0.0, IngredientSource.LIVSMEDELSVERKET),

            // Mejeri
            i("ing_creme_fraiche", "Lätt crème fraîche (ICA)", IngredientCategory.DAIRY_EGGS,
                150.0, 3.0, 7.0, 4.0, IngredientSource.LABEL),
            i("ing_matlagningsgradde", "Matlagningsgrädde 15%", IngredientCategory.DAIRY_EGGS,
                160.0, 3.0, 15.0, 4.0, IngredientSource.LABEL),
            i("ing_graddfil", "Gräddfil", IngredientCategory.DAIRY_EGGS,
                138.0, 3.0, 12.0, 4.0, IngredientSource.LABEL),
            i("ing_mozzarella", "Mozzarella (Arla)", IngredientCategory.DAIRY_EGGS,
                303.0, 27.0, 23.0, 0.0, IngredientSource.LABEL),
            i("ing_arla_ost", "Arla Köket mager ost 12%", IngredientCategory.DAIRY_EGGS,
                243.0, 30.0, 13.0, 0.0, IngredientSource.LABEL),
            i("ing_mjolk_laktos", "Mjölk laktosfri 1.5%", IngredientCategory.DAIRY_EGGS,
                45.0, 4.0, 2.0, 5.0, IngredientSource.LABEL),
            i("ing_smor", "Smör (Arla)", IngredientCategory.DAIRY_EGGS,
                714.0, 1.0, 80.0, 0.0, IngredientSource.LABEL),
            i("ing_tzatsiki", "Tzatsiki (Fontana)", IngredientCategory.DAIRY_EGGS,
                131.0, 5.4, 10.1, 4.5, IngredientSource.LABEL),
            i("ing_smelteost", "Smelteost cheddar (Country Cow)", IngredientCategory.DAIRY_EGGS,
                308.0, 10.0, 25.0, 4.0, IngredientSource.LABEL),
            i("ing_grekghurt_larsa", "Grekisk Yoghurt 0% (Larsa)", IngredientCategory.DAIRY_EGGS,
                59.0, 10.0, 0.0, 4.0, IngredientSource.LABEL),
            i("ing_grekghurt_arla", "Grekisk Yoghurt 0.2% (Arla)", IngredientCategory.DAIRY_EGGS,
                60.0, 11.0, 0.0, 4.0, IngredientSource.LABEL),

            // Spannmål – torrt
            i("ing_jasminris", "Jasminris (okokt)", IngredientCategory.GRAINS,
                349.0, 7.0, 1.0, 78.0, IngredientSource.LABEL),
            i("ing_bovete", "Bovete Ekologiskt (Garant, torrt)", IngredientCategory.GRAINS,
                356.0, 8.0, 1.0, 74.0, IngredientSource.LABEL),
            i("ing_fullkornspasta", "Fullkornspasta (Garant, torr)", IngredientCategory.GRAINS,
                350.0, 12.0, 2.0, 67.0, IngredientSource.LABEL),
            i("ing_cavatappi", "Cavatappi pasta (De Cecco, torr)", IngredientCategory.GRAINS,
                358.0, 12.0, 2.0, 71.0, IngredientSource.LABEL),
            i("ing_basmatiris", "Basmatiris vit (torrt)", IngredientCategory.GRAINS,
                365.0, 7.0, 1.0, 80.0, IngredientSource.LABEL),
            i("ing_japansktris", "Japanskt ris (okokt)", IngredientCategory.GRAINS,
                355.0, 8.0, 1.0, 77.0, IngredientSource.LABEL),

            // Grönsaker
            i("ing_potatis", "Potatis", IngredientCategory.FRUIT_VEG,
                79.0, 2.0, 0.0, 17.0, IngredientSource.LIVSMEDELSVERKET),
            i("ing_gullok", "Gullök", IngredientCategory.FRUIT_VEG,
                32.0, 2.0, 0.0, 7.0, IngredientSource.LABEL),
            i("ing_krossade_tomater", "Krossade tomater", IngredientCategory.FRUIT_VEG,
                22.0, 1.0, 0.0, 4.0, IngredientSource.LABEL),
            i("ing_gurka", "Gurka", IngredientCategory.FRUIT_VEG,
                12.0, 1.0, 0.0, 2.0, IngredientSource.LABEL),
            i("ing_rodlok", "Rödlök", IngredientCategory.FRUIT_VEG,
                40.0, 1.0, 0.0, 9.0, IngredientSource.LABEL),
            i("ing_paprika", "Paprika", IngredientCategory.FRUIT_VEG,
                31.0, 1.0, 0.0, 6.0, IngredientSource.LABEL),
            i("ing_vitlok", "Vitlök", IngredientCategory.FRUIT_VEG,
                135.0, 6.0, 1.0, 28.0, IngredientSource.LABEL),
            i("ing_basilika", "Basilika (färsk)", IngredientCategory.FRUIT_VEG,
                23.0, 3.0, 1.0, 1.0, IngredientSource.LABEL),
            i("ing_morotter", "Morötter", IngredientCategory.FRUIT_VEG,
                38.0, 1.0, 0.0, 8.0, IngredientSource.LABEL),
            i("ing_salladslok", "Salladslök", IngredientCategory.FRUIT_VEG,
                30.0, 2.0, 0.0, 5.0, IngredientSource.LABEL),
            i("ing_dill", "Dill (färsk)", IngredientCategory.FRUIT_VEG,
                43.0, 3.0, 1.0, 4.0, IngredientSource.LABEL),

            // Frukt
            i("ing_banan", "Banan", IngredientCategory.FRUIT_VEG,
                83.0, 1.0, 0.0, 19.0, IngredientSource.LABEL),

            // Frö/nötter
            i("ing_sesamfro", "Sesamfrö (GoGreen)", IngredientCategory.NUTS,
                600.0, 20.0, 53.0, 12.0, IngredientSource.LABEL),

            // Fryst
            i("ing_frysta_artor", "Frysta ärtor", IngredientCategory.FROZEN,
                73.0, 5.0, 0.0, 12.0, IngredientSource.LABEL),

            // Bröd
            i("ing_tortilla", "Tortilla Large (ICA)", IngredientCategory.BREAD_BAKERY,
                302.0, 8.3, 4.9, 55.0, IngredientSource.LABEL),
            i("ing_burgarbrod", "Potato Burger Bun (Korvbröds Bagarn)", IngredientCategory.BREAD_BAKERY,
                280.0, 9.0, 5.0, 50.0, IngredientSource.LABEL),

            // Flingor/müsli
            i("ing_musli_gold", "Gold BERRIES musli", IngredientCategory.DRY_GOODS,
                422.0, 10.0, 16.0, 55.0, IngredientSource.LABEL),
            i("ing_musli_frebaco", "Frebaco Müsli (Persika Hallon)", IngredientCategory.DRY_GOODS,
                320.0, 12.0, 4.0, 60.0, IngredientSource.LABEL),

            // Buljong
            i("ing_kalvbuljong", "Kalvbuljong (Knorr)", IngredientCategory.OILS_SAUCES,
                170.0, 4.0, 0.0, 38.0, IngredientSource.LABEL),
            i("ing_gronsaksbuljong", "Grönsaksbuljong", IngredientCategory.OILS_SAUCES,
                10.0, 1.0, 0.0, 2.0, IngredientSource.LABEL),

            // Sås
            i("ing_kinesisk_soja", "Kinesisk Soja", IngredientCategory.OILS_SAUCES,
                50.0, 5.0, 0.0, 8.0, IngredientSource.LABEL),
            i("ing_ostronsas", "Ostronsås", IngredientCategory.OILS_SAUCES,
                51.0, 2.0, 0.0, 13.0, IngredientSource.LABEL),
            i("ing_dijonsenap", "Dijonsenap", IngredientCategory.OILS_SAUCES,
                70.0, 4.0, 4.0, 5.0, IngredientSource.LABEL),

            // Fett
            i("ing_rapsolja", "Rapsolja", IngredientCategory.OILS_SAUCES,
                900.0, 0.0, 100.0, 0.0, IngredientSource.LABEL),
            i("ing_sesamolja", "Sesamolja", IngredientCategory.OILS_SAUCES,
                884.0, 0.0, 100.0, 0.0, IngredientSource.LABEL),

            // Konserv
            i("ing_tomatpure", "Tomatpuré", IngredientCategory.CANNED,
                82.0, 4.0, 0.0, 16.0, IngredientSource.LABEL),
            i("ing_gochujang", "Gochujang", IngredientCategory.CANNED,
                200.0, 5.0, 5.0, 35.0, IngredientSource.LABEL),
        )
        ingredientRepository.saveIngredients(all)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Recipes — all 10 from spreadsheet
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun seedRecipes() {

        // 1. Bovete kyckling — Lunch/Dinner storkok
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_bovete_kyckling",
            name = "Buckwheat chicken",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.LUNCH, MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_bovete",           85.0),
                ri("ing_kyckling_kronfagel", 180.0),
                ri("ing_tzatsiki",          45.0),
            ),
            steps = listOf(
                "Cook buckwheat according to package instructions. Set aside.",
                "Season chicken breast with salt and pepper. Grill or pan-fry 6–8 minutes per side until cooked through.",
                "Slice chicken and serve over buckwheat. Top with tzatsiki.",
            ),
            notes = "Tweaked: chicken 150g → 180g. Target: ~580 kcal / ~52g P"
        ))

        // 2. Curry kyckling — Lunch/Dinner storkok
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_curry_kyckling",
            name = "Curry chicken",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.LUNCH, MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_jasminris",          60.0),
                ri("ing_matlagningsgradde",  50.0),
                ri("ing_creme_fraiche",      50.0),
                ri("ing_kyckling_kronfagel", 200.0),
                ri("ing_kinesisk_soja",       4.0),
                ri("ing_gronsaksbuljong",    50.0),
                ri("ing_gullok",            100.0),
                ri("ing_vitlok",             10.0),
            ),
            steps = listOf(
                "Cook jasmine rice according to package instructions.",
                "Sauté onion and garlic in a pan until soft.",
                "Add chicken and cook through. Add soy sauce.",
                "Stir in crème fraîche and cooking cream. Simmer 5 minutes.",
                "Add vegetable stock and season to taste. Serve over rice.",
            ),
            notes = "Tweaked: rice 100g → 60g, chicken 100 → 200g. Target: ~620 kcal / ~57g P"
        ))

        // 3. Köttfärs & pasta — Lunch/Dinner storkok
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_kottfars_pasta",
            name = "Minced beef & pasta",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.LUNCH, MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_nottfars_12",      220.0),
                ri("ing_fullkornspasta",    50.0),
                ri("ing_krossade_tomater",  50.0),
                ri("ing_tomatpure",         30.0),
                ri("ing_gochujang",         30.0),
                ri("ing_gullok",           100.0),
            ),
            steps = listOf(
                "Cook pasta according to package instructions.",
                "Fry onion until soft. Add minced beef and brown thoroughly.",
                "Add tomato purée, crushed tomatoes and gochujang. Stir well.",
                "Simmer 10–15 minutes. Season to taste.",
                "Serve beef sauce over pasta.",
            ),
            notes = "Tweaked: beef 150g → 220g. Target: ~650 kcal / ~58g P"
        ))

        // 4. Makaronigratäng — Lunch/Dinner storkok
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_makaronigratang",
            name = "Macaroni gratin",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.LUNCH, MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_nottfars_12",  170.0),
                ri("ing_agg",           55.0),
                ri("ing_mjolk_laktos",  50.0),
                ri("ing_krossade_tomater", 50.0),
                ri("ing_arla_ost",      30.0),
                ri("ing_cavatappi",     50.0),
                ri("ing_gullok",       100.0),
                ri("ing_basilika",      15.0),
            ),
            steps = listOf(
                "Cook pasta until al dente. Drain and set aside.",
                "Fry onion and minced beef. Season with salt and pepper.",
                "Add crushed tomatoes and simmer 5 minutes.",
                "Mix egg and milk in a bowl.",
                "Layer pasta, beef and tomato in an oven dish. Pour egg mixture over. Top with grated cheese and basil.",
                "Bake at 200°C for 25–30 minutes until golden.",
            ),
            notes = "Tweaked: beef 150g → 170g. Target: ~700 kcal / ~60g P"
        ))

        // 5. Fried rice — Lunch/Dinner storkok
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_fried_rice",
            name = "Fried rice",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.LUNCH, MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_basmatiris",         70.0),
                ri("ing_kyckling_kronfagel", 180.0),
                ri("ing_agg",               110.0),
                ri("ing_ostronsas",          30.0),
                ri("ing_sesamolja",           5.0),
                ri("ing_frysta_artor",       100.0),
                ri("ing_morotter",           100.0),
                ri("ing_salladslok",          30.0),
            ),
            steps = listOf(
                "Cook and cool rice (ideally day-old rice works best).",
                "Cook chicken in a hot wok until done. Set aside.",
                "Scramble eggs in the same wok. Push to the side.",
                "Add rice, peas, carrot and chicken. Stir-fry on high heat 3–4 minutes.",
                "Add oyster sauce and sesame oil. Toss well.",
                "Top with sliced spring onion and serve.",
            ),
            notes = "Tweaked: rice 100g → 70g, chicken 125g → 180g, eggs 1 → 2. Target: ~740 kcal / ~76g P"
        ))

        // 6. Tonfisk m. ris — Dinner
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_tonfisk_ris",
            name = "Tuna with rice",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_creme_fraiche", 150.0),
                ri("ing_sesamfro",        5.0),
                ri("ing_tonfisk",       180.0),
                ri("ing_jasminris",      50.0),
                ri("ing_gurka",         150.0),
                ri("ing_rodlok",         80.0),
            ),
            steps = listOf(
                "Cook jasmine rice according to package instructions.",
                "Mix tuna with crème fraîche and sesame seeds.",
                "Slice cucumber and red onion.",
                "Serve tuna mix over rice with cucumber and red onion on the side.",
            ),
            notes = "Removed Hellman's, more crème fraîche & tuna. Target: ~560 kcal / ~60g P"
        ))

        // 7. Quesadillas — Dinner
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_quesadillas",
            name = "Quesadillas",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_tortilla",           93.0),
                ri("ing_mozzarella",         30.0),
                ri("ing_kyckling_kronfagel", 250.0),
                ri("ing_gullok",            100.0),
                ri("ing_paprika",           150.0),
                ri("ing_tomatpure",          30.0),
            ),
            steps = listOf(
                "Season and cook chicken until done. Slice into strips.",
                "Sauté onion and pepper until soft.",
                "Spread tomato purée on tortilla. Add chicken, vegetables and mozzarella.",
                "Fold tortilla and pan-fry 2–3 minutes per side until golden and cheese is melted.",
            ),
            notes = "Tweaked: 1 → 1.5 tortilla, chicken 200g → 250g. Target: ~800 kcal / ~82g P"
        ))

        // 8. Union burger — Dinner
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_union_burger",
            name = "Union burger",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_gullok",      50.0),
                ri("ing_nottfars_12", 200.0),
                ri("ing_smelteost",    40.0),
                ri("ing_burgarbrod",   70.0),
                ri("ing_agg",          55.0),
            ),
            steps = listOf(
                "Mix minced beef with egg, salt and pepper. Form into a patty.",
                "Fry onion until golden and caramelised.",
                "Cook burger patty in a hot pan 3–4 minutes per side.",
                "Top with melted cheddar during the last minute.",
                "Serve in burger bun with caramelised onion.",
            ),
            notes = "Tweaked: 12% beef 200g, added egg. Target: ~782 kcal / ~57g P"
        ))

        // 9. Biffstroganoff — Dinner
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_biffstroganoff",
            name = "Beef stroganoff",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_japansktris",    60.0),
                ri("ing_biff_ryggbiff", 230.0),
                ri("ing_graddfil",       50.0),
                ri("ing_matlagningsgradde", 25.0),
                ri("ing_kalvbuljong",    50.0),
                ri("ing_smor",            7.0),
            ),
            steps = listOf(
                "Cook Japanese rice according to package instructions.",
                "Slice beef into thin strips. Season with salt and pepper.",
                "Fry beef quickly in butter over high heat. Set aside.",
                "Add stock to the pan and reduce slightly.",
                "Stir in sour cream and cooking cream. Simmer 5 minutes.",
                "Return beef to the sauce. Serve over rice.",
            ),
            notes = "Tweaked: rice 80g → 60g, beef 200g → 230g. Target: ~745 kcal / ~61g P"
        ))

        // 10. Lax & potatis — Dinner
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_lax_potatis",
            name = "Salmon & potatoes",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.DINNER),
            ingredients = listOf(
                ri("ing_lax",          230.0),
                ri("ing_potatis",      220.0),
                ri("ing_creme_fraiche", 100.0),
                ri("ing_dijonsenap",     5.0),
                ri("ing_dill",          15.0),
            ),
            steps = listOf(
                "Boil potatoes until tender, about 20 minutes.",
                "Season salmon and bake or pan-fry 4–5 minutes per side.",
                "Mix crème fraîche with Dijon mustard and chopped dill for the sauce.",
                "Serve salmon and potatoes with dill sauce.",
            ),
            notes = "Tweaked: salmon 150g → 230g, potatoes 300g → 220g, added dill sauce. Target: ~740 kcal / ~56g P"
        ))

        // 11. Müsli banan grekghurt — Snack
        recipeRepository.saveRecipe(Recipe(
            id = "recipe_musli_snack",
            name = "Muesli with banana & yoghurt",
            type = RecipeType.MEAL,
            mealCategories = setOf(MealCategory.SNACK),
            ingredients = listOf(
                ri("ing_musli_gold",       35.0),
                ri("ing_banan",            60.0),
                ri("ing_grekghurt_larsa", 200.0),
            ),
            steps = listOf(
                "Add yoghurt to a bowl.",
                "Top with muesli and sliced banana.",
            ),
            notes = "Tweaked: muesli 25g → 35g, agave removed. Target: ~285 kcal / ~29g P"
        ))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Meal Plan — current week
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun seedMealPlan() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val sunday = monday.plus(6, DateTimeUnit.DAY)

        val days = (0..6).map { offset ->
            val date = monday.plus(offset, DateTimeUnit.DAY)
            val isWeekend = offset == 4 || offset == 5  // Fri, Sat
            val isSunday = offset == 6

            val meals = when {
                isWeekend -> listOf(
                    MealSlot(MealCategory.LUNCH,  "recipe_bovete_kyckling"),
                    MealSlot(MealCategory.DINNER, "recipe_quesadillas"),
                    MealSlot(MealCategory.SNACK,  "recipe_musli_snack"),
                )
                isSunday -> listOf(
                    MealSlot(MealCategory.LUNCH,  "recipe_bovete_kyckling"),
                    MealSlot(MealCategory.DINNER, "recipe_quesadillas"),
                )
                else -> listOf(
                    MealSlot(MealCategory.LUNCH,  "recipe_bovete_kyckling"),
                    MealSlot(MealCategory.DINNER, "recipe_tonfisk_ris"),
                )
            }

            DayPlan(
                id = "day_$date",
                date = date,
                meals = meals,
                proteinPowderGrams = if (isWeekend || isSunday) 0.0 else 24.5,
                goal = DailyGoal(
                    kcalTarget = if (isWeekend) 1700 else 1350,
                    proteinTarget = if (isWeekend) 130 else 120
                )
            )
        }

        mealPlanRepository.saveMealPlan(MealPlan(
            id = "plan_week_$monday",
            name = "Week of $monday",
            startDate = monday,
            endDate = sunday,
            days = days
        ))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun i(
        id: String, name: String, category: IngredientCategory,
        kcal: Double, protein: Double, fat: Double, carbs: Double,
        source: IngredientSource = IngredientSource.LABEL
    ) = Ingredient(
        id = id, name = name, category = category,
        kcalPer100g = kcal, proteinPer100g = protein,
        fatPer100g = fat, carbsPer100g = carbs,
        source = source
    )

    private fun ri(ingredientId: String, grams: Double) =
        RecipeIngredient(ingredientId = ingredientId, grams = grams)
}