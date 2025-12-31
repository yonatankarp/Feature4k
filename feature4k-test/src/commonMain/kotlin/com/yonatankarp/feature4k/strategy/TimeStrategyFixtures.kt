package com.yonatankarp.feature4k.strategy

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Test fixtures for time-based flipping strategies.
 * These can be reused across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object TimeStrategyFixtures {
    /**
     * Standard business hours interval expression (09:00-17:00).
     * Commonly used in tests for office hour strategies.
     */
    const val STANDARD_BUSINESS_HOURS = "09:00-17:00"

    /**
     * Creates a ReleaseDateFlipStrategy with a specified release date.
     *
     * @param releaseDate The instant when the feature should be released
     * @return A configured ReleaseDateFlipStrategy
     */
    fun releaseDateStrategy(releaseDate: Instant) = ReleaseDateFlipStrategy(releaseDate = releaseDate)

    /**
     * Creates a ReleaseDateFlipStrategy with a release date from an ISO-8601 string.
     *
     * @param releaseDate The release instant as ISO-8601 string
     * @return A configured ReleaseDateFlipStrategy
     */
    fun releaseDateStrategy(releaseDate: String) = releaseDateStrategy(Instant.parse(releaseDate))

    /**
     * Creates a simple office hours strategy for standard weekday 9-5 hours.
     *
     * @return An OfficeHourStrategy configured for Monday-Friday 09:00-17:00
     */
    fun standardWeekdayHours() = OfficeHourStrategy(
        weeklySchedule = mapOf(
            DayOfWeek.MONDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
            DayOfWeek.TUESDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
            DayOfWeek.WEDNESDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
            DayOfWeek.THURSDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
            DayOfWeek.FRIDAY to listOf(HourInterval(LocalTime(9, 0), LocalTime(17, 0))),
        ),
    )

    /**
     * Creates an office hours strategy with split shifts (lunch break).
     *
     * @param dayOfWeek The day of week to configure
     * @param morningStart Morning shift start time (default: 08:00)
     * @param morningEnd Morning shift end time (default: 12:00)
     * @param afternoonStart Afternoon shift start time (default: 13:00)
     * @param afternoonEnd Afternoon shift end time (default: 17:00)
     * @return An OfficeHourStrategy with split shifts
     */
    fun splitShiftHours(
        dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
        morningStart: LocalTime = LocalTime(8, 0),
        morningEnd: LocalTime = LocalTime(12, 0),
        afternoonStart: LocalTime = LocalTime(13, 0),
        afternoonEnd: LocalTime = LocalTime(17, 0),
    ) = OfficeHourStrategy(
        weeklySchedule = mapOf(
            dayOfWeek to listOf(
                HourInterval(morningStart, morningEnd),
                HourInterval(afternoonStart, afternoonEnd),
            ),
        ),
    )

    /**
     * Creates an office hours strategy with public holidays.
     *
     * @param weeklySchedule The weekly schedule
     * @param holidays Set of dates to treat as public holidays (always closed)
     * @return An OfficeHourStrategy with public holidays
     */
    fun hoursWithHolidays(
        weeklySchedule: Map<DayOfWeek, List<HourInterval>>,
        holidays: Set<LocalDate>,
    ) = OfficeHourStrategy(
        weeklySchedule = weeklySchedule,
        publicHolidays = holidays,
    )

    /**
     * Creates an office hours strategy with special openings.
     *
     * @param weeklySchedule The weekly schedule
     * @param specialOpenings Map of specific dates to custom hour intervals
     * @return An OfficeHourStrategy with special openings
     */
    fun hoursWithSpecialOpenings(
        weeklySchedule: Map<DayOfWeek, List<HourInterval>>,
        specialOpenings: Map<LocalDate, List<HourInterval>>,
    ) = OfficeHourStrategy(
        weeklySchedule = weeklySchedule,
        specialOpenings = specialOpenings,
    )
}
