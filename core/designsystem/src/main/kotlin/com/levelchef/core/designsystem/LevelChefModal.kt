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
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary

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
        LevelChefCard(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(48.dp).background(AccentPrimary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, color = AccentPrimary, style = LevelChefTextStyles.h2)
            }
            Text(title, color = TextPrimary, style = LevelChefTextStyles.bodyLargeBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(message, color = TextSecondary, style = LevelChefTextStyles.bodyRegular, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LevelChefButton(label = cancelLabel, type = ButtonType.SECONDARY, onClick = onDismiss)
                    LevelChefButton(label = confirmLabel, type = ButtonType.PRIMARY, onClick = onConfirm)
                }
            }
        }
    }
}
