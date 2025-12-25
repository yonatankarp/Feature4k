package com.yonatankarp.feature4k.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Linux/Native implementation of Logger using console output.
 *
 * @author Yonatan Karp-Rudin
 */
actual interface Logger {
    /**
     * Logs a debug-level message.
     * Suspend function to support async logging backends.
     *
     * @param message The message to log.
     */
    actual suspend fun debug(message: String)

    /**
     * Logs an informational message.
     * Suspend function to support async logging backends.
     *
     * @param message The message to log.
     */
    actual suspend fun info(message: String)

    /**
     * Logs a warning message.
     * Suspend function to support async logging backends.
     *
     * @param message The warning message to log.
     */
    actual suspend fun warn(message: String)

    /**
     * Logs an error entry that includes the logger's name and optional throwable details.
     * Suspend function to support async logging backends.
     *
     * Writes an error-level log line prefixed with the logger name; if `throwable` is non-null,
     * the throwable's stack trace is included in the log output.
     *
     * @param message The error message to log.
     * @param throwable An optional throwable whose stack trace will be logged; pass `null` to omit.
     */
    actual suspend fun error(
        message: String,
        throwable: Throwable?,
    )
}

/**
 * Console-based logger implementation for Native platforms.
 * Uses Dispatchers.Default for logging operations to support async patterns.
 */
private class ConsoleLogger(
    private val name: String,
) : Logger {
    /**
     * Writes a debug-level log line to standard output, prefixed with the log level and logger name.
     * Uses Dispatchers.Default for consistency with async logging pattern.
     *
     * @param message The message to log; it will be printed after the prefix "[DEBUG] [<name>]".
     */
    override suspend fun debug(message: String) = withContext(Dispatchers.Default) {
        println("[DEBUG] [$name] $message")
    }

    /**
     * Logs an informational message to the console using a standardized prefix that includes the logger name.
     * Uses Dispatchers.Default for consistency with async logging pattern.
     *
     * @param message The message to log.
     */
    override suspend fun info(message: String) = withContext(Dispatchers.Default) {
        println("[INFO] [$name] $message")
    }

    /**
     * Logs a warning message prefixed with the logger's name.
     * Uses Dispatchers.Default for consistency with async logging pattern.
     *
     * The message is printed to standard output and prefixed with "[WARN] [<name>]".
     *
     * @param message The warning text to log.
     */
    override suspend fun warn(message: String) = withContext(Dispatchers.Default) {
        println("[WARN] [$name] $message")
    }

    /**
     * Logs an error-level message for this logger and, if provided, logs the throwable's stack trace.
     * Uses Dispatchers.Default for consistency with async logging pattern.
     *
     * @param message The error message to log.
     * @param throwable An optional throwable whose stack trace will be logged on a separate error line if not null.
     */
    override suspend fun error(
        message: String,
        throwable: Throwable?,
    ) = withContext(Dispatchers.Default) {
        println("[ERROR] [$name] $message")
        throwable?.let {
            println("[ERROR] [$name] ${it.stackTraceToString()}")
        }
    }
}

/**
 * Creates a logger that writes messages to the console and prefixes entries with the provided name.
 *
 * @param name Identifier included in each log line (for example, a component or class name).
 * @return A Console-backed Logger that outputs formatted log lines to standard output.
 */
actual fun logger(name: String): Logger = ConsoleLogger(name)
