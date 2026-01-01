package com.yonatankarp.feature4k.strategy.expression

/**
 * Enumeration of operators supported by the expression evaluation engine.
 *
 * The expression parser handles boolean logic expressions using these three fundamental operators:
 * - **OR** (`|`): Logical disjunction - evaluates to true if any operand is true
 * - **AND** (`&`): Logical conjunction - evaluates to true only if all operands are true
 * - **NOT** (`!`): Logical negation - inverts the boolean value of its operand
 *
 * ## Operator Precedence
 *
 * Operators follow standard boolean logic precedence (highest to lowest):
 * 1. NOT (`!`) - Unary negation
 * 2. AND (`&`) - Logical conjunction
 * 3. OR (`|`) - Logical disjunction
 *
 * Parentheses can be used to override the default precedence.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Expression: featureA & (featureB | !featureC)
 * // Parsed tree will respect precedence:
 * //       AND
 * //      /   \
 * //     A     OR
 * //          /  \
 * //         B   NOT
 * //              |
 * //              C
 * ```
 *
 * @property char The character representing this operator in expression strings
 * @see ExpressionParser
 * @see ExpressionNode
 * @author Yonatan Karp-Rudin
 */
enum class ExpressionOperator(val char: Char) {
    /**
     * Logical OR operator (`|`).
     * Returns true if at least one operand evaluates to true.
     * Lowest precedence among operators.
     */
    OR('|'),

    /**
     * Logical AND operator (`&`).
     * Returns true only if all operands evaluate to true.
     * Medium precedence (higher than OR, lower than NOT).
     */
    AND('&'),

    /**
     * Logical NOT operator (`!`).
     * Negates the boolean value of its single operand.
     * Highest precedence among operators.
     */
    NOT('!'),
}
