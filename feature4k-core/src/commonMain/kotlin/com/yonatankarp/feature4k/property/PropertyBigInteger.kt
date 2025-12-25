package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("bigInteger")
data class PropertyBigInteger(
    override val name: String,
    override val value: String,
    override val description: String? = null,
    override val fixedValues: Set<String> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<String>() {
    init {
        require(value.matches("""^-?\d+$""".toRegex())) {
            "Value '$value' is not a valid BigInteger"
        }
    }
}
