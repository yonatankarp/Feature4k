package com.yonatankarp.feature4k.store

/**
 * Test suite for [InMemoryFeatureStore] implementation.
 *
 * This test class extends [FeatureStoreContract] to ensure that the in-memory
 * implementation adheres to the contract defined for all FeatureStore implementations.
 *
 * @author Yonatan Karp-Rudin
 */
class InMemoryFeatureStoreTest : FeatureStoreContract() {
    override suspend fun createStore(): FeatureStore = InMemoryFeatureStore()
}
