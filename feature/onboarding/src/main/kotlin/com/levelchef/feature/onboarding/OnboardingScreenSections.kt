package com.levelchef.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levelchef.core.designsystem.IconButtonStyle
import com.levelchef.core.designsystem.LevelChefChoiceCard
import com.levelchef.core.designsystem.LevelChefIconButton
import com.levelchef.core.designsystem.LevelChefPageIndicator
import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.WeeknightTime
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** Presentation for one option: an emoji plus a label and (optionally) a one-line description. */
internal data class OptionUi(
    val emoji: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val descRes: Int? = null,
)

@Composable
internal fun OnboardingHeader(questionNumber: Int, showBack: Boolean, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            if (showBack) {
                LevelChefIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.onboarding_back),
                    style = IconButtonStyle.PLAIN,
                    onClick = onBack,
                )
            }
        }
        LevelChefPageIndicator(pageCount = QUESTION_COUNT, currentPage = questionNumber - 1)
        Text(
            stringResource(R.string.onboarding_step_counter, questionNumber, QUESTION_COUNT),
            color = LevelChefTheme.colors.tagPurpleText,
            style = LevelChefTextStyles.captionBold,
        )
    }
}

@Composable
private fun QuestionHeader(@StringRes titleRes: Int, @StringRes subtitleRes: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(titleRes), color = LevelChefTheme.colors.textPrimary, style = LevelChefTextStyles.h2)
        Text(
            stringResource(subtitleRes),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodySmall,
        )
    }
}

@Composable
internal fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("👨‍🍳", style = LevelChefTextStyles.h1.copy(fontSize = 72.sp))
        Text(
            stringResource(R.string.onboarding_welcome_title),
            color = LevelChefTheme.colors.textPrimary,
            style = LevelChefTextStyles.h1,
        )
        Text(
            stringResource(R.string.onboarding_welcome_tagline),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodyRegular,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.onboarding_welcome_body),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun <T> ChoiceCard(
    option: T,
    ui: OptionUi,
    selected: Boolean,
    multiSelect: Boolean,
    onToggle: (T) -> Unit,
) {
    LevelChefChoiceCard(
        emoji = ui.emoji,
        title = stringResource(ui.labelRes),
        subtitle = ui.descRes?.let { stringResource(it) },
        selected = selected,
        showCheck = multiSelect,
        onClick = { onToggle(option) },
    )
}

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod")
internal fun OnboardingStepContent(state: OnboardingUiState, actions: OnboardingActions) {
    when (state.currentStep) {
        OnboardingStep.WELCOME -> WelcomeStep()

        OnboardingStep.EXPERIENCE -> {
            QuestionHeader(R.string.onboarding_experience_title, R.string.onboarding_experience_subtitle)
            CookingExperience.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.cookingExperience == option, false, actions.selectExperience)
            }
        }

        OnboardingStep.DIET -> {
            QuestionHeader(R.string.onboarding_diet_title, R.string.onboarding_diet_subtitle)
            DietaryPreference.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.dietaryPreference == option, false, actions.selectDiet)
            }
        }

        OnboardingStep.ALLERGENS -> {
            QuestionHeader(R.string.onboarding_allergens_title, R.string.onboarding_allergens_subtitle)
            LevelChefChoiceCard(
                emoji = "🚫",
                title = stringResource(R.string.onboarding_allergens_none),
                selected = state.noAllergies,
                showCheck = true,
                onClick = actions.noAllergies,
            )
            Allergen.entries.forEach { option ->
                ChoiceCard(option, option.ui(), option in state.allergens, true, actions.toggleAllergen)
            }
        }

        OnboardingStep.CUISINES -> {
            QuestionHeader(R.string.onboarding_cuisines_title, R.string.onboarding_cuisines_subtitle)
            Cuisine.entries.forEach { option ->
                ChoiceCard(option, option.ui(), option in state.cuisines, true, actions.toggleCuisine)
            }
        }

        OnboardingStep.SPICE -> {
            QuestionHeader(R.string.onboarding_spice_title, R.string.onboarding_spice_subtitle)
            SpiceTolerance.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.spiceTolerance == option, false, actions.selectSpice)
            }
        }

        OnboardingStep.GOAL -> {
            QuestionHeader(R.string.onboarding_goal_title, R.string.onboarding_goal_subtitle)
            CookingGoal.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.cookingGoal == option, false, actions.selectGoal)
            }
        }

        OnboardingStep.TIME -> {
            QuestionHeader(R.string.onboarding_time_title, R.string.onboarding_time_subtitle)
            WeeknightTime.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.weeknightTime == option, false, actions.selectTime)
            }
        }

        OnboardingStep.HOUSEHOLD -> {
            QuestionHeader(R.string.onboarding_household_title, R.string.onboarding_household_subtitle)
            HouseholdSize.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.householdSize == option, false, actions.selectHousehold)
            }
        }
    }
}

// --- Option presentation (emoji + string resources; the domain enums stay presentation-free) ---

private fun CookingExperience.ui(): OptionUi = when (this) {
    CookingExperience.NEVER_COOKED ->
        OptionUi("🥚", R.string.onboarding_experience_never, R.string.onboarding_experience_never_desc)
    CookingExperience.BEGINNER ->
        OptionUi("🌱", R.string.onboarding_experience_beginner, R.string.onboarding_experience_beginner_desc)
    CookingExperience.COMFORTABLE ->
        OptionUi("🍳", R.string.onboarding_experience_comfortable, R.string.onboarding_experience_comfortable_desc)
    CookingExperience.CONFIDENT ->
        OptionUi("🔥", R.string.onboarding_experience_confident, R.string.onboarding_experience_confident_desc)
    CookingExperience.PRO ->
        OptionUi("👨‍🍳", R.string.onboarding_experience_pro, R.string.onboarding_experience_pro_desc)
}

