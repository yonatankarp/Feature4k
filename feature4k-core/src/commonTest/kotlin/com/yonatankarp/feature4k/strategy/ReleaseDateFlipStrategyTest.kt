package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.strategy.FeatureEvaluationContextFixture.featureEvaluationContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.contextWithInstant
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.executionContextWithUser
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.CHRISTMAS_2024_MORNING
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.FUTURE_DATE
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.PAST_DATE
import com.yonatankarp.feature4k.strategy.TimeStrategyFixtures.releaseDateStrategy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for ReleaseDateFlipStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class ReleaseDateFlipStrategyTest {
    @Test
    fun `should return true when current time is after release date`() = runTest {
        // Given
        val strategy = releaseDateStrategy("2024-01-01T00:00:00Z")
        val context = contextWithInstant("2024-12-25T10:00:00Z")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when current time is after release date")
    }

    @Test
    fun `should return true when current time equals release date`() = runTest {
        // Given
        val strategy = releaseDateStrategy(CHRISTMAS_2024_MORNING)
        val context = contextWithInstant("2024-12-25T10:00:00Z")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when current time equals release date")
    }

    @Test
    fun `should return false when current time is before release date`() = runTest {
        // Given
        val strategy = releaseDateStrategy(CHRISTMAS_2024_MORNING)
        val context = contextWithInstant("2024-12-24T23:59:59Z")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when current time is before release date")
    }

    @Test
    fun `should handle far future release dates`() = runTest {
        // Given
        val strategy = releaseDateStrategy(FUTURE_DATE)
        val context = contextWithInstant("2024-12-25T00:00:00Z")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false for far future release dates")
    }

    @Test
    fun `should handle past release dates`() = runTest {
        // Given
        val strategy = releaseDateStrategy(PAST_DATE)
        val context = contextWithInstant("2024-12-25T00:00:00Z")
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true for past release dates")
    }

    @Test
    fun `should work without time override using system time`() = runTest {
        // Given
        val strategy = releaseDateStrategy(PAST_DATE)
        val context = executionContextWithUser()
        val evalContext = featureEvaluationContext(context = context)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should use system time when no override is provided")
    }

    @Test
    fun `should ignore user context and only check time`() = runTest {
        // Given
        val strategy = releaseDateStrategy("2024-12-25T00:00:00Z")
        val currentTime = "2024-12-26T00:00:00Z"

        val user1Context = contextWithInstant(currentTime)
        val user2Context = contextWithInstant(currentTime)
        val user1EvalContext = featureEvaluationContext(context = user1Context)
        val user2EvalContext = featureEvaluationContext(context = user2Context)

        // When & Then
        assertTrue(strategy.evaluate(user1EvalContext))
        assertTrue(strategy.evaluate(user2EvalContext))
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = releaseDateStrategy(CHRISTMAS_2024_MORNING)
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"release-date\""), "Should contain type")
        assertTrue(serialized.contains("\"releaseDate\""), "Should contain releaseDate field")
        assertTrue(serialized.contains("2024-12-25T10:00:00Z"), "Should contain the ISO-8601 timestamp")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "release-date",
                "releaseDate": "2024-12-25T10:00:00Z"
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is ReleaseDateFlipStrategy, "Should deserialize to ReleaseDateFlipStrategy")
        assertEquals(CHRISTMAS_2024_MORNING, strategy.releaseDate)
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = releaseDateStrategy("2024-12-25T00:00:00Z")
        val strategy2 = releaseDateStrategy("2024-12-25T00:00:00Z")
        val strategy3 = releaseDateStrategy("2024-12-26T00:00:00Z")

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same release date should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different release dates should not be equal")
    }

    @Test
    fun `should handle millisecond precision in timestamps`() = runTest {
        // Given
        val strategy = releaseDateStrategy("2024-12-25T10:00:00.123Z")
        val beforeContext = contextWithInstant("2024-12-25T10:00:00.122Z")
        val afterContext = contextWithInstant("2024-12-25T10:00:00.124Z")
        val beforeEvalContext = featureEvaluationContext(context = beforeContext)
        val afterEvalContext = featureEvaluationContext(context = afterContext)

        // When & Then
        assertFalse(strategy.evaluate(beforeEvalContext), "Should be false 1ms before release")
        assertTrue(strategy.evaluate(afterEvalContext), "Should be true 1ms after release")
    }
}
