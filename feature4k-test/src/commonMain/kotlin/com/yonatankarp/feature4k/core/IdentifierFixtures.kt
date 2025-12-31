package com.yonatankarp.feature4k.core

/**
 * Common identifier fixtures used across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object IdentifierFixtures {
    /**
     * Identifier representing a non-existent entity used in tests
     * to verify behavior when accessing missing or invalid entities.
     */
    const val NON_EXISTENT = "non-existent"

    /**
     * Default test feature identifier used across test fixtures and test cases.
     */
    const val FEATURE_UID = "test-feature"

    /**
     * Default test property identifier used across test fixtures and test cases.
     */
    const val PROPERTY_UID = "test-property"
}
