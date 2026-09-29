package com.levelchef.feature.onboarding

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
import com.levelchef.feature.onboarding.generated.resources.Res
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergen_dairy
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergen_eggs
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergen_gluten
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergen_nuts
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergen_shellfish
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergen_soy
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergens_none
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergens_subtitle
import com.levelchef.feature.onboarding.generated.resources.onboarding_allergens_title
import com.levelchef.feature.onboarding.generated.resources.onboarding_back
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisine_american
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisine_asian
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisine_french
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisine_indian
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisine_italian
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisine_mediterranean
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisine_mexican
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisine_middle_eastern
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisines_subtitle
import com.levelchef.feature.onboarding.generated.resources.onboarding_cuisines_title
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_flexitarian
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_flexitarian_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_omnivore
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_omnivore_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_pescatarian
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_pescatarian_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_subtitle
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_title
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_vegan
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_vegan_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_vegetarian
import com.levelchef.feature.onboarding.generated.resources.onboarding_diet_vegetarian_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_beginner
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_beginner_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_comfortable
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_comfortable_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_confident
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_confident_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_never
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_never_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_pro
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_pro_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_subtitle
import com.levelchef.feature.onboarding.generated.resources.onboarding_experience_title
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_budget_friendly
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_budget_friendly_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_healthier_eating
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_healthier_eating_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_high_protein
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_high_protein_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_more_variety
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_more_variety_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_quick_meals
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_quick_meals_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_subtitle
import com.levelchef.feature.onboarding.generated.resources.onboarding_goal_title
import com.levelchef.feature.onboarding.generated.resources.onboarding_household_five_plus
import com.levelchef.feature.onboarding.generated.resources.onboarding_household_solo
import com.levelchef.feature.onboarding.generated.resources.onboarding_household_subtitle
import com.levelchef.feature.onboarding.generated.resources.onboarding_household_three_four
import com.levelchef.feature.onboarding.generated.resources.onboarding_household_title
import com.levelchef.feature.onboarding.generated.resources.onboarding_household_two
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_fire
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_fire_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_hot
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_hot_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_medium
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_medium_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_mild
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_mild_desc
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_subtitle
import com.levelchef.feature.onboarding.generated.resources.onboarding_spice_title
import com.levelchef.feature.onboarding.generated.resources.onboarding_step_counter
import com.levelchef.feature.onboarding.generated.resources.onboarding_time_15_30
import com.levelchef.feature.onboarding.generated.resources.onboarding_time_30_60
import com.levelchef.feature.onboarding.generated.resources.onboarding_time_over_60
import com.levelchef.feature.onboarding.generated.resources.onboarding_time_subtitle
import com.levelchef.feature.onboarding.generated.resources.onboarding_time_title
import com.levelchef.feature.onboarding.generated.resources.onboarding_time_under_15
import com.levelchef.feature.onboarding.generated.resources.onboarding_welcome_body
import com.levelchef.feature.onboarding.generated.resources.onboarding_welcome_tagline
import com.levelchef.feature.onboarding.generated.resources.onboarding_welcome_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Presentation for one option: an emoji plus a label and (optionally) a one-line description. */
internal data class OptionUi(
    val emoji: String,
    val labelRes: StringResource,
    val descRes: StringResource? = null,
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
                    contentDescription = stringResource(Res.string.onboarding_back),
                    style = IconButtonStyle.PLAIN,
                    onClick = onBack,
                )
            }
        }
        LevelChefPageIndicator(pageCount = QUESTION_COUNT, currentPage = questionNumber - 1)
        Text(
            stringResource(Res.string.onboarding_step_counter, questionNumber, QUESTION_COUNT),
            color = LevelChefTheme.colors.tagPurpleText,
            style = LevelChefTextStyles.captionBold,
        )
    }
}

