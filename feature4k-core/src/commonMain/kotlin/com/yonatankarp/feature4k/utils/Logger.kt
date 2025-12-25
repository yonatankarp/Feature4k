package com.yonatankarp.feature4k.utils

/**
 * Platform-specific logging interface.
 *
 * Provides a multiplatform abstraction for logging. Each platform implements
 * this using appropriate logging mechanisms (e.g., SLF4J on JVM, console on other platforms).
 */
expect interface Logger {
    /**
     * Logs a debug message.
     *
     * @param message The message to log
     */
    fun debug(message: String)

    /**
     * Logs an info message.
     *
     * @param message The message to log
     */
    fun info(message: String)

    /**
     * Logs a warning message.
     *
     * @param message The message to log
     */
    fun warn(message: String)

    /**
     * Logs an error message.
     *
     * @param message The message to log
     * @param throwable Optional throwable/exception to log
     */
    fun error(
        message: String,
        throwable: Throwable? = null,
    )
}

/**
 * Creates a logger for the specified name.
 *
 * @param name The logger name (typically a class name)
 * @return A platform-specific Logger instance
 */
expect fun logger(name: String): Logger
