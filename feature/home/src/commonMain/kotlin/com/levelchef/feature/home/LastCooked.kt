package com.levelchef.feature.home

/** [daysAgo] is whole days since the cook (0 = today); the screen turns it into localized text. */
data class LastCooked(val recipeName: String, val daysAgo: Int, val stars: Int)
