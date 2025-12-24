package com.yonatankarp.feature4k.core

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
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Feature(
    val uid: String,
    @EncodeDefault val enabled: Boolean = false,
    val description: String? = null,
    val group: String? = null,
    val permissions: Set<String> = emptySet()
) {
    init {
        require(uid.isNotBlank()) { "Feature UID cannot be blank" }
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
}
