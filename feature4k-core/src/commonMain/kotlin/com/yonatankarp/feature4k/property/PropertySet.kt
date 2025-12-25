package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Set values.
 *
 * @property name Unique name of the property
 * @property value Current set value
 * @property description Optional description
 * @property fixedValues Set of allowed set values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("set")
data class PropertySet<T>(
    override val name: String,
    override val value: Set<T>,
    override val description: String? = null,
    override val fixedValues: Set<Set<T>> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Set<T>>()
