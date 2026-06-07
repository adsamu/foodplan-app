package com.adasa.foodplan.data.remote

import com.adasa.foodplan.data.repository.SettingsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-way mirror from local DataStore to users/{userId}/settings/main.
 * The Cloud Function reads from that document, so every settings change
 * must be flushed before triggering a plan generation.
 */
@Singleton
class SettingsSync @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val firestore: FirebaseFirestore
) {
    @OptIn(FlowPreview::class)
    fun start(userId: String, scope: CoroutineScope) {
        settingsRepository.getMealPlanConfigFlow()
            .debounce(800)
            .distinctUntilChanged()
            .onEach { config -> writeOnce(userId, config.toFirestoreMap()) }
            .launchIn(scope)
    }

    /** Synchronously push the current settings; useful before the first plan generation. */
    suspend fun pushNow(userId: String) {
        val config = settingsRepository.getMealPlanConfig()
        writeOnce(userId, config.toFirestoreMap())
    }

    private suspend fun writeOnce(userId: String, payload: Map<String, Any?>) {
        firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document(SETTINGS_DOC_ID)
            .set(payload)
            .await()
    }

    companion object {
        const val SETTINGS_DOC_ID = "main"
    }
}
