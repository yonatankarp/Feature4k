package com.yonatankarp.feature4k.property

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertySetTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "allowedRoles"
        val value = setOf("admin", "user", "guest")

        // When
        val property = PropertySet(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertySet(name = "permissions", value = setOf("read", "write"))

        // When
        val serializer = PropertySet.serializer(String.serializer())
        val jsonString = Json.encodeToString(serializer, property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"permissions\""))
        assertTrue(jsonString.contains("\"read\""))
        assertTrue(jsonString.contains("\"write\""))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original =
            PropertySet(
                name = "countries",
                value = setOf("US", "UK", "CA"),
                description = "Supported countries",
            )

        // When
        val serializer = PropertySet.serializer(String.serializer())
        val jsonString = Json.encodeToString(serializer, original)
        val deserialized = Json.decodeFromString(serializer, jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `works with integer sets`() {
        // Given
        val name = "ports"
        val value = setOf(80, 443, 8080)

        // When
        val property = PropertySet(name = name, value = value)

        // Then
        assertEquals(value, property.value)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "allowedRoles"
        val value = setOf("admin", "user")

        // When
        val property = PropertySet(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "permissions"
        val value = setOf("read", "write")
        val fixedValues = setOf(
            setOf("read"),
            setOf("read", "write"),
            setOf("read", "write", "execute")
        )

        // When
        val property = PropertySet(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "allowedRoles"
        val value = setOf("admin", "user")

        // When
        val property = PropertySet(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "permissions"
        val value = setOf("read", "write")
        val fixedValues = setOf(
            setOf("read"),
            setOf("read", "write"),
            setOf("read", "write", "execute")
        )

        // When
        val property = PropertySet(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "permissions"
        val value = setOf("read", "delete")
        val fixedValues = setOf(
            setOf("read"),
            setOf("read", "write"),
            setOf("read", "write", "execute")
        )

        // When
        val property = PropertySet(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
