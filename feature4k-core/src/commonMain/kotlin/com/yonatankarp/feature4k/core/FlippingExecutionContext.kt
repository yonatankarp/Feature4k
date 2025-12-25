package com.yonatankarp.feature4k.core

import kotlinx.serialization.Serializable

/**
 * Execution context for feature evaluation.
 *
 * Carries contextual information (user, client, server, custom parameters) that can be used
 * by flipping strategies to determine if a feature should be enabled.
 *
 * @property user Optional user identifier or principal
 * @property client Optional client identifier (e.g., application name, client ID)
 * @property server Optional server identifier (e.g., hostname, server instance)
 * @property customParams Additional custom parameters for strategy evaluation
 */
@Serializable
data class FlippingExecutionContext(
    val user: String? = null,
    val client: String? = null,
    val server: String? = null,
    val customParams: Map<String, String> = emptyMap()
) {
    /**
     * Returns a new context with the specified user.
     */
    fun withUser(user: String): FlippingExecutionContext =
        copy(user = user)

    /**
     * Returns a new context with the specified client.
     */
    fun withClient(client: String): FlippingExecutionContext =
        copy(client = client)

    /**
     * Returns a new context with the specified server.
     */
    fun withServer(server: String): FlippingExecutionContext =
        copy(server = server)

    /**
     * Returns a new context with an additional custom parameter.
     */
    fun withParam(key: String, value: String): FlippingExecutionContext =
        copy(customParams = customParams + (key to value))

    /**
     * Returns a new context with multiple additional custom parameters.
     */
    fun withParams(params: Map<String, String>): FlippingExecutionContext =
        copy(customParams = customParams + params)

    /**
     * Gets a custom parameter value by key.
     */
    fun getParam(key: String): String? = customParams[key]

    /**
     * Checks if a custom parameter exists.
     */
    fun hasParam(key: String): Boolean = customParams.containsKey(key)

    companion object {
        /**
         * Creates an empty execution context.
         */
        fun empty(): FlippingExecutionContext = FlippingExecutionContext()
    }
}
