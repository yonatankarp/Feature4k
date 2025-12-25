package com.yonatankarp.feature4k.utils

/**
 * Platform-specific UUID generation.
 *
 * Provides a multiplatform abstraction for generating unique identifiers.
 * Each platform implements this using its native UUID capabilities.
 *
 * @author Yonatan Karp-Rudin
 */
expect object Uid {
    /**
     * Generates a new universally unique identifier (UUID).
     *
     * @return A UUID string in standard 8-4-4-4-12 hexadecimal format (RFC 4122), for example "550e8400-e29b-41d4-a716-446655440000".
     */
    fun generate(): String
}
