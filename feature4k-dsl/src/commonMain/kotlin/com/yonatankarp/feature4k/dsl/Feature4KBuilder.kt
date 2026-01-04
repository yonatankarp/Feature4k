package com.yonatankarp.feature4k.dsl

import com.yonatankarp.feature4k.core.Feature4K

/**
 * DSL builder for creating [Feature4K] instances with type-safe configuration.
 *
 * ## Usage Examples
 *
 * ### Basic configuration with defaults
 * ```kotlin
 * val feature4k = feature4k {
 *     autoCreate = true
 * }
 * ```
 *
 * ### With custom stores and authorization
 * ```kotlin
 * val feature4k = feature4k {
 *     featureStore = SqlDelightFeatureStore(database)
 *     propertyStore = SqlDelightPropertyStore(database)
 *     authorizationsManager = MyAuthManager()
 *     eventPublisher = MyEventPublisher()
 *     autoCreate = false
 * }
 * ```
 *
 * ### With inline feature definitions
 * ```kotlin
 * val feature4k = feature4k {
 *     features {
 *         feature("dark-mode") {
 *             enabled = true
 *             description = "Enable dark theme"
 *             group = "ui"
 *         }
 *         feature("premium-features") {
 *             enabled = true
 *             permissions += listOf("ROLE_PREMIUM", "ROLE_ADMIN")
 *             flippingStrategy = allowList("alice", "bob")
 *         }
 *     }
 * }
 * ```
 *
 * ### With inline property definitions
 * ```kotlin
 * val feature4k = feature4k {
 *     properties {
 *         string("api.url", "https://api.example.com")
 *         int("max.connections", 100)
 *         boolean("debug.enabled", false)
 *     }
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin
 */
@DslMarker
annotation class Feature4KDsl
