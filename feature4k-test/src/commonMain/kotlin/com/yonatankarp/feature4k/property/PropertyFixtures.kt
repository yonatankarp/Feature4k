package com.yonatankarp.feature4k.property

/**
 * Test fixtures for Property-related tests.
 * These can be reused across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object PropertyFixtures {
    /** Sample high-precision decimal value for testing BigDecimal properties */
    const val HIGH_PRECISION_DECIMAL = "123.456789012345"

    /** Low-tier price value for testing fixed value constraints */
    const val PRICE_TIER_LOW = "19.99"

    /** Medium-tier price value for testing fixed value constraints */
    const val PRICE_TIER_MEDIUM = "99.99"

    /** High-tier price value for testing fixed value constraints */
    const val PRICE_TIER_HIGH = "199.99"

    /** Set of all price tiers for testing fixed value constraints */
    val PRICE_TIERS = setOf(PRICE_TIER_LOW, PRICE_TIER_MEDIUM, PRICE_TIER_HIGH)
}
