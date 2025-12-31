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
 * Tests for ServerFilterStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class ServerFilterStrategyTest {
    @Test
    fun `should return true when server is in granted list`() = runTest {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = setOf("server-1", "server-2", "canary-01"))
        val execContext = executionContextWithUser(server = "server-1")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true for granted server")
    }

    @Test
    fun `should return false when server is not in granted list`() = runTest {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = setOf("server-1", "server-2", "canary-01"))
        val execContext = executionContextWithUser(server = "server-99")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false for non-granted server")
    }

    @Test
    fun `should return false when context has no server`() = runTest {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = setOf("server-1", "server-2"))
        val execContext = emptyExecutionContext()
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when context has no server")
    }

    @Test
    fun `should return false when context has null server`() = runTest {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = setOf("server-1", "server-2"))
        val execContext = executionContextWithUser(user = "alice", server = null)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when server is null")
    }

    @Test
    fun `should return false when granted servers list is empty`() = runTest {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = emptySet())
        val execContext = executionContextWithUser(server = "server-1")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when granted servers list is empty")
    }

    @Test
    fun `should handle single server in granted list`() = runTest {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = setOf("canary-01"))
        val canaryExecContext = executionContextWithUser(server = "canary-01")
        val prodExecContext = executionContextWithUser(server = "prod-01")
        val store = InMemoryFeatureStore()
        val canaryEvalContext = featureEvaluationContext(context = canaryExecContext, store = store)
        val prodEvalContext = featureEvaluationContext(context = prodExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(canaryEvalContext), "Should allow canary-01")
        assertFalse(strategy.evaluate(prodEvalContext), "Should not allow prod-01")
    }

    @Test
    fun `should be case-sensitive for server matching`() = runTest {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = setOf("Server-1"))
        val upperCaseExecContext = executionContextWithUser(server = "Server-1")
        val lowerCaseExecContext = executionContextWithUser(server = "server-1")
        val store = InMemoryFeatureStore()
        val upperCaseEvalContext = featureEvaluationContext(context = upperCaseExecContext, store = store)
        val lowerCaseEvalContext = featureEvaluationContext(context = lowerCaseExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(upperCaseEvalContext), "Should match exact case 'Server-1'")
        assertFalse(strategy.evaluate(lowerCaseEvalContext), "Should not match different case 'server-1'")
    }

    @Test
    fun `should handle granted list with many servers`() = runTest {
        // Given
        val manyServers = (1..100).map { "server$it" }.toSet()
        val strategy = ServerFilterStrategy(grantedServers = manyServers)
        val server50ExecContext = executionContextWithUser(server = "server50")
        val server101ExecContext = executionContextWithUser(server = "server101")
        val store = InMemoryFeatureStore()
        val server50EvalContext = featureEvaluationContext(context = server50ExecContext, store = store)
        val server101EvalContext = featureEvaluationContext(context = server101ExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(server50EvalContext), "Should allow server50")
        assertFalse(strategy.evaluate(server101EvalContext), "Should not allow server101")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = setOf("server-1", "server-2", "canary-01"))
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"server-filter\""), "Should contain type")
        assertTrue(serialized.contains("\"grantedServers\""), "Should contain grantedServers field")
        assertTrue(serialized.contains("\"server-1\""), "Should contain server-1")
        assertTrue(serialized.contains("\"server-2\""), "Should contain server-2")
        assertTrue(serialized.contains("\"canary-01\""), "Should contain canary-01")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "server-filter",
                "grantedServers": ["server-1", "server-2", "canary-01"]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is ServerFilterStrategy, "Should deserialize to ServerFilterStrategy")
        assertEquals(setOf("server-1", "server-2", "canary-01"), strategy.grantedServers)
    }

    @Test
    fun `should serialize and deserialize empty granted servers`() {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = emptySet())
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is ServerFilterStrategy)
        assertTrue(deserialized.grantedServers.isEmpty())
    }

    @Test
    fun `should use default empty set when not specified`() = runTest {
        // Given
        val strategy = ServerFilterStrategy()
        val execContext = executionContextWithUser(server = "any-server")
        val evalContext = featureEvaluationContext(context = execContext)

        // When & Then
        assertTrue(strategy.grantedServers.isEmpty(), "Default grantedServers should be empty set")
        assertFalse(strategy.evaluate(evalContext))
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = ServerFilterStrategy(grantedServers = setOf("server-1", "server-2"))
        val strategy2 = ServerFilterStrategy(grantedServers = setOf("server-1", "server-2"))
        val strategy3 = ServerFilterStrategy(grantedServers = setOf("server-1", "canary-01"))

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same servers should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different servers should not be equal")
    }

    @Test
    fun `should ignore other context fields`() = runTest {
        // Given
        val strategy = ServerFilterStrategy(grantedServers = setOf("server-1"))
        val execContext = executionContextWithUser(
            user = "alice",
            client = "mobile-app",
            server = "server-1",
            customParams = mapOf("region" to "us-east"),
        )
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should only care about server field, ignoring other context fields")
    }
}
