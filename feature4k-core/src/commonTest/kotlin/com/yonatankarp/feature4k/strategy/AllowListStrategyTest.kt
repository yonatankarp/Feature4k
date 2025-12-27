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
 * Tests for AllowListStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class AllowListStrategyTest {
    @Test
    fun `should return true when user is in allowlist`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob", "charlie"))
        val context = FlippingExecutionContext(user = "alice")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true for allowed user")
    }

    @Test
    fun `should return false when user is not in allowlist`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob", "charlie"))
        val context = FlippingExecutionContext(user = "eve")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertFalse(result, "Strategy should return false for non-allowed user")
    }

    @Test
    fun `should return false when context has no user`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob"))
        val context = FlippingExecutionContext.empty()

        // When
        val result = strategy.evaluate(context)

        // Then
        assertFalse(result, "Strategy should return false when context has no user")
    }

    @Test
    fun `should return false when context has null user`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob"))
        val context = FlippingExecutionContext(user = null, client = "web-app")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertFalse(result, "Strategy should return false when user is null")
    }

    @Test
    fun `should return false when allowlist is empty`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = emptySet())
        val context = FlippingExecutionContext(user = "alice")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertFalse(result, "Strategy should return false when allowlist is empty")
    }

    @Test
    fun `should handle single user in allowlist`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice"))
        val aliceContext = FlippingExecutionContext(user = "alice")
        val bobContext = FlippingExecutionContext(user = "bob")

        // When & Then
        assertTrue(strategy.evaluate(aliceContext), "Should allow alice")
        assertFalse(strategy.evaluate(bobContext), "Should not allow bob")
    }

    @Test
    fun `should be case-sensitive for user matching`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("Alice"))
        val upperCaseContext = FlippingExecutionContext(user = "Alice")
        val lowerCaseContext = FlippingExecutionContext(user = "alice")

        // When & Then
        assertTrue(strategy.evaluate(upperCaseContext), "Should match exact case 'Alice'")
        assertFalse(strategy.evaluate(lowerCaseContext), "Should not match different case 'alice'")
    }

    @Test
    fun `should handle allowlist with many users`() {
        // Given
        val manyUsers = (1..100).map { "user$it" }.toSet()
        val strategy = AllowListStrategy(allowedUsers = manyUsers)
        val user50Context = FlippingExecutionContext(user = "user50")
        val user101Context = FlippingExecutionContext(user = "user101")

        // When & Then
        assertTrue(strategy.evaluate(user50Context), "Should allow user50")
        assertFalse(strategy.evaluate(user101Context), "Should not allow user101")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob", "charlie"))
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"allowlist\""), "Should contain type")
        assertTrue(serialized.contains("\"allowedUsers\""), "Should contain allowedUsers field")
        assertTrue(serialized.contains("\"alice\""), "Should contain alice")
        assertTrue(serialized.contains("\"bob\""), "Should contain bob")
        assertTrue(serialized.contains("\"charlie\""), "Should contain charlie")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "type": "allowlist",
                "allowedUsers": ["alice", "bob", "charlie"]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is AllowListStrategy, "Should deserialize to AllowListStrategy")
        assertEquals(setOf("alice", "bob", "charlie"), strategy.allowedUsers)
    }

    @Test
    fun `should serialize and deserialize empty allowlist`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = emptySet())
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is AllowListStrategy)
        assertTrue(deserialized.allowedUsers.isEmpty())
    }

    @Test
    fun `should use default empty set when not specified`() {
        // Given
        val strategy = AllowListStrategy()

        // When & Then
        assertTrue(strategy.allowedUsers.isEmpty(), "Default allowedUsers should be empty set")
        assertFalse(strategy.evaluate(FlippingExecutionContext(user = "anyone")))
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = AllowListStrategy(allowedUsers = setOf("alice", "bob"))
        val strategy2 = AllowListStrategy(allowedUsers = setOf("alice", "bob"))
        val strategy3 = AllowListStrategy(allowedUsers = setOf("alice", "charlie"))

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same users should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different users should not be equal")
    }

    @Test
    fun `should ignore other context fields`() {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice"))
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
}
