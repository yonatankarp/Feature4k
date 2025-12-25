package com.yonatankarp.feature4k.property

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("localDateTime")
data class PropertyLocalDateTime(
    override val name: String,
    override val value: LocalDateTime,
    override val description: String? = null,
    override val fixedValues: Set<LocalDateTime> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<LocalDateTime>()
