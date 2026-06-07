package com.adasa.foodplan.di

import com.adasa.foodplan.domain.usecase.FirebaseRemoteMealPlanOptimizer
import com.adasa.foodplan.domain.usecase.RemoteMealPlanOptimizer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions = FirebaseFunctions.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteOptimizerModule {
    @Binds
    @Singleton
    abstract fun bindRemoteMealPlanOptimizer(
        impl: FirebaseRemoteMealPlanOptimizer
    ): RemoteMealPlanOptimizer
}
