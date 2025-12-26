package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.audit.emission.NoOpEventEmitter
import com.yonatankarp.feature4k.audit.emission.StoreEventEmitter
import com.yonatankarp.feature4k.exception.PropertyAlreadyExistException
import com.yonatankarp.feature4k.exception.PropertyNotFoundException
import com.yonatankarp.feature4k.property.Property
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
 * @property eventFactory Factory for creating property events with audit metadata
 * @author Yonatan Karp-Rudin
 */
class InMemoryPropertyStore(
    private val eventEmitter: StoreEventEmitter<PropertyStoreEvent> = NoOpEventEmitter(),
    private val eventFactory: PropertyEventFactory = PropertyEventFactory.Default,
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
            eventEmitter.emit(eventFactory.created(property.name))
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
            val event = if (isUpdate) eventFactory.updated(propertyName) else eventFactory.created(propertyName)
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
            eventEmitter.emit(eventFactory.deleted(propertyName))
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            val propertyNames = properties.keys.toList()
            properties.clear()
            propertyNames.forEach { eventEmitter.emit(eventFactory.deleted(it)) }
        }
    }

    override suspend fun importProperties(properties: Collection<Property<*>>) {
        mutex.withLock {
            properties.forEach { property ->
                val isUpdate = this.properties.containsKey(property.name)
                this.properties[property.name] = property
                val event = if (isUpdate) eventFactory.updated(property.name) else eventFactory.created(property.name)
                eventEmitter.emit(event)
            }
        }
    }

    override suspend fun createSchema() {
        // No-op for in-memory store
    }

    override fun observeChanges(): Flow<PropertyStoreEvent> = eventEmitter.observe()
}
