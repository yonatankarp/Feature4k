package com.yonatankarp.feature4k.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

/**
 * JVM implementation of Logger using SLF4J.
 *
 * @author Yonatan Karp-Rudin
 */
actual interface Logger {
    /**
     * Logs the given message at debug level.
     * Suspend function to support async logging backends.
     */
    actual suspend fun debug(message: String)

    /**
     * Logs an informational message.
     * Suspend function to support async logging backends.
     */
    actual suspend fun info(message: String)

    /**
     * Logs a warning-level message.
     * Suspend function to support async logging backends.
     *
     * @param message The warning message to log.
     */
    actual suspend fun warn(message: String)

    /**
     * Logs an error-level message, optionally including an associated throwable.
     * Suspend function to support async logging backends.
     *
     * @param message The error message to log.
     * @param throwable An optional throwable whose stack trace will be logged if provided; pass `null` to log only the message.
     */
    actual suspend fun error(
        message: String,
        throwable: Throwable?,
    )
}

/**
 * SLF4J-based logger implementation.
 * Uses Dispatchers.IO for logging operations to avoid blocking the calling coroutine.
 */
private class Slf4jLogger(
    private val slf4jLogger: org.slf4j.Logger,
) : Logger {
    /**
     * Logs the provided message at debug level.
     * Uses Dispatchers.IO to avoid blocking on potential I/O operations.
     *
     * @param message The message to log.
     */
    override suspend fun debug(message: String) = withContext(Dispatchers.IO) {
        slf4jLogger.debug(message)
    }

    /**
     * Logs the given message at INFO level.
     * Uses Dispatchers.IO to avoid blocking on potential I/O operations.
     *
     * @param message The message to log.
     */
    override suspend fun info(message: String) = withContext(Dispatchers.IO) {
        slf4jLogger.info(message)
    }

    /**
     * Logs a warning-level message to the underlying SLF4J logger.
     * Uses Dispatchers.IO to avoid blocking on potential I/O operations.
     *
     * @param message The warning message to record.
     */
    override suspend fun warn(message: String) = withContext(Dispatchers.IO) {
        slf4jLogger.warn(message)
    }

    /**
     * Logs an error message, optionally including a throwable's stack trace.
     * Uses Dispatchers.IO to avoid blocking on potential I/O operations.
     *
     * @param message The error message to log.
     * @param throwable The throwable to log with the message, or `null` to log the message without a stack trace.
     */
    override suspend fun error(
        message: String,
        throwable: Throwable?,
    ) = withContext(Dispatchers.IO) {
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
