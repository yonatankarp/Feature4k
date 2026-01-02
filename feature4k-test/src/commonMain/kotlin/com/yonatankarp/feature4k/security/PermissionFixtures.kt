package com.yonatankarp.feature4k.security

/**
 * Test fixture providing commonly used permission sets.
 *
 * This object centralizes reusable permission combinations for tests involving
 * authorization logic, improving readability and reducing duplication.
 *
 * All permissions are expressed as simple string identifiers and are intended
 * **for testing purposes only**.
 *
 * @author Yonatan Karp-Rudin
 */
object PermissionFixtures {
    /**
     * Permission set granting read-only access.
     */
    val READ_PERMISSIONS = setOf("READ")

    /**
     * Permission set granting write-only access.
     */
    val WRITE_PERMISSIONS = setOf("WRITE")

    /**
     * Permission set granting delete-only access.
     */
    val DELETE_PERMISSIONS = setOf("DELETE")

    /**
     * Permission set granting both read and write access.
     */
    val READ_WRITE_PERMISSIONS = setOf("READ", "WRITE")

    /**
     * Permission set granting read and delete access.
     */
    val READ_DELETE_PERMISSIONS = setOf("READ", "DELETE")
}
