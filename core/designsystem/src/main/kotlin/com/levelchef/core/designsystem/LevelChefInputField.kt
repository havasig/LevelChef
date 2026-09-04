package com.levelchef.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.BackgroundSurface
import com.levelchef.core.ui.theme.BorderDefault
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary

/** The Input Field base component (Figma node 23:26) — label above a bordered text box. */
@Composable
fun LevelChefInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextPrimary, style = LevelChefTextStyles.bodyRegularBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSecondary, style = MaterialTheme.typography.bodyMedium) },
            textStyle = LevelChefTextStyles.bodyRegular.copy(color = TextPrimary),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BackgroundSurface,
                unfocusedContainerColor = BackgroundSurface,
                focusedBorderColor = BorderDefault,
                unfocusedBorderColor = BorderDefault,
                cursorColor = TextPrimary,
            ),
        )
    }
}
