package com.yonatankarp.feature4k.core

import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.REGION_US_EAST
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for FlippingExecutionContext class.
 *
 * @author Yonatan Karp-Rudin
 */
class FlippingExecutionContextTest {
    @Test
    fun `empty context has all null fields`() {
        with(FlippingExecutionContext()) {
            assertNull(user)
            assertNull(client)
            assertNull(server)
            assertTrue(customParams.isEmpty())
        }
    }

    @Test
    fun `empty companion creates empty context`() {
        with(FlippingExecutionContext.empty()) {
            assertNull(user)
            assertNull(client)
            assertNull(server)
            assertTrue(customParams.isEmpty())
        }
    }

    @Test
    fun `withUser creates new context with user`() {
        with(FlippingExecutionContext().withUser("john.doe")) {
            assertEquals("john.doe", user)
            assertNull(client)
            assertNull(server)
        }
    }

    @Test
    fun `withClient creates new context with client`() {
        with(FlippingExecutionContext().withClient("mobile-app")) {
            assertEquals("mobile-app", client)
            assertNull(user)
            assertNull(server)
        }
    }

    @Test
    fun `withServer creates new context with server`() {
        with(FlippingExecutionContext().withServer("server-01")) {
            assertEquals("server-01", server)
            assertNull(user)
            assertNull(client)
        }
    }

    @Test
    fun `withParam adds custom parameter`() {
        val context = FlippingExecutionContext().withParam("region", REGION_US_EAST)
        assertEquals(REGION_US_EAST, context.getParam("region"))
    }

    @Test
    fun `withParams adds multiple custom parameters`() {
        val params = mapOf("region" to REGION_US_EAST, "env" to "prod")
        with(FlippingExecutionContext().withParams(params)) {
            assertEquals(REGION_US_EAST, getParam("region"))
            assertEquals("prod", getParam("env"))
        }
    }

    @Test
    fun `getParam returns null for non-existent key`() {
        val context = FlippingExecutionContext()
        assertNull(context.getParam("nonexistent"))
    }

    @Test
    fun `hasParam returns true for existing parameter`() {
        val context = FlippingExecutionContext().withParam("key", "value")
        assertTrue(context.hasParam("key"))
    }

    @Test
    fun `hasParam returns false for non-existent parameter`() {
        val context = FlippingExecutionContext()
        assertFalse(context.hasParam("nonexistent"))
    }

    @Test
    fun `chaining methods creates context with multiple fields`() {
        with(
            FlippingExecutionContext()
                .withUser("alice")
                .withClient("web-app")
                .withServer("server-02")
                .withParam("role", "admin")
                .withParam("team", "engineering"),
        ) {
            assertEquals("alice", user)
            assertEquals("web-app", client)
            assertEquals("server-02", server)
            assertEquals("admin", getParam("role"))
            assertEquals("engineering", getParam("team"))
        }
    }

    @Test
    fun `context can be serialized and deserialized`() {
        val original =
            FlippingExecutionContext(
                user = "bob",
                client = "api-client",
                server = "server-03",
                customParams = mapOf("env" to "staging", "version" to "1.2.3"),
            )

        val json = Json.encodeToString(FlippingExecutionContext.serializer(), original)
        val deserialized = Json.decodeFromString(FlippingExecutionContext.serializer(), json)

        assertEquals(original, deserialized)
    }

    @Test
    fun `withParam preserves existing parameters`() {
        with(
            FlippingExecutionContext()
                .withParam("key1", "value1")
                .withParam("key2", "value2"),
        ) {
            assertEquals("value1", getParam("key1"))
            assertEquals("value2", getParam("key2"))
        }
    }

    @Test
    fun `withParams preserves existing parameters`() {
        with(
            FlippingExecutionContext()
                .withParam("key1", "value1")
                .withParams(mapOf("key2" to "value2", "key3" to "value3")),
        ) {
            assertEquals("value1", getParam("key1"))
            assertEquals("value2", getParam("key2"))
            assertEquals("value3", getParam("key3"))
        }
    }
}
