package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.property.Property
import com.yonatankarp.feature4k.strategy.FlippingStrategy

/**
 * DSL builder for creating [Feature] instances with type-safe configuration.
 *
 * ## Usage Examples
 *
 * ### Basic feature
 * ```kotlin
 * val feature = feature("dark-mode") {
 *     enabled = true
 *     description = "Enable dark theme"
 * }
 * ```
 *
 * ### Feature with group and permissions
 * ```kotlin
 * val feature = feature("premium-dashboard") {
 *     enabled = true
 *     group = "premium"
 *     permissions += listOf("ROLE_PREMIUM", "ROLE_ADMIN")
 * }
 * ```
 *
 * ### Feature with flipping strategy
 * ```kotlin
 * val feature = feature("beta-features") {
 *     enabled = true
 *     flippingStrategy = allowList("alice", "bob")
 * }
 * ```
 *
 * ### Feature with custom properties
 * ```kotlin
 * val feature = feature("api-endpoint") {
 *     enabled = true
 *     customProperties {
 *         string("base.url", "https://api.example.com")
 *         int("timeout", 5000)
 *     }
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin
 */
@Feature4KDsl
class FeatureBuilder(private val uid: String) {
    /**
     * Whether the feature is enabled (defaults to false).
     */
    var enabled: Boolean = false

    /**
     * Human-readable description of the feature.
     */
    var description: String? = null

    /**
     * Optional group name for organizing related features.
     */
    var group: String? = null

    /**
     * Mutable set of permissions required to use this feature.
     * Use += operator to add permissions.
     */
    val permissions: MutableSet<String> = mutableSetOf()

    /**
     * Optional flipping strategy for conditional feature activation.
     */
    var flippingStrategy: FlippingStrategy? = null

    /**
     * Map of custom properties associated with this feature.
     */
    private val customProperties: MutableMap<String, Property<*>> = mutableMapOf()

    /**
     * Adds custom properties to this feature using a PropertiesBuilder.
     *
     * The provided [block] is executed on a fresh PropertiesBuilder and each resulting property is registered on the feature by its name.
     *
     * @param block DSL block that declares properties on a PropertiesBuilder.
     */
    fun customProperties(block: PropertiesBuilder.() -> Unit) {
        val builder = PropertiesBuilder()
        builder.block()
        builder.build().forEach { property ->
            customProperties[property.name] = property
        }
    }

    /**
     * Create a Feature configured by this builder.
     *
     * @return The constructed Feature whose properties (uid, enabled, description, group, permissions, flippingStrategy, and customProperties) reflect the builder's current state.
     */
    internal fun build(): Feature = Feature(
        uid = uid,
        enabled = enabled,
        description = description,
        group = group,
        permissions = permissions.toSet(),
        flippingStrategy = flippingStrategy,
        customProperties = customProperties.toMap(),
    )
}

/**
 * DSL entry point to declare and configure a Feature.
 *
 * Example:
 * ```kotlin
 * val feature = feature("dark-mode") {
 *     enabled = true
 *     description = "Enable dark theme"
 * }
 * ```
 *
 * @param uid The feature's unique identifier.
 * @param block Configuration block applied to a newly created FeatureBuilder.
 * @return The constructed Feature configured by the provided block.
 */
fun feature(uid: String, block: FeatureBuilder.() -> Unit = {}): Feature {
    val builder = FeatureBuilder(uid)
    builder.block()
    return builder.build()
}

/**
 * Builder for collecting multiple Feature instances.
 */
@Feature4KDsl
class FeaturesBuilder {
    private val features = mutableListOf<Feature>()

    /**
     * Adds a feature with the given UID to the collection, configured by the provided builder block.
     *
     * @param uid The unique identifier for the feature.
     * @param block A configuration block executed on a new [FeatureBuilder] for this feature.
     */
    fun feature(uid: String, block: FeatureBuilder.() -> Unit = {}) {
        val builder = FeatureBuilder(uid)
        builder.block()
        features.add(builder.build())
    }

    /**
     * Create an immutable snapshot of the accumulated features.
     *
     * @return An immutable List containing the accumulated Feature instances.
     */
    internal fun build(): List<Feature> = features.toList()
}
