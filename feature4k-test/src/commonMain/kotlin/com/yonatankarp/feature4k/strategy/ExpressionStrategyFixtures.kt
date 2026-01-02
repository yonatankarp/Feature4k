package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.IdentifierFixtures.ANALYTICS
import com.yonatankarp.feature4k.core.IdentifierFixtures.BASIC_DASHBOARD
import com.yonatankarp.feature4k.core.IdentifierFixtures.BETA_ACCESS
import com.yonatankarp.feature4k.core.IdentifierFixtures.DARK_MODE
import com.yonatankarp.feature4k.core.IdentifierFixtures.NEW_UI

/**
 * Test fixtures for [ExpressionFlipStrategy].
 *
 * @author Yonatan Karp-Rudin
 */
object ExpressionStrategyFixtures {
    /**
     * Creates an ExpressionFlipStrategy representing the logical OR of dark mode and new UI.
     *
     * @return An ExpressionFlipStrategy with the expression "$DARK_MODE|$NEW_UI".
     */
    fun darkModeOrNewUi() = ExpressionFlipStrategy(expression = "$DARK_MODE|$NEW_UI")

    /**
     * Expression that requires both dark-mode and new-ui to be enabled.
     *
     * @return An ExpressionFlipStrategy configured with the expression "$DARK_MODE&$NEW_UI".
     */
    fun darkModeAndNewUi() = ExpressionFlipStrategy(expression = "$DARK_MODE&$NEW_UI")

    /**
     * Expression strategy that matches when the `ANALYTICS` feature is disabled.
     *
     * @return An ExpressionFlipStrategy with expression `!$ANALYTICS`.
     */
    fun notAnalytics() = ExpressionFlipStrategy(expression = "!$ANALYTICS")

    /**
     * Builds an ExpressionFlipStrategy that requires the basic dashboard and either beta access or analytics.
     *
     * @return An ExpressionFlipStrategy configured with the expression `BASIC_DASHBOARD & (BETA_ACCESS | ANALYTICS)`.
     */
    fun premiumRequiresBasicAndBeta() = ExpressionFlipStrategy(expression = "$BASIC_DASHBOARD&($BETA_ACCESS|$ANALYTICS)")

    /**
     * Builds a feature expression that enables dark-mode or new-ui while excluding analytics.
     *
     * @return An ExpressionFlipStrategy configured with the expression "($DARK_MODE|$NEW_UI)&!$ANALYTICS".
     */
    fun newFeaturesWithoutAnalytics() = ExpressionFlipStrategy(expression = "($DARK_MODE|$NEW_UI)&!$ANALYTICS")

    /**
     * Creates an ExpressionFlipStrategy that references the dark mode feature.
     *
     * @return An ExpressionFlipStrategy whose expression is `DARK_MODE`.
     */
    fun darkModeOnly() = ExpressionFlipStrategy(expression = DARK_MODE)
}
