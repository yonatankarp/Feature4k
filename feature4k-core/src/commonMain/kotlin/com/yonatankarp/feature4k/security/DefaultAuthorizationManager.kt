package com.yonatankarp.feature4k.security

/**
 * Default immutable implementation of [AuthorizationsManager].
 *
 * This implementation represents a *snapshot* of the current user's
 * authorization state, consisting of the user's identity and the set of
 * permissions granted to them.
 *
 * Instances of [DefaultAuthorizationManager] are immutable. Any change to
 * the user name or permissions results in a *new instance*, created via the
 * provided `with…` methods.
 *
 * This design makes the class:
 * - Easy to reason about
 * - Safe to share across threads
 * - Well-suited for testing and functional-style usage
 *
 * @author Yonatan Karp-Rudin
 */
data class DefaultAuthorizationManager(
    /** @inheritDoc */
    override val currentUserName: String,
    /** @inheritDoc */
    override val currentUserPermissions: Set<String>,
) : AuthorizationsManager {

    /**
     * Returns a new [DefaultAuthorizationManager] with the given permissions.
     *
     * The original instance remains unchanged.
     *
     * @param permissions the new set of permissions for the current user
     * @return a new [DefaultAuthorizationManager] instance with updated permissions
     */
    fun withPermissions(permissions: Set<String>): DefaultAuthorizationManager = copy(currentUserPermissions = permissions)

    /**
     * Returns a new [DefaultAuthorizationManager] with the given user name.
     *
     * The original instance remains unchanged.
     *
     * @param userName the new user name or identifier
     * @return a new [DefaultAuthorizationManager] instance with updated user name
     */
    fun withUserName(userName: String): DefaultAuthorizationManager = copy(currentUserName = userName)
}
