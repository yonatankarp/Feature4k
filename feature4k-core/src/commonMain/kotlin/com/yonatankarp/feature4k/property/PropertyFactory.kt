package com.yonatankarp.feature4k.property

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

object PropertyFactory {
    fun string(
        name: String,
        value: String,
        description: String? = null,
        fixedValues: Set<String> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyString = PropertyString(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun int(
        name: String,
        value: Int,
        description: String? = null,
        fixedValues: Set<Int> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyInt = PropertyInt(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun boolean(
        name: String,
        value: Boolean,
        description: String? = null,
        readOnly: Boolean = false,
    ): PropertyBoolean = PropertyBoolean(
        name = name,
        value = value,
        description = description,
        readOnly = readOnly,
    )

    fun long(
        name: String,
        value: Long,
        description: String? = null,
        fixedValues: Set<Long> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyLong = PropertyLong(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun double(
        name: String,
        value: Double,
        description: String? = null,
        fixedValues: Set<Double> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyDouble = PropertyDouble(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun float(
        name: String,
        value: Float,
        description: String? = null,
        fixedValues: Set<Float> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyFloat = PropertyFloat(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun byte(
        name: String,
        value: Byte,
        description: String? = null,
        fixedValues: Set<Byte> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyByte = PropertyByte(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun short(
        name: String,
        value: Short,
        description: String? = null,
        fixedValues: Set<Short> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyShort = PropertyShort(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun bigInteger(
        name: String,
        value: String,
        description: String? = null,
        fixedValues: Set<String> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyBigInteger = PropertyBigInteger(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun bigDecimal(
        name: String,
        value: String,
        description: String? = null,
        fixedValues: Set<String> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyBigDecimal = PropertyBigDecimal(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun instant(
        name: String,
        value: Instant,
        description: String? = null,
        fixedValues: Set<Instant> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyInstant = PropertyInstant(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun localDateTime(
        name: String,
        value: LocalDateTime,
        description: String? = null,
        fixedValues: Set<LocalDateTime> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyLocalDateTime = PropertyLocalDateTime(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun <T> list(
        name: String,
        value: List<T>,
        description: String? = null,
        fixedValues: Set<List<T>> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyList<T> = PropertyList(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    fun <T> set(
        name: String,
        value: Set<T>,
        description: String? = null,
        fixedValues: Set<Set<T>> = emptySet(),
        readOnly: Boolean = false,
    ): PropertySet<T> = PropertySet(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )
}
