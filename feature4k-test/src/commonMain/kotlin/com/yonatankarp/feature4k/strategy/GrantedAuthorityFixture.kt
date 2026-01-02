package com.yonatankarp.feature4k.strategy

/**
 * Test fixtures for GrantedAuthorityStrategy testing.
 * Provides common authority constants and sets for test consistency.
 *
 * @author Yonatan Karp-Rudin
 */
object GrantedAuthorityFixture {
    const val ROLE_ADMIN = "ROLE_ADMIN"
    const val ROLE_USER = "ROLE_USER"
    const val ROLE_MODERATOR = "ROLE_MODERATOR"

    val ADMIN_AND_USER_AUTHORITIES = setOf(ROLE_ADMIN, ROLE_USER)

    const val ADMIN_USER_MODERATOR_STRING = "ROLE_ADMIN,ROLE_USER,ROLE_MODERATOR"
    const val ADMIN_USER_MODERATOR_WITH_SPACES_STRING = "ROLE_ADMIN, ROLE_USER, ROLE_MODERATOR"
    const val ONLY_USER_STRING = "ROLE_USER"
    const val ONLY_ADMIN_STRING = "ROLE_ADMIN"
    const val LOWERCASE_ADMIN_STRING = "role_admin"
    const val EMPTY_AUTHORITIES_STRING = ""
    const val ADMIN_WITH_EMPTY_ENTRIES_STRING = "ROLE_ADMIN,,, ,"
}
