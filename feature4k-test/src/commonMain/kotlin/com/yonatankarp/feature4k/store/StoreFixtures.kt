package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.event.ChannelEventBus
import com.yonatankarp.feature4k.event.NoOpEventBus
import com.yonatankarp.feature4k.event.SharedFlowEventBus

/**
 * Test fixtures for Store objects.
 * These can be reused across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object StoreFixtures {
    /**
     * Creates an InMemoryPropertyStore with SharedFlow event bus for testing.
     *
     * This fixture uses [com.yonatankarp.feature4k.event.SharedFlowEventBus] to allow tests to observe and
     * verify store events with replay support. Configured with larger buffers
     * to handle high-volume test scenarios.
     *
     * @return An InMemoryPropertyStore configured with SharedFlowEventBus
     */
    fun inMemoryPropertyStoreWithSharedFlow() = InMemoryPropertyStore(
        eventBus = SharedFlowEventBus(
            replay = 50,
            extraBufferCapacity = 200,
        ),
    )

    /**
     * Creates an InMemoryPropertyStore with Channel event bus for testing.
     *
     * This fixture uses [com.yonatankarp.feature4k.event.ChannelEventBus] to allow tests to observe and
     * verify store events without replay.
     *
     * @return An InMemoryPropertyStore configured with ChannelEventBus
     */
    fun inMemoryPropertyStoreWithChannel() = InMemoryPropertyStore(
        eventBus = ChannelEventBus(),
    )

    /**
     * Creates an InMemoryPropertyStore with no event bus.
     *
     * This fixture uses [com.yonatankarp.feature4k.event.NoOpEventBus] for tests that don't require
     * event observation (default behavior).
     *
     * @return An InMemoryPropertyStore configured with NoOpEventBus
     */
    fun inMemoryPropertyStoreWithNoOp() = InMemoryPropertyStore(
        eventBus = NoOpEventBus(),
    )

    /**
     * Creates an InMemoryFeatureStore with SharedFlow event bus for testing.
     *
     * This fixture uses [com.yonatankarp.feature4k.event.SharedFlowEventBus] to allow tests to observe and
     * verify store events with replay support. Configured with larger buffers
     * to handle high-volume test scenarios.
     *
     * @return An InMemoryFeatureStore configured with SharedFlowEventBus
     */
    fun inMemoryFeatureStoreWithSharedFlow() = InMemoryFeatureStore(
        eventBus = SharedFlowEventBus(
            replay = 50,
            extraBufferCapacity = 200,
        ),
    )

    /**
     * Creates an InMemoryFeatureStore with Channel event bus for testing.
     *
     * This fixture uses [com.yonatankarp.feature4k.event.ChannelEventBus] to allow tests to observe and
     * verify store events without replay.
     *
     * @return An InMemoryFeatureStore configured with ChannelEventBus
     */
    fun inMemoryFeatureStoreWithChannel() = InMemoryFeatureStore(
        eventBus = ChannelEventBus(),
    )

    /**
     * Creates an InMemoryFeatureStore with no event bus.
     *
     * This fixture uses [com.yonatankarp.feature4k.event.NoOpEventBus] for tests that don't require
     * event observation (default behavior).
     *
     * @return An InMemoryFeatureStore configured with NoOpEventBus
     */
    fun inMemoryFeatureStoreWithNoOp() = InMemoryFeatureStore(
        eventBus = NoOpEventBus(),
    )
}
