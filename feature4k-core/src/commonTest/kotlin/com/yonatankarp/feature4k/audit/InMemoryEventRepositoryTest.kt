package com.yonatankarp.feature4k.audit

/**
 * Tests for [InMemoryEventRepository].
 *
 * Extends [EventRepositoryContract] to ensure the in-memory implementation
 * satisfies all repository behavioral requirements.
 *
 * @author Yonatan Karp-Rudin
 */
class InMemoryEventRepositoryTest : EventRepositoryContract() {
    override suspend fun createRepository(): EventRepository = InMemoryEventRepository()
}
