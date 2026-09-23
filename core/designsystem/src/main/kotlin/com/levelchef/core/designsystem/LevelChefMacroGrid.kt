package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** A row of label/value cells (Figma node 371:728's Calories/Protein/Carbs/Fat grid) — takes
 * plain (label, value) pairs so this stays free of any particular domain model. */
@Composable
fun LevelChefMacroGrid(cells: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        cells.forEach { (label, value) -> MacroGridCell(label, value, Modifier.weight(1f)) }
    }
}

@Composable
private fun MacroGridCell(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = colors.textSecondary, style = LevelChefTextStyles.captionRegular, maxLines = 1)
        Text(value, color = colors.textPrimary, style = LevelChefTextStyles.bodySmallBold, maxLines = 1)
    }
}

@LevelChefPreview
@Composable
private fun LevelChefMacroGridPreview() {
    LevelChefTheme {
        LevelChefMacroGrid(
            cells = listOf(
                "Calories" to "450 kcal",
                "Protein" to "30g",
                "Carbs" to "40g",
                "Fat" to "12g",
            ),
        )
    }
}
