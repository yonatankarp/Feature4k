package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.audit.EventPublisher
import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.core.Feature4K
import com.yonatankarp.feature4k.property.Property
import com.yonatankarp.feature4k.security.AuthorizationsManager
import com.yonatankarp.feature4k.store.FeatureStore
import com.yonatankarp.feature4k.store.InMemoryFeatureStore
import com.yonatankarp.feature4k.store.InMemoryPropertyStore
import com.yonatankarp.feature4k.store.PropertyStore

/**
 * DSL builder for creating [Feature4K] instances with type-safe configuration.
 *
 * ## Usage Examples
 *
 * ### Basic configuration with defaults
 * ```kotlin
 * val feature4k = feature4k {
 *     autoCreate = true
 * }
 * ```
 *
 * ### With custom stores and authorization
 * ```kotlin
 * val feature4k = feature4k {
 *     featureStore = SqlDelightFeatureStore(database)
 *     propertyStore = SqlDelightPropertyStore(database)
 *     authorizationsManager = MyAuthManager()
 *     eventPublisher = MyEventPublisher()
 *     autoCreate = false
 * }
 * ```
 *
 * ### With inline feature definitions
 * ```kotlin
 * val feature4k = feature4k {
 *     features {
 *         feature("dark-mode") {
 *             enabled = true
 *             description = "Enable dark theme"
 *             group = "ui"
 *         }
 *         feature("premium-features") {
 *             enabled = true
 *             permissions += listOf("ROLE_PREMIUM", "ROLE_ADMIN")
 *             flippingStrategy = allowList("alice", "bob")
 *         }
 *     }
 * }
 * ```
 *
 * ### With inline property definitions
 * ```kotlin
 * val feature4k = feature4k {
 *     properties {
 *         string("api.url", "https://api.example.com")
 *         int("max.connections", 100)
 *         boolean("debug.enabled", false)
 *     }
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin
 */
@DslMarker
annotation class Feature4KDsl

/**
 * Builder class for configuring Feature4K instances.
 */
@Feature4KDsl
class Feature4KBuilder {
    /**
     * Custom feature store implementation (defaults to in-memory).
     */
    var featureStore: FeatureStore = InMemoryFeatureStore()

    /**
     * Custom property store implementation (defaults to in-memory).
     */
    var propertyStore: PropertyStore = InMemoryPropertyStore()

    /**
     * Optional authorization manager for permission checks.
     */
    var authorizationsManager: AuthorizationsManager? = null

    /**
     * Optional event publisher for audit trail.
     */
    var eventPublisher: EventPublisher? = null

    /**
     * Whether to auto-create features when checking non-existent ones.
     */
    var autoCreate: Boolean = false

    /**
     * List of features to be created during initialization.
     */
    private val featuresToCreate = mutableListOf<Feature>()

    /**
     * List of properties to be created during initialization.
     */
    private val propertiesToCreate = mutableListOf<Property<*>>()

    /**
     * Collects feature definitions declared in the given DSL block to be created during initialization.
     *
     * Executes the provided `FeaturesBuilder` block and accumulates the resulting features so they
     * will be created when the builder is built.
     *
     * @param block DSL block used to declare features to initialize.
     */
    fun features(block: FeaturesBuilder.() -> Unit) {
        val builder = FeaturesBuilder()
        builder.block()
        featuresToCreate.addAll(builder.build())
    }

    /**
     * Configures properties to be created during initialization.
     *
     * The provided block is executed on a PropertiesBuilder; all properties produced by the builder are added to the list of properties to be created when the Feature4K instance is initialized.
     *
     * @param block DSL block used to declare properties.
     */
    fun properties(block: PropertiesBuilder.() -> Unit) {
        val builder = PropertiesBuilder()
        builder.block()
        propertiesToCreate.addAll(builder.build())
    }

    /**
     * Constructs a Feature4K configured with the builder's stores, managers, and flags, and initializes it with any collected features and properties.
     *
     * @return The initialized Feature4K instance configured with the builder's settings and populated with the collected features and properties.
     */
    internal suspend fun build(): Feature4K {
        val instance = Feature4K(
            featureStore = featureStore,
            propertyStore = propertyStore,
            authorizationsManager = authorizationsManager,
            eventPublisher = eventPublisher,
            autoCreate = autoCreate,
        )

        // Initialize with configured features
        featuresToCreate.forEach { feature ->
            instance.create(feature)
        }

        // Initialize with configured properties
        propertiesToCreate.forEach { property ->
            instance.createProperty(property)
        }

        return instance
    }
}

/**
 * DSL entry point for creating Feature4K instances.
 *
 * Use the supplied block to configure the builder before the instance is created and initialized.
 *
 * @param block Configuration block applied to a new [Feature4KBuilder].
 * @return A configured and initialized [Feature4K] instance.
 */
suspend fun feature4k(block: Feature4KBuilder.() -> Unit): Feature4K {
    val builder = Feature4KBuilder()
    builder.block()
    return builder.build()
}
