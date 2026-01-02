package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.property.Property
import com.yonatankarp.feature4k.property.PropertyBigDecimal
import com.yonatankarp.feature4k.property.PropertyBigInteger
import com.yonatankarp.feature4k.property.PropertyBoolean
import com.yonatankarp.feature4k.property.PropertyByte
import com.yonatankarp.feature4k.property.PropertyDouble
import com.yonatankarp.feature4k.property.PropertyFloat
import com.yonatankarp.feature4k.property.PropertyInstant
import com.yonatankarp.feature4k.property.PropertyInt
import com.yonatankarp.feature4k.property.PropertyList
import com.yonatankarp.feature4k.property.PropertyLocalDateTime
import com.yonatankarp.feature4k.property.PropertyLong
import com.yonatankarp.feature4k.property.PropertySet
import com.yonatankarp.feature4k.property.PropertyShort
import com.yonatankarp.feature4k.property.PropertyString
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

@Feature4KDsl
class PropertiesBuilder {
    private val properties = mutableListOf<Property<*>>()

    /**
     * Adds a string property with the given name and value to the builder.
     *
     * @param name The property's identifier.
     * @param value The string value for the property.
     * @param description Optional human-readable description for the property.
     */
    fun string(name: String, value: String, description: String? = null) {
        properties.add(PropertyString(name = name, value = value, description = description))
    }

    /**
     * Adds an integer property with the given name, value, and optional description to the builder.
     *
     * @param name The property's identifier.
     * @param value The integer value for the property.
     * @param description Optional human-readable description for the property.
     */
    fun int(name: String, value: Int, description: String? = null) {
        properties.add(PropertyInt(name = name, value = value, description = description))
    }

    /**
     * Adds a long-typed property to the builder.
     *
     * @param name The property's name.
     * @param value The property's long value.
     * @param description Optional human-readable description for the property.
     */
    fun long(name: String, value: Long, description: String? = null) {
        properties.add(PropertyLong(name = name, value = value, description = description))
    }

    /**
     * Adds a double property with the given name and value to the builder.
     *
     * @param name The property name.
     * @param value The double value for the property.
     * @param description Optional human-readable description for the property.
     */
    fun double(name: String, value: Double, description: String? = null) {
        properties.add(PropertyDouble(name = name, value = value, description = description))
    }

    /**
     * Add a float property with the given name, value, and optional description to the builder.
     *
     * @param name The property's name.
     * @param value The property's float value.
     * @param description Optional human-readable description for the property.
     */
    fun float(name: String, value: Float, description: String? = null) {
        properties.add(PropertyFloat(name = name, value = value, description = description))
    }

    /**
     * Adds a boolean property with the given name, value, and optional description to the builder.
     *
     * @param name The property's identifier.
     * @param value The boolean value for the property.
     * @param description Optional human-readable description for the property.
     */
    fun boolean(name: String, value: Boolean, description: String? = null) {
        properties.add(PropertyBoolean(name = name, value = value, description = description))
    }

    /**
     * Adds a byte property to the builder.
     *
     * @param name The property's name.
     * @param value The byte value for the property.
     * @param description Optional human-readable description for the property.
     */
    fun byte(name: String, value: Byte, description: String? = null) {
        properties.add(PropertyByte(name = name, value = value, description = description))
    }

    /**
     * Adds a Short property with the given name and value to the builder's properties.
     *
     * @param name The property's identifier.
     * @param value The short value for the property.
     * @param description Optional human-readable description for the property.
     */
    fun short(name: String, value: Short, description: String? = null) {
        properties.add(PropertyShort(name = name, value = value, description = description))
    }

    /**
     * Adds a BigInteger property with the given name and decimal string value.
     *
     * @param name The property's name.
     * @param value The integer value represented as a decimal string.
     * @param description Optional human-readable description for the property.
     */
    fun bigInteger(name: String, value: String, description: String? = null) {
        properties.add(PropertyBigInteger(name = name, value = value, description = description))
    }

    /**
     * Adds a BigDecimal property with the given name and decimal value represented as a string.
     *
     * @param name The property's unique name.
     * @param value The decimal value as a string (exact decimal representation).
     * @param description Optional human-readable description of the property.
     */
    fun bigDecimal(name: String, value: String, description: String? = null) {
        properties.add(PropertyBigDecimal(name = name, value = value, description = description))
    }

    /**
     * Adds a property whose value is an Instant to this builder.
     *
     * @param name The property's name.
     * @param value The Instant value of the property.
     * @param description Optional human-readable description for the property.
     */
    fun instant(name: String, value: Instant, description: String? = null) {
        properties.add(PropertyInstant(name = name, value = value, description = description))
    }

    /**
     * Adds a LocalDateTime property with the given name and value to the builder.
     *
     * @param name The property's identifier.
     * @param value The LocalDateTime value for the property.
     * @param description Optional human-readable description for the property.
     */
    fun localDateTime(name: String, value: LocalDateTime, description: String? = null) {
        properties.add(PropertyLocalDateTime(name = name, value = value, description = description))
    }

    /**
     * Adds a list-typed property with the specified name and value to the builder.
     *
     * @param name The property's identifier.
     * @param value The list value for the property.
     * @param description Optional human-readable description for the property.
     */
    fun <T> list(name: String, value: List<T>, description: String? = null) {
        properties.add(PropertyList(name = name, value = value, description = description))
    }

    /**
     * Adds a property representing a set with the given name, value, and optional description to the builder.
     *
     * @param name The property's name.
     * @param value The property's value as a set of items.
     * @param description Optional human-readable description.
     */
    fun <T> set(name: String, value: Set<T>, description: String? = null) {
        properties.add(PropertySet(name = name, value = value, description = description))
    }

    /**
     * Produce an immutable snapshot of the accumulated properties.
     *
     * @return A list containing all properties that have been added to the builder.
     */
    internal fun build(): List<Property<*>> = properties.toList()
}

/**
 * DSL entry point for constructing a list of properties.
 *
 * @param block Lambda with receiver that configures the builder by declaring properties.
 * @return A list of created Property instances.
 */
fun properties(block: PropertiesBuilder.() -> Unit): List<Property<*>> {
    val builder = PropertiesBuilder()
    builder.block()
    return builder.build()
}
