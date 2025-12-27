package com.yonatankarp.feature4k.core

import com.yonatankarp.feature4k.exception.InvalidFeatureIdentifierException
import com.yonatankarp.feature4k.property.Property
import com.yonatankarp.feature4k.strategy.FlippingStrategy
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Represents a feature flag identified by a unique identifier.
 *
 * Feature flags (or feature toggles) enable/disable functionalities at runtime,
 * following Martin Fowler's continuous delivery principles.
 *
 * @property uid Unique feature identifier
 * @property enabled Current state of the feature (enabled/disabled)
 * @property description Human-readable description of the feature
 * @property group Optional group name for organizing related features
 * @property permissions Set of permissions required to use this feature
 * @property flippingStrategy Optional strategy for conditional feature activation
 * @property customProperties Map of custom properties associated with this feature
 * @author Yonatan Karp-Rudin
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Feature(
    val uid: String,
    @EncodeDefault val enabled: Boolean = false,
    val description: String? = null,
    val group: String? = null,
    val permissions: Set<String> = emptySet(),
    val flippingStrategy: FlippingStrategy? = null,
    val customProperties: Map<String, Property<*>> = emptyMap(),
) {
    init {
        if (uid.isBlank()) {
            throw InvalidFeatureIdentifierException(uid, "UID cannot be blank")
        }
    }

    /**
     * Creates a copy of this feature with enabled set to true
     */
    fun enable(): Feature = copy(enabled = true)

    /**
     * Creates a copy of this feature with enabled set to false
     */
    fun disable(): Feature = copy(enabled = false)

    /**
     * Checks if this feature has any permissions defined
     */
    fun hasPermissions(): Boolean = permissions.isNotEmpty()

    /**
     * Checks if this feature belongs to a group
     */
    fun hasGroup(): Boolean = group.isNullOrBlank().not()

    /**
     * Checks if this feature has any custom properties defined
     */
    fun hasCustomProperties(): Boolean = customProperties.isNotEmpty()

    /**
     * Checks if this feature has a flipping strategy defined
     */
    fun hasFlippingStrategy(): Boolean = flippingStrategy != null

    /**
     * Evaluates whether this feature should be enabled for the given execution context.
     *
     * If a flipping strategy is defined, it will be used to determine if the feature
     * should be enabled. If no strategy is defined, returns the current enabled state.
     *
     * @param context The execution context containing user, client, server, and custom parameters
     * @return `true` if the feature should be enabled for the given context, `false` otherwise
     */
    fun evaluate(context: FlippingExecutionContext): Boolean = flippingStrategy?.evaluate(context) ?: enabled
}
