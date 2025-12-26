package com.yonatankarp.feature4k.audit.emission

import com.yonatankarp.feature4k.utils.logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Channel-based implementation of [StoreEventEmitter] for auditing scenarios.
 *
 * This implementation uses an unbounded channel with non-blocking sends via [Channel.trySend].
 * Events are never dropped under normal circumstances, but if channel operations fail,
 * the failure is logged.
 *
 * Use this when:
 * - You need reliable event delivery for auditing
 * - You want events to flow to observers as they occur (no replay)
 * - You want to avoid blocking store operations if consumers are slow
 *
 * @param T The type of events this emitter handles
 * @author Yonatan Karp-Rudin
 */
class ChannelEventEmitter<T> : StoreEventEmitter<T> {
    private val channel = Channel<T>(Channel.UNLIMITED)
    private val logger = logger("ChannelEventEmitter")

    override suspend fun emit(event: T) {
        val result = channel.trySend(event)
        if (result.isFailure) {
            logger.warn("Failed to emit event (channel closed or failed): $event")
        }
    }

    override fun observe(): Flow<T> = channel.receiveAsFlow()
}
