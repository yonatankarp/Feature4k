package com.yonatankarp.feature4k.event

import kotlinx.datetime.Instant

/**
 * Base interface for all Feature4K events.
 *
 * This interface is open for extension, allowing library users to create
 * their own custom event types for application-specific needs.
 *
 * Events serve dual purposes:
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
 * @see com.yonatankarp.feature4k.event.FeatureStoreEvent
 * @see com.yonatankarp.feature4k.event.PropertyStoreEvent
 * @author Yonatan Karp-Rudin
 */
interface Feature4KEvent {
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
