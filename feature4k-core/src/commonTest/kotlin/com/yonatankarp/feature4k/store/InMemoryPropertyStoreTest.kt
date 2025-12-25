package com.yonatankarp.feature4k.store

/**
 * Test suite for [InMemoryPropertyStore] implementation.
 *
 * This test class extends [PropertyStoreContract] to ensure that the in-memory
 * implementation adheres to the contract defined for all PropertyStore implementations.
 *
 * @author Yonatan Karp-Rudin
 */
class InMemoryPropertyStoreTest : PropertyStoreContract() {
    override suspend fun createStore(): PropertyStore = InMemoryPropertyStore()
}
