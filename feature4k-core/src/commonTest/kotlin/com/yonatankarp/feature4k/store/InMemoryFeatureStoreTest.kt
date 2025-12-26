package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.store.StoreFixtures.inMemoryFeatureStoreWithSharedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test suite for [InMemoryFeatureStore] implementation.
 *
 * This test class extends [FeatureStoreContract] to ensure that the in-memory
 * implementation adheres to the contract defined for all FeatureStore implementations.
 *
 * @author Yonatan Karp-Rudin
 */
class InMemoryFeatureStoreTest : FeatureStoreContract() {
    override suspend fun createStore(): FeatureStore = inMemoryFeatureStoreWithSharedFlow()

    @Test
    fun `should emit events in correct order with concurrent plusAssign and minusAssign`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithSharedFlow()
        val events = mutableListOf<StoreEvent>()

        // When
        val job = launch {
            store.observeChanges().take(100).toList(events)
        }

        repeat(50) { i ->
            launch {
                runCatching {
                    store += Feature(uid = "feature$i", enabled = false)
                    delay(1)
                    store -= "feature$i"
                }.getOrNull()
            }
        }

        job.join()

        // Then
        val groupedByFeature = events.groupBy { it.featureId }
        groupedByFeature.forEach { (featureId, featureEvents) ->
            val createdIndex = featureEvents.indexOfFirst { it is StoreEvent.Created }
            val deletedIndex = featureEvents.indexOfLast { it is StoreEvent.Deleted }

            if (createdIndex >= 0 && deletedIndex >= 0) {
                assertTrue(
                    createdIndex < deletedIndex,
                    "For feature $featureId: Created event must come before Deleted event",
                )
            }
        }
    }

    @Test
    fun `should maintain event-state consistency under concurrent operations`() = runTest {
        // Given
        val store = inMemoryFeatureStoreWithSharedFlow()
        val events = mutableListOf<StoreEvent>()
        val featureCount = 50
        // Create + Update (enable/disable) + Delete (for even indices)
        val expectedEvents = featureCount * 2 + (featureCount / 2)

        // When
        val collectJob = launch {
            store.observeChanges().take(expectedEvents).toList(events)
        }

        val jobs = List(featureCount) { i ->
            launch {
                store += Feature(uid = "feature$i", enabled = false)
                store.enable("feature$i")
                if (i % 2 == 0) {
                    store -= "feature$i"
                }
            }
        }

        jobs.joinAll()
        collectJob.join()

        // Then
        val stateFromEvents = mutableMapOf<String, Boolean>()
        events.forEach { event ->
            when (event) {
                is StoreEvent.Created -> stateFromEvents[event.featureId] = true
                is StoreEvent.Updated -> stateFromEvents[event.featureId] = true
                is StoreEvent.Enabled -> stateFromEvents[event.featureId] = true
                is StoreEvent.Disabled -> stateFromEvents[event.featureId] = true
                is StoreEvent.Deleted -> stateFromEvents.remove(event.featureId)
                is StoreEvent.RoleUpdated -> stateFromEvents[event.featureId] = true
                is StoreEvent.RoleDeleted -> stateFromEvents[event.featureId] = true
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
