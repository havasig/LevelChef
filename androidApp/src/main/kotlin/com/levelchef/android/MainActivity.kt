package com.levelchef.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.levelchef.android.ui.nav.LevelChefApp
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * `AppCompatActivity` (not `ComponentActivity`) so `AppCompatDelegate` can apply the in-app
 * theme (`setDefaultNightMode`) and language (`setApplicationLocales`) chosen in Settings — the
 * night-mode override flows into `isSystemInDarkTheme()`, which `LevelChefTheme` already reads.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LevelChefTheme {
                LevelChefApp()
            }
        }
    }
}
