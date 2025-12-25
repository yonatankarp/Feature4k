package com.yonatankarp.feature4k.utils

import java.util.UUID

/**
 * JVM implementation of UUID generation using java.util.UUID.
 *
 * @author Yonatan Karp-Rudin
 */
actual object Uid {
    /**
     * Generates a new UUID string.
     *
     * @return The UUID in standard string form (e.g., "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx").
     */
    actual fun generate(): String = UUID.randomUUID().toString()
}
