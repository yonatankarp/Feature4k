package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.event.FeatureStoreEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for persisting and retrieving [com.yonatankarp.feature4k.core.Feature]s.
 *
 * All operations are suspend functions to support asynchronous I/O across all platforms.
 * Store implementations can observe changes through [observeChanges] Flow.
 *
 * This interface provides:
 * - CRUD operations for individual features (using operators where idiomatic)
 * - Batch operations (getAll, importFeatures, clear)
 * - Group management (enable/disable/read groups)
 * - Permission management (grant/remove roles)
 * - Schema initialization for database-backed stores
 *
 * @author Yonatan Karp-Rudin
 */
interface FeatureStore {
    /**
     * Enable a feature by its unique identifier.
     *
     * @param featureId Unique feature identifier
     * @throws com.yonatankarp.feature4k.exception.FeatureNotFoundException if feature does not exist
     */
    suspend fun enable(featureId: String)

    /**
     * Disable a feature by its unique identifier.
     *
     * @param featureId Unique feature identifier
     * @throws com.yonatankarp.feature4k.exception.FeatureNotFoundException if feature does not exist
     */
    suspend fun disable(featureId: String)

    /**
     * Check if a feature exists in the store.
     * Idiomatic Kotlin operator for 'in' checks: if (featureId in store)
     *
     * @param featureId Unique feature identifier
     * @return true if feature exists, false otherwise
     */
    suspend operator fun contains(featureId: String): Boolean

    /**
     * Create a new feature in the store.
     * Idiomatic Kotlin operator for addition: store += feature
     *
     * @param feature Feature to create
     * @throws com.yonatankarp.feature4k.exception.FeatureAlreadyExistException if feature already exists
     */
    suspend operator fun plusAssign(feature: Feature)

    /**
     * Read a feature by its unique identifier.
     * Idiomatic Kotlin operator for indexed access: store[featureId]
     *
     * @param featureId Unique feature identifier
     * @return Feature if found, null otherwise
     */
    suspend operator fun get(featureId: String): Feature?

    /**
     * Update an existing feature in the store, or create if it doesn't exist.
     * Idiomatic Kotlin operator for indexed assignment: store[featureId] = feature
     *
     * @param featureId Unique feature identifier
     * @param feature Feature with updated values
     */
    suspend operator fun set(
        featureId: String,
        feature: Feature,
    )

    /**
     * Get all features from the store.
     *
     * @return Map of feature IDs to Feature objects
     */
    suspend fun getAll(): Map<String, Feature>

    /**
     * Delete a feature by its unique identifier.
     * Idiomatic Kotlin operator for removal: store -= featureId
     *
     * @param featureId Unique feature identifier
     * @throws com.yonatankarp.feature4k.exception.FeatureNotFoundException if feature does not exist
     */
    suspend operator fun minusAssign(featureId: String)

    /**
     * Grant a role/permission to a feature.
     *
     * @param featureId Unique feature identifier
     * @param roleName Role name to grant
     * @throws com.yonatankarp.feature4k.exception.FeatureNotFoundException if feature does not exist
     */
    suspend fun grantRoleOnFeature(
        featureId: String,
        roleName: String,
    )

    /**
     * Remove a role/permission from a feature.
     *
     * @param featureId Unique feature identifier
     * @param roleName Role name to remove
     * @throws com.yonatankarp.feature4k.exception.FeatureNotFoundException if feature does not exist
     */
    suspend fun removeRoleFromFeature(
        featureId: String,
        roleName: String,
    )

    /**
     * Enable all features in a group.
     *
     * @param groupName Group name
     * @throws com.yonatankarp.feature4k.exception.GroupNotFoundException if group does not exist
     */
    suspend fun enableGroup(groupName: String)

    /**
     * Disable all features in a group.
     *
     * @param groupName Group name
     * @throws com.yonatankarp.feature4k.exception.GroupNotFoundException if group does not exist
     */
    suspend fun disableGroup(groupName: String)

    /**
     * Check if a group exists in the store.
     *
     * @param groupName Group name
     * @return true if group exists, false otherwise
     */
    suspend fun existsGroup(groupName: String): Boolean

    /**
     * Get all features in a group.
     *
     * @param groupName Group name
     * @return Map of feature IDs to Feature objects in the group
     * @throws com.yonatankarp.feature4k.exception.GroupNotFoundException if group does not exist
     */
    suspend fun getGroup(groupName: String): Map<String, Feature>

    /**
     * Add a feature to a group.
     *
     * @param featureId Unique feature identifier
     * @param groupName Group name
     * @throws com.yonatankarp.feature4k.exception.FeatureNotFoundException if feature does not exist
     */
    suspend fun addToGroup(
        featureId: String,
        groupName: String,
    )

    /**
     * Remove a feature from a group.
     *
     * @param featureId Unique feature identifier
     * @param groupName Group name
     * @throws com.yonatankarp.feature4k.exception.FeatureNotFoundException if feature does not exist
     */
    suspend fun removeFromGroup(
        featureId: String,
        groupName: String,
    )

    /**
     * Get all group names in the store.
     *
     * @return Set of group names
     */
    suspend fun getAllGroups(): Set<String>

    /**
     * Remove all features from the store.
     */
    suspend fun clear()

    /**
     * Import a collection of features into the store.
     * Existing features may be overwritten depending on implementation.
     *
     * @param features Collection of features to import
     */
    suspend fun importFeatures(features: Collection<Feature>)

    /**
     * Initialize the database schema if needed.
     * For database-backed stores, this creates tables, indexes, etc.
     * For in-memory stores, this is typically a no-op.
     */
    suspend fun createSchema()

    /**
     * Observe changes to the feature store.
     *
     * @return Flow of FeatureStoreEvent representing changes (create, update, delete, etc.)
     */
    fun observeChanges(): Flow<FeatureStoreEvent>
}
