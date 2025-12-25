package com.yonatankarp.feature4k.property

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

/**
 * Factory object for creating Property instances of various types.
 *
 * @author Yonatan Karp-Rudin
 */
object PropertyFactory {
    /**
     * Create a PropertyString configured with the given name and value.
     *
     * @param name The property's identifier.
     * @param value The property's string value.
     * @param description Optional human-readable description.
     * @param fixedValues Allowed set of values for the property.
     * @param readOnly When true, the property is immutable.
     * @return A PropertyString configured with the provided name, value, description, allowed values, and read-only flag.
     */
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

    /**
     * Creates an integer property with the provided name and initial value.
     *
     * @param name The property's identifier.
     * @param value The property's integer value.
     * @param description Optional human-readable description.
     * @param fixedValues Allowed set of values for the property.
     * @param readOnly When true, the property is immutable.
     * @return A PropertyInt configured with the provided name, value, description, fixed values, and read-only flag.
     */
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

    /**
     * Constructs a PropertyBoolean with the provided name, value, and optional metadata.
     *
     * @param name The property's identifier.
     * @param value The property's boolean value.
     * @param description Optional human-readable description for the property.
     * @param fixedValues Allowed set of values for the property.
     * @param readOnly Whether the property is read-only.
     * @return A PropertyBoolean initialized with the supplied arguments.
     */
    fun boolean(
        name: String,
        value: Boolean,
        description: String? = null,
        fixedValues: Set<Boolean> = emptySet(),
        readOnly: Boolean = false,
    ): PropertyBoolean = PropertyBoolean(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    /**
     * Creates a PropertyLong with the provided name, value, and optional metadata.
     *
     * @param name The property identifier.
     * @param value The long value of the property.
     * @param description Optional human-readable description.
     * @param fixedValues Optional set of allowed values for the property.
     * @param readOnly When true, marks the property as immutable.
     * @return A PropertyLong configured with the given parameters.
     */
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

    /**
     * Create a PropertyDouble with the provided name, value, and optional metadata.
     *
     * @param name The property's identifier.
     * @param value The property's double value.
     * @param description Optional human-readable description for the property.
     * @param fixedValues Optional set of allowed values for the property.
     * @param readOnly Whether the property is immutable.
     * @return The created PropertyDouble.
     */
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

    /**
     * Creates a PropertyFloat with the given name, value, and optional metadata.
     *
     * @param name The property's identifier.
     * @param value The initial float value.
     * @param description Optional human-readable description.
     * @param fixedValues Optional set of allowed float values for the property.
     * @param readOnly If `true`, the property is immutable after creation.
     * @return A configured [PropertyFloat] instance.
     */
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

    /**
     * Create a PropertyByte with the given name and byte value.
     *
     * @param name The property's name.
     * @param value The property's byte value.
     * @param description Optional human-readable description.
     * @param fixedValues Optional set of allowed byte values.
     * @param readOnly If `true`, the property is read-only and cannot be modified.
     * @return The created PropertyByte.
     */
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

    /**
     * Create a PropertyShort with the specified name, value, and optional metadata.
     *
     * @param name The property's identifier.
     * @param value The property's short value.
     * @param description Optional user-facing description.
     * @param fixedValues Optional set of allowed values for the property.
     * @param readOnly If `true`, the property is immutable.
     * @return A PropertyShort initialized with the provided arguments.
     */
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

    /**
     * Creates a PropertyBigInteger with the supplied metadata and value.
     *
     * @param name The property identifier.
     * @param value The decimal string representation of the BigInteger value.
     * @param description Optional human-readable description of the property.
     * @param fixedValues Optional set of allowed decimal string values for the property.
     * @param readOnly `true` if the property must not be modified, `false` otherwise.
     * @return A PropertyBigInteger populated with the provided name, value, description, fixed values, and readOnly flag.
     */
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

    /**
     * Create a PropertyBigDecimal with the provided name, string decimal value, and optional metadata.
     *
     * @param name The property's identifier.
     * @param value The decimal value encoded as a string.
     * @param description Optional human-readable description.
     * @param fixedValues Optional set of allowed decimal values (each encoded as a string).
     * @param readOnly When true, the property cannot be modified.
     * @return A PropertyBigDecimal initialized with the supplied values.
     */
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

    /**
     * Create a property representing an Instant value.
     *
     * @param name The property's identifier.
     * @param value The initial Instant value.
     * @param description Optional human-readable description.
     * @param fixedValues A set of permitted Instant values for the property.
     * @param readOnly `true` if the property cannot be modified, `false` otherwise.
     * @return A PropertyInstant populated with the provided name, value, description, fixedValues, and readOnly flag.
     */
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

    /**
     * Create a PropertyLocalDateTime with the given name, value, optional description, allowed values, and read-only flag.
     *
     * @param name The property's identifier.
     * @param value The initial LocalDateTime value.
     * @param description Optional human-readable description.
     * @param fixedValues A set of permitted LocalDateTime values for the property.
     * @param readOnly `true` if the property cannot be modified, `false` otherwise.
     * @return The created PropertyLocalDateTime.
     */
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

    /**
     * Creates a PropertyList that holds a list value and its metadata.
     *
     * @param name The property name.
     * @param value The initial list value.
     * @param description Optional human-readable description.
     * @param fixedValues Optional set of allowed list values.
     * @param readOnly If `true`, the property is read-only.
     * @return A PropertyList<T> representing the property configured with the provided value and metadata.
     */
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

    /**
     * Create a PropertySet<T> with the given name, value, and optional metadata.
     *
     * @param name The property identifier.
     * @param value The property's current set value.
     * @param description Optional human-readable description for the property.
     * @param fixedValues Optional set of allowed set values for the property.
     * @param readOnly If true, the property cannot be modified.
     * @return A PropertySet<T> configured with the provided name, value, description, fixed values, and read-only flag.
     */
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
