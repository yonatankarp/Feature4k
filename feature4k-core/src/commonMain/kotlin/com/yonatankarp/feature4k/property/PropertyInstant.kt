package com.yonatankarp.feature4k.property

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("instant")
data class PropertyInstant(
    override val name: String,
    override val value: Instant,
    override val description: String? = null,
    override val fixedValues: Set<Instant> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Instant>()
