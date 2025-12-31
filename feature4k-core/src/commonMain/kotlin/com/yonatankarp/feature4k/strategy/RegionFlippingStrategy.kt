package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that enables a feature only in specific regions.
 *
 * This strategy evaluates to `true` only when the region from the execution context's custom
 * parameters is present in the configured [grantedRegions] set. The region is expected to be
 * passed as a custom parameter with key "region".
 *
 * ## Use Cases
 *
 * - **Geographic rollout**: Enable features in specific countries or regions
 * - **Compliance**: Enable/disable features based on regulatory requirements per region
 * - **Regional testing**: Test features in specific geographic areas before global rollout
 * - **Performance optimization**: Enable features only in regions with adequate infrastructure
 *
 * ## Example
 *
 * ```kotlin
 * val strategy = RegionFlippingStrategy(grantedRegions = setOf("US", "CA", "EU"))
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "payment-provider",
 *     store = store,
 *     context = FlippingExecutionContext(
 *         customParams = mapOf("region" to "US")
 *     )
 * )
 * strategy.evaluate(evalContext) // returns true
 *
 * // Region not in granted list
 * val otherContext = FeatureEvaluationContext(
 *     featureName = "payment-provider",
 *     store = store,
 *     context = FlippingExecutionContext(
 *         customParams = mapOf("region" to "AS")
 *     )
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
 *   "type": "region",
 *   "grantedRegions": ["US", "CA", "EU"]
 * }
 * ```
 *
 * @property grantedRegions The set of region identifiers that are granted access to the feature
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("region")
data class RegionFlippingStrategy(
    val grantedRegions: Set<String> = emptySet(),
) : FlippingStrategy {
    companion object {
        /**
         * The custom parameter key used to identify the user's region in the execution context.
         */
        const val REGION_PARAM_KEY = "region"
    }

    /**
     * Evaluates whether the feature should be enabled based on the region in the context.
     *
     * The region is retrieved from the custom parameters using the [REGION_PARAM_KEY] key.
     *
     * @param evalContext The evaluation context containing the execution context with region in custom parameters
     * @return `true` if the context has a region parameter and that region is in [grantedRegions], `false` otherwise
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val region = evalContext.context[REGION_PARAM_KEY] ?: return false
        return region in grantedRegions
    }
}
