package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.IngredientMacros
import com.levelchef.core.model.MeasurementUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Exercises the real SQLDelight schema through an in-memory JDBC database. */
class IngredientRepositoryImplTest {

    private lateinit var driver: SqlDriver
    private lateinit var repository: IngredientRepositoryImpl

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(driver)
        repository = IngredientRepositoryImpl(LevelChefDatabase(driver))
    }

    @AfterTest
    fun tearDown() = driver.close()

    private val chicken = Ingredient(
        id = "chicken",
        name = "Chicken breast",
        category = IngredientCategory.MEAT,
        emoji = "🍗",
        defaultUnit = MeasurementUnit.GRAM,
        macros = IngredientMacros(165, 31.0, 0.0, 3.6),
    )

    @Test
    fun saves_and_reads_back_a_full_ingredient() = runTest {
        repository.save(chicken)
        assertEquals(chicken, repository.getById("chicken"))
    }

    @Test
    fun an_ingredient_without_macros_or_unit_round_trips() = runTest {
        val bare = Ingredient("salt", "Salt", IngredientCategory.PANTRY, "🧂")
        repository.save(bare)
        assertEquals(bare, repository.getById("salt"))
    }

    @Test
    fun observe_all_re_emits_on_save_and_delete_ordered_by_name() = runTest {
        repository.observeAll().test {
            assertEquals(emptyList(), awaitItem())

            repository.save(chicken)
            assertEquals(listOf("Chicken breast"), awaitItem().map { it.name })

            repository.save(Ingredient("apple", "Apple", IngredientCategory.FRUIT, "🍎"))
            assertEquals(listOf("Apple", "Chicken breast"), awaitItem().map { it.name })

            repository.delete("apple")
            assertEquals(listOf("Chicken breast"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_overwrites_the_previous_row() = runTest {
        repository.save(chicken)
        repository.save(chicken.copy(name = "Chicken thigh", macros = null))

        val stored = repository.getById("chicken")!!
        assertEquals("Chicken thigh", stored.name)
        assertNull(stored.macros)
    }

    @Test
    fun seed_defaults_populates_an_empty_pantry_once() = runTest {
        repository.seedDefaults()
        val afterFirst = repository.count()
        assertTrue(afterFirst >= 15)

        repository.seedDefaults()
        assertEquals(afterFirst, repository.count())
    }

    @Test
    fun seed_defaults_is_a_no_op_when_the_pantry_is_not_empty() = runTest {
        repository.save(chicken)
        repository.seedDefaults()
        assertEquals(1, repository.count())
    }

    @Test
    fun delete_all_empties_the_pantry() = runTest {
        repository.save(chicken)
        repository.save(Ingredient("apple", "Apple", IngredientCategory.FRUIT, "🍎"))

        repository.deleteAll()

        assertEquals(0, repository.count())
    }

    @Test
    fun seed_defaults_rethrows_when_the_database_is_unavailable() = runTest {
        val closedDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(closedDriver)
        val brokenRepository = IngredientRepositoryImpl(LevelChefDatabase(closedDriver))
        closedDriver.close()

        assertFailsWith<Exception> { brokenRepository.seedDefaults() }
    }

    @Test
    fun seed_defaults_does_not_refill_a_pantry_the_user_emptied() = runTest {
        repository.seedDefaults()
        DEFAULT_INGREDIENTS.forEach { repository.delete(it.id) }

        repository.seedDefaults()

        assertEquals(0, repository.count())
    }

    @Test
    fun delete_all_lets_seed_defaults_run_again() = runTest {
        repository.seedDefaults()
        repository.deleteAll()

        repository.seedDefaults()

        assertEquals(DEFAULT_INGREDIENTS.size, repository.count())
    }

    @Test
    fun seeded_starter_items_read_in_hungarian_when_the_app_language_is_hungarian() = runTest {
        val hungarian = IngredientRepositoryImpl(LevelChefDatabase(driver), languageTag = { "hu" })
        hungarian.seedDefaults()
        assertEquals("Csirkemell", hungarian.getById("chicken-breast")?.name)
        assertTrue(hungarian.observeAll().first().any { it.id == "broccoli" && it.name == "Brokkoli" })
    }

    @Test
    fun seeded_starter_items_keep_their_english_name_in_english() = runTest {
        val english = IngredientRepositoryImpl(LevelChefDatabase(driver), languageTag = { "en" })
        english.seedDefaults()
        assertEquals("Chicken breast", english.getById("chicken-breast")?.name)
    }

    @Test
    fun a_renamed_starter_item_keeps_the_user_name_in_hungarian() = runTest {
        val hungarian = IngredientRepositoryImpl(LevelChefDatabase(driver), languageTag = { "hu" })
        hungarian.seedDefaults()
        val starter = hungarian.getById("chicken-breast")!!
        hungarian.save(starter.copy(name = "Grilled chicken"))
        assertEquals("Grilled chicken", hungarian.getById("chicken-breast")?.name)
    }

    @Test
    fun saving_an_untouched_starter_item_in_hungarian_stores_the_english_seed_name() = runTest {
        val hungarian = IngredientRepositoryImpl(LevelChefDatabase(driver), languageTag = { "hu" })
        hungarian.seedDefaults()
        hungarian.save(hungarian.getById("chicken-breast")!!.copy(emoji = "🐔"))
        assertEquals("Chicken breast", repository.getById("chicken-breast")?.name)
        assertEquals("Csirkemell", hungarian.getById("chicken-breast")?.name)
    }

    @Test
    fun user_added_ingredients_are_never_translated() = runTest {
        val hungarian = IngredientRepositoryImpl(LevelChefDatabase(driver), languageTag = { "hu" })
        hungarian.save(chicken)
        assertEquals("Chicken breast", hungarian.getById("chicken")?.name)
    }
}
