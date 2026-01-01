package com.yonatankarp.feature4k.strategy.expression

/**
 * Parser for boolean logic expressions involving feature flags.
 *
 * Parses expressions like `(featureA | featureB) & !featureC` into an [ExpressionNode] tree
 * that can be evaluated against feature states.
 *
 * ## Supported Syntax
 *
 * - Feature names: Alphanumeric identifiers
 * - Operators: `|` (OR), `&` (AND), `!` (NOT prefix)
 * - Parentheses: `(` `)` for grouping
 * - Whitespace: Ignored
 *
 * ## Operator Precedence (highest to lowest)
 *
 * 1. Parentheses `( )`
 * 2. NOT `!`
 * 3. AND `&`
 * 4. OR `|`
 *
 * @author Yonatan Karp-Rudin
 */
class ExpressionParser {
    private companion object {
        const val OPEN_PAREN = '('
        const val CLOSE_PAREN = ')'
        val OR_CHAR = ExpressionOperator.OR.char
        val AND_CHAR = ExpressionOperator.AND.char
        val NOT_CHAR = ExpressionOperator.NOT.char
    }

    /** Holds a sub-expression extracted from parentheses and its end offset. */
    private data class ParenthesizedExpression(
        val content: String,
        val endOffset: Int,
    )

    /** Holds the result of extracting all sub-expressions with their placeholders and parsed nodes. */
    private data class SubExpressions(
        val placeholderMap: Map<String, String>,
        val nodeMap: Map<String, ExpressionNode>,
    )

    /**
     * Parses a boolean expression string into an expression tree.
     *
     * @param expression The expression string to parse
     * @return The root node of the parsed expression tree
     */
    fun parse(expression: String): ExpressionNode {
        val normalized = normalizeExpression(expression)

        return if (OPEN_PAREN !in normalized) {
            parseWithoutParentheses(normalized)
        } else {
            parseWithParentheses(normalized)
        }
    }

    /* Removes all whitespace from the expression. */
    private fun normalizeExpression(expression: String): String = expression.replace(" ", "")

    /* Parses an expression containing parentheses by extracting, parsing, and substituting sub-expressions. */
    private fun parseWithParentheses(expression: String): ExpressionNode {
        val subExpressions = extractAllSubExpressions(expression)
        val withPlaceholders = buildPlaceholderExpression(expression, subExpressions.placeholderMap)
        val parsedWithPlaceholders = parseWithoutParentheses(withPlaceholders)

        return substituteNode(parsedWithPlaceholders, subExpressions.nodeMap)
    }

    /* Extracts all parenthesized sub-expressions and recursively parses them into nodes. */
    private fun extractAllSubExpressions(expression: String): SubExpressions {
        val placeholders = mutableMapOf<String, String>()
        val nodes = mutableMapOf<String, ExpressionNode>()

        var working = expression
        var index = 0

        while (OPEN_PAREN in working) {
            val (subExpr, endOffset) = extractParenthesizedExpression(working)
            val placeholder = "P$index"

            placeholders[placeholder] = subExpr
                .replace("|", " OR ")
                .replace("&", " AND ")

            nodes[placeholder] = parse(subExpr)
            index++

            working = if (endOffset < working.length) {
                working.substring(endOffset)
            } else {
                ""
            }
        }

        return SubExpressions(placeholders, nodes)
    }

    /* Extracts the first parenthesized sub-expression, handling nested parentheses correctly. */
    private fun extractParenthesizedExpression(expression: String): ParenthesizedExpression {
        val chars = expression.toCharArray()
        val openIndex = expression.indexOf(OPEN_PAREN)
        var offset = openIndex + 1
        var nestingLevel = 0

        while (true) {
            when (chars[offset]) {
                OPEN_PAREN -> nestingLevel++
                CLOSE_PAREN -> {
                    if (nestingLevel == 0) {
                        val content = expression.substring(openIndex + 1, offset)
                        return ParenthesizedExpression(content, offset + 1)
                    }
                    nestingLevel--
                }
            }
            offset++
        }
    }

    /* Builds an expression with sub-expressions replaced by placeholder names. */
    private fun buildPlaceholderExpression(
        expression: String,
        subExpressions: Map<String, String>,
    ): String {
        var result = expression
            .replace("|", " OR ")
            .replace("&", " AND ")

        for ((placeholder, subExpr) in subExpressions) {
            val escapedSubExpr = subExpr
                .replace("(", "\\(")
                .replace(")", "\\)")
            result = result.replace(Regex("\\($escapedSubExpr\\)"), placeholder)
        }

        return result
            .replace(" AND ", "&")
            .replace(" OR ", "|")
            .replace(" ", "")
    }

    /* Recursively substitutes placeholder feature references with their actual parsed sub-expression nodes. */
    private fun substituteNode(
        node: ExpressionNode,
        substitutions: Map<String, ExpressionNode>,
    ): ExpressionNode = when (node) {
        is ExpressionNode.FeatureReference -> substitutions[node.featureName] ?: node
        is ExpressionNode.Operation -> {
            val newChildren = node.children
                .map { substituteNode(it, substitutions) }
                .toMutableList()
            ExpressionNode.Operation(node.operator, newChildren)
        }
    }

    /* Parses an expression without parentheses, respecting operator precedence (OR < AND < NOT). */
    private fun parseWithoutParentheses(expression: String): ExpressionNode {
        if (OR_CHAR !in expression && AND_CHAR !in expression && NOT_CHAR !in expression) {
            return ExpressionNode.FeatureReference(expression)
        }

        val orParts = expression.split(OR_CHAR)
        return if (orParts.size > 1) {
            buildOrNode(orParts)
        } else {
            parseAndNot(expression)
        }
    }

    /* Builds an OR operation node from multiple expression parts. */
    private fun buildOrNode(parts: List<String>): ExpressionNode {
        val orNode = ExpressionNode.Operation(ExpressionOperator.OR)
        parts.forEach { part ->
            orNode.children.add(parseAndNot(part))
        }
        return orNode
    }

    /* Parses expressions containing AND and NOT operators. */
    private fun parseAndNot(expression: String): ExpressionNode {
        val andParts = expression.split(AND_CHAR)
        return if (andParts.size > 1) {
            buildAndNode(andParts)
        } else {
            parseNot(expression)
        }
    }

    /* Builds an AND operation node from multiple expression parts. */
    private fun buildAndNode(parts: List<String>): ExpressionNode {
        val andNode = ExpressionNode.Operation(ExpressionOperator.AND)
        parts.forEach { part ->
            andNode.children.add(parseNot(part))
        }
        return andNode
    }

    /* Parses expressions that may start with a NOT operator. */
    private fun parseNot(expression: String): ExpressionNode = if (expression.startsWith(NOT_CHAR)) {
        buildNotNode(expression.substring(1))
    } else {
        ExpressionNode.FeatureReference(expression)
    }

    /* Builds a NOT operation node. */
    private fun buildNotNode(featureName: String): ExpressionNode {
        val notNode = ExpressionNode.Operation(ExpressionOperator.NOT)
        notNode.children.add(ExpressionNode.FeatureReference(featureName))
        return notNode
    }
}
