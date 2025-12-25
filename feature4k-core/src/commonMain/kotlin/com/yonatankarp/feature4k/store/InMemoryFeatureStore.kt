package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.exception.FeatureAlreadyExistException
import com.yonatankarp.feature4k.exception.FeatureNotFoundException
import com.yonatankarp.feature4k.exception.GroupNotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory implementation of [FeatureStore] using thread-safe collections.
 *
 * This implementation stores all features in memory using a mutable map protected
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
class InMemoryFeatureStore(
    private val eventReplay: Int = 10,
    private val eventBufferCapacity: Int = 64,
) : FeatureStore {
    private val features = mutableMapOf<String, Feature>()
    private val mutex = Mutex()
    private val changeEvents = MutableSharedFlow<StoreEvent>(
        replay = eventReplay,
        extraBufferCapacity = eventBufferCapacity,
    )

    override suspend fun enable(featureId: String) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.enable()
        }
        changeEvents.emit(StoreEvent.Enabled(featureId))
    }

    override suspend fun disable(featureId: String) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.disable()
        }
        changeEvents.emit(StoreEvent.Disabled(featureId))
    }

    override suspend fun contains(featureId: String): Boolean = mutex.withLock {
        featureId in features
    }

    override suspend fun plusAssign(feature: Feature) {
        mutex.withLock {
            if (feature.uid in features) {
                throw FeatureAlreadyExistException(feature.uid)
            }
            features[feature.uid] = feature
        }
        changeEvents.emit(StoreEvent.Created(feature.uid))
    }

    override suspend fun get(featureId: String): Feature? = mutex.withLock {
        features[featureId]
    }

    override suspend fun set(
        featureId: String,
        feature: Feature,
    ) {
        val event = mutex.withLock {
            val isUpdate = featureId in features
            features[featureId] = feature
            if (isUpdate) StoreEvent.Updated(featureId) else StoreEvent.Created(featureId)
        }
        changeEvents.emit(event)
    }

    override suspend fun getAll(): Map<String, Feature> = mutex.withLock {
        features.toMap()
    }

    override suspend fun minusAssign(featureId: String) {
        mutex.withLock {
            if (featureId !in features) {
                throw FeatureNotFoundException(featureId)
            }
            features.remove(featureId)
        }
        changeEvents.emit(StoreEvent.Deleted(featureId))
    }

    override suspend fun grantRoleOnFeature(
        featureId: String,
        roleName: String,
    ) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.copy(permissions = feature.permissions + roleName)
        }
        changeEvents.emit(StoreEvent.RoleUpdated(featureId))
    }

    override suspend fun removeRoleFromFeature(
        featureId: String,
        roleName: String,
    ) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.copy(permissions = feature.permissions - roleName)
        }
        changeEvents.emit(StoreEvent.RoleDeleted(featureId))
    }

    override suspend fun enableGroup(groupName: String) {
        val events = mutex.withLock {
            val groupFeatures = features.values.filter { it.group == groupName }
            if (groupFeatures.isEmpty()) {
                throw GroupNotFoundException(groupName)
            }
            groupFeatures.map { feature ->
                features[feature.uid] = feature.enable()
                StoreEvent.Enabled(feature.uid)
            }
        }
        events.forEach { changeEvents.emit(it) }
    }

    override suspend fun disableGroup(groupName: String) {
        val events = mutex.withLock {
            val groupFeatures = features.values.filter { it.group == groupName }
            if (groupFeatures.isEmpty()) {
                throw GroupNotFoundException(groupName)
            }
            groupFeatures.map { feature ->
                features[feature.uid] = feature.disable()
                StoreEvent.Disabled(feature.uid)
            }
        }
        events.forEach { changeEvents.emit(it) }
    }

    override suspend fun existsGroup(groupName: String): Boolean = mutex.withLock {
        features.values.any { it.group == groupName }
    }

    override suspend fun getGroup(groupName: String): Map<String, Feature> = mutex.withLock {
        val groupFeatures = features.filterValues { it.group == groupName }
        if (groupFeatures.isEmpty()) {
            throw GroupNotFoundException(groupName)
        }
        groupFeatures
    }

    override suspend fun addToGroup(
        featureId: String,
        groupName: String,
    ) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.copy(group = groupName)
        }
        changeEvents.emit(StoreEvent.Updated(featureId))
    }

    override suspend fun removeFromGroup(
        featureId: String,
        groupName: String,
    ) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            if (feature.group != groupName) {
                throw GroupNotFoundException(groupName)
            }
            features[featureId] = feature.copy(group = null)
        }
        changeEvents.emit(StoreEvent.Updated(featureId))
    }

    override suspend fun getAllGroups(): Set<String> = mutex.withLock {
        features.values.mapNotNull { it.group }.toSet()
    }

    override suspend fun clear() {
        val events = mutex.withLock {
            val featureIds = features.keys.toList()
            features.clear()
            featureIds.map { StoreEvent.Deleted(it) }
        }
        events.forEach { changeEvents.emit(it) }
    }

    override suspend fun importFeatures(features: Collection<Feature>) {
        val events = mutex.withLock {
            features.map { feature ->
                val isUpdate = this.features.containsKey(feature.uid)
                this.features[feature.uid] = feature
                if (isUpdate) StoreEvent.Updated(feature.uid) else StoreEvent.Created(feature.uid)
            }
        }
        events.forEach { changeEvents.emit(it) }
    }

    override suspend fun createSchema() {
        // No-op for in-memory store
    }

    override fun observeChanges(): Flow<StoreEvent> = changeEvents.asSharedFlow()
}
