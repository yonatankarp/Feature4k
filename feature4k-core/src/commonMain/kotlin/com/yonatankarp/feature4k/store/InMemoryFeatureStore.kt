package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.audit.emission.NoOpEventEmitter
import com.yonatankarp.feature4k.audit.emission.StoreEventEmitter
import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.exception.FeatureAlreadyExistException
import com.yonatankarp.feature4k.exception.FeatureNotFoundException
import com.yonatankarp.feature4k.exception.GroupNotFoundException
import kotlinx.coroutines.flow.Flow
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
 * @property eventEmitter Event emitter for publishing store changes (defaults to [NoOpEventEmitter])
 * @property eventFactory Factory for creating feature events with audit metadata
 * @author Yonatan Karp-Rudin
 */
class InMemoryFeatureStore(
    private val eventEmitter: StoreEventEmitter<FeatureStoreEvent> = NoOpEventEmitter(),
    private val eventFactory: FeatureEventFactory = FeatureEventFactory.Default,
) : FeatureStore {
    private val features = mutableMapOf<String, Feature>()
    private val mutex = Mutex()

    override suspend fun enable(featureId: String) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.enable()
            eventEmitter.emit(eventFactory.enabled(featureId))
        }
    }

    override suspend fun disable(featureId: String) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.disable()
            eventEmitter.emit(eventFactory.disabled(featureId))
        }
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
            eventEmitter.emit(eventFactory.created(feature.uid))
        }
    }

    override suspend fun get(featureId: String): Feature? = mutex.withLock {
        features[featureId]
    }

    override suspend fun set(
        featureId: String,
        feature: Feature,
    ) {
        mutex.withLock {
            val isUpdate = featureId in features
            features[featureId] = feature
            val event = if (isUpdate) eventFactory.updated(featureId) else eventFactory.created(featureId)
            eventEmitter.emit(event)
        }
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
            eventEmitter.emit(eventFactory.deleted(featureId))
        }
    }

    override suspend fun grantRoleOnFeature(
        featureId: String,
        roleName: String,
    ) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.copy(permissions = feature.permissions + roleName)
            eventEmitter.emit(eventFactory.roleUpdated(featureId))
        }
    }

    override suspend fun removeRoleFromFeature(
        featureId: String,
        roleName: String,
    ) {
        mutex.withLock {
            val feature = features[featureId] ?: throw FeatureNotFoundException(featureId)
            features[featureId] = feature.copy(permissions = feature.permissions - roleName)
            eventEmitter.emit(eventFactory.roleDeleted(featureId))
        }
    }

    override suspend fun enableGroup(groupName: String) {
        mutex.withLock {
            val groupFeatures = features.values.filter { it.group == groupName }
            if (groupFeatures.isEmpty()) {
                throw GroupNotFoundException(groupName)
            }
            groupFeatures.forEach { feature ->
                features[feature.uid] = feature.enable()
                eventEmitter.emit(eventFactory.enabled(feature.uid))
            }
        }
    }

    override suspend fun disableGroup(groupName: String) {
        mutex.withLock {
            val groupFeatures = features.values.filter { it.group == groupName }
            if (groupFeatures.isEmpty()) {
                throw GroupNotFoundException(groupName)
            }
            groupFeatures.forEach { feature ->
                features[feature.uid] = feature.disable()
                eventEmitter.emit(eventFactory.disabled(feature.uid))
            }
        }
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
            eventEmitter.emit(eventFactory.updated(featureId))
        }
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
            eventEmitter.emit(eventFactory.updated(featureId))
        }
    }

    override suspend fun getAllGroups(): Set<String> = mutex.withLock {
        features.values.mapNotNull { it.group }.toSet()
    }

    override suspend fun clear() {
        mutex.withLock {
            val featureIds = features.keys.toList()
            features.clear()
            featureIds.forEach { eventEmitter.emit(eventFactory.deleted(it)) }
        }
    }

    override suspend fun importFeatures(features: Collection<Feature>) {
        mutex.withLock {
            features.forEach { feature ->
                val isUpdate = this.features.containsKey(feature.uid)
                this.features[feature.uid] = feature
                val event = if (isUpdate) eventFactory.updated(feature.uid) else eventFactory.created(feature.uid)
                eventEmitter.emit(event)
            }
        }
    }

    override suspend fun createSchema() {
        // No-op for in-memory store
    }

    override fun observeChanges(): Flow<FeatureStoreEvent> = eventEmitter.observe()
}
