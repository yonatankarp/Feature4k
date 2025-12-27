package com.yonatankarp.feature4k.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for UniformHash.
 *
 * @author Yonatan Karp-Rudin
 */
class UniformHashTest {
    @Test
    fun `should return deterministic hash for same input`() {
        // Given
        val input = "user123"

        // When
        val hash1 = UniformHash(input)
        val hash2 = UniformHash(input)
        val hash3 = UniformHash(input)

        // Then
        assertEquals(hash1, hash2, "Same input should produce same hash")
        assertEquals(hash2, hash3, "Same input should produce same hash")
    }

    @Test
    fun `should return values in valid range`() {
        // Given
        val inputs = listOf(
            "user1",
            "alice",
            "bob",
            "test@example.com",
            "user-with-special-chars_123",
            "",
        )

        // When & Then
        inputs.forEach { input ->
            val hash = UniformHash(input)
            assertTrue(
                hash in 0.0..<1.0,
                "Hash for '$input' should be in [0.0, 1.0), got $hash",
            )
        }
    }

    @Test
    fun `should produce different hashes for different inputs`() {
        // Given
        val inputs = (1..100).map { "user$it" }

        // When
        val hashes = inputs.map { UniformHash(it) }

        // Then
        val uniqueHashes = hashes.toSet()
        assertTrue(
            uniqueHashes.size > 95,
            "Should have mostly unique hashes, got ${uniqueHashes.size} unique out of ${inputs.size}",
        )
    }

    @Test
    fun `should distribute sequential strings uniformly`() {
        // Given
        val users = (1..1000).map { "user$it" }

        // When
        val hashes = users.map { UniformHash(it) }

        // Then - Check distribution across 10 buckets
        val buckets = hashes.groupBy { (it * 10).toInt() }
        buckets.values.forEach { bucket ->
            val bucketSize = bucket.size
            // Each bucket should have roughly 100 items (1000/10)
            assertTrue(
                bucketSize in 70..130,
                "Bucket should have 70-130 items, got $bucketSize",
            )
        }
    }

    @Test
    fun `should handle empty string`() {
        // Given
        val input = ""

        // When
        val hash = UniformHash(input)

        // Then
        assertTrue(hash in 0.0..<1.0, "Empty string should produce valid hash")
    }

    @Test
    fun `should handle special characters`() {
        // Given
        val specialInputs = listOf(
            "user@example.com",
            "user-with-dash",
            "user_with_underscore",
            "user.with.dots",
            "用户", // Chinese
            "пользователь", // Cyrillic
            "🎉🎊", // Emojis
            "user\nwith\nnewlines",
            "user\twith\ttabs",
        )

        // When & Then
        specialInputs.forEach { input ->
            val hash = UniformHash(input)
            assertTrue(
                hash in 0.0..<1.0,
                "Special input '$input' should produce valid hash",
            )
        }
    }

    @Test
    fun `should handle very long strings`() {
        // Given
        val longInput = "x".repeat(10000)

        // When
        val hash = UniformHash(longInput)

        // Then
        assertTrue(hash in 0.0..<1.0, "Long string should produce valid hash")
    }

    @Test
    fun `should produce different hashes for similar strings`() {
        // Given
        val similar = listOf("user1", "user2", "user10", "user11", "user100")

        // When
        val hashes = similar.map { UniformHash(it) }

        // Then
        val uniqueHashes = hashes.toSet()
        assertEquals(similar.size, uniqueHashes.size, "Similar strings should produce different hashes")
    }

    @Test
    fun `should not cluster for sequential numeric suffixes`() {
        // Given - Sequential IDs that might cluster with poor hash functions
        val users = (1..100).map { "user$it" }

        // When
        val hashes = users.map { UniformHash(it) }

        // Then - Check that values are spread across the range
        val min = hashes.minOrNull() ?: 0.0
        val max = hashes.maxOrNull() ?: 0.0

        assertTrue(
            min < 0.2,
            "Minimum hash should be in lower range, got $min",
        )
        assertTrue(
            max > 0.8,
            "Maximum hash should be in upper range, got $max",
        )
    }

    @Test
    fun `should have good avalanche effect`() {
        // Given
        val pairs = listOf(
            "user1" to "user2",
            "alice" to "alicf",
            "test" to "tesc",
        )

        // When & Then
        pairs.forEach { (input1, input2) ->
            val hash1 = UniformHash(input1)
            val hash2 = UniformHash(input2)

            assertNotEquals(hash1, hash2, "Different inputs should produce different hashes")
            val difference = kotlin.math.abs(hash1 - hash2)
            assertTrue(
                difference > 0.01,
                "Small input change should cause significant hash change, got difference $difference",
            )
        }
    }

    @Test
    fun `should match expected distribution for percentage thresholds`() {
        // Given
        val users = (1..1000).map { "user$it" }
        val threshold = 0.25 // 25%

        // When
        val belowThreshold = users.count { UniformHash(it) < threshold }

        // Then
        val percentage = belowThreshold.toDouble() / users.size
        // Should be within 5% of expected 25%
        assertTrue(
            percentage in 0.2375..0.2625,
            "25% threshold should match approximately, got ${percentage * 100}%",
        )
    }
}
