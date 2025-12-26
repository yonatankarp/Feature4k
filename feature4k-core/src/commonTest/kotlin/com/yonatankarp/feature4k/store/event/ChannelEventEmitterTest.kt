package com.yonatankarp.feature4k.store.event

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test suite for [ChannelEventEmitter].
 *
 * @author Yonatan Karp-Rudin
 */
class ChannelEventEmitterTest {
    @Test
    fun `should emit and observe events`() = runTest {
        // Given
        val emitter = ChannelEventEmitter<String>()
        val expected = listOf("event1", "event2", "event3")

        // When
        val job = launch {
            expected.forEach { emitter.emit(it) }
        }

        // Then
        val events = emitter.observe().take(3).toList()
        job.join()
        assertEquals(expected, events, "ChannelEventEmitter should emit all events in order")
    }

    @Test
    fun `should buffer events until consumed`() = runTest {
        // Given
        val emitter = ChannelEventEmitter<String>()

        // When - emit events before observing
        emitter.emit("event1")
        emitter.emit("event2")
        emitter.emit("event3")

        // Then
        val events = emitter.observe().take(3).toList()
        assertEquals(listOf("event1", "event2", "event3"), events, "ChannelEventEmitter should buffer events until consumed")
    }

    @Test
    fun `should handle concurrent emissions without blocking`() = runTest {
        // Given
        val emitter = ChannelEventEmitter<Int>()
        val eventCount = 100

        // When
        val collectJob = async {
            emitter.observe().take(eventCount).toList()
        }

        val emitJob = async {
            repeat(eventCount) { i ->
                emitter.emit(i)
            }
        }

        // Then
        awaitAll(collectJob, emitJob)
        val events = collectJob.await()
        assertEquals(eventCount, events.size, "All events should be collected without blocking")
    }
}
