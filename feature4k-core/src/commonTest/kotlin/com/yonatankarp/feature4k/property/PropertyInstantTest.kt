package com.yonatankarp.feature4k.property

import com.yonatankarp.feature4k.core.DateTimeFixtures.CHRISTMAS_2024_MIDNIGHT
import com.yonatankarp.feature4k.core.DateTimeFixtures.RELEASE_MILESTONES
import com.yonatankarp.feature4k.core.DateTimeFixtures.TIMESTAMP_ISO
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for PropertyInstant class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyInstantTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse(TIMESTAMP_ISO)

        // When
        val property = PropertyInstant(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyInstant(name = "timestamp", value = Instant.parse("2024-06-20T15:45:30Z"))

        // When
        val jsonString = Json.encodeToString(PropertyInstant.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"timestamp\""))
        assertTrue(jsonString.contains("\"value\":\"2024-06-20T15:45:30Z\""))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original =
            PropertyInstant(
                name = "eventTime",
                value = CHRISTMAS_2024_MIDNIGHT,
                description = "Event occurrence time",
            )

        // When
        val jsonString = Json.encodeToString(PropertyInstant.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyInstant.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse(TIMESTAMP_ISO)

        // When
        val property = PropertyInstant(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "releaseDate"
        val value = CHRISTMAS_2024_MIDNIGHT

        // When
        val property = PropertyInstant(name = name, value = value, fixedValues = RELEASE_MILESTONES)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse(TIMESTAMP_ISO)

        // When
        val property = PropertyInstant(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "releaseDate"
        val value = CHRISTMAS_2024_MIDNIGHT

        // When
        val property = PropertyInstant(name = name, value = value, fixedValues = RELEASE_MILESTONES)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "releaseDate"
        val value = Instant.parse("2024-10-01T00:00:00Z")

        // When
        val property = PropertyInstant(name = name, value = value, fixedValues = RELEASE_MILESTONES)

        // Then
        assertFalse(property.isValid)
    }
}
