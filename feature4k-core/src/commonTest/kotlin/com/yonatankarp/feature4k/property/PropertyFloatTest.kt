package com.yonatankarp.feature4k.property

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyFloatTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "temperature"
        val value = 98.6f

        // When
        val property = PropertyFloat(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyFloat(name = "ratio", value = 3.14f)

        // When
        val jsonString = Json.encodeToString(PropertyFloat.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"ratio\""))
        assertTrue(jsonString.contains("\"value\":3.14"))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyFloat(name = "rate", value = 0.05f, description = "Interest rate")

        // When
        val jsonString = Json.encodeToString(PropertyFloat.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyFloat.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "temperature"
        val value = 98.6f

        // When
        val property = PropertyFloat(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "temperature"
        val value = 98.6f
        val fixedValues = setOf(98.6f, 100.4f, 102.2f)

        // When
        val property = PropertyFloat(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "temperature"
        val value = 98.6f

        // When
        val property = PropertyFloat(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "temperature"
        val value = 98.6f
        val fixedValues = setOf(98.6f, 100.4f, 102.2f)

        // When
        val property = PropertyFloat(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "temperature"
        val value = 95.0f
        val fixedValues = setOf(98.6f, 100.4f, 102.2f)

        // When
        val property = PropertyFloat(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
