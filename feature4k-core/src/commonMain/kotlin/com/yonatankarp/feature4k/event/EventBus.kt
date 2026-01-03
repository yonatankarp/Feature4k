package com.yonatankarp.feature4k.event

import kotlinx.coroutines.flow.Flow

/**
 * Generic event bus interface for emitting and observing events.
 *
 * This interface unifies event publishing and observation in a single contract,
 * supporting both one-way event emission and bidirectional reactive streams.
 *
 * Implementations should prefer non-blocking behavior to prevent stalling
 * operations that emit events.
 *
 * @param T The type of events this bus handles
 * @author Yonatan Karp-Rudin
 */
interface EventBus<T> {
    /**
     * Emits an event to all observers.
     *
     * Implementations should prefer non-blocking behavior to prevent
     * stalling operations that emit events.
     *
     * @param event The event to emit
     */
    suspend fun emit(event: T)

    /**
     * Returns a Flow that observers can collect to receive events.
     *
     * @return A Flow of events
     */
    fun observe(): Flow<T>
}
