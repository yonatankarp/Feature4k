package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("list")
data class PropertyList<T>(
    override val name: String,
    override val value: List<T>,
    override val description: String? = null,
    override val fixedValues: Set<List<T>> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<List<T>>()
