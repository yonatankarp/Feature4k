package com.yonatankarp.feature4k.utils

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UidTest {
    @Test
    fun `generate returns non-empty string`() {
        val uid = Uid.generate()
        assertTrue(uid.isNotEmpty(), "Generated UID should not be empty")
    }

    @Test
    fun `generate returns different values on subsequent calls`() {
        val uid1 = Uid.generate()
        val uid2 = Uid.generate()
        assertNotEquals(uid1, uid2, "Subsequent calls should generate different UIDs")
    }

    @Test
    fun `generated UID matches UUID format`() {
        val uid = Uid.generate()
        // UUID format: 8-4-4-4-12 hexadecimal characters separated by hyphens
        val uuidPattern = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        assertTrue(
            uuidPattern.matches(uid.lowercase()),
            "Generated UID should match UUID format, got: $uid",
        )
    }

    @Test
    fun `multiple UUIDs are unique`() {
        // Generate multiple UIDs to ensure uniqueness
        val uids = (1..100).map { Uid.generate() }.toSet()
        assertTrue(uids.size == 100, "All 100 generated UIDs should be unique")
    }
}
