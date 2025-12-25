package com.yonatankarp.feature4k.property

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyListTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "tags"
        val value = listOf("kotlin", "multiplatform", "feature-flags")

        // When
        val property = PropertyList(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyList(name = "languages", value = listOf("en", "fr", "de"))

        // When
        val serializer = PropertyList.serializer(String.serializer())
        val jsonString = Json.encodeToString(serializer, property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"languages\""))
        assertTrue(jsonString.contains("\"value\":[\"en\",\"fr\",\"de\"]"))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original =
            PropertyList(
                name = "features",
                value = listOf("auth", "payments", "analytics"),
                description = "Enabled features",
            )

        // When
        val serializer = PropertyList.serializer(String.serializer())
        val jsonString = Json.encodeToString(serializer, original)
        val deserialized = Json.decodeFromString(serializer, jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `works with integer lists`() {
        // Given
        val name = "scores"
        val value = listOf(100, 200, 300)

        // When
        val property = PropertyList(name = name, value = value)

        // Then
        assertEquals(value, property.value)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "tags"
        val value = listOf("kotlin", "multiplatform")

        // When
        val property = PropertyList(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "environments"
        val value = listOf("dev", "staging")
        val fixedValues = setOf(
            listOf("dev"),
            listOf("dev", "staging"),
            listOf("dev", "staging", "prod")
        )

        // When
        val property = PropertyList(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "tags"
        val value = listOf("kotlin", "multiplatform")

        // When
        val property = PropertyList(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "environments"
        val value = listOf("dev", "staging")
        val fixedValues = setOf(
            listOf("dev"),
            listOf("dev", "staging"),
            listOf("dev", "staging", "prod")
        )

        // When
        val property = PropertyList(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "environments"
        val value = listOf("dev", "qa")
        val fixedValues = setOf(
            listOf("dev"),
            listOf("dev", "staging"),
            listOf("dev", "staging", "prod")
        )

        // When
        val property = PropertyList(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
