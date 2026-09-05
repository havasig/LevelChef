package com.levelchef.android.ui.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.BadgeStyle
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.IconButtonStyle
import com.levelchef.core.designsystem.LevelChefAvatar
import com.levelchef.core.designsystem.LevelChefBadge
import com.levelchef.core.designsystem.LevelChefBottomNavigationBar
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefCard
import com.levelchef.core.designsystem.LevelChefCheckbox
import com.levelchef.core.designsystem.LevelChefDivider
import com.levelchef.core.designsystem.LevelChefDropdown
import com.levelchef.core.designsystem.LevelChefIconButton
import com.levelchef.core.designsystem.LevelChefInputField
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefLastCookedCard
import com.levelchef.core.designsystem.LevelChefList
import com.levelchef.core.designsystem.LevelChefListEntry
import com.levelchef.core.designsystem.LevelChefModal
import com.levelchef.core.designsystem.LevelChefNavItem
import com.levelchef.core.designsystem.LevelChefPageIndicator
import com.levelchef.core.designsystem.LevelChefRadioButton
import com.levelchef.core.designsystem.LevelChefRecipeCard
import com.levelchef.core.designsystem.LevelChefSearchBar
import com.levelchef.core.designsystem.LevelChefSnackbar
import com.levelchef.core.designsystem.LevelChefSwitch
import com.levelchef.core.designsystem.LevelChefTabBar
import com.levelchef.core.designsystem.LevelChefTag
import com.levelchef.core.designsystem.LevelChefTopAppBarHome
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.designsystem.LevelChefTopAppBarSearch
import com.levelchef.core.designsystem.LevelChefWeeklyChallengeCard
import com.levelchef.core.designsystem.TagColor
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * Renders every component in `core:designsystem` for visual verification — not part of the
 * product, reachable only via the hidden 5-tap gesture on the Home bottom-nav item (see
 * `LevelChefNav.kt`).
 */
@Composable
fun DesignSystemShowcaseScreen(onBackClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item { LevelChefTopAppBarInner(title = "Design System", onBackClick = onBackClick) }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                TypographySection()
                ShowcaseSection("Buttons") { ButtonsSection() }
                ShowcaseSection("Input Field") { InputFieldSection() }
                ShowcaseSection("Avatar") { AvatarSection() }
                ShowcaseSection("Divider") { LevelChefDivider() }
                ShowcaseSection("Card") { CardSection() }
                ShowcaseSection("Recipe Card") { RecipeCardSection() }
                ShowcaseSection("Last Cooked Card") { LevelChefLastCookedCard(title = "Tofu stir-fry", time = "3 days ago", stars = 4) }
                ShowcaseSection("Weekly Challenge Card") { WeeklyChallengeCardSection() }
                ShowcaseSection("List") { ListSection() }
                ShowcaseSection("Page Indicator") { PageIndicatorSection() }
                ShowcaseSection("Badge") { BadgeSection() }
                ShowcaseSection("Tag") { TagSection() }
                ShowcaseSection("Checkbox") { CheckboxSection() }
                ShowcaseSection("Radio Button") { RadioButtonSection() }
                ShowcaseSection("Toggle") { ToggleSection() }
                ShowcaseSection("Icon Button") { IconButtonSection() }
                ShowcaseSection("Snackbar") { LevelChefSnackbar("Your profile has been updated.") }
                ShowcaseSection("Modal") { ModalSection() }
                ShowcaseSection("Top App Bar — Home") { LevelChefTopAppBarHome("LevelChef", onSettingsClick = {}) }
                ShowcaseSection("Top App Bar — Inner") { LevelChefTopAppBarInner("Recipe details", onBackClick = {}, onSettingsClick = {}) }
                ShowcaseSection("Top App Bar — Search") { LevelChefTopAppBarSearch("Search recipes...", onBackClick = {}) }
                ShowcaseSection("Bottom Navigation Bar") { BottomNavigationBarSection() }
                ShowcaseSection("Tab Bar") { TabBarSection() }
                ShowcaseSection("Search Bar") { SearchBarSection() }
                ShowcaseSection("Dropdown") { DropdownSection() }
            }
        }
    }
}

@Composable
private fun ShowcaseSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = LevelChefTheme.colors.textSecondary, style = LevelChefTextStyles.captionBold)
        content()
    }
}

@Composable
private fun TypographySection() {
    ShowcaseSection("Typography") {
        val textPrimary = LevelChefTheme.colors.textPrimary
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Heading/H1 — Page Title", color = textPrimary, style = LevelChefTextStyles.h1)
            Text("Heading/H2 — Section Header", color = textPrimary, style = LevelChefTextStyles.h2)
            Text("Body/Large — Introductory paragraph text", color = textPrimary, style = LevelChefTextStyles.bodyLarge)
            Text("Body/Large Bold — Emphasized large text", color = textPrimary, style = LevelChefTextStyles.bodyLargeBold)
            Text("Body/Regular — Default body copy for content", color = textPrimary, style = LevelChefTextStyles.bodyRegular)
            Text("Body/Regular Bold — Bold labels and actions", color = textPrimary, style = LevelChefTextStyles.bodyRegularBold)
            Text("Body/Small — Supporting details and hints", color = textPrimary, style = LevelChefTextStyles.bodySmall)
            Text("Body/Small Bold — Small bold labels", color = textPrimary, style = LevelChefTextStyles.bodySmallBold)
            Text("Caption/Regular — Timestamps and metadata", color = textPrimary, style = LevelChefTextStyles.captionRegular)
            Text("Caption/Bold — Tags and badges", color = textPrimary, style = LevelChefTextStyles.captionBold)
        }
    }
}

