package com.levelchef.android.ui.nav

import androidx.annotation.StringRes
import com.levelchef.android.R

sealed class LevelChefDestination(val route: String, @param:StringRes val labelRes: Int) {
    data object Home : LevelChefDestination("home", R.string.nav_home)
    data object Recipes : LevelChefDestination("recipes", R.string.nav_recipes)
    data object Trophies : LevelChefDestination("trophies", R.string.nav_trophies)
    data object Log : LevelChefDestination("log", R.string.nav_log)
}
