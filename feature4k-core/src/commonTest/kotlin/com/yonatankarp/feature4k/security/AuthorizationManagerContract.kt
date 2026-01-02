package com.yonatankarp.feature4k.security

import com.yonatankarp.feature4k.core.IdentifierFixtures.ALICE
import com.yonatankarp.feature4k.security.PermissionFixtures.DELETE_PERMISSIONS
import com.yonatankarp.feature4k.security.PermissionFixtures.READ_DELETE_PERMISSIONS
import com.yonatankarp.feature4k.security.PermissionFixtures.READ_PERMISSIONS
import com.yonatankarp.feature4k.security.PermissionFixtures.READ_WRITE_PERMISSIONS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class AuthorizationManagerContract {

    /**
     * Create a fresh AuthorizationManager instance for each test.
     */
    abstract fun createManager(
        userName: String = ALICE,
        permissions: Set<String> = READ_WRITE_PERMISSIONS,
    ): AuthorizationManager

    @Test
    fun `isAllowedAny returns false when no permissions match`() {
        // Given
        val manager = createManager()

        // When
        val result = manager.isAllowedAny(DELETE_PERMISSIONS)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAllowedAny returns true when at least one permission matches`() {
        // Given
        val manager = createManager()

        // When
        val result = manager.isAllowedAny(READ_DELETE_PERMISSIONS)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isAllowedAll returns true only when all permissions match`() {
        // Given
        val manager = createManager()

        // When
        val isAllowedReadWrite = manager.isAllowedAll(READ_WRITE_PERMISSIONS)
        val isAllowedReadDelete = manager.isAllowedAll(READ_DELETE_PERMISSIONS)

        // Then
        assertTrue(isAllowedReadWrite)
        assertFalse(isAllowedReadDelete)
    }

    @Test
    fun `isAllowedAny returns false when user has no permissions`() {
        // Given
        val manager = createManager(permissions = emptySet())

        // When
        val result = manager.isAllowedAny(READ_PERMISSIONS)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAllowedAll returns true for empty required set`() {
        // Given
        val manager = createManager(permissions = emptySet())

        // When
        val result = manager.isAllowedAll(emptySet())

        // Then
        assertTrue(result)
    }

    @Test
    fun `currentUserName is exposed as provided`() {
        // Given
        val manager = createManager(permissions = emptySet())

        // When / Then
        assertEquals(ALICE, manager.currentUserName)
    }

    @Test
    fun `isAllowedAny returns false for empty required set`() {
        // Given
        val manager = createManager()

        // When
        val result = manager.isAllowedAny(emptySet())

        // Then
        assertFalse(result)
    }
}
