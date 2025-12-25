package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.property.Property
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for persisting and retrieving properties.
 *
 * @author Yonatan Karp-Rudin
 */
interface PropertyStore {
    /**
     * Check if a property exists.
     *
     * @param propertyName Property name
     * @return true if property exists
     */
    suspend operator fun contains(propertyName: String): Boolean

    /**
     * Create a new property.
     *
     * @param property Property to create
     * @throws PropertyAlreadyExistException if property already exists
     */
    suspend operator fun plusAssign(property: Property<*>)

    /**
     * Read a property by name.
     *
     * @param propertyName Property name
     * @return Property if found, null otherwise
     */
    suspend operator fun get(propertyName: String): Property<*>?

    /**
     * Update or create a property.
     *
     * @param propertyName Property name
     * @param property Property with updated values
     */
    suspend operator fun set(
        propertyName: String,
        property: Property<*>,
    )

    /**
     * Get all properties.
     *
     * @return Map of property names to Property objects
     */
    suspend fun getAll(): Map<String, Property<*>>

    /**
     * Delete a property.
     *
     * @param propertyName Property name
     * @throws PropertyNotFoundException if property does not exist
     */
    suspend operator fun minusAssign(propertyName: String)

    /**
     * Remove all properties.
     */
    suspend fun clear()

    /**
     * Import a collection of properties.
     *
     * @param properties Properties to import
     */
    suspend fun importProperties(properties: Collection<Property<*>>)

    /**
     * Initialize the database schema if needed.
     */
    suspend fun createSchema()

    /**
     * Observe changes to the property store.
     *
     * @return Flow of PropertyStoreEvent
     */
    fun observeChanges(): Flow<PropertyStoreEvent>
}
