package com.yonatankarp.feature4k.security

import com.yonatankarp.feature4k.core.IdentifierFixtures.ALICE
import com.yonatankarp.feature4k.core.IdentifierFixtures.BOB
import com.yonatankarp.feature4k.security.PermissionFixtures.READ_PERMISSIONS
import com.yonatankarp.feature4k.security.PermissionFixtures.READ_WRITE_PERMISSIONS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class DefaultAuthorizationManagerTest : AuthorizationManagerContract() {

    override fun createManager(
        userName: String,
        permissions: Set<String>,
    ): AuthorizationManager = DefaultAuthorizationManager(
        currentUserName = userName,
        currentUserPermissions = permissions,
    )

    @Test
    fun `withPermissions returns new instance with updated permissions and keeps original unchanged`() {
        // Given
        val original = createManager(permissions = READ_PERMISSIONS) as DefaultAuthorizationManager

        // When
        val updated = original.withPermissions(READ_WRITE_PERMISSIONS)

        // Then
        assertNotSame(original, updated)

        assertEquals(ALICE, original.currentUserName)
        assertEquals(READ_PERMISSIONS, original.currentUserPermissions)

        assertEquals(ALICE, updated.currentUserName)
        assertEquals(READ_WRITE_PERMISSIONS, updated.currentUserPermissions)
    }

    @Test
    fun `withUserName returns new instance with updated user name and keeps original unchanged`() {
        // Given
        val original = createManager(permissions = READ_PERMISSIONS) as DefaultAuthorizationManager

        // When
        val updated = original.withUserName(BOB)

        // Then
        assertNotSame(original, updated)

        assertEquals(ALICE, original.currentUserName)
        assertEquals(READ_PERMISSIONS, original.currentUserPermissions)

        assertEquals(BOB, updated.currentUserName)
        assertEquals(READ_PERMISSIONS, updated.currentUserPermissions)
    }
}
