package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that enables features during specific office hours.
 *
 * This strategy allows fine-grained control over when features are available based on:
 * - **Weekly schedule**: Define operating hours for each day of the week
 * - **Special openings**: Override schedule for specific dates (e.g., extended hours)
 * - **Public holidays**: Disable features on specific dates regardless of weekly schedule
 *
 * The priority order is: Special Openings > Public Holidays > Weekly Schedule
 *
 * ## Use Cases
 *
 * - **Business hours**: Enable features only during office hours
 * - **Maintenance windows**: Disable features during maintenance times
 * - **Geographic schedules**: Different hours for different regions
 * - **Holiday handling**: Automatically disable features on public holidays
 * - **Special events**: Extended hours for special occasions
 *
 * ## Time Handling
 *
 * Uses [kotlinx.datetime] for multiplatform time handling. By default, uses UTC timezone
 * and current time for consistent behavior across environments. Both can be overridden:
 * - Via constructor: specify a different timezone when creating the strategy
 * - Via execution context: override date/time and timezone for testing
 *   - [OVERRIDE_DATETIME_KEY]: Override current date/time (ISO-8601 format)
 *   - [OVERRIDE_TIMEZONE_KEY]: Override timezone (e.g., "America/New_York")
 *
 * ## Examples
 *
 * ### Basic weekday office hours
 * ```kotlin
 * val strategy = OfficeHourStrategy(
 *     weeklySchedule = mapOf(
 *         DayOfWeek.MONDAY to listOf(HourInterval("09:00-17:00")),
 *         DayOfWeek.TUESDAY to listOf(HourInterval("09:00-17:00")),
 *         DayOfWeek.WEDNESDAY to listOf(HourInterval("09:00-17:00")),
 *         DayOfWeek.THURSDAY to listOf(HourInterval("09:00-17:00")),
 *         DayOfWeek.FRIDAY to listOf(HourInterval("09:00-17:00"))
 *     )
 * )
 * ```
 *
 * ### Split shifts with lunch break
 * ```kotlin
 * val strategy = OfficeHourStrategy(
 *     weeklySchedule = mapOf(
 *         DayOfWeek.MONDAY to listOf(
 *             HourInterval("08:00-12:00"),
 *             HourInterval("13:00-17:00")
 *         )
 *     )
 * )
 * ```
 *
 * ### With holidays and special openings
 * ```kotlin
 * val strategy = OfficeHourStrategy(
 *     weeklySchedule = mapOf(
 *         DayOfWeek.MONDAY to listOf(HourInterval("09:00-17:00"))
 *     ),
 *     publicHolidays = setOf(
 *         LocalDate(2024, 12, 25), // Christmas
 *         LocalDate(2024, 1, 1)    // New Year
 *     ),
 *     specialOpenings = mapOf(
 *         LocalDate(2024, 12, 24) to listOf(HourInterval("09:00-13:00")) // Half day
 *     )
 * )
 * ```
 *
 * @property weeklySchedule Map of day of week to list of hour intervals when feature is enabled
 * @property publicHolidays Set of dates when feature is always disabled
 * @property specialOpenings Map of specific dates to custom hour intervals (overrides weekly schedule)
 * @property timezone The timezone to use for time calculations (default: UTC for consistency across environments)
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("office-hours")
data class OfficeHourStrategy(
    val weeklySchedule: Map<DayOfWeek, List<HourInterval>> = emptyMap(),
    val publicHolidays: Set<LocalDate> = emptySet(),
    val specialOpenings: Map<LocalDate, List<HourInterval>> = emptyMap(),
    @EncodeDefault val timezone: String = "UTC",
) : FlippingStrategy {
    /**
     * Evaluates whether the feature should be enabled based on current time and schedule.
     *
     * Evaluation priority:
     * 1. If current date is in [specialOpenings], check those intervals
     * 2. If current date is in [publicHolidays], return false
     * 3. Otherwise, check [weeklySchedule] for current day of week
     *
     * The current time and timezone can be overridden for testing via execution context
     * custom parameters.
     *
     * @param evalContext The evaluation context (may contain time/timezone overrides)
     * @return `true` if feature should be enabled based on schedule, `false` otherwise
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val timezone = evalContext.context.getParam(OVERRIDE_TIMEZONE_KEY)?.let { TimeZone.of(it) }
            ?: TimeZone.of(this@OfficeHourStrategy.timezone)

        val now = evalContext.context.getParam(OVERRIDE_DATETIME_KEY)?.let {
            LocalDateTime.parse(it)
        } ?: Clock.System.now().toLocalDateTime(timezone)

        return isOpen(now)
    }

    /**
     * Determines whether the feature should be enabled at the given date/time.
     *
     * This is the core evaluation logic that checks the schedule configuration in priority order:
     * 1. Special openings (highest priority) - custom hours for specific dates override weekly schedule
     * 2. Public holidays (second priority) - always closed
     * 3. Weekly schedule (default) - regular operating hours by day of week
     *
     * When a date has special opening hours defined, those hours are the ONLY hours the feature
     * is enabled on that date. The weekly schedule is completely ignored for that day.
     *
     * @param now The date and time to check against the schedule
     * @return `true` if the feature should be enabled at the given time, `false` otherwise
     */
    fun isOpen(now: LocalDateTime): Boolean = when {
        hasSpecialOpeningDefined(now) -> matchesSpecialOpening(now)
        isPublicHoliday(now) -> false
        else -> matchesWeeklySchedule(now)
    }

    /**
     * Checks if a special opening is defined for the given date.
     *
     * Special openings override both public holidays and weekly schedules for the entire day.
     *
     * @param now The date and time to check
     * @return `true` if there is a special opening configured for this date, `false` otherwise
     */
    private fun hasSpecialOpeningDefined(now: LocalDateTime): Boolean = now.date in specialOpenings

    /**
     * Checks if the current time falls within the special opening hours for the given date.
     *
     * @param now The date and time to check
     * @return `true` if the time matches the special opening intervals, `false` otherwise
     */
    private fun matchesSpecialOpening(now: LocalDateTime): Boolean = specialOpenings[now.date]?.let { matchesAnyInterval(now.time, it) } ?: false

    /**
     * Checks if the given date is a configured public holiday.
     *
     * @param date The date and time to check
     * @return `true` if the date is in the public holidays set, `false` otherwise
     */
    private fun isPublicHoliday(date: LocalDateTime): Boolean = date.date in publicHolidays

    /**
     * Checks if the feature should be enabled based on the weekly schedule.
     *
     * @param now The date and time to check
     * @return `true` if the day has a schedule and the time falls within its intervals, `false` otherwise
     */
    private fun matchesWeeklySchedule(now: LocalDateTime): Boolean = weeklySchedule[now.dayOfWeek]
        ?.let { matchesAnyInterval(now.time, it) }
        ?: false

    /**
     * Checks if the given time matches any of the provided hour intervals.
     *
     * @param time The time to check
     * @param intervals The list of intervals to check against
     * @return `true` if time matches at least one interval, `false` otherwise
     */
    private fun matchesAnyInterval(
        time: LocalTime,
        intervals: List<HourInterval>,
    ): Boolean = intervals.any { it.matches(time) }

    companion object {
        /**
         * Custom parameter key for overriding the current date/time in tests.
         *
         * Value should be an ISO-8601 datetime string (e.g., "2024-12-25T10:30:00").
         */
        const val OVERRIDE_DATETIME_KEY = "overrideDateTime"

        /**
         * Custom parameter key for overriding the timezone in tests.
         *
         * Value should be a valid timezone ID (e.g., "America/New_York", "UTC").
         */
        const val OVERRIDE_TIMEZONE_KEY = "overrideTimezone"
    }
}
