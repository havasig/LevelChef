package com.levelchef.shared.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.levelchef.shared.generated.resources.Res
import com.levelchef.shared.generated.resources.nav_home
import com.levelchef.shared.generated.resources.nav_recipes
import com.levelchef.shared.generated.resources.nav_trophies
import org.jetbrains.compose.resources.StringResource

/** A top-level destination reachable from the bottom navigation bar — the iOS counterpart of
 * androidApp's `LevelChefDestination`. */
sealed class SharedDestination(
    val route: String,
    val labelRes: StringResource,
    val icon: ImageVector,
) {
    data object Home : SharedDestination("home", Res.string.nav_home, Icons.Filled.Home)
    data object Recipes : SharedDestination("recipes", Res.string.nav_recipes, Icons.AutoMirrored.Filled.List)
    data object Trophies : SharedDestination("trophies", Res.string.nav_trophies, Icons.Filled.Star)
}
