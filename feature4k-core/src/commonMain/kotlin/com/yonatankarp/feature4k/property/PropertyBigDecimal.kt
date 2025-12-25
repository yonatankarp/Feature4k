package com.yonatankarp.feature4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for BigDecimal values.
 *
 * @property name Unique name of the property
 * @property value Current big decimal value (stored as String)
 * @property description Optional description
 * @property fixedValues Set of allowed big decimal values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("bigDecimal")
data class PropertyBigDecimal(
    override val name: String,
    override val value: String,
    override val description: String? = null,
    override val fixedValues: Set<String> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<String>() {
    init {
        require(value.matches("""^-?\d+(\.\d+)?([eE][+-]?\d+)?$""".toRegex())) {
            "Value '$value' is not a valid BigDecimal"
        }
    }
}
