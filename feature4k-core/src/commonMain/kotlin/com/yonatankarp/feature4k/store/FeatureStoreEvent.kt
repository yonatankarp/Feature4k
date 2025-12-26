package com.yonatankarp.feature4k.store

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Events emitted by feature stores for auditing and observation.
 *
 * These events capture the full lifecycle of features including creation, updates,
 * deletion, enable/disable state changes, permission updates, and feature checks.
 *
 * For feature events, the [uid] field maps to [Feature.uid][com.yonatankarp.feature4k.core.Feature.uid].
 *
 * @see StoreEvent
 * @author Yonatan Karp-Rudin
 */
@Serializable
sealed interface FeatureStoreEvent : StoreEvent {

    /**
     * Event emitted when a feature is created in the store.
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
    ) : FeatureStoreEvent

    /**
     * Event emitted when a feature is updated in the store.
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
    ) : FeatureStoreEvent

    /**
     * Event emitted when a feature is deleted from the store.
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
    ) : FeatureStoreEvent

    /**
     * Event emitted when a feature is explicitly enabled.
     */
    @Serializable
    data class Enabled(
        override val uid: String,
        override val eventUid: String,
        override val timestamp: Instant,
        override val user: String?,
        override val source: String?,
        override val host: String?,
        override val duration: Long?,
        override val value: String?,
        override val customProperties: Map<String, String>,
    ) : FeatureStoreEvent

    /**
     * Event emitted when a feature is explicitly disabled.
     */
    @Serializable
    data class Disabled(
        override val uid: String,
        override val eventUid: String,
        override val timestamp: Instant,
        override val user: String?,
        override val source: String?,
        override val host: String?,
        override val duration: Long?,
        override val value: String?,
        override val customProperties: Map<String, String>,
    ) : FeatureStoreEvent

    /**
     * Event emitted when a feature's enabled state is checked.
     * Used for audit trails and analytics to track feature usage patterns.
     */
    @Serializable
    data class Checked(
        override val uid: String,
        override val eventUid: String,
        override val timestamp: Instant,
        override val user: String?,
        override val source: String?,
        override val host: String?,
        override val duration: Long?,
        override val value: String?,
        override val customProperties: Map<String, String>,
    ) : FeatureStoreEvent

    /**
     * Event emitted when a feature's role/permission is updated.
     */
    @Serializable
    data class RoleUpdated(
        override val uid: String,
        override val eventUid: String,
        override val timestamp: Instant,
        override val user: String?,
        override val source: String?,
        override val host: String?,
        override val duration: Long?,
        override val value: String?,
        override val customProperties: Map<String, String>,
    ) : FeatureStoreEvent

    /**
     * Event emitted when a feature's role/permission is deleted.
     */
    @Serializable
    data class RoleDeleted(
        override val uid: String,
        override val eventUid: String,
        override val timestamp: Instant,
        override val user: String?,
        override val source: String?,
        override val host: String?,
        override val duration: Long?,
        override val value: String?,
        override val customProperties: Map<String, String>,
    ) : FeatureStoreEvent
}
