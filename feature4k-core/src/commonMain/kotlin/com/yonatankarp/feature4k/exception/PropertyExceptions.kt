package com.yonatankarp.feature4k.exception

import com.yonatankarp.feature4k.exception.Feature4kException.PropertyException

/**
 * Thrown when a requested property cannot be found in the store.
 *
 * @property propertyName The name of the property that was not found
 * @author Yonatan Karp-Rudin
 */
class PropertyNotFoundException(
    val propertyName: String,
    cause: Throwable? = null,
) : PropertyException("Property not found: $propertyName", cause)

/**
 * Thrown when attempting to create a property that already exists.
 *
 * @property propertyName The name of the property that already exists
 * @author Yonatan Karp-Rudin
 */
class PropertyAlreadyExistException(
    val propertyName: String,
    cause: Throwable? = null,
) : PropertyException("Property already exists: $propertyName", cause)

/**
 * Thrown when a property value doesn't match the expected type.
 *
 * @property propertyName The name of the property
 * @property expectedType The expected type of the property
 * @property actualType The actual type provided
 * @author Yonatan Karp-Rudin
 */
class InvalidPropertyTypeException(
    val propertyName: String,
    val expectedType: String,
    val actualType: String,
    cause: Throwable? = null,
) : PropertyException(
    "Invalid type for property '$propertyName'. Expected: $expectedType, got: $actualType",
    cause,
)

/**
 * Thrown when a property value is not within the allowed fixed values.
 *
 * @property propertyName The name of the property
 * @property value The invalid value
 * @property allowedValues The set of allowed values
 * @author Yonatan Karp-Rudin
 */
class InvalidPropertyValueException(
    val propertyName: String,
    val value: String,
    val allowedValues: Set<String>,
    cause: Throwable? = null,
) : PropertyException(
    "Invalid value '$value' for property '$propertyName'. Allowed values: ${allowedValues.joinToString()}",
    cause,
)
