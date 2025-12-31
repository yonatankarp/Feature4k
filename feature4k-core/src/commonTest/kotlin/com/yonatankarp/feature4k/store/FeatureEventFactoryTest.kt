package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.audit.AuditFixtures.ADMIN_USER
import com.yonatankarp.feature4k.audit.AuditFixtures.LOCALHOST
import com.yonatankarp.feature4k.audit.AuditFixtures.WEB_API_SOURCE
import com.yonatankarp.feature4k.core.IdentifierFixtures.FEATURE_UID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for FeatureEventFactory.
 *
 * @author Yonatan Karp-Rudin
 */
@Suppress("USELESS_IS_CHECK")
class FeatureEventFactoryTest {
    @Test
    fun `default factory creates events without audit metadata`() {
        // Given
        val factory = FeatureEventFactory.Default

        // When
        val event = factory.created(FEATURE_UID)

        // Then
        assertEquals(FEATURE_UID, event.uid)
        assertNull(event.source)
        assertNull(event.user)
        assertNull(event.host)
        assertNotNull(event.eventUid)
        assertNotNull(event.timestamp)
    }

    @Test
    fun `factory with metadata propagates audit fields to created events`() {
        // Given
        val factory = FeatureEventFactory(
            source = WEB_API_SOURCE,
            user = ADMIN_USER,
            host = LOCALHOST,
        )

        // When
        val event = factory.created(FEATURE_UID)

        // Then
        assertEquals(FEATURE_UID, event.uid)
        assertEquals(WEB_API_SOURCE, event.source)
        assertEquals(ADMIN_USER, event.user)
        assertEquals(LOCALHOST, event.host)
    }

    @Test
    fun `created method creates FeatureCreated event with custom properties`() {
        // Given
        val factory = FeatureEventFactory(source = "TEST")
        val customProps = mapOf("key1" to "value1", "key2" to "value2")

        // When
        val event = factory.created("feature1", value = "enabled", customProperties = customProps)

        // Then
        assertTrue(event is FeatureStoreEvent.Created)
        assertEquals("feature1", event.uid)
        assertEquals("enabled", event.value)
        assertEquals(customProps, event.customProperties)
        assertEquals("TEST", event.source)
    }

    @Test
    fun `updated method creates FeatureUpdated event`() {
        // Given
        val factory = FeatureEventFactory(user = "testUser")

        // When
        val event = factory.updated("feature2")

        // Then
        assertTrue(event is FeatureStoreEvent.Updated)
        assertEquals("feature2", event.uid)
        assertEquals("testUser", event.user)
    }

    @Test
    fun `deleted method creates FeatureDeleted event`() {
        // Given
        val factory = FeatureEventFactory()

        // When
        val event = factory.deleted("feature3")

        // Then
        assertTrue(event is FeatureStoreEvent.Deleted)
        assertEquals("feature3", event.uid)
    }

    @Test
    fun `enabled method creates FeatureEnabled event`() {
        // Given
        val factory = FeatureEventFactory(host = "server1")

        // When
        val event = factory.enabled("feature4")

        // Then
        assertTrue(event is FeatureStoreEvent.Enabled)
        assertEquals("feature4", event.uid)
        assertEquals("server1", event.host)
    }

    @Test
    fun `disabled method creates FeatureDisabled event`() {
        // Given
        val factory = FeatureEventFactory()

        // When
        val event = factory.disabled("feature5")

        // Then
        assertTrue(event is FeatureStoreEvent.Disabled)
        assertEquals("feature5", event.uid)
    }

    @Test
    fun `checked method creates FeatureChecked event with duration`() {
        // Given
        val factory = FeatureEventFactory(source = "MOBILE_APP")

        // When
        val event = factory.checked("feature6", value = "true", duration = 100L)

        // Then
        assertTrue(event is FeatureStoreEvent.Checked)
        assertEquals("feature6", event.uid)
        assertEquals("true", event.value)
        assertEquals(100L, event.duration)
        assertEquals("MOBILE_APP", event.source)
    }

    @Test
    fun `roleUpdated method creates FeatureRoleUpdated event`() {
        // Given
        val factory = FeatureEventFactory()

        // When
        val event = factory.roleUpdated("feature7", value = "ADMIN")

        // Then
        assertTrue(event is FeatureStoreEvent.RoleUpdated)
        assertEquals("feature7", event.uid)
        assertEquals("ADMIN", event.value)
    }

    @Test
    fun `roleDeleted method creates FeatureRoleDeleted event`() {
        // Given
        val factory = FeatureEventFactory()

        // When
        val event = factory.roleDeleted("feature8", value = "USER")

        // Then
        assertTrue(event is FeatureStoreEvent.RoleDeleted)
        assertEquals("feature8", event.uid)
        assertEquals("USER", event.value)
    }

    @Test
    fun `events are serializable to JSON`() {
        // Given
        val factory = FeatureEventFactory(source = "TEST", user = "testUser")
        val event = factory.created(FEATURE_UID, value = "test")

        // When
        val json = Json.encodeToString<FeatureStoreEvent>(event)
        val deserialized = Json.decodeFromString<FeatureStoreEvent>(json)

        // Then
        assertTrue(deserialized is FeatureStoreEvent.Created)
        assertEquals(event.uid, deserialized.uid)
        assertEquals(event.source, deserialized.source)
        assertEquals(event.user, deserialized.user)
        assertEquals(event.value, deserialized.value)
    }

    @Test
    fun `events with custom properties are serializable`() {
        // Given
        val factory = FeatureEventFactory()
        val customProps = mapOf("env" to "production", "region" to "us-west")
        val event = factory.updated("feature-prod", customProperties = customProps)

        // When
        val json = Json.encodeToString<FeatureStoreEvent>(event)
        val deserialized = Json.decodeFromString<FeatureStoreEvent>(json)

        // Then
        assertTrue(deserialized is FeatureStoreEvent.Updated)
        assertEquals(customProps, deserialized.customProperties)
    }
}
