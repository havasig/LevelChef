package com.levelchef.android.ui.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.levelchef.android.R

/** A top-level destination reachable from the bottom navigation bar. */
sealed class LevelChefDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    data object Home : LevelChefDestination("home", R.string.nav_home, Icons.Filled.Home)
    data object Recipes : LevelChefDestination("recipes", R.string.nav_recipes, Icons.AutoMirrored.Filled.List)
    data object Trophies : LevelChefDestination("trophies", R.string.nav_trophies, Icons.Filled.Star)
}
