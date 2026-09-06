package com.levelchef.feature.trophyroom

import com.levelchef.core.model.Badge

/** Maps domain models (from [com.levelchef.domain.repository]) onto this screen's presentation types. */

internal fun Badge.toBadgeUiModel() = BadgeUiModel(
    id = id,
    emoji = emoji,
    name = name,
    description = description,
    progressCurrent = progressCurrent,
    progressTarget = progressTarget,
    earned = isEarned,
)

private const val MINUTES_PER_HOUR = 60

/** "0h" below an hour, else whole hours plus any leftover minutes, e.g. "14h 30m". */
internal fun formatKitchenTime(totalMinutes: Int): String {
    val hours = totalMinutes / MINUTES_PER_HOUR
    val minutes = totalMinutes % MINUTES_PER_HOUR
    return when {
        hours <= 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}
