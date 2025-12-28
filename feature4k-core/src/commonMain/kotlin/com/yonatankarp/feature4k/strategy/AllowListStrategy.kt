package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that enables a feature only for users in an explicit allowlist.
 *
 * This strategy evaluates to `true` only when the user from the execution context is present
 * in the configured [allowedUsers] set. If the context has no user, or the user is not in the
 * allowlist, the strategy evaluates to `false`.
 *
 * ## Use Cases
 *
 * - **Beta testing**: Enable features for specific beta testers
 * - **VIP access**: Grant early access to premium users
 * - **Internal testing**: Enable features for internal team members only
 * - **Gradual rollout**: Manually control which users get access to new features
 *
 * ## Example
 *
 * ```kotlin
 * val strategy = AllowListStrategy(allowedUsers = setOf("alice", "bob", "charlie"))
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "beta-feature",
 *     store = store,
 *     context = FlippingExecutionContext(user = "alice")
 * )
 * strategy.evaluate(evalContext) // returns true
 *
 * // User not in allowlist
 * val eveContext = FeatureEvaluationContext(
 *     featureName = "beta-feature",
 *     store = store,
 *     context = FlippingExecutionContext(user = "eve")
 * )
 * strategy.evaluate(eveContext) // returns false
 * ```
 *
 * ## Serialization
 *
 * This strategy is serializable and can be persisted alongside feature definitions:
 *
 * ```json
 * {
 *   "type": "allowlist",
 *   "allowedUsers": ["alice", "bob", "charlie"]
 * }
 * ```
 *
 * @property allowedUsers The set of user identifiers that are allowed to access the feature
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("allowlist")
data class AllowListStrategy(
    val allowedUsers: Set<String> = emptySet(),
) : FlippingStrategy {
    /**
     * Evaluates whether the feature should be enabled based on the user in the context.
     *
     * @param evalContext The evaluation context containing the execution context with user identifier
     * @return `true` if the context has a user and that user is in [allowedUsers], `false` otherwise
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val user = evalContext.context.user ?: return false
        return user in allowedUsers
    }
}
