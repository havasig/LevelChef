package com.levelchef.feature.onboarding

/**
 * The ordered steps of the first-launch survey wizard. [WELCOME] is an intro with no input; the
 * rest are one question each. [entries] order is the on-screen order.
 */
enum class OnboardingStep {
    WELCOME,
    EXPERIENCE,
    DIET,
    ALLERGENS,
    CUISINES,
    SPICE,
    GOAL,
    TIME,
    HOUSEHOLD,
}

/** Number of actual questions (every step except [OnboardingStep.WELCOME]). */
internal const val QUESTION_COUNT: Int = 8
