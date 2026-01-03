package com.yonatankarp.feature4k.audit

import com.yonatankarp.feature4k.event.Feature4KEvent
import kotlinx.datetime.Instant

/**
 * Repository interface for storing and querying audit events.
 *
 * EventRepository provides persistent storage for store events with support for
 * time-based queries, feature/property filtering, and full event history retrieval.
 *
 * All methods are suspend functions to support asynchronous I/O operations.
 *
 * @author Yonatan Karp-Rudin
 */
interface EventRepository {
    /**
     * Persists an event to the repository.
     *
     * @param event The event to save
     */
    suspend fun save(event: Feature4KEvent)

    /**
     * Retrieves all events within a time range.
     *
     * @param start Start of the time range (inclusive)
     * @param end End of the time range (inclusive)
     * @return List of events within the time range, ordered by timestamp
     */
    suspend fun findByTimeRange(
        start: Instant,
        end: Instant,
    ): List<Feature4KEvent>

    /**
     * Retrieves all events for a specific feature or property.
     *
     * @param uid The unique identifier of the feature or property
     * @return List of events for the specified entity, ordered by timestamp
     */
    suspend fun findByUid(uid: String): List<Feature4KEvent>

    /**
     * Retrieves all events in the repository.
     *
     * @return List of all events, ordered by timestamp
     */
    suspend fun findAll(): List<Feature4KEvent>

    /**
     * Retrieves events that match a custom filter predicate.
     *
     * This method allows for complex queries beyond the standard filters,
     * such as filtering by event type, user, source, or custom properties.
     *
     * @param predicate Filter function to apply to events
     * @return List of events matching the predicate, ordered by timestamp
     */
    suspend fun findBy(predicate: (Feature4KEvent) -> Boolean): List<Feature4KEvent>
}
