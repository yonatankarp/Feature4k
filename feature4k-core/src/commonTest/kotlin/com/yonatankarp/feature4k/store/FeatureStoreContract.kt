package com.yonatankarp.feature4k.store

import com.yonatankarp.feature4k.core.Feature
import com.yonatankarp.feature4k.exception.FeatureAlreadyExistException
import com.yonatankarp.feature4k.exception.FeatureNotFoundException
import com.yonatankarp.feature4k.exception.GroupNotFoundException
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
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
 * Abstract test contract for FeatureStore implementations.
 *
 * All FeatureStore implementations must extend this class and implement [createStore]
 * to ensure consistent behavior across different store implementations.
 *
 * @author Yonatan Karp-Rudin
 */
abstract class FeatureStoreContract {
    /**
     * Create a fresh instance of the FeatureStore implementation to test.
     * This method is called before each test to ensure test isolation.
     */
    abstract suspend fun createStore(): FeatureStore

    @Test
    fun `should return false for contains when feature does not exist`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFalse("non-existent" in store)
    }

    @Test
    fun `should create feature using plusAssign operator`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "test", enabled = true)

        // When
        store += feature

        // Then
        assertTrue("test" in store)
        assertEquals(feature, store["test"])
    }

    @Test
    fun `should throw FeatureAlreadyExistException when creating duplicate feature`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "test", enabled = true)
        store += feature

        // When & Then
        assertFailsWith<FeatureAlreadyExistException> {
            store += feature
        }
    }

    @Test
    fun `should return null when getting non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When
        val result = store["non-existent"]

        // Then
        assertNull(result)
    }

    @Test
    fun `should return feature after creation`() = runTest {
        // Given
        val store = createStore()
        val feature =
            Feature(uid = "test", enabled = true, description = "Test feature")

        // When
        store += feature

        // Then
        val retrieved = store["test"]
        assertNotNull(retrieved)
        assertEquals("test", retrieved.uid)
        assertEquals(true, retrieved.enabled)
        assertEquals("Test feature", retrieved.description)
    }

    @Test
    fun `should create feature using set operator when not exists`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "test", enabled = true)

        // When
        store["test"] = feature

        // Then
        assertTrue("test" in store)
        assertEquals(feature, store["test"])
    }

    @Test
    fun `should update feature using set operator when exists`() = runTest {
        // Given
        val store = createStore()
        val original =
            Feature(uid = "test", enabled = false, description = "Original")
        store += original

        // When
        val updated =
            Feature(uid = "test", enabled = true, description = "Updated")
        store["test"] = updated

        // Then
        val retrieved = store["test"]
        assertNotNull(retrieved)
        assertEquals(true, retrieved.enabled)
        assertEquals("Updated", retrieved.description)
    }

    @Test
    fun `should delete feature using minusAssign operator`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "test", enabled = true)
        store += feature

        // When
        store -= "test"

        // Then
        assertFalse("test" in store)
    }

    @Test
    fun `should throw FeatureNotFoundException when deleting non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            store -= "non-existent"
        }
    }

    @Test
    fun `should return empty map when no features exist`() = runTest {
        // Given
        val store = createStore()

        // When
        val result = store.getAll()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return all features`() = runTest {
        // Given
        val store = createStore()
        val feature1 = Feature(uid = "feature1", enabled = true)
        val feature2 = Feature(uid = "feature2", enabled = false)
        val feature3 = Feature(uid = "feature3", enabled = true)
        store += feature1
        store += feature2
        store += feature3

        // When
        val all = store.getAll()

        // Then
        assertEquals(3, all.size)
        assertTrue("feature1" in all)
        assertTrue("feature2" in all)
        assertTrue("feature3" in all)
    }

    @Test
    fun `should remove all features when clearing`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "feature1", enabled = true)
        store += Feature(uid = "feature2", enabled = false)

        // When
        store.clear()

        // Then
        assertTrue(store.getAll().isEmpty())
    }

    @Test
    fun `should enable disabled feature`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "test", enabled = false)
        store += feature

        // When
        store.enable("test")

        // Then
        val retrieved = store["test"]
        assertNotNull(retrieved)
        assertTrue(retrieved.enabled)
    }

    @Test
    fun `should throw FeatureNotFoundException when enabling non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            store.enable("non-existent")
        }
    }

    @Test
    fun `should disable enabled feature`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "test", enabled = true)
        store += feature

        // When
        store.disable("test")

        // Then
        val retrieved = store["test"]
        assertNotNull(retrieved)
        assertFalse(retrieved.enabled)
    }

    @Test
    fun `should throw FeatureNotFoundException when disabling non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            store.disable("non-existent")
        }
    }

    @Test
    fun `should return false when checking for non-existent group`() = runTest {
        // Given
        val store = createStore()

        // When
        val result = store.existsGroup("non-existent")

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return true when group has features`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "test", enabled = true, group = "mygroup")
        store += feature

        // When
        val result = store.existsGroup("mygroup")

        // Then
        assertTrue(result)
    }

    @Test
    fun `should throw GroupNotFoundException when getting non-existent group`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<GroupNotFoundException> {
            store.getGroup("non-existent")
        }
    }

    @Test
    fun `should return all features in group`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "feature1", enabled = true, group = "group1")
        store += Feature(uid = "feature2", enabled = false, group = "group1")
        store += Feature(uid = "feature3", enabled = true, group = "group2")

        // When
        val group1Features = store.getGroup("group1")

        // Then
        assertEquals(2, group1Features.size)
        assertTrue("feature1" in group1Features)
        assertTrue("feature2" in group1Features)
        assertFalse("feature3" in group1Features)
    }

    @Test
    fun `should add feature to group`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "test", enabled = true)

        // When
        store.addToGroup("test", "mygroup")

        // Then
        val feature = store["test"]
        assertNotNull(feature)
        assertEquals("mygroup", feature.group)
    }

    @Test
    fun `should throw FeatureNotFoundException when adding non-existent feature to group`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            store.addToGroup("non-existent", "mygroup")
        }
    }

    @Test
    fun `should remove feature from group`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "test", enabled = true, group = "mygroup")

        // When
        store.removeFromGroup("test", "mygroup")

        // Then
        val feature = store["test"]
        assertNotNull(feature)
        assertNull(feature.group)
    }

    @Test
    fun `should fail to remove group if feature is not in group`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "test", enabled = true, group = "mygroup")

        // When / Then
        assertFailsWith<GroupNotFoundException> {
            store.removeFromGroup("test", "anotherGroup")
        }
    }

    @Test
    fun `should throw FeatureNotFoundException when removing non-existent feature from group`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            store.removeFromGroup("non-existent", "mygroup")
        }
    }

    @Test
    fun `should enable all features in group`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "feature1", enabled = false, group = "mygroup")
        store += Feature(uid = "feature2", enabled = false, group = "mygroup")
        store += Feature(uid = "feature3", enabled = false, group = "other")

        // When
        store.enableGroup("mygroup")

        // Then
        assertTrue(store["feature1"]!!.enabled)
        assertTrue(store["feature2"]!!.enabled)
        assertFalse(store["feature3"]!!.enabled)
    }

    @Test
    fun `should throw GroupNotFoundException when enabling non-existent group`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<GroupNotFoundException> {
            store.enableGroup("non-existent")
        }
    }

    @Test
    fun `should disable all features in group`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "feature1", enabled = true, group = "mygroup")
        store += Feature(uid = "feature2", enabled = true, group = "mygroup")
        store += Feature(uid = "feature3", enabled = true, group = "other")

        // When
        store.disableGroup("mygroup")

        // Then
        assertFalse(store["feature1"]!!.enabled)
        assertFalse(store["feature2"]!!.enabled)
        assertTrue(store["feature3"]!!.enabled)
    }

    @Test
    fun `should throw GroupNotFoundException when disabling non-existent group`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<GroupNotFoundException> {
            store.disableGroup("non-existent")
        }
    }

    @Test
    fun `should return empty set when no groups exist`() = runTest {
        // Given
        val store = createStore()

        // When
        val result = store.getAllGroups()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return all unique group names`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "feature1", enabled = true, group = "group1")
        store += Feature(uid = "feature2", enabled = false, group = "group1")
        store += Feature(uid = "feature3", enabled = true, group = "group2")
        store += Feature(uid = "feature4", enabled = true)

        // When
        val groups = store.getAllGroups()

        // Then
        assertEquals(2, groups.size)
        assertTrue("group1" in groups)
        assertTrue("group2" in groups)
    }

    @Test
    fun `should grant role to feature`() = runTest {
        // Given
        val store = createStore()
        store += Feature(uid = "test", enabled = true)

        // When
        store.grantRoleOnFeature("test", "ROLE_ADMIN")

        // Then
        val feature = store["test"]
        assertNotNull(feature)
        assertTrue("ROLE_ADMIN" in feature.permissions)
    }

    @Test
    fun `should throw FeatureNotFoundException when granting role to non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            store.grantRoleOnFeature("non-existent", "ROLE_ADMIN")
        }
    }

    @Test
    fun `should remove role from feature`() = runTest {
        // Given
        val store = createStore()
        store += Feature(
            uid = "test",
            enabled = true,
            permissions = setOf("ROLE_ADMIN", "ROLE_USER"),
        )

        // When
        store.removeRoleFromFeature("test", "ROLE_ADMIN")

        // Then
        val feature = store["test"]
        assertNotNull(feature)
        assertFalse("ROLE_ADMIN" in feature.permissions)
        assertTrue("ROLE_USER" in feature.permissions)
    }

    @Test
    fun `should throw FeatureNotFoundException when removing role from non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When & Then
        assertFailsWith<FeatureNotFoundException> {
            store.removeRoleFromFeature("non-existent", "ROLE_ADMIN")
        }
    }

    @Test
    fun `should import multiple features`() = runTest {
        // Given
        val store = createStore()
        val features =
            listOf(
                Feature(uid = "feature1", enabled = true),
                Feature(uid = "feature2", enabled = false),
                Feature(uid = "feature3", enabled = true),
            )

        // When
        store.importFeatures(features)

        // Then
        assertEquals(3, store.getAll().size)
        assertTrue("feature1" in store)
        assertTrue("feature2" in store)
        assertTrue("feature3" in store)
    }

    @Test
    fun `should overwrite existing features when importing`() = runTest {
        // Given
        val store = createStore()
        store += Feature(
            uid = "feature1",
            enabled = false,
            description = "Original",
        )
        val features =
            listOf(
                Feature(
                    uid = "feature1",
                    enabled = true,
                    description = "Updated",
                ),
                Feature(uid = "feature2", enabled = true),
            )

        // When
        store.importFeatures(features)

        // Then
        val feature1 = store["feature1"]
        assertNotNull(feature1)
        assertTrue(feature1.enabled)
        assertEquals("Updated", feature1.description)
    }

    @Test
    fun `should emit Created event when feature is created`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "test", enabled = true)
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges().take(1).toList(events)
            }
        store += feature
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is FeatureStoreEvent.Created)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Enabled event when feature is enabled`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }
        store += Feature(uid = "test", enabled = false)
        store.enable("test")
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is FeatureStoreEvent.Enabled)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Disabled event when feature is disabled`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }
        store += Feature(uid = "test", enabled = true)
        store.disable("test")
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is FeatureStoreEvent.Disabled)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Updated event when feature is updated`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }
        store += Feature(uid = "test", enabled = true)
        store["test"] = Feature(uid = "test", enabled = false)
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is FeatureStoreEvent.Updated)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Deleted event when feature is deleted`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }
        store += Feature(uid = "test", enabled = true)
        store -= "test"
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is FeatureStoreEvent.Deleted)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit RoleUpdated event when feature role is updated`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }
        store += Feature(uid = "test", enabled = true)
        store.grantRoleOnFeature(featureId = "test", roleName = "testRole")
        job.join()

        // Then
        assertEquals(1, events.size)
        val event = events[0] as? FeatureStoreEvent.RoleUpdated
        assertNotNull(event)
        assertEquals("test", event.uid)
    }

    @Test
    fun `should emit RoleDeleted event when feature role is removed`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }

        store += Feature(
            uid = "test",
            enabled = true,
            permissions = setOf("testRole"),
        )
        store.removeRoleFromFeature(
            featureId = "test",
            roleName = "testRole",
        )
        job.join()

        // Then
        assertEquals(1, events.size)
        val event = events[0] as? FeatureStoreEvent.RoleDeleted
        assertNotNull(event)
        assertEquals("test", event.uid)
    }

    @Test
    fun `should emit Enabled events when group is enabled`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(3) // Skip 3 Created events from setup
                    .take(2)
                    .toList(events)
            }
        store += Feature(uid = "feature1", enabled = false, group = "mygroup")
        store += Feature(uid = "feature2", enabled = false, group = "mygroup")
        store += Feature(
            uid = "feature3",
            enabled = false,
            group = "othergroup",
        )
        store.enableGroup("mygroup")
        job.join()

        // Then
        assertEquals(2, events.size)
        assertTrue(events.all { it is FeatureStoreEvent.Enabled })
        assertTrue(events.any { it.uid == "feature1" })
        assertTrue(events.any { it.uid == "feature2" })
    }

    @Test
    fun `should emit Disabled events when group is disabled`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(3) // Skip 3 Created events from setup
                    .take(2)
                    .toList(events)
            }
        store += Feature(uid = "feature1", enabled = true, group = "mygroup")
        store += Feature(uid = "feature2", enabled = true, group = "mygroup")
        store += Feature(uid = "feature3", enabled = true, group = "othergroup")
        store.disableGroup("mygroup")
        job.join()

        // Then
        assertEquals(2, events.size)
        assertTrue(
            events.all { it is FeatureStoreEvent.Disabled },
            "Not all events are Disabled",
        )
        assertTrue(
            events.any { it.uid == "feature1" },
            "Feature with id feature1 is missing",
        )
        assertTrue(
            events.any { it.uid == "feature2" },
            "Feature with id feature2 is missing",
        )
    }

    @Test
    fun `should emit Updated event when feature is added to group`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }
        store += Feature(uid = "test", enabled = true)
        store.addToGroup("test", "mygroup")
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is FeatureStoreEvent.Updated)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Updated event when feature is removed from group`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }
        store += Feature(uid = "test", enabled = true, group = "mygroup")
        store.removeFromGroup("test", "mygroup")
        job.join()

        // Then
        assertEquals(1, events.size)
        assertTrue(events[0] is FeatureStoreEvent.Updated)
        assertEquals("test", events[0].uid)
    }

    @Test
    fun `should emit Deleted events when clearing all features`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(3) // Skip 3 Created events from setup
                    .take(3)
                    .toList(events)
            }
        store += Feature(uid = "feature1", enabled = true)
        store += Feature(uid = "feature2", enabled = false)
        store += Feature(uid = "feature3", enabled = true)
        store.clear()
        job.join()

        // Then
        assertEquals(3, events.size)
        assertTrue(
            events.all { it is FeatureStoreEvent.Deleted },
            "Not all events are Deleted",
        )
        assertTrue(
            events.any { it.uid == "feature1" },
            "Feature with id feature1 is missing",
        )
        assertTrue(
            events.any { it.uid == "feature2" },
            "Feature with id feature2 is missing",
        )
        assertTrue(
            events.any { it.uid == "feature3" },
            "Feature with id feature3 is missing",
        )
    }

    @Test
    fun `should emit Created events when importing features`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        val featuresToImport = listOf(
            Feature(uid = "new1", enabled = true),
            Feature(uid = "new2", enabled = false),
        )

        // When
        val job =
            launch {
                store.observeChanges().take(2).toList(events)
            }
        store.importFeatures(featuresToImport)
        job.join()

        // Then - verify Created events for new features
        assertEquals(2, events.size)
        assertTrue(events.all { it is FeatureStoreEvent.Created }, "Not all events are Created")
        assertTrue(events.any { it.uid == "new1" }, "Feature with id new1 is missing")
        assertTrue(events.any { it.uid == "new2" }, "Feature with id new2 is missing")
    }

    @Test
    fun `should emit Updated event when importing existing feature`() = runTest {
        // Given
        val store = createStore()
        val events = mutableListOf<StoreEvent>()

        val featuresToImport = listOf(
            Feature(
                uid = "existing",
                enabled = true,
                description = "Updated",
            ),
        )

        // When
        val job =
            launch {
                store.observeChanges()
                    .drop(1) // Skip Created event from setup
                    .take(1)
                    .toList(events)
            }
        store += Feature(
            uid = "existing",
            enabled = false,
            description = "Original",
        )
        store.importFeatures(featuresToImport)
        job.join()

        // Then - verify Updated event for existing feature
        assertEquals(1, events.size)
        assertTrue(events[0] is FeatureStoreEvent.Updated)
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