@Composable
private fun QuestionHeader(titleRes: StringResource, subtitleRes: StringResource) {
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
            stringResource(Res.string.onboarding_welcome_title),
            color = LevelChefTheme.colors.textPrimary,
            style = LevelChefTextStyles.h1,
        )
        Text(
            stringResource(Res.string.onboarding_welcome_tagline),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodyRegular,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(Res.string.onboarding_welcome_body),
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
            QuestionHeader(Res.string.onboarding_experience_title, Res.string.onboarding_experience_subtitle)
            CookingExperience.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.cookingExperience == option, false, actions.selectExperience)
            }
        }

        OnboardingStep.DIET -> {
            QuestionHeader(Res.string.onboarding_diet_title, Res.string.onboarding_diet_subtitle)
            DietaryPreference.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.dietaryPreference == option, false, actions.selectDiet)
            }
        }

        OnboardingStep.ALLERGENS -> {
            QuestionHeader(Res.string.onboarding_allergens_title, Res.string.onboarding_allergens_subtitle)
            LevelChefChoiceCard(
                emoji = "🚫",
                title = stringResource(Res.string.onboarding_allergens_none),
                selected = state.noAllergies,
                showCheck = true,
                onClick = actions.noAllergies,
            )
            Allergen.entries.forEach { option ->
                ChoiceCard(option, option.ui(), option in state.allergens, true, actions.toggleAllergen)
            }
        }

        OnboardingStep.CUISINES -> {
            QuestionHeader(Res.string.onboarding_cuisines_title, Res.string.onboarding_cuisines_subtitle)
            Cuisine.entries.forEach { option ->
                ChoiceCard(option, option.ui(), option in state.cuisines, true, actions.toggleCuisine)
            }
        }

        OnboardingStep.SPICE -> {
            QuestionHeader(Res.string.onboarding_spice_title, Res.string.onboarding_spice_subtitle)
            SpiceTolerance.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.spiceTolerance == option, false, actions.selectSpice)
            }
        }

        OnboardingStep.GOAL -> {
            QuestionHeader(Res.string.onboarding_goal_title, Res.string.onboarding_goal_subtitle)
            CookingGoal.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.cookingGoal == option, false, actions.selectGoal)
            }
        }

        OnboardingStep.TIME -> {
            QuestionHeader(Res.string.onboarding_time_title, Res.string.onboarding_time_subtitle)
            WeeknightTime.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.weeknightTime == option, false, actions.selectTime)
            }
        }

        OnboardingStep.HOUSEHOLD -> {
            QuestionHeader(Res.string.onboarding_household_title, Res.string.onboarding_household_subtitle)
            HouseholdSize.entries.forEach { option ->
                ChoiceCard(option, option.ui(), state.householdSize == option, false, actions.selectHousehold)
            }
        }
    }
}

// --- Option presentation (emoji + string resources; the domain enums stay presentation-free) ---

private fun CookingExperience.ui(): OptionUi = when (this) {
    CookingExperience.NEVER_COOKED ->
        OptionUi("🥚", Res.string.onboarding_experience_never, Res.string.onboarding_experience_never_desc)
    CookingExperience.BEGINNER ->
        OptionUi("🌱", Res.string.onboarding_experience_beginner, Res.string.onboarding_experience_beginner_desc)
    CookingExperience.COMFORTABLE ->
        OptionUi("🍳", Res.string.onboarding_experience_comfortable, Res.string.onboarding_experience_comfortable_desc)
    CookingExperience.CONFIDENT ->
        OptionUi("🔥", Res.string.onboarding_experience_confident, Res.string.onboarding_experience_confident_desc)
    CookingExperience.PRO ->
        OptionUi("👨‍🍳", Res.string.onboarding_experience_pro, Res.string.onboarding_experience_pro_desc)
}

private fun DietaryPreference.ui(): OptionUi = when (this) {
    DietaryPreference.OMNIVORE ->
        OptionUi("🍗", Res.string.onboarding_diet_omnivore, Res.string.onboarding_diet_omnivore_desc)
    DietaryPreference.VEGETARIAN ->
        OptionUi("🥦", Res.string.onboarding_diet_vegetarian, Res.string.onboarding_diet_vegetarian_desc)
    DietaryPreference.VEGAN ->
        OptionUi("🌱", Res.string.onboarding_diet_vegan, Res.string.onboarding_diet_vegan_desc)
    DietaryPreference.PESCATARIAN ->
        OptionUi("🐟", Res.string.onboarding_diet_pescatarian, Res.string.onboarding_diet_pescatarian_desc)
    DietaryPreference.FLEXITARIAN ->
        OptionUi("🥗", Res.string.onboarding_diet_flexitarian, Res.string.onboarding_diet_flexitarian_desc)
}

