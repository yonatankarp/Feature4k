package com.yonatankarp.feature4k.utils

import platform.Foundation.NSLog

/**
 * Apple platform (iOS/macOS) implementation of Logger using NSLog.
 *
 * @author Yonatan Karp-Rudin
 */
actual interface Logger {
    /**
     * Logs the given message at debug level.
     */
    actual fun debug(message: String)

    /**
     * Logs an informational message.
     */
    actual fun info(message: String)

    /**
     * Logs a warning-level message.
     *
     * @param message The warning message to log.
     */
    actual fun warn(message: String)

    /**
     * Logs an error-level message, optionally including an associated throwable.
     *
     * @param message The error message to log.
     * @param throwable An optional throwable whose stack trace will be logged if provided; pass `null` to log only the message.
     */
    actual fun error(
        message: String,
        throwable: Throwable?,
    )
}

/**
 * NSLog-based logger implementation for Apple platforms.
 */
private class AppleLogger(
    private val tag: String,
) : Logger {
    /**
     * Logs the provided message at debug level.
     *
     * @param message The message to log.
     */
    override fun debug(message: String) {
        NSLog("[$tag] DEBUG: $message")
    }

    /**
     * Logs the given message at INFO level.
     *
     * @param message The message to log.
     */
    override fun info(message: String) {
        NSLog("[$tag] INFO: $message")
    }

    /**
     * Logs a warning-level message.
     *
     * @param message The warning message to record.
     */
    override fun warn(message: String) {
        NSLog("[$tag] WARN: $message")
    }

    /**
     * Logs an error message, optionally including a throwable's stack trace.
     *
     * @param message The error message to log.
     * @param throwable The throwable to log with the message, or `null` to log the message without a stack trace.
     */
    override fun error(
        message: String,
        throwable: Throwable?,
    ) {
        if (throwable != null) {
            NSLog("[$tag] ERROR: $message - ${throwable.stackTraceToString()}")
        } else {
            NSLog("[$tag] ERROR: $message")
        }
    }
}

/**
 * Create a Logger backed by NSLog for the given logger name.
 *
 * @param name The name used as the tag for logging (typically a class or category name).
 * @return A Logger implementation that delegates logging calls to NSLog.
 */
actual fun logger(name: String): Logger = AppleLogger(name)
