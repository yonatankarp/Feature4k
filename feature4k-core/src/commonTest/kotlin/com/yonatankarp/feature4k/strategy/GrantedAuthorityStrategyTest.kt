package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.IdentifierFixtures.ADMIN
import com.yonatankarp.feature4k.core.IdentifierFixtures.ALICE
import com.yonatankarp.feature4k.core.IdentifierFixtures.BOB
import com.yonatankarp.feature4k.core.IdentifierFixtures.CHARLIE
import com.yonatankarp.feature4k.core.IdentifierFixtures.USER_REGULAR
import com.yonatankarp.feature4k.core.IdentifierFixtures.USER_SUPERUSER
import com.yonatankarp.feature4k.store.InMemoryFeatureStore
import com.yonatankarp.feature4k.strategy.FeatureEvaluationContextFixture.featureEvaluationContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.contextWithAuthorities
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.emptyExecutionContext
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.executionContextWithUser
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ADMIN_AND_USER_AUTHORITIES
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ADMIN_USER_MODERATOR_STRING
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ADMIN_USER_MODERATOR_WITH_SPACES_STRING
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ADMIN_WITH_EMPTY_ENTRIES_STRING
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.EMPTY_AUTHORITIES_STRING
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.LOWERCASE_ADMIN_STRING
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ONLY_ADMIN_STRING
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ONLY_USER_STRING
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ROLE_ADMIN
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ROLE_MODERATOR
import com.yonatankarp.feature4k.strategy.GrantedAuthorityFixture.ROLE_USER
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for GrantedAuthorityStrategy.
 *
 * @author Yonatan Karp-Rudin
 */
