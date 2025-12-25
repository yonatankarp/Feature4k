package com.yonatankarp.feature4k.utils

import kotlin.test.Test
import kotlin.test.assertNotNull

class LoggerTest {
    @Test
    fun `logger can be created with name`() {
        val log = logger("TestLogger")
        assertNotNull(log, "Logger should be created successfully")
    }

    @Test
    fun `logger debug does not throw`() {
        val log = logger("TestLogger")
        log.debug("Debug message")
    }

    @Test
    fun `logger info does not throw`() {
        val log = logger("TestLogger")
        log.info("Info message")
    }

    @Test
    fun `logger warn does not throw`() {
        val log = logger("TestLogger")
        log.warn("Warning message")
    }

    @Test
    fun `logger error does not throw`() {
        val log = logger("TestLogger")
        log.error("Error message")
    }

    @Test
    fun `logger error with throwable does not throw`() {
        val log = logger("TestLogger")
        val exception = Exception("Test exception")
        log.error("Error message with exception", exception)
    }
}
