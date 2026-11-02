package com.levelchef.data.recipe

import com.levelchef.core.model.Allergen
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.RecipeIngredient
import com.levelchef.core.model.RecipeStep
import com.levelchef.core.model.SurveyResponse

/** Which diets a bundled recipe suits and which allergens it (possibly) contains. */
private class Suitability(val diets: Set<DietaryPreference>, val allergens: Set<Allergen> = emptySet())

private val meatDiets = setOf(DietaryPreference.OMNIVORE, DietaryPreference.FLEXITARIAN)
private val allDiets = DietaryPreference.entries.toSet()

/** Conservative on purpose — e.g. yellow curry paste often contains shrimp paste. Every id in
 * [bundledRecipesEn]/[bundledRecipesHu] must be listed here; a missing id counts as unsuitable. */
private val suitability = mapOf(
    "chicken-curry" to Suitability(meatDiets, setOf(Allergen.SHELLFISH)),
    "steak-quinoa-bowl" to Suitability(meatDiets),
    "juicy-pasta" to Suitability(allDiets, setOf(Allergen.GLUTEN)),
    "chickpea-spinach-stew" to Suitability(allDiets),
    "roasted-vegetable-tray-bake" to Suitability(allDiets),
)

/**
 * The small bundled recipe set served when there's no live recommendation (no proxy URL, no survey
 * yet, or the proxy/Gemini failed) — so Home never shows an empty "Recommended for you". With a
 * [survey], only recipes that match its dietary preference and contain none of its allergens are
 * returned; the vegan, allergen-free ones guarantee that is never empty. [languageTag] picks the
 * Hungarian set for "hu", else English.
 */
internal fun bundledRecipes(languageTag: String?, survey: SurveyResponse? = null): List<Recipe> {
    val all = if (contentLanguage(languageTag) == "hu") bundledRecipesHu else bundledRecipesEn
    if (survey == null) return all
    return all.filter { recipe ->
        val fit = suitability[recipe.id] ?: return@filter false
        survey.dietaryPreference in fit.diets && fit.allergens.none { it in survey.allergens }
    }
}

/** Resolves any bundled recipe by id, unfiltered — a recipe saved or cooked earlier must stay
 * openable even after the survey changes. */
internal fun bundledRecipeById(id: String, languageTag: String?): Recipe? =
    bundledRecipes(languageTag).find { it.id == id }

