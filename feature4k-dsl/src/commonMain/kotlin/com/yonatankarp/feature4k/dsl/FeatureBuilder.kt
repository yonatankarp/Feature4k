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
     * Configures custom properties for this feature.
     *
     * ```kotlin
     * customProperties {
     *     string("config.url", "https://example.com")
     *     int("max.retries", 3)
     * }
     * ```
     */
    fun customProperties(block: PropertiesBuilder.() -> Unit) {
        val builder = PropertiesBuilder()
        builder.block()
        builder.build().forEach { property ->
            customProperties[property.name] = property
        }
    }

    /**
     * Builds the Feature instance.
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
 * DSL entry point for creating Feature instances.
 *
 * ```kotlin
 * val feature = feature("dark-mode") {
 *     enabled = true
 *     description = "Enable dark theme"
 * }
 * ```
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
     * Adds a feature to the collection.
     *
     * ```kotlin
     * feature("dark-mode") {
     *     enabled = true
     * }
     * ```
     */
    fun feature(uid: String, block: FeatureBuilder.() -> Unit = {}) {
        val builder = FeatureBuilder(uid)
        builder.block()
        features.add(builder.build())
    }

    /**
     * Builds the list of features.
     */
    internal fun build(): List<Feature> = features.toList()
}
