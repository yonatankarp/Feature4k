package com.yonatankarp.feature4k.core

import com.yonatankarp.feature4k.audit.EventPublisher
import com.yonatankarp.feature4k.core.FeatureFixtures.basicFeature
import com.yonatankarp.feature4k.core.FeatureFixtures.disabledFeature
import com.yonatankarp.feature4k.core.FeatureFixtures.enabledFeature
import com.yonatankarp.feature4k.core.FeatureFixtures.featureWithAlwaysOffStrategy
import com.yonatankarp.feature4k.core.FeatureFixtures.featureWithAlwaysOnStrategy
import com.yonatankarp.feature4k.core.IdentifierFixtures.NON_EXISTENT
import com.yonatankarp.feature4k.core.IdentifierFixtures.PROPERTY_UID
import com.yonatankarp.feature4k.exception.FeatureAlreadyExistException
import com.yonatankarp.feature4k.exception.FeatureNotFoundException
import com.yonatankarp.feature4k.exception.PropertyAlreadyExistException
import com.yonatankarp.feature4k.exception.PropertyNotFoundException
import com.yonatankarp.feature4k.property.PropertyInt
import com.yonatankarp.feature4k.property.PropertyString
import com.yonatankarp.feature4k.security.DefaultAuthorizationManager
import com.yonatankarp.feature4k.security.PermissionFixtures.READ_PERMISSIONS
import com.yonatankarp.feature4k.store.FeatureStoreEvent
import com.yonatankarp.feature4k.store.StoreEvent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive tests for Feature4K facade.
 *
 * Tests cover:
 * - Feature check() evaluation with strategies
 * - Authorization integration
 * - Event publishing
 * - Feature CRUD operations
 * - Property management
 * - Auto-creation behavior
 *
 * @author Yonatan Karp-Rudin
 */
class Feature4KTest {

    @Test
    fun `check should return false for non-existent feature`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When
        val result = feature4k[NON_EXISTENT]

