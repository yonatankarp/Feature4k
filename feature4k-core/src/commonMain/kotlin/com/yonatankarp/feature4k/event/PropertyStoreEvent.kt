package com.yonatankarp.feature4k.event

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Events emitted by property stores for auditing and observation.
 *
 * These events capture the lifecycle of properties including creation, updates, and deletion.
 *
 * For property events, the [uid] field contains the property name identifier.
 *
 * @see Feature4KEvent
 * @author Yonatan Karp-Rudin
 */
@Serializable
sealed interface PropertyStoreEvent : Feature4KEvent {

    /**
     * Event emitted when a property is created in the store.
     */
    @Serializable
    data class Created(
        override val uid: String,
        override val eventUid: String,
        override val timestamp: Instant,
        override val user: String?,
        override val source: String?,
        override val host: String?,
        override val duration: Long?,
        override val value: String?,
        override val customProperties: Map<String, String>,
    ) : PropertyStoreEvent

    /**
     * Event emitted when a property is updated in the store.
     */
    @Serializable
    data class Updated(
        override val uid: String,
        override val eventUid: String,
        override val timestamp: Instant,
        override val user: String?,
        override val source: String?,
        override val host: String?,
        override val duration: Long?,
        override val value: String?,
        override val customProperties: Map<String, String>,
    ) : PropertyStoreEvent

    /**
     * Event emitted when a property is deleted from the store.
     */
    @Serializable
    data class Deleted(
        override val uid: String,
        override val eventUid: String,
        override val timestamp: Instant,
        override val user: String?,
        override val source: String?,
        override val host: String?,
        override val duration: Long?,
        override val value: String?,
        override val customProperties: Map<String, String>,
    ) : PropertyStoreEvent
}
