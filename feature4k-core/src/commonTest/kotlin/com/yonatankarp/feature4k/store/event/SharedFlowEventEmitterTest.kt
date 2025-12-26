package com.yonatankarp.feature4k.store.event

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test suite for [SharedFlowEventEmitter].
 *
 * @author Yonatan Karp-Rudin
 */
class SharedFlowEventEmitterTest {
    @Test
    fun `should emit and observe events`() = runTest {
        // Given
        val emitter = SharedFlowEventEmitter<String>()
        val expected = listOf("event1", "event2", "event3")

        // When
        val collectJob = launch {
            emitter.observe().take(3).toList()
        }

        expected.forEach { emitter.emit(it) }

        // Then
        collectJob.join()
    }

    @Test
    fun `should replay recent events to new observers`() = runTest {
        // Given
        val emitter = SharedFlowEventEmitter<String>(replay = 2, extraBufferCapacity = 0)

        // When
        emitter.emit("event1")
        emitter.emit("event2")
        emitter.emit("event3")

        delay(10)

        // Then
        val events = emitter.observe().take(2).toList()
        assertEquals(listOf("event2", "event3"), events, "SharedFlowEventEmitter should replay the last 2 events")
    }

    @Test
    fun `should drop events when buffer is full`() = runTest {
        // Given
        val emitter = SharedFlowEventEmitter<Int>(replay = 2, extraBufferCapacity = 2)

        // When
        repeat(10) { i ->
            emitter.emit(i)
        }

        delay(10)

        // Then
        val events = emitter.observe().take(2).toList()
        assertEquals(events.size, 2, "SharedFlowEventEmitter should drop events when buffer is full")
    }

    @Test
    fun `should handle concurrent emissions without blocking`() = runTest {
        // Given
        val emitter = SharedFlowEventEmitter<Int>(replay = 10, extraBufferCapacity = 50)
        val eventCount = 50

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
