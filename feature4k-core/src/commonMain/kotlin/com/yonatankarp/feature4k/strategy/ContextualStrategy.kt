package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that combines multiple sub-strategies using AND or OR logic.
 *
 * This strategy enables composition of complex evaluation logic by combining simpler strategies.
 * It supports both AND (all must pass) and OR (at least one must pass) combination modes, and
 * can be nested recursively for sophisticated feature gating scenarios.
 *
 * ## Use Cases
 *
 * - **Multi-condition gates**: Require both time-based and user-based conditions
 * - **Fallback logic**: Enable if primary OR fallback strategy passes
 * - **Complex authorization**: Combine multiple permission checks
 * - **Layered rollouts**: Require region AND percentage AND user criteria
 *
 * ## Example - AND Combination
 *
 * ```kotlin
 * val strategy = ContextualStrategy(
 *     combineWith = CombineWith.AND,
 *     strategies = listOf(
 *         AllowListStrategy(allowedUsers = setOf("alice", "bob")),
 *         OfficeHourStrategy(startHour = 9, endHour = 17)
 *     )
 * )
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "internal-tool",
 *     store = store,
 *     context = FlippingExecutionContext(user = "alice")
 * )
 * strategy.evaluate(evalContext) // returns true only if user is alice/bob AND during office hours
 * ```
 *
 * ## Example - OR Combination
 *
 * ```kotlin
 * val strategy = ContextualStrategy(
 *     combineWith = CombineWith.OR,
 *     strategies = listOf(
 *         AllowListStrategy(allowedUsers = setOf("admin")),
 *         DarkLaunchStrategy(percentage = 10.0)
 *     )
 * )
 * strategy.evaluate(evalContext) // returns true if user is "admin" OR in the 10% rollout
 * ```
 *
 * ## Example - Nested Composition
 *
 * ```kotlin
 * val strategy = ContextualStrategy(
 *     combineWith = CombineWith.AND,
 *     strategies = listOf(
 *         RegionFlippingStrategy(grantedRegions = setOf("US", "EU")),
 *         ContextualStrategy(
 *             combineWith = CombineWith.OR,
 *             strategies = listOf(
 *                 AllowListStrategy(allowedUsers = setOf("vip")),
 *                 DarkLaunchStrategy(weight = 5.0)
 *             )
 *         )
 *     )
 * )
 * // Enabled for US/EU users who are either VIPs OR in the 5% rollout
 * ```
 *
 * ## Serialization
 *
 * This strategy is serializable and can be persisted alongside feature definitions:
 *
 * ```json
 * {
 *   "type": "contextual",
 *   "combineWith": "AND",
 *   "strategies": [
 *     {"type": "allowlist", "allowedUsers": ["alice"]},
 *     {"type": "office_hours", "startHour": 9, "endHour": 17}
 *   ]
 * }
 * ```
 *
 * @property combineWith The combination mode (AND or OR) for evaluating sub-strategies
 * @property strategies The list of strategies to combine
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("contextual")
data class ContextualStrategy(
    val combineWith: CombineWith,
    val strategies: List<FlippingStrategy> = emptyList(),
) : FlippingStrategy {
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean = when (combineWith) {
        CombineWith.AND -> strategies.all { it.evaluate(evalContext) }
        CombineWith.OR -> strategies.any { it.evaluate(evalContext) }
    }

    /**
     * Combination mode for evaluating multiple strategies.
     */
    @Serializable
    enum class CombineWith {
        /**
         * All sub-strategies must evaluate to true for the feature to be enabled.
         */
        AND,

        /**
         * At least one sub-strategy must evaluate to true for the feature to be enabled.
         */
        OR,
    }
}
