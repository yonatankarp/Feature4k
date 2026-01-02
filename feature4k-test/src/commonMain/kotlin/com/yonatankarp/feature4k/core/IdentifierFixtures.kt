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

    // Expression testing feature identifiers

    /** Feature representing dark mode UI capability. */
    const val DARK_MODE = "dark-mode"

    /** Feature representing new UI redesign. */
    const val NEW_UI = "new-ui"

    /** Feature representing analytics tracking. */
    const val ANALYTICS = "analytics"

    /** Feature representing premium user dashboard. */
    const val PREMIUM_DASHBOARD = "premium-dashboard"

    /** Feature representing basic user dashboard. */
    const val BASIC_DASHBOARD = "basic-dashboard"

    /** Feature representing beta access program. */
    const val BETA_ACCESS = "beta-access"

    // User identifiers

    /** Common test user - alice. */
    const val ALICE = "alice"

    /** Common test user - bob. */
    const val BOB = "bob"

    /** Common test user - charlie. */
    const val CHARLIE = "charlie"

    /** Admin user identifier. */
    const val ADMIN = "admin"

    /** Regular user identifier. */
    const val USER_REGULAR = "user"

    /** Superuser identifier. */
    const val USER_SUPERUSER = "superuser"
}
