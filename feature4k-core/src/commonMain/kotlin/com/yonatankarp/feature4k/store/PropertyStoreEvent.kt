package com.yonatankarp.feature4k.store

/**
 * Represents an event that occurred in the property store.
 *
 * @author Yonatan Karp-Rudin
 */
sealed class PropertyStoreEvent {
    abstract val propertyName: String

    /**
     * Event emitted when a property is created.
     */
    data class Created(override val propertyName: String) : PropertyStoreEvent()

    /**
     * Event emitted when a property is updated.
     */
    data class Updated(override val propertyName: String) : PropertyStoreEvent()

    /**
     * Event emitted when a property is deleted.
     */
    data class Deleted(override val propertyName: String) : PropertyStoreEvent()
}
