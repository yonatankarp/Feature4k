package com.yonatankarp.feature4k.core

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Shared date and time fixtures for testing.
 * These can be reused across property tests, strategy tests, and other test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object DateTimeFixtures {
    /** ISO-8601 formatted timestamp for testing Instant properties and time-based features */
    const val TIMESTAMP_ISO = "2024-01-15T10:30:00Z"

    /** Local datetime timestamp for testing LocalDateTime properties */
    const val TIMESTAMP_LOCAL = "2024-01-15T10:30:00"

    /** Monday, December 23, 2024 at 10:00 AM - used for office hours testing */
    const val MONDAY_MORNING_DEC_23 = "2024-12-23T10:00:00"

    /** Christmas 2024 morning appointment slot (10:00 AM) */
    val CHRISTMAS_MORNING_SLOT = LocalDateTime.parse("2024-12-25T10:00:00")

    /** Christmas 2024 noon (12:00 PM) */
    val CHRISTMAS_NOON_SLOT = LocalDateTime.parse("2024-12-25T12:00:00")

    /** Christmas 2024 afternoon appointment slot (2:00 PM) */
    val CHRISTMAS_AFTERNOON_SLOT = LocalDateTime.parse("2024-12-25T14:00:00")

    /** Christmas 2024 evening appointment slot (6:00 PM) */
    val CHRISTMAS_EVENING_SLOT = LocalDateTime.parse("2024-12-25T18:00:00")

    /** Common appointment slots for Christmas 2024 */
    val CHRISTMAS_APPOINTMENT_SLOTS = setOf(
        CHRISTMAS_MORNING_SLOT,
        CHRISTMAS_AFTERNOON_SLOT,
        CHRISTMAS_EVENING_SLOT,
    )

    /** Mid-June 2024 midnight timestamp */
    val MID_JUNE_2024 = Instant.parse("2024-06-15T00:00:00Z")

    /** Christmas 2024 midnight timestamp */
    val CHRISTMAS_2024_MIDNIGHT = Instant.parse("2024-12-25T00:00:00Z")

    /** Christmas 2024 morning timestamp */
    val CHRISTMAS_2024_MORNING = Instant.parse("2024-12-25T10:00:00Z")

    /** Mid-June 2025 midnight timestamp */
    val MID_JUNE_2025 = Instant.parse("2025-06-15T00:00:00Z")

    /** Past date for testing historical data */
    val PAST_DATE = Instant.parse("2000-01-01T00:00:00Z")

    /** Future date for testing upcoming events */
    val FUTURE_DATE = Instant.parse("2099-12-31T23:59:59Z")

    /** Common release date milestones used in property validation tests */
    val RELEASE_MILESTONES = setOf(MID_JUNE_2024, CHRISTMAS_2024_MIDNIGHT, MID_JUNE_2025)

    /** Christmas 2024 date */
    val CHRISTMAS_2024 = LocalDate(2024, 12, 25)
}
