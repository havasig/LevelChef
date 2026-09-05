package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The Modal extended component (Figma node 251:982) — an icon-chip alert dialog with cancel/confirm actions. */
@Composable
fun LevelChefModal(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String = "!",
    cancelLabel: String = "Cancel",
    confirmLabel: String = "Confirm",
) {
    Dialog(onDismissRequest = onDismiss) {
        ModalContent(title, message, onDismiss, onConfirm, modifier, icon, cancelLabel, confirmLabel)
    }
}

// Split out from LevelChefModal so the preview below can render the card content directly —
// @Preview can't render a Dialog/Popup's content, since it's composed into a separate window.
@Composable
private fun ModalContent(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String = "!",
    cancelLabel: String = "Cancel",
    confirmLabel: String = "Confirm",
) {
    val colors = LevelChefTheme.colors
    LevelChefCard(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).background(colors.accentPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, color = colors.accentPrimary, style = LevelChefTextStyles.h2)
        }
        Text(title, color = colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(message, color = colors.textSecondary, style = LevelChefTextStyles.bodyRegular, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LevelChefButton(label = cancelLabel, type = ButtonType.SECONDARY, onClick = onDismiss)
                LevelChefButton(label = confirmLabel, type = ButtonType.PRIMARY, onClick = onConfirm)
            }
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefModalPreview() {
    LevelChefTheme {
        ModalContent(
            title = "Unsaved Changes",
            message = "Are you sure you want to discard your changes? This action cannot be undone.",
            onDismiss = {},
            onConfirm = {},
        )
    }
}
