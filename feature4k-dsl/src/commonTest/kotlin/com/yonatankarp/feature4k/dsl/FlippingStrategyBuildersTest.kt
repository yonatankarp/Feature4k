package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.strategy.AllowListStrategy
import com.yonatankarp.feature4k.strategy.AlwaysOffStrategy
import com.yonatankarp.feature4k.strategy.AlwaysOnStrategy
import com.yonatankarp.feature4k.strategy.ClientFilterStrategy
import com.yonatankarp.feature4k.strategy.ContextualStrategy
import com.yonatankarp.feature4k.strategy.DarkLaunchStrategy
import com.yonatankarp.feature4k.strategy.DenyListStrategy
import com.yonatankarp.feature4k.strategy.ExpressionFlipStrategy
import com.yonatankarp.feature4k.strategy.GrantedAuthorityStrategy
import com.yonatankarp.feature4k.strategy.OfficeHourStrategy
import com.yonatankarp.feature4k.strategy.PonderationStrategy
import com.yonatankarp.feature4k.strategy.RegionFlippingStrategy
import com.yonatankarp.feature4k.strategy.ReleaseDateFlipStrategy
import com.yonatankarp.feature4k.strategy.ServerFilterStrategy
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Comprehensive tests for FlippingStrategy DSL builders.
 *
 * Tests cover:
 * - Always on/off strategies
 * - User-based strategies (allowlist, denylist)
 * - Percentage-based strategies
 * - Time-based strategies
 * - Filter strategies
 * - Advanced strategies (expression, authority, contextual)
 *
 * @author Yonatan Karp-Rudin
 */
class FlippingStrategyBuildersTest {

    @Test
    fun `should create always on strategy`() {
        // Given / When
        val strategy = alwaysOn()

        // Then
        assertTrue(strategy is AlwaysOnStrategy)
    }

    @Test
    fun `should create always off strategy`() {
        // Given / When
        val strategy = alwaysOff()

        // Then
        assertTrue(strategy is AlwaysOffStrategy)
    }

    @Test
    fun `should create allowlist strategy with varargs`() {
        // Given / When
        val strategy = allowList("alice", "bob", "charlie")

        // Then
        assertTrue(strategy is AllowListStrategy)
        assertEquals(setOf("alice", "bob", "charlie"), strategy.allowedUsers)
    }

    @Test
    fun `should create allowlist strategy from collection`() {
        // Given
        val users = listOf("alice", "bob")

        // When
        val strategy = allowList(users)

        // Then
        assertTrue(strategy is AllowListStrategy)
        assertEquals(setOf("alice", "bob"), strategy.allowedUsers)
    }

    @Test
    fun `should create denylist strategy with varargs`() {
        // Given / When
        val strategy = denyList("spammer1", "abuser2")

        // Then
        assertTrue(strategy is DenyListStrategy)
        assertEquals(setOf("spammer1", "abuser2"), strategy.deniedUsers)
    }

    @Test
    fun `should create denylist strategy from collection`() {
        // Given
        val bannedUsers = setOf("spammer1", "abuser2")

        // When
        val strategy = denyList(bannedUsers)

        // Then
        assertTrue(strategy is DenyListStrategy)
        assertEquals(setOf("spammer1", "abuser2"), strategy.deniedUsers)
    }

    @Test
    fun `should create percentage strategy`() {
        // Given / When
        val strategy = percentage(0.25)

        // Then
        assertTrue(strategy is PonderationStrategy)
        assertEquals(0.25, strategy.weight)
    }

    @Test
    fun `should create dark launch strategy`() {
        // Given / When
        val strategy = darkLaunch(0.1)

        // Then
        assertTrue(strategy is DarkLaunchStrategy)
        assertEquals(0.1, strategy.weight)
    }

    @Test
    fun `should create release after strategy`() {
        // Given
        val releaseDate = Instant.parse("2024-12-25T00:00:00Z")

        // When
        val strategy = releaseAfter(releaseDate)

        // Then
        assertTrue(strategy is ReleaseDateFlipStrategy)
        assertEquals(releaseDate, strategy.releaseDate)
    }

