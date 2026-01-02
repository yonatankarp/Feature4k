package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.store.InMemoryFeatureStore
import com.yonatankarp.feature4k.strategy.FeatureEvaluationContextFixture.featureEvaluationContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.REGION_US_EAST
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
 * Tests for DenyListStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class DenyListStrategyTest {
    @Test
    fun `should return false when user is in denylist`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2", "troll3"))
        val execContext = executionContextWithUser(user = "spammer1")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false for denied user")
    }

    @Test
    fun `should return true when user is not in denylist`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2", "troll3"))
        val execContext = executionContextWithUser(user = "alice")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true for non-denied user")
    }

    @Test
    fun `should return true when context has no user`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val execContext = emptyExecutionContext()
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when context has no user")
    }

    @Test
    fun `should return true when context has null user`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
        val execContext = executionContextWithUser(user = null, source = "web-app")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when user is null")
    }

    @Test
    fun `should return true when denylist is empty`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = emptySet())
        val execContext = executionContextWithUser(user = "anyone")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when denylist is empty")
    }

    @Test
    fun `should handle single user in denylist`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1"))
        val spammerExecContext = executionContextWithUser(user = "spammer1")
        val aliceExecContext = executionContextWithUser(user = "alice")
        val store = InMemoryFeatureStore()
        val spammerEvalContext = featureEvaluationContext(context = spammerExecContext, store = store)
        val aliceEvalContext = featureEvaluationContext(context = aliceExecContext, store = store)

        // When & Then
        assertFalse(strategy.evaluate(spammerEvalContext), "Should deny spammer1")
        assertTrue(strategy.evaluate(aliceEvalContext), "Should allow alice")
    }

    @Test
    fun `should be case-sensitive for user matching`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("Spammer"))
        val upperCaseExecContext = executionContextWithUser(user = "Spammer")
        val lowerCaseExecContext = executionContextWithUser(user = "spammer")
        val store = InMemoryFeatureStore()
        val upperCaseEvalContext = featureEvaluationContext(context = upperCaseExecContext, store = store)
        val lowerCaseEvalContext = featureEvaluationContext(context = lowerCaseExecContext, store = store)

        // When & Then
        assertFalse(strategy.evaluate(upperCaseEvalContext), "Should deny exact case 'Spammer'")
        assertTrue(strategy.evaluate(lowerCaseEvalContext), "Should allow different case 'spammer'")
    }

    @Test
    fun `should handle denylist with many users`() = runTest {
        // Given
        val manyUsers = (1..100).map { "blocked$it" }.toSet()
        val strategy = DenyListStrategy(deniedUsers = manyUsers)
        val blocked50ExecContext = executionContextWithUser(user = "blocked50")
        val allowed101ExecContext = executionContextWithUser(user = "blocked101")
        val store = InMemoryFeatureStore()
        val blocked50EvalContext = featureEvaluationContext(context = blocked50ExecContext, store = store)
        val allowed101EvalContext = featureEvaluationContext(context = allowed101ExecContext, store = store)

        // When & Then
        assertFalse(strategy.evaluate(blocked50EvalContext), "Should deny blocked50")
        assertTrue(strategy.evaluate(allowed101EvalContext), "Should allow blocked101 (not in denylist)")
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
        // language=json
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
    fun `should use default empty set when not specified`() = runTest {
        // Given
        val strategy = DenyListStrategy()
        val execContext = executionContextWithUser(user = "anyone")
        val evalContext = featureEvaluationContext(context = execContext)

        // When & Then
        assertTrue(strategy.deniedUsers.isEmpty(), "Default deniedUsers should be empty set")
        assertTrue(strategy.evaluate(evalContext))
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
    fun `should ignore other context fields`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1"))
        val execContext = executionContextWithUser(
            user = "alice",
            source = "mobile-app",
            host = "prod-server",
            customParams = mapOf("region" to REGION_US_EAST),
        )
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should only care about user field, ignoring other context fields")
    }

    @Test
    fun `should work correctly with denied user and extra context`() = runTest {
        // Given
        val strategy = DenyListStrategy(deniedUsers = setOf("spammer1"))
        val execContext = executionContextWithUser(
            user = "spammer1",
            source = "mobile-app",
            host = "prod-server",
        )
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should deny denied user even with extra context fields")
    }

    @Test
    fun `should demonstrate inverse behavior to allowlist`() = runTest {
        // Given
        val allowlistStrategy = AllowListStrategy(allowedUsers = setOf("alice", "bob"))
        val denylistStrategy = DenyListStrategy(deniedUsers = setOf("alice", "bob"))

        val aliceExecContext = executionContextWithUser(user = "alice")
        val charlieExecContext = executionContextWithUser(user = "charlie")
        val emptyExecContext = emptyExecutionContext()
        val store = InMemoryFeatureStore()
        val aliceEvalContext = featureEvaluationContext(context = aliceExecContext, store = store)
        val charlieEvalContext = featureEvaluationContext(context = charlieExecContext, store = store)
        val emptyEvalContext = featureEvaluationContext(context = emptyExecContext, store = store)

        // When & Then - allowlist behavior
        assertTrue(allowlistStrategy.evaluate(aliceEvalContext), "Allowlist allows alice")
        assertFalse(allowlistStrategy.evaluate(charlieEvalContext), "Allowlist denies charlie")
        assertFalse(allowlistStrategy.evaluate(emptyEvalContext), "Allowlist denies empty context")

        // When & Then - denylist behavior (inverse for users)
        assertFalse(denylistStrategy.evaluate(aliceEvalContext), "Denylist denies alice")
        assertTrue(denylistStrategy.evaluate(charlieEvalContext), "Denylist allows charlie")
        assertTrue(denylistStrategy.evaluate(emptyEvalContext), "Denylist allows empty context")
    }
}
