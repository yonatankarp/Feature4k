package com.yonatankarp.feature4k.event

import com.yonatankarp.feature4k.utils.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * A no-operation implementation of [com.yonatankarp.feature4k.event.EventBus] that discards all events.
 *
 * This is the recommended default implementation when event handling is not required,
 * as it has zero overhead.
 *
 * Use this when:
 * - You don't need to track or observe events
 * - You want to maximize performance by avoiding event emission overhead
 * - You're in a development or testing scenario where events aren't consumed
 *
 * @param T The type of events (unused, as all events are discarded)
 * @author Yonatan Karp-Rudin
 */
class NoOpEventBus<T> : EventBus<T> {
    private val logger = logger("NoOpEventBus")

    init {
        logger.debug("NoOpEventBus initialized - all events will be discarded. If you need event handling, consider using alternative implementations of EventBus.")
    }

    override suspend fun emit(event: T) {
        // No-op: events are discarded
    }

    override fun observe(): Flow<T> {
        logger.debug("observe() called - no events will be emitted as this is a no-op bus.")
        return emptyFlow()
    }
}
