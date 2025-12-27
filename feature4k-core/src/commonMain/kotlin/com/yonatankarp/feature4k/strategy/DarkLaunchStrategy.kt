package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FlippingExecutionContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A dark launch strategy for safely testing new features with a percentage of production traffic.
 *
 * The Dark Launch pattern is a DevOps technique that deploys new functionality to all servers
 * but activates it only for a subset of requests. This enables teams to measure performance impact
 * and validate behavior with real production data before committing to a full rollout.
 *
 * ## What is Dark Launching?
 *
 * Unlike traditional deployment strategies (blue/green, canary) that deploy new code to a subset
 * of servers, dark launching deploys to **all** servers but activates features for only a percentage
 * of traffic. This provides several advantages:
 *
 * - **Realistic measurement**: New code runs in the full production environment
 * - **Early detection**: Issues are caught with minimal user impact
 * - **Real data validation**: Test with actual production traffic patterns
 * - **Gradual confidence building**: Increase percentage as metrics prove stability
 *
 * ## When to Use DarkLaunchStrategy vs PonderationStrategy
 *
 * Both strategies use identical percentage-based evaluation logic. Choose based on intent:
 *
 * - **Use `DarkLaunchStrategy`** when:
 *   - Testing new features for performance impact
 *   - Validating database migrations or schema changes
 *   - Measuring resource consumption of new algorithms
 *   - Gathering production metrics before deciding on full rollout
 *
 * - **Use `PonderationStrategy`** when:
 *   - Running A/B tests to compare user behavior
 *   - Implementing gradual rollouts for feature releases
 *   - Testing multiple variants of UI/UX changes
 *
 * ## Use Cases
 *
 * - **Performance testing**: Enable resource-intensive features for 10% of users to measure overhead
 * - **Database migration**: Gradually shift users to new data models while monitoring impact
 * - **Algorithm validation**: Compare new recommendation systems against existing ones
 * - **API changes**: Test new endpoints with production traffic before deprecating old ones
 *
 * @property weight The percentage threshold (0.0 to 1.0) for enabling the feature.
 *                  Use [Weight] constants for common values.
 * @throws IllegalArgumentException if weight is not between 0.0 and 1.0 (inclusive)
 * @author Yonatan Karp-Rudin
 * @see PonderationStrategy
 * @see Weight
 */
@Serializable
@SerialName("dark-launch")
data class DarkLaunchStrategy(
    val weight: Double = 0.5,
) : FlippingStrategy {
    @Transient
    private val delegate = PonderationStrategy(weight)

    override fun evaluate(context: FlippingExecutionContext): Boolean = delegate.evaluate(context)
}
