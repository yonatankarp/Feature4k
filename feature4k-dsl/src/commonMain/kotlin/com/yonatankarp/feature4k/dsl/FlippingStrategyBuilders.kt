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

fun alwaysOn(): FlippingStrategy = AlwaysOnStrategy

fun alwaysOff(): FlippingStrategy = AlwaysOffStrategy

fun allowList(vararg users: String): FlippingStrategy = AllowListStrategy(users.toSet())

fun allowList(users: Collection<String>): FlippingStrategy = AllowListStrategy(users.toSet())

fun denyList(vararg users: String): FlippingStrategy = DenyListStrategy(users.toSet())

fun denyList(users: Collection<String>): FlippingStrategy = DenyListStrategy(users.toSet())

fun percentage(weight: Double): FlippingStrategy = PonderationStrategy(weight)

fun darkLaunch(percentage: Double): FlippingStrategy = DarkLaunchStrategy(percentage)

fun releaseAfter(date: Instant): FlippingStrategy = ReleaseDateFlipStrategy(date)

@Feature4KDsl
class OfficeHoursBuilder {
    private val weeklySchedule = mutableMapOf<DayOfWeek, List<HourInterval>>()
    private val publicHolidays = mutableSetOf<LocalDate>()
    private val specialOpenings = mutableMapOf<LocalDate, List<HourInterval>>()
    var timezone: String = "UTC"

    fun weekdays(vararg intervals: String) {
        val parsedIntervals = intervals.map { HourInterval(it) }
        DayOfWeek.entries.filter { it != DayOfWeek.SATURDAY && it != DayOfWeek.SUNDAY }.forEach { day ->
            weeklySchedule[day] = parsedIntervals
        }
    }

    fun weekend(vararg intervals: String) {
        val parsedIntervals = intervals.map { HourInterval(it) }
        weeklySchedule[DayOfWeek.SATURDAY] = parsedIntervals
        weeklySchedule[DayOfWeek.SUNDAY] = parsedIntervals
    }

    fun day(dayOfWeek: DayOfWeek, vararg intervals: String) {
        weeklySchedule[dayOfWeek] = intervals.map { HourInterval(it) }
    }

    fun holiday(date: LocalDate) {
        publicHolidays.add(date)
    }

    fun specialOpening(date: LocalDate, vararg intervals: String) {
        specialOpenings[date] = intervals.map { HourInterval(it) }
    }

    internal fun build(): OfficeHourStrategy = OfficeHourStrategy(
        weeklySchedule = weeklySchedule.toMap(),
        publicHolidays = publicHolidays.toSet(),
        specialOpenings = specialOpenings.toMap(),
        timezone = timezone,
    )
}

fun officeHours(block: OfficeHoursBuilder.() -> Unit): FlippingStrategy {
    val builder = OfficeHoursBuilder()
    builder.block()
    return builder.build()
}

fun clientFilter(vararg clients: String): FlippingStrategy = ClientFilterStrategy(clients.toSet())

fun clientFilter(clients: Collection<String>): FlippingStrategy = ClientFilterStrategy(clients.toSet())

fun serverFilter(vararg servers: String): FlippingStrategy = ServerFilterStrategy(servers.toSet())

fun serverFilter(servers: Collection<String>): FlippingStrategy = ServerFilterStrategy(servers.toSet())

fun regionFilter(vararg regions: String): FlippingStrategy = RegionFlippingStrategy(regions.toSet())

fun regionFilter(regions: Collection<String>): FlippingStrategy = RegionFlippingStrategy(regions.toSet())

fun expression(expression: String): FlippingStrategy = ExpressionFlipStrategy(expression)

fun grantedAuthority(vararg authorities: String): FlippingStrategy = GrantedAuthorityStrategy(authorities.toSet())

fun grantedAuthority(authorities: Collection<String>): FlippingStrategy = GrantedAuthorityStrategy(authorities.toSet())

fun contextual(combineWith: ContextualStrategy.CombineWith, vararg strategies: FlippingStrategy): FlippingStrategy = ContextualStrategy(combineWith, strategies.toList())

fun contextual(combineWith: ContextualStrategy.CombineWith, strategies: List<FlippingStrategy>): FlippingStrategy = ContextualStrategy(combineWith, strategies)
