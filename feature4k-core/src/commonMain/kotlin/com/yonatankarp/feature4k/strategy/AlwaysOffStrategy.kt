package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FlippingExecutionContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that always disables the feature, regardless of context or enabled state.
 *
 * This strategy always returns `false` from [evaluate], making the feature unconditionally
 * disabled for all users and contexts.
 *
 * **Primarily intended for testing purposes** where you need guaranteed feature deactivation.
 * Can also be used in production for kill switch scenarios or features that should always
 * be off regardless of the feature's enabled state.
 *
 * ## Example
 *
 * ```kotlin
 * val feature = Feature(
 *     uid = "test-feature",
 *     enabled = true,  // Note: enabled is true
 *     flippingStrategy = AlwaysOffStrategy
 * )
 *
 * val result = feature.evaluate(FlippingExecutionContext.empty()) // returns false (overrides enabled)
 * ```
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("always_off")
data object AlwaysOffStrategy : FlippingStrategy {
    override fun evaluate(context: FlippingExecutionContext): Boolean = false
}
