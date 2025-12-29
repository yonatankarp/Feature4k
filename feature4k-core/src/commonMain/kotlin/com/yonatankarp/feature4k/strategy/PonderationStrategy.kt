package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import com.yonatankarp.feature4k.utils.UniformHash
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random.Default.nextDouble

/**
 * A flipping strategy that enables a feature based on a configurable percentage threshold
 * with deterministic per-user behavior.
 *
 * This strategy uses weighted random distribution to enable features for approximately [weight]
 * percent of evaluations. Unlike purely random strategies, PonderationStrategy provides stable,
 * deterministic behavior per user - the same user will consistently get the same result across
 * multiple evaluations. This is achieved through stable hashing of the user identifier.
 *
 * ## Use Cases
 *
 * - **A/B testing**: Enable features for a percentage of users for experimentation
 * - **Gradual rollouts**: Progressively increase feature availability (e.g., 10% → 25% → 50% → 100%)
 * - **Load distribution**: Spread resource-intensive features across users
 * - **Canary deployments**: Test new features with a small percentage before full rollout
 *
 * ## Deterministic Behavior
 *
 * The strategy ensures that the same user always gets the same evaluation result for a given weight.
 * This prevents flickering where a user would see a feature enabled on one request and disabled
 * on the next. The determinism is achieved by:
 * - Hashing the user identifier to a stable numeric value
 * - Normalizing the hash to a 0.0-1.0 range
 * - Comparing against the configured weight threshold
 *
 * ## Examples
 *
 * ### Basic 50% rollout
 * ```kotlin
 * val strategy = PonderationStrategy(weight = 0.5)
 *
 * // Over many users, approximately 50% will be enabled
 * val user1Context = FlippingExecutionContext(user = "user1")
 * val user2Context = FlippingExecutionContext(user = "user2")
 *
 * // Each user gets consistent results
 * strategy.evaluate(user1Context) // e.g., true
 * strategy.evaluate(user1Context) // always true for user1
 * strategy.evaluate(user2Context) // e.g., false
 * strategy.evaluate(user2Context) // always false for user2
 * ```
 *
 * ### Gradual rollout progression
 * ```kotlin
 * // Start with 10% of users
 * val phase1 = PonderationStrategy(weight = 0.1)
 *
 * // Increase to 50% after monitoring
 * val phase2 = PonderationStrategy(weight = 0.5)
 *
 * // Full rollout to everyone
 * val phase3 = PonderationStrategy(weight = 1.0)
 * ```
 *
 * ## Serialization
 *
 * This strategy is serializable and can be persisted alongside feature definitions:
 *
 * ```json
 * {
 *   "type": "ponderation",
 *   "weight": 0.25
 * }
 * ```
 *
 * ## Behavior Without User
 *
 * When the execution context has no user identifier, the strategy falls back to pure random
 * evaluation based on [kotlin.random.Random]. This means evaluations without a user context
 * will not be deterministic.
 *
 * @property weight The percentage threshold (0.0 to 1.0) controlling what proportion of users
 *                  should have the feature enabled. 0.0 means nobody, 1.0 means everybody.
 * @throws IllegalArgumentException if weight is not between 0.0 and 1.0 (inclusive)
 * @author Yonatan Karp-Rudin
 * @see DarkLaunchStrategy
 */
@Serializable
@SerialName("ponderation")
data class PonderationStrategy(
    val weight: Double = 0.5,
) : FlippingStrategy {
    init {
        require(weight in 0.0..1.0) {
            "The ponderation weight is a percentage and must be between 0.0 and 1.0, got: $weight"
        }
    }

    /**
     * Evaluates whether the feature should be enabled based on the configured weight.
     *
     * If the context contains a user identifier, the evaluation is deterministic - the same
     * user will always get the same result. If no user is present, evaluation falls back to
     * random behavior.
     *
     * @param evalContext The evaluation context containing the execution context with optional user identifier
     * @return `true` if the feature should be enabled based on weight threshold, `false` otherwise
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean = evalContext.context.user?.let { user ->
        UniformHash(user) < weight
    } ?: (nextDouble() < weight)
}
