package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.audit.emission.ChannelEventEmitter
import com.yonatankarp.feature4k.audit.emission.NoOpEventEmitter
import com.yonatankarp.feature4k.audit.emission.SharedFlowEventEmitter

/**
 * Test fixtures for Store objects.
 * These can be reused across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object StoreFixtures {
    /**
     * Creates an InMemoryPropertyStore with SharedFlow event emission for testing.
     *
     * This fixture uses [SharedFlowEventEmitter] to allow tests to observe and
     * verify store events with replay support. Configured with larger buffers
     * to handle high-volume test scenarios.
     *
     * @return An InMemoryPropertyStore configured with SharedFlowEventEmitter
     */
    fun inMemoryPropertyStoreWithSharedFlow() = InMemoryPropertyStore(
        eventEmitter = SharedFlowEventEmitter(
            replay = 50,
            extraBufferCapacity = 200,
        ),
    )

    /**
     * Creates an InMemoryPropertyStore with Channel event emission for testing.
     *
     * This fixture uses [ChannelEventEmitter] to allow tests to observe and
     * verify store events without replay.
     *
     * @return An InMemoryPropertyStore configured with ChannelEventEmitter
     */
    fun inMemoryPropertyStoreWithChannel() = InMemoryPropertyStore(
        eventEmitter = ChannelEventEmitter(),
    )

    /**
     * Creates an InMemoryPropertyStore with no event emission.
     *
     * This fixture uses [NoOpEventEmitter] for tests that don't require
     * event observation (default behavior).
     *
     * @return An InMemoryPropertyStore configured with NoOpEventEmitter
     */
    fun inMemoryPropertyStoreWithNoOp() = InMemoryPropertyStore(
        eventEmitter = NoOpEventEmitter(),
    )

    /**
     * Creates an InMemoryFeatureStore with SharedFlow event emission for testing.
     *
     * This fixture uses [SharedFlowEventEmitter] to allow tests to observe and
     * verify store events with replay support. Configured with larger buffers
     * to handle high-volume test scenarios.
     *
     * @return An InMemoryFeatureStore configured with SharedFlowEventEmitter
     */
    fun inMemoryFeatureStoreWithSharedFlow() = InMemoryFeatureStore(
        eventEmitter = SharedFlowEventEmitter(
            replay = 50,
            extraBufferCapacity = 200,
        ),
    )

    /**
     * Creates an InMemoryFeatureStore with Channel event emission for testing.
     *
     * This fixture uses [ChannelEventEmitter] to allow tests to observe and
     * verify store events without replay.
     *
     * @return An InMemoryFeatureStore configured with ChannelEventEmitter
     */
    fun inMemoryFeatureStoreWithChannel() = InMemoryFeatureStore(
        eventEmitter = ChannelEventEmitter(),
    )

    /**
     * Creates an InMemoryFeatureStore with no event emission.
     *
     * This fixture uses [NoOpEventEmitter] for tests that don't require
     * event observation (default behavior).
     *
     * @return An InMemoryFeatureStore configured with NoOpEventEmitter
     */
    fun inMemoryFeatureStoreWithNoOp() = InMemoryFeatureStore(
        eventEmitter = NoOpEventEmitter(),
    )
}
