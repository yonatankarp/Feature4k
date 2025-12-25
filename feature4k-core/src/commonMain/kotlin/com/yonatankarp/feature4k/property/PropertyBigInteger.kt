package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for BigInteger values.
 *
 * @property name Unique name of the property
 * @property value Current big integer value (stored as String)
 * @property description Optional description
 * @property fixedValues Set of allowed big integer values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 * @author Yonatan Karp-Rudin
 */
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
