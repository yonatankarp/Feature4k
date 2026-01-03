package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.core.IdentifierFixtures.NON_EXISTENT
import com.yonatankarp.feature4k.event.PropertyStoreEvent
import com.yonatankarp.feature4k.exception.PropertyAlreadyExistException
import com.yonatankarp.feature4k.exception.PropertyNotFoundException
import com.yonatankarp.feature4k.property.PropertyBoolean
import com.yonatankarp.feature4k.property.PropertyInt
import com.yonatankarp.feature4k.property.PropertyString
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Abstract test contract for PropertyStore implementations.
 *
 * All PropertyStore implementations must extend this class and implement [createStore]
 * to ensure consistent behavior across different store implementations.
 *
 * @author Yonatan Karp-Rudin
 */
abstract class PropertyStoreContract {
    /**
     * Create a fresh instance of the PropertyStore implementation to test.
     * This method is called before each test to ensure test isolation.
     */
    abstract suspend fun createStore(): PropertyStore

    @Test
    fun `should return false for contains when property does not exist`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFalse(NON_EXISTENT in store)
    }

    @Test
    fun `should create property using plusAssign operator`() = runTest {
        // Given
        val store = createStore()
        val property = PropertyString(name = "test", value = "value")

        // When
        store += property

        // Then
        assertTrue("test" in store)
        assertEquals(property, store["test"])
    }

    @Test
    fun `should throw PropertyAlreadyExistException when creating duplicate property`() = runTest {
        // Given
        val store = createStore()
        val property = PropertyString(name = "test", value = "value")
        store += property

        // When & Then
        assertFailsWith<PropertyAlreadyExistException> {
            store += property
        }
    }

    @Test
    fun `should return null when getting non-existent property`() = runTest {
        // Given
        val store = createStore()

        // When
        val result = store[NON_EXISTENT]

        // Then
        assertNull(result)
    }

    @Test
    fun `should return property after creation`() = runTest {
        // Given
        val store = createStore()
        val property = PropertyString(
            name = "test",
            value = "value",
            description = "Test property",
        )

        // When
        store += property

        // Then
        val retrieved = store["test"]
        assertNotNull(retrieved)
        assertEquals("test", (retrieved as PropertyString).name)
        assertEquals("value", retrieved.value)
        assertEquals("Test property", retrieved.description)
    }

    @Test
    fun `should create property using set operator when not exists`() = runTest {
        // Given
        val store = createStore()
        val property = PropertyString(name = "test", value = "value")

        // When
        store["test"] = property

        // Then
        assertTrue("test" in store)
        assertEquals(property, store["test"])
    }

    @Test
    fun `should update property using set operator when exists`() = runTest {
        // Given
        val store = createStore()
        val original = PropertyString(name = "test", value = "original")
        store += original

        // When
        val updated = PropertyString(name = "test", value = "updated")
        store["test"] = updated

        // Then
        val retrieved = store["test"] as PropertyString
        assertNotNull(retrieved)
        assertEquals("updated", retrieved.value)
    }

    @Test
    fun `should delete property using minusAssign operator`() = runTest {
        // Given
        val store = createStore()
        val property = PropertyString(name = "test", value = "value")
        store += property

        // When
        store -= "test"

        // Then
        assertFalse("test" in store)
    }

    @Test
    fun `should throw PropertyNotFoundException when deleting non-existent property`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<PropertyNotFoundException> {
            store -= NON_EXISTENT
        }
    }

    @Test
    fun `should return empty map when no properties exist`() = runTest {
        // Given
        val store = createStore()

        // When
        val result = store.getAll()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return all properties`() = runTest {
        // Given
        val store = createStore()
        val property1 = PropertyString(name = "prop1", value = "value1")
        val property2 = PropertyInt(name = "prop2", value = 42)
        val property3 = PropertyBoolean(name = "prop3", value = true)
        store += property1
        store += property2
        store += property3

        // When
        val all = store.getAll()

        // Then
        assertEquals(3, all.size)
        assertTrue("prop1" in all)
        assertTrue("prop2" in all)
        assertTrue("prop3" in all)
    }

    @Test
    fun `should remove all properties when clearing`() = runTest {
        // Given
        val store = createStore()
        store += PropertyString(name = "prop1", value = "value1")
        store += PropertyString(name = "prop2", value = "value2")

        // When
        store.clear()

        // Then
        assertTrue(store.getAll().isEmpty())
    }

    @Test
    fun `should import multiple properties`() = runTest {
        // Given
        val store = createStore()
        val properties = listOf(
            PropertyString(name = "prop1", value = "value1"),
            PropertyInt(name = "prop2", value = 42),
            PropertyBoolean(name = "prop3", value = true),
        )

        // When
        store.importProperties(properties)

        // Then
        assertEquals(3, store.getAll().size)
        assertTrue("prop1" in store)
        assertTrue("prop2" in store)
        assertTrue("prop3" in store)
    }

    @Test
    fun `should overwrite existing properties when importing`() = runTest {
        // Given
        val store = createStore()
        store += PropertyString(name = "prop1", value = "original")
        val properties = listOf(
            PropertyString(name = "prop1", value = "updated"),
            PropertyString(name = "prop2", value = "new"),
        )

        // When
        store.importProperties(properties)

        // Then
        val property1 = store["prop1"] as PropertyString
        assertNotNull(property1)
        assertEquals("updated", property1.value)
    }

    @Test
    fun `should emit Created event when property is created`() = runTest {
        // Given
        val store = createStore()
        val property = PropertyString(name = "test", value = "value")
        val events = mutableListOf<PropertyStoreEvent>()

        // When
        val job = launch {
            store.observeChanges().take(1).collect { events.add(it) }
        }
        store += property
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is PropertyStoreEvent.Created)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Updated event when property is updated`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<PropertyStoreEvent>()

        // When
        val job = launch {
            store.observeChanges()
                .drop(1) // Skip Created event from setup
                .take(1)
                .collect { events.add(it) }
        }
        store += PropertyString(name = "test", value = "original")
        store["test"] = PropertyString(name = "test", value = "updated")
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is PropertyStoreEvent.Updated)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Deleted event when property is deleted`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<PropertyStoreEvent>()

        // When
        val job = launch {
            store.observeChanges()
                .drop(1) // Skip Created event from setup
                .take(1)
                .collect { events.add(it) }
        }
        store += PropertyString(name = "test", value = "value")
        store -= "test"
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is PropertyStoreEvent.Deleted)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Deleted events when clearing all properties`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<PropertyStoreEvent>()

        // When
        val job = launch {
            store.observeChanges()
                .drop(3) // Skip 3 Created events from setup
                .take(3)
                .collect { events.add(it) }
        }
        store += PropertyString(name = "prop1", value = "value1")
        store += PropertyString(name = "prop2", value = "value2")
        store += PropertyString(name = "prop3", value = "value3")
        store.clear()
        job.join()

        // Then
        assertEquals(3, events.size)
        assertTrue(events.all { it is PropertyStoreEvent.Deleted })
        assertTrue(events.any { it.uid == "prop1" })
        assertTrue(events.any { it.uid == "prop2" })
        assertTrue(events.any { it.uid == "prop3" })
    }

    @Test
    fun `should emit Created events when importing properties`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<PropertyStoreEvent>()

        val propertiesToImport = listOf(
            PropertyString(name = "new1", value = "value1"),
            PropertyString(name = "new2", value = "value2"),
        )

        // When
        val job = launch {
            store.observeChanges().take(2).collect { events.add(it) }
        }
        store.importProperties(propertiesToImport)
        job.join()

        // Then
        assertEquals(2, events.size)
        assertTrue(events.all { it is PropertyStoreEvent.Created })
        assertTrue(events.any { it.uid == "new1" })
        assertTrue(events.any { it.uid == "new2" })
    }

    @Test
    fun `should emit Updated event when importing existing property`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<PropertyStoreEvent>()

        val propertiesToImport = listOf(
            PropertyString(name = "existing", value = "updated"),
        )

        // When
        val job = launch {
            store.observeChanges()
                .drop(1) // Skip Created event from setup
                .take(1)
                .collect { events.add(it) }
        }
        store += PropertyString(name = "existing", value = "original")
        store.importProperties(propertiesToImport)
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is PropertyStoreEvent.Updated)
        assertEquals("existing", events[0].uid)
    }

    @Test
    fun `should not throw when creating schema`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        store.createSchema()
    }
}
