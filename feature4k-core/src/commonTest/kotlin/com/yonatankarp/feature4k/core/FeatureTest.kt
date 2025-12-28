package com.yonatankarp.feature4k.core

import com.yonatankarp.feature4k.core.FeatureFixtures.basicFeature
import com.yonatankarp.feature4k.core.FeatureFixtures.disabledFeature
import com.yonatankarp.feature4k.core.FeatureFixtures.enabledFeature
import com.yonatankarp.feature4k.core.FeatureFixtures.featureWithCustomProperties
import com.yonatankarp.feature4k.core.FeatureFixtures.featureWithGroup
import com.yonatankarp.feature4k.core.FeatureFixtures.featureWithPermissions
import com.yonatankarp.feature4k.core.FeatureFixtures.fullFeature
import com.yonatankarp.feature4k.exception.InvalidFeatureIdentifierException
import com.yonatankarp.feature4k.property.PropertyBoolean
import com.yonatankarp.feature4k.property.PropertyInt
import com.yonatankarp.feature4k.property.PropertyLong
import com.yonatankarp.feature4k.property.PropertyString
import com.yonatankarp.feature4k.strategy.AlwaysOffStrategy
import com.yonatankarp.feature4k.strategy.AlwaysOnStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for Feature class.
 *
 * @author Yonatan Karp-Rudin
 */
class FeatureTest {
    @Test
    fun `should create feature with minimal constructor`() {
        // Given
        val uid = "feature1"

        // When
        val feature = Feature(uid = uid)

        // Then
        assertEquals("feature1", feature.uid)
        assertFalse(feature.enabled)
        assertNull(feature.description)
        assertNull(feature.group)
        assertTrue(feature.permissions.isEmpty())
    }

    @Test
    fun `should create feature with all properties`() {
        // Given
        val uid = "feature1"
        val enabled = true
        val description = "Test feature"
        val group = "testGroup"
        val permissions = setOf("ADMIN", "USER")

        // When
        val feature =
            Feature(
                uid = uid,
                enabled = enabled,
                description = description,
                group = group,
                permissions = permissions,
            )

        // Then
        assertEquals(uid, feature.uid)
        assertTrue(feature.enabled)
        assertEquals(description, feature.description)
        assertEquals(group, feature.group)
        assertEquals(permissions, feature.permissions)
    }

    @Test
    fun `should fail when uid is blank`() {
        // Given
        val blankUid = ""

        // When & Then
        val exception =
            assertFailsWith<InvalidFeatureIdentifierException> {
                Feature(uid = blankUid)
            }
        assertEquals(blankUid, exception.featureUid)
        assertEquals("UID cannot be blank", exception.reason)
    }

    @Test
    fun `should fail when uid contains only whitespace`() {
        // Given
        val whitespaceUid = "   "

        // When & Then
        val exception =
            assertFailsWith<InvalidFeatureIdentifierException> {
                Feature(uid = whitespaceUid)
            }
        assertEquals(whitespaceUid, exception.featureUid)
        assertEquals("UID cannot be blank", exception.reason)
    }

    @Test
    fun `should enable feature`() {
        // Given
        val feature = disabledFeature()

        // When
        val enabled = feature.enable()

        // Then
        assertFalse(feature.enabled)
        assertTrue(enabled.enabled)
        assertEquals(feature.uid, enabled.uid)
    }

    @Test
    fun `should disable feature`() {
        // Given
        val feature = enabledFeature()

        // When
        val disabled = feature.disable()

        // Then
        assertTrue(feature.enabled)
        assertFalse(disabled.enabled)
        assertEquals(feature.uid, disabled.uid)
    }

