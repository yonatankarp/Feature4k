package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.strategy.FeatureEvaluationContextFixture.featureEvaluationContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.REGION_US_EAST
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.emptyExecutionContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.executionContextWithUser
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for PonderationStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class PonderationStrategyTest {
    @Test
    fun `should enable feature for approximately correct percentage of users`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val users = (1..1000).map { "user$it" }

        // When
        val enabledCount = users.count { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext =
                featureEvaluationContext(context = execContext)
            strategy.evaluate(evalContext)
        }

        // Then
        val enabledPercentage = enabledCount.toDouble() / users.size
        assertPercentageWithin(expected = 0.5, actual = enabledPercentage)
    }

    @Test
    fun `should be deterministic for same user`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val execContext = executionContextWithUser(user = "alice")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val firstEvaluation = strategy.evaluate(evalContext)
        val evaluations = (1..100).map { strategy.evaluate(evalContext) }

        // Then
        assertTrue(
            evaluations.all { it == firstEvaluation },
            "Same user should always get same result",
        )
    }

    @Test
    fun `should enable all users when weight is 1_0`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FULL)
        val users = (1..100).map { "user$it" }

        // When
        val results = users.map { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext = featureEvaluationContext(context = execContext)
            strategy.evaluate(evalContext)
        }

        // Then
        assertTrue(
            results.all { it },
            "All users should be enabled when weight is 1.0",
        )
    }

    @Test
    fun `should disable all users when weight is 0_0`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.ZERO)
        val users = (1..100).map { "user$it" }

        // When
        val results = users.map { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext = featureEvaluationContext(context = execContext)
            strategy.evaluate(evalContext)
        }

        // Then
        assertTrue(
            results.all { it.not() },
            "All users should be disabled when weight is 0.0",
        )
    }

    @Test
    fun `should enable approximately 25 percent of users when weight is 0_25`() = runTest {
        // Given
        val strategy =
            PonderationStrategy(weight = Weight.TWENTY_FIVE_PERCENT)
        val users = (1..1000).map { "user$it" }

        // When
        val enabledCount = users.count { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext =
                featureEvaluationContext(context = execContext)
            strategy.evaluate(evalContext)
        }

        // Then
        val enabledPercentage = enabledCount.toDouble() / users.size
        assertPercentageWithin(expected = 0.25, actual = enabledPercentage)
    }

    @Test
    fun `should enable approximately 75 percent of users when weight is 0_75`() = runTest {
        // Given
        val strategy =
            PonderationStrategy(weight = Weight.SEVENTY_FIVE_PERCENT)
        val users = (1..1000).map { "user$it" }

        // When
        val enabledCount = users.count { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext =
                featureEvaluationContext(context = execContext)
            strategy.evaluate(evalContext)
        }

        // Then
        val enabledPercentage = enabledCount.toDouble() / users.size
        assertPercentageWithin(expected = 0.75, actual = enabledPercentage)
    }

    @Test
    fun `should distribute users evenly across hash space`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val users = (1..1000).map { "user$it" }

        // When
        val grouped = users.groupBy { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext = featureEvaluationContext(context = execContext)
            strategy.evaluate(evalContext)
        }

        // Then
        assertTrue(grouped.containsKey(true), "Should have enabled users")
        assertTrue(grouped.containsKey(false), "Should have disabled users")
        val enabledCount = grouped[true]?.size ?: 0
        val disabledCount = grouped[false]?.size ?: 0
        assertTrue(
            enabledCount in 400..600 && disabledCount in 400..600,
            "Distribution should be balanced: $enabledCount enabled, $disabledCount disabled",
        )
    }

    @Test
    fun `should use random evaluation when context has no user`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val execContext = emptyExecutionContext()
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val evaluations = (1..100).map { strategy.evaluate(evalContext) }

        // Then
        val hasTrue = evaluations.any { it }
        val hasFalse = evaluations.any { it.not() }
        assertTrue(
            hasTrue && hasFalse,
            "Without user, evaluation should be random and produce both true and false",
        )
    }

    @Test
    fun `should use random evaluation when user is null`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val execContext =
            executionContextWithUser(user = null, source = "web-app")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val evaluations = (1..100).map { strategy.evaluate(evalContext) }

        // Then
        val hasTrue = evaluations.any { it }
        val hasFalse = evaluations.any { it.not() }
        assertTrue(
            hasTrue && hasFalse,
            "With null user, evaluation should be random",
        )
    }

    @Test
    fun `should throw IllegalArgumentException when weight is negative`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            PonderationStrategy(weight = -0.1)
        }
        assertEquals(exception.message?.contains("between 0.0 and 1.0"), true)
    }

    @Test
    fun `should throw IllegalArgumentException when weight is greater than 1_0`() {
        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            PonderationStrategy(weight = 1.1)
        }
        assertEquals(exception.message?.contains("between 0.0 and 1.0"), true)
    }

    @Test
    fun `should default to 0_5 weight when not specified`() {
        // Given
        val strategy = PonderationStrategy()

        // When & Then
        assertEquals(0.5, strategy.weight, "Default weight should be 0.5 (50%)")
    }

    @Test
    fun `should handle edge case users with same hash`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val user1 = "user123"
        val user2 = "user123" // Same user
        val execContext1 = executionContextWithUser(user = user1)
        val execContext2 = executionContextWithUser(user = user2)
        val evalContext1 = featureEvaluationContext(context = execContext1)
        val evalContext2 = featureEvaluationContext(context = execContext2)

        // When
        val result1 = strategy.evaluate(evalContext1)
        val result2 = strategy.evaluate(evalContext2)

        // Then
        assertEquals(
            result1,
            result2,
            "Same user should always get same result",
        )
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = PonderationStrategy(weight = Weight.SEVENTY_FIVE_PERCENT)
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(
            serialized.contains("\"type\": \"ponderation\""),
            "Should contain type",
        )
        assertTrue(
            serialized.contains("\"weight\": ${Weight.SEVENTY_FIVE_PERCENT}"),
            "Should contain weight field",
        )
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "ponderation",
                "weight": 0.25
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(
            strategy is PonderationStrategy,
            "Should deserialize to PonderationStrategy",
        )
        assertEquals(0.25, strategy.weight)
    }

    @Test
    fun `should serialize and deserialize with default weight`() {
        // Given
        val strategy = PonderationStrategy()
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is PonderationStrategy)
        assertEquals(0.5, deserialized.weight)
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val strategy2 = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val strategy3 =
            PonderationStrategy(weight = Weight.SEVENTY_FIVE_PERCENT)

        // When & Then
        assertEquals(
            strategy1,
            strategy2,
            "Strategies with same weight should be equal",
        )
        assertNotEquals(
            strategy1,
            strategy3,
            "Strategies with different weight should not be equal",
        )
    }

    @Test
    fun `should ignore other context fields`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val user = "alice"
        val execContext1 = executionContextWithUser(user = user)
        val execContext2 = executionContextWithUser(
            user = user,
            source = "mobile-app",
            host = "prod-server",
            customParams = mapOf("region" to REGION_US_EAST),
        )
        val evalContext1 = featureEvaluationContext(context = execContext1)
        val evalContext2 = featureEvaluationContext(context = execContext2)

        // When
        val result1 = strategy.evaluate(evalContext1)
        val result2 = strategy.evaluate(evalContext2)

        // Then
        assertEquals(
            result1,
            result2,
            "Should only care about user field, ignoring other context fields",
        )
    }

    @Test
    fun `should handle special characters in user identifier`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val specialUsers = listOf(
            "user@example.com",
            "user-with-dash",
            "user_with_underscore",
            "user.with.dots",
            "用户", // Chinese characters
            "пользователь", // Cyrillic characters
        )

        // When & Then
        specialUsers.forEach { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext = featureEvaluationContext(context = execContext)

            val firstResult = strategy.evaluate(evalContext)
            val secondResult = strategy.evaluate(evalContext)
            assertEquals(
                firstResult,
                secondResult,
                "User '$user' should have deterministic result",
            )
        }
    }

    @Test
    fun `should handle very long user identifiers`() = runTest {
        // Given
        val strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val longUser = "user" + "x".repeat(10000)
        val execContext = executionContextWithUser(user = longUser)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val firstResult = strategy.evaluate(evalContext)
        val secondResult = strategy.evaluate(evalContext)

        // Then
        assertEquals(
            firstResult,
            secondResult,
            "Long user identifier should have deterministic result",
        )
    }

    @Test
    fun `should maintain stability across weight changes for incremental rollouts`() = runTest {
        // Given
        val users = (1..1000).map { "user$it" }

        // Phase 1: 25% rollout
        val phase1Strategy =
            PonderationStrategy(weight = Weight.TWENTY_FIVE_PERCENT)
        val phase1Enabled = users.filter { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext = featureEvaluationContext(context = execContext)
            phase1Strategy.evaluate(evalContext)
        }.toSet()

        // Phase 2: 50% rollout
        val phase2Strategy = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
        val phase2Enabled = users.filter { user ->
            val execContext = executionContextWithUser(user = user)
            val evalContext = featureEvaluationContext(context = execContext)
            phase2Strategy.evaluate(evalContext)
        }.toSet()

        // Then - Users enabled in phase 1 should still be enabled in phase 2
        // (This verifies stable hashing allows for graceful rollout expansion)
        val phase1StillEnabled = phase1Enabled.filter { it in phase2Enabled }
        val percentageRetained =
            phase1StillEnabled.size.toDouble() / phase1Enabled.size

        // With stable hashing, most users from phase1 should still be enabled in phase2
        assertTrue(
            percentageRetained > 0.8,
            "At least 80% of phase1 users should remain enabled in phase2, got ${percentageRetained * 100}%",
        )
    }
}
