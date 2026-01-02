package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.core.IdentifierFixtures.ANALYTICS
import com.yonatankarp.feature4k.core.IdentifierFixtures.BASIC_DASHBOARD
import com.yonatankarp.feature4k.core.IdentifierFixtures.BETA_ACCESS
import com.yonatankarp.feature4k.core.IdentifierFixtures.DARK_MODE
import com.yonatankarp.feature4k.core.IdentifierFixtures.NEW_UI
import com.yonatankarp.feature4k.core.IdentifierFixtures.PREMIUM_DASHBOARD
import com.yonatankarp.feature4k.store.StoreFixtures.inMemoryFeatureStoreWithNoOp
import com.yonatankarp.feature4k.strategy.ExpressionStrategyFixtures.darkModeAndNewUi
import com.yonatankarp.feature4k.strategy.ExpressionStrategyFixtures.darkModeOnly
import com.yonatankarp.feature4k.strategy.ExpressionStrategyFixtures.darkModeOrNewUi
import com.yonatankarp.feature4k.strategy.ExpressionStrategyFixtures.notAnalytics
import com.yonatankarp.feature4k.strategy.ExpressionStrategyFixtures.premiumRequiresBasicAndBeta
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
 * Tests for [ExpressionFlipStrategy].
 *
 * @author Yonatan Karp-Rudin
 */