@Composable
private fun ButtonsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ButtonType.entries.forEach { type ->
            LevelChefButton(label = type.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, type = type, onClick = {})
        }
    }
}

@Composable
private fun InputFieldSection() {
    var value by remember { mutableStateOf("") }
    LevelChefInputField(label = "Label", value = value, onValueChange = { value = it }, placeholder = "Placeholder text")
}

@Composable
private fun AvatarSection() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LevelChefAvatar(initials = "AB")
    }
}

@Composable
private fun CardSection() {
    val colors = LevelChefTheme.colors
    LevelChefCard {
        Text("Card Title", color = colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold)
        Text("Description text goes here. This card can hold any content.", color = colors.textSecondary, style = LevelChefTextStyles.bodyRegular)
    }
}

@Composable
private fun RecipeCardSection() {
    LevelChefRecipeCard(
        emoji = "🥩",
        title = "Chicken curry with coconut milk",
        xp = 45,
        minutes = 25,
        difficulty = "Easy",
        onClick = {},
        tagLabel = "New ingredient",
        tagEmoji = "🌿",
        tagColor = TagColor.PURPLE,
    )
}

@Composable
private fun WeeklyChallengeCardSection() {
    LevelChefWeeklyChallengeCard(
        title = "Cook one Asian-inspired dish this week",
        xp = 200,
        inProgress = true,
        action = { LevelChefButton(label = "Done", type = ButtonType.SECONDARY, onClick = {}) },
    )
}

@Composable
private fun ListSection() {
    LevelChefList(
        entries = listOf(
            LevelChefListEntry("JA", "John Appleseed", "Product Designer"),
            LevelChefListEntry("SC", "Sarah Chen", "Engineer"),
            LevelChefListEntry("ML", "Marcus Lee", "Design Lead"),
        ),
    )
}

@Composable
private fun PageIndicatorSection() {
    LevelChefPageIndicator(pageCount = 4, currentPage = 1)
}

@Composable
private fun BadgeSection() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LevelChefBadge("Active", style = BadgeStyle.LIGHT)
        LevelChefBadge("Active", style = BadgeStyle.DARK)
    }
}

@Composable
private fun TagSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TagColor.entries.forEach { color ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LevelChefTag(label = "Tag Label", color = color, emoji = "⚡", selected = false)
                LevelChefTag(label = "Tag Label", color = color, emoji = "⚡", selected = true, showClose = true, onClose = {})
            }
        }
    }
}

@Composable
private fun CheckboxSection() {
    var checked by remember { mutableStateOf(true) }
    LevelChefCheckbox(checked = checked, onCheckedChange = { checked = it }, label = if (checked) "Checked" else "Unchecked")
}

@Composable
private fun RadioButtonSection() {
    var selected by remember { mutableStateOf(true) }
    LevelChefRadioButton(selected = selected, onClick = { selected = !selected }, label = if (selected) "Selected" else "Unselected")
}

@Composable
private fun ToggleSection() {
    var checked by remember { mutableStateOf(true) }
    LevelChefSwitch(checked = checked, onCheckedChange = { checked = it })
}

@Composable
private fun IconButtonSection() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LevelChefIconButton(icon = Icons.Filled.Add, contentDescription = "Add", style = IconButtonStyle.FILLED, onClick = {})
        LevelChefIconButton(icon = Icons.Filled.Settings, contentDescription = "Settings", style = IconButtonStyle.OUTLINED, onClick = {})
    }
}

@Composable
private fun ModalSection() {
    var showModal by remember { mutableStateOf(false) }
    LevelChefButton(label = "Show modal", type = ButtonType.SECONDARY, onClick = { showModal = true })
    if (showModal) {
        LevelChefModal(
            title = "Unsaved Changes",
            message = "Are you sure you want to discard your changes? This action cannot be undone.",
            onDismiss = { showModal = false },
            onConfirm = { showModal = false },
        )
    }
}

@Composable
private fun BottomNavigationBarSection() {
    var selected by remember { mutableStateOf(0) }
    val tabs = listOf(Pair("Home", Icons.Filled.Home), Pair("Recipes", Icons.AutoMirrored.Filled.List), Pair("Trophies", Icons.Filled.Star))
    LevelChefBottomNavigationBar(
        items = tabs.mapIndexed { index, (label, icon) ->
            LevelChefNavItem(icon = icon, label = label, selected = index == selected, onClick = { selected = index })
        },
    )
}

@Composable
private fun TabBarSection() {
    var selected by remember { mutableStateOf(0) }
    LevelChefTabBar(tabs = listOf("Details", "Security", "Billing"), selectedIndex = selected, onTabSelected = { selected = it })
}

@Composable
private fun SearchBarSection() {
    var query by remember { mutableStateOf("") }
    LevelChefSearchBar(query = query, onQueryChange = { query = it })
}

@Composable
private fun DropdownSection() {
    var selected by remember { mutableStateOf("Development") }
    val options = listOf("Development", "Design", "Marketing")
    LevelChefDropdown(label = "Category", selectedOption = selected, options = options, onOptionSelected = { selected = it })
}

@LevelChefPreview
@Composable
private fun DesignSystemShowcaseScreenPreview() {
    LevelChefTheme { DesignSystemShowcaseScreen(onBackClick = {}) }
}
