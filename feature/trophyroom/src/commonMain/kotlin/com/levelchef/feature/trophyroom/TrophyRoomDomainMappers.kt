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
