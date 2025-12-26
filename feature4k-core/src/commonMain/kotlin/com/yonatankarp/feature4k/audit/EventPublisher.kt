package com.yonatankarp.feature4k.audit

import com.yonatankarp.feature4k.store.StoreEvent

/**
 * Interface for publishing store events for auditing and persistence.
 *
 * EventPublisher provides an asynchronous way to emit events to audit trail storage.
 * This is separate from real-time event observation (handled by StoreEventEmitter)
 * and focuses on ensuring events are persisted for compliance and analytics.
 *
 * Implementations should handle errors gracefully - failures during event publishing
 * should be logged but should not interrupt the main operation flow.
 *
 * @author Yonatan Karp-Rudin
 */
interface EventPublisher {
    /**
     * Publishes an event for audit trail persistence.
     *
     * This is a suspend function that may perform I/O operations to persist the event.
     * Implementations should handle errors gracefully and not throw exceptions that
     * would interrupt the caller's operation.
     *
     * @param event The store event to publish
     */
    suspend fun publish(event: StoreEvent)
}
