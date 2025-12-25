package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("short")
data class PropertyShort(
    override val name: String,
    override val value: Short,
    override val description: String? = null,
    override val fixedValues: Set<Short> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Short>()
