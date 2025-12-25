package com.yonatankarp.feature4k.exception

import com.yonatankarp.feature4k.exception.Feature4kException.AuthorizationException

/**
 * Thrown when an authorization manager is required but not provided.
 *
 * @author Yonatan Karp-Rudin
 */
class AuthorizationsNotProvidedException(
    message: String = "Authorization manager is required but not provided",
    cause: Throwable? = null,
) : AuthorizationException(message, cause)

/**
 * Thrown when a user lacks the required permissions for an operation.
 *
 * @property requiredPermissions The permissions required for the operation
 * @property userPermissions The permissions the user actually has
 * @author Yonatan Karp-Rudin
 */
class InsufficientPermissionsException(
    val requiredPermissions: Set<String>,
    val userPermissions: Set<String>,
    cause: Throwable? = null,
) : AuthorizationException(
    "Insufficient permissions. Required: ${requiredPermissions.joinToString()}, " +
        "User has: ${userPermissions.joinToString()}",
    cause,
)
