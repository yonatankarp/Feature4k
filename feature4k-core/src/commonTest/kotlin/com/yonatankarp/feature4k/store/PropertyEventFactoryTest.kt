package com.yonatankarp.feature4k.store

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for PropertyEventFactory.
 *
 * @author Yonatan Karp-Rudin
 */
@Suppress("USELESS_IS_CHECK")
class PropertyEventFactoryTest {
    @Test
    fun `default factory creates events without audit metadata`() {
        // Given
        val factory = PropertyEventFactory.Default

        // When
        val event = factory.created("my-property")

        // Then
        assertEquals("my-property", event.uid)
        assertNull(event.source)
        assertNull(event.user)
        assertNull(event.host)
        assertNotNull(event.eventUid)
        assertNotNull(event.timestamp)
    }

    @Test
    fun `factory with metadata propagates audit fields to created events`() {
        // Given
        val factory = PropertyEventFactory(
            source = "WEB_API",
            user = "admin",
            host = "localhost",
        )

        // When
        val event = factory.created("my-property")

        // Then
        assertEquals("my-property", event.uid)
        assertEquals("WEB_API", event.source)
        assertEquals("admin", event.user)
        assertEquals("localhost", event.host)
    }

    @Test
    fun `created method creates PropertyCreated event with custom properties`() {
        // Given
        val factory = PropertyEventFactory(source = "TEST")
        val customProps = mapOf("key1" to "value1", "key2" to "value2")

        // When
        val event = factory.created("property1", value = "100", customProperties = customProps)

        // Then
        assertTrue(event is PropertyStoreEvent.Created)
        assertEquals("property1", event.uid)
        assertEquals("100", event.value)
        assertEquals(customProps, event.customProperties)
        assertEquals("TEST", event.source)
    }

    @Test
    fun `updated method creates PropertyUpdated event`() {
        // Given
        val factory = PropertyEventFactory(user = "testUser")

        // When
        val event = factory.updated("property2")

        // Then
        assertTrue(event is PropertyStoreEvent.Updated)
        assertEquals("property2", event.uid)
        assertEquals("testUser", event.user)
    }

    @Test
    fun `deleted method creates PropertyDeleted event`() {
        // Given
        val factory = PropertyEventFactory()

        // When
        val event = factory.deleted("property3")

        // Then
        assertTrue(event is PropertyStoreEvent.Deleted)
        assertEquals("property3", event.uid)
    }

    @Test
    fun `events with value are created correctly`() {
        // Given
        val factory = PropertyEventFactory(host = "server1")

        // When
        val event = factory.updated("property4", value = "new-value")

        // Then
        assertTrue(event is PropertyStoreEvent.Updated)
        assertEquals("property4", event.uid)
        assertEquals("new-value", event.value)
        assertEquals("server1", event.host)
    }

    @Test
    fun `events are serializable to JSON`() {
        // Given
        val factory = PropertyEventFactory(source = "TEST", user = "testUser")
        val event = factory.created("test-property", value = "test")

        // When
        val json = Json.encodeToString<PropertyStoreEvent>(event)
        val deserialized = Json.decodeFromString<PropertyStoreEvent>(json)

        // Then
        assertTrue(deserialized is PropertyStoreEvent.Created)
        assertEquals(event.uid, deserialized.uid)
        assertEquals(event.source, deserialized.source)
        assertEquals(event.user, deserialized.user)
        assertEquals(event.value, deserialized.value)
    }

    @Test
    fun `events with custom properties are serializable`() {
        // Given
        val factory = PropertyEventFactory()
        val customProps = mapOf("env" to "production", "region" to "us-west")
        val event = factory.deleted("property-prod", customProperties = customProps)

        // When
        val json = Json.encodeToString<PropertyStoreEvent>(event)
        val deserialized = Json.decodeFromString<PropertyStoreEvent>(json)

        // Then
        assertTrue(deserialized is PropertyStoreEvent.Deleted)
        assertEquals(customProps, deserialized.customProperties)
    }

    @Test
    fun `multiple events from same factory share audit metadata`() {
        // Given
        val factory = PropertyEventFactory(
            source = "BATCH_JOB",
            user = "system",
            host = "worker-1",
        )

        // When
        val event1 = factory.created("prop1")
        val event2 = factory.updated("prop2")
        val event3 = factory.deleted("prop3")

        // Then
        assertEquals("BATCH_JOB", event1.source)
        assertEquals("BATCH_JOB", event2.source)
        assertEquals("BATCH_JOB", event3.source)

        assertEquals("system", event1.user)
        assertEquals("system", event2.user)
        assertEquals("system", event3.user)

        assertEquals("worker-1", event1.host)
        assertEquals("worker-1", event2.host)
        assertEquals("worker-1", event3.host)
    }
}
