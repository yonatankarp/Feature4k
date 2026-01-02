package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.audit.EventPublisher
import com.yonatankarp.feature4k.property.PropertyBoolean
import com.yonatankarp.feature4k.property.PropertyInt
import com.yonatankarp.feature4k.property.PropertyString
import com.yonatankarp.feature4k.security.DefaultAuthorizationManager
import com.yonatankarp.feature4k.store.StoreEvent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for Feature4K DSL builder.
 *
 * Tests cover:
 * - Basic Feature4K instance creation
 * - Auto-create configuration
 * - Inline feature definitions
 * - Inline property definitions
 * - Combined features and properties
 *
 * @author Yonatan Karp-Rudin
 */
class Feature4KBuilderTest {

    @Test
    fun `should create Feature4K with default configuration`() = runTest {
        // Given / When
        val feature4k = feature4k {
            // No configuration
        }

        // Then
        assertNotNull(feature4k)
        assertTrue(feature4k.allFeatures().isEmpty())
        assertTrue(feature4k.allProperties().isEmpty())
    }

    @Test
    fun `should create Feature4K with autoCreate enabled`() = runTest {
        // Given / When
        val feature4k = feature4k {
            autoCreate = true
        }

        // Then
        assertNotNull(feature4k)
        assertFalse(feature4k["non-existent-feature"])
        assertTrue(feature4k.exists("non-existent-feature"))
    }

    @Test
    fun `should create Feature4K with inline feature definitions`() = runTest {
        // Given / When
        val feature4k = feature4k {
            features {
                feature("dark-mode") {
                    enabled = true
                    description = "Enable dark theme"
                }
                feature("beta-features") {
                    enabled = false
                    permissions += "ROLE_BETA"
                }
            }
        }

        // Then
        assertTrue(feature4k.exists("dark-mode"))
        assertTrue(feature4k.exists("beta-features"))

        val darkMode = feature4k.feature("dark-mode")
        assertTrue(darkMode.enabled)
        assertEquals("Enable dark theme", darkMode.description)

        val betaFeatures = feature4k.feature("beta-features")
        assertFalse(betaFeatures.enabled)
        assertTrue(betaFeatures.hasPermissions())
    }

    @Test
    fun `should create Feature4K with inline property definitions`() = runTest {
        // Given / When
        val feature4k = feature4k {
            properties {
                string("api.url", "https://api.example.com")
                int("max.connections", 100)
                boolean("debug.enabled", false)
            }
        }

        // Then
        val apiUrl = feature4k.property("api.url") as PropertyString
        assertEquals("https://api.example.com", apiUrl.value)

        val maxConnections = feature4k.property("max.connections") as PropertyInt
        assertEquals(100, maxConnections.value)

        val debugEnabled = feature4k.property("debug.enabled") as PropertyBoolean
        assertEquals(false, debugEnabled.value)
    }

    @Test
    fun `should create Feature4K with both features and properties`() = runTest {
        // Given / When
        val feature4k = feature4k {
            features {
                feature("premium-dashboard") {
                    enabled = true
                    group = "premium"
                }
            }
            properties {
                string("app.version", "1.0.0")
            }
        }

        // Then
        assertTrue(feature4k.exists("premium-dashboard"))
        val version = feature4k.property("app.version") as PropertyString
        assertEquals("1.0.0", version.value)
    }

    @Test
    fun `should create Feature4K with features having strategies`() = runTest {
        // Given / When
        val feature4k = feature4k {
            features {
                feature("beta-rollout") {
                    enabled = true
                    flippingStrategy = percentage(0.5)
                }
                feature("admin-only") {
                    enabled = true
                    flippingStrategy = allowList("admin", "superuser")
                }
            }
        }

        // Then
        assertTrue(feature4k.exists("beta-rollout"))
        assertTrue(feature4k.exists("admin-only"))

        val betaRollout = feature4k.feature("beta-rollout")
        assertTrue(betaRollout.hasFlippingStrategy())

        val adminOnly = feature4k.feature("admin-only")
        assertTrue(adminOnly.hasFlippingStrategy())
    }

    @Test
    fun `should create Feature4K with custom authorizationsManager`() = runTest {
        // Given
        val authManager = DefaultAuthorizationManager(
            currentUserName = "testuser",
            currentUserPermissions = setOf("ROLE_ADMIN", "ROLE_USER"),
        )

        // When
        val feature4k = feature4k {
            authorizationsManager = authManager
            features {
                feature("admin-feature") {
                    enabled = true
                    permissions += "ROLE_ADMIN"
                }
            }
        }

        // Then
        assertNotNull(feature4k)
        assertTrue(feature4k.exists("admin-feature"))
    }

    @Test
    fun `should create Feature4K with null authorizationsManager by default`() = runTest {
        // Given / When
        val feature4k = feature4k {
            // authorizationsManager not set (null by default)
        }

        // Then
        assertNotNull(feature4k)
    }

    @Test
    fun `should create Feature4K with custom eventPublisher`() = runTest {
        // Given
        val publishedEvents = mutableListOf<StoreEvent>()

        // When
        val feature4k = feature4k {
            eventPublisher = EventPublisher { event ->
                publishedEvents.add(event)
            }
            features {
                feature("test-feature") {
                    enabled = true
                }
            }
        }

        // Then
        assertNotNull(feature4k)
        assertTrue(feature4k.exists("test-feature"))
        assertTrue(publishedEvents.isNotEmpty(), "Expected events to be published")
        assertEquals(1, publishedEvents.size)
    }

    @Test
    fun `should create Feature4K with null eventPublisher by default`() = runTest {
        // Given / When
        val feature4k = feature4k {
            // eventPublisher not set (null by default)
        }

        // Then
        assertNotNull(feature4k)
    }

    @Test
    fun `should create feature with default empty block in FeaturesBuilder`() = runTest {
        // Given / When
        val feature4k = feature4k {
            features {
                feature("minimal-feature") // No block provided, uses default empty block
            }
        }

        // Then
        assertTrue(feature4k.exists("minimal-feature"))
        val minimalFeature = feature4k.feature("minimal-feature")
        assertEquals("minimal-feature", minimalFeature.uid)
        assertFalse(minimalFeature.enabled)
        assertNull(minimalFeature.description)
        assertNull(minimalFeature.group)
    }
}
