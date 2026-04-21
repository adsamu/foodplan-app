package com.adasa.foodplan.ui.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppTheme { SYSTEM, LIGHT, DARK }

private val THEME_KEY = stringPreferencesKey("theme")

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val theme: StateFlow<AppTheme> = dataStore.data
        .map { prefs ->
            when (prefs[THEME_KEY]) {
                "light" -> AppTheme.LIGHT
                "dark"  -> AppTheme.DARK
                else    -> AppTheme.SYSTEM
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[THEME_KEY] = when (theme) {
                    AppTheme.LIGHT  -> "light"
                    AppTheme.DARK   -> "dark"
                    AppTheme.SYSTEM -> "system"
                }
            }
        }
    }
}
