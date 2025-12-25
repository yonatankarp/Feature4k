package com.yonatankarp.feature4k.property

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PropertyFactoryTest {
    @Test
    fun `creates string property`() {
        // Given
        val name = "apiKey"
        val value = "secret123"

        // When
        val property = PropertyFactory.string(name, value)

        // Then
        assertIs<PropertyString>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates int property`() {
        // Given
        val name = "maxRetries"
        val value = 3

        // When
        val property = PropertyFactory.int(name, value)

        // Then
        assertIs<PropertyInt>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates boolean property`() {
        // Given
        val name = "enabled"
        val value = true

        // When
        val property = PropertyFactory.boolean(name, value)

        // Then
        assertIs<PropertyBoolean>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates long property`() {
        // Given
        val name = "timestamp"
        val value = 1234567890L

        // When
        val property = PropertyFactory.long(name, value)

        // Then
        assertIs<PropertyLong>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates double property`() {
        // Given
        val name = "pi"
        val value = 3.14159

        // When
        val property = PropertyFactory.double(name, value)

        // Then
        assertIs<PropertyDouble>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates float property`() {
        // Given
        val name = "temperature"
        val value = 98.6f

        // When
        val property = PropertyFactory.float(name, value)

        // Then
        assertIs<PropertyFloat>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates byte property`() {
        // Given
        val name = "flag"
        val value: Byte = 127

        // When
        val property = PropertyFactory.byte(name, value)

        // Then
        assertIs<PropertyByte>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates short property`() {
        // Given
        val name = "port"
        val value: Short = 8080

        // When
        val property = PropertyFactory.short(name, value)

        // Then
        assertIs<PropertyShort>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates bigInteger property`() {
        // Given
        val name = "largeNumber"
        val value = "12345678901234567890"

        // When
        val property = PropertyFactory.bigInteger(name, value)

        // Then
        assertIs<PropertyBigInteger>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates bigDecimal property`() {
        // Given
        val name = "preciseValue"
        val value = "123.456789012345"

        // When
        val property = PropertyFactory.bigDecimal(name, value)

        // Then
        assertIs<PropertyBigDecimal>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates instant property`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse("2024-01-15T10:30:00Z")

        // When
        val property = PropertyFactory.instant(name, value)

        // Then
        assertIs<PropertyInstant>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates localDateTime property`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse("2024-01-15T10:30:00")

        // When
        val property = PropertyFactory.localDateTime(name, value)

        // Then
        assertIs<PropertyLocalDateTime>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates list property`() {
        // Given
        val name = "tags"
        val value = listOf("kotlin", "multiplatform")

        // When
        val property = PropertyFactory.list(name, value)

        // Then
        assertIs<PropertyList<String>>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates set property`() {
        // Given
        val name = "roles"
        val value = setOf("admin", "user")

        // When
        val property = PropertyFactory.set(name, value)

        // Then
        assertIs<PropertySet<String>>(property)
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `creates property with description`() {
        // Given
        val name = "apiKey"
        val value = "secret"
        val description = "API key for external service"

        // When
        val property = PropertyFactory.string(name, value, description)

        // Then
        assertEquals(description, property.description)
    }

    @Test
    fun `creates property with fixed values`() {
        // Given
        val name = "env"
        val value = "prod"
        val fixedValues = setOf("dev", "staging", "prod")

        // When
        val property = PropertyFactory.string(name, value, fixedValues = fixedValues)

        // Then
        assertEquals(fixedValues, property.fixedValues)
    }

    @Test
    fun `creates property with readOnly false by default`() {
        // Given
        val name = "apiKey"
        val value = "secret"

        // When
        val property = PropertyFactory.string(name, value)

        // Then
        assertFalse(property.readOnly)
    }

    @Test
    fun `creates property with readOnly true`() {
        // Given
        val name = "apiKey"
        val value = "secret"
        val readOnly = true

        // When
        val property = PropertyFactory.string(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates int property with readOnly true`() {
        // Given
        val name = "maxRetries"
        val value = 3
        val readOnly = true

        // When
        val property = PropertyFactory.int(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates boolean property with readOnly true`() {
        // Given
        val name = "enabled"
        val value = true
        val readOnly = true

        // When
        val property = PropertyFactory.boolean(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates long property with readOnly true`() {
        // Given
        val name = "timestamp"
        val value = 1234567890L
        val readOnly = true

        // When
        val property = PropertyFactory.long(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates double property with readOnly true`() {
        // Given
        val name = "pi"
        val value = 3.14159
        val readOnly = true

        // When
        val property = PropertyFactory.double(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates float property with readOnly true`() {
        // Given
        val name = "temperature"
        val value = 98.6f
        val readOnly = true

        // When
        val property = PropertyFactory.float(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates byte property with readOnly true`() {
        // Given
        val name = "flag"
        val value: Byte = 127
        val readOnly = true

        // When
        val property = PropertyFactory.byte(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates short property with readOnly true`() {
        // Given
        val name = "port"
        val value: Short = 8080
        val readOnly = true

        // When
        val property = PropertyFactory.short(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates bigInteger property with readOnly true`() {
        // Given
        val name = "largeNumber"
        val value = "12345678901234567890"
        val readOnly = true

        // When
        val property = PropertyFactory.bigInteger(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates bigDecimal property with readOnly true`() {
        // Given
        val name = "preciseValue"
        val value = "123.456789012345"
        val readOnly = true

        // When
        val property = PropertyFactory.bigDecimal(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates instant property with readOnly true`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse("2024-01-15T10:30:00Z")
        val readOnly = true

        // When
        val property = PropertyFactory.instant(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates localDateTime property with readOnly true`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse("2024-01-15T10:30:00")
        val readOnly = true

        // When
        val property = PropertyFactory.localDateTime(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates list property with readOnly true`() {
        // Given
        val name = "tags"
        val value = listOf("kotlin", "multiplatform")
        val readOnly = true

        // When
        val property = PropertyFactory.list(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates set property with readOnly true`() {
        // Given
        val name = "roles"
        val value = setOf("admin", "user")
        val readOnly = true

        // When
        val property = PropertyFactory.set(name, value, readOnly = readOnly)

        // Then
        assertTrue(property.readOnly)
    }
}
