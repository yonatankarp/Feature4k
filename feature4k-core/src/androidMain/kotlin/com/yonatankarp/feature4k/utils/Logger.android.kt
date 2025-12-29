package com.yonatankarp.feature4k.utils

import android.util.Log

/**
 * Android implementation of Logger using Android's Log API.
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
 * Android Log-based logger implementation.
 *
 * Note: Android's Log API has a tag length limit of 23 characters.
 * Tags longer than this will be automatically truncated to ensure consistent behavior.
 */
private class AndroidLogger(
    tag: String,
) : Logger {
    // Android Log has a 23-character tag limit; truncate if needed
    private val tag: String = if (tag.length <= 23) tag else tag.take(23)

    /**
     * Logs the provided message at debug level.
     *
     * @param message The message to log.
     */
    override fun debug(message: String) {
        Log.d(tag, message)
    }

    /**
     * Logs the given message at INFO level.
     *
     * @param message The message to log.
     */
    override fun info(message: String) {
        Log.i(tag, message)
    }

    /**
     * Logs a warning-level message to the underlying Android logger.
     *
     * @param message The warning message to record.
     */
    override fun warn(message: String) {
        Log.w(tag, message)
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
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}

/**
 * Create a Logger backed by Android Log for the given logger name.
 *
 * @param name The name used as the tag for Android logging (typically a class or category name).
 * @return A Logger implementation that delegates logging calls to Android Log.
 */
actual fun logger(name: String): Logger = AndroidLogger(name)
