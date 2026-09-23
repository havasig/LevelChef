package com.levelchef.feature.recipedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levelchef.core.designsystem.BadgeStyle
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefBadge
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefStepCard
import com.levelchef.core.designsystem.LevelChefTag
import com.levelchef.core.designsystem.TagColor
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.RecipeStep
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.OnAccent

/** Section composables for [RecipeDetailScreen], kept internal to this feature. */

@Composable
internal fun HeroTile(emoji: String, xpReward: Int) {
    val colors = LevelChefTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(colors.accentPrimary.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 88.sp)
        LevelChefBadge(
            text = stringResource(R.string.recipe_detail_xp_badge, xpReward),
            style = BadgeStyle.LIGHT,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        )
    }
}

@Composable
internal fun TitleAndTags(recipe: Recipe) {
    val colors = LevelChefTheme.colors
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(recipe.name, color = colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold)
        val tags = buildList {
            add(TagSpec(stringResource(R.string.recipe_detail_tag_time, recipe.timeMinutes), "⏱", TagColor.PURPLE))
            recipe.proteinGrams?.let {
                add(TagSpec(stringResource(R.string.recipe_detail_tag_protein, it), "💪", TagColor.YELLOW))
            }
            add(TagSpec(recipe.difficulty.label(), null, TagColor.GREEN))
            add(
                TagSpec(
                    stringResource(R.string.recipe_detail_tag_ingredients, recipe.ingredients.size),
                    "🛒",
                    TagColor.RED,
                ),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { LevelChefTag(label = it.label, emoji = it.emoji, color = it.color) }
                }
            }
        }
    }
}

private data class TagSpec(val label: String, val emoji: String?, val color: TagColor)

@Composable
internal fun recipeMacroCells(recipe: Recipe): List<Pair<String, String>> {
    val gramsFormat = R.string.recipe_detail_macro_grams_value
    val unknown = stringResource(R.string.recipe_detail_macro_unknown)
    return listOf(
        stringResource(R.string.recipe_detail_macro_calories) to
            (recipe.caloriesKcal?.let { stringResource(R.string.recipe_detail_macro_kcal_value, it) } ?: unknown),
        stringResource(R.string.recipe_detail_macro_protein) to
            (recipe.proteinGrams?.let { stringResource(gramsFormat, it) } ?: unknown),
        stringResource(R.string.recipe_detail_macro_carbs) to
            (recipe.carbsGrams?.let { stringResource(gramsFormat, it) } ?: unknown),
        stringResource(R.string.recipe_detail_macro_fat) to
            (recipe.fatGrams?.let { stringResource(gramsFormat, it) } ?: unknown),
    )
}

@Composable
internal fun IngredientsSection(
    recipe: Recipe,
    servings: Int,
    checked: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    val colors = LevelChefTheme.colors
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.recipe_detail_ingredients_title),
            color = colors.textPrimary,
            style = LevelChefTextStyles.bodyLargeBold,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
                .background(colors.surface, RoundedCornerShape(16.dp)),
        ) {
            recipe.ingredients.forEachIndexed { index, ingredient ->
                IngredientRow(
                    text = ingredient.toDisplayLine(servings, recipe.servings),
                    isNew = ingredient.isNewToUser,
                    checked = index in checked,
                    onToggle = { onToggle(index) },
                )
            }
        }
    }
}

@Composable
private fun IngredientRow(text: String, isNew: Boolean, checked: Boolean, onToggle: () -> Unit) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .then(
                    if (checked) {
                        Modifier.background(colors.accentPrimary, RoundedCornerShape(5.dp))
                    } else {
                        Modifier.border(2.dp, colors.accentPrimary, RoundedCornerShape(5.dp))
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = OnAccent, modifier = Modifier.size(12.dp))
            }
        }
        Text(
            text,
            color = if (checked) colors.textSecondary else colors.textPrimary,
            style = LevelChefTextStyles.bodySmall,
            textDecoration = if (checked) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
        )
        if (isNew) LevelChefBadge(stringResource(R.string.recipe_detail_ingredient_new), style = BadgeStyle.LIGHT)
    }
}

