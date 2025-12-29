package com.yonatankarp.feature4k.utils

import platform.Foundation.NSUUID

/**
 * Apple platform (iOS/macOS) implementation of UUID generation using NSUUID.
 *
 * @author Yonatan Karp-Rudin
 */
actual object Uid {
    /**
     * Generates a new UUID string in lowercase format.
     *
     * Note: NSUUID().UUIDString() returns uppercase UUIDs on Apple platforms, while
     * UUID.randomUUID().toString() on Android and JVM returns lowercase by default.
     * We normalize to lowercase here to ensure cross-platform consistency.
     *
     * @return The UUID in standard lowercase string form (e.g., "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx").
     */
    actual fun generate(): String = NSUUID().UUIDString().lowercase()
}
