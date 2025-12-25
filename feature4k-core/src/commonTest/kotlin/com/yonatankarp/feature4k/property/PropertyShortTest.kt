package com.yonatankarp.feature4k.property

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyShortTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "port"
        val value: Short = 8080

        // When
        val property = PropertyShort(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyShort(name = "year", value = 2024)

        // When
        val jsonString = Json.encodeToString(PropertyShort.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"year\""))
        assertTrue(jsonString.contains("\"value\":2024"))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyShort(name = "port", value = 443, description = "HTTPS port")

        // When
        val jsonString = Json.encodeToString(PropertyShort.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyShort.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "port"
        val value: Short = 8080

        // When
        val property = PropertyShort(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "port"
        val value: Short = 443
        val fixedValues = setOf<Short>(80, 443, 8080)

        // When
        val property = PropertyShort(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "port"
        val value: Short = 8080

        // When
        val property = PropertyShort(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "port"
        val value: Short = 443
        val fixedValues = setOf<Short>(80, 443, 8080)

        // When
        val property = PropertyShort(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "port"
        val value: Short = 9000
        val fixedValues = setOf<Short>(80, 443, 8080)

        // When
        val property = PropertyShort(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
