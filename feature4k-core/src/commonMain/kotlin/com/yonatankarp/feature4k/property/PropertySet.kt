package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("set")
data class PropertySet<T>(
    override val name: String,
    override val value: Set<T>,
    override val description: String? = null,
    override val fixedValues: Set<Set<T>> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Set<T>>()
