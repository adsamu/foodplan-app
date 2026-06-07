package com.adasa.foodplan.di

import android.content.Context
import androidx.room.Room
import com.adasa.foodplan.data.local.FoodPlanDatabase
import com.adasa.foodplan.data.local.dao.IngredientDao
import com.adasa.foodplan.data.local.dao.MealPlanDao
import com.adasa.foodplan.data.local.dao.RecipeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FoodPlanDatabase =
        Room.databaseBuilder(
            context,
            FoodPlanDatabase::class.java,
            "foodplan.db"
        )
            .addMigrations(FoodPlanDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    @Singleton
    fun provideIngredientDao(db: FoodPlanDatabase): IngredientDao = db.ingredientDao()

    @Provides
    @Singleton
    fun provideRecipeDao(db: FoodPlanDatabase): RecipeDao = db.recipeDao()

    @Provides
    @Singleton
    fun provideMealPlanDao(db: FoodPlanDatabase): MealPlanDao = db.mealPlanDao()
}