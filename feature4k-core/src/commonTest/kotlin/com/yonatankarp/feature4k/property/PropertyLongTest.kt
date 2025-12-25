package com.yonatankarp.feature4k.property

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyLongTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "timestamp"
        val value = 1234567890L

        // When
        val property = PropertyLong(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyLong(name = "userId", value = 9876543210L)

        // When
        val jsonString = Json.encodeToString(PropertyLong.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"userId\""))
        assertTrue(jsonString.contains("\"value\":9876543210"))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyLong(name = "timestamp", value = 1234567890L, description = "Unix timestamp")

        // When
        val jsonString = Json.encodeToString(PropertyLong.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyLong.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "timestamp"
        val value = 1234567890L

        // When
        val property = PropertyLong(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "status"
        val value = 200L
        val fixedValues = setOf(200L, 404L, 500L)

        // When
        val property = PropertyLong(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "timestamp"
        val value = 1234567890L

        // When
        val property = PropertyLong(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "status"
        val value = 200L
        val fixedValues = setOf(200L, 404L, 500L)

        // When
        val property = PropertyLong(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "status"
        val value = 403L
        val fixedValues = setOf(200L, 404L, 500L)

        // When
        val property = PropertyLong(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