private fun Allergen.ui(): OptionUi = when (this) {
    Allergen.GLUTEN -> OptionUi("🌾", Res.string.onboarding_allergen_gluten)
    Allergen.DAIRY -> OptionUi("🥛", Res.string.onboarding_allergen_dairy)
    Allergen.NUTS -> OptionUi("🥜", Res.string.onboarding_allergen_nuts)
    Allergen.EGGS -> OptionUi("🥚", Res.string.onboarding_allergen_eggs)
    Allergen.SHELLFISH -> OptionUi("🦐", Res.string.onboarding_allergen_shellfish)
    Allergen.SOY -> OptionUi("🌱", Res.string.onboarding_allergen_soy)
}

private fun Cuisine.ui(): OptionUi = when (this) {
    Cuisine.ITALIAN -> OptionUi("🍝", Res.string.onboarding_cuisine_italian)
    Cuisine.ASIAN -> OptionUi("🍜", Res.string.onboarding_cuisine_asian)
    Cuisine.MEXICAN -> OptionUi("🌮", Res.string.onboarding_cuisine_mexican)
    Cuisine.INDIAN -> OptionUi("🍛", Res.string.onboarding_cuisine_indian)
    Cuisine.MEDITERRANEAN -> OptionUi("🥗", Res.string.onboarding_cuisine_mediterranean)
    Cuisine.AMERICAN -> OptionUi("🍔", Res.string.onboarding_cuisine_american)
    Cuisine.MIDDLE_EASTERN -> OptionUi("🧆", Res.string.onboarding_cuisine_middle_eastern)
    Cuisine.FRENCH -> OptionUi("🥐", Res.string.onboarding_cuisine_french)
}

private fun SpiceTolerance.ui(): OptionUi = when (this) {
    SpiceTolerance.MILD -> OptionUi("🥛", Res.string.onboarding_spice_mild, Res.string.onboarding_spice_mild_desc)
    SpiceTolerance.MEDIUM ->
        OptionUi("🌶️", Res.string.onboarding_spice_medium, Res.string.onboarding_spice_medium_desc)
    SpiceTolerance.HOT -> OptionUi("🔥", Res.string.onboarding_spice_hot, Res.string.onboarding_spice_hot_desc)
    SpiceTolerance.FIRE -> OptionUi("💀", Res.string.onboarding_spice_fire, Res.string.onboarding_spice_fire_desc)
}

private fun CookingGoal.ui(): OptionUi = when (this) {
    CookingGoal.MORE_VARIETY ->
        OptionUi("🥗", Res.string.onboarding_goal_more_variety, Res.string.onboarding_goal_more_variety_desc)
    CookingGoal.QUICK_MEALS ->
        OptionUi("⚡", Res.string.onboarding_goal_quick_meals, Res.string.onboarding_goal_quick_meals_desc)
    CookingGoal.HIGH_PROTEIN ->
        OptionUi("💪", Res.string.onboarding_goal_high_protein, Res.string.onboarding_goal_high_protein_desc)
    CookingGoal.HEALTHIER_EATING ->
        OptionUi("🥦", Res.string.onboarding_goal_healthier_eating, Res.string.onboarding_goal_healthier_eating_desc)
    CookingGoal.BUDGET_FRIENDLY ->
        OptionUi("💰", Res.string.onboarding_goal_budget_friendly, Res.string.onboarding_goal_budget_friendly_desc)
}

private fun WeeknightTime.ui(): OptionUi = when (this) {
    WeeknightTime.UNDER_15 -> OptionUi("⏱️", Res.string.onboarding_time_under_15)
    WeeknightTime.FROM_15_TO_30 -> OptionUi("🕓", Res.string.onboarding_time_15_30)
    WeeknightTime.FROM_30_TO_60 -> OptionUi("🕛", Res.string.onboarding_time_30_60)
    WeeknightTime.OVER_60 -> OptionUi("🍽️", Res.string.onboarding_time_over_60)
}

private fun HouseholdSize.ui(): OptionUi = when (this) {
    HouseholdSize.SOLO -> OptionUi("🙋", Res.string.onboarding_household_solo)
    HouseholdSize.TWO -> OptionUi("👫", Res.string.onboarding_household_two)
    HouseholdSize.THREE_TO_FOUR -> OptionUi("👨‍👩‍👧", Res.string.onboarding_household_three_four)
    HouseholdSize.FIVE_PLUS -> OptionUi("👨‍👩‍👧‍👦", Res.string.onboarding_household_five_plus)
}
