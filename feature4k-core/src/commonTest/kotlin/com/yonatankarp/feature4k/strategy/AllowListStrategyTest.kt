package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.store.InMemoryFeatureStore
import com.yonatankarp.feature4k.strategy.FeatureEvaluationContextFixture.featureEvaluationContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.emptyExecutionContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.executionContextWithUser
import kotlinx.coroutines.test.runTest
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
    fun `should return true when user is in allowlist`() = runTest {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob", "charlie"))
        val execContext = executionContextWithUser(user = "alice")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true for allowed user")
    }

    @Test
    fun `should return false when user is not in allowlist`() = runTest {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob", "charlie"))
        val execContext = executionContextWithUser(user = "eve")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false for non-allowed user")
    }

    @Test
    fun `should return false when context has no user`() = runTest {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob"))
        val execContext = emptyExecutionContext()
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when context has no user")
    }

    @Test
    fun `should return false when context has null user`() = runTest {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob"))
        val execContext = executionContextWithUser(user = null, client = "web-app")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when user is null")
    }

    @Test
    fun `should return false when allowlist is empty`() = runTest {
        // Given
        val strategy = AllowListStrategy(allowedUsers = emptySet())
        val execContext = executionContextWithUser(user = "alice")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when allowlist is empty")
    }

    @Test
    fun `should handle single user in allowlist`() = runTest {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice"))
        val aliceExecContext = executionContextWithUser(user = "alice")
        val bobExecContext = executionContextWithUser(user = "bob")
        val store = InMemoryFeatureStore()
        val aliceEvalContext = featureEvaluationContext(context = aliceExecContext, store = store)
        val bobEvalContext = featureEvaluationContext(context = bobExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(aliceEvalContext), "Should allow alice")
        assertFalse(strategy.evaluate(bobEvalContext), "Should not allow bob")
    }

    @Test
    fun `should be case-sensitive for user matching`() = runTest {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("Alice"))
        val upperCaseExecContext = executionContextWithUser(user = "Alice")
        val lowerCaseExecContext = executionContextWithUser(user = "alice")
        val store = InMemoryFeatureStore()
        val upperCaseEvalContext = featureEvaluationContext(context = upperCaseExecContext, store = store)
        val lowerCaseEvalContext = featureEvaluationContext(context = lowerCaseExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(upperCaseEvalContext), "Should match exact case 'Alice'")
        assertFalse(strategy.evaluate(lowerCaseEvalContext), "Should not match different case 'alice'")
    }

    @Test
    fun `should handle allowlist with many users`() = runTest {
        // Given
        val manyUsers = (1..100).map { "user$it" }.toSet()
        val strategy = AllowListStrategy(allowedUsers = manyUsers)
        val user50ExecContext = executionContextWithUser(user = "user50")
        val user101ExecContext = executionContextWithUser(user = "user101")
        val store = InMemoryFeatureStore()
        val user50EvalContext = featureEvaluationContext(context = user50ExecContext, store = store)
        val user101EvalContext = featureEvaluationContext(context = user101ExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(user50EvalContext), "Should allow user50")
        assertFalse(strategy.evaluate(user101EvalContext), "Should not allow user101")
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
        // language=json
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
    fun `should use default empty set when not specified`() = runTest {
        // Given
        val strategy = AllowListStrategy()
        val execContext = executionContextWithUser(user = "anyone")
        val evalContext = featureEvaluationContext(context = execContext)

        // When & Then
        assertTrue(strategy.allowedUsers.isEmpty(), "Default allowedUsers should be empty set")
        assertFalse(strategy.evaluate(evalContext))
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
    fun `should ignore other context fields`() = runTest {
        // Given
        val strategy = AllowListStrategy(allowedUsers = setOf("alice"))
        val execContext = executionContextWithUser(
            user = "alice",
            client = "mobile-app",
            server = "prod-server",
            customParams = mapOf("region" to "us-east"),
        )
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should only care about user field, ignoring other context fields")
    }
}
