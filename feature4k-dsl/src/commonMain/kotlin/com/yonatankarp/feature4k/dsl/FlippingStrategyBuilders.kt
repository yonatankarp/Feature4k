package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.strategy.AllowListStrategy
import com.yonatankarp.feature4k.strategy.AlwaysOffStrategy
import com.yonatankarp.feature4k.strategy.AlwaysOnStrategy
import com.yonatankarp.feature4k.strategy.ClientFilterStrategy
import com.yonatankarp.feature4k.strategy.ContextualStrategy
import com.yonatankarp.feature4k.strategy.DarkLaunchStrategy
import com.yonatankarp.feature4k.strategy.DenyListStrategy
import com.yonatankarp.feature4k.strategy.ExpressionFlipStrategy
import com.yonatankarp.feature4k.strategy.FlippingStrategy
import com.yonatankarp.feature4k.strategy.GrantedAuthorityStrategy
import com.yonatankarp.feature4k.strategy.HourInterval
import com.yonatankarp.feature4k.strategy.OfficeHourStrategy
import com.yonatankarp.feature4k.strategy.PonderationStrategy
import com.yonatankarp.feature4k.strategy.RegionFlippingStrategy
import com.yonatankarp.feature4k.strategy.ReleaseDateFlipStrategy
import com.yonatankarp.feature4k.strategy.ServerFilterStrategy
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Creates a flipping strategy that always enables the feature.
 *
 * @return The flipping strategy that always evaluates to enabled.
 */
fun alwaysOn(): FlippingStrategy = AlwaysOnStrategy

/**
 * Provides a flipping strategy that always keeps a feature turned off.
 *
 * @return The `FlippingStrategy` that always evaluates to off.
 */
fun alwaysOff(): FlippingStrategy = AlwaysOffStrategy

/**
 * Creates a flipping strategy that enables the feature only for the specified users.
 *
 * @param users Identifiers of users who should be allowed to use the feature.
 * @return A FlippingStrategy that enables the feature for the given users and disables it for others.
 */
fun allowList(vararg users: String): FlippingStrategy = AllowListStrategy(users.toSet())

/**
 * Creates a flipping strategy that enables the feature only for the specified users.
 *
 * @param users The collection of user identifiers that are allowed to use the feature.
 * @return A FlippingStrategy that evaluates to enabled for the provided users and disabled for others.
 */
fun allowList(users: Collection<String>): FlippingStrategy = AllowListStrategy(users.toSet())

/**
 * Creates a flipping strategy that denies feature access to specific users.
 *
 * @param users The user identifiers that will be denied access.
 * @return A FlippingStrategy that denies feature access for the specified users.
 */
fun denyList(vararg users: String): FlippingStrategy = DenyListStrategy(users.toSet())

/**
 * Creates a flipping strategy that denies the feature for the specified users.
 *
 * @param users Collection of user identifiers that should be denied access.
 * @return A `FlippingStrategy` that is disabled for any user in `users` and enabled for others.
 */
fun denyList(users: Collection<String>): FlippingStrategy = DenyListStrategy(users.toSet())

/**
 * Creates a ponderation-based strategy that enables the feature for a fraction of evaluations.
 *
 * @param weight The fraction of evaluations for which the feature should be enabled (for example, `0.25` enables ~25%).
 * @return A `FlippingStrategy` configured to enable the feature for the specified fraction of evaluations.
 */
fun percentage(weight: Double): FlippingStrategy = PonderationStrategy(weight)

/**
 * Enables a feature for a portion of traffic to perform a dark launch.
 *
 * @param percentage Percentage of traffic to enable the feature for, expressed as a value between 0 and 100 inclusive.
 * @return A FlippingStrategy that enables the feature for approximately `percentage` percent of requests.
 */
fun darkLaunch(percentage: Double): FlippingStrategy = DarkLaunchStrategy(percentage)

/**
 * Creates a flipping strategy that enables a feature after the given instant.
 *
 * @param date The instant after which the feature is considered enabled.
 * @return A `FlippingStrategy` that is enabled when the current time is after `date`, `false` otherwise.
 */
