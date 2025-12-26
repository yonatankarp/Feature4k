package com.yonatankarp.feature4k.store.event

import com.yonatankarp.feature4k.utils.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * SharedFlow-based implementation of [StoreEventEmitter] for scenarios needing replay.
 *
 * This implementation uses [MutableSharedFlow] with configurable replay buffer and extra capacity.
 * New observers receive recent events based on the replay buffer size. Events are emitted
 * non-blockingly using [MutableSharedFlow.tryEmit], which may drop events if all buffers are full.
 *
 * Use this when:
 * - New observers need to receive recent historical events (replay)
 * - You want multiple observers to share the same event stream
 * - You can tolerate potential event loss when buffers fill up
 *
 * @param replay Number of recent events to replay to new observers (default: 10)
 * @param extraBufferCapacity Extra buffer capacity for events beyond replay (default: 64)
 * @param T The type of events this emitter handles
 * @author Yonatan Karp-Rudin
 */
class SharedFlowEventEmitter<T>(
    replay: Int = 10,
    extraBufferCapacity: Int = 64,
) : StoreEventEmitter<T> {
    private val flow = MutableSharedFlow<T>(
        replay = replay,
        extraBufferCapacity = extraBufferCapacity,
    )
    private val logger = logger("SharedFlowEventEmitter")

    override suspend fun emit(event: T) {
        if (!flow.tryEmit(event)) {
            logger.warn("Failed to emit event (buffers full, event dropped): $event")
        }
    }

    override fun observe(): Flow<T> = flow.asSharedFlow()
}
