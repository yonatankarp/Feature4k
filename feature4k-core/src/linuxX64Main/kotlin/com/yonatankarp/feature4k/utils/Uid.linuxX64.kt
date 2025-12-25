package com.yonatankarp.feature4k.utils

import kotlinx.cinterop.*
import platform.posix.*

/**
 * Linux/Native implementation of UUID generation.
 * Uses a combination of timestamp and random bytes for UUID generation.
 */
@OptIn(ExperimentalForeignApi::class)
actual object Uid {
    actual fun generate(): String {
        // Simple UUID v4 implementation
        val uuid = ByteArray(16)

        // Fill with random bytes
        memScoped {
            val ptr = uuid.refTo(0).getPointer(this)

            // Try to read from /dev/urandom for high-quality randomness
            val file = fopen("/dev/urandom", "rb")
            if (file != null) {
                val bytesToRead = uuid.size.toULong()
                val bytesRead = fread(ptr, 1.convert(), bytesToRead, file)
                fclose(file)

                if (bytesRead != bytesToRead) {
                    // Fallback: seed rand() and generate bytes
                    srand(time(null)?.toUInt() ?: 0u)
                    for (i in 0 until uuid.size) {
                        ptr[i] = (rand() and 0xFF).toByte()
                    }
                }
            } else {
                // Fallback: seed rand() and generate bytes
                srand(time(null)?.toUInt() ?: 0u)
                for (i in 0 until uuid.size) {
                    ptr[i] = (rand() and 0xFF).toByte()
                }
            }
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
}