    @Test
    fun `hasPermissions should return true when permissions exist`() {
        // Given
        val feature = featureWithPermissions()

        // When
        val result = feature.hasPermissions()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasPermissions should return false when no permissions`() {
        // Given
        val feature = basicFeature()

        // When
        val result = feature.hasPermissions()

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasGroup should return true when group exists`() {
        // Given
        val feature = featureWithGroup()

        // When
        val result = feature.hasGroup()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasGroup should return false when group is null`() {
        // Given
        val feature = basicFeature()

        // When
        val result = feature.hasGroup()

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasGroup should return false when group is blank`() {
        // Given
        val uid = "feature1"
        val blankGroup = "   "

        // When
        val feature = Feature(uid = uid, group = blankGroup)
        val result = feature.hasGroup()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should serialize and deserialize feature`() {
        // Given
        val feature = fullFeature()

        // When
        val json = Json.encodeToString(feature)
        val deserialized = Json.decodeFromString<Feature>(json)

        // Then
        assertEquals(feature, deserialized)
    }

    @Test
    fun `should serialize feature with minimal properties`() {
        // Given
        val feature = basicFeature()

        // When
        val json = Json.encodeToString(feature)

        // Then
        assertTrue(json.contains("\"uid\":\"feature1\""))
        assertTrue(json.contains("\"enabled\":false"))
    }

    @Test
    fun `should handle copy with modifications`() {
        // Given
        val original = disabledFeature()
        val newDescription = "Modified"

        // When
        val modified =
            original.copy(enabled = true, description = newDescription)

        // Then
        assertEquals("feature1", modified.uid)
        assertTrue(modified.enabled)
        assertEquals(newDescription, modified.description)
        assertFalse(original.enabled)
    }

    @Test
    fun `should respect data class equality`() {
        // Given
        val feature1 = enabledFeature()
        val feature2 = enabledFeature()
        val feature3 = disabledFeature()

        // When
        val equals12 = feature1 == feature2
        val equals13 = feature1 == feature3

        // Then
        assertTrue(equals12)
        assertFalse(equals13)
    }

    @Test
    fun `should have consistent hashCode`() {
        // Given
        val feature1 = enabledFeature()
        val feature2 = enabledFeature()

        // When
        val hash1 = feature1.hashCode()
        val hash2 = feature2.hashCode()

        // Then
        assertEquals(hash1, hash2)
    }

    @Test
    fun `should support empty permissions set`() {
        // Given
        val uid = "feature1"
        val emptyPermissions = emptySet<String>()

        // When
        val feature = Feature(uid = uid, permissions = emptyPermissions)

        // Then
        assertTrue(feature.permissions.isEmpty())
        assertFalse(feature.hasPermissions())
    }

    @Test
    fun `should maintain permissions immutability`() {
        // Given
        val uid = "feature1"
        val permissions = setOf("ADMIN", "USER")

        // When
        val feature = Feature(uid = uid, permissions = permissions)

        // Then
        assertEquals(permissions, feature.permissions)
    }

    @Test
    fun `should create feature with empty custom properties by default`() {
        // Given & When
        val feature = basicFeature()

        // Then
        assertTrue(feature.customProperties.isEmpty())
        assertFalse(feature.hasCustomProperties())
    }

    @Test
    fun `should create feature with custom properties`() {
        // Given
        val uid = "feature1"
        val properties = mapOf(
            "maxRetries" to PropertyInt(name = "maxRetries", value = 3),
            "timeout" to PropertyLong(name = "timeout", value = 5000L),
        )

        // When
        val feature = Feature(uid = uid, customProperties = properties)

        // Then
        assertEquals(properties, feature.customProperties)
        assertTrue(feature.hasCustomProperties())
        assertEquals(2, feature.customProperties.size)
    }

    @Test
    fun `hasCustomProperties should return true when properties exist`() {
        // Given
        val feature = Feature(
            uid = "feature1",
            customProperties = mapOf(
                "enabled" to PropertyBoolean(name = "enabled", value = true),
            ),
        )

        // When
        val result = feature.hasCustomProperties()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasCustomProperties should return false when no properties`() {
        // Given
        val feature = basicFeature()

        // When
        val result = feature.hasCustomProperties()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should serialize and deserialize feature with custom properties`() {
        // Given
        val feature = featureWithCustomProperties()

        // When
        val json = Json.encodeToString(feature)
        val deserialized = Json.decodeFromString<Feature>(json)

        // Then
        assertEquals(feature.uid, deserialized.uid)
        assertEquals(feature.customProperties.size, deserialized.customProperties.size)
        assertEquals(
            (feature.customProperties["maxRetries"] as PropertyInt).value,
            (deserialized.customProperties["maxRetries"] as PropertyInt).value,
        )
        assertEquals(
            (feature.customProperties["message"] as PropertyString).value,
            (deserialized.customProperties["message"] as PropertyString).value,
        )
    }

    @Test
    fun `should copy feature with modified custom properties`() {
        // Given
        val original = Feature(
            uid = "feature1",
            customProperties = mapOf(
                "count" to PropertyInt(name = "count", value = 1),
            ),
        )
        val newProperties = mapOf(
            "count" to PropertyInt(name = "count", value = 2),
        )

        // When
        val modified = original.copy(customProperties = newProperties)

        // Then
        assertEquals(
            1,
            (original.customProperties["count"] as PropertyInt).value,
        )
        assertEquals(
            2,
            (modified.customProperties["count"] as PropertyInt).value,
        )
    }

    @Test
    fun `should create feature without flipping strategy by default`() {
        // Given & When
        val feature = basicFeature()

        // Then
        assertNull(feature.flippingStrategy)
        assertFalse(feature.hasFlippingStrategy())
    }

    @Test
    fun `should create feature with flipping strategy`() {
        // Given
        val uid = "feature1"
        val strategy = AlwaysOnStrategy

        // When
        val feature = Feature(uid = uid, flippingStrategy = strategy)

        // Then
        assertEquals(strategy, feature.flippingStrategy)
        assertTrue(feature.hasFlippingStrategy())
    }

    @Test
    fun `should serialize and deserialize feature without flipping strategy`() {
        // Given
        val feature = Feature(
            uid = "feature1",
            enabled = true,
            flippingStrategy = null,
        )

        // When
        val json = Json.encodeToString(feature)
        val deserialized = Json.decodeFromString<Feature>(json)

        // Then
        assertEquals(feature.uid, deserialized.uid)
        assertEquals(feature.enabled, deserialized.enabled)
        assertNull(deserialized.flippingStrategy)
        assertFalse(json.contains("flippingStrategy"))
    }

    @Test
    fun `should serialize and deserialize feature with AlwaysOnStrategy`() {
        // Given
        val feature = Feature(
            uid = "feature1",
            enabled = true,
            flippingStrategy = AlwaysOnStrategy,
        )

        // When
        val json = Json.encodeToString(feature)
        val deserialized = Json.decodeFromString<Feature>(json)

        // Then
        assertEquals(feature.uid, deserialized.uid)
        assertEquals(feature.enabled, deserialized.enabled)
        assertEquals(feature.flippingStrategy, deserialized.flippingStrategy)
        assertTrue(json.contains("always_on"))
    }

    @Test
    fun `should serialize and deserialize feature with AlwaysOffStrategy`() {
        // Given
        val feature = Feature(
            uid = "feature1",
            enabled = false,
            flippingStrategy = AlwaysOffStrategy,
        )

        // When
        val json = Json.encodeToString(feature)
        val deserialized = Json.decodeFromString<Feature>(json)

        // Then
        assertEquals(feature.uid, deserialized.uid)
        assertEquals(feature.enabled, deserialized.enabled)
        assertEquals(feature.flippingStrategy, deserialized.flippingStrategy)
        assertTrue(json.contains("always_off"))
    }

    @Test
    fun `should copy feature with modified flipping strategy`() {
        // Given
        val original = Feature(
            uid = "feature1",
            flippingStrategy = AlwaysOnStrategy,
        )

        // When
        val modified = original.copy(flippingStrategy = AlwaysOffStrategy)

        // Then
        assertEquals(AlwaysOnStrategy, original.flippingStrategy)
        assertEquals(AlwaysOffStrategy, modified.flippingStrategy)
    }

    @Test
    fun `should copy feature to remove flipping strategy`() {
        // Given
        val original = Feature(
            uid = "feature1",
            enabled = true,
            flippingStrategy = AlwaysOffStrategy,
        )

        // When
        val modified = original.copy(flippingStrategy = null)

        // Then
        assertTrue(original.hasFlippingStrategy())
        assertFalse(modified.hasFlippingStrategy())
        assertNull(modified.flippingStrategy)
    }
}
