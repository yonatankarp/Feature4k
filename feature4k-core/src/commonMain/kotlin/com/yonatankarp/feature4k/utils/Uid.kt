package com.yonatankarp.feature4k.utils

/**
 * Platform-specific UUID generation.
 *
 * Provides a multiplatform abstraction for generating unique identifiers.
 * Each platform implements this using its native UUID capabilities.
 */
expect object Uid {
    /**
     * Generates a new universally unique identifier (UUID).
     *
     * @return A UUID string in standard format (e.g., "550e8400-e29b-41d4-a716-446655440000")
     */
    fun generate(): String
}
