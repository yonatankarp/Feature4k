package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FlippingExecutionContext
import com.yonatankarp.feature4k.store.InMemoryFeatureStore
import com.yonatankarp.feature4k.strategy.FeatureEvaluationContextFixture.featureEvaluationContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.emptyExecutionContext
import com.yonatankarp.feature4k.strategy.RegionFlippingStrategy.Companion.REGION_PARAM_KEY
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for RegionFlippingStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class RegionFlippingStrategyTest {
    @Test
    fun `should return true when region is in granted list`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = setOf("US", "CA", "EU"))
        val execContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "US"))
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true for granted region")
    }

    @Test
    fun `should return false when region is not in granted list`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = setOf("US", "CA", "EU"))
        val execContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "AS"))
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false for non-granted region")
    }

    @Test
    fun `should return false when context has no region parameter`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = setOf("US", "CA"))
        val execContext = emptyExecutionContext()
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when context has no region parameter")
    }

    @Test
    fun `should return false when region parameter is in customParams but empty`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = setOf("US", "CA"))
        val execContext = FlippingExecutionContext(customParams = mapOf("other-param" to "value"))
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when region parameter is not present")
    }

    @Test
    fun `should return false when granted regions list is empty`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = emptySet())
        val execContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "US"))
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when granted regions list is empty")
    }

    @Test
    fun `should handle single region in granted list`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = setOf("US"))
        val usExecContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "US"))
        val euExecContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "EU"))
        val store = InMemoryFeatureStore()
        val usEvalContext = featureEvaluationContext(context = usExecContext, store = store)
        val euEvalContext = featureEvaluationContext(context = euExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(usEvalContext), "Should allow US")
        assertFalse(strategy.evaluate(euEvalContext), "Should not allow EU")
    }

    @Test
    fun `should be case-sensitive for region matching`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = setOf("US"))
        val upperCaseExecContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "US"))
        val lowerCaseExecContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "us"))
        val store = InMemoryFeatureStore()
        val upperCaseEvalContext = featureEvaluationContext(context = upperCaseExecContext, store = store)
        val lowerCaseEvalContext = featureEvaluationContext(context = lowerCaseExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(upperCaseEvalContext), "Should match exact case 'US'")
        assertFalse(strategy.evaluate(lowerCaseEvalContext), "Should not match different case 'us'")
    }

    @Test
    fun `should handle granted list with many regions`() = runTest {
        // Given
        val manyRegions = (1..100).map { "region$it" }.toSet()
        val strategy = RegionFlippingStrategy(grantedRegions = manyRegions)
        val region50ExecContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "region50"))
        val region101ExecContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "region101"))
        val store = InMemoryFeatureStore()
        val region50EvalContext = featureEvaluationContext(context = region50ExecContext, store = store)
        val region101EvalContext = featureEvaluationContext(context = region101ExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(region50EvalContext), "Should allow region50")
        assertFalse(strategy.evaluate(region101EvalContext), "Should not allow region101")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = setOf("US", "CA", "EU"))
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"region\""), "Should contain type")
        assertTrue(serialized.contains("\"grantedRegions\""), "Should contain grantedRegions field")
        assertTrue(serialized.contains("\"US\""), "Should contain US")
        assertTrue(serialized.contains("\"CA\""), "Should contain CA")
        assertTrue(serialized.contains("\"EU\""), "Should contain EU")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "region",
                "grantedRegions": ["US", "CA", "EU"]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is RegionFlippingStrategy, "Should deserialize to RegionFlippingStrategy")
        assertEquals(setOf("US", "CA", "EU"), strategy.grantedRegions)
    }

    @Test
    fun `should serialize and deserialize empty granted regions`() {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = emptySet())
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is RegionFlippingStrategy)
        assertTrue(deserialized.grantedRegions.isEmpty())
    }

    @Test
    fun `should use default empty set when not specified`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy()
        val execContext = FlippingExecutionContext(customParams = mapOf(REGION_PARAM_KEY to "US"))
        val evalContext = featureEvaluationContext(context = execContext)

        // When & Then
        assertTrue(strategy.grantedRegions.isEmpty(), "Default grantedRegions should be empty set")
        assertFalse(strategy.evaluate(evalContext))
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = RegionFlippingStrategy(grantedRegions = setOf("US", "CA"))
        val strategy2 = RegionFlippingStrategy(grantedRegions = setOf("US", "CA"))
        val strategy3 = RegionFlippingStrategy(grantedRegions = setOf("US", "EU"))

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same regions should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different regions should not be equal")
    }

    @Test
    fun `should only check region parameter and ignore other fields`() = runTest {
        // Given
        val strategy = RegionFlippingStrategy(grantedRegions = setOf("US"))
        val execContext = FlippingExecutionContext(
            user = "alice",
            client = "mobile-app",
            server = "server-1",
            customParams = mapOf(
                REGION_PARAM_KEY to "US",
                "other-param" to "other-value",
            ),
        )
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should only care about region parameter, ignoring other context fields")
    }

    @Test
    fun `should use correct region parameter key constant`() {
        // Then
        assertEquals("region", REGION_PARAM_KEY, "Region parameter key should be 'region'")
    }
}
