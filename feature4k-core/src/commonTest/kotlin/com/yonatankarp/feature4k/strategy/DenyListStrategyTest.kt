package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FlippingExecutionContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for DenyListStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class DenyListStrategyTest {
    @Test
    fun `should return false when user is in denylist`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2", "troll3"))
        val context = FlippingExecutionContext(user = "spammer1")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertFalse(result, "Strategy should return false for denied user")
    }

    @Test
    fun `should return true when user is not in denylist`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2", "troll3"))
        val context = FlippingExecutionContext(user = "alice")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true for non-denied user")
    }

    @Test
    fun `should return true when context has no user`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val context = FlippingExecutionContext.empty()

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true when context has no user")
    }

    @Test
    fun `should return true when context has null user`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val context = FlippingExecutionContext(user = null, client = "web-app")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true when user is null")
    }

    @Test
    fun `should return true when denylist is empty`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = emptySet())
        val context = FlippingExecutionContext(user = "anyone")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true when denylist is empty")
    }

    @Test
    fun `should handle single user in denylist`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1"))
        val spammerContext = FlippingExecutionContext(user = "spammer1")
        val aliceContext = FlippingExecutionContext(user = "alice")

        // When & Then
        assertFalse(strategy.evaluate(spammerContext), "Should deny spammer1")
        assertTrue(strategy.evaluate(aliceContext), "Should allow alice")
    }

    @Test
    fun `should be case-sensitive for user matching`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("Spammer"))
        val upperCaseContext = FlippingExecutionContext(user = "Spammer")
        val lowerCaseContext = FlippingExecutionContext(user = "spammer")

        // When & Then
        assertFalse(strategy.evaluate(upperCaseContext), "Should deny exact case 'Spammer'")
        assertTrue(strategy.evaluate(lowerCaseContext), "Should allow different case 'spammer'")
    }

    @Test
    fun `should handle denylist with many users`() {
        // Given
        val manyUsers = (1..100).map { "blocked$it" }.toSet()
        val strategy = DenyListStrategy(deniedUsers = manyUsers)
        val blocked50Context = FlippingExecutionContext(user = "blocked50")
        val allowed101Context = FlippingExecutionContext(user = "blocked101")

        // When & Then
        assertFalse(strategy.evaluate(blocked50Context), "Should deny blocked50")
        assertTrue(strategy.evaluate(allowed101Context), "Should allow blocked101 (not in denylist)")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2", "troll3"))
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"denylist\""), "Should contain type")
        assertTrue(serialized.contains("\"deniedUsers\""), "Should contain deniedUsers field")
        assertTrue(serialized.contains("\"spammer1\""), "Should contain spammer1")
        assertTrue(serialized.contains("\"abuser2\""), "Should contain abuser2")
        assertTrue(serialized.contains("\"troll3\""), "Should contain troll3")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "type": "denylist",
                "deniedUsers": ["spammer1", "abuser2", "troll3"]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is DenyListStrategy, "Should deserialize to DenyListStrategy")
        assertEquals(setOf("spammer1", "abuser2", "troll3"), strategy.deniedUsers)
    }

    @Test
    fun `should serialize and deserialize empty denylist`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = emptySet())
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is DenyListStrategy)
        assertTrue(deserialized.deniedUsers.isEmpty())
    }

    @Test
    fun `should use default empty set when not specified`() {
        // Given
        val strategy = DenyListStrategy()

        // When & Then
        assertTrue(strategy.deniedUsers.isEmpty(), "Default deniedUsers should be empty set")
        assertTrue(strategy.evaluate(FlippingExecutionContext(user = "anyone")))
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val strategy2 = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val strategy3 = DenyListStrategy(deniedUsers = setOf("spammer1", "troll3"))

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same users should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different users should not be equal")
    }

    @Test
    fun `should ignore other context fields`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1"))
        val contextWithExtras = FlippingExecutionContext(
            user = "alice",
            client = "mobile-app",
            server = "prod-server",
            customParams = mapOf("region" to "us-east"),
        )

        // When
        val result = strategy.evaluate(contextWithExtras)

        // Then
        assertTrue(result, "Should only care about user field, ignoring other context fields")
    }

    @Test
    fun `should work correctly with denied user and extra context`() {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1"))
        val contextWithExtras = FlippingExecutionContext(
            user = "spammer1",
            client = "mobile-app",
            server = "prod-server",
        )

        // When
        val result = strategy.evaluate(contextWithExtras)

        // Then
        assertFalse(result, "Should deny denied user even with extra context fields")
    }

    @Test
    fun `should demonstrate inverse behavior to allowlist`() {
        // Given
        val allowlistStrategy = AllowListStrategy(allowedUsers = setOf("alice", "bob"))
        val denylistStrategy = DenyListStrategy(deniedUsers = setOf("alice", "bob"))

        val aliceContext = FlippingExecutionContext(user = "alice")
        val charlieContext = FlippingExecutionContext(user = "charlie")
        val emptyContext = FlippingExecutionContext.empty()

        // When & Then - allowlist behavior
        assertTrue(allowlistStrategy.evaluate(aliceContext), "Allowlist allows alice")
        assertFalse(allowlistStrategy.evaluate(charlieContext), "Allowlist denies charlie")
        assertFalse(allowlistStrategy.evaluate(emptyContext), "Allowlist denies empty context")

        // When & Then - denylist behavior (inverse for users)
        assertFalse(denylistStrategy.evaluate(aliceContext), "Denylist denies alice")
        assertTrue(denylistStrategy.evaluate(charlieContext), "Denylist allows charlie")
        assertTrue(denylistStrategy.evaluate(emptyContext), "Denylist allows empty context")
    }
}
