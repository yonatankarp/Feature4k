package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.exception.PropertyAlreadyExistException
import com.yonatankarp.feature4k.exception.PropertyNotFoundException
import com.yonatankarp.feature4k.property.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
 * @property eventReplay Number of recent events to replay to new observers (default: 10)
 * @property eventBufferCapacity Extra buffer capacity for events beyond replay (default: 64)
 * @author Yonatan Karp-Rudin
 */
class InMemoryPropertyStore(
    private val eventReplay: Int = 10,
    private val eventBufferCapacity: Int = 64,
) : PropertyStore {
    private val properties = mutableMapOf<String, Property<*>>()
    private val mutex = Mutex()
    private val changeEvents = MutableSharedFlow<PropertyStoreEvent>(
        replay = eventReplay,
        extraBufferCapacity = eventBufferCapacity,
    )

    override suspend fun contains(propertyName: String): Boolean = mutex.withLock {
        propertyName in properties
    }

    override suspend fun plusAssign(property: Property<*>) {
        mutex.withLock {
            if (property.name in properties) {
                throw PropertyAlreadyExistException(property.name)
            }
            properties[property.name] = property
        }
        changeEvents.emit(PropertyStoreEvent.Created(property.name))
    }

    override suspend fun get(propertyName: String): Property<*>? = mutex.withLock {
        properties[propertyName]
    }

    override suspend fun set(
        propertyName: String,
        property: Property<*>,
    ) {
        val event = mutex.withLock {
            val isUpdate = propertyName in properties
            properties[propertyName] = property
            if (isUpdate) PropertyStoreEvent.Updated(propertyName) else PropertyStoreEvent.Created(propertyName)
        }
        changeEvents.emit(event)
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
        }
        changeEvents.emit(PropertyStoreEvent.Deleted(propertyName))
    }

    override suspend fun clear() {
        val events = mutex.withLock {
            val propertyNames = properties.keys.toList()
            properties.clear()
            propertyNames.map { PropertyStoreEvent.Deleted(it) }
        }
        events.forEach { changeEvents.emit(it) }
    }

    override suspend fun importProperties(properties: Collection<Property<*>>) {
        val events = mutex.withLock {
            properties.map { property ->
                val isUpdate = this.properties.containsKey(property.name)
                this.properties[property.name] = property
                if (isUpdate) PropertyStoreEvent.Updated(property.name) else PropertyStoreEvent.Created(property.name)
            }
        }
        events.forEach { changeEvents.emit(it) }
    }

    override suspend fun createSchema() {
        // No-op for in-memory store
    }

    override fun observeChanges(): Flow<PropertyStoreEvent> = changeEvents.asSharedFlow()
}
