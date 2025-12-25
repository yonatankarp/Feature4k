package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for List values.
 *
 * @property name Unique name of the property
 * @property value Current list value
 * @property description Optional description
 * @property fixedValues Set of allowed list values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("list")
data class PropertyList<T>(
    override val name: String,
    override val value: List<T>,
    override val description: String? = null,
    override val fixedValues: Set<List<T>> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<List<T>>()
