package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import com.yonatankarp.feature4k.strategy.expression.ExpressionNode
import com.yonatankarp.feature4k.strategy.expression.ExpressionParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
 *     expression = "basic-dashboard & (premium-user | beta-tester)",
 *     strict = true // Optional: Fail fast if referenced features don't exist
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
 * 1. Parses and caches the expression tree
 * 2. Extracts feature names referenced in the expression
 * 3. Retrieves only the referenced features from the store
 * 4. Evaluates each referenced feature (applying its own strategy if present)
 * 5. Builds a map of feature states
 * 6. Evaluates the expression tree against this map
 *
 * ## Important Notes
 *
 * - Expressions are parsed once and cached for performance
 * - Features without strategies are evaluated based on their `enabled` flag
 * - Features with ExpressionFlipStrategy are also evaluated based on their `enabled` flag to prevent circular evaluation chains
 * - Self-referencing expressions are avoided (a feature can't reference itself)
 * - **Strict mode**: When `strict = true`, throws `IllegalStateException` if referenced features don't exist
 * - **Non-strict mode** (default): Unknown feature names evaluate to `false`
 *
 * @property expression The boolean logic expression to evaluate
 * @property strict When true, throws an exception if any referenced feature doesn't exist in the store. Default is false for backward compatibility.
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("expression")
data class ExpressionFlipStrategy(
    val expression: String,
    val strict: Boolean = false,
) : FlippingStrategy {
    private val cachedTree: ExpressionNode by lazy { ExpressionParser().parse(expression) }

    /**
     * Evaluates the strategy's boolean expression against feature states derived from the provided evaluation context.
     *
     * @param evalContext Context containing the feature store and current feature information used to resolve dependent feature states; the current feature is excluded when collecting dependency states.
     * @return `true` if the parsed expression evaluates to true given the resolved feature states, `false` otherwise.
     * @throws IllegalStateException if strict mode is enabled and a referenced feature does not exist in the store.
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val featureStates = buildFeatureStates(evalContext)
        return cachedTree.evaluate(featureStates)
    }

    /**
     * Builds a map of feature UIDs to their evaluated Boolean states for features referenced in the expression.
     *
     * Only retrieves and evaluates features that are actually referenced in the parsed expression tree,
     * optimizing performance by avoiding unnecessary feature evaluations.
     *
     * @param evalContext Context containing the feature store and the name of the feature being evaluated; used to retrieve and evaluate other features.
     * @return A map where keys are feature UIDs and values are each feature's evaluated `Boolean` state, excluding `evalContext.featureName`.
     * @throws IllegalStateException if strict mode is enabled and a referenced feature doesn't exist in the store.
     */
    private suspend fun buildFeatureStates(evalContext: FeatureEvaluationContext): Map<String, Boolean> {
        val referencedFeatures = cachedTree.featureNames()
        val states = mutableMapOf<String, Boolean>()

        for (featureName in referencedFeatures) {
            if (featureName == evalContext.featureName) continue

            val feature = evalContext.store[featureName]
            check(feature != null || strict.not()) {
                "Feature '$featureName' referenced in expression '$expression' " +
                    "does not exist in the feature store. Referenced by feature '${evalContext.featureName}'."
            }

            feature?.let {
                states[featureName] = evaluateFeature(it, evalContext)
            }
        }

        return states
    }

    /**
     * Evaluate a single feature's effective enabled state considering its flipping strategy.
     *
     * **Evaluation Rules:**
     * - If the feature's `enabled` flag is false, always returns false
     * - If the feature has no strategy, returns the `enabled` flag value
     * - If the feature has an ExpressionFlipStrategy, returns the `enabled` flag value (prevents circular evaluation)
     * - Otherwise, evaluates the feature's configured strategy
     *
     * **Important:** Features with ExpressionFlipStrategy are treated the same as features without strategies
     * (evaluated based solely on their `enabled` flag) to prevent infinite recursion in circular dependency chains.
     * For example, if Feature A depends on Feature B, and Feature B depends on Feature A, treating expression
     * strategies as simple boolean flags breaks the cycle.
     *
     * Expression strategies define dependencies between features rather than additional evaluation logic for
     * individual features. The expression itself handles the dependency evaluation logic.
     *
     * @param feature The feature to evaluate.
     * @param parentContext The evaluation context of the parent feature; used to construct a context for dependent strategy evaluation.
     * @return `true` if the feature is considered enabled after applying its strategy, `false` otherwise.
     */
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
