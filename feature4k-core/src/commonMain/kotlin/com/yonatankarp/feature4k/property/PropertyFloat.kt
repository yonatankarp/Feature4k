package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("float")
data class PropertyFloat(
    override val name: String,
    override val value: Float,
    override val description: String? = null,
    override val fixedValues: Set<Float> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Float>()
