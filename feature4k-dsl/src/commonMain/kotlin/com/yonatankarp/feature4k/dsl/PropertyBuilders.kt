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

    fun string(name: String, value: String, description: String? = null) {
        properties.add(PropertyString(name = name, value = value, description = description))
    }

    fun int(name: String, value: Int, description: String? = null) {
        properties.add(PropertyInt(name = name, value = value, description = description))
    }

    fun long(name: String, value: Long, description: String? = null) {
        properties.add(PropertyLong(name = name, value = value, description = description))
    }

    fun double(name: String, value: Double, description: String? = null) {
        properties.add(PropertyDouble(name = name, value = value, description = description))
    }

    fun float(name: String, value: Float, description: String? = null) {
        properties.add(PropertyFloat(name = name, value = value, description = description))
    }

    fun boolean(name: String, value: Boolean, description: String? = null) {
        properties.add(PropertyBoolean(name = name, value = value, description = description))
    }

    fun byte(name: String, value: Byte, description: String? = null) {
        properties.add(PropertyByte(name = name, value = value, description = description))
    }

    fun short(name: String, value: Short, description: String? = null) {
        properties.add(PropertyShort(name = name, value = value, description = description))
    }

    fun bigInteger(name: String, value: String, description: String? = null) {
        properties.add(PropertyBigInteger(name = name, value = value, description = description))
    }

    fun bigDecimal(name: String, value: String, description: String? = null) {
        properties.add(PropertyBigDecimal(name = name, value = value, description = description))
    }

    fun instant(name: String, value: Instant, description: String? = null) {
        properties.add(PropertyInstant(name = name, value = value, description = description))
    }

    fun localDateTime(name: String, value: LocalDateTime, description: String? = null) {
        properties.add(PropertyLocalDateTime(name = name, value = value, description = description))
    }

    fun <T> list(name: String, value: List<T>, description: String? = null) {
        properties.add(PropertyList(name = name, value = value, description = description))
    }

    fun <T> set(name: String, value: Set<T>, description: String? = null) {
        properties.add(PropertySet(name = name, value = value, description = description))
    }

    internal fun build(): List<Property<*>> = properties.toList()
}

/**
 * DSL entry point for creating a list of properties.
 *
 * ```kotlin
 * val props = properties {
 *     string("api.url", "https://api.example.com")
 *     int("max.connections", 100)
 * }
 * ```
 */
fun properties(block: PropertiesBuilder.() -> Unit): List<Property<*>> {
    val builder = PropertiesBuilder()
    builder.block()
    return builder.build()
}
