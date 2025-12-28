package com.yonatankarp.feature4k.core

import com.yonatankarp.feature4k.store.FeatureStore

/**
 * Context for evaluating a flipping strategy.
 *
 * Packages all parameters that strategies might need for feature evaluation. This design
 * follows a single-parameter object pattern for extensibility - new fields can be added
 * to this context in the future without breaking existing strategy implementations.
 *
 * ## Usage
 *
 * FeatureEvaluationContext is created by the Feature4k orchestration class when evaluating
 * a feature. Strategies use only the fields they need:
 *
 * ```kotlin
 * // Simple strategy - only uses context.user
 * override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean =
 *     evalContext.context.user?.let { it in allowedUsers } ?: false
 *
 * // Advanced strategy - uses store for dependencies
 * override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
 *     val dependency = evalContext.store[requiredFeature] ?: return false
 *     return dependency.enabled
 * }
 * ```
 *
 * @property featureName The name of the feature being evaluated (useful for logging/debugging)
 * @property store The feature store for accessing other features (enables dependency checking)
 * @property context User execution context (user, client, server, custom params)
 * @author Yonatan Karp-Rudin
 */
data class FeatureEvaluationContext(
    val featureName: String,
    val store: FeatureStore,
    val context: FlippingExecutionContext,
)
