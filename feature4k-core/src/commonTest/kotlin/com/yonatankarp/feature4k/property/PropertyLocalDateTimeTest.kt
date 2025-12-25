package com.yonatankarp.feature4k.property

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyLocalDateTimeTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse("2024-01-15T10:30:00")

        // When
        val property = PropertyLocalDateTime(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyLocalDateTime(name = "appointmentTime", value = LocalDateTime.parse("2024-06-20T15:45:30"))

        // When
        val jsonString = Json.encodeToString(PropertyLocalDateTime.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"appointmentTime\""))
        assertTrue(jsonString.contains("\"value\":\"2024-06-20T15:45:30\""))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original =
            PropertyLocalDateTime(
                name = "meetingTime",
                value = LocalDateTime.parse("2024-12-25T14:00:00"),
                description = "Meeting scheduled time",
            )

        // When
        val jsonString = Json.encodeToString(PropertyLocalDateTime.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyLocalDateTime.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse("2024-01-15T10:30:00")

        // When
        val property = PropertyLocalDateTime(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "appointmentSlot"
        val value = LocalDateTime.parse("2024-12-25T14:00:00")
        val fixedValues = setOf(
            LocalDateTime.parse("2024-12-25T10:00:00"),
            LocalDateTime.parse("2024-12-25T14:00:00"),
            LocalDateTime.parse("2024-12-25T18:00:00"),
        )

        // When
        val property = PropertyLocalDateTime(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse("2024-01-15T10:30:00")

        // When
        val property = PropertyLocalDateTime(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "appointmentSlot"
        val value = LocalDateTime.parse("2024-12-25T14:00:00")
        val fixedValues = setOf(
            LocalDateTime.parse("2024-12-25T10:00:00"),
            LocalDateTime.parse("2024-12-25T14:00:00"),
            LocalDateTime.parse("2024-12-25T18:00:00"),
        )

        // When
        val property = PropertyLocalDateTime(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "appointmentSlot"
        val value = LocalDateTime.parse("2024-12-25T12:00:00")
        val fixedValues = setOf(
            LocalDateTime.parse("2024-12-25T10:00:00"),
            LocalDateTime.parse("2024-12-25T14:00:00"),
            LocalDateTime.parse("2024-12-25T18:00:00"),
        )

        // When
        val property = PropertyLocalDateTime(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
