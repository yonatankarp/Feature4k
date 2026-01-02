package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that enables a feature only when the user possesses all required authorities.
 *
 * This strategy evaluates to `true` when the user's granted authorities (passed via
 * `context.customParams["authorities"]` as a comma-separated string) contain all the
 * authorities specified in [requiredAuthorities]. If no authorities are provided in the
 * context, or if any required authority is missing, the strategy evaluates to `false`.
 *
 * ## Use Cases
 *
 * - **Role-based access**: Enable features only for users with specific roles (e.g., "ADMIN", "MODERATOR")
 * - **Permission-based features**: Grant access based on fine-grained permissions (e.g., "READ", "WRITE", "DELETE")
 * - **Multi-level authorization**: Require multiple authorities for sensitive operations
 * - **Enterprise feature gates**: Control feature access based on organizational roles
 *
 * ## Example
 *
 * ```kotlin
 * val strategy = GrantedAuthorityStrategy(
 *     requiredAuthorities = setOf("ROLE_ADMIN", "ROLE_MODERATOR")
 * )
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "admin-panel",
 *     store = store,
 *     context = FlippingExecutionContext(
 *         user = "alice",
 *         customParams = mapOf("authorities" to "ROLE_ADMIN,ROLE_MODERATOR,ROLE_USER")
 *     )
 * )
 * strategy.evaluate(evalContext) // returns true (has all required authorities)
 *
 * // User missing one authority
 * val bobContext = FeatureEvaluationContext(
 *     featureName = "admin-panel",
 *     store = store,
 *     context = FlippingExecutionContext(
 *         user = "bob",
 *         customParams = mapOf("authorities" to "ROLE_USER")
 *     )
 * )
 * strategy.evaluate(bobContext) // returns false (missing ROLE_ADMIN and ROLE_MODERATOR)
 * ```
 *
 * ## Authority Format
 *
 * User authorities must be provided in the execution context's `customParams` map
 * with the key `"authorities"`. The value should be a comma-separated string of
 * authority identifiers (whitespace around commas is trimmed).
 *
 * Example:
 * ```kotlin
 * customParams = mapOf("authorities" to "ROLE_ADMIN, ROLE_USER, CREATE_POSTS")
 * ```
 *
 * ## Serialization
 *
 * This strategy is serializable and can be persisted alongside feature definitions:
 *
 * ```json
 * {
 *   "type": "granted_authority",
 *   "requiredAuthorities": ["ROLE_ADMIN", "ROLE_MODERATOR"]
 * }
 * ```
 *
 * @property requiredAuthorities The set of authorities that the user must possess to access the feature
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("granted_authority")
data class GrantedAuthorityStrategy(
    val requiredAuthorities: Set<String> = emptySet(),
) : FlippingStrategy {
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        if (requiredAuthorities.isEmpty()) return true

        val authorities = evalContext.context.customParams[AUTHORITIES_PARAM_KEY] ?: return false
        val userAuthorities = parseAuthorities(authorities)

        return requiredAuthorities.all { it in userAuthorities }
    }

    private fun parseAuthorities(authorities: String): Set<String> = authorities
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    companion object {
        /**
         * The custom parameter key used to pass user authorities in the evaluation context.
         */
        const val AUTHORITIES_PARAM_KEY: String = "authorities"
    }
}