class GrantedAuthorityStrategyTest {
    @Test
    fun `should return true when user has all required authorities`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = ADMIN_AND_USER_AUTHORITIES)
        val execContext = contextWithAuthorities(authorities = ADMIN_USER_MODERATOR_STRING, user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when user has all required authorities")
    }

    @Test
    fun `should return false when user is missing one required authority`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = ADMIN_AND_USER_AUTHORITIES)
        val execContext = contextWithAuthorities(authorities = ONLY_USER_STRING, user = BOB)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when user is missing required authority")
    }

    @Test
    fun `should return false when user has no authorities`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN))
        val execContext = executionContextWithUser(user = CHARLIE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when authorities param is missing")
    }

    @Test
    fun `should return true when no authorities are required`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = emptySet())
        val execContext = executionContextWithUser(user = "anyone")
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should return true when no authorities are required")
    }

    @Test
    fun `should handle authorities with whitespace correctly`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = ADMIN_AND_USER_AUTHORITIES)
        val execContext = contextWithAuthorities(authorities = ADMIN_USER_MODERATOR_WITH_SPACES_STRING, user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should trim whitespace from authority names")
    }

    @Test
    fun `should handle single authority requirement`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN))
        val adminExecContext = contextWithAuthorities(authorities = ONLY_ADMIN_STRING, user = ADMIN)
        val userExecContext = contextWithAuthorities(authorities = ONLY_USER_STRING, user = USER_REGULAR)
        val store = InMemoryFeatureStore()
        val adminEvalContext = featureEvaluationContext(context = adminExecContext, store = store)
        val userEvalContext = featureEvaluationContext(context = userExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(adminEvalContext), "Should allow admin")
        assertFalse(strategy.evaluate(userEvalContext), "Should not allow user")
    }

    @Test
    fun `should be case-sensitive for authority matching`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN))
        val upperCaseExecContext = contextWithAuthorities(authorities = ONLY_ADMIN_STRING, user = ALICE)
        val lowerCaseExecContext = contextWithAuthorities(authorities = LOWERCASE_ADMIN_STRING, user = BOB)
        val store = InMemoryFeatureStore()
        val upperCaseEvalContext = featureEvaluationContext(context = upperCaseExecContext, store = store)
        val lowerCaseEvalContext = featureEvaluationContext(context = lowerCaseExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(upperCaseEvalContext), "Should match exact case 'ROLE_ADMIN'")
        assertFalse(strategy.evaluate(lowerCaseEvalContext), "Should not match different case 'role_admin'")
    }

    @Test
    fun `should handle many required authorities`() = runTest {
        // Given
        val manyAuthorities = (1..10).map { "PERMISSION_$it" }.toSet()
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = manyAuthorities)
        val hasAllExecContext = contextWithAuthorities(
            authorities = manyAuthorities.joinToString(","),
            user = USER_SUPERUSER,
        )
        val missingOneExecContext = contextWithAuthorities(
            authorities = manyAuthorities.drop(1).joinToString(","),
            user = USER_REGULAR,
        )
        val store = InMemoryFeatureStore()
        val hasAllEvalContext = featureEvaluationContext(context = hasAllExecContext, store = store)
        val missingOneEvalContext = featureEvaluationContext(context = missingOneExecContext, store = store)

        // When & Then
        assertTrue(strategy.evaluate(hasAllEvalContext), "Should allow user with all authorities")
        assertFalse(strategy.evaluate(missingOneEvalContext), "Should not allow user missing one authority")
    }

    @Test
    fun `should ignore empty authority entries`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN))
        val execContext = contextWithAuthorities(authorities = ADMIN_WITH_EMPTY_ENTRIES_STRING, user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Strategy should ignore empty entries after splitting")
    }

    @Test
    fun `should return false when authorities string is empty`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN))
        val execContext = contextWithAuthorities(authorities = EMPTY_AUTHORITIES_STRING, user = ALICE)
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when authorities string is empty")
    }

    @Test
    fun `should return false when context is empty`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN))
        val execContext = emptyExecutionContext()
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertFalse(result, "Strategy should return false when context is empty")
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN, ROLE_MODERATOR))
        val json = Json { prettyPrint = true }

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)

        // Then
        assertTrue(serialized.contains("\"type\": \"granted_authority\""), "Should contain type")
        assertTrue(serialized.contains("\"requiredAuthorities\""), "Should contain requiredAuthorities field")
        assertTrue(serialized.contains("\"$ROLE_ADMIN\""), "Should contain ROLE_ADMIN")
        assertTrue(serialized.contains("\"$ROLE_MODERATOR\""), "Should contain ROLE_MODERATOR")
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "type": "granted_authority",
                "requiredAuthorities": ["$ROLE_ADMIN", "$ROLE_MODERATOR"]
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }

        // When
        val strategy = json.decodeFromString<FlippingStrategy>(jsonString)

        // Then
        assertTrue(strategy is GrantedAuthorityStrategy, "Should deserialize to GrantedAuthorityStrategy")
        assertEquals(setOf(ROLE_ADMIN, ROLE_MODERATOR), strategy.requiredAuthorities)
    }

    @Test
    fun `should serialize and deserialize empty authority set`() {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = emptySet())
        val json = Json

        // When
        val serialized = json.encodeToString<FlippingStrategy>(strategy)
        val deserialized = json.decodeFromString<FlippingStrategy>(serialized)

        // Then
        assertTrue(deserialized is GrantedAuthorityStrategy)
        assertTrue(deserialized.requiredAuthorities.isEmpty())
    }

    @Test
    fun `should use default empty set when not specified`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy()
        val execContext = executionContextWithUser(user = "anyone")
        val evalContext = featureEvaluationContext(context = execContext)

        // When & Then
        assertTrue(strategy.requiredAuthorities.isEmpty(), "Default requiredAuthorities should be empty set")
        assertTrue(strategy.evaluate(evalContext), "Should return true when no authorities required")
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val strategy1 = GrantedAuthorityStrategy(requiredAuthorities = ADMIN_AND_USER_AUTHORITIES)
        val strategy2 = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN, ROLE_USER))
        val strategy3 = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN))

        // When & Then
        assertEquals(strategy1, strategy2, "Strategies with same authorities should be equal")
        assertNotEquals(strategy1, strategy3, "Strategies with different authorities should not be equal")
    }

    @Test
    fun `should ignore other context fields`() = runTest {
        // Given
        val strategy = GrantedAuthorityStrategy(requiredAuthorities = setOf(ROLE_ADMIN))
        val execContext = executionContextWithUser(
            user = ALICE,
            client = "mobile-app",
            server = "prod-server",
            customParams = mapOf(
                "authorities" to ONLY_ADMIN_STRING,
                "region" to "us-east",
                "other" to "value",
            ),
        )
        val evalContext = featureEvaluationContext(context = execContext)

        // When
        val result = strategy.evaluate(evalContext)

        // Then
        assertTrue(result, "Should only care about authorities param, ignoring other fields")
    }
}
