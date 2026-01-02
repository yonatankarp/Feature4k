package com.yonatankarp.feature4k.security

/**
 * Provides authorization decisions for the *current user*.
 *
 * An [AuthorizationsManager] exposes the identity of the current user and
 * the set of permissions granted to them, and offers convenience methods
 * for evaluating authorization requirements.
 *
 * This interface is intentionally minimal and Kotlin-first:
 * - It represents a *snapshot* of authorization state
 * - Implementations should be side-effect free
 * - Permission checks are deterministic and purely functional
 *
 * ### Permission semantics
 *
 * - Permissions are treated as opaque identifiers (usually strings).
 * - Authorization checks are **OR-based** ([isAllowedAny]) or **AND-based**
 *   ([isAllowedAll]).
 *
 * ### Thread-safety
 *
 * Implementations should be safe to use concurrently. Mutable implementations
 * are discouraged unless explicitly documented.
 *
 * @author Yonatan Karp-Rudin
 */
interface AuthorizationsManager {
    /**
     * The name or identifier of the current user.
     *
     * This value is intended for auditing, logging, or diagnostic purposes
     * and must uniquely identify the user within the current security context.
     */
    val currentUserName: String

    /**
     * The set of permissions granted to the current user.
     *
     * Implementations should return a complete and immutable snapshot of the
     * user's permissions at the time this [AuthorizationsManager] instance
     * was created.
     */
    val currentUserPermissions: Set<String>

    /**
     * Determines whether the current user is allowed access when **any**
     * of the required permissions is sufficient.
     *
     * An empty [required] set results in `false` since there are no
     * required permissions to match against.
     *
     * @param required the set of permissions required to perform an action
     * @return `true` if the current user possesses *at least one* of the
     *         required permissions; `false` otherwise
     */
    fun isAllowedAny(required: Set<String>): Boolean = required.any(currentUserPermissions::contains)

    /**
     * Determines whether the current user is allowed access when **all**
     * required permissions must be present.
     *
     * An empty [required] set is treated as having no requirements and
     * therefore results in `true`.
     *
     * @param required the set of permissions required to perform an action
     * @return `true` if the current user possesses *all* of the required
     *         permissions; `false` otherwise
     *
     * @implNote An empty [required] set always returns `true`.
     */
    fun isAllowedAll(required: Set<String>): Boolean = currentUserPermissions.containsAll(required)
}
