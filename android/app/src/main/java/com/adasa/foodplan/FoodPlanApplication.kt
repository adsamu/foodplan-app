package com.adasa.foodplan

import android.app.Application
import com.adasa.foodplan.data.local.DatabaseSeeder
import com.adasa.foodplan.data.remote.SettingsSync
import com.adasa.foodplan.data.repository.MealPlanRepository
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
    @Inject lateinit var settingsSync: SettingsSync

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            FirebaseFunctions.getInstance().useEmulator("10.0.2.2", 5001)
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
        }

        appScope.launch { seeder.seedIfEmpty() }

        mealPlanRepository.startListening(DEFAULT_USER_ID, appScope)
        settingsSync.start(DEFAULT_USER_ID, appScope)
    }

    companion object {
        const val DEFAULT_USER_ID = "default_user"
    }
}