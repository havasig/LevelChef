package com.levelchef.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.levelchef.android.ui.nav.LevelChefApp
import com.levelchef.core.ui.theme.LevelChefTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // LevelChef ships dark-only — force it regardless of the device's system theme.
            LevelChefTheme(darkTheme = true) {
                LevelChefApp()
            }
        }
    }
}
