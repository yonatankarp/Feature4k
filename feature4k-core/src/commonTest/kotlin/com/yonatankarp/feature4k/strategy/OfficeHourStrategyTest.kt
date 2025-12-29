package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.strategy.FeatureEvaluationContextFixture.featureEvaluationContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.contextWithDateTime
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.CHRISTMAS_2024
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.hoursWithHolidays
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.hoursWithSpecialOpenings
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.splitShiftHours
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.standardWeekdayHours
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for OfficeHourStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class OfficeHourStrategyTest {
    @Test
    fun `should return true during office hours on weekday`() = runTest {
        // Given
        val strategy = standardWeekdayHours()
        val context = contextWithDateTime("2024-12-23T10:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should be enabled during office hours on Monday")
    }

    @Test
    fun `should return false outside office hours on weekday`() = runTest {
        // Given
        val strategy = standardWeekdayHours()
        val context = contextWithDateTime("2024-12-23T18:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should be disabled after office hours on Monday")
    }

    @Test
    fun `should return false on weekend when not configured`() = runTest {
        // Given
        val strategy = standardWeekdayHours()
        val context = contextWithDateTime("2024-12-28T10:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should be disabled on Saturday when weekend not configured")
    }

    @Test
    fun `should return true at start of office hours`() = runTest {
        // Given
        val strategy = standardWeekdayHours()
        val context = contextWithDateTime("2024-12-23T09:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should be enabled at exact start time (inclusive)")
    }

    @Test
    fun `should return false at end of office hours`() = runTest {
        // Given
        val strategy = standardWeekdayHours()
        val context = contextWithDateTime("2024-12-23T17:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should be disabled at exact end time (exclusive)")
    }

    @Test
    fun `should handle split shifts with lunch break`() = runTest {
        // Given
        val strategy = splitShiftHours(DayOfWeek.TUESDAY)
        val morningContext = contextWithDateTime("2024-12-24T10:00:00")
        val lunchContext = contextWithDateTime("2024-12-24T12:30:00")
        val afternoonContext = contextWithDateTime("2024-12-24T14:00:00")

        // When & Then
        assertTrue(strategy.evaluate(featureEvaluationContext(context = morningContext)))
        assertFalse(strategy.evaluate(featureEvaluationContext(context = lunchContext)))
        assertTrue(strategy.evaluate(featureEvaluationContext(context = afternoonContext)))
    }

    @Test
    fun `should handle public holidays`() = runTest {
        // Given
        val weeklySchedule = mapOf(
            DayOfWeek.WEDNESDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
        )
        val strategy = hoursWithHolidays(weeklySchedule, setOf(CHRISTMAS_2024))
        val context = contextWithDateTime("2024-12-25T10:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should be disabled on public holiday even during normal office hours")
    }

    @Test
    fun `should handle special openings override weekly schedule`() = runTest {
        // Given
        val weeklySchedule = mapOf(
            DayOfWeek.WEDNESDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
        )
        val specialOpenings = mapOf(
            CHRISTMAS_2024 to listOf(HourInterval(LocalTime(10, 0), LocalTime(14, 0))),
        )
        val strategy = hoursWithSpecialOpenings(weeklySchedule, specialOpenings)
        val duringSpecialHours = contextWithDateTime("2024-12-25T11:00:00")
        val outsideSpecialHours = contextWithDateTime("2024-12-25T16:00:00")

        // When & Then
        assertTrue(
            strategy.evaluate(featureEvaluationContext(context = duringSpecialHours)),
            "Should be enabled during special opening hours",
        )
        assertFalse(
            strategy.evaluate(featureEvaluationContext(context = outsideSpecialHours)),
            "Should be disabled outside special opening hours",
        )
    }

    @Test
    fun `should prioritize special openings over public holidays`() = runTest {
        // Given
        val weeklySchedule = mapOf(
            DayOfWeek.WEDNESDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
        )
        val strategy = OfficeHourStrategy(
            weeklySchedule = weeklySchedule,
            publicHolidays = setOf(CHRISTMAS_2024),
            specialOpenings = mapOf(
                CHRISTMAS_2024 to listOf(HourInterval(LocalTime(10, 0), LocalTime(14, 0))),
            ),
        )
        val context = contextWithDateTime("2024-12-25T11:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Special opening should override public holiday")
    }

    @Test
    fun `should respect timezone parameter`() = runTest {
        // Given
        val strategy = OfficeHourStrategy(
            weeklySchedule = mapOf(
                DayOfWeek.MONDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
            ),
            timezone = "America/New_York",
        )
        val context = contextWithDateTime("2024-12-23T10:00:00", "America/New_York")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should evaluate correctly with specified timezone")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = standardWeekdayHours()
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"office-hours\""), "Should contain type")
        assertTrue(serialized.contains("\"weeklySchedule\""), "Should contain weeklySchedule")
        assertTrue(serialized.contains("MONDAY"), "Should contain day of week")
        assertTrue(serialized.contains("\"timezone\": \"UTC\""), "Should contain timezone with UTC default")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "office-hours",
                "weeklySchedule": {
                    "MONDAY": [{"from": "09:00", "to": "17:00"}]
                },
                "publicHolidays": [],
                "specialOpenings": {},
                "timezone": "UTC"
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is OfficeHourStrategy, "Should deserialize to OfficeHourStrategy")
        assertTrue(strategy.weeklySchedule.containsKey(DayOfWeek.MONDAY))
        assertEquals(1, strategy.weeklySchedule[DayOfWeek.MONDAY]?.size)
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = standardWeekdayHours()
        val strategy2 = standardWeekdayHours()
        val strategy3 = splitShiftHours()

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same configuration should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different configuration should not be equal")
    }

    @Test
    fun `should return false when no schedule defined for day`() = runTest {
        // Given
        val strategy = OfficeHourStrategy(
            weeklySchedule = mapOf(
                DayOfWeek.MONDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
            ),
        )
        val context = contextWithDateTime("2024-12-24T10:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should return false when day has no schedule defined")
    }

    @Test
    fun `should return false when schedule is empty for day`() = runTest {
        // Given
        val strategy = OfficeHourStrategy(
            weeklySchedule = mapOf(
                DayOfWeek.MONDAY to emptyList(),
            ),
        )
        val context = contextWithDateTime("2024-12-23T10:00:00")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should return false when day has empty schedule")
    }
}