@Composable
internal fun StepsSection(
    steps: List<RecipeStep>,
    runningTimerStepIndex: Int?,
    timerSecondsRemaining: Int,
    onStartTimer: (stepIndex: Int, minutes: Int) -> Unit,
    onCancelTimer: () -> Unit,
) {
    val colors = LevelChefTheme.colors
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.recipe_detail_steps_title),
            color = colors.textPrimary,
            style = LevelChefTextStyles.bodyLargeBold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            steps.forEachIndexed { index, step ->
                LevelChefStepCard(
                    number = index + 1,
                    text = step.text,
                    trailingContent = step.timerMinutes?.let { minutes ->
                        {
                            if (index == runningTimerStepIndex) {
                                RunningTimerChip(secondsRemaining = timerSecondsRemaining, onClick = onCancelTimer)
                            } else {
                                IdleTimerChip(minutes = minutes, onClick = { onStartTimer(index, minutes) })
                            }
                        }
                    },
                )
            }
        }
    }
}

/** The "Start N min timer" pill. */
@Composable
private fun IdleTimerChip(minutes: Int, onClick: () -> Unit) {
    TimerChipShell(
        icon = Icons.Filled.PlayArrow,
        label = stringResource(R.string.recipe_detail_start_timer, minutes),
        onClick = onClick,
    )
}

/** A live "MM:SS" countdown, tap to cancel. */
@Composable
private fun RunningTimerChip(secondsRemaining: Int, onClick: () -> Unit) {
    val minutes = secondsRemaining / SECONDS_PER_MINUTE
    val seconds = secondsRemaining % SECONDS_PER_MINUTE
    TimerChipShell(
        icon = Icons.Filled.Close,
        label = "%d:%02d".format(minutes, seconds),
        contentDescription = stringResource(R.string.recipe_detail_cancel_timer),
        onClick = onClick,
    )
}

private const val SECONDS_PER_MINUTE = 60

@Composable
private fun TimerChipShell(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    contentDescription: String? = null,
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.accentPrimary.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
            .clickable(onClickLabel = contentDescription, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.accentPrimary, modifier = Modifier.size(14.dp))
        Text(label, color = colors.accentPrimary, style = LevelChefTextStyles.captionBold)
    }
}

@Composable
internal fun RelatedVideoRow(onClick: () -> Unit) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
            .background(colors.surface, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
            Text(
                stringResource(R.string.recipe_detail_related_video),
                color = colors.textPrimary,
                style = LevelChefTextStyles.bodySmallBold,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.recipe_detail_open_video),
            tint = colors.textSecondary,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
internal fun ActionButtons(
    xpReward: Int,
    isSaved: Boolean,
    onMadeIt: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    val colors = LevelChefTheme.colors
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, colors.accentPrimary, RoundedCornerShape(12.dp))
                .clickable(onClick = onMadeIt)
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.recipe_detail_made_it),
                color = colors.accentPrimary,
                style = LevelChefTextStyles.bodyRegularBold,
            )
            LevelChefBadge(stringResource(R.string.recipe_detail_xp_badge, xpReward), style = BadgeStyle.LIGHT)
        }
        LevelChefButton(
            label = stringResource(if (isSaved) R.string.recipe_detail_saved else R.string.recipe_detail_save),
            type = if (isSaved) ButtonType.SECONDARY else ButtonType.PRIMARY,
            onClick = onToggleSaved,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun TransientMessage.text(): String = when (this) {
    TransientMessage.SAVED -> stringResource(R.string.recipe_detail_snackbar_saved)
    TransientMessage.UNSAVED -> stringResource(R.string.recipe_detail_snackbar_unsaved)
    TransientMessage.TIMER_DONE -> stringResource(R.string.recipe_detail_snackbar_timer_done)
}
