package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that always enables the feature, regardless of context or enabled state.
 *
 * This strategy always returns `true` from [evaluate], making the feature unconditionally
 * enabled for all users and contexts.
 *
 * **Primarily intended for testing purposes** where you need guaranteed feature activation.
 * Can also be used in production for features that should always be on regardless of the
 * feature's enabled state.
 *
 * ## Example
 *
 * ```kotlin
 * val feature = Feature(
 *     uid = "test-feature",
 *     enabled = false,  // Note: enabled is false
 *     flippingStrategy = AlwaysOnStrategy
 * )
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "test-feature",
 *     store = store,
 *     context = FlippingExecutionContext.empty()
 * )
 * val result = AlwaysOnStrategy.evaluate(evalContext) // returns true (overrides enabled)
 * ```
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("always_on")
data object AlwaysOnStrategy : FlippingStrategy {
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean = true
}
