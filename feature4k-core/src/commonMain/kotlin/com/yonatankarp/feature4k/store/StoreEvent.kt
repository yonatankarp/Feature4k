package com.yonatankarp.feature4k.store

/**
 * Represents an event that occurred in the feature store.
 *
 * Store events enable real-time observation of changes through Flow,
 * supporting use cases like cache invalidation, audit logging, and UI updates.
 *
 * @author Yonatan Karp-Rudin
 */
sealed class StoreEvent {
    abstract val featureId: String

    /**
     * Event emitted when a feature is created.
     */
    data class Created(override val featureId: String) : StoreEvent()

    /**
     * Event emitted when a feature is updated.
     */
    data class Updated(override val featureId: String) : StoreEvent()

    /**
     * Event emitted when a feature is deleted.
     */
    data class Deleted(override val featureId: String) : StoreEvent()

    /**
     * Event emitted when a feature is enabled.
     */
    data class Enabled(override val featureId: String) : StoreEvent()

    /**
     * Event emitted when a feature is disabled.
     */
    data class Disabled(override val featureId: String) : StoreEvent()

    /**
     * Event emitted when a feature role is changed.
     */
    data class RoleUpdated(override val featureId: String) : StoreEvent()

    /**
     * Event emitted when a feature role is removed
     */
    data class RoleDeleted(override val featureId: String) : StoreEvent()
}