    @Test
    fun `should create office hours strategy with weekdays`() {
        // Given / When
        val strategy = officeHours {
            weekdays("09:00-17:00")
            timezone = "America/New_York"
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertEquals("America/New_York", strategy.timezone)
        assertTrue(strategy.weeklySchedule.containsKey(DayOfWeek.MONDAY))
        assertTrue(strategy.weeklySchedule.containsKey(DayOfWeek.FRIDAY))
    }

    @Test
    fun `should create office hours strategy with weekend`() {
        // Given / When
        val strategy = officeHours {
            weekend("10:00-14:00")
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertTrue(strategy.weeklySchedule.containsKey(DayOfWeek.SATURDAY))
        assertTrue(strategy.weeklySchedule.containsKey(DayOfWeek.SUNDAY))
    }

    @Test
    fun `should create office hours strategy with specific day`() {
        // Given / When
        val strategy = officeHours {
            day(DayOfWeek.MONDAY, "08:00-12:00", "13:00-17:00")
            day(DayOfWeek.FRIDAY, "09:00-15:00")
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertTrue(strategy.weeklySchedule.containsKey(DayOfWeek.MONDAY))
        assertTrue(strategy.weeklySchedule.containsKey(DayOfWeek.FRIDAY))
        assertEquals(2, strategy.weeklySchedule[DayOfWeek.MONDAY]?.size)
        assertEquals(1, strategy.weeklySchedule[DayOfWeek.FRIDAY]?.size)
    }

    @Test
    fun `should create office hours strategy with holidays and special openings`() {
        // Given / When
        val strategy = officeHours {
            weekdays("09:00-17:00")
            holiday(LocalDate(2024, 12, 25))
            specialOpening(LocalDate(2024, 12, 24), "09:00-13:00")
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertTrue(strategy.publicHolidays.contains(LocalDate(2024, 12, 25)))
        assertTrue(strategy.specialOpenings.containsKey(LocalDate(2024, 12, 24)))
    }

    @Test
    fun `should create client filter strategy with varargs`() {
        // Given / When
        val strategy = clientFilter("web-app", "mobile-app")

        // Then
        assertTrue(strategy is ClientFilterStrategy)
        assertEquals(setOf("web-app", "mobile-app"), strategy.grantedClients)
    }

    @Test
    fun `should create client filter strategy from collection`() {
        // Given
        val clients = listOf("web-app", "mobile-app")

        // When
        val strategy = clientFilter(clients)

        // Then
        assertTrue(strategy is ClientFilterStrategy)
        assertEquals(setOf("web-app", "mobile-app"), strategy.grantedClients)
    }

    @Test
    fun `should create server filter strategy with varargs`() {
        // Given / When
        val strategy = serverFilter("server1", "server2")

        // Then
        assertTrue(strategy is ServerFilterStrategy)
        assertEquals(setOf("server1", "server2"), strategy.grantedServers)
    }

    @Test
    fun `should create server filter strategy from collection`() {
        // Given
        val servers = setOf("server1", "server2")

        // When
        val strategy = serverFilter(servers)

        // Then
        assertTrue(strategy is ServerFilterStrategy)
        assertEquals(setOf("server1", "server2"), strategy.grantedServers)
    }

    @Test
    fun `should create region filter strategy with varargs`() {
        // Given / When
        val strategy = regionFilter("US", "EU")

        // Then
        assertTrue(strategy is RegionFlippingStrategy)
        assertEquals(setOf("US", "EU"), strategy.grantedRegions)
    }

    @Test
    fun `should create region filter strategy from collection`() {
        // Given
        val regions = listOf("US", "EU", "APAC")

        // When
        val strategy = regionFilter(regions)

        // Then
        assertTrue(strategy is RegionFlippingStrategy)
        assertEquals(setOf("US", "EU", "APAC"), strategy.grantedRegions)
    }

    @Test
    fun `should create expression strategy`() {
        // Given / When
        val strategy = expression("feature1 AND feature2")

        // Then
        assertTrue(strategy is ExpressionFlipStrategy)
        assertEquals("feature1 AND feature2", strategy.expression)
    }

    @Test
    fun `should create granted authority strategy with varargs`() {
        // Given / When
        val strategy = grantedAuthority("ROLE_ADMIN", "ROLE_SUPERUSER")

        // Then
        assertTrue(strategy is GrantedAuthorityStrategy)
        assertEquals(setOf("ROLE_ADMIN", "ROLE_SUPERUSER"), strategy.requiredAuthorities)
    }

    @Test
    fun `should create granted authority strategy from collection`() {
        // Given
        val roles = listOf("ROLE_ADMIN", "ROLE_PREMIUM")

        // When
        val strategy = grantedAuthority(roles)

        // Then
        assertTrue(strategy is GrantedAuthorityStrategy)
        assertEquals(setOf("ROLE_ADMIN", "ROLE_PREMIUM"), strategy.requiredAuthorities)
    }

    @Test
    fun `should create contextual strategy with AND combination`() {
        // Given / When
        val strategy = contextual(
            ContextualStrategy.CombineWith.AND,
            allowList("alice"),
            percentage(0.5),
        )

        // Then
        assertTrue(strategy is ContextualStrategy)
        assertEquals(ContextualStrategy.CombineWith.AND, strategy.combineWith)
        assertEquals(2, strategy.strategies.size)
    }

    @Test
    fun `should create contextual strategy from list`() {
        // Given
        val strategies = listOf(allowList("alice"), percentage(0.5))

        // When
        val strategy = contextual(ContextualStrategy.CombineWith.OR, strategies)

        // Then
        assertTrue(strategy is ContextualStrategy)
        assertEquals(ContextualStrategy.CombineWith.OR, strategy.combineWith)
        assertEquals(2, strategy.strategies.size)
    }

    @Test
    fun `should reject invalid timezone in office hours`() {
        // Given / When / Then
        val exception = assertFailsWith<IllegalArgumentException> {
            officeHours {
                timezone = "Invalid/Timezone"
            }
        }
        assertTrue(exception.message!!.contains("Invalid timezone ID: 'Invalid/Timezone'"))
    }

    @Test
    fun `should accept valid timezone in office hours`() {
        // Given / When
        val strategy = officeHours {
            timezone = "Europe/London"
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertEquals("Europe/London", strategy.timezone)
    }

    @Test
    fun `should reject invalid hour interval in weekdays`() {
        // Given / When / Then
        val exception = assertFailsWith<IllegalArgumentException> {
            officeHours {
                weekdays("09:00-17:00", "invalid-interval")
            }
        }
        assertTrue(exception.message!!.contains("Invalid hour interval 'invalid-interval' in weekdays()"))
    }

    @Test
    fun `should reject malformed hour interval in weekdays`() {
        // Given / When / Then
        val exception = assertFailsWith<IllegalArgumentException> {
            officeHours {
                weekdays("25:00-17:00")
            }
        }
        assertTrue(exception.message!!.contains("Invalid hour interval '25:00-17:00' in weekdays()"))
    }

    @Test
    fun `should reject invalid hour interval in weekend`() {
        // Given / When / Then
        val exception = assertFailsWith<IllegalArgumentException> {
            officeHours {
                weekend("10:00-14:00", "not-a-time-range")
            }
        }
        assertTrue(exception.message!!.contains("Invalid hour interval 'not-a-time-range' in weekend()"))
    }

    @Test
    fun `should reject invalid hour interval in day`() {
        // Given / When / Then
        val exception = assertFailsWith<IllegalArgumentException> {
            officeHours {
                day(DayOfWeek.MONDAY, "08:00-12:00", "bad-interval")
            }
        }
        assertTrue(exception.message!!.contains("Invalid hour interval 'bad-interval' in day(MONDAY)"))
    }

    @Test
    fun `should reject invalid hour interval in specialOpening`() {
        // Given / When / Then
        val exception = assertFailsWith<IllegalArgumentException> {
            officeHours {
                specialOpening(LocalDate(2024, 12, 24), "09:00-13:00", "invalid")
            }
        }
        assertTrue(exception.message!!.contains("Invalid hour interval 'invalid' in specialOpening(2024-12-24)"))
    }

    @Test
    fun `should accept multiple valid hour intervals in weekdays`() {
        // Given / When
        val strategy = officeHours {
            weekdays("09:00-12:00", "13:00-17:00")
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertEquals(2, strategy.weeklySchedule[DayOfWeek.MONDAY]?.size)
    }

    @Test
    fun `should accept multiple valid hour intervals in weekend`() {
        // Given / When
        val strategy = officeHours {
            weekend("10:00-12:00", "14:00-16:00")
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertEquals(2, strategy.weeklySchedule[DayOfWeek.SATURDAY]?.size)
        assertEquals(2, strategy.weeklySchedule[DayOfWeek.SUNDAY]?.size)
    }

    @Test
    fun `should accept multiple valid hour intervals in day`() {
        // Given / When
        val strategy = officeHours {
            day(DayOfWeek.TUESDAY, "08:00-10:00", "11:00-13:00", "14:00-16:00")
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertEquals(3, strategy.weeklySchedule[DayOfWeek.TUESDAY]?.size)
    }

    @Test
    fun `should accept multiple valid hour intervals in specialOpening`() {
        // Given / When
        val strategy = officeHours {
            specialOpening(LocalDate(2024, 12, 24), "09:00-11:00", "13:00-15:00")
        }

        // Then
        assertTrue(strategy is OfficeHourStrategy)
        assertEquals(2, strategy.specialOpenings[LocalDate(2024, 12, 24)]?.size)
    }
}
