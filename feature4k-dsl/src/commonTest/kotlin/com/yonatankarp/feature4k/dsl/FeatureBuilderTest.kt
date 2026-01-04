package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.property.PropertyInt
import com.yonatankarp.feature4k.property.PropertyString
import com.yonatankarp.feature4k.strategy.AllowListStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for Feature DSL builder.
 *
 * Tests cover:
 * - Basic feature creation
 * - Enabled/disabled state
 * - Description and group configuration
 * - Permissions configuration
 * - Flipping strategy assignment
 * - Custom properties
 *
 * @author Yonatan Karp-Rudin
 */
class FeatureBuilderTest {

    @Test
    fun `should create basic feature with UID only`() {
        // Given / When
        val feature = feature("test-feature")

        // Then
        assertEquals("test-feature", feature.uid)
        assertFalse(feature.enabled)
        assertNull(feature.description)
        assertNull(feature.group)
        assertFalse(feature.hasPermissions())
        assertFalse(feature.hasFlippingStrategy())
        assertFalse(feature.hasCustomProperties())
    }

    @Test
    fun `should create enabled feature`() {
        // Given / When
        val feature = feature("enabled-feature") {
            enabled = true
        }

        // Then
        assertTrue(feature.enabled)
    }

    @Test
    fun `should create feature with description`() {
        // Given / When
        val feature = feature("described-feature") {
            description = "This is a test feature"
        }

        // Then
        assertEquals("This is a test feature", feature.description)
    }

    @Test
    fun `should create feature with group`() {
        // Given / When
        val feature = feature("grouped-feature") {
            group = "premium"
        }

        // Then
        assertEquals("premium", feature.group)
        assertTrue(feature.hasGroup())
    }

    @Test
    fun `should create feature with single permission`() {
        // Given / When
        val feature = feature("permission-feature") {
            permissions += "ROLE_ADMIN"
        }

        // Then
        assertTrue(feature.hasPermissions())
        assertTrue(feature.permissions.contains("ROLE_ADMIN"))
        assertEquals(1, feature.permissions.size)
    }

    @Test
    fun `should create feature with multiple permissions`() {
        // Given / When
        val feature = feature("multi-permission-feature") {
            permissions += listOf("ROLE_ADMIN", "ROLE_PREMIUM", "ROLE_BETA")
        }

        // Then
        assertTrue(feature.hasPermissions())
        assertEquals(3, feature.permissions.size)
        assertTrue(feature.permissions.containsAll(listOf("ROLE_ADMIN", "ROLE_PREMIUM", "ROLE_BETA")))
    }

    @Test
    fun `should create feature with flipping strategy`() {
        // Given / When
        val feature = feature("strategy-feature") {
            enabled = true
            flippingStrategy = allowList("alice", "bob")
        }

        // Then
        assertTrue(feature.hasFlippingStrategy())
        assertNotNull(feature.flippingStrategy)
        assertTrue(feature.flippingStrategy is AllowListStrategy)
    }

    @Test
    fun `should create feature with custom properties`() {
        // Given / When
        val feature = feature("property-feature") {
            enabled = true
            customProperties {
                string("api.url", "https://api.example.com")
                int("timeout", 5000)
            }
        }

        // Then
        assertTrue(feature.hasCustomProperties())
        assertEquals(2, feature.customProperties.size)

        val apiUrl = feature.customProperties["api.url"] as PropertyString
        assertEquals("https://api.example.com", apiUrl.value)

        val timeout = feature.customProperties["timeout"] as PropertyInt
        assertEquals(5000, timeout.value)
    }

    @Test
    fun `should create fully configured feature`() {
        // Given / When
        val feature = feature("full-feature") {
            enabled = true
            description = "Fully configured feature"
            group = "premium"
            permissions += listOf("ROLE_ADMIN", "ROLE_PREMIUM")
            flippingStrategy = percentage(0.5)
            customProperties {
                string("config.url", "https://config.example.com")
                boolean("debug", false)
            }
        }

        // Then
        assertTrue(feature.enabled)
        assertEquals("Fully configured feature", feature.description)
        assertEquals("premium", feature.group)
        assertTrue(feature.hasPermissions())
        assertTrue(feature.hasFlippingStrategy())
        assertTrue(feature.hasCustomProperties())
        assertEquals(2, feature.permissions.size)
        assertEquals(2, feature.customProperties.size)
    }
}