fun releaseAfter(date: Instant): FlippingStrategy = ReleaseDateFlipStrategy(date)

@Feature4KDsl
class OfficeHoursBuilder {
    private val weeklySchedule = mutableMapOf<DayOfWeek, List<HourInterval>>()
    private val publicHolidays = mutableSetOf<LocalDate>()
    private val specialOpenings = mutableMapOf<LocalDate, List<HourInterval>>()
    var timezone: String = "UTC"

    /**
     * Set the same opening hour intervals for Monday through Friday.
     *
     * @param intervals Strings that will be parsed into `HourInterval` instances and applied to each weekday.
     */
    fun weekdays(vararg intervals: String) {
        val parsedIntervals = intervals.map { HourInterval(it) }
        DayOfWeek.entries.filter { it != DayOfWeek.SATURDAY && it != DayOfWeek.SUNDAY }.forEach { day ->
            weeklySchedule[day] = parsedIntervals
        }
    }

    /**
     * Sets the same list of opening hour intervals for Saturday and Sunday.
     *
     * @param intervals One or more interval strings in the format accepted by `HourInterval` (e.g. "09:00-17:00").
     */
    fun weekend(vararg intervals: String) {
        val parsedIntervals = intervals.map { HourInterval(it) }
        weeklySchedule[DayOfWeek.SATURDAY] = parsedIntervals
        weeklySchedule[DayOfWeek.SUNDAY] = parsedIntervals
    }

    /**
     * Sets the schedule for a specific day of the week using the given hour interval strings.
     *
     * Each interval string is parsed into an HourInterval and becomes the day's list of open intervals.
     *
     * @param dayOfWeek The day of the week to configure.
     * @param intervals One or more interval strings that will be parsed into `HourInterval` objects;
     *     each string must conform to the format expected by `HourInterval`.
     */
    fun day(dayOfWeek: DayOfWeek, vararg intervals: String) {
        weeklySchedule[dayOfWeek] = intervals.map { HourInterval(it) }
    }

    /**
     * Marks the given date as a public holiday for the office-hours configuration.
     *
     * Dates added via this function will be treated as public holidays when the
     * builder constructs the resulting OfficeHourStrategy.
     *
     * @param date The date to mark as a public holiday.
     */
    fun holiday(date: LocalDate) {
        publicHolidays.add(date)
    }

    /**
     * Adds a special opening schedule for the given date.
     *
     * Associates the specified date with the provided hour intervals (each interval string is parsed into an `HourInterval`).
     * Any existing special openings for the same date are replaced.
     *
     * @param date The date to set the special opening for.
     * @param intervals One or more interval strings to parse into `HourInterval` entries for that date.
     */
    fun specialOpening(date: LocalDate, vararg intervals: String) {
        specialOpenings[date] = intervals.map { HourInterval(it) }
    }

    /**
     * Constructs an OfficeHourStrategy from the builder's configured schedules, holidays, special openings, and timezone.
     *
     * @return An OfficeHourStrategy containing the builder's weekly schedule, public holidays, special openings, and timezone.
     */
    internal fun build(): OfficeHourStrategy = OfficeHourStrategy(
        weeklySchedule = weeklySchedule.toMap(),
        publicHolidays = publicHolidays.toSet(),
        specialOpenings = specialOpenings.toMap(),
        timezone = timezone,
    )
}

/**
 * Creates an office-hours-based FlippingStrategy configured by the provided DSL block.
 *
 * @param block A configuration lambda applied to a new OfficeHoursBuilder to define weekly schedules, public holidays,
 * special openings, and timezone.
 * @return A FlippingStrategy that uses the configured office hours to determine feature availability.
 */
fun officeHours(block: OfficeHoursBuilder.() -> Unit): FlippingStrategy {
    val builder = OfficeHoursBuilder()
    builder.block()
    return builder.build()
}

/**
 * Creates a flipping strategy that matches requests by client identifier.
 *
 * @param clients The client identifiers to match.
 * @return A `FlippingStrategy` that enables the feature for requests originating from any of the given clients.
 */
