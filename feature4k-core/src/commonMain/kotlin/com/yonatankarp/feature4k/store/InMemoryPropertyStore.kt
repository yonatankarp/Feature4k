package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.exception.PropertyAlreadyExistException
import com.yonatankarp.feature4k.exception.PropertyNotFoundException
import com.yonatankarp.feature4k.property.Property
import com.yonatankarp.feature4k.store.event.NoOpEventEmitter
import com.yonatankarp.feature4k.store.event.StoreEventEmitter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory implementation of [PropertyStore] using thread-safe collections.
 *
 * This implementation stores all properties in memory using a mutable map protected
 * by a [Mutex] for coroutine-safe concurrent access. Changes are published through
 * a [Flow] for observation.
 *
 * This implementation is suitable for:
 * - Development and testing
 * - Small-scale deployments
 * - Scenarios where persistence is not required
 *
 * For production use with persistence requirements, consider using a database-backed
 * store implementation.
 *
 * @property eventEmitter Event emitter for publishing store changes (defaults to [NoOpEventEmitter])
 * @author Yonatan Karp-Rudin
 */
class InMemoryPropertyStore(
    private val eventEmitter: StoreEventEmitter<PropertyStoreEvent> = NoOpEventEmitter(),
) : PropertyStore {
    private val properties = mutableMapOf<String, Property<*>>()
    private val mutex = Mutex()

    override suspend fun contains(propertyName: String): Boolean = mutex.withLock {
        propertyName in properties
    }

    override suspend fun plusAssign(property: Property<*>) {
        mutex.withLock {
            if (property.name in properties) {
                throw PropertyAlreadyExistException(property.name)
            }
            properties[property.name] = property
            eventEmitter.emit(PropertyStoreEvent.Created(property.name))
        }
    }

    override suspend fun get(propertyName: String): Property<*>? = mutex.withLock {
        properties[propertyName]
    }

    override suspend fun set(
        propertyName: String,
        property: Property<*>,
    ) {
        mutex.withLock {
            val isUpdate = propertyName in properties
            properties[propertyName] = property
            val event = if (isUpdate) PropertyStoreEvent.Updated(propertyName) else PropertyStoreEvent.Created(propertyName)
            eventEmitter.emit(event)
        }
    }

    override suspend fun getAll(): Map<String, Property<*>> = mutex.withLock {
        properties.toMap()
    }

    override suspend fun minusAssign(propertyName: String) {
        mutex.withLock {
            if (propertyName !in properties) {
                throw PropertyNotFoundException(propertyName)
            }
            properties.remove(propertyName)
            eventEmitter.emit(PropertyStoreEvent.Deleted(propertyName))
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            val propertyNames = properties.keys.toList()
            properties.clear()
            propertyNames.forEach { eventEmitter.emit(PropertyStoreEvent.Deleted(it)) }
        }
    }

    override suspend fun importProperties(properties: Collection<Property<*>>) {
        mutex.withLock {
            properties.forEach { property ->
                val isUpdate = this.properties.containsKey(property.name)
                this.properties[property.name] = property
                val event = if (isUpdate) PropertyStoreEvent.Updated(property.name) else PropertyStoreEvent.Created(property.name)
                eventEmitter.emit(event)
            }
        }
    }

    override suspend fun createSchema() {
        // No-op for in-memory store
    }

    override fun observeChanges(): Flow<PropertyStoreEvent> = eventEmitter.observe()
}
