package com.yonatankarp.feature4k.audit

import com.yonatankarp.feature4k.audit.AuditFixtures.ADMIN_USER
import com.yonatankarp.feature4k.audit.AuditFixtures.LOCALHOST
import com.yonatankarp.feature4k.audit.AuditFixtures.REGULAR_USER
import com.yonatankarp.feature4k.audit.AuditFixtures.WEB_API_SOURCE
import com.yonatankarp.feature4k.audit.EventFixtures.eventWithCustomProperties
import com.yonatankarp.feature4k.audit.EventFixtures.eventWithOffset
import com.yonatankarp.feature4k.audit.EventFixtures.featureCreatedEvent
import com.yonatankarp.feature4k.audit.EventFixtures.featureUpdatedEvent
import com.yonatankarp.feature4k.audit.EventFixtures.fullAuditEvent
import com.yonatankarp.feature4k.audit.EventFixtures.propertyCreatedEvent
import com.yonatankarp.feature4k.core.IdentifierFixtures.FEATURE_UID
import com.yonatankarp.feature4k.core.IdentifierFixtures.NON_EXISTENT
import com.yonatankarp.feature4k.event.FeatureStoreEvent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Abstract test contract for EventRepository implementations.
 *
 * All EventRepository implementations must extend this class and implement [createRepository]
 * to ensure consistent behavior across different repository implementations.
 *
 * Tests cover:
 * - Event persistence (save)
 * - Time-based queries (findByTimeRange)
 * - Entity-based queries (findByUid)
 * - Custom filtering (findBy)
 * - Full retrieval (findAll)
 * - Edge cases (empty results, chronological ordering)
 *
 * @author Yonatan Karp-Rudin
 */
abstract class EventRepositoryContract {
    /**
     * Create a fresh instance of the EventRepository implementation to test.
     * This method is called before each test to ensure test isolation.
     */
    abstract suspend fun createRepository(): EventRepository

    @Test
    fun `should save and retrieve event`() = runTest {
        // Given
        val repository = createRepository()
        val event = featureCreatedEvent(uid = FEATURE_UID, user = "testuser")

        // When
        repository.save(event)

        // Then
        val all = repository.findAll()
        assertEquals(1, all.size)
        assertEquals(event.uid, all[0].uid)
        assertEquals(event.eventUid, all[0].eventUid)
    }

    @Test
    fun `should return empty list when no events exist`() = runTest {
        // Given
        val repository = createRepository()

        // When
        val result = repository.findAll()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should find events by time range`() = runTest {
        // Given
        val repository = createRepository()
        val now = Clock.System.now()
        val event1 = eventWithOffset("feature1", (-2).hours, baseTime = now)
        val event2 = eventWithOffset("feature2", (-1).hours, baseTime = now)
        val event3 = eventWithOffset("feature3", 0.hours, baseTime = now)

        repository.save(event1)
        repository.save(event2)
        repository.save(event3)

        // When
        val result = repository.findByTimeRange(start = now - 30.hours, end = now - 2.hours)

        // Then
        assertEquals(1, result.size)
        assertEquals("feature1", result[0].uid)
    }

    @Test
    fun `should find events by uid`() = runTest {
        // Given
        val repository = createRepository()
        val event1 = featureCreatedEvent(uid = FEATURE_UID)
        val event2 = featureUpdatedEvent(uid = FEATURE_UID)
        val event3 = featureCreatedEvent(uid = "other-feature")

        repository.save(event1)
        repository.save(event2)
        repository.save(event3)

        // When
        val result = repository.findByUid(FEATURE_UID)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.uid == FEATURE_UID })
    }

    @Test
    fun `should find events by custom filter`() = runTest {
        // Given
        val repository = createRepository()
        val event1 = featureCreatedEvent(user = ADMIN_USER, source = WEB_API_SOURCE)
        val event2 = featureUpdatedEvent(user = REGULAR_USER)
        val event3 = propertyCreatedEvent()

        repository.save(event1)
        repository.save(event2)
        repository.save(event3)

        // When
        val result = repository.findBy { it.user == ADMIN_USER }

        // Then
        assertEquals(1, result.size)
        assertEquals(ADMIN_USER, result[0].user)
    }

    @Test
    fun `should return events in chronological order`() = runTest {
        // Given
        val repository = createRepository()
        val event1 = eventWithOffset("feature1", 0.hours)
        val event2 = eventWithOffset("feature2", (-1).hours)
        val event3 = eventWithOffset("feature3", 1.hours)

        // Save in random order
        repository.save(event1)
        repository.save(event3)
        repository.save(event2)

        // When
        val result = repository.findAll()

        // Then
        assertEquals(3, result.size)
        assertTrue(result[0].timestamp <= result[1].timestamp)
        assertTrue(result[1].timestamp <= result[2].timestamp)
    }

    @Test
    fun `should handle time range with no matching events`() = runTest {
        // Given
        val repository = createRepository()
        val now = Clock.System.now()
        val event = featureCreatedEvent()
        repository.save(event)

        // When
        val result = repository.findByTimeRange(now - 10.days, now - 5.days)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should handle uid with no matching events`() = runTest {
        // Given
        val repository = createRepository()
        val event = featureCreatedEvent(uid = "feature1")
        repository.save(event)

        // When
        val result = repository.findByUid(NON_EXISTENT)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should handle filter with no matching events`() = runTest {
        // Given
        val repository = createRepository()
        val event = featureCreatedEvent(user = ADMIN_USER)
        repository.save(event)

        // When
        val result = repository.findBy { it.user == "unknown" }

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should support filtering by event type using sealed interface`() = runTest {
        // Given
        val repository = createRepository()
        val event1 = featureCreatedEvent(uid = "feature1")
        val event2 = featureUpdatedEvent(uid = "feature2")
        val event3 = featureCreatedEvent(uid = "feature3")

        repository.save(event1)
        repository.save(event2)
        repository.save(event3)

        // When
        val result = repository.findBy { it is FeatureStoreEvent.Created }

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it is FeatureStoreEvent.Created })
    }

    @Test
    fun `should support filtering by multiple criteria`() = runTest {
        // Given
        val repository = createRepository()
        val event1 = featureCreatedEvent(user = ADMIN_USER, source = WEB_API_SOURCE)
        val event2 = featureCreatedEvent(user = ADMIN_USER, source = "JAVA_API")
        val event3 = featureUpdatedEvent(user = REGULAR_USER)

        repository.save(event1)
        repository.save(event2)
        repository.save(event3)

        // When - Find all Created events by admin
        val result =
            repository.findBy {
                it is FeatureStoreEvent.Created && it.user == ADMIN_USER
            }

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it is FeatureStoreEvent.Created && it.user == ADMIN_USER })
    }

    @Test
    fun `should persist events with custom properties`() = runTest {
        // Given
        val repository = createRepository()
        val event = eventWithCustomProperties()
        repository.save(event)

        // When
        val result = repository.findAll()

        // Then
        assertEquals(1, result.size)
        assertEquals(2, result[0].customProperties.size)
        assertEquals("value1", result[0].customProperties["key1"])
        assertEquals("value2", result[0].customProperties["key2"])
    }

    @Test
    fun `should persist full audit metadata`() = runTest {
        // Given
        val repository = createRepository()
        val event = fullAuditEvent()
        repository.save(event)

        // When
        val result = repository.findAll()

        // Then
        assertEquals(1, result.size)
        val saved = result[0]
        assertEquals(ADMIN_USER, saved.user)
        assertEquals(WEB_API_SOURCE, saved.source)
        assertEquals(LOCALHOST, saved.host)
        assertEquals(100L, saved.duration)
    }
}
