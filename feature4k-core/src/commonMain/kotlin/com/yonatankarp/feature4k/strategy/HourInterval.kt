package com.yonatankarp.feature4k.strategy

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * Represents a time interval within a single day, from a start time to an end time.
 *
 * Used by [OfficeHourStrategy] to define when features should be enabled during a day.
 * The interval is inclusive of the start time and exclusive of the end time.
 *
 * ## Important: Overnight Intervals Not Supported
 *
 * This class does **not** support intervals that cross midnight (e.g., "22:00-02:00").
 * If you need overnight coverage, define separate intervals for each day:
 * - Day 1: "22:00-23:59"
 * - Day 2: "00:00-02:00"
 *
 * Reversed time bounds (where from > to) will be automatically normalized by swapping
 * the values, which may produce unexpected results for intended overnight shifts.
 *
 * ## Format
 *
 * The interval can be created from a string expression in the format "HH:mm-HH:mm",
 * for example: "08:00-12:00" or "13:30-18:00".
 *
 * ## Time Comparison
 *
 * The interval uses [kotlinx.datetime.LocalTime] for time-only comparisons, which means
 * it compares only hours and minutes, ignoring dates, timezones, and seconds.
 *
 * ## Examples
 *
 * ```kotlin
 * // Morning office hours
 * val morning = HourInterval("08:00-12:00")
 * morning.matches(LocalTime(10, 30)) // true
 * morning.matches(LocalTime(13, 0))  // false
 *
 * // Afternoon hours
 * val afternoon = HourInterval("13:30-18:00")
 * afternoon.matches(LocalTime(15, 0)) // true
 * afternoon.matches(LocalTime(12, 0)) // false
 * ```
 *
 * @property from The start time of the interval (inclusive)
 * @property to The end time of the interval (exclusive)
 * @throws IllegalArgumentException if the expression format is invalid
 * @author Yonatan Karp-Rudin
 */
@Serializable
data class HourInterval(
    val from: LocalTime,
    val to: LocalTime,
) {
    /**
     * Creates an HourInterval from a string expression in the format "HH:mm-HH:mm".
     *
     * **Note:** Reversed time bounds are automatically swapped to ensure `from` is before `to`.
     * This means "18:00-08:00" becomes `from = 08:00, to = 18:00`, which is likely **not**
     * what you want for an overnight shift. See class documentation for handling overnight intervals.
     *
     * @param expression The time interval expression (e.g., "08:00-12:00")
     * @throws IllegalArgumentException if the expression format is invalid
     */
    constructor(expression: String) : this(parseExpression(expression))

    /**
     * Internal constructor used by the string constructor after parsing.
     */
    private constructor(bounds: TimeBounds) : this(bounds.from, bounds.to)

    /**
     * Checks if the given time falls within this interval.
     *
     * The interval is inclusive of the start time and exclusive of the end time.
     * For example, an interval "08:00-12:00" matches 08:00 but not 12:00.
     *
     * @param time The time to check against this interval
     * @return `true` if the time is within the interval, `false` otherwise
     */
    fun matches(time: LocalTime): Boolean = time >= from && time < to

    private data class TimeBounds(val from: LocalTime, val to: LocalTime)

    companion object {
        /**
         * Parses a time interval expression into start and end LocalTime values.
         *
         * @param expression The time interval expression in format "HH:mm-HH:mm"
         * @return TimeBounds with from and to times, automatically ordered
         * @throws IllegalArgumentException if the format is invalid
         */
        private fun parseExpression(expression: String): TimeBounds {
            val trimmed = expression.trim()
            require(trimmed.isNotEmpty()) { "Hour interval expression cannot be empty" }

            val bounds = trimmed.split("-")
            require(bounds.size == 2) {
                "Invalid syntax, expected HH:mm-HH:mm but got: $expression"
            }

            val fromTime = bounds[0].parseOrThrow(expression)
            val toTime = bounds[1].parseOrThrow(expression)

            return if (fromTime <= toTime) {
                TimeBounds(from = fromTime, to = toTime)
            } else {
                TimeBounds(from = toTime, to = fromTime)
            }
        }

        /**
         * Parses a time string into a LocalTime, throwing a descriptive exception on failure.
         *
         * This extension function wraps [LocalTime.parse] to provide better error messages
         * that include the full interval expression context.
         *
         * @param expression The full interval expression (for error reporting)
         * @return The parsed LocalTime
         * @throws IllegalArgumentException if the time string cannot be parsed
         */
        private fun String.parseOrThrow(expression: String): LocalTime = runCatching {
            LocalTime.parse(trim())
        }.getOrElse { cause ->
            throw IllegalArgumentException(
                "Cannot parse time '${trim()}' in expression: $expression",
                cause,
            )
        }
    }
}
