package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.BackgroundSurface
import com.levelchef.core.ui.theme.BorderDefault
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary

/** The Search Bar base component (Figma node 251:1030). */
@Composable
fun LevelChefSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "Search...") {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundSurface, RoundedCornerShape(12.dp))
            .border(0.5.dp, BorderDefault, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) Text(placeholder, color = TextSecondary, style = LevelChefTextStyles.bodyRegular)
            BasicTextField(value = query, onValueChange = onQueryChange, textStyle = LevelChefTextStyles.bodyRegular.copy(color = TextPrimary))
        }
    }
}
