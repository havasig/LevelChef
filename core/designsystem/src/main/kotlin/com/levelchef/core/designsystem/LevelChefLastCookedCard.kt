package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.BackgroundSurface
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.SuccessGreen
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary
import com.levelchef.core.ui.theme.WarningYellow

/** The Last Cooked Card base component (Figma node 171:543). */
@Composable
fun LevelChefLastCookedCard(title: String, time: String, stars: Int, modifier: Modifier = Modifier, label: String = "Last Cooked") {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSurface),
        border = cardBorder(),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(AccentPrimary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("🕐") }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, color = TextSecondary, style = LevelChefTextStyles.captionBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, color = TextPrimary, style = LevelChefTextStyles.bodyRegularBold)
                    Text("· $time", color = TextSecondary, style = LevelChefTextStyles.bodySmall)
                }
                Text("★".repeat(stars) + "☆".repeat((5 - stars).coerceAtLeast(0)), color = WarningYellow)
            }
            Text("✓", color = SuccessGreen, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
