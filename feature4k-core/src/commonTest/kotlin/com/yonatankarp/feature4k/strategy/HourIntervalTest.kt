package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.STANDARD_BUSINESS_HOURS
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for HourInterval.
 *
 * @author Yonatan Karp-Rudin
 */
class HourIntervalTest {
    @Test
    fun `should parse valid hour interval expression`() {
        // Given
        val expression = STANDARD_BUSINESS_HOURS

        // When
        val interval = HourInterval(expression)

        // Then
        assertEquals(LocalTime(9, 0), interval.from)
        assertEquals(LocalTime(17, 0), interval.to)
    }

    @Test
    fun `should parse interval with spaces`() {
        // Given
        val expression = " 08:30 - 12:45 "

        // When
        val interval = HourInterval(expression)

        // Then
        assertEquals(LocalTime(8, 30), interval.from)
        assertEquals(LocalTime(12, 45), interval.to)
    }

    @Test
    fun `should normalize reversed time bounds when overnight intervals are not supported`() {
        // Given
        val expression = "17:00-09:00"

        // When
        val interval = HourInterval(expression)

        // Then
        assertEquals(LocalTime(9, 0), interval.from)
        assertEquals(LocalTime(17, 0), interval.to)
    }

    @Test
    fun `should match time within interval`() {
        // Given
        val interval = HourInterval(STANDARD_BUSINESS_HOURS)

        // When & Then
        assertTrue(interval.matches(LocalTime(9, 0)), "Should match start time (inclusive)")
        assertTrue(interval.matches(LocalTime(12, 0)), "Should match midpoint")
        assertTrue(interval.matches(LocalTime(16, 59)), "Should match just before end")
    }

    @Test
    fun `should not match time at end boundary`() {
        // Given
        val interval = HourInterval(STANDARD_BUSINESS_HOURS)

        // When & Then
        assertFalse(interval.matches(LocalTime(17, 0)), "Should not match end time (exclusive)")
    }

    @Test
    fun `should not match time before interval`() {
        // Given
        val interval = HourInterval(STANDARD_BUSINESS_HOURS)

        // When & Then
        assertFalse(interval.matches(LocalTime(8, 59)), "Should not match before start")
        assertFalse(interval.matches(LocalTime(0, 0)), "Should not match midnight")
    }

    @Test
    fun `should not match time after interval`() {
        // Given
        val interval = HourInterval(STANDARD_BUSINESS_HOURS)

        // When & Then
        assertFalse(interval.matches(LocalTime(17, 1)), "Should not match after end")
        assertFalse(interval.matches(LocalTime(23, 59)), "Should not match late evening")
    }

    @Test
    fun `should handle minute precision`() {
        // Given
        val interval = HourInterval("08:30-12:45")

        // When & Then
        assertTrue(interval.matches(LocalTime(8, 30)), "Should match 08:30")
        assertTrue(interval.matches(LocalTime(12, 44)), "Should match 12:44")
        assertFalse(interval.matches(LocalTime(8, 29)), "Should not match 08:29")
        assertFalse(interval.matches(LocalTime(12, 45)), "Should not match 12:45 (exclusive)")
    }

    @Test
    fun `should handle single minute interval`() {
        // Given
        val interval = HourInterval("10:00-10:01")

        // When & Then
        assertTrue(interval.matches(LocalTime(10, 0)), "Should match 10:00")
        assertFalse(interval.matches(LocalTime(10, 1)), "Should not match 10:01")
        assertFalse(interval.matches(LocalTime(9, 59)), "Should not match 9:59")
    }

    @Test
    fun `should fail on empty expression`() {
        // Given
        val expression = ""

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            HourInterval(expression)
        }
    }

    @Test
    fun `should fail on expression with no dash`() {
        // Given
        val expression = "09:00"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            HourInterval(expression)
        }
    }

    @Test
    fun `should fail on expression with multiple dashes`() {
        // Given
        val expression = "09:00-12:00-15:00"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            HourInterval(expression)
        }
    }

    @Test
    fun `should fail on invalid time format`() {
        // Given
        val expression = "9:00-17:00"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            HourInterval(expression)
        }
    }

    @Test
    fun `should fail on invalid hour`() {
        // Given
        val expression = "25:00-17:00"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            HourInterval(expression)
        }
    }

    @Test
    fun `should fail on invalid minute`() {
        // Given
        val expression = "09:60-17:00"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            HourInterval(expression)
        }
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val interval = HourInterval(STANDARD_BUSINESS_HOURS)

        // When
        val serialized = Json.encodeToString(interval)

        // Then
        assertTrue(serialized.contains("\"from\""))
        assertTrue(serialized.contains("\"to\""))
        assertTrue(serialized.contains("09:00"))
        assertTrue(serialized.contains("17:00"))
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "from": "09:00",
                "to": "17:00"
            }
        """.trimIndent()

        // When
        val interval = Json.decodeFromString<HourInterval>(jsonString)

        // Then
        assertEquals(LocalTime(9, 0), interval.from)
        assertEquals(LocalTime(17, 0), interval.to)
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val interval1 = HourInterval(STANDARD_BUSINESS_HOURS)
        val interval2 = HourInterval(LocalTime(9, 0), LocalTime(17, 0))
        val interval3 = HourInterval("10:00-18:00")

        // When & Then
        assertEquals(interval1, interval2, "Intervals with same times should be equal")
        assertEquals(interval1.hashCode(), interval2.hashCode(), "Hash codes should match")
        assertTrue(interval1 != interval3, "Intervals with different times should not be equal")
    }
}
