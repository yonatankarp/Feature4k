package com.yonatankarp.feature4k.property

import com.yonatankarp.feature4k.core.DateTimeFixtures.CHRISTMAS_AFTERNOON_SLOT
import com.yonatankarp.feature4k.core.DateTimeFixtures.CHRISTMAS_APPOINTMENT_SLOTS
import com.yonatankarp.feature4k.core.DateTimeFixtures.CHRISTMAS_NOON_SLOT
import com.yonatankarp.feature4k.core.DateTimeFixtures.TIMESTAMP_LOCAL
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for PropertyLocalDateTime class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyLocalDateTimeTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse(TIMESTAMP_LOCAL)

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
                value = CHRISTMAS_AFTERNOON_SLOT,
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
        val value = LocalDateTime.parse(TIMESTAMP_LOCAL)

        // When
        val property = PropertyLocalDateTime(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "appointmentSlot"
        val value = CHRISTMAS_AFTERNOON_SLOT

        // When
        val property = PropertyLocalDateTime(name = name, value = value, fixedValues = CHRISTMAS_APPOINTMENT_SLOTS)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse(TIMESTAMP_LOCAL)

        // When
        val property = PropertyLocalDateTime(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "appointmentSlot"
        val value = CHRISTMAS_AFTERNOON_SLOT

        // When
        val property = PropertyLocalDateTime(name = name, value = value, fixedValues = CHRISTMAS_APPOINTMENT_SLOTS)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "appointmentSlot"
        val value = CHRISTMAS_NOON_SLOT

        // When
        val property = PropertyLocalDateTime(name = name, value = value, fixedValues = CHRISTMAS_APPOINTMENT_SLOTS)

        // Then
        assertFalse(property.isValid)
    }
}
