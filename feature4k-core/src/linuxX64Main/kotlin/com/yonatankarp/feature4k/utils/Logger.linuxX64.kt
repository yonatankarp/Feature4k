package com.yonatankarp.feature4k.utils

/**
 * Linux/Native implementation of Logger using console output.
 */
actual interface Logger {
    actual fun debug(message: String)
    actual fun info(message: String)
    actual fun warn(message: String)
    actual fun error(message: String, throwable: Throwable?)
}

/**
 * Console-based logger implementation for Native platforms.
 */
private class ConsoleLogger(private val name: String) : Logger {
    override fun debug(message: String) {
        println("[DEBUG] [$name] $message")
    }

    override fun info(message: String) {
        println("[INFO] [$name] $message")
    }

    override fun warn(message: String) {
        println("[WARN] [$name] $message")
    }

    override fun error(message: String, throwable: Throwable?) {
        println("[ERROR] [$name] $message")
        throwable?.let {
            println("[ERROR] [$name] ${it.stackTraceToString()}")
        }
    }
}

/**
 * Creates a console-based logger for Native platforms.
 */
actual fun logger(name: String): Logger = ConsoleLogger(name)
