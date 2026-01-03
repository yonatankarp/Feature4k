package com.yonatankarp.feature4k.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Simple EventBus implementation that collects all emitted events into a list for testing.
 */
class CollectorEventBus<T> : EventBus<T> {
    val events = mutableListOf<T>()

    override suspend fun emit(event: T) {
        events.add(event)
    }

    override fun observe(): Flow<T> = emptyFlow()
}
