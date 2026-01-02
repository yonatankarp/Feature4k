package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that enables a feature only for requests from specific clients.
 *
 * This strategy evaluates to `true` only when the client from the execution context is present
 * in the configured [grantedClients] set. If the context has no client, or the client is not in
 * the granted list, the strategy evaluates to `false`.
 *
 * ## Use Cases
 *
 * - **Client-specific features**: Enable features for specific applications or services
 * - **A/B testing by client**: Test features on specific client applications
 * - **Gradual rollout**: Enable features for specific client versions or environments
 * - **Partner access**: Grant features to specific partner applications
 *
 * ## Example
 *
 * ```kotlin
 * val strategy = ClientFilterStrategy(grantedClients = setOf("mobile-app", "web-app", "admin-portal"))
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "new-checkout",
 *     store = store,
 *     context = FlippingExecutionContext(client = "mobile-app")
 * )
 * strategy.evaluate(evalContext) // returns true
 *
 * // Client not in granted list
 * val otherContext = FeatureEvaluationContext(
 *     featureName = "new-checkout",
 *     store = store,
 *     context = FlippingExecutionContext(client = "legacy-app")
 * )
 * strategy.evaluate(otherContext) // returns false
 * ```
 *
 * ## Serialization
 *
 * This strategy is serializable and can be persisted alongside feature definitions:
 *
 * ```json
 * {
 *   "type": "client-filter",
 *   "grantedClients": ["mobile-app", "web-app", "admin-portal"]
 * }
 * ```
 *
 * @property grantedClients The set of client identifiers that are granted access to the feature
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("client-filter")
data class ClientFilterStrategy(
    val grantedClients: Set<String> = emptySet(),
) : FlippingStrategy {
    /**
     * Determines whether the feature is enabled for the evaluation's execution source.
     *
     * @param evalContext The evaluation context whose execution context provides the `source` identifier to check.
     * @return `true` if the evaluation context contains a `source` and that `source` is in [grantedClients], `false` otherwise.
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val source = evalContext.context.source ?: return false
        return source in grantedClients
    }
}