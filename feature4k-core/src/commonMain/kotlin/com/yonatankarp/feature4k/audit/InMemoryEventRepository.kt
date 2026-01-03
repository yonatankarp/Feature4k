package com.yonatankarp.feature4k.audit

import com.yonatankarp.feature4k.event.Feature4KEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant

/**
 * In-memory implementation of [EventRepository] using thread-safe collections.
 *
 * This implementation stores all events in memory using a mutable list protected
 * by a [Mutex] for coroutine-safe concurrent access. Events are stored in insertion
 * order and queried efficiently using filtering operations, with results returned
 * in chronological order.
 *
 * This implementation is suitable for:
 * - Development and testing
 * - Small-scale deployments with limited event history
 * - Scenarios where event persistence across restarts is not required
 *
 * For production use with persistence requirements and large-scale event storage,
 * consider using a database-backed repository implementation.
 *
 * ## Performance Characteristics
 * - Save: O(1) amortized - append to list
 * - FindAll: O(n) - returns copy of entire event list
 * - FindByTimeRange: O(n) - filters all events
 * - FindByUid: O(n) - filters all events
 * - FindBy: O(n log n) - filters all events with custom predicate
 *
 * All query results are returned in chronological order (sorted by timestamp).
 *
 * @author Yonatan Karp-Rudin
 */
class InMemoryEventRepository : EventRepository {

    private val events = mutableListOf<Feature4KEvent>()
    private val mutex = Mutex()

    override suspend fun save(event: Feature4KEvent) {
        mutex.withLock {
            events.add(event)
        }
    }

    override suspend fun findByTimeRange(start: Instant, end: Instant): List<Feature4KEvent> = mutex.withLock {
        events
            .filter { it.timestamp in start..end }
            .sortedBy { it.timestamp }
    }

    override suspend fun findByUid(uid: String): List<Feature4KEvent> = mutex.withLock {
        events
            .filter { it.uid == uid }
            .sortedBy { it.timestamp }
    }

    override suspend fun findAll(): List<Feature4KEvent> = mutex.withLock {
        events.sortedBy { it.timestamp }
    }

    override suspend fun findBy(predicate: (Feature4KEvent) -> Boolean): List<Feature4KEvent> = mutex.withLock {
        events
            .filter(predicate)
            .sortedBy { it.timestamp }
    }
}
