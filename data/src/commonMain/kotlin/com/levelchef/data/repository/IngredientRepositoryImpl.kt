package com.levelchef.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import com.levelchef.core.database.db.IngredientQueries
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.IngredientMacros
import com.levelchef.core.model.MeasurementUnit
import com.levelchef.domain.repository.IngredientRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** SQLDelight-backed [IngredientRepository]. Enum columns store the enum `name`; macros are null-all-four.
 * Blocking SQLite calls run on [dispatcher] — `Dispatchers.IO` on Android (see `databaseModule`).
 * Seeded starter items are shown in the current app [languageTag] (see [localized]) until the user renames them. */
class IngredientRepositoryImpl(
    private val database: LevelChefDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val languageTag: () -> String? = { null },
) : IngredientRepository {

    private val queries: IngredientQueries get() = database.ingredientQueries

    override fun observeAll(): Flow<List<Ingredient>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain().localized(languageTag()) } }

    override suspend fun getById(id: String): Ingredient? = withContext(dispatcher) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()?.localized(languageTag())
    }

    override suspend fun save(ingredient: Ingredient) {
        withContext(dispatcher) { queries.upsert(ingredient.delocalized()) }
    }

    override suspend fun delete(id: String) {
        withContext(dispatcher) { queries.deleteById(id) }
    }

    override suspend fun deleteAll() {
        withContext(dispatcher) {
            queries.transaction {
                queries.deleteAll()
                queries.clearSeeded()
            }
        }
    }

    override suspend fun count(): Int = withContext(dispatcher) {
        queries.countAll().executeAsOne().toInt()
    }

    // A seed failure must surface regardless of the SQLDelight exception type; it is rethrown so the
    // caller's CoroutineExceptionHandler still sees it.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun seedDefaults() {
        try {
            withContext(dispatcher) {
                if (queries.isSeeded().executeAsOne()) return@withContext
                queries.transaction {
                    // Never add defaults on top of a pantry that already has rows; just record the seed.
                    if (queries.countAll().executeAsOne() == 0L) DEFAULT_INGREDIENTS.forEach(queries::upsert)
                    queries.markSeeded()
                }
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to seed default ingredients" }
            throw e
        }
    }
}

private fun IngredientQueries.upsert(ingredient: Ingredient) = upsert(
    id = ingredient.id,
    name = ingredient.name,
    category = ingredient.category.name,
    emoji = ingredient.emoji,
    defaultUnit = ingredient.defaultUnit?.name,
    calories = ingredient.macros?.calories?.toLong(),
    proteinGrams = ingredient.macros?.proteinGrams,
    carbsGrams = ingredient.macros?.carbsGrams,
    fatGrams = ingredient.macros?.fatGrams,
    imageUrl = ingredient.imageUrl,
)

private fun com.levelchef.core.database.db.Ingredient.toDomain(): Ingredient = Ingredient(
    id = id,
    name = name,
    category = enumValueOf<IngredientCategory>(category),
    emoji = emoji,
    defaultUnit = defaultUnit?.let { enumValueOf<MeasurementUnit>(it) },
    macros = calories?.let {
        IngredientMacros(
            calories = it.toInt(),
            proteinGrams = proteinGrams ?: 0.0,
            carbsGrams = carbsGrams ?: 0.0,
            fatGrams = fatGrams ?: 0.0,
        )
    },
    imageUrl = imageUrl,
)

private fun default(
    id: String,
    name: String,
    category: IngredientCategory,
    emoji: String,
    unit: MeasurementUnit,
    calories: Int,
    protein: Double,
    carbs: Double,
    fat: Double,
) = Ingredient(
    id = id,
    name = name,
    category = category,
    emoji = emoji,
    defaultUnit = unit,
    macros = IngredientMacros(calories, protein, carbs, fat),
)

