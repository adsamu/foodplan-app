package com.adasa.foodplan.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.adasa.foodplan.data.local.FoodPlanDatabase
import com.adasa.foodplan.data.local.dao.OptimizerSettingsDao
import com.adasa.foodplan.data.local.dao.RecipeRatingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideRecipeRatingDao(db: FoodPlanDatabase): RecipeRatingDao = db.recipeRatingDao()

    @Provides
    @Singleton
    fun provideOptimizerSettingsDao(db: FoodPlanDatabase): OptimizerSettingsDao = db.optimizerSettingsDao()
}
