package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.property.PropertyBigDecimal
import com.yonatankarp.feature4k.property.PropertyBigInteger
import com.yonatankarp.feature4k.property.PropertyBoolean
import com.yonatankarp.feature4k.property.PropertyByte
import com.yonatankarp.feature4k.property.PropertyDouble
import com.yonatankarp.feature4k.property.PropertyFloat
import com.yonatankarp.feature4k.property.PropertyInstant
import com.yonatankarp.feature4k.property.PropertyInt
import com.yonatankarp.feature4k.property.PropertyList
import com.yonatankarp.feature4k.property.PropertyLocalDateTime
import com.yonatankarp.feature4k.property.PropertyLong
import com.yonatankarp.feature4k.property.PropertySet
import com.yonatankarp.feature4k.property.PropertyShort
import com.yonatankarp.feature4k.property.PropertyString
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for Property DSL builders.
 *
 * Tests cover:
 * - Primitive property types (String, Int, Long, Double, Float, Boolean, Byte, Short)
 * - Numeric property types (BigInteger, BigDecimal)
 * - Date/Time property types (Instant, LocalDateTime)
 * - Collection property types (List, Set)
 * - Optional descriptions
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyBuildersTest {

    @Test
    fun `should create string property`() {
        // Given / When
        val props = properties {
            string("api.url", "https://api.example.com")
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyString
        assertEquals("api.url", property.name)
        assertEquals("https://api.example.com", property.value)
        assertNull(property.description)
    }

    @Test
    fun `should create string property with description`() {
        // Given / When
        val props = properties {
            string("api.url", "https://api.example.com", "The API base URL")
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyString
        assertEquals("The API base URL", property.description)
    }

    @Test
    fun `should create int property`() {
        // Given / When
        val props = properties {
            int("max.connections", 100)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyInt
        assertEquals("max.connections", property.name)
        assertEquals(100, property.value)
    }

    @Test
    fun `should create long property`() {
        // Given / When
        val props = properties {
            long("timeout.ms", 5000L)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyLong
        assertEquals("timeout.ms", property.name)
        assertEquals(5000L, property.value)
    }

    @Test
    fun `should create double property`() {
        // Given / When
        val props = properties {
            double("threshold", 0.95)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyDouble
        assertEquals("threshold", property.name)
        assertEquals(0.95, property.value)
    }

    @Test
    fun `should create float property`() {
        // Given / When
        val props = properties {
            float("ratio", 1.5f)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyFloat
        assertEquals("ratio", property.name)
        assertEquals(1.5f, property.value)
    }

    @Test
    fun `should create boolean property`() {
        // Given / When
        val props = properties {
            boolean("debug.enabled", false)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyBoolean
        assertEquals("debug.enabled", property.name)
        assertEquals(false, property.value)
    }

    @Test
    fun `should create byte property`() {
        // Given / When
        val props = properties {
            byte("priority", 5)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyByte
        assertEquals("priority", property.name)
        assertEquals(5.toByte(), property.value)
    }

    @Test
    fun `should create short property`() {
        // Given / When
        val props = properties {
            short("port", 8080)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyShort
        assertEquals("port", property.name)
        assertEquals(8080.toShort(), property.value)
    }

    @Test
    fun `should create big integer property`() {
        // Given / When
        val bigInt = "123456789012345678901234567890"
        val props = properties {
            bigInteger("large.number", bigInt)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyBigInteger
        assertEquals("large.number", property.name)
        assertEquals(bigInt, property.value)
    }

    @Test
    fun `should create big decimal property`() {
        // Given / When
        val bigDec = "123.456789012345678901234567890"
        val props = properties {
            bigDecimal("precise.value", bigDec)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyBigDecimal
        assertEquals("precise.value", property.name)
        assertEquals(bigDec, property.value)
    }

    @Test
    fun `should create instant property`() {
        // Given / When
        val timestamp = Instant.parse("2024-12-25T10:30:00Z")
        val props = properties {
            instant("created.at", timestamp)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyInstant
        assertEquals("created.at", property.name)
        assertEquals(timestamp, property.value)
    }

    @Test
    fun `should create local date time property`() {
        // Given / When
        val dateTime = LocalDateTime.parse("2024-12-25T10:30:00")
        val props = properties {
            localDateTime("scheduled.at", dateTime)
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyLocalDateTime
        assertEquals("scheduled.at", property.name)
        assertEquals(dateTime, property.value)
    }

    @Test
    fun `should create list property`() {
        // Given / When
        val props = properties {
            list("allowed.regions", listOf("US", "EU", "APAC"))
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertyList<*>
        assertEquals("allowed.regions", property.name)
        assertEquals(listOf("US", "EU", "APAC"), property.value)
    }

    @Test
    fun `should create set property`() {
        // Given / When
        val props = properties {
            set("roles", setOf("ADMIN", "USER", "MODERATOR"))
        }

        // Then
        assertEquals(1, props.size)
        val property = props.first() as PropertySet<*>
        assertEquals("roles", property.name)
        assertEquals(setOf("ADMIN", "USER", "MODERATOR"), property.value)
    }

    @Test
    fun `should create multiple properties`() {
        // Given / When
        val props = properties {
            string("api.url", "https://api.example.com")
            int("max.connections", 100)
            boolean("debug.enabled", false)
            list("environments", listOf("dev", "staging", "prod"))
        }

        // Then
        assertEquals(4, props.size)
        assertTrue(props[0] is PropertyString)
        assertTrue(props[1] is PropertyInt)
        assertTrue(props[2] is PropertyBoolean)
        assertTrue(props[3] is PropertyList<*>)
    }
}
