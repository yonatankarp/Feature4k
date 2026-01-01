package com.yonatankarp.feature4k.strategy.expression

import com.yonatankarp.feature4k.core.IdentifierFixtures.ANALYTICS
import com.yonatankarp.feature4k.core.IdentifierFixtures.BASIC_DASHBOARD
import com.yonatankarp.feature4k.core.IdentifierFixtures.DARK_MODE
import com.yonatankarp.feature4k.core.IdentifierFixtures.NEW_UI
import com.yonatankarp.feature4k.core.IdentifierFixtures.PREMIUM_DASHBOARD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [ExpressionNode].
 *
 * @author Yonatan Karp-Rudin
 */
class ExpressionNodeTest {
    @Test
    fun `should evaluate feature reference to true when present in states`() {
        // Given
        val node = ExpressionNode.FeatureReference(DARK_MODE)
        val states = mapOf(DARK_MODE to true)

        // When
        val result = node.evaluate(states)

        // Then
        assertTrue(result, "Feature reference should evaluate to true when present and true")
    }

    @Test
    fun `should evaluate feature reference to false when not present in states`() {
        // Given
        val node = ExpressionNode.FeatureReference(DARK_MODE)
        val states = emptyMap<String, Boolean>()

        // When
        val result = node.evaluate(states)

        // Then
        assertFalse(result, "Feature reference should evaluate to false when not present")
    }

    @Test
    fun `should evaluate feature reference to false when present but false`() {
        // Given
        val node = ExpressionNode.FeatureReference(DARK_MODE)
        val states = mapOf(DARK_MODE to false)

        // When
        val result = node.evaluate(states)

        // Then
        assertFalse(result, "Feature reference should evaluate to false when explicitly false")
    }

    @Test
    fun `should evaluate NOT operation correctly`() {
        // Given
        val node =
            ExpressionNode.Operation(
                operator = ExpressionOperator.NOT,
                children = mutableListOf(ExpressionNode.FeatureReference(ANALYTICS)),
            )
        val statesTrue = mapOf(ANALYTICS to true)
        val statesFalse = mapOf(ANALYTICS to false)

        // When & Then
        assertFalse(node.evaluate(statesTrue), "NOT true should be false")
        assertTrue(node.evaluate(statesFalse), "NOT false should be true")
    }

    @Test
    fun `should evaluate AND operation with all true`() {
        // Given
        val node =
            ExpressionNode.Operation(
                operator = ExpressionOperator.AND,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                    ExpressionNode.FeatureReference(ANALYTICS),
                ),
            )
        val states = mapOf(DARK_MODE to true, NEW_UI to true, ANALYTICS to true)

        // When
        val result = node.evaluate(states)

