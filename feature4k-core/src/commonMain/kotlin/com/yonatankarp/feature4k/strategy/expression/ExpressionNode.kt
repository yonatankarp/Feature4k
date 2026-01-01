package com.yonatankarp.feature4k.strategy.expression

/**
 * A node in an expression evaluation tree.
 *
 * Expression trees represent boolean logic expressions as a hierarchy of sealed nodes:
 * - [FeatureReference] nodes represent feature identifiers (leaf nodes)
 * - [Operation] nodes represent logical operations (internal nodes)
 *
 * ## Tree Structure Example
 *
 * Expression: `(featureA | featureB) & !featureC`
 *
 * ```
 *      Operation(AND)
 *         /      \
 *   Operation(OR)  Operation(NOT)
 *      /  \            \
 *    Ref   Ref        Ref
 *    (A)   (B)        (C)
 * ```
 *
 * @see ExpressionParser
 * @see ExpressionOperator
 * @author Yonatan Karp-Rudin
 */
sealed class ExpressionNode {
    /**
     * Evaluates this node and its subtree against the provided feature states.
     *
     * @param featureStates Map of feature names to their boolean evaluation results
     * @return The boolean result of evaluating this node and its subtree
     */
    abstract fun evaluate(featureStates: Map<String, Boolean>): Boolean

    /**
     * A leaf node representing a feature reference.
     *
     * Feature reference nodes are evaluated by looking up the feature name in the provided states.
     * If the feature is not found, it evaluates to `false`.
     *
     * @property featureName The identifier of the feature being referenced
     */
    data class FeatureReference(val featureName: String) : ExpressionNode() {
        override fun evaluate(featureStates: Map<String, Boolean>): Boolean = featureStates[featureName] ?: false

        override fun toString(): String = featureName
    }

    /**
     * An operator node representing a logical operation.
     *
     * Operation nodes combine the results of their child nodes using the specified operator:
     * - **NOT**: Negates the single child node
     * - **AND**: Returns true only if all children are true (short-circuits on first false)
     * - **OR**: Returns true if any child is true (short-circuits on first true)
     *
     * @property operator The logical operator
     * @property children The child nodes to evaluate
     */
    data class Operation(
        val operator: ExpressionOperator,
        internal val children: MutableList<ExpressionNode> = mutableListOf(),
    ) : ExpressionNode() {
        override fun evaluate(featureStates: Map<String, Boolean>): Boolean = when (operator) {
            ExpressionOperator.NOT -> children.first().evaluate(featureStates).not()
            ExpressionOperator.AND -> children.all { it.evaluate(featureStates) }
            ExpressionOperator.OR -> children.any { it.evaluate(featureStates) }
        }

        override fun toString(): String = buildString {
            // NOT is a prefix operator
            if (operator == ExpressionOperator.NOT) {
                append("!")
            }

            children.forEachIndexed { index, child ->
                if (index > 0) {
                    append(" $operator ")
                }

                // Add parentheses for nested operations
                when (child) {
                    is FeatureReference -> append(child)
                    is Operation -> append("($child)")
                }
            }
        }
    }
}
