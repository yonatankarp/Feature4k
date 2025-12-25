package com.yonatankarp.feature4k.property

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for PropertyBoolean class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyBooleanTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "enableCache"
        val value = true

        // When
        val property = PropertyBoolean(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `stores false value`() {
        // Given
        val name = "disabled"
        val value = false

        // When
        val property = PropertyBoolean(name = name, value = value)

        // Then
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyBoolean(name = "enabled", value = true)

        // When
        val jsonString = Json.encodeToString(PropertyBoolean.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"enabled\""))
        assertTrue(jsonString.contains("\"value\":true"))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyBoolean(name = "enabled", value = true, description = "Feature enabled")

        // When
        val jsonString = Json.encodeToString(PropertyBoolean.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyBoolean.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "enabled"
        val value = true

        // When
        val property = PropertyBoolean(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "enabled"
        val value = true
        val fixedValues = setOf(true)

        // When
        val property = PropertyBoolean(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "enabled"
        val value = true

        // When
        val property = PropertyBoolean(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "enabled"
        val value = true
        val fixedValues = setOf(true)

        // When
        val property = PropertyBoolean(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "enabled"
        val value = false
        val fixedValues = setOf(true)

        // When
        val property = PropertyBoolean(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
