package com.levelchef.android.ui.nav

sealed class LevelChefDestination(val route: String, val label: String) {
    data object Home : LevelChefDestination("home", "Home")
    data object Recipes : LevelChefDestination("recipes", "Recipes")
    data object Trophies : LevelChefDestination("trophies", "Trophies")
    data object Log : LevelChefDestination("log", "Log")
}
