package com.yonatankarp.feature4k.property

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for PropertyDouble class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyDoubleTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "pi"
        val value = 3.14159

        // When
        val property = PropertyDouble(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyDouble(name = "rate", value = 0.05)

        // When
        val jsonString = Json.encodeToString(PropertyDouble.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"rate\""))
        assertTrue(jsonString.contains("\"value\":0.05"))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyDouble(name = "temperature", value = 98.6, description = "Body temperature in F")

        // When
        val jsonString = Json.encodeToString(PropertyDouble.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyDouble.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "rate"
        val value = 0.05

        // When
        val property = PropertyDouble(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "rate"
        val value = 0.05
        val fixedValues = setOf(0.01, 0.05, 0.1)

        // When
        val property = PropertyDouble(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "rate"
        val value = 0.05

        // When
        val property = PropertyDouble(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "rate"
        val value = 0.05
        val fixedValues = setOf(0.01, 0.05, 0.1)

        // When
        val property = PropertyDouble(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "rate"
        val value = 0.15
        val fixedValues = setOf(0.01, 0.05, 0.1)

        // When
        val property = PropertyDouble(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
