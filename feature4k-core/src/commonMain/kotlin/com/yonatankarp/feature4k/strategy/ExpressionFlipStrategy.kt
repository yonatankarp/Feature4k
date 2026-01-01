package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import com.yonatankarp.feature4k.strategy.expression.ExpressionNode
import com.yonatankarp.feature4k.strategy.expression.ExpressionParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A flipping strategy that evaluates complex boolean logic expressions involving other features.
 *
 * This strategy parses and evaluates boolean expressions like `(featureA | featureB) & !featureC`,
 * allowing features to depend on the state of other features in the store.
 *
 * ## Expression Syntax
 *
 * - Feature names: Reference other features by their UID
 * - Operators: `|` (OR), `&` (AND), `!` (NOT)
 * - Parentheses: `(` `)` for grouping
 *
 * ## Use Cases
 *
 * - **Feature dependencies**: Enable a feature only when prerequisite features are enabled
 * - **Complex rollout logic**: Combine multiple feature states with boolean logic
 * - **Conditional features**: Create features that activate based on the state of other features
 *
 * ## Example
 *
 * ```kotlin
 * // Enable "advanced-dashboard" only when "basic-dashboard" is enabled
 * // AND either "premium-user" OR "beta-tester" is enabled
 * val strategy = ExpressionFlipStrategy(
 *     expression = "basic-dashboard & (premium-user | beta-tester)"
 * )
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "advanced-dashboard",
 *     store = featureStore,
 *     context = FlippingExecutionContext()
 * )
 * strategy.evaluate(evalContext) // Evaluates based on other feature states
 * ```
 *
 * ## Evaluation Process
 *
 * 1. Retrieves all features from the store
 * 2. Evaluates each feature (applying its own strategy if present)
 * 3. Builds a map of feature states
 * 4. Evaluates the expression tree against this map
 *
 * ## Important Notes
 *
 * - Expressions are parsed once and cached for performance
 * - Features without strategies are evaluated based on their `enabled` flag
 * - Self-referencing expressions are avoided (a feature can't reference itself)
 * - Unknown feature names in expressions evaluate to `false`
 *
 * @property expression The boolean logic expression to evaluate
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("expression")
data class ExpressionFlipStrategy(
    val expression: String,
) : FlippingStrategy {
    @Transient
    private val parser = ExpressionParser()

    @Transient
    private var cachedTree: ExpressionNode? = null

    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val expressionTree = getCachedOrParse()
        val featureStates = buildFeatureStates(evalContext)
        return expressionTree.evaluate(featureStates)
    }

    /* Gets the cached expression tree or parses the expression if not yet cached. */
    private fun getCachedOrParse(): ExpressionNode = cachedTree ?: parser.parse(expression).also { cachedTree = it }

    /* Builds a map of all feature names to their current evaluation states. */
    private suspend fun buildFeatureStates(evalContext: FeatureEvaluationContext): Map<String, Boolean> {
        val allFeatures = evalContext.store.getAll()
        val states = mutableMapOf<String, Boolean>()

        for ((uid, feature) in allFeatures) {
            if (uid == evalContext.featureName) continue

            states[uid] = evaluateFeature(feature, evalContext)
        }

        return states
    }

    /* Evaluates a single feature, applying its strategy if present. */
    private suspend fun evaluateFeature(
        feature: Feature,
        parentContext: FeatureEvaluationContext,
    ): Boolean {
        if (!feature.enabled) return false

        return when (val strategy = feature.flippingStrategy) {
            null, is ExpressionFlipStrategy -> feature.enabled
            else -> {
                val featureEvalContext = FeatureEvaluationContext(
                    featureName = feature.uid,
                    store = parentContext.store,
                    context = parentContext.context,
                )
                strategy.evaluate(featureEvalContext)
            }
        }
    }
}
