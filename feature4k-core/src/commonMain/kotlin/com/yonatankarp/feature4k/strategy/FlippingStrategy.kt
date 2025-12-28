package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.Serializable

/**
 * Strategy interface for determining if a feature should be enabled based on runtime context.
 *
 * FlippingStrategy implementations provide the core logic for conditional feature activation.
 * Each strategy evaluates a [FeatureEvaluationContext] and returns whether the feature should
 * be enabled for that specific context.
 *
 * Strategies are serializable and can be persisted alongside feature definitions, enabling
 * dynamic feature evaluation logic that can be configured at runtime.
 *
 * ## Common Strategy Types
 *
 * - **User-based**: Allowlist/denylist strategies that enable features for specific users
 * - **Percentage-based**: Gradual rollout strategies (e.g., enable for 20% of users)
 * - **Time-based**: Enable features during specific time windows or after release dates
 * - **Filter-based**: Enable based on client, server, or region filters
 * - **Expression-based**: Complex logic using custom expression evaluation
 *
 * ## Usage Example
 *
 * ```kotlin
 * val strategy = AllowListStrategy(allowedUsers = setOf("user1", "user2"))
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "my-feature",
 *     store = featureStore,
 *     context = FlippingExecutionContext(user = "user1")
 * )
 * val enabled = strategy.evaluate(evalContext) // returns true
 * ```
 *
 * ## Implementation Notes
 *
 * - All implementations must be **immutable** data classes or objects
 * - Evaluation should be **deterministic** for the same context
 * - Implementations should be **efficient** as they may be called frequently
 * - Use `@SerialName` for consistent serialization across versions
 * - Simple strategies use only `evalContext.context`, advanced strategies may use `evalContext.store`
 *
 * @see FeatureEvaluationContext
 * @author Yonatan Karp-Rudin
 */
@Serializable
sealed interface FlippingStrategy {
    /**
     * Evaluates whether a feature should be enabled for the given evaluation context.
     *
     * This method contains the core logic for determining feature state. Implementations
     * should examine the [evalContext] and return `true` if the feature should be enabled,
     * or `false` otherwise.
     *
     * Strategies use only what they need from the evaluation context:
     * - Simple strategies typically only use `evalContext.context` (user, client, server, etc.)
     * - Advanced strategies may also use `evalContext.store` to check feature dependencies
     * - The `evalContext.featureName` is available for logging/debugging
     *
     * @param evalContext The evaluation context containing feature name, store, and execution context
     * @return `true` if the feature should be enabled, `false` otherwise
     */
    suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean
}
