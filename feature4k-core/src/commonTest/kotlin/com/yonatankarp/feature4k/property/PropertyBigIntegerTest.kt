package com.yonatankarp.feature4k.property

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyBigIntegerTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "largeNumber"
        val value = "12345678901234567890"

        // When
        val property = PropertyBigInteger(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `validates value is valid BigInteger`() {
        // Given
        val name = "invalid"
        val value = "not-a-number"

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            PropertyBigInteger(name = name, value = value)
        }
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyBigInteger(name = "bigNum", value = "999999999999999999")

        // When
        val jsonString = Json.encodeToString(PropertyBigInteger.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"bigNum\""))
        assertTrue(jsonString.contains("\"value\":\"999999999999999999\""))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyBigInteger(name = "huge", value = "123456789012345", description = "Very large number")

        // When
        val jsonString = Json.encodeToString(PropertyBigInteger.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyBigInteger.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "largeNumber"
        val value = "12345678901234567890"

        // When
        val property = PropertyBigInteger(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "size"
        val value = "1000"
        val fixedValues = setOf("100", "1000", "10000")

        // When
        val property = PropertyBigInteger(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "largeNumber"
        val value = "12345678901234567890"

        // When
        val property = PropertyBigInteger(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "size"
        val value = "1000"
        val fixedValues = setOf("100", "1000", "10000")

        // When
        val property = PropertyBigInteger(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "size"
        val value = "5000"
        val fixedValues = setOf("100", "1000", "10000")

        // When
        val property = PropertyBigInteger(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
