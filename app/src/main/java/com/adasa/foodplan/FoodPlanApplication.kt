package com.adasa.foodplan

import android.app.Application
import com.adasa.foodplan.data.local.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FoodPlanApplication : Application() {

    @Inject
    lateinit var seeder: DatabaseSeeder

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            seeder.seedIfEmpty()
        }
    }
}