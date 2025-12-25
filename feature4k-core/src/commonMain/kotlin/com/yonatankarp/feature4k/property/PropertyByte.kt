package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("byte")
data class PropertyByte(
    override val name: String,
    override val value: Byte,
    override val description: String? = null,
    override val fixedValues: Set<Byte> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Byte>()
