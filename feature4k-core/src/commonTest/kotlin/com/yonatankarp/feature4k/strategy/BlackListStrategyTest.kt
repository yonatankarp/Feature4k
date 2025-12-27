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
 * Tests for BlackListStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class BlackListStrategyTest {
    @Test
    fun `should return false when user is in blacklist`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = setOf("spammer1", "abuser2", "troll3"))
        val context = FlippingExecutionContext(user = "spammer1")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertFalse(result, "Strategy should return false for blacklisted user")
    }

    @Test
    fun `should return true when user is not in blacklist`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = setOf("spammer1", "abuser2", "troll3"))
        val context = FlippingExecutionContext(user = "alice")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true for non-blacklisted user")
    }

    @Test
    fun `should return true when context has no user`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val context = FlippingExecutionContext.empty()

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true when context has no user")
    }

    @Test
    fun `should return true when context has null user`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val context = FlippingExecutionContext(user = null, client = "web-app")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true when user is null")
    }

    @Test
    fun `should return true when blacklist is empty`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = emptySet())
        val context = FlippingExecutionContext(user = "anyone")

        // When
        val result = strategy.evaluate(context)

        // Then
        assertTrue(result, "Strategy should return true when blacklist is empty")
    }

    @Test
    fun `should handle single user in blacklist`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = setOf("spammer1"))
        val spammerContext = FlippingExecutionContext(user = "spammer1")
        val aliceContext = FlippingExecutionContext(user = "alice")

        // When & Then
        assertFalse(strategy.evaluate(spammerContext), "Should deny spammer1")
        assertTrue(strategy.evaluate(aliceContext), "Should allow alice")
    }

    @Test
    fun `should be case-sensitive for user matching`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = setOf("Spammer"))
        val upperCaseContext = FlippingExecutionContext(user = "Spammer")
        val lowerCaseContext = FlippingExecutionContext(user = "spammer")

        // When & Then
        assertFalse(strategy.evaluate(upperCaseContext), "Should deny exact case 'Spammer'")
        assertTrue(strategy.evaluate(lowerCaseContext), "Should allow different case 'spammer'")
    }

    @Test
    fun `should handle blacklist with many users`() {
        // Given
        val manyUsers = (1..100).map { "blocked$it" }.toSet()
        val strategy = BlackListStrategy(deniedUsers = manyUsers)
        val blocked50Context = FlippingExecutionContext(user = "blocked50")
        val allowed101Context = FlippingExecutionContext(user = "blocked101")

        // When & Then
        assertFalse(strategy.evaluate(blocked50Context), "Should deny blocked50")
        assertTrue(strategy.evaluate(allowed101Context), "Should allow blocked101 (not in blacklist)")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = setOf("spammer1", "abuser2", "troll3"))
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"blacklist\""), "Should contain type")
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
                "type": "blacklist",
                "deniedUsers": ["spammer1", "abuser2", "troll3"]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is BlackListStrategy, "Should deserialize to BlackListStrategy")
        assertEquals(setOf("spammer1", "abuser2", "troll3"), strategy.deniedUsers)
    }

    @Test
    fun `should serialize and deserialize empty blacklist`() {
        // Given
        val strategy = BlackListStrategy(deniedUsers = emptySet())
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is BlackListStrategy)
        assertTrue(deserialized.deniedUsers.isEmpty())
    }

    @Test
    fun `should use default empty set when not specified`() {
        // Given
        val strategy = BlackListStrategy()

        // When & Then
        assertTrue(strategy.deniedUsers.isEmpty(), "Default deniedUsers should be empty set")
        assertTrue(strategy.evaluate(FlippingExecutionContext(user = "anyone")))
    }

    @Test
    fun `should be immutable`() {
        // Given
        val originalUsers = setOf("spammer1", "abuser2")
        val strategy = BlackListStrategy(deniedUsers = originalUsers)

        // When
        originalUsers + "troll3"

        // Then
        assertEquals(setOf("spammer1", "abuser2"), strategy.deniedUsers)
        assertTrue(strategy.evaluate(FlippingExecutionContext(user = "troll3")))
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = BlackListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val strategy2 = BlackListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val strategy3 = BlackListStrategy(deniedUsers = setOf("spammer1", "troll3"))

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same users should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different users should not be equal")
    }
}
