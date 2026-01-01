package com.yonatankarp.feature4k.strategy.expression

import com.yonatankarp.feature4k.core.IdentifierFixtures.ANALYTICS
import com.yonatankarp.feature4k.core.IdentifierFixtures.BASIC_DASHBOARD
import com.yonatankarp.feature4k.core.IdentifierFixtures.BETA_ACCESS
import com.yonatankarp.feature4k.core.IdentifierFixtures.DARK_MODE
import com.yonatankarp.feature4k.core.IdentifierFixtures.NEW_UI
import com.yonatankarp.feature4k.core.IdentifierFixtures.PREMIUM_DASHBOARD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [ExpressionParser].
 *
 * @author Yonatan Karp-Rudin
 */
class ExpressionParserTest {
    private val parser = ExpressionParser()

    @Test
    fun `should parse single feature reference`() {
        // Given
        val expression = DARK_MODE

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result is ExpressionNode.FeatureReference)
        assertEquals(DARK_MODE, result.featureName)
    }

    @Test
    fun `should parse simple OR expression`() {
        // Given
        val expression = "$DARK_MODE|$NEW_UI"
        val states = mapOf(DARK_MODE to false, NEW_UI to true)

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(states), "OR with one true should evaluate to true")
    }

    @Test
    fun `should parse simple AND expression`() {
        // Given
        val expression = "$DARK_MODE&$NEW_UI"
        val statesAllTrue = mapOf(DARK_MODE to true, NEW_UI to true)
        val statesOneFalse = mapOf(DARK_MODE to true, NEW_UI to false)

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(statesAllTrue), "AND with all true should be true")
        assertFalse(result.evaluate(statesOneFalse), "AND with one false should be false")
    }

    @Test
    fun `should parse NOT expression`() {
        // Given
        val expression = "!$ANALYTICS"
        val statesTrue = mapOf(ANALYTICS to true)
        val statesFalse = mapOf(ANALYTICS to false)

        // When
        val result = parser.parse(expression)

        // Then
        assertFalse(result.evaluate(statesTrue), "NOT true should be false")
        assertTrue(result.evaluate(statesFalse), "NOT false should be true")
    }

    @Test
    fun `should respect operator precedence - NOT before AND`() {
        // Given: !dark-mode & new-ui = (!dark-mode) & new-ui
        val expression = "!$DARK_MODE&$NEW_UI"
        val states = mapOf(DARK_MODE to false, NEW_UI to true)

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(states), "(!F) & T should be T & T = T")
    }

    @Test
    fun `should respect operator precedence - AND before OR`() {
        // Given: dark-mode | new-ui & analytics = dark-mode | (new-ui & analytics)
        val expression = "$DARK_MODE|$NEW_UI&$ANALYTICS"
        val states1 = mapOf(DARK_MODE to false, NEW_UI to true, ANALYTICS to true)
        val states2 = mapOf(DARK_MODE to false, NEW_UI to true, ANALYTICS to false)
        val states3 = mapOf(DARK_MODE to true, NEW_UI to false, ANALYTICS to false)

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(states1), "F | (T & T) = F | T = T")
        assertFalse(result.evaluate(states2), "F | (T & F) = F | F = F")
        assertTrue(result.evaluate(states3), "T | (F & F) = T | F = T")
    }

    @Test
    fun `should parse parenthesized expression`() {
        // Given: (dark-mode | new-ui) & analytics
        val expression = "($DARK_MODE|$NEW_UI)&$ANALYTICS"
        val states1 = mapOf(DARK_MODE to true, NEW_UI to false, ANALYTICS to true)
        val states2 = mapOf(DARK_MODE to false, NEW_UI to false, ANALYTICS to true)

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(states1), "(T | F) & T = T & T = T")
        assertFalse(result.evaluate(states2), "(F | F) & T = F & T = F")
    }

    @Test
    fun `should parse nested parentheses`() {
        // Given: ((dark-mode | new-ui) & analytics) | beta-access
        val expression = "(($DARK_MODE|$NEW_UI)&$ANALYTICS)|$BETA_ACCESS"
        val states1 = mapOf(
            DARK_MODE to false,
            NEW_UI to false,
            ANALYTICS to true,
            BETA_ACCESS to true,
        )
        val states2 = mapOf(
            DARK_MODE to true,
            NEW_UI to false,
            ANALYTICS to true,
            BETA_ACCESS to false,
        )

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(states1), "((F|F)&T)|T = (F&T)|T = F|T = T")
        assertTrue(result.evaluate(states2), "((T|F)&T)|F = (T&T)|F = T|F = T")
    }

    @Test
    fun `should parse complex premium dashboard requirement expression`() {
        // Given: premium-dashboard requires (basic-dashboard & (beta-access | analytics)) & !dark-mode
        val expression = "($BASIC_DASHBOARD&($BETA_ACCESS|$ANALYTICS))&!$DARK_MODE"
        val statesValid = mapOf(
            BASIC_DASHBOARD to true,
            BETA_ACCESS to true,
            ANALYTICS to false,
            DARK_MODE to false,
        )
        val statesInvalid = mapOf(
            BASIC_DASHBOARD to true,
            BETA_ACCESS to false,
            ANALYTICS to false,
            DARK_MODE to false,
        )

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(statesValid), "Should be true when requirements met")
        assertFalse(result.evaluate(statesInvalid), "Should be false when beta and analytics both false")
    }

    @Test
    fun `should ignore whitespace in expression`() {
        // Given
        val withSpaces = "  $DARK_MODE  |  $NEW_UI  "
        val withoutSpaces = "$DARK_MODE|$NEW_UI"
        val states = mapOf(DARK_MODE to true, NEW_UI to false)

        // When
        val result1 = parser.parse(withSpaces)
        val result2 = parser.parse(withoutSpaces)

        // Then
        assertEquals(result1.evaluate(states), result2.evaluate(states))
    }

    @Test
    fun `should parse expression with multiple AND operations`() {
        // Given
        val expression = "$DARK_MODE&$NEW_UI&$ANALYTICS"
        val statesAllTrue = mapOf(DARK_MODE to true, NEW_UI to true, ANALYTICS to true)
        val statesOneFalse = mapOf(DARK_MODE to true, NEW_UI to false, ANALYTICS to true)

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(statesAllTrue), "A & B & C with all true should be true")
        assertFalse(result.evaluate(statesOneFalse), "A & B & C with one false should be false")
    }

    @Test
    fun `should parse expression with multiple OR operations`() {
        // Given
        val expression = "$DARK_MODE|$NEW_UI|$ANALYTICS"
        val statesAllFalse = mapOf(DARK_MODE to false, NEW_UI to false, ANALYTICS to false)
        val statesOneTrue = mapOf(DARK_MODE to false, NEW_UI to true, ANALYTICS to false)

        // When
        val result = parser.parse(expression)

        // Then
        assertFalse(result.evaluate(statesAllFalse), "A | B | C with all false should be false")
        assertTrue(result.evaluate(statesOneTrue), "A | B | C with one true should be true")
    }

    @Test
    fun `should parse expression with NOT and parentheses`() {
        // Given: !(dark-mode & new-ui) = !dark-mode | !new-ui (De Morgan's law)
        val expression = "!($DARK_MODE&$NEW_UI)"
        val statesBothTrue = mapOf(DARK_MODE to true, NEW_UI to true)
        val statesOneFalse = mapOf(DARK_MODE to true, NEW_UI to false)

        // When
        val result = parser.parse(expression)

        // Then
        assertFalse(result.evaluate(statesBothTrue), "!(T & T) = !T = F")
        assertTrue(result.evaluate(statesOneFalse), "!(T & F) = !F = T")
    }

    @Test
    fun `should parse deeply nested expression`() {
        // Given: ((A | B) & (C | D)) | (!E & F)
        val expression = "(($DARK_MODE|$NEW_UI)&($ANALYTICS|$BETA_ACCESS))|(!$BASIC_DASHBOARD&$PREMIUM_DASHBOARD)"
        val states = mapOf(
            DARK_MODE to true,
            NEW_UI to false,
            ANALYTICS to false,
            BETA_ACCESS to true,
            BASIC_DASHBOARD to false,
            PREMIUM_DASHBOARD to true,
        )

        // When
        val result = parser.parse(expression)

        // Then: ((T|F)&(F|T))|((!F)&T) = (T&T)|(T&T) = T|T = T
        assertTrue(result.evaluate(states))
    }

    @Test
    fun `should parse expression evaluating feature dependency chain`() {
        // Given: premium requires basic, basic requires neither dark nor analytics
        val expression = "$BASIC_DASHBOARD&!$DARK_MODE&!$ANALYTICS"
        val statesValid = mapOf(BASIC_DASHBOARD to true, DARK_MODE to false, ANALYTICS to false)
        val statesInvalidDark = mapOf(BASIC_DASHBOARD to true, DARK_MODE to true, ANALYTICS to false)
        val statesInvalidAnalytics = mapOf(BASIC_DASHBOARD to true, DARK_MODE to false, ANALYTICS to true)

        // When
        val result = parser.parse(expression)

        // Then
        assertTrue(result.evaluate(statesValid), "Should be true when basic on, dark and analytics off")
        assertFalse(result.evaluate(statesInvalidDark), "Should be false when dark mode is on")
        assertFalse(result.evaluate(statesInvalidAnalytics), "Should be false when analytics is on")
    }
}
