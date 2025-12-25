package com.yonatankarp.feature4k.property

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyByteTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "flag"
        val value: Byte = 127

        // When
        val property = PropertyByte(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyByte(name = "status", value = 1)

        // When
        val jsonString = Json.encodeToString(PropertyByte.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"status\""))
        assertTrue(jsonString.contains("\"value\":1"))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyByte(name = "code", value = 42, description = "Status code")

        // When
        val jsonString = Json.encodeToString(PropertyByte.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyByte.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "flag"
        val value: Byte = 127

        // When
        val property = PropertyByte(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "status"
        val value: Byte = 1
        val fixedValues = setOf<Byte>(0, 1, 2)

        // When
        val property = PropertyByte(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "flag"
        val value: Byte = 127

        // When
        val property = PropertyByte(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "status"
        val value: Byte = 1
        val fixedValues = setOf<Byte>(0, 1, 2)

        // When
        val property = PropertyByte(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "status"
        val value: Byte = 5
        val fixedValues = setOf<Byte>(0, 1, 2)

        // When
        val property = PropertyByte(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
