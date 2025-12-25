package com.yonatankarp.feature4k.utils

import org.slf4j.LoggerFactory

/**
 * JVM implementation of Logger using SLF4J.
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
 * SLF4J-based logger implementation.
 */
private class Slf4jLogger(
    private val slf4jLogger: org.slf4j.Logger,
) : Logger {
    /**
     * Logs the provided message at debug level.
     *
     * @param message The message to log.
     */
    override fun debug(message: String) = slf4jLogger.debug(message)

    /**
     * Logs the given message at INFO level.
     *
     * @param message The message to log.
     */
    override fun info(message: String) = slf4jLogger.info(message)

    /**
     * Logs a warning-level message to the underlying SLF4J logger.
     *
     * @param message The warning message to record.
     */
    override fun warn(message: String) = slf4jLogger.warn(message)

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
            slf4jLogger.error(message, throwable)
        } else {
            slf4jLogger.error(message)
        }
    }
}

/**
 * Create a Logger backed by SLF4J for the given logger name.
 *
 * @param name The name used to obtain the underlying SLF4J logger (typically a class or category name).
 * @return A Logger implementation that delegates logging calls to SLF4J.
 */
actual fun logger(name: String): Logger = Slf4jLogger(LoggerFactory.getLogger(name))
