@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.SurveyResponse
import com.levelchef.core.model.WeeknightTime
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Exercises the real SQLDelight schema through an in-memory JDBC database. */
class SurveyResponseRepositoryImplTest {

    private lateinit var driver: SqlDriver
    private lateinit var repository: SurveyResponseRepositoryImpl

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(driver)
        repository = SurveyResponseRepositoryImpl(LevelChefDatabase(driver))
    }

    @AfterTest
    fun tearDown() = driver.close()

    private val response = SurveyResponse(
        completedAt = Instant.parse("2026-02-01T12:00:00Z"),
        cookingExperience = CookingExperience.CONFIDENT,
        dietaryPreference = DietaryPreference.VEGETARIAN,
        allergens = setOf(Allergen.NUTS, Allergen.SOY),
        cuisines = setOf(Cuisine.ASIAN, Cuisine.MEXICAN, Cuisine.FRENCH),
        spiceTolerance = SpiceTolerance.MEDIUM,
        cookingGoal = CookingGoal.MORE_VARIETY,
        weeknightTime = WeeknightTime.FROM_30_TO_60,
        householdSize = HouseholdSize.THREE_TO_FOUR,
    )

    @Test
    fun saves_and_reads_back_a_full_response() = runTest {
        repository.save(response)

        repository.observeResponse().test {
            assertEquals(response, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observe_emits_null_until_a_response_is_saved_then_re_emits() = runTest {
        repository.observeResponse().test {
            assertNull(awaitItem())

            repository.save(response)

            assertEquals(response, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun an_empty_allergen_set_round_trips() = runTest {
        repository.save(response.copy(allergens = emptySet()))

        repository.observeResponse().test {
            assertEquals(emptySet(), awaitItem()?.allergens)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_overwrites_the_previous_response() = runTest {
        repository.save(response)
        repository.save(response.copy(spiceTolerance = SpiceTolerance.FIRE, householdSize = HouseholdSize.SOLO))

        repository.observeResponse().test {
            val latest = awaitItem()!!
            assertEquals(SpiceTolerance.FIRE, latest.spiceTolerance)
            assertEquals(HouseholdSize.SOLO, latest.householdSize)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
