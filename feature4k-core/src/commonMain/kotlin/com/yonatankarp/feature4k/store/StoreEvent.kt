package com.yonatankarp.feature4k.store

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Base interface for all store events.
 *
 * Store events serve dual purposes:
 * 1. Real-time observation of changes through Flow (cache invalidation, UI updates)
 * 2. Persistent audit trail with full metadata (user, timestamp, source, etc.)
 *
 * Each event includes rich audit metadata for traceability and analytics.
 *
 * @property uid Identifier of the entity being acted upon
 * @property eventUid Unique identifier for this event instance
 * @property timestamp When the event occurred
 * @property user User who triggered the event (optional)
 * @property source Source system that generated the event (e.g., "WEB_API", "JAVA_API")
 * @property host Hostname where the event was generated (optional)
 * @property duration Duration of the operation in milliseconds (optional)
 * @property value Additional value associated with the event (optional)
 * @property customProperties Additional custom metadata
 *
 * @see FeatureStoreEvent
 * @see PropertyStoreEvent
 * @author Yonatan Karp-Rudin
 */
@Serializable
sealed interface StoreEvent {
    val uid: String
    val eventUid: String
    val timestamp: Instant
    val user: String?
    val source: String?
    val host: String?
    val duration: Long?
    val value: String?
    val customProperties: Map<String, String>
}
