package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FlippingExecutionContext
import com.yonatankarp.feature4k.strategy.Weight
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for DarkLaunchStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class DarkLaunchStrategyTest {
    @Test
    fun `should enable feature for approximately correct percentage of users`() {
        // Given
        val strategy = DarkLaunchStrategy(weight = Weight.FIFTY_PERCENT)
        val users = (1..1000).map { "user$it" }

        // When
        val enabledCount = users.count { user ->
            val context = FlippingExecutionContext(user = user)
            strategy.evaluate(context)
        }

        // Then
        val enabledPercentage = enabledCount.toDouble() / users.size
        assertPercentageWithin(expected = 0.5, actual = enabledPercentage)
    }

    @Test
    fun `should be deterministic for same user across evaluations`() {
        // Given
        val strategy = DarkLaunchStrategy(weight = Weight.FIFTY_PERCENT)
        val context = FlippingExecutionContext(user = "alice")

        // When
        val firstEvaluation = strategy.evaluate(context)
        val evaluations = (1..100).map { strategy.evaluate(context) }

        // Then
        assertTrue(evaluations.all { it == firstEvaluation }, "Same user should always get same result")
    }

    @Test
    fun `should enable all users for full rollout`() {
        // Given
        val strategy = DarkLaunchStrategy(weight = Weight.FULL)
        val users = (1..100).map { "user$it" }

        // When
        val results = users.map { user ->
            val context = FlippingExecutionContext(user = user)
            strategy.evaluate(context)
        }

        // Then
        assertTrue(results.all { it }, "Full rollout should enable all users")
    }

    @Test
    fun `should disable all users for zero weight`() {
        // Given
        val strategy = DarkLaunchStrategy(weight = Weight.ZERO)
        val users = (1..100).map { "user$it" }

        // When
        val results = users.map { user ->
            val context = FlippingExecutionContext(user = user)
            strategy.evaluate(context)
        }

        // Then
        assertTrue(results.all { it.not() }, "Zero weight should disable all users")
    }

    @Test
    fun `should support canary deployment with 1 percent rollout`() {
        // Given
        val strategy = DarkLaunchStrategy(weight = Weight.ONE_PERCENT)
        val users = (1..1000).map { "user$it" }

        // When
        val enabledCount = users.count { user ->
            val context = FlippingExecutionContext(user = user)
            strategy.evaluate(context)
        }

        // Then
        val enabledPercentage = enabledCount.toDouble() / users.size
        assertPercentageWithin(expected = 0.01, actual = enabledPercentage, marginPercent = 10.0)
    }

    @Test
    fun `should support typical dark launch rollout of 10 percent`() {
        // Given
        val strategy = DarkLaunchStrategy(weight = Weight.TEN_PERCENT)
        val users = (1..1000).map { "user$it" }

        // When
        val enabledCount = users.count { user ->
            val context = FlippingExecutionContext(user = user)
            strategy.evaluate(context)
        }

        // Then
        val enabledPercentage = enabledCount.toDouble() / users.size
        assertPercentageWithin(expected = 0.10, actual = enabledPercentage)
    }

    @Test
    fun `should throw IllegalArgumentException when weight is out of bounds`() {
        // When & Then
        assertFailsWith<IllegalArgumentException> {
            DarkLaunchStrategy(weight = -0.1)
        }

        assertFailsWith<IllegalArgumentException> {
            DarkLaunchStrategy(weight = 1.1)
        }
    }

    @Test
    fun `should default to 50 percent for A-B testing`() {
        // Given
        val strategy = DarkLaunchStrategy()

        // When & Then
        assertEquals(Weight.FIFTY_PERCENT, strategy.weight, "Default should be 50% for A/B testing")
    }

    @Test
    fun `should serialize with dark-launch type discriminator`() {
        // Given
        val strategy = DarkLaunchStrategy(weight = Weight.TEN_PERCENT)
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"dark-launch\""))
        assertTrue(serialized.contains("\"weight\": 0.1"))
    }

    @Test
    fun `should deserialize from JSON`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "dark-launch",
                "weight": 0.25
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is DarkLaunchStrategy)
        assertEquals(Weight.TWENTY_FIVE_PERCENT, strategy.weight)
    }

    @Test
    fun `should support equality based on weight`() {
        // Given
        val strategy1 = DarkLaunchStrategy(weight = Weight.FIFTY_PERCENT)
        val strategy2 = DarkLaunchStrategy(weight = Weight.FIFTY_PERCENT)
        val strategy3 = DarkLaunchStrategy(weight = Weight.SEVENTY_FIVE_PERCENT)

        // When & Then
        assertEquals(strategy1, strategy2)
        assertNotEquals(strategy1, strategy3)
    }

    @Test
    fun `should maintain user cohorts across weight increases for dark launch expansion`() {
        // Given - Simulate progressive dark launch rollout
        val users = (1..1000).map { "user$it" }

        // Phase 1: Dark launch to 10% of users
        val phase1 = DarkLaunchStrategy(weight = Weight.TEN_PERCENT)
        val phase1Enabled = users.filter { user ->
            phase1.evaluate(FlippingExecutionContext(user = user))
        }.toSet()

        // Phase 2: Expand dark launch to 50% after monitoring phase 1
        val phase2 = DarkLaunchStrategy(weight = Weight.FIFTY_PERCENT)
        val phase2Enabled = users.filter { user ->
            phase2.evaluate(FlippingExecutionContext(user = user))
        }.toSet()

        // Then - Users from phase 1 should still be in phase 2 (stable cohorts)
        val retainedUsers = phase1Enabled.filter { it in phase2Enabled }
        val retentionRate = retainedUsers.size.toDouble() / phase1Enabled.size

        assertTrue(
            retentionRate > 0.8,
            "Dark launch should maintain stable user cohorts when expanding rollout, got ${retentionRate * 100}% retention",
        )
    }

    @Test
    fun `should use random evaluation for anonymous traffic without user context`() {
        // Given
        val strategy = DarkLaunchStrategy(weight = Weight.FIFTY_PERCENT)
        val context = FlippingExecutionContext.empty()

        // When
        val evaluations = (1..100).map { strategy.evaluate(context) }

        // Then - Should have variation (not all same)
        val hasEnabled = evaluations.any { it }
        val hasDisabled = evaluations.any { it.not() }
        assertTrue(
            hasEnabled && hasDisabled,
            "Anonymous traffic should have random distribution for dark launch testing",
        )
    }
}
