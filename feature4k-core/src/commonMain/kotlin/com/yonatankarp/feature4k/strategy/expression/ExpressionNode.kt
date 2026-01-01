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
     * Evaluate this node and its subtree using the provided feature states.
     *
     * @param featureStates A map from feature name to its boolean value; implementations resolve feature references using this map.
     * @return `true` if the expression represented by this node and its subtree evaluates to true given `featureStates`, `false` otherwise.
     */
    abstract fun evaluate(featureStates: Map<String, Boolean>): Boolean

    /**
     * Returns all feature names referenced in this expression tree.
     *
     * Traverses the entire expression tree and returns the set of all unique feature names
     * that appear in FeatureReference nodes. This is useful for optimizing feature evaluation
     * by only loading the features that are actually needed.
     *
     * @return A set containing all unique feature names referenced in this expression tree.
     */
    abstract fun featureNames(): Set<String>

    /**
     * A leaf node representing a feature reference.
     *
     * Feature reference nodes are evaluated by looking up the feature name in the provided states.
     * If the feature is not found, it evaluates to `false`.
     *
     * @property featureName The identifier of the feature being referenced
     */
    data class FeatureReference(val featureName: String) : ExpressionNode() {
        /**
         * Evaluates this feature reference using the provided feature states map.
         *
         * @param featureStates Map of feature names to their enabled (`true`) or disabled (`false`) states.
         * @return `true` if `featureName` maps to `true` in `featureStates`, `false` otherwise.
         */
        override fun evaluate(featureStates: Map<String, Boolean>): Boolean = featureStates[featureName] ?: false

        /**
         * Returns a set containing only this feature's name.
         *
         * @return A set containing the single feature name referenced by this node.
         */
        override fun featureNames(): Set<String> = setOf(featureName)

        /**
         * The feature reference's name as its textual representation.
         *
         * @return The feature name.
         */
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
        /**
         * Evaluates this operation node against the provided feature states.
         *
         * The result depends on the operator:
         * - NOT: negates the evaluation of the single child.
         * - AND: `true` only if all children evaluate to `true`.
         * - OR: `true` if any child evaluates to `true`.
         *
         * @param featureStates Map of feature names to their boolean states used for resolving feature references.
         * @return `true` if the operation evaluates to true for the given feature states, `false` otherwise.
         */
        override fun evaluate(featureStates: Map<String, Boolean>): Boolean = when (operator) {
            ExpressionOperator.NOT -> children.first().evaluate(featureStates).not()
            ExpressionOperator.AND -> children.all { it.evaluate(featureStates) }
            ExpressionOperator.OR -> children.any { it.evaluate(featureStates) }
        }

        /**
         * Returns all unique feature names referenced in this operation's subtree.
         *
         * Recursively collects feature names from all child nodes and returns their union.
         *
         * @return A set containing all unique feature names from all child nodes.
         */
        override fun featureNames(): Set<String> = children.flatMap { it.featureNames() }.toSet()

        /**
         * Produces a human-readable textual representation of this operation node.
         *
         * The output prefixes the expression with "!" when the operator is `NOT`, inserts the operator between children
         * (e.g., "A AND B"), renders `FeatureReference` children as their feature name, and encloses nested `Operation`
         * children in parentheses.
         *
         * @return The formatted string representation of this operation subtree.
         */
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
