package com.yonatankarp.feature4k.exception

import com.yonatankarp.feature4k.exception.Feature4kException.FeatureException

/**
 * Thrown when a requested feature cannot be found in the store.
 *
 * @property featureUid The unique identifier of the feature that was not found
 * @author Yonatan Karp-Rudin
 */
class FeatureNotFoundException(
    val featureUid: String,
    cause: Throwable? = null,
) : FeatureException("Feature not found: $featureUid", cause)

/**
 * Thrown when attempting to create a feature that already exists.
 *
 * @property featureUid The unique identifier of the feature that already exists
 * @author Yonatan Karp-Rudin
 */
class FeatureAlreadyExistException(
    val featureUid: String,
    cause: Throwable? = null,
) : FeatureException("Feature already exists: $featureUid", cause)

/**
 * Thrown when a feature's unique identifier is invalid.
 *
 * @property featureUid The invalid feature identifier
 * @property reason The reason why the identifier is invalid
 * @author Yonatan Karp-Rudin
 */
class InvalidFeatureIdentifierException(
    val featureUid: String,
    val reason: String,
    cause: Throwable? = null,
) : FeatureException("Invalid feature identifier '$featureUid': $reason", cause)

/**
 * Thrown when access to a feature is denied due to insufficient permissions.
 *
 * @property featureUid The unique identifier of the feature
 * @property requiredPermissions The permissions required to access the feature
 * @author Yonatan Karp-Rudin
 */
class FeatureAccessException(
    val featureUid: String,
    val requiredPermissions: Set<String>,
    cause: Throwable? = null,
) : FeatureException(
    "Access denied to feature '$featureUid'. Required permissions: ${requiredPermissions.joinToString()}",
    cause,
)
