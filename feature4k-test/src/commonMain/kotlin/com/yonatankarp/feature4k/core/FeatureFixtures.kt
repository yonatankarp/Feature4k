package com.yonatankarp.feature4k.core

import com.yonatankarp.feature4k.property.PropertyInt
import com.yonatankarp.feature4k.property.PropertyString
import com.yonatankarp.feature4k.strategy.AlwaysOffStrategy
import com.yonatankarp.feature4k.strategy.AlwaysOnStrategy

/**
 * Test fixtures for Feature objects.
 * These can be reused across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object FeatureFixtures {
    fun basicFeature() = Feature(uid = "feature1")

    fun enabledFeature() = Feature(uid = "feature1", enabled = true)

    /**
     * Creates a disabled Feature test fixture with a fixed UID.
     *
     * @return a `Feature` with uid "feature1" and enabled = false.
     */
    fun disabledFeature() = Feature(uid = "feature1", enabled = false)

    /**
     * Creates a Feature with uid "feature1" and permissions containing "ADMIN".
     *
     * @return A Feature whose uid is "feature1" and whose permissions set contains "ADMIN".
     */
    fun featureWithPermissions() = Feature(
        uid = "feature1",
        permissions = setOf("ADMIN"),
    )

    /**
     * Creates a Feature configured with uid "feature1" and group "testGroup".
     *
     * @return A Feature instance with `uid = "feature1"` and `group = "testGroup"`.
     */
    fun featureWithGroup() = Feature(
        uid = "feature1",
        group = "testGroup",
    )

    /**
     * Creates a Feature configured for tests with uid "feature1".
     *
     * @return A Feature with uid "feature1", enabled = true, description = "Test feature", group = "testGroup", and permissions set containing "ADMIN" and "USER".
     */
    fun fullFeature() = Feature(
        uid = "feature1",
        enabled = true,
        description = "Test feature",
        group = "testGroup",
        permissions = setOf("ADMIN", "USER"),
    )

    /**
     * Creates a Feature with uid "feature1" and custom properties.
     *
     * @return A Feature with uid "feature1" and custom properties including "maxRetries" (Int) and "message" (String).
     */
    fun featureWithCustomProperties() = Feature(
        uid = "feature1",
        customProperties = mapOf(
            "maxRetries" to PropertyInt(name = "maxRetries", value = 3),
            "message" to PropertyString(name = "message", value = "test"),
        ),
    )

    /**
     * Creates a Feature configured with the always-on flipping strategy.
     *
     * @return A Feature with enabled = true and flippingStrategy = AlwaysOnStrategy.
     */
    fun featureWithAlwaysOnStrategy() = Feature(
        uid = "feature1",
        enabled = true,
        flippingStrategy = AlwaysOnStrategy,
    )

    /**
     * Creates a Feature configured with the AlwaysOffStrategy.
     *
     * The feature has uid "feature1" and is enabled.
     *
     * @return A Feature with uid "feature1", enabled = true, and flippingStrategy = AlwaysOffStrategy.
     */
    fun featureWithAlwaysOffStrategy() = Feature(
        uid = "feature1",
        enabled = true,
        flippingStrategy = AlwaysOffStrategy,
    )
}