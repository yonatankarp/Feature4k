package com.yonatankarp.feature4k.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.set
import platform.posix.rand
import platform.posix.srand
import platform.posix.time

/**
 * Windows/MinGW implementation of UUID generation.
 * Uses a combination of timestamp and random bytes for UUID generation.
 *
 * @author Yonatan Karp-Rudin
 */
@OptIn(ExperimentalForeignApi::class)
actual object Uid {
    init {
        srand(time(null).toUInt())
    }

    /**
     * Generate an RFC 4122 version 4 UUID.
     *
     * Uses the C standard library random number generator seeded with the current time to fill 16 bytes, sets the version and variant bits per RFC 4122, then formats the bytes as a lowercase hexadecimal UUID in the standard 8-4-4-4-12 layout (segments of characters separated by hyphens).
     *
     * @return The UUID string in the standard 8-4-4-4-12 lowercase hexadecimal format (e.g. `xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx`).
     */
    actual fun generate(): String {
        // Simple UUID v4 implementation
        val uuid = ByteArray(16)

        // Fill with random bytes using time-seeded PRNG
        memScoped {
            inMemoryRandomSeed(uuid)
        }

        // Set version (4) and variant bits according to RFC 4122
        uuid[6] = ((uuid[6].toInt() and 0x0F) or 0x40).toByte() // Version 4
        uuid[8] = ((uuid[8].toInt() and 0x3F) or 0x80).toByte() // Variant bits

        // Format as UUID string
        return buildString {
            for (i in uuid.indices) {
                if (i == 4 || i == 6 || i == 8 || i == 10) append('-')
                val byte = uuid[i].toInt() and 0xFF
                append(byte.toString(16).padStart(2, '0'))
            }
        }
    }

    private fun MemScope.inMemoryRandomSeed(uuid: ByteArray) {
        val ptr = uuid.refTo(0).getPointer(this)
        for (i in 0 until uuid.size) {
            ptr[i] = (rand() and 0xFF).toByte()
        }
    }
}
