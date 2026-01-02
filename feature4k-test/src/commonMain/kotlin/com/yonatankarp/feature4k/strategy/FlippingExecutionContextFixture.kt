package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FlippingExecutionContext
import com.yonatankarp.feature4k.strategy.OfficeHourStrategy.Companion.OVERRIDE_DATETIME_KEY
import com.yonatankarp.feature4k.strategy.OfficeHourStrategy.Companion.OVERRIDE_TIMEZONE_KEY
import com.yonatankarp.feature4k.strategy.ReleaseDateFlipStrategy.Companion.OVERRIDE_INSTANT_KEY

/**
 * Test fixtures for FlippingStrategy evaluation.
 * These can be reused across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object FlippingExecutionContextFixture {
    /** Common test region value used across strategy tests */
    const val REGION_US_EAST = "us-east"

    /**
     * Creates an empty FlippingExecutionContext for testing.
     *
     * @return An empty FlippingExecutionContext with no user, client, server, or custom params
     */
    fun emptyExecutionContext() = FlippingExecutionContext.empty()

    /**
     * Creates a FlippingExecutionContext with a user for testing.
     *
     * @param user The user identifier (default: "test-user", can be null)
     * @param client Optional client identifier
     * @param server Optional server identifier
     * @param customParams Custom parameters map (default: empty)
     * @return A FlippingExecutionContext configured for testing
     */
    fun executionContextWithUser(
        user: String? = "test-user",
        client: String? = null,
        server: String? = null,
        customParams: Map<String, String> = emptyMap(),
    ) = FlippingExecutionContext(
        user = user,
        client = client,
        server = server,
        customParams = customParams,
    )

    /**
     * Creates a FlippingExecutionContext with an overridden instant for testing time-based strategies.
     *
     * @param instant The instant to use as the current time (ISO-8601 format)
     * @return A FlippingExecutionContext with the instant override
     */
    fun contextWithInstant(instant: String) = FlippingExecutionContext(
        customParams = mapOf(OVERRIDE_INSTANT_KEY to instant),
    )

    /**
     * Creates a FlippingExecutionContext with an overridden datetime for testing time-based strategies.
     *
     * @param dateTime The datetime to use as the current time (ISO-8601 format)
     * @param timezone Optional timezone ID (e.g., "America/New_York", "UTC")
     * @return A FlippingExecutionContext with the datetime and timezone overrides
     */
    fun contextWithDateTime(
        dateTime: String,
        timezone: String? = null,
    ) = FlippingExecutionContext(
        customParams = buildMap {
            put(OVERRIDE_DATETIME_KEY, dateTime)
            timezone?.let { put(OVERRIDE_TIMEZONE_KEY, it) }
        },
    )

    /**
     * Creates a FlippingExecutionContext with authorities for testing authorization-based strategies.
     *
     * @param authorities Comma-separated string of authority identifiers
     * @param user Optional user identifier (default: "test-user")
     * @return A FlippingExecutionContext with authorities in custom params
     */
    fun contextWithAuthorities(
        authorities: String,
        user: String? = "test-user",
    ) = FlippingExecutionContext(
        user = user,
        customParams = mapOf("authorities" to authorities),
    )
}
