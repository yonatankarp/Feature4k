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
     * Parse a boolean feature-flag expression into an ExpressionNode tree.
     *
     * @param expression The expression to parse; may contain feature identifiers, `|` (OR), `&` (AND), `!` (NOT), parentheses, and whitespace.
     * @return The root `ExpressionNode` representing the parsed expression tree.
     * @throws IllegalArgumentException if the expression contains empty or invalid feature names
     */
    fun parse(expression: String): ExpressionNode {
        val normalized = normalizeExpression(expression)

        val result = if (OPEN_PAREN !in normalized) {
            parseWithoutParentheses(normalized)
        } else {
            parseWithParentheses(normalized)
        }

        validateFeatureNames(result, expression)
        return result
    }

    /**
     * Validates that all feature names in the expression tree are non-empty.
     *
     * @param node The root node of the expression tree to validate.
     * @param originalExpression The original expression string for error messages.
     * @throws IllegalArgumentException if any feature name is empty.
     */
    private fun validateFeatureNames(node: ExpressionNode, originalExpression: String) {
        val featureNames = node.featureNames()
        val emptyNames = featureNames.filter { it.isEmpty() }

        if (emptyNames.isNotEmpty()) {
            throw IllegalArgumentException(
                "Invalid expression: '$originalExpression' contains empty feature name(s). " +
                    "This can happen when operators appear consecutively (e.g., 'a||b'), " +
                    "at the start (e.g., '|a'), at the end (e.g., 'a|'), " +
                    "or when the expression consists only of operators (e.g., '&|!').",
            )
        }
    }

    /**
     * Produce the expression with all space characters removed.
     *
     * @param expression The input expression.
     * @return The expression with all space characters (' ') removed.
     */
    private fun normalizeExpression(expression: String): String = expression.replace(" ", "")

    /**
     * Parse a parenthesized boolean expression into an ExpressionNode tree, resolving and substituting all nested sub-expressions.
     *
     * @param expression The boolean expression to parse; may include parentheses, `|`, `&`, `!`, and whitespace.
     * @return An ExpressionNode representing the fully parsed expression with parenthesized sub-expressions substituted.
     */
    private fun parseWithParentheses(expression: String): ExpressionNode {
        val subExpressions = extractAllSubExpressions(expression)
        val withPlaceholders = buildPlaceholderExpression(expression, subExpressions.placeholderMap)
        val parsedWithPlaceholders = parseWithoutParentheses(withPlaceholders)

        return substituteNode(parsedWithPlaceholders, subExpressions.nodeMap)
    }

    /**
     * Extracts each parenthesized sub-expression, assigns it a placeholder, and parses it into nodes.
     *
     * Iteratively finds parenthesized segments (respecting nested parentheses), produces placeholders
     * named "P0", "P1", ... for each segment, stores the raw sub-expression with `|` and `&` replaced
     * by the words "OR" and "AND" in the placeholders map, and parses each raw sub-expression into an
     * ExpressionNode stored in the nodes map.
     *
     * @return A SubExpressions instance containing:
     * - placeholder -> raw sub-expression string (with `|`/`&` converted to "OR"/"AND")
     * - placeholder -> parsed ExpressionNode for that sub-expression
     */
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

    /**
     * Extracts the first parenthesized sub-expression from the input string while correctly handling nested parentheses.
     *
     * Finds the first `(` in `expression` and locates its matching `)`. The returned `ParenthesizedExpression`
     * contains the inner content (excluding the surrounding parentheses) and the offset immediately after the
     * closing `)` in the original string.
     *
     * @return A [ParenthesizedExpression] whose `content` is the substring between the matched parentheses and whose
     * `endOffset` is the index in `expression` immediately following the closing parenthesis.
     */
    private fun extractParenthesizedExpression(expression: String): ParenthesizedExpression {
        val chars = expression.toCharArray()
        val openIndex = expression.indexOf(OPEN_PAREN)
        var offset = openIndex + 1
        var nestingLevel = 0

        while (offset < chars.size) {
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

        error("Invalid expression: unmatched opening parenthesis at position $openIndex in '$expression'")
    }

    /**
     * Produce an expression where parenthesized sub-expressions are replaced by placeholders and all whitespace is removed.
     *
     * @param expression The original expression string that may contain parenthesized sub-expressions.
     * @param subExpressions Map of placeholder name to the raw inner sub-expression (without surrounding parentheses) to substitute.
     * @return The transformed expression string with each matching parenthesized sub-expression replaced by its placeholder, using `&` and `|` operators and no spaces.
     */
    private fun buildPlaceholderExpression(
        expression: String,
        subExpressions: Map<String, String>,
    ): String {
        var result = expression
            .replace("|", " OR ")
            .replace("&", " AND ")

        for ((placeholder, subExpr) in subExpressions) {
            val escapedSubExpr = Regex.escape(subExpr)
            val pattern = """\($escapedSubExpr\)""".toRegex()
            result = result.replace(pattern, placeholder)
        }

        return result
            .replace(" AND ", "&")
            .replace(" OR ", "|")
            .replace(" ", "")
    }

    /**
     * Replace placeholder feature references in an ExpressionNode tree with their corresponding parsed sub-expression nodes.
     *
     * @param node The root ExpressionNode to process.
     * @param substitutions Map from placeholder feature names (e.g., "P0") to their replacement ExpressionNode.
     * @return A new ExpressionNode with all matching FeatureReference placeholders replaced by the mapped nodes; non-matching nodes are left unchanged.
     */
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

    /**
     * Parse an expression that contains no parentheses into an ExpressionNode, applying operator precedence where OR has lowest precedence, then AND, then NOT.
     *
     * @param expression The expression string without parentheses (whitespace removed).
     * @return The root ExpressionNode representing the parsed expression.
     */
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

    /**
     * Constructs an OR operation node from the given subexpression strings.
     *
     * Each string in `parts` is parsed (respecting AND/NOT precedence) and added as a child of the returned OR node.
     *
     * @param parts Subexpression strings to combine with a logical OR.
     * @return An `ExpressionNode.Operation` representing the logical OR of the parsed parts.
     */
    private fun buildOrNode(parts: List<String>): ExpressionNode {
        val orNode = ExpressionNode.Operation(ExpressionOperator.OR)
        parts.forEach { part ->
            orNode.children.add(parseAndNot(part))
        }
        return orNode
    }

    /**
     * Parse an expression into an AND operation if it contains the AND operator, otherwise parse it as a NOT or feature reference.
     *
     * @param expression The expression segment to parse (whitespace removed).
     * @return An ExpressionNode representing either an AND operation over the expression's parts or the parsed NOT/feature node.
     */
    private fun parseAndNot(expression: String): ExpressionNode {
        val andParts = expression.split(AND_CHAR)
        return if (andParts.size > 1) {
            buildAndNode(andParts)
        } else {
            parseNot(expression)
        }
    }

    /**
     * Constructs an AND operation node from the given expression parts.
     *
     * @param parts Sub-expressions that should be combined with a logical AND; each part is parsed as an operand.
     * @return An ExpressionNode.Operation representing a logical AND with each parsed part as a child.
     */
    private fun buildAndNode(parts: List<String>): ExpressionNode {
        val andNode = ExpressionNode.Operation(ExpressionOperator.AND)
        parts.forEach { part ->
            andNode.children.add(parseNot(part))
        }
        return andNode
    }

    /**
     * Parse an expression that may represent a logical negation into an ExpressionNode.
     *
     * @param expression The expression text, possibly starting with the NOT operator character.
     * @return An `Operation` node representing logical NOT applied to the remainder if the expression starts with the NOT operator; otherwise a `FeatureReference` node for the expression.
     */
    private fun parseNot(expression: String): ExpressionNode = if (expression.startsWith(NOT_CHAR)) {
        buildNotNode(expression.substring(1))
    } else {
        ExpressionNode.FeatureReference(expression)
    }

    /**
     * Create a NOT operation node for the given expression.
     *
     * Recursively parses the expression to handle consecutive NOT operators (e.g., `!!feature`).
     *
     * @param expression The expression to negate (may start with additional NOT operators).
     * @return An ExpressionNode representing a NOT operation whose child is the parsed expression.
     */
    private fun buildNotNode(expression: String): ExpressionNode {
        val notNode = ExpressionNode.Operation(ExpressionOperator.NOT)
        notNode.children.add(parseNot(expression))
        return notNode
    }
}
