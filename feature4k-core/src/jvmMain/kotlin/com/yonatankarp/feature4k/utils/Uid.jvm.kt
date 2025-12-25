package com.yonatankarp.feature4k.utils

import java.util.UUID

/**
 * JVM implementation of UUID generation using java.util.UUID.
 */
actual object Uid {
    actual fun generate(): String = UUID.randomUUID().toString()
}
