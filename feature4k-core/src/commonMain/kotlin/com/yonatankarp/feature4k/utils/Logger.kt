package com.yonatankarp.feature4k.utils

/**
 * Platform-specific logging interface.
 *
 * Provides a multiplatform abstraction for logging. Each platform implements
 * this using appropriate logging mechanisms (e.g., SLF4J on JVM, console on other platforms).
 *
 * @author Yonatan Karp-Rudin
 */
expect interface Logger {
    /**
     * Logs a debug message.
     * Suspend function to support async logging backends (file I/O, network sinks, etc.).
     *
     * @param message The message to log
     */
    suspend fun debug(message: String)

    /**
     * Logs an info message.
     * Suspend function to support async logging backends (file I/O, network sinks, etc.).
     *
     * @param message The message to log
     */
    suspend fun info(message: String)

    /**
     * Logs a warning message.
     * Suspend function to support async logging backends (file I/O, network sinks, etc.).
     *
     * @param message The message to log
     */
    suspend fun warn(message: String)

    /**
     * Logs an error message.
     * Suspend function to support async logging backends (file I/O, network sinks, etc.).
     *
     * @param message The message to log
     * @param throwable Optional throwable/exception to log
     */
    suspend fun error(
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