/** The ingredients seeded on first launch — echoes the Figma "Used Ingredients" screen. */
internal val DEFAULT_INGREDIENTS: List<Ingredient> = listOf(
    default("chicken-breast", "Chicken breast", IngredientCategory.MEAT, "🍗", MeasurementUnit.GRAM, 165, 31.0, 0.0, 3.6),
    default("beef-brisket", "Beef brisket", IngredientCategory.MEAT, "🥩", MeasurementUnit.GRAM, 250, 27.0, 0.0, 15.0),
    default("turkey-breast", "Turkey breast", IngredientCategory.MEAT, "🦃", MeasurementUnit.GRAM, 135, 30.0, 0.0, 1.0),
    default("salmon-fillet", "Salmon fillet", IngredientCategory.MEAT, "🐟", MeasurementUnit.GRAM, 208, 20.0, 0.0, 13.0),
    default("greek-yogurt", "Greek yogurt", IngredientCategory.DAIRY, "🥛", MeasurementUnit.MILLILITER, 59, 10.0, 3.6, 0.4),
    default("cottage-cheese", "Cottage cheese", IngredientCategory.DAIRY, "🧀", MeasurementUnit.GRAM, 98, 11.0, 3.4, 4.3),
    default("parmesan", "Parmesan", IngredientCategory.DAIRY, "🧀", MeasurementUnit.GRAM, 431, 38.0, 4.1, 29.0),
    default("mozzarella", "Mozzarella", IngredientCategory.DAIRY, "🧀", MeasurementUnit.GRAM, 280, 28.0, 3.1, 17.0),
    default("broccoli", "Broccoli", IngredientCategory.VEGETABLE, "🥦", MeasurementUnit.GRAM, 34, 2.8, 7.0, 0.4),
    default("avocado", "Avocado", IngredientCategory.VEGETABLE, "🥑", MeasurementUnit.PIECE, 160, 2.0, 9.0, 15.0),
    default("spinach", "Spinach", IngredientCategory.VEGETABLE, "🥬", MeasurementUnit.GRAM, 23, 2.9, 3.6, 0.4),
    default("bell-pepper", "Bell pepper", IngredientCategory.VEGETABLE, "🫑", MeasurementUnit.PIECE, 31, 1.0, 6.0, 0.3),
    default("zucchini", "Zucchini", IngredientCategory.VEGETABLE, "🥒", MeasurementUnit.PIECE, 17, 1.2, 3.1, 0.3),
    default("eggplant", "Eggplant", IngredientCategory.VEGETABLE, "🍆", MeasurementUnit.PIECE, 25, 1.0, 6.0, 0.2),
    default("lemon", "Lemon", IngredientCategory.FRUIT, "🍋", MeasurementUnit.PIECE, 29, 1.1, 9.0, 0.3),
    default("apple", "Apple", IngredientCategory.FRUIT, "🍎", MeasurementUnit.PIECE, 52, 0.3, 14.0, 0.2),
    default("banana", "Banana", IngredientCategory.FRUIT, "🍌", MeasurementUnit.PIECE, 89, 1.1, 23.0, 0.3),
    default("strawberry", "Strawberry", IngredientCategory.FRUIT, "🍓", MeasurementUnit.GRAM, 32, 0.7, 7.7, 0.3),
)

/** Ids of [DEFAULT_INGREDIENTS]: the starter pantry, which isn't something the user tried. */
internal val DEFAULT_INGREDIENT_IDS: Set<String> = DEFAULT_INGREDIENTS.mapTo(mutableSetOf()) { it.id }

/** The pantry minus the seeded starter items — what "ingredients tried" and the pantry badges count. */
internal fun List<Ingredient>.userAdded(): List<Ingredient> = filterNot { it.id in DEFAULT_INGREDIENT_IDS }

/** Hungarian names for [DEFAULT_INGREDIENTS], keyed by id. English is the seeded name itself. */
private val DEFAULT_INGREDIENT_NAMES_HU: Map<String, String> = mapOf(
    "chicken-breast" to "Csirkemell",
    "beef-brisket" to "Marhaszegy",
    "turkey-breast" to "Pulykamell",
    "salmon-fillet" to "Lazacfilé",
    "greek-yogurt" to "Görög joghurt",
    "cottage-cheese" to "Cottage cheese",
    "parmesan" to "Parmezán",
    "mozzarella" to "Mozzarella",
    "broccoli" to "Brokkoli",
    "avocado" to "Avokádó",
    "spinach" to "Spenót",
    "bell-pepper" to "Kaliforniai paprika",
    "zucchini" to "Cukkini",
    "eggplant" to "Padlizsán",
    "lemon" to "Citrom",
    "apple" to "Alma",
    "banana" to "Banán",
    "strawberry" to "Eper",
)

private val DEFAULT_INGREDIENT_NAMES_EN: Map<String, String> = DEFAULT_INGREDIENTS.associate { it.id to it.name }

/**
 * A starter item whose stored name is still the seeded English one is shown in the app language
 * ([languageTag] `"hu"` → Hungarian, anything else → as stored). A renamed starter item keeps the
 * user's name.
 */
internal fun Ingredient.localized(languageTag: String?): Ingredient {
    if (languageTag != "hu" || name != DEFAULT_INGREDIENT_NAMES_EN[id]) return this
    return DEFAULT_INGREDIENT_NAMES_HU[id]?.let { copy(name = it) } ?: this
}

/** Reverses [localized] on save, so editing an untouched starter item in Hungarian (e.g. only its
 * macros) doesn't pin its Hungarian name when the app is switched back to English. */
internal fun Ingredient.delocalized(): Ingredient {
    if (name != DEFAULT_INGREDIENT_NAMES_HU[id]) return this
    return DEFAULT_INGREDIENT_NAMES_EN[id]?.let { copy(name = it) } ?: this
}