class ExpressionFlipStrategyTest {
    @Test
    fun `should evaluate simple OR expression with one feature enabled`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = DARK_MODE, enabled = false)
        store += Feature(uid = NEW_UI, enabled = true)

        val strategy = darkModeOrNewUi()
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should be true when one feature in OR is enabled")
    }

    @Test
    fun `should evaluate simple AND expression with all features enabled`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = DARK_MODE, enabled = true)
        store += Feature(uid = NEW_UI, enabled = true)

        val strategy = darkModeAndNewUi()
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should be true when all features in AND are enabled")
    }

    @Test
    fun `should evaluate simple AND expression with one feature disabled`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = DARK_MODE, enabled = true)
        store += Feature(uid = NEW_UI, enabled = false)

        val strategy = darkModeAndNewUi()
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should be false when any feature in AND is disabled")
    }

    @Test
    fun `should evaluate NOT expression correctly`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = ANALYTICS, enabled = true)

        val strategy = notAnalytics()
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should be false when NOT applied to enabled feature")
    }

    @Test
    fun `should evaluate complex expression - premium requires basic and beta`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = BASIC_DASHBOARD, enabled = true)
        store += Feature(uid = BETA_ACCESS, enabled = true)
        store += Feature(uid = ANALYTICS, enabled = false)

        val strategy = premiumRequiresBasicAndBeta()
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should be true when basic and beta are enabled")
    }

    @Test
    fun `should evaluate complex expression with all requirements missing`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = BASIC_DASHBOARD, enabled = false)
        store += Feature(uid = BETA_ACCESS, enabled = false)
        store += Feature(uid = ANALYTICS, enabled = false)

        val strategy = premiumRequiresBasicAndBeta()
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should be false when all requirements are missing")
    }

    @Test
    fun `should evaluate feature with its own strategy`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store +=
            Feature(
                uid = DARK_MODE,
                enabled = true,
                flippingStrategy = AllowListStrategy(allowedUsers = setOf("alice")),
            )

        val strategy = darkModeOnly()
        val evalContext = featureEvaluationContext(
            featureName = PREMIUM_DASHBOARD,
            store = store,
            context = executionContextWithUser("bob"),
        )

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Should evaluate dark-mode strategy and return false for bob")
    }

    @Test
    fun `should avoid self-reference in expression evaluation`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = DARK_MODE, enabled = true)

        val strategy = darkModeOnly()
        val evalContext = featureEvaluationContext(featureName = DARK_MODE, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Self-referencing feature should not be included in evaluation")
    }

    @Test
    fun `should treat disabled features as false regardless of strategy`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store +=
            Feature(
                uid = DARK_MODE,
                enabled = false,
                flippingStrategy = AllowListStrategy(allowedUsers = setOf("alice")),
            )

        val strategy = darkModeOnly()
        val evalContext = featureEvaluationContext(
            featureName = PREMIUM_DASHBOARD,
            store = store,
            context = executionContextWithUser("alice"),
        )

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Disabled features should always evaluate to false")
    }

    @Test
    fun `should cache parsed expression tree`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = DARK_MODE, enabled = true)

        val strategy = darkModeOrNewUi()
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result1 = strategy.evaluate(evalContext)
        val result2 = strategy.evaluate(evalContext)

        // Then
        assertEquals(result1, result2)
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = darkModeAndNewUi()
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"expression\""), "Should contain type")
        assertTrue(serialized.contains("\"expression\""), "Should contain expression field")
        assertTrue(serialized.contains(DARK_MODE), "Should contain feature reference")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "expression",
                "expression": "$DARK_MODE&$NEW_UI"
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is ExpressionFlipStrategy, "Should deserialize to ExpressionFlipStrategy")
        assertEquals("$DARK_MODE&$NEW_UI", strategy.expression)
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = darkModeOrNewUi()
        val strategy2 = darkModeOrNewUi()
        val strategy3 = darkModeAndNewUi()

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same expression should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different expressions should not be equal")
    }

    @Test
    fun `should handle unknown features as false`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()

        val strategy = darkModeOrNewUi()
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Unknown features should evaluate to false")
    }

    @Test
    fun `should use enabled flag for features with expression strategy to prevent circular evaluation`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = ANALYTICS, enabled = true)
        store +=
            Feature(
                uid = BETA_ACCESS,
                enabled = true,
                flippingStrategy = ExpressionFlipStrategy(expression = ANALYTICS),
            )
        store +=
            Feature(
                uid = BASIC_DASHBOARD,
                enabled = true,
                flippingStrategy = ExpressionFlipStrategy(expression = BETA_ACCESS),
            )

        val strategy = ExpressionFlipStrategy(expression = BASIC_DASHBOARD)
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should return enabled flag value without evaluating nested expression strategies")
    }

    @Test
    fun `should handle direct circular dependency between two features`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store +=
            Feature(
                uid = DARK_MODE,
                enabled = true,
                flippingStrategy = ExpressionFlipStrategy(expression = NEW_UI),
            )
        store +=
            Feature(
                uid = NEW_UI,
                enabled = true,
                flippingStrategy = ExpressionFlipStrategy(expression = DARK_MODE),
            )

        val strategy = ExpressionFlipStrategy(expression = DARK_MODE)
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should safely return enabled flag without infinite recursion")
    }

    @Test
    fun `should throw exception in strict mode when referenced feature does not exist`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        // Store is empty, so dark-mode doesn't exist

        val strategy = ExpressionFlipStrategy(expression = DARK_MODE, strict = true)
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When & Then
        val exception = kotlin.test.assertFailsWith<IllegalStateException> {
            strategy.evaluate(evalContext)
        }
        assertTrue(exception.message!!.contains(DARK_MODE), "Error should mention missing feature")
        assertTrue(exception.message!!.contains(PREMIUM_DASHBOARD), "Error should mention referencing feature")
    }

    @Test
    fun `should not throw exception in non-strict mode when referenced feature does not exist`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        // Store is empty, so dark-mode doesn't exist

        val strategy = ExpressionFlipStrategy(expression = DARK_MODE, strict = false)
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Unknown features should evaluate to false in non-strict mode")
    }

    @Test
    fun `should work correctly in strict mode when all referenced features exist`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = DARK_MODE, enabled = true)
        store += Feature(uid = NEW_UI, enabled = true)

        val strategy = ExpressionFlipStrategy(expression = "$DARK_MODE&$NEW_UI", strict = true)
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should evaluate normally when all features exist in strict mode")
    }

    @Test
    fun `should throw exception in strict mode when one of multiple referenced features does not exist`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithNoOp()
        store += Feature(uid = DARK_MODE, enabled = true)
        // NEW_UI doesn't exist

        val strategy = ExpressionFlipStrategy(expression = "$DARK_MODE&$NEW_UI", strict = true)
        val evalContext = featureEvaluationContext(featureName = PREMIUM_DASHBOARD, store = store)

        // When & Then
        val exception = kotlin.test.assertFailsWith<IllegalStateException> {
            strategy.evaluate(evalContext)
        }
        assertTrue(exception.message!!.contains(NEW_UI), "Error should mention the missing feature")
    }

    @Test
    fun `should serialize strict mode to JSON correctly`() {
        // Given
        val strategy = ExpressionFlipStrategy(expression = DARK_MODE, strict = true)
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"strict\": true"), "Should contain strict flag")
    }

    @Test
    fun `should deserialize strict mode from JSON correctly`() {
        // Given
        // language=json
        val jsonString = """
            {
                "type": "expression",
                "expression": "$DARK_MODE",
                "strict": true
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString) as ExpressionFlipStrategy

        // Then
        assertTrue(strategy.strict, "Should deserialize strict flag correctly")
        assertEquals(DARK_MODE, strategy.expression)
    }
}