        // Then
        assertFalse(result)
    }

    @Test
    fun `check should create disabled feature when auto-create is enabled`() = runTest {
        // Given
        val feature4k = Feature4K(autoCreate = true)
        val featureId = "new-feature"

        // When
        val result = feature4k[featureId]

        // Then
        assertFalse(result)
        assertTrue(feature4k.exists(featureId))
        val created = feature4k.feature(featureId)
        assertFalse(created.enabled)
    }

    @Test
    fun `check should return false when feature is disabled`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(disabledFeature())

        // When
        val result = feature4k["feature1"]

        // Then
        assertFalse(result)
    }

    @Test
    fun `check should return true when feature is enabled and has no strategy`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(enabledFeature())

        // When
        val result = feature4k["feature1"]

        // Then
        assertTrue(result)
    }

    @Test
    fun `check should respect AlwaysOnStrategy`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(featureWithAlwaysOnStrategy())

        // When
        val result = feature4k["feature1"]

        // Then
        assertTrue(result)
    }

    @Test
    fun `check should respect AlwaysOffStrategy even when enabled`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(featureWithAlwaysOffStrategy())

        // When
        val result = feature4k["feature1"]

        // Then
        assertFalse(result)
    }

    @Test
    fun `check should return false when user lacks required permissions`() = runTest {
        // Given
        val authManager = DefaultAuthorizationManager(
            currentUserName = "test-user",
            currentUserPermissions = READ_PERMISSIONS,
        )
        val feature4k = Feature4K(authorizationsManager = authManager)
        feature4k.create(enabledFeature().copy(permissions = setOf("ADMIN")))

        // When
        val result = feature4k["feature1"]

        // Then
        assertFalse(result)
    }

    @Test
    fun `check should return true when user has all required permissions`() = runTest {
        // Given
        val authManager = DefaultAuthorizationManager(
            currentUserName = "test-user",
            currentUserPermissions = setOf("ADMIN"),
        )
        val feature4k = Feature4K(authorizationsManager = authManager)
        feature4k.create(enabledFeature().copy(permissions = setOf("ADMIN")))

        // When
        val result = feature4k["feature1"]

        // Then
        assertTrue(result)
    }

    @Test
    fun `check should return true when feature has no permissions`() = runTest {
        // Given
        val authManager = DefaultAuthorizationManager(
            currentUserName = "test-user",
            currentUserPermissions = READ_PERMISSIONS,
        )
        val feature4k = Feature4K(authorizationsManager = authManager)
        feature4k.create(enabledFeature())

        // When
        val result = feature4k["feature1"]

        // Then
        assertTrue(result)
    }

    @Test
    fun `check should publish event when publisher is configured`() = runTest {
        // Given
        val events = mutableListOf<StoreEvent>()
        val publisher = EventPublisher { events.add(it) }
        val feature4k = Feature4K(eventPublisher = publisher)
        feature4k.create(enabledFeature())

        // When
        feature4k["feature1"]

        // Then
        assertEquals(1, events.size)
        val event = events[0] as FeatureStoreEvent.Checked
        assertEquals("feature1", event.uid)
        assertEquals("true", event.value)
    }

    @Test
    fun `create should add feature to store`() = runTest {
        // Given
        val feature4k = Feature4K()
        val feature = basicFeature()

        // When
        feature4k.create(feature)

        // Then
        assertTrue(feature4k.exists("feature1"))
        assertEquals(feature, feature4k.feature("feature1"))
    }

    @Test
    fun `create should throw exception when feature already exists`() = runTest {
        // Given
        val feature4k = Feature4K()
        val feature = basicFeature()
        feature4k.create(feature)

        // When & Then
        assertFailsWith<FeatureAlreadyExistException> {
            feature4k.create(feature)
        }
    }

    @Test
    fun `enable should enable existing feature`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(disabledFeature())

        // When
        feature4k.enable("feature1")

        // Then
        val enabled = feature4k.feature("feature1")
        assertTrue(enabled.enabled)
    }

    @Test
    fun `enable should throw exception for non-existent feature`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            feature4k.enable(NON_EXISTENT)
        }
    }

    @Test
    fun `disable should disable existing feature`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(enabledFeature())

        // When
        feature4k.disable("feature1")

        // Then
        val disabled = feature4k.feature("feature1")
        assertFalse(disabled.enabled)
    }

    @Test
    fun `disable should throw exception for non-existent feature`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            feature4k.disable(NON_EXISTENT)
        }
    }

    @Test
    fun `update should modify existing feature`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(basicFeature())
        val updated = basicFeature().copy(
            enabled = true,
            description = "Updated description",
        )

        // When
        feature4k.update(updated)

        // Then
        val retrieved = feature4k.feature("feature1")
        assertTrue(retrieved.enabled)
        assertEquals("Updated description", retrieved.description)
    }

    @Test
    fun `update should create feature if it does not exist`() = runTest {
        // Given
        val feature4k = Feature4K()
        val feature = basicFeature()

        // When
        feature4k.update(feature)

        // Then
        assertTrue(feature4k.exists("feature1"))
        assertEquals(feature, feature4k.feature("feature1"))
    }

    @Test
    fun `delete should remove existing feature`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(basicFeature())

        // When
        feature4k.delete("feature1")

        // Then
        assertFalse(feature4k.exists("feature1"))
    }

    @Test
    fun `delete should throw exception for non-existent feature`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            feature4k.delete(NON_EXISTENT)
        }
    }

    @Test
    fun `feature should retrieve existing feature`() = runTest {
        // Given
        val feature4k = Feature4K()
        val created = basicFeature()
        feature4k.create(created)

        // When
        val retrieved = feature4k.feature("feature1")

        // Then
        assertEquals(created, retrieved)
    }

    @Test
    fun `feature should throw exception for non-existent feature without auto-create`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            feature4k.feature(NON_EXISTENT)
        }
    }

    @Test
    fun `feature should create feature when auto-create is enabled`() = runTest {
        // Given
        val feature4k = Feature4K(autoCreate = true)
        val featureId = "new-feature"

        // When
        val feature = feature4k.feature(featureId)

        // Then
        assertEquals(featureId, feature.uid)
        assertFalse(feature.enabled)
        assertTrue(feature4k.exists(featureId))
    }

    @Test
    fun `exists should return true for existing feature`() = runTest {
        // Given
        val feature4k = Feature4K()
        feature4k.create(basicFeature())

        // When
        val result = feature4k.exists("feature1")

        // Then
        assertTrue(result)
    }

    @Test
    fun `exists should return false for non-existent feature`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When
        val result = feature4k.exists(NON_EXISTENT)

        // Then
        assertFalse(result)
    }

    @Test
    fun `allFeatures should return all features`() = runTest {
        // Given
        val feature4k = Feature4K()
        val feature1 = Feature(uid = "feature1", enabled = true)
        val feature2 = Feature(uid = "feature2", enabled = false)
        feature4k.create(feature1)
        feature4k.create(feature2)

        // When
        val all = feature4k.allFeatures()

        // Then
        assertEquals(2, all.size)
        assertEquals(feature1, all["feature1"])
        assertEquals(feature2, all["feature2"])
    }

    @Test
    fun `allFeatures should return empty map when no features`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When
        val all = feature4k.allFeatures()

        // Then
        assertTrue(all.isEmpty())
    }

    @Test
    fun `createProperty should add property to store`() = runTest {
        // Given
        val feature4k = Feature4K()
        val property = PropertyString(name = PROPERTY_UID, value = "test-value")

        // When
        feature4k.createProperty(property)

        // Then
        assertEquals(property, feature4k.property(PROPERTY_UID))
    }

    @Test
    fun `createProperty should throw exception when property already exists`() = runTest {
        // Given
        val feature4k = Feature4K()
        val property = PropertyString(name = PROPERTY_UID, value = "test-value")
        feature4k.createProperty(property)

        // When & Then
        assertFailsWith<PropertyAlreadyExistException> {
            feature4k.createProperty(property)
        }
    }

    @Test
    fun `property should retrieve existing property`() = runTest {
        // Given
        val feature4k = Feature4K()
        val created = PropertyString(name = PROPERTY_UID, value = "test-value")
        feature4k.createProperty(created)

        // When
        val retrieved = feature4k.property(PROPERTY_UID)

        // Then
        assertEquals(created, retrieved)
    }

    @Test
    fun `property should throw exception for non-existent property`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When & Then
        assertFailsWith<PropertyNotFoundException> {
            feature4k.property(NON_EXISTENT)
        }
    }

    @Test
    fun `updateProperty should modify existing property`() = runTest {
        // Given
        val feature4k = Feature4K()
        val original = PropertyString(name = PROPERTY_UID, value = "old-value")
        feature4k.createProperty(original)
        val updated = PropertyString(name = PROPERTY_UID, value = "new-value")

        // When
        feature4k.updateProperty(updated)

        // Then
        val retrieved = feature4k.property(PROPERTY_UID) as PropertyString
        assertEquals("new-value", retrieved.value)
    }

    @Test
    fun `updateProperty should create property if it does not exist`() = runTest {
        // Given
        val feature4k = Feature4K()
        val property = PropertyString(name = PROPERTY_UID, value = "test-value")

        // When
        feature4k.updateProperty(property)

        // Then
        assertEquals(property, feature4k.property(PROPERTY_UID))
    }

    @Test
    fun `deleteProperty should remove existing property`() = runTest {
        // Given
        val feature4k = Feature4K()
        val property = PropertyString(name = PROPERTY_UID, value = "test-value")
        feature4k.createProperty(property)

        // When
        feature4k.deleteProperty(PROPERTY_UID)

        // Then
        assertFailsWith<PropertyNotFoundException> {
            feature4k.property(PROPERTY_UID)
        }
    }

    @Test
    fun `deleteProperty should throw exception for non-existent property`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When & Then
        assertFailsWith<PropertyNotFoundException> {
            feature4k.deleteProperty(NON_EXISTENT)
        }
    }

    @Test
    fun `allProperties should return all properties`() = runTest {
        // Given
        val feature4k = Feature4K()
        val prop1 = PropertyString(name = "prop1", value = "value1")
        val prop2 = PropertyInt(name = "prop2", value = 42)
        feature4k.createProperty(prop1)
        feature4k.createProperty(prop2)

        // When
        val all = feature4k.allProperties()

        // Then
        assertEquals(2, all.size)
        assertEquals(prop1, all["prop1"])
        assertEquals(prop2, all["prop2"])
    }

    @Test
    fun `allProperties should return empty map when no properties`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When
        val all = feature4k.allProperties()

        // Then
        assertTrue(all.isEmpty())
    }

    @Test
    fun `CRUD methods should support method chaining`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When
        val result = feature4k
            .create(Feature(uid = "feature1", enabled = false))
            .enable("feature1")
            .disable("feature1")
            .delete("feature1")

        // Then
        assertEquals(feature4k, result)
    }

    @Test
    fun `property methods should support method chaining`() = runTest {
        // Given
        val feature4k = Feature4K()

        // When
        val result = feature4k
            .createProperty(PropertyString(name = PROPERTY_UID, value = "value1"))
            .updateProperty(PropertyString(name = PROPERTY_UID, value = "value2"))
            .deleteProperty(PROPERTY_UID)

        // Then
        assertEquals(feature4k, result)
    }
}