        // Then
        assertTrue(result, "AND with all true should be true")
    }

    @Test
    fun `should evaluate AND operation with one false`() {
        // Given
        val node =
            ExpressionNode.Operation(
                operator = ExpressionOperator.AND,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                    ExpressionNode.FeatureReference(ANALYTICS),
                ),
            )
        val states = mapOf(DARK_MODE to true, NEW_UI to false, ANALYTICS to true)

        // When
        val result = node.evaluate(states)

        // Then
        assertFalse(result, "AND with any false should be false")
    }

    @Test
    fun `should evaluate OR operation with all false`() {
        // Given
        val node =
            ExpressionNode.Operation(
                operator = ExpressionOperator.OR,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                ),
            )
        val states = mapOf(DARK_MODE to false, NEW_UI to false)

        // When
        val result = node.evaluate(states)

        // Then
        assertFalse(result, "OR with all false should be false")
    }

    @Test
    fun `should evaluate OR operation with one true`() {
        // Given
        val node =
            ExpressionNode.Operation(
                operator = ExpressionOperator.OR,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                    ExpressionNode.FeatureReference(ANALYTICS),
                ),
            )
        val states = mapOf(DARK_MODE to false, NEW_UI to true, ANALYTICS to false)

        // When
        val result = node.evaluate(states)

        // Then
        assertTrue(result, "OR with any true should be true")
    }

    @Test
    fun `should evaluate complex expression - premium dashboard requires basic and beta`() {
        // Given: (basic-dashboard | beta-access) & !analytics
        val orNode =
            ExpressionNode.Operation(
                operator = ExpressionOperator.OR,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(BASIC_DASHBOARD),
                    ExpressionNode.FeatureReference(BASIC_DASHBOARD.replace("basic", "beta")),
                ),
            )
        val notNode =
            ExpressionNode.Operation(
                operator = ExpressionOperator.NOT,
                children = mutableListOf(ExpressionNode.FeatureReference(ANALYTICS)),
            )
        val andNode =
            ExpressionNode.Operation(
                operator = ExpressionOperator.AND,
                children = mutableListOf(orNode, notNode),
            )

        val states1 = mapOf(BASIC_DASHBOARD to true, ANALYTICS to false)
        val states2 = mapOf(BASIC_DASHBOARD to false, ANALYTICS to false)
        val states3 = mapOf(BASIC_DASHBOARD to true, ANALYTICS to true)

        // When & Then
        assertTrue(andNode.evaluate(states1), "Should be true when basic enabled and no analytics")
        assertFalse(andNode.evaluate(states2), "Should be false when neither basic nor beta enabled")
        assertFalse(andNode.evaluate(states3), "Should be false when analytics is enabled")
    }

    @Test
    fun `should format feature reference toString correctly`() {
        // Given
        val node = ExpressionNode.FeatureReference(PREMIUM_DASHBOARD)

        // When
        val result = node.toString()

        // Then
        assertEquals(PREMIUM_DASHBOARD, result)
    }

    @Test
    fun `should format NOT operation toString correctly`() {
        // Given
        val node =
            ExpressionNode.Operation(
                operator = ExpressionOperator.NOT,
                children = mutableListOf(ExpressionNode.FeatureReference(DARK_MODE)),
            )

        // When
        val result = node.toString()

        // Then
        assertEquals("!$DARK_MODE", result)
    }

    @Test
    fun `should format AND operation toString correctly`() {
        // Given
        val node =
            ExpressionNode.Operation(
                operator = ExpressionOperator.AND,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                ),
            )

        // When
        val result = node.toString()

        // Then
        assertEquals("$DARK_MODE AND $NEW_UI", result)
    }

    @Test
    fun `should format OR operation toString correctly`() {
        // Given
        val node =
            ExpressionNode.Operation(
                operator = ExpressionOperator.OR,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                ),
            )

        // When
        val result = node.toString()

        // Then
        assertEquals("$DARK_MODE OR $NEW_UI", result)
    }

    @Test
    fun `should format nested operations toString with parentheses`() {
        // Given: (dark-mode | new-ui) & !analytics
        val orNode =
            ExpressionNode.Operation(
                operator = ExpressionOperator.OR,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                ),
            )
        val notNode =
            ExpressionNode.Operation(
                operator = ExpressionOperator.NOT,
                children = mutableListOf(ExpressionNode.FeatureReference(ANALYTICS)),
            )
        val andNode =
            ExpressionNode.Operation(
                operator = ExpressionOperator.AND,
                children = mutableListOf(orNode, notNode),
            )

        // When
        val result = andNode.toString()

        // Then
        assertEquals("($DARK_MODE OR $NEW_UI) AND (!$ANALYTICS)", result)
    }

    @Test
    fun `should support data class equality for FeatureReference`() {
        // Given
        val node1 = ExpressionNode.FeatureReference(DARK_MODE)
        val node2 = ExpressionNode.FeatureReference(DARK_MODE)
        val node3 = ExpressionNode.FeatureReference(NEW_UI)

        // When & Then
        assertEquals(node1, node2, "Same feature references should be equal")
        assertEquals(node1.hashCode(), node2.hashCode(), "Equal nodes should have same hashCode")
        assertFalse(node1 == node3, "Different feature references should not be equal")
    }

    @Test
    fun `should support data class equality for Operation`() {
        // Given
        val node1 =
            ExpressionNode.Operation(
                operator = ExpressionOperator.AND,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                ),
            )
        val node2 =
            ExpressionNode.Operation(
                operator = ExpressionOperator.AND,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                ),
            )
        val node3 =
            ExpressionNode.Operation(
                operator = ExpressionOperator.OR,
                children =
                mutableListOf(
                    ExpressionNode.FeatureReference(DARK_MODE),
                    ExpressionNode.FeatureReference(NEW_UI),
                ),
            )

        // When & Then
        assertEquals(node1, node2, "Same operations should be equal")
        assertFalse(node1 == node3, "Different operators should not be equal")
    }
}
