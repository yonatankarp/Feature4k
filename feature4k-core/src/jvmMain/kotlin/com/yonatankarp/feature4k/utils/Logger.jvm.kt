package com.yonatankarp.feature4k.utils

import org.slf4j.LoggerFactory

/**
 * JVM implementation of Logger using SLF4J.
 */
actual interface Logger {
    actual fun debug(message: String)
    actual fun info(message: String)
    actual fun warn(message: String)
    actual fun error(message: String, throwable: Throwable?)
}

/**
 * SLF4J-based logger implementation.
 */
private class Slf4jLogger(private val slf4jLogger: org.slf4j.Logger) : Logger {
    override fun debug(message: String) = slf4jLogger.debug(message)
    override fun info(message: String) = slf4jLogger.info(message)
    override fun warn(message: String) = slf4jLogger.warn(message)
    override fun error(message: String, throwable: Throwable?) {
        if (throwable != null) {
            slf4jLogger.error(message, throwable)
        } else {
            slf4jLogger.error(message)
        }
    }
}

/**
 * Creates an SLF4J-based logger.
 */
actual fun logger(name: String): Logger = Slf4jLogger(LoggerFactory.getLogger(name))
