package com.yonatankarp.feature4k.core

/**
 * Test fixtures for Feature objects.
 * These can be reused across different test modules.
 */
object FeatureFixtures {

    fun basicFeature() = Feature(uid = "feature1")

    fun enabledFeature() = Feature(uid = "feature1", enabled = true)

    fun disabledFeature() = Feature(uid = "feature1", enabled = false)

    fun featureWithPermissions() = Feature(
        uid = "feature1",
        permissions = setOf("ADMIN")
    )

    fun featureWithGroup() = Feature(
        uid = "feature1",
        group = "testGroup"
    )

    fun fullFeature() = Feature(
        uid = "feature1",
        enabled = true,
        description = "Test feature",
        group = "testGroup",
        permissions = setOf("ADMIN", "USER")
    )
}
