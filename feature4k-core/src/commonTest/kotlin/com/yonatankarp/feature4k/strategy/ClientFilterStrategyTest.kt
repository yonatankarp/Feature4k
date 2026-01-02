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
 * Tests for ClientFilterStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class ClientFilterStrategyTest {
    @Test
    fun `should return true when client is in granted list`() = runTest {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = setOf("mobile-app", "web-app", "admin-portal"))
        val execContext = executionContextWithUser(source = "mobile-app")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true for granted client")
    }

    @Test
    fun `should return false when client is not in granted list`() = runTest {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = setOf("mobile-app", "web-app", "admin-portal"))
        val execContext = executionContextWithUser(source = "legacy-app")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false for non-granted client")
    }

    @Test
    fun `should return false when context has no client`() = runTest {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = setOf("mobile-app", "web-app"))
        val execContext = emptyExecutionContext()
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when context has no client")
    }

    @Test
    fun `should return false when context has null client`() = runTest {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = setOf("mobile-app", "web-app"))
        val execContext = executionContextWithUser(user = "alice", source = null)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when client is null")
    }

    @Test
    fun `should return false when granted clients list is empty`() = runTest {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = emptySet())
        val execContext = executionContextWithUser(source = "mobile-app")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when granted clients list is empty")
    }

    @Test
    fun `should handle single client in granted list`() = runTest {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = setOf("mobile-app"))
        val mobileExecContext = executionContextWithUser(source = "mobile-app")
        val webExecContext = executionContextWithUser(source = "web-app")
        val store = InMemoryFeatureStore()
        val mobileEvalContext = featureEvaluationContext(context = mobileExecContext, store = store)
        val webEvalContext = featureEvaluationContext(context = webExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(mobileEvalContext), "Should allow mobile-app")
        assertFalse(strategy.evaluate(webEvalContext), "Should not allow web-app")
    }

    @Test
    fun `should be case-sensitive for client matching`() = runTest {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = setOf("Mobile-App"))
        val upperCaseExecContext = executionContextWithUser(source = "Mobile-App")
        val lowerCaseExecContext = executionContextWithUser(source = "mobile-app")
        val store = InMemoryFeatureStore()
        val upperCaseEvalContext = featureEvaluationContext(context = upperCaseExecContext, store = store)
        val lowerCaseEvalContext = featureEvaluationContext(context = lowerCaseExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(upperCaseEvalContext), "Should match exact case 'Mobile-App'")
        assertFalse(strategy.evaluate(lowerCaseEvalContext), "Should not match different case 'mobile-app'")
    }

    @Test
    fun `should handle granted list with many clients`() = runTest {
        // Given
        val manyClients = (1..100).map { "client$it" }.toSet()
        val strategy = ClientFilterStrategy(grantedClients = manyClients)
        val client50ExecContext = executionContextWithUser(source = "client50")
        val client101ExecContext = executionContextWithUser(source = "client101")
        val store = InMemoryFeatureStore()
        val client50EvalContext = featureEvaluationContext(context = client50ExecContext, store = store)
        val client101EvalContext = featureEvaluationContext(context = client101ExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(client50EvalContext), "Should allow client50")
        assertFalse(strategy.evaluate(client101EvalContext), "Should not allow client101")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = setOf("mobile-app", "web-app", "admin-portal"))
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"client-filter\""), "Should contain type")
        assertTrue(serialized.contains("\"grantedClients\""), "Should contain grantedClients field")
        assertTrue(serialized.contains("\"mobile-app\""), "Should contain mobile-app")
        assertTrue(serialized.contains("\"web-app\""), "Should contain web-app")
        assertTrue(serialized.contains("\"admin-portal\""), "Should contain admin-portal")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "client-filter",
                "grantedClients": ["mobile-app", "web-app", "admin-portal"]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is ClientFilterStrategy, "Should deserialize to ClientFilterStrategy")
        assertEquals(setOf("mobile-app", "web-app", "admin-portal"), strategy.grantedClients)
    }

    @Test
    fun `should serialize and deserialize empty granted clients`() {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = emptySet())
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is ClientFilterStrategy)
        assertTrue(deserialized.grantedClients.isEmpty())
    }

    @Test
    fun `should use default empty set when not specified`() = runTest {
        // Given
        val strategy = ClientFilterStrategy()
        val execContext = executionContextWithUser(source = "any-client")
        val evalContext = featureEvaluationContext(context = execContext)

        // When & Then
        assertTrue(strategy.grantedClients.isEmpty(), "Default grantedClients should be empty set")
        assertFalse(strategy.evaluate(evalContext))
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = ClientFilterStrategy(grantedClients = setOf("mobile-app", "web-app"))
        val strategy2 = ClientFilterStrategy(grantedClients = setOf("mobile-app", "web-app"))
        val strategy3 = ClientFilterStrategy(grantedClients = setOf("mobile-app", "admin-portal"))

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same clients should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different clients should not be equal")
    }

    @Test
    fun `should ignore other context fields`() = runTest {
        // Given
        val strategy = ClientFilterStrategy(grantedClients = setOf("mobile-app"))
        val execContext = executionContextWithUser(
            user = "alice",
            source = "mobile-app",
            host = "prod-server",
            customParams = mapOf("region" to "us-east"),
        )
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should only care about client field, ignoring other context fields")
    }
}
