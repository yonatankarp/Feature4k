package com.yonatankarp.feature4k.event

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test suite for [com.yonatankarp.feature4k.event.NoOpEventBus].
 *
 * @author Yonatan Karp-Rudin
 */
class NoOpEventBusTest {
    @Test
    fun `should discard all events and return empty flow`() = runTest {
        // Given
        val emitter = NoOpEventBus<String>()

        // When
        repeat(100) { i ->
            emitter.emit("event$i")
        }

        // Then
        val events = emitter.observe().toList()
        assertEquals(0, events.size, "NoOpEventBus should discard all events and return an empty flow")
    }
}
