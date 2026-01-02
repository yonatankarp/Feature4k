package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.IdentifierFixtures.ALICE
import com.yonatankarp.feature4k.core.IdentifierFixtures.BOB
import com.yonatankarp.feature4k.core.IdentifierFixtures.CHARLIE
import com.yonatankarp.feature4k.store.InMemoryFeatureStore
import com.yonatankarp.feature4k.strategy.ContextualStrategy.CombineWith
import com.yonatankarp.feature4k.strategy.FeatureEvaluationContextFixture.featureEvaluationContext
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
 * Tests for ContextualStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class ContextualStrategyTest {
    @Test
    fun `should return true when all sub-strategies pass with AND combination`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(
                AllowListStrategy(allowedUsers = setOf(ALICE, BOB)),
                AlwaysOnStrategy,
            ),
        )
        val execContext = executionContextWithUser(user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when all sub-strategies pass")
    }

    @Test
    fun `should return false when one sub-strategy fails with AND combination`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(
                AllowListStrategy(allowedUsers = setOf(ALICE)),
                AllowListStrategy(allowedUsers = setOf(BOB)),
            ),
        )
        val execContext = executionContextWithUser(user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when one sub-strategy fails with AND")
    }

    @Test
    fun `should return true when at least one sub-strategy passes with OR combination`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.OR,
            strategies = listOf(
                AllowListStrategy(allowedUsers = setOf(ALICE)),
                AllowListStrategy(allowedUsers = setOf(BOB)),
            ),
        )
        val execContext = executionContextWithUser(user = BOB)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when at least one sub-strategy passes with OR")
    }

    @Test
    fun `should return false when all sub-strategies fail with OR combination`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.OR,
            strategies = listOf(
                AllowListStrategy(allowedUsers = setOf(ALICE)),
                AllowListStrategy(allowedUsers = setOf(BOB)),
            ),
        )
        val execContext = executionContextWithUser(user = CHARLIE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when all sub-strategies fail with OR")
    }

    @Test
    fun `should handle empty strategies list with AND combination`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = emptyList(),
        )
        val execContext = executionContextWithUser(user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Empty list with AND should return true (vacuous truth)")
    }

    @Test
    fun `should handle empty strategies list with OR combination`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.OR,
            strategies = emptyList(),
        )
        val execContext = executionContextWithUser(user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Empty list with OR should return false")
    }

    @Test
    fun `should handle single sub-strategy with AND combination`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(AllowListStrategy(allowedUsers = setOf(ALICE))),
        )
        val aliceExecContext = executionContextWithUser(user = ALICE)
        val bobExecContext = executionContextWithUser(user = BOB)
        val store = InMemoryFeatureStore()
        val aliceEvalContext = featureEvaluationContext(context = aliceExecContext, store = store)
        val bobEvalContext = featureEvaluationContext(context = bobExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(aliceEvalContext), "Should pass for allowed user")
        assertFalse(strategy.evaluate(bobEvalContext), "Should fail for non-allowed user")
    }

    @Test
    fun `should handle nested contextual strategies`() = runTest {
        // Given
        val innerStrategy = ContextualStrategy(
            combineWith = CombineWith.OR,
            strategies = listOf(
                AllowListStrategy(allowedUsers = setOf(BOB)),
                AllowListStrategy(allowedUsers = setOf(CHARLIE)),
            ),
        )
        val outerStrategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(
                AllowListStrategy(allowedUsers = setOf(ALICE, BOB, CHARLIE)),
                innerStrategy,
            ),
        )
        val aliceExecContext = executionContextWithUser(user = ALICE)
        val bobExecContext = executionContextWithUser(user = BOB)
        val store = InMemoryFeatureStore()
        val aliceEvalContext = featureEvaluationContext(context = aliceExecContext, store = store)
        val bobEvalContext = featureEvaluationContext(context = bobExecContext, store = store)

        // When & Then
        assertFalse(outerStrategy.evaluate(aliceEvalContext), "Alice passes outer but fails inner")
        assertTrue(outerStrategy.evaluate(bobEvalContext), "Bob passes both outer and inner")
    }

    @Test
    fun `should handle complex nested composition`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(
                ContextualStrategy(
                    combineWith = CombineWith.OR,
                    strategies = listOf(
                        AllowListStrategy(allowedUsers = setOf(ALICE)),
                        AllowListStrategy(allowedUsers = setOf(BOB)),
                    ),
                ),
                ContextualStrategy(
                    combineWith = CombineWith.OR,
                    strategies = listOf(
                        AllowListStrategy(allowedUsers = setOf(BOB)),
                        AllowListStrategy(allowedUsers = setOf(CHARLIE)),
                    ),
                ),
            ),
        )
        val aliceExecContext = executionContextWithUser(user = ALICE)
        val bobExecContext = executionContextWithUser(user = BOB)
        val charlieExecContext = executionContextWithUser(user = CHARLIE)
        val store = InMemoryFeatureStore()
        val aliceEvalContext = featureEvaluationContext(context = aliceExecContext, store = store)
        val bobEvalContext = featureEvaluationContext(context = bobExecContext, store = store)
        val charlieEvalContext = featureEvaluationContext(context = charlieExecContext, store = store)

        // When & Then
        assertFalse(strategy.evaluate(aliceEvalContext), "Alice in first OR but not in second OR")
        assertTrue(strategy.evaluate(bobEvalContext), "Bob in both ORs")
        assertFalse(strategy.evaluate(charlieEvalContext), "Charlie in second OR but not in first OR")
    }

    @Test
    fun `should combine different strategy types`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(
                AllowListStrategy(allowedUsers = setOf(ALICE)),
                AlwaysOnStrategy,
            ),
        )
        val execContext = executionContextWithUser(user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should combine different strategy types correctly")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(
                AllowListStrategy(allowedUsers = setOf(ALICE)),
                AlwaysOnStrategy,
            ),
        )
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"contextual\""), "Should contain type")
        assertTrue(serialized.contains("\"combineWith\": \"AND\""), "Should contain combineWith")
        assertTrue(serialized.contains("\"strategies\""), "Should contain strategies field")
        assertTrue(serialized.contains("\"allowlist\""), "Should contain nested strategy type")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "type": "contextual",
                "combineWith": "OR",
                "strategies": [
                    {
                        "type": "allowlist",
                        "allowedUsers": ["$ALICE"]
                    },
                    {
                        "type": "always_on"
                    }
                ]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is ContextualStrategy, "Should deserialize to ContextualStrategy")
        assertEquals(CombineWith.OR, strategy.combineWith)
        assertEquals(2, strategy.strategies.size)
    }

    @Test
    fun `should serialize and deserialize empty strategies list`() {
        // Given
        val strategy = ContextualStrategy(combineWith = CombineWith.AND, strategies = emptyList())
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is ContextualStrategy)
        assertTrue(deserialized.strategies.isEmpty())
        assertEquals(CombineWith.AND, deserialized.combineWith)
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(AllowListStrategy(allowedUsers = setOf(ALICE))),
        )
        val strategy2 = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(AllowListStrategy(allowedUsers = setOf(ALICE))),
        )
        val strategy3 = ContextualStrategy(
            combineWith = CombineWith.OR,
            strategies = listOf(AllowListStrategy(allowedUsers = setOf(ALICE))),
        )

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same config should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different combineWith should not be equal")
    }

    @Test
    fun `should handle mix of always on and always off strategies with AND`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.AND,
            strategies = listOf(AlwaysOnStrategy, AlwaysOffStrategy),
        )
        val execContext = executionContextWithUser(user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "AND with AlwaysOff should return false")
    }

    @Test
    fun `should handle mix of always on and always off strategies with OR`() = runTest {
        // Given
        val strategy = ContextualStrategy(
            combineWith = CombineWith.OR,
            strategies = listOf(AlwaysOnStrategy, AlwaysOffStrategy),
        )
        val execContext = executionContextWithUser(user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "OR with AlwaysOn should return true")
    }
}
