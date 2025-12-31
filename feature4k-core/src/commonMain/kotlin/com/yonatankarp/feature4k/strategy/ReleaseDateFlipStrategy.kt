package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flipping strategy that enables a feature after a specified release date is reached.
 *
 * This strategy evaluates to `true` when the current time is after or equal to the configured
 * [releaseDate]. Before the release date, the strategy always returns `false`. This is useful
 * for scheduling features to automatically become available at a specific time.
 *
 * ## Use Cases
 *
 * - **Scheduled releases**: Automatically enable features at a specific date/time
 * - **Coordinated launches**: Synchronize feature releases across distributed systems
 * - **Time-based access**: Grant access to features based on time constraints
 * - **Planned rollouts**: Schedule feature availability in advance
 *
 * ## Time Handling
 *
 * The strategy uses [kotlinx.datetime.Instant] for precise, timezone-agnostic time comparison.
 * By default, it uses [Clock.System.now] for the current time, but this can be overridden
 * via the execution context for testing purposes using the [OVERRIDE_INSTANT_KEY] parameter.
 *
 * ## Examples
 *
 * ### Basic scheduled release
 * ```kotlin
 * val releaseInstant = Instant.parse("2024-12-25T00:00:00Z")
 * val strategy = ReleaseDateFlipStrategy(releaseDate = releaseInstant)
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "christmas-feature",
 *     store = store,
 *     context = FlippingExecutionContext()
 * )
 *
 * // Before Christmas: returns false
 * // After Christmas: returns true
 * strategy.evaluate(evalContext)
 * ```
 *
 * ### Testing with overridden time
 * ```kotlin
 * val releaseInstant = Instant.parse("2024-12-25T00:00:00Z")
 * val strategy = ReleaseDateFlipStrategy(releaseDate = releaseInstant)
 *
 * // Simulate time after release
 * val testTime = "2024-12-26T00:00:00Z"
 * val testContext = FlippingExecutionContext(
 *     customParams = mapOf(ReleaseDateFlipStrategy.OVERRIDE_INSTANT_KEY to testTime)
 * )
 *
 * val evalContext = FeatureEvaluationContext(
 *     featureName = "test-feature",
 *     store = store,
 *     context = testContext
 * )
 *
 * strategy.evaluate(evalContext) // returns true
 * ```
 *
 * ## Serialization
 *
 * This strategy is serializable and can be persisted alongside feature definitions.
 * The [releaseDate] is serialized as an ISO-8601 string:
 *
 * ```json
 * {
 *   "type": "release-date",
 *   "releaseDate": "2024-12-25T00:00:00Z"
 * }
 * ```
 *
 * @property releaseDate The instant when the feature should become available
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("release-date")
data class ReleaseDateFlipStrategy(
    val releaseDate: Instant,
) : FlippingStrategy {
    /**
     * Evaluates whether the feature should be enabled based on the current time vs release date.
     *
     * The strategy compares the current time (or overridden time from context) against the
     * configured [releaseDate]. Returns `true` if current time is at or after the release date.
     *
     * The current time can be overridden for testing by adding an [OVERRIDE_INSTANT_KEY]
     * parameter to the execution context's custom parameters, with an ISO-8601 instant string
     * as the value.
     *
     * @param evalContext The evaluation context (may contain time override in customParams)
     * @return `true` if current time >= releaseDate, `false` otherwise
     */
    override suspend fun evaluate(evalContext: FeatureEvaluationContext): Boolean {
        val now = evalContext.context[OVERRIDE_INSTANT_KEY]?.let { Instant.parse(it) }
            ?: Clock.System.now()
        return now >= releaseDate
    }

    companion object {
        /**
         * Custom parameter key for overriding the current time in tests.
         *
         * When present in the execution context's custom parameters, the value should be
         * an ISO-8601 instant string (e.g., "2024-12-25T00:00:00Z").
         */
        const val OVERRIDE_INSTANT_KEY = "overrideInstant"
    }
}
