package com.levelchef.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The Input Field base component (Figma node 23:26) — label above a bordered text box. */
@Composable
fun LevelChefInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
) {
    val colors = LevelChefTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = colors.textPrimary, style = LevelChefTextStyles.bodyRegularBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            placeholder = {
                Text(
                    placeholder,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            textStyle = LevelChefTextStyles.bodyRegular.copy(color = colors.textPrimary),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedBorderColor = colors.border,
                unfocusedBorderColor = colors.border,
                cursorColor = colors.textPrimary,
            ),
        )
    }
}

@LevelChefPreview
@Composable
private fun LevelChefInputFieldPreview() {
    var value by remember { mutableStateOf("") }
    LevelChefTheme {
        LevelChefInputField(
            label = "Label",
            value = value,
            onValueChange = { value = it },
            placeholder = "Placeholder text",
        )
    }
}
