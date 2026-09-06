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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** SQLDelight-backed [IngredientRepository]. Enum columns store the enum `name`; macros are null-all-four. */
class IngredientRepositoryImpl(
    private val database: LevelChefDatabase,
) : IngredientRepository {

    private val queries: IngredientQueries get() = database.ingredientQueries

    override fun observeAll(): Flow<List<Ingredient>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: String): Ingredient? =
        queries.selectById(id).executeAsOneOrNull()?.toDomain()

    override suspend fun save(ingredient: Ingredient) {
        queries.upsert(ingredient)
    }

    override suspend fun delete(id: String) {
        queries.deleteById(id)
    }

    override suspend fun deleteAll() {
        queries.deleteAll()
    }

    override suspend fun count(): Int = queries.countAll().executeAsOne().toInt()

    // A seed failure must surface regardless of the SQLDelight exception type; it is rethrown so the
    // caller's CoroutineExceptionHandler still sees it.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun seedDefaults() {
        try {
            if (queries.countAll().executeAsOne() != 0L) return
            queries.transaction {
                DEFAULT_INGREDIENTS.forEach(queries::upsert)
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
    default("bell-pepper", "Bell pepper", IngredientCategory.VEGETABLE, "🌶️", MeasurementUnit.PIECE, 31, 1.0, 6.0, 0.3),
    default("zucchini", "Zucchini", IngredientCategory.VEGETABLE, "🥒", MeasurementUnit.PIECE, 17, 1.2, 3.1, 0.3),
    default("eggplant", "Eggplant", IngredientCategory.VEGETABLE, "🍆", MeasurementUnit.PIECE, 25, 1.0, 6.0, 0.2),
    default("lemon", "Lemon", IngredientCategory.FRUIT, "🍋", MeasurementUnit.PIECE, 29, 1.1, 9.0, 0.3),
    default("apple", "Apple", IngredientCategory.FRUIT, "🍎", MeasurementUnit.PIECE, 52, 0.3, 14.0, 0.2),
    default("banana", "Banana", IngredientCategory.FRUIT, "🍌", MeasurementUnit.PIECE, 89, 1.1, 23.0, 0.3),
    default("strawberry", "Strawberry", IngredientCategory.FRUIT, "🍓", MeasurementUnit.GRAM, 32, 0.7, 7.7, 0.3),
)
