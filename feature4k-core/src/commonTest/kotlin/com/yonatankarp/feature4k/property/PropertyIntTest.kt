package com.yonatankarp.feature4k.property

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for PropertyInt class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyIntTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "maxRetries"
        val value = 3

        // When
        val property = PropertyInt(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "maxRetries"
        val value = 3

        // When
        val property = PropertyInt(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "priority"
        val value = 1
        val fixedValues = setOf(1, 2, 3)

        // When
        val property = PropertyInt(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "maxRetries"
        val value = 3

        // When
        val property = PropertyInt(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val fixedValues = setOf(1, 2, 3)

        // When
        val property = PropertyInt(name = "priority", value = 1, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val fixedValues = setOf(1, 2, 3)

        // When
        val property = PropertyInt(name = "priority", value = 5, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyInt(name = "count", value = 42)

        // When
        val jsonString = Json.encodeToString(PropertyInt.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"count\""))
        assertTrue(jsonString.contains("\"value\":42"))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyInt(name = "timeout", value = 30, description = "Timeout in seconds")

        // When
        val jsonString = Json.encodeToString(PropertyInt.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyInt.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }
}
