package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.event.PropertyStoreEvent
import com.yonatankarp.feature4k.property.PropertyString
import com.yonatankarp.feature4k.store.StoreFixtures.inMemoryPropertyStoreWithSharedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test suite for [InMemoryPropertyStore] implementation.
 *
 * This test class extends [PropertyStoreContract] to ensure that the in-memory
 * implementation adheres to the contract defined for all PropertyStore implementations.
 *
 * @author Yonatan Karp-Rudin
 */
class InMemoryPropertyStoreTest : PropertyStoreContract() {
    override suspend fun createStore(): PropertyStore = inMemoryPropertyStoreWithSharedFlow()

    @Test
    fun `should emit events in correct order with concurrent plusAssign and minusAssign`() = runTest {
        // Given
        val store = inMemoryPropertyStoreWithSharedFlow()
        val events = mutableListOf<PropertyStoreEvent>()

        // When
        val job = launch {
            store.observeChanges().take(100).collect { events.add(it) }
        }

        repeat(50) { i ->
            launch {
                runCatching {
                    store += PropertyString(name = "prop$i", value = "value$i")
                    delay(1)
                    store -= "prop$i"
                }.getOrNull()
            }
        }

        job.join()

        // Then
        val groupedByProperty = events.groupBy { it.uid }
        groupedByProperty.forEach { (propertyName, propertyEvents) ->
            val createdIndex = propertyEvents.indexOfFirst { it is PropertyStoreEvent.Created }
            val deletedIndex = propertyEvents.indexOfLast { it is PropertyStoreEvent.Deleted }

            if (createdIndex >= 0 && deletedIndex >= 0) {
                assertTrue(
                    createdIndex < deletedIndex,
                    "For property $propertyName: Created event must come before Deleted event",
                )
            }
        }
    }

    @Test
    fun `should maintain event-state consistency under concurrent operations`() = runTest {
        // Given
        val store = inMemoryPropertyStoreWithSharedFlow()
        val events = mutableListOf<PropertyStoreEvent>()
        val propertyCount = 50
        // Create + Update + Delete (for even indices)
        val expectedEvents = propertyCount * 2 + (propertyCount / 2)

        // When
        val collectJob = launch {
            store.observeChanges().take(expectedEvents).collect { events.add(it) }
        }

        val jobs = List(propertyCount) { i ->
            launch {
                store += PropertyString(name = "prop$i", value = "initial")
                store["prop$i"] = PropertyString(name = "prop$i", value = "updated")
                if (i % 2 == 0) {
                    store -= "prop$i"
                }
            }
        }

        jobs.joinAll()
        collectJob.join()

        // Then
        val stateFromEvents = mutableMapOf<String, Boolean>()
        events.forEach { event ->
            when (event) {
                is PropertyStoreEvent.Created -> stateFromEvents[event.uid] = true
                is PropertyStoreEvent.Updated -> stateFromEvents[event.uid] = true
                is PropertyStoreEvent.Deleted -> stateFromEvents.remove(event.uid)
            }
        }

        val actualState = store.getAll()

        assertEquals(
            actualState.keys,
            stateFromEvents.keys,
            "State reconstructed from events should match actual store state",
        )
    }
}
