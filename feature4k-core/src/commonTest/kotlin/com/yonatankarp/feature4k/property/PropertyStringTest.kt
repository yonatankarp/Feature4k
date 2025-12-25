package com.yonatankarp.feature4k.property

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for PropertyString class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyStringTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "apiKey"
        val value = "secret123"

        // When
        val property = PropertyString(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `description defaults to null`() {
        // When
        val property = PropertyString(name = "key", value = "value")

        // Then
        assertNull(property.description)
    }

    @Test
    fun `stores description when provided`() {
        // Given
        val description = "API key for external service"

        // When
        val property = PropertyString(name = "key", value = "value", description = description)

        // Then
        assertEquals(description, property.description)
    }

    @Test
    fun `fixedValues defaults to empty set`() {
        // When
        val property = PropertyString(name = "key", value = "value")

        // Then
        assertTrue(property.fixedValues.isEmpty())
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `stores fixedValues when provided`() {
        // Given
        val fixedValues = setOf("dev", "staging", "prod")

        // When
        val property = PropertyString(name = "env", value = "prod", fixedValues = fixedValues)

        // Then
        assertEquals(fixedValues, property.fixedValues)
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // When
        val property = PropertyString(name = "key", value = "any value")

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val fixedValues = setOf("dev", "staging", "prod")

        // When
        val property = PropertyString(name = "env", value = "prod", fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val fixedValues = setOf("dev", "staging", "prod")

        // When
        val property = PropertyString(name = "env", value = "invalid", fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyString(name = "key", value = "value")

        // When
        val jsonString = Json.encodeToString(PropertyString.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"key\""))
        assertTrue(jsonString.contains("\"value\":\"value\""))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyString(name = "key", value = "value", description = "Test property")

        // When
        val jsonString = Json.encodeToString(PropertyString.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyString.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `serializes and deserializes with fixed values`() {
        // Given
        val original =
            PropertyString(
                name = "env",
                value = "prod",
                description = "Environment",
                fixedValues = setOf("dev", "staging", "prod"),
            )

        // When
        val jsonString = Json.encodeToString(PropertyString.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyString.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
        assertEquals(original.fixedValues, deserialized.fixedValues)
    }
}
