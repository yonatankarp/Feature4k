package com.yonatankarp.feature4k.store.event

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for emitting and observing store events.
 *
 * This interface decouples event emission from store implementations, providing:
 * - **Flexibility**: Swap event emission strategies without changing store logic
 * - **Optional auditing**: Choose whether to track events based on your needs
 * - **Non-blocking operations**: Implementations control blocking behavior
 * - **Separation of concerns**: Stores focus on storage, emitters handle events
 *
 * @param T The type of events this emitter handles
 * @author Yonatan Karp-Rudin
 */
interface StoreEventEmitter<T> {
    /**
     * Emits an event to all observers.
     *
     * Implementations should prefer non-blocking behavior to prevent
     * stalling store operations.
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
