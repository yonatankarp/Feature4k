package com.yonatankarp.feature4k.audit.emission

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test suite for [NoOpEventEmitter].
 *
 * @author Yonatan Karp-Rudin
 */
class NoOpEventEmitterTest {
    @Test
    fun `should discard all events and return empty flow`() = runTest {
        // Given
        val emitter = NoOpEventEmitter<String>()

        // When
        repeat(100) { i ->
            emitter.emit("event$i")
        }

        // Then
        val events = emitter.observe().toList()
        assertEquals(0, events.size, "NoOpEventEmitter should discard all events and return an empty flow")
    }
}
