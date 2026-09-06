package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.IngredientMacros
import com.levelchef.core.model.MeasurementUnit
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
