package com.yonatankarp.feature4k.core

import kotlinx.serialization.Serializable

/**
 * Execution context for feature evaluation.
 *
 * Carries contextual information (user, source, host, custom parameters) that can be used
 * by flipping strategies to determine if a feature should be enabled.
 *
 * @property user Optional user identifier or principal
 * @property source Optional source identifier (e.g., application name, client ID, API source)
 * @property host Optional host identifier (e.g., hostname, server instance)
 * @property customParams Additional custom parameters for strategy evaluation
 * @author Yonatan Karp-Rudin
 */
@Serializable
data class FlippingExecutionContext(
    val user: String? = null,
    val source: String? = null,
    val host: String? = null,
    val customParams: Map<String, String> = emptyMap(),
) {
    /**
     * Create a new FlippingExecutionContext with the user field set to the provided identifier.
     *
     * @param user The user identifier to set on the new context.
     * @return A new FlippingExecutionContext with `user` updated and all other properties preserved.
     */
    fun withUser(user: String): FlippingExecutionContext = copy(user = user)

    /**
     * Create a new context with the specified source identifier.
     *
     * @param source The source identifier to set on the returned context.
     * @return A new FlippingExecutionContext with `source` set to the provided value.
     */
    fun withSource(source: String): FlippingExecutionContext = copy(source = source)

    /**
 * Creates a new execution context with the host set to the given identifier.
 *
 * @param host The host identifier to set in the new context.
 * @return A FlippingExecutionContext with the host set to the provided identifier.
 */
    fun withHost(host: String): FlippingExecutionContext = copy(host = host)

    /**
     * Create a copy of the context with the given custom parameter added.
     *
     * @param key The custom parameter name.
     * @param value The custom parameter value.
     * @return A new FlippingExecutionContext whose `customParams` contains the given key mapped to the given value (overwriting any existing value for that key).
     */
    fun withParam(
        key: String,
        value: String,
    ): FlippingExecutionContext = copy(customParams = customParams + (key to value))

    /**
     * Create a new context with additional custom parameters.
     *
     * @param params Map of parameters to merge into the context's customParams. Entries in `params` override existing keys.
     * @return A FlippingExecutionContext whose `customParams` contains the original entries merged with `params`.
     */
    fun withParams(params: Map<String, String>): FlippingExecutionContext = copy(customParams = customParams + params)

    /**
     * Retrieve the custom parameter value for the given key.
     *
     * @param key The parameter name to look up.
     * @return The parameter value associated with `key`, or `null` if no such parameter exists.
     */
    operator fun get(key: String): String? = customParams[key]

    /**
     * Determines whether a custom parameter with the given key is present in the context.
     *
     * @param key The name of the custom parameter to check.
     * @return `true` if the parameter is present, `false` otherwise.
     */
    fun hasParam(key: String): Boolean = customParams.containsKey(key)

    companion object {
        /**
 * Create an empty FlippingExecutionContext.
 *
 * @return A FlippingExecutionContext whose `user`, `source`, and `host` are `null` and whose `customParams` is an empty map.
 */
        fun empty(): FlippingExecutionContext = FlippingExecutionContext()
    }
}