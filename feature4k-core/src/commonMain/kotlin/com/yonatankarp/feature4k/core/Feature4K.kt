package com.yonatankarp.feature4k.core

import com.yonatankarp.feature4k.audit.EventPublisher
import com.yonatankarp.feature4k.exception.FeatureAlreadyExistException
import com.yonatankarp.feature4k.exception.FeatureNotFoundException
import com.yonatankarp.feature4k.exception.PropertyAlreadyExistException
import com.yonatankarp.feature4k.exception.PropertyNotFoundException
import com.yonatankarp.feature4k.property.Property
import com.yonatankarp.feature4k.security.AuthorizationsManager
import com.yonatankarp.feature4k.store.FeatureStore
import com.yonatankarp.feature4k.store.FeatureStoreEvent
import com.yonatankarp.feature4k.store.InMemoryFeatureStore
import com.yonatankarp.feature4k.store.InMemoryPropertyStore
import com.yonatankarp.feature4k.store.PropertyStore
import com.yonatankarp.feature4k.utils.Uid
import kotlinx.datetime.Clock

/**
 * Main facade for Feature4K - the central entry point for feature flag management.
 *
 * Feature4K provides a unified API for:
 * - **Feature evaluation** - Check if features are enabled with strategy support
 * - **Authorization** - Integrate permission checks before feature access
 * - **Event publishing** - Emit audit events for all operations
 * - **Feature management** - CRUD operations for features and properties
 *
 * ## Architecture
 *
 * Feature4K follows the Facade pattern, orchestrating:
 * - [FeatureStore] - Persistent storage for features
 * - [PropertyStore] - Persistent storage for properties
 * - [AuthorizationsManager] - Permission checking for current user
 * - [EventPublisher] - Asynchronous event emission for audit trails
 *
 * ## Usage
 *
 * ### Basic setup with defaults
 * ```kotlin
 * val feature4k = Feature4K()
 * if (feature4k["new-dashboard"]) {
 *     // Show new dashboard
 * }
 * ```
 *
 * ### With custom stores and authorization
 * ```kotlin
 * val feature4k = Feature4K(
 *     featureStore = SqlDelightFeatureStore(database),
 *     propertyStore = SqlDelightPropertyStore(database),
 *     authorizationsManager = myAuthManager,
 *     eventPublisher = myEventPublisher,
 *     autoCreate = true
 * )
 * ```
 *
 * ### Auto-creation behavior
 * When `autoCreate = true`, checking non-existent features will:
 * 1. Create the feature as disabled
 * 2. Store it in the FeatureStore
 * 3. Return `false`
 *
 * This is useful during development to avoid manual feature registration.
 *
 * @property featureStore Storage backend for features (defaults to in-memory)
 * @property propertyStore Storage backend for properties (defaults to in-memory)
 * @property authorizationsManager Optional authorization provider for permission checks
 * @property eventPublisher Optional event publisher for audit trail
 * @property autoCreate Whether to auto-create features when checking non-existent ones
 *
 * @author Yonatan Karp-Rudin
 */
class Feature4K(
    private val featureStore: FeatureStore = InMemoryFeatureStore(),
    private val propertyStore: PropertyStore = InMemoryPropertyStore(),
    private val authorizationsManager: AuthorizationsManager? = null,
    private val eventPublisher: EventPublisher? = null,
    private val autoCreate: Boolean = false,
) {
    /**
     * Checks if a feature is enabled for the current context.
     *
     * This is the primary operator for feature evaluation, supporting idiomatic syntax:
     * `if (feature4k["dark-mode"]) { ... }`
     *
     * ## Evaluation flow
     *
     * 1. Verify feature exists (auto-create if configured)
     * 2. Check if feature is enabled
     * 3. Validate user authorization (if configured)
     * 4. Evaluate flipping strategy (if defined)
     * 5. Publish check event for audit trail (if configured)
     *
     * ## Examples
     *
     * ```kotlin
     * // Operator syntax (recommended)
     * if (feature4k["dark-mode"]) {
     *     applyDarkTheme()
     * }
     *
     * // With execution context
     * val context = FlippingExecutionContext(
     *     user = "alice",
     *     customParams = mapOf("region" to "US")
     * )
     * if (feature4k["premium-features", context]) {
     *     showPremiumUI()
     * }
     *
     * // Auto-creation during development
     * val dev = Feature4K(autoCreate = true)
     * dev["experimental-feature"] // returns false, feature now exists
     * ```
     *
     * @param featureId Unique feature identifier
     * @param context Execution context for strategy evaluation
     * @return `true` if feature is enabled and passes all checks, `false` otherwise
     */
    suspend operator fun get(
        featureId: String,
        context: FlippingExecutionContext = FlippingExecutionContext(),
    ): Boolean {
        val feature = featureStore[featureId] ?: run {
            val result = handleNonExistentFeature(featureId)
            publishCheckEvent(featureId, result, context)
            return result
        }

        val result = feature.enabled &&
            isAuthorized(feature) &&
            evaluateStrategy(feature, featureId, context)

        publishCheckEvent(featureId, result, context)
        return result
    }

    private fun isAuthorized(feature: Feature): Boolean = authorizationsManager?.let { manager ->
        feature.hasPermissions().not() || manager.isAllowedAll(feature.permissions)
    } ?: true

    private suspend fun evaluateStrategy(
        feature: Feature,
        featureId: String,
        context: FlippingExecutionContext,
    ): Boolean = feature.flippingStrategy?.let { strategy ->
        val evalContext = FeatureEvaluationContext(
            featureName = featureId,
            store = featureStore,
            context = context,
        )
        strategy.evaluate(evalContext)
    } ?: true

    private suspend fun publishCheckEvent(
        featureId: String,
        result: Boolean,
        context: FlippingExecutionContext,
    ) {
        eventPublisher?.publish(
            FeatureStoreEvent.Checked(
                uid = featureId,
                eventUid = Uid.generate(),
                timestamp = Clock.System.now(),
                user = context.user,
                source = context.source,
                host = context.host,
                duration = null,
                value = result.toString(),
                customProperties = emptyMap(),
            ),
        )
    }

    private suspend fun handleNonExistentFeature(featureId: String): Boolean {
        if (autoCreate) {
            featureStore += Feature(uid = featureId, enabled = false)
        }
        return false
    }

    /**
     * Creates a new feature in the store.
     *
     * The store automatically emits a [FeatureStoreEvent.Created] event.
     *
     * ```kotlin
     * feature4k.create(Feature(
     *     uid = "dark-mode",
     *     enabled = true,
     *     description = "Enable dark theme",
     *     flippingStrategy = OfficeHourStrategy(startHour = 9, endHour = 17)
     * ))
     * ```
     *
     * @param feature The feature to create
     * @throws FeatureAlreadyExistException if feature already exists
     * @return This Feature4K instance for method chaining
     */
    suspend fun create(feature: Feature): Feature4K {
        featureStore += feature
        return this
    }

    /**
     * Enables a feature by its unique identifier.
     *
     * The store automatically emits a [FeatureStoreEvent.Enabled] event.
     *
     * ```kotlin
     * feature4k.enable("premium-features")
     * ```
     *
     * @param featureId Unique feature identifier
     * @throws FeatureNotFoundException if feature does not exist
     * @return This Feature4K instance for method chaining
     */
    suspend fun enable(featureId: String): Feature4K {
        featureStore.enable(featureId)
        return this
    }

    /**
     * Disables a feature by its unique identifier.
     *
     * The store automatically emits a [FeatureStoreEvent.Disabled] event.
     *
     * ```kotlin
     * feature4k.disable("experimental-feature")
     * ```
     *
     * @param featureId Unique feature identifier
     * @throws FeatureNotFoundException if feature does not exist
     * @return This Feature4K instance for method chaining
     */
    suspend fun disable(featureId: String): Feature4K {
        featureStore.disable(featureId)
        return this
    }

    /**
     * Updates an existing feature or creates it if it doesn't exist.
     *
     * The store emits either [FeatureStoreEvent.Updated] or [FeatureStoreEvent.Created].
     *
     * ```kotlin
     * val updated = feature4k.feature("dark-mode")
     *     .copy(flippingStrategy = PonderationStrategy(weight = 50.0))
     * feature4k.update(updated)
     * ```
     *
     * @param feature The feature with updated values
     * @return This Feature4K instance for method chaining
     */
    suspend fun update(feature: Feature): Feature4K {
        featureStore[feature.uid] = feature
        return this
    }

    /**
     * Deletes a feature by its unique identifier.
     *
     * The store automatically emits a [FeatureStoreEvent.Deleted] event.
     *
     * ```kotlin
     * feature4k.delete("deprecated-feature")
     * ```
     *
     * @param featureId Unique feature identifier
     * @throws FeatureNotFoundException if feature does not exist
     * @return This Feature4K instance for method chaining
     */
    suspend fun delete(featureId: String): Feature4K {
        featureStore -= featureId
        return this
    }

    /**
     * Retrieves a feature by its unique identifier.
     *
     * Supports idiomatic operator syntax: `feature4k.feature("dark-mode")`
     *
     * If [autoCreate] is enabled and the feature doesn't exist, it will be created as disabled.
     *
     * ```kotlin
     * val feature = feature4k.feature("dark-mode")
     * println("Dark mode is ${if (feature.enabled) "enabled" else "disabled"}")
     * ```
     *
     * @param featureId Unique feature identifier
     * @return The feature instance
     * @throws FeatureNotFoundException if feature does not exist and autoCreate is false
     */
    suspend fun feature(featureId: String): Feature {
        var feature = featureStore[featureId]
        if (feature == null && autoCreate) {
            feature = Feature(uid = featureId, enabled = false)
            featureStore += feature
        }
        return feature ?: throw FeatureNotFoundException(featureId)
    }

    /**
     * Checks if a feature exists in the store.
     *
     * ```kotlin
     * if (feature4k.exists("dark-mode")) {
     *     println("Dark mode feature is configured")
     * }
     * ```
     *
     * @param featureId Unique feature identifier
     * @return `true` if the feature exists, `false` otherwise
     */
    suspend fun exists(featureId: String): Boolean = featureId in featureStore

    /**
     * Retrieves all features from the store.
     *
     * ```kotlin
     * feature4k.allFeatures().forEach { (id, feature) ->
     *     println("$id: ${if (feature.enabled) "ON" else "OFF"}")
     * }
     * ```
     *
     * @return Map of feature IDs to Feature objects
     */
    suspend fun allFeatures(): Map<String, Feature> = featureStore.getAll()

    /**
     * Creates a new property in the store.
     *
     * The store automatically emits a PropertyStoreEvent.Created event.
     *
     * ```kotlin
     * feature4k.createProperty(PropertyString(name = "api.url", value = "https://api.example.com"))
     * ```
     *
     * @param property The property to create
     * @throws PropertyAlreadyExistException if property already exists
     * @return This Feature4K instance for method chaining
     */
    suspend fun createProperty(property: Property<*>): Feature4K {
        propertyStore += property
        return this
    }

    /**
     * Retrieves a property by its name.
     *
     * ```kotlin
     * val apiUrl = feature4k.property("api.url")
     * ```
     *
     * @param propertyName Unique property name
     * @return The property instance
     * @throws PropertyNotFoundException if property does not exist
     */
    suspend fun property(propertyName: String): Property<*> = propertyStore[propertyName]
        ?: throw PropertyNotFoundException(propertyName)

    /**
     * Updates an existing property or creates it if it doesn't exist.
     *
     * The store emits either PropertyStoreEvent.Updated or PropertyStoreEvent.Created.
     *
     * ```kotlin
     * val updated = feature4k.property("api.url") as PropertyString
     * feature4k.updateProperty(updated.copy(value = "https://new-api.example.com"))
     * ```
     *
     * @param property The property with updated values
     * @return This Feature4K instance for method chaining
     */
    suspend fun updateProperty(property: Property<*>): Feature4K {
        propertyStore[property.name] = property
        return this
    }

    /**
     * Deletes a property by its name.
     *
     * The store automatically emits a PropertyStoreEvent.Deleted event.
     *
     * ```kotlin
     * feature4k.deleteProperty("deprecated.setting")
     * ```
     *
     * @param propertyName Unique property name
     * @throws PropertyNotFoundException if property does not exist
     * @return This Feature4K instance for method chaining
     */
    suspend fun deleteProperty(propertyName: String): Feature4K {
        propertyStore -= propertyName
        return this
    }

    /**
     * Retrieves all properties from the store.
     *
     * ```kotlin
     * feature4k.allProperties().forEach { (name, property) ->
     *     println("$name: ${property.value}")
     * }
     * ```
     *
     * @return Map of property names to Property objects
     */
    suspend fun allProperties(): Map<String, Property<*>> = propertyStore.getAll()
}