private fun DietaryPreference.ui(): OptionUi = when (this) {
    DietaryPreference.OMNIVORE ->
        OptionUi("🍗", R.string.onboarding_diet_omnivore, R.string.onboarding_diet_omnivore_desc)
    DietaryPreference.VEGETARIAN ->
        OptionUi("🥦", R.string.onboarding_diet_vegetarian, R.string.onboarding_diet_vegetarian_desc)
    DietaryPreference.VEGAN ->
        OptionUi("🌱", R.string.onboarding_diet_vegan, R.string.onboarding_diet_vegan_desc)
    DietaryPreference.PESCATARIAN ->
        OptionUi("🐟", R.string.onboarding_diet_pescatarian, R.string.onboarding_diet_pescatarian_desc)
    DietaryPreference.FLEXITARIAN ->
        OptionUi("🥗", R.string.onboarding_diet_flexitarian, R.string.onboarding_diet_flexitarian_desc)
}

private fun Allergen.ui(): OptionUi = when (this) {
    Allergen.GLUTEN -> OptionUi("🌾", R.string.onboarding_allergen_gluten)
    Allergen.DAIRY -> OptionUi("🥛", R.string.onboarding_allergen_dairy)
    Allergen.NUTS -> OptionUi("🥜", R.string.onboarding_allergen_nuts)
    Allergen.EGGS -> OptionUi("🥚", R.string.onboarding_allergen_eggs)
    Allergen.SHELLFISH -> OptionUi("🦐", R.string.onboarding_allergen_shellfish)
    Allergen.SOY -> OptionUi("🌱", R.string.onboarding_allergen_soy)
}

private fun Cuisine.ui(): OptionUi = when (this) {
    Cuisine.ITALIAN -> OptionUi("🍝", R.string.onboarding_cuisine_italian)
    Cuisine.ASIAN -> OptionUi("🍜", R.string.onboarding_cuisine_asian)
    Cuisine.MEXICAN -> OptionUi("🌮", R.string.onboarding_cuisine_mexican)
    Cuisine.INDIAN -> OptionUi("🍛", R.string.onboarding_cuisine_indian)
    Cuisine.MEDITERRANEAN -> OptionUi("🥗", R.string.onboarding_cuisine_mediterranean)
    Cuisine.AMERICAN -> OptionUi("🍔", R.string.onboarding_cuisine_american)
    Cuisine.MIDDLE_EASTERN -> OptionUi("🧆", R.string.onboarding_cuisine_middle_eastern)
    Cuisine.FRENCH -> OptionUi("🥐", R.string.onboarding_cuisine_french)
}

private fun SpiceTolerance.ui(): OptionUi = when (this) {
    SpiceTolerance.MILD -> OptionUi("🥛", R.string.onboarding_spice_mild, R.string.onboarding_spice_mild_desc)
    SpiceTolerance.MEDIUM ->
        OptionUi("🌶️", R.string.onboarding_spice_medium, R.string.onboarding_spice_medium_desc)
    SpiceTolerance.HOT -> OptionUi("🔥", R.string.onboarding_spice_hot, R.string.onboarding_spice_hot_desc)
    SpiceTolerance.FIRE -> OptionUi("💀", R.string.onboarding_spice_fire, R.string.onboarding_spice_fire_desc)
}

private fun CookingGoal.ui(): OptionUi = when (this) {
    CookingGoal.MORE_VARIETY ->
        OptionUi("🥗", R.string.onboarding_goal_more_variety, R.string.onboarding_goal_more_variety_desc)
    CookingGoal.QUICK_MEALS ->
        OptionUi("⚡", R.string.onboarding_goal_quick_meals, R.string.onboarding_goal_quick_meals_desc)
    CookingGoal.HIGH_PROTEIN ->
        OptionUi("💪", R.string.onboarding_goal_high_protein, R.string.onboarding_goal_high_protein_desc)
    CookingGoal.HEALTHIER_EATING ->
        OptionUi("🥦", R.string.onboarding_goal_healthier_eating, R.string.onboarding_goal_healthier_eating_desc)
    CookingGoal.BUDGET_FRIENDLY ->
        OptionUi("💰", R.string.onboarding_goal_budget_friendly, R.string.onboarding_goal_budget_friendly_desc)
}

private fun WeeknightTime.ui(): OptionUi = when (this) {
    WeeknightTime.UNDER_15 -> OptionUi("⏱️", R.string.onboarding_time_under_15)
    WeeknightTime.FROM_15_TO_30 -> OptionUi("🕓", R.string.onboarding_time_15_30)
    WeeknightTime.FROM_30_TO_60 -> OptionUi("🕛", R.string.onboarding_time_30_60)
    WeeknightTime.OVER_60 -> OptionUi("🍽️", R.string.onboarding_time_over_60)
}

private fun HouseholdSize.ui(): OptionUi = when (this) {
    HouseholdSize.SOLO -> OptionUi("🙋", R.string.onboarding_household_solo)
    HouseholdSize.TWO -> OptionUi("👫", R.string.onboarding_household_two)
    HouseholdSize.THREE_TO_FOUR -> OptionUi("👨‍👩‍👧", R.string.onboarding_household_three_four)
    HouseholdSize.FIVE_PLUS -> OptionUi("👨‍👩‍👧‍👦", R.string.onboarding_household_five_plus)
}
