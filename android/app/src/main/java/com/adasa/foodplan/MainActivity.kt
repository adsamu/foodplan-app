package com.adasa.foodplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.adasa.foodplan.ui.navigation.AppNavGraph
import com.adasa.foodplan.ui.profile.AppTheme
import com.adasa.foodplan.ui.profile.ProfileViewModel
import com.adasa.foodplan.ui.theme.FoodPlanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by profileViewModel.theme.collectAsStateWithLifecycle()
            val darkTheme = when (theme) {
                AppTheme.DARK   -> true
                AppTheme.LIGHT  -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            FoodPlanTheme(darkTheme = darkTheme) {
                AppNavGraph()
            }
        }
    }
}