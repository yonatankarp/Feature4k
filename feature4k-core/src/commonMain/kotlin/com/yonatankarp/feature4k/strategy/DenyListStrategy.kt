package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that disables a feature for users in an explicit denylist.
 *
 * This strategy evaluates to `false` when the user from the execution context is present
 * in the configured [deniedUsers] set. If the context has no user, or the user is not in the
 * denylist, the strategy evaluates to `true`.
 *
 * ## Use Cases
 *
 * - **Block problematic users**: Disable features for users causing issues
 * - **Regional restrictions**: Block users from specific regions (when combined with user metadata)
 * - **A/B testing exclusions**: Exclude specific users from experiments
 * - **Rollback**: Quickly disable features for affected users during incidents
 *
 * ## Example
 *
 * ```kotlin
 * val strategy = DenyListStrategy(deniedUsers = setOf("spammer1", "abuser2"))
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "premium-feature",
 *     store = store,
 *     context = FlippingExecutionContext(user = "spammer1")
 * )
 * strategy.evaluate(evalContext) // returns false
 *
 * // User not in denylist
 * val aliceContext = FeatureEvaluationContext(
 *     featureName = "premium-feature",
 *     store = store,
 *     context = FlippingExecutionContext(user = "alice")
 * )
 * strategy.evaluate(aliceContext) // returns true
 * ```
 *
 * ## Serialization
 *
 * This strategy is serializable and can be persisted alongside feature definitions:
 *
 * ```json
 * {
 *   "type": "denylist",
 *   "deniedUsers": ["spammer1", "abuser2"]
 * }
 * ```
 *
 * @property deniedUsers The set of user identifiers that are denied access to the feature
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("denylist")
data class DenyListStrategy(
    val deniedUsers: Set<String> = emptySet(),
) : FlippingStrategy {
    /**
     * Evaluates whether the feature should be enabled based on the user in the context.
     *
     * @param evalContext The evaluation context containing the execution context with user identifier
     * @return `false` if the context has a user and that user is in [deniedUsers], `true` otherwise
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val user = evalContext.context.user ?: return true
        return user !in deniedUsers
    }
}
