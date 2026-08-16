package com.adasa.foodplan

import android.app.Application
import com.adasa.foodplan.data.local.DatabaseSeeder
import com.adasa.foodplan.data.remote.SettingsSync
import com.adasa.foodplan.data.repository.IngredientRepository
import com.adasa.foodplan.data.repository.MealPlanRepository
import com.adasa.foodplan.data.repository.RecipeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FoodPlanApplication : Application() {

    @Inject lateinit var seeder: DatabaseSeeder
    @Inject lateinit var mealPlanRepository: MealPlanRepository
    @Inject lateinit var recipeRepository: RecipeRepository
    @Inject lateinit var ingredientRepository: IngredientRepository
    @Inject lateinit var settingsSync: SettingsSync

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Temporarily disabled to test a debug build against the live backend on a physical device
        // (10.0.2.2 only resolves inside the Android Studio emulator, not on real phones).
        // Re-enable for normal local emulator-based development.
        // if (BuildConfig.DEBUG) {
        //     FirebaseFunctions.getInstance().useEmulator("10.0.2.2", 5001)
        //     FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
        // }

        recipeRepository.startListening(appScope)
        ingredientRepository.startListening(appScope)
        mealPlanRepository.startListening(DEFAULT_USER_ID, appScope)
        settingsSync.start(DEFAULT_USER_ID, appScope)

        appScope.launch { seeder.seedIfEmpty() }
    }

    companion object {
        const val DEFAULT_USER_ID = "default_user"
    }
}