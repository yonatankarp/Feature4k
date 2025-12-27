package com.yonatankarp.feature4k.strategy

import kotlin.test.assertTrue

/**
 * Helper functions for testing flipping strategies.
 *
 * @author Yonatan Karp-Rudin
 */

/**
 * Asserts that the actual percentage is within an acceptable margin of the expected percentage.
 *
 * @param expected The expected percentage (0.0 to 1.0)
 * @param actual The actual percentage (0.0 to 1.0)
 * @param marginPercent The acceptable margin as a percentage (default 10%, meaning +/- 10% of expected)
 * @param message Optional custom message to display on failure
 *
 * @example
 * ```kotlin
 * assertPercentageWithin(expected = 0.5, actual = 0.52) // passes (52% within 10% margin of 50%)
 * assertPercentageWithin(expected = 0.25, actual = 0.26, marginPercent = 5.0) // passes
 * assertPercentageWithin(expected = 0.5, actual = 0.7) // fails (70% exceeds 10% margin from 50%)
 * ```
 */
fun assertPercentageWithin(
    expected: Double,
    actual: Double,
    marginPercent: Double = 5.0,
    message: String? = null,
) {
    require(expected in 0.0..1.0) { "Expected percentage must be between 0.0 and 1.0, got: $expected" }
    require(actual in 0.0..1.0) { "Actual percentage must be between 0.0 and 1.0, got: $actual" }
    require(marginPercent > 0.0) { "Margin percent must be positive, got: $marginPercent" }

    val margin = expected * (marginPercent / 100.0)
    val lowerBound = (expected - margin).coerceAtLeast(0.0)
    val upperBound = (expected + margin).coerceAtMost(1.0)

    val defaultMessage = buildString {
        append("Expected approximately ${expected * 100}% ")
        append("(within $marginPercent% margin: ${lowerBound * 100}%-${upperBound * 100}%), ")
        append("but got ${actual * 100}%")
    }

    assertTrue(
        actual in lowerBound..upperBound,
        message ?: defaultMessage,
    )
}
