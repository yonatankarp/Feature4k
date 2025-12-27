package com.yonatankarp.feature4k.strategy

/**
 * Common weight constants for percentage-based feature rollout strategies.
 *
 * These constants provide convenient, self-documenting values for use with [PonderationStrategy]
 * and [DarkLaunchStrategy]. Using named constants makes code more readable and reduces errors
 * from typos or incorrect decimal values.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // Canary deployment - start with 1% of users
 * val canary = PonderationStrategy(weight = Weight.ONE_PERCENT)
 *
 * // A/B test - split users 50/50
 * val abTest = PonderationStrategy(weight = Weight.FIFTY_PERCENT)
 *
 * // Full rollout
 * val fullRollout = PonderationStrategy(weight = Weight.FULL)
 * ```
 *
 * ## Progressive Rollout Pattern
 *
 * ```kotlin
 * // Phase 1: Canary to 1%
 * featureStore.update(feature.copy(
 *     flippingStrategy = PonderationStrategy(Weight.ONE_PERCENT)
 * ))
 *
 * // Phase 2: Expand to 10%
 * featureStore.update(feature.copy(
 *     flippingStrategy = PonderationStrategy(Weight.TEN_PERCENT)
 * ))
 *
 * // Phase 3: Half rollout
 * featureStore.update(feature.copy(
 *     flippingStrategy = PonderationStrategy(Weight.FIFTY_PERCENT)
 * ))
 *
 * // Phase 4: Full release
 * featureStore.update(feature.copy(
 *     flippingStrategy = PonderationStrategy(Weight.FULL)
 * ))
 * ```
 *
 * @author Yonatan Karp-Rudin
 * @see PonderationStrategy
 * @see DarkLaunchStrategy
 */
object Weight {
    /** Disable the feature for all users (0%) - equivalent to feature being disabled */
    const val ZERO = 0.0

    /** Enable for 1% of users - typical for initial canary deployments */
    const val ONE_PERCENT = 0.01

    /** Enable for 10% of users - common first-stage rollout percentage */
    const val TEN_PERCENT = 0.10

    /** Enable for 25% of users - quarter rollout */
    const val TWENTY_FIVE_PERCENT = 0.25

    /** Enable for 50% of users - classic A/B testing split */
    const val FIFTY_PERCENT = 0.50

    /** Enable for 75% of users - three-quarter rollout before full release */
    const val SEVENTY_FIVE_PERCENT = 0.75

    /** Enable for all users (100%) - full rollout */
    const val FULL = 1.0
}
