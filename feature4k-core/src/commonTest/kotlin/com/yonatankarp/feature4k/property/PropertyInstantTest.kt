package com.yonatankarp.feature4k.property

import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyInstantTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse("2024-01-15T10:30:00Z")

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
                value = Instant.parse("2024-12-25T00:00:00Z"),
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
        val value = Instant.parse("2024-01-15T10:30:00Z")

        // When
        val property = PropertyInstant(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "releaseDate"
        val value = Instant.parse("2024-12-25T00:00:00Z")
        val fixedValues = setOf(
            Instant.parse("2024-06-15T00:00:00Z"),
            Instant.parse("2024-12-25T00:00:00Z"),
            Instant.parse("2025-06-15T00:00:00Z"),
        )

        // When
        val property = PropertyInstant(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse("2024-01-15T10:30:00Z")

        // When
        val property = PropertyInstant(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "releaseDate"
        val value = Instant.parse("2024-12-25T00:00:00Z")
        val fixedValues = setOf(
            Instant.parse("2024-06-15T00:00:00Z"),
            Instant.parse("2024-12-25T00:00:00Z"),
            Instant.parse("2025-06-15T00:00:00Z"),
        )

        // When
        val property = PropertyInstant(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "releaseDate"
        val value = Instant.parse("2024-10-01T00:00:00Z")
        val fixedValues = setOf(
            Instant.parse("2024-06-15T00:00:00Z"),
            Instant.parse("2024-12-25T00:00:00Z"),
            Instant.parse("2025-06-15T00:00:00Z"),
        )

        // When
        val property = PropertyInstant(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
