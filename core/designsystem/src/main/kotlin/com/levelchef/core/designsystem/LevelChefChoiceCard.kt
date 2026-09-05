package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.OnAccent

private val CardShape = RoundedCornerShape(12.dp)

/**
 * A selectable option card (Figma "Choice Card", nodes 236:1228 / 465:668) — leading emoji, a
 * title and optional subtitle, and a selected state shown as an accent border plus, when
 * [showCheck] is set (multi-select lists), a trailing check pill.
 */
@Composable
fun LevelChefChoiceCard(
    emoji: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showCheck: Boolean = false,
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(colors.surface, CardShape)
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = if (selected) colors.accentPrimary else colors.border,
                shape = CardShape,
            )
            .clickable(interactionSource = null, indication = null, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, style = LevelChefTextStyles.h2.copy(fontSize = 28.sp), textAlign = TextAlign.Center)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = colors.textPrimary, style = LevelChefTextStyles.bodyRegularBold)
            if (subtitle != null) {
                Text(subtitle, color = colors.textSecondary, style = LevelChefTextStyles.bodySmall)
            }
        }
        if (showCheck && selected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(colors.accentPrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = OnAccent,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefChoiceCardPreview() {
    var selected by remember { mutableStateOf(true) }
    LevelChefTheme {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
            LevelChefChoiceCard(
                emoji = "🍳",
                title = "Comfortable",
                subtitle = "I cook a few times a week",
                selected = selected,
                onClick = { selected = !selected },
            )
            LevelChefChoiceCard(
                emoji = "🌾",
                title = "Gluten",
                selected = selected,
                onClick = { selected = !selected },
                showCheck = true,
            )
        }
    }
}
