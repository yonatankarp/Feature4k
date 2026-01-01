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
    /** Simple OR expression: dark-mode | new-ui */
    fun darkModeOrNewUi() = ExpressionFlipStrategy(expression = "$DARK_MODE|$NEW_UI")

    /** Simple AND expression: dark-mode & new-ui */
    fun darkModeAndNewUi() = ExpressionFlipStrategy(expression = "$DARK_MODE&$NEW_UI")

    /** NOT expression: !analytics */
    fun notAnalytics() = ExpressionFlipStrategy(expression = "!$ANALYTICS")

    /** Complex expression: basic-dashboard & (beta-access | analytics) */
    fun premiumRequiresBasicAndBeta() = ExpressionFlipStrategy(expression = "$BASIC_DASHBOARD&($BETA_ACCESS|$ANALYTICS)")

    /** Expression with NOT and parentheses: (dark-mode | new-ui) & !analytics */
    fun newFeaturesWithoutAnalytics() = ExpressionFlipStrategy(expression = "($DARK_MODE|$NEW_UI)&!$ANALYTICS")

    /** Single feature reference */
    fun darkModeOnly() = ExpressionFlipStrategy(expression = DARK_MODE)
}
