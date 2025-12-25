package com.yonatankarp.feature4k.property

import kotlinx.serialization.Serializable

/**
 * Base sealed class for all property types.
 *
 * Properties provide typed configuration values that can be associated with features.
 * Each property has a name, typed value, optional description, and optional fixed values
 * for validation.
 *
 * @param T The type of the property value
 */
@Serializable
sealed class Property<T> {
    /**
     * Unique name of the property.
     */
    abstract val name: String

    /**
     * Current value of the property.
     */
    abstract val value: T

    /**
     * Optional human-readable description of the property.
     */
    abstract val description: String?

    /**
     * Set of allowed values. If not empty, the property value must be one of these values.
     */
    abstract val fixedValues: Set<T>

    /**
     * Indicates whether this property is read-only.
     * Some stores do not allow property edition.
     */
    abstract val readOnly: Boolean

    /**
     * Checks if this property has fixed values defined.
     */
    val hasFixedValues: Boolean
        get() = fixedValues.isNotEmpty()

    /**
     * Validates that the current value is in the fixed values set.
     * Returns true if no fixed values are defined or if the value is valid.
     */
    val isValid: Boolean
        get() = fixedValues.isEmpty() || fixedValues.contains(value)
}