private val bundledRecipesEn = listOf(
    Recipe(
        id = "chicken-curry",
        name = "Chicken curry with coconut milk",
        emoji = "🍲",
        xpReward = 45,
        timeMinutes = 25,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 520,
        proteinGrams = 38,
        carbsGrams = 18,
        fatGrams = 32,
        tags = listOf("Comfort", "One pot"),
        ingredients = listOf(
            RecipeIngredient("chicken thigh", quantity = 300.0, unit = "g"),
            RecipeIngredient("coconut milk", quantity = 400.0, unit = "ml"),
            RecipeIngredient("yellow curry paste", quantity = 2.0, unit = "tbsp"),
            RecipeIngredient("onion", quantity = 1.0),
            RecipeIngredient("Cilantro, lime, salt"),
        ),
        steps = listOf(
            RecipeStep("Dice the chicken and slice the onion."),
            RecipeStep("Fry the curry paste with the onion until fragrant."),
            RecipeStep("Add the chicken and sear on all sides."),
            RecipeStep("Pour in the coconut milk and simmer.", timerMinutes = 15),
            RecipeStep("Finish with lime juice and fresh cilantro."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=chicken+curry+coconut+milk",
    ),
    Recipe(
        id = "steak-quinoa-bowl",
        name = "Steak quinoa bowl",
        emoji = "🥩",
        xpReward = 120,
        timeMinutes = 35,
        difficulty = Difficulty.MEDIUM,
        servings = 2,
        caloriesKcal = 610,
        proteinGrams = 46,
        carbsGrams = 52,
        fatGrams = 24,
        tags = listOf("High protein", "Meal prep"),
        ingredients = listOf(
            RecipeIngredient("flank steak", quantity = 250.0, unit = "g"),
            RecipeIngredient("quinoa", quantity = 150.0, unit = "g"),
            RecipeIngredient("cherry tomatoes", quantity = 100.0, unit = "g"),
            RecipeIngredient("avocado", quantity = 1.0),
            RecipeIngredient("Olive oil, lemon, salt, pepper"),
        ),
        steps = listOf(
            RecipeStep("Rinse the quinoa, then simmer in salted water.", timerMinutes = 15),
            RecipeStep("Season the steak generously with salt and pepper."),
            RecipeStep("Sear the steak 3-4 minutes per side, then rest.", timerMinutes = 5),
            RecipeStep("Halve the tomatoes and slice the avocado."),
            RecipeStep("Slice the steak against the grain and build the bowl."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=steak+quinoa+bowl",
    ),
    Recipe(
        id = "juicy-pasta",
        name = "Juicy pasta",
        emoji = "🍝",
        xpReward = 80,
        timeMinutes = 14,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 480,
        proteinGrams = 17,
        carbsGrams = 72,
        fatGrams = 14,
        tags = listOf("Quick", "Vegetarian"),
        ingredients = listOf(
            RecipeIngredient("spaghetti", quantity = 200.0, unit = "g"),
            RecipeIngredient("canned tomatoes", quantity = 400.0, unit = "g"),
            RecipeIngredient("garlic", quantity = 2.0, unit = "cloves"),
            RecipeIngredient("Basil, olive oil, salt", isNewToUser = true),
        ),
        steps = listOf(
            RecipeStep("Boil the spaghetti in well-salted water.", timerMinutes = 9),
            RecipeStep("Gently fry the sliced garlic in olive oil."),
            RecipeStep("Add the tomatoes and simmer until thickened."),
            RecipeStep("Toss the drained pasta through the sauce with basil."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=easy+tomato+pasta",
    ),
    Recipe(
        id = "chickpea-spinach-stew",
        name = "Chickpea and spinach stew with rice",
        emoji = "🥘",
        xpReward = 60,
        timeMinutes = 30,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 540,
        proteinGrams = 19,
        carbsGrams = 88,
        fatGrams = 12,
        tags = listOf("Vegan", "One pot"),
        ingredients = listOf(
            RecipeIngredient("rice", quantity = 150.0, unit = "g"),
            RecipeIngredient("cooked chickpeas", quantity = 400.0, unit = "g"),
            RecipeIngredient("canned tomatoes", quantity = 400.0, unit = "g"),
            RecipeIngredient("fresh spinach", quantity = 100.0, unit = "g"),
            RecipeIngredient("onion", quantity = 1.0),
            RecipeIngredient("Garlic, ground cumin, olive oil, salt"),
        ),
        steps = listOf(
            RecipeStep("Cook the rice in salted water.", timerMinutes = 15),
            RecipeStep("Soften the chopped onion and garlic in olive oil."),
            RecipeStep("Stir in the cumin, then the tomatoes and chickpeas."),
            RecipeStep("Simmer until slightly thickened.", timerMinutes = 10),
            RecipeStep("Wilt the spinach through the stew and serve over the rice."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=chickpea+spinach+stew",
    ),
    Recipe(
        id = "roasted-vegetable-tray-bake",
        name = "Roasted potato and vegetable tray bake",
        emoji = "🥔",
        xpReward = 50,
        timeMinutes = 45,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 430,
        proteinGrams = 9,
        carbsGrams = 62,
        fatGrams = 16,
        tags = listOf("Vegan", "Sheet pan"),
        ingredients = listOf(
            RecipeIngredient("potatoes", quantity = 500.0, unit = "g"),
            RecipeIngredient("bell pepper", quantity = 2.0),
            RecipeIngredient("zucchini", quantity = 1.0),
            RecipeIngredient("red onion", quantity = 1.0),
            RecipeIngredient("Olive oil, paprika, rosemary, salt"),
        ),
        steps = listOf(
            RecipeStep("Heat the oven to 220 °C."),
            RecipeStep("Cut the potatoes and vegetables into bite-sized pieces."),
            RecipeStep("Toss everything with olive oil, paprika, rosemary and salt."),
            RecipeStep("Roast on a tray until golden, turning once.", timerMinutes = 35),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=roasted+vegetable+tray+bake",
    ),
)

/** Hungarian translation of [bundledRecipesEn] — same `id`s and numeric fields (macros, time,
 * difficulty, servings, xpReward) so a recipe saved/cooked under one language still resolves by
 * id under the other; only the display text is translated. */
private val bundledRecipesHu = listOf(
    Recipe(
        id = "chicken-curry",
        name = "Csirke curry kókusztejjel",
        emoji = "🍲",
        xpReward = 45,
        timeMinutes = 25,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 520,
        proteinGrams = 38,
        carbsGrams = 18,
        fatGrams = 32,
        tags = listOf("Laktató", "Egytálétel"),
        ingredients = listOf(
            RecipeIngredient("csirkecomb", quantity = 300.0, unit = "g"),
            RecipeIngredient("kókusztej", quantity = 400.0, unit = "ml"),
            RecipeIngredient("sárga curry paszta", quantity = 2.0, unit = "evőkanál"),
            RecipeIngredient("vöröshagyma", quantity = 1.0),
            RecipeIngredient("Koriander, lime, só"),
        ),
        steps = listOf(
            RecipeStep("Kockázd fel a csirkét, és szeleteld fel a hagymát."),
            RecipeStep("Pirítsd meg a curry pasztát a hagymával, amíg illatos nem lesz."),
            RecipeStep("Add hozzá a csirkét, és süsd körbe minden oldalról."),
            RecipeStep("Öntsd hozzá a kókusztejet, és főzd lassú tűzön.", timerMinutes = 15),
            RecipeStep("Fejezd be lime levével és friss korianderrel."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=csirke+curry+kokusztejjel",
    ),
    Recipe(
        id = "steak-quinoa-bowl",
        name = "Steak quinoa tál",
        emoji = "🥩",
        xpReward = 120,
        timeMinutes = 35,
        difficulty = Difficulty.MEDIUM,
        servings = 2,
        caloriesKcal = 610,
        proteinGrams = 46,
        carbsGrams = 52,
        fatGrams = 24,
        tags = listOf("Fehérjedús", "Előre elkészíthető"),
        ingredients = listOf(
            RecipeIngredient("marha steak", quantity = 250.0, unit = "g"),
            RecipeIngredient("quinoa", quantity = 150.0, unit = "g"),
            RecipeIngredient("koktélparadicsom", quantity = 100.0, unit = "g"),
            RecipeIngredient("avokádó", quantity = 1.0),
            RecipeIngredient("Olívaolaj, citrom, só, bors"),
        ),
        steps = listOf(
            RecipeStep("Öblítsd le a quinoát, majd főzd meg sós vízben.", timerMinutes = 15),
            RecipeStep("Fűszerezd bőségesen sóval és borssal a steaket."),
            RecipeStep("Süsd a steaket 3-4 percig oldalanként, majd pihentesd.", timerMinutes = 5),
            RecipeStep("Vágd félbe a paradicsomokat, és szeleteld fel az avokádót."),
            RecipeStep("Szeleteld fel a steaket a rostokkal szemben, és állítsd össze a tálat."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=steak+quinoa+tal",
    ),
    Recipe(
        id = "juicy-pasta",
        name = "Szószos tészta",
        emoji = "🍝",
        xpReward = 80,
        timeMinutes = 14,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 480,
        proteinGrams = 17,
        carbsGrams = 72,
        fatGrams = 14,
        tags = listOf("Gyors", "Vegetáriánus"),
        ingredients = listOf(
            RecipeIngredient("spagetti", quantity = 200.0, unit = "g"),
            RecipeIngredient("konzerv paradicsom", quantity = 400.0, unit = "g"),
            RecipeIngredient("fokhagyma", quantity = 2.0, unit = "gerezd"),
            RecipeIngredient("Bazsalikom, olívaolaj, só", isNewToUser = true),
        ),
        steps = listOf(
            RecipeStep("Főzd meg a spagettit bőséges sós vízben.", timerMinutes = 9),
            RecipeStep("Pirítsd meg enyhén a szeletelt fokhagymát olívaolajon."),
            RecipeStep("Add hozzá a paradicsomot, és főzd, amíg besűrűsödik."),
            RecipeStep("Forgasd össze a leszűrt tésztát a szósszal és a bazsalikommal."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=szoszos+teszta",
    ),
    Recipe(
        id = "chickpea-spinach-stew",
        name = "Csicseriborsós-spenótos ragu rizzsel",
        emoji = "🥘",
        xpReward = 60,
        timeMinutes = 30,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 540,
        proteinGrams = 19,
        carbsGrams = 88,
        fatGrams = 12,
        tags = listOf("Vegán", "Egytálétel"),
        ingredients = listOf(
            RecipeIngredient("rizs", quantity = 150.0, unit = "g"),
            RecipeIngredient("főtt csicseriborsó", quantity = 400.0, unit = "g"),
            RecipeIngredient("konzerv paradicsom", quantity = 400.0, unit = "g"),
            RecipeIngredient("friss spenót", quantity = 100.0, unit = "g"),
            RecipeIngredient("vöröshagyma", quantity = 1.0),
            RecipeIngredient("Fokhagyma, őrölt kömény, olívaolaj, só"),
        ),
        steps = listOf(
            RecipeStep("Főzd meg a rizst sós vízben.", timerMinutes = 15),
            RecipeStep("Párold üvegesre az aprított hagymát és fokhagymát olívaolajon."),
            RecipeStep("Keverd hozzá a köményt, majd a paradicsomot és a csicseriborsót."),
            RecipeStep("Főzd lassú tűzön, amíg kicsit besűrűsödik.", timerMinutes = 10),
            RecipeStep("Forgasd bele a spenótot, és tálald a rizzsel."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=csicseriborso+spenot+ragu",
    ),
    Recipe(
        id = "roasted-vegetable-tray-bake",
        name = "Tepsis sült krumpli zöldségekkel",
        emoji = "🥔",
        xpReward = 50,
        timeMinutes = 45,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 430,
        proteinGrams = 9,
        carbsGrams = 62,
        fatGrams = 16,
        tags = listOf("Vegán", "Tepsis"),
        ingredients = listOf(
            RecipeIngredient("burgonya", quantity = 500.0, unit = "g"),
            RecipeIngredient("kaliforniai paprika", quantity = 2.0),
            RecipeIngredient("cukkini", quantity = 1.0),
            RecipeIngredient("lilahagyma", quantity = 1.0),
            RecipeIngredient("Olívaolaj, pirospaprika, rozmaring, só"),
        ),
        steps = listOf(
            RecipeStep("Melegítsd elő a sütőt 220 °C-ra."),
            RecipeStep("Vágd falatnyi darabokra a krumplit és a zöldségeket."),
            RecipeStep("Forgasd össze mindent olívaolajjal, pirospaprikával, rozmaringgal és sóval."),
            RecipeStep("Süsd tepsiben aranybarnára, egyszer megforgatva.", timerMinutes = 35),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=tepsis+sult+zoldseg",
    ),
)