fun clientFilter(vararg clients: String): FlippingStrategy = ClientFilterStrategy(clients.toSet())

/**
 * Creates a flipping strategy that restricts feature activation to a set of client identifiers.
 *
 * @param clients The client identifiers that should be allowed by the strategy.
 * @return A FlippingStrategy that enables the feature only for the specified client identifiers.
 */
fun clientFilter(clients: Collection<String>): FlippingStrategy = ClientFilterStrategy(clients.toSet())

/**
 * Creates a flipping strategy that restricts feature access to the provided server identifiers.
 *
 * @param servers Identifiers of servers for which the feature should be enabled.
 * @return A `FlippingStrategy` that allows the feature only when the request's server identifier is contained in `servers`.
 */
fun serverFilter(vararg servers: String): FlippingStrategy = ServerFilterStrategy(servers.toSet())

/**
 * Creates a server-based filter that enables the feature for requests originating from the provided servers.
 *
 * @param servers Collection of server identifiers to match.
 * @return A FlippingStrategy that matches when the request's server identifier is one of the provided servers.
 */
fun serverFilter(servers: Collection<String>): FlippingStrategy = ServerFilterStrategy(servers.toSet())

/**
 * Creates a region-based flipping strategy.
 *
 * @param regions Region identifiers that the strategy will match.
 * @return A `FlippingStrategy` that enables the feature only for the specified regions.
 */
fun regionFilter(vararg regions: String): FlippingStrategy = RegionFlippingStrategy(regions.toSet())

/**
 * Creates a flipping strategy limited to the given regions.
 *
 * @param regions Collection of region identifiers to match.
 * @return A FlippingStrategy that enables the feature only for requests originating from the specified regions.
 */
fun regionFilter(regions: Collection<String>): FlippingStrategy = RegionFlippingStrategy(regions.toSet())

/**
 * Create a flipping strategy that evaluates a boolean expression to decide feature enablement.
 *
 * @param expression The expression to evaluate; when it evaluates to `true` the feature is enabled.
 * @return A strategy that enables the feature when the provided expression evaluates to `true`.
 */
fun expression(expression: String): FlippingStrategy = ExpressionFlipStrategy(expression)

/**
 * Creates a strategy that enables the feature when the current principal has any of the specified authorities.
 *
 * @param authorities The authority names to check; the feature is enabled if the principal has at least one of these.
 * @return A `FlippingStrategy` that checks granted authorities.
 */
fun grantedAuthority(vararg authorities: String): FlippingStrategy = GrantedAuthorityStrategy(authorities.toSet())

/**
 * Creates a flipping strategy that enables the feature when the caller holds one of the specified authorities.
 *
 * @param authorities Collection of authority names to check.
 * @return A `FlippingStrategy` that enables the feature when the caller has at least one of the specified authorities.
 */
fun grantedAuthority(authorities: Collection<String>): FlippingStrategy = GrantedAuthorityStrategy(authorities.toSet())

/**
 * Creates a contextual strategy that combines multiple strategies using the specified combine rule.
 *
 * @param combineWith The rule that determines how individual strategies are combined (e.g., ALL, ANY).
 * @param strategies The strategies to combine.
 * @return A FlippingStrategy that evaluates the provided strategies according to `combineWith`.
 */
fun contextual(combineWith: ContextualStrategy.CombineWith, vararg strategies: FlippingStrategy): FlippingStrategy = ContextualStrategy(combineWith, strategies.toList())

/**
 * Creates a contextual flipping strategy that combines multiple strategies using the specified combine rule.
 *
 * @param combineWith Rule that determines how the provided strategies are combined.
 * @param strategies List of strategies to combine.
 * @return A `FlippingStrategy` that evaluates the given strategies according to the combine rule.
 */
fun contextual(combineWith: ContextualStrategy.CombineWith, strategies: List<FlippingStrategy>): FlippingStrategy = ContextualStrategy(combineWith, strategies)
