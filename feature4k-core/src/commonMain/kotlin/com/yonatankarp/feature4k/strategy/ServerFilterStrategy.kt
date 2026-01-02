package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that enables a feature only on specific servers.
 *
 * This strategy evaluates to `true` only when the server from the execution context is present
 * in the configured [grantedServers] set. If the context has no server, or the server is not in
 * the granted list, the strategy evaluates to `false`.
 *
 * ## Use Cases
 *
 * - **Canary deployments**: Enable features on specific canary servers
 * - **Environment-specific features**: Enable features on specific environments (staging, production)
 * - **Server-based rollout**: Gradually enable features server by server
 * - **Regional deployments**: Enable features on servers in specific regions or data centers
 *
 * ## Example
 *
 * ```kotlin
 * val strategy = ServerFilterStrategy(grantedServers = setOf("server-1", "server-2", "canary-01"))
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "new-algorithm",
 *     store = store,
 *     context = FlippingExecutionContext(server = "server-1")
 * )
 * strategy.evaluate(evalContext) // returns true
 *
 * // Server not in granted list
 * val otherContext = FeatureEvaluationContext(
 *     featureName = "new-algorithm",
 *     store = store,
 *     context = FlippingExecutionContext(server = "server-99")
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
 *   "type": "server-filter",
 *   "grantedServers": ["server-1", "server-2", "canary-01"]
 * }
 * ```
 *
 * @property grantedServers The set of server identifiers that are granted access to the feature
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("server-filter")
data class ServerFilterStrategy(
    val grantedServers: Set<String> = emptySet(),
) : FlippingStrategy {
    /**
     * Enables the feature only when the evaluation context contains a host that is listed in [grantedServers].
     *
     * @param evalContext Evaluation context whose `context.host` value is checked against [grantedServers].
     * @return `true` if `context.host` is non-null and is contained in [grantedServers], `false` otherwise.
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val host = evalContext.context.host ?: return false
        return host in grantedServers
    }
}