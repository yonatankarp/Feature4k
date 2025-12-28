package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import com.yonatankarp.feature4k.core.FlippingExecutionContext
import com.yonatankarp.feature4k.store.FeatureStore
import com.yonatankarp.feature4k.store.InMemoryFeatureStore

/**
 * Test fixtures for FlippingStrategy evaluation.
 * These can be reused across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object StrategyFixtures {
    /**
     * Creates an empty FlippingExecutionContext for testing.
     *
     * @return An empty FlippingExecutionContext with no user, client, server, or custom params
     */
    fun emptyExecutionContext() = FlippingExecutionContext.empty()

    /**
     * Creates a FlippingExecutionContext with a user for testing.
     *
     * @param user The user identifier (default: "test-user", can be null)
     * @param client Optional client identifier
     * @param server Optional server identifier
     * @param customParams Custom parameters map (default: empty)
     * @return A FlippingExecutionContext configured for testing
     */
    fun executionContextWithUser(
        user: String? = "test-user",
        client: String? = null,
        server: String? = null,
        customParams: Map<String, String> = emptyMap(),
    ) = FlippingExecutionContext(
        user = user,
        client = client,
        server = server,
        customParams = customParams,
    )

    /**
     * Creates a FeatureEvaluationContext for testing strategy evaluation.
     *
     * This fixture provides a convenient way to create evaluation contexts with
     * sensible defaults for testing. Each parameter can be customized as needed.
     *
     * @param featureName The name of the feature being evaluated (default: "test-feature")
     * @param store The feature store to use (default: new InMemoryFeatureStore instance)
     * @param context The flipping execution context (default: empty context)
     * @return A FeatureEvaluationContext configured for testing
     */
    fun featureEvaluationContext(
        featureName: String = "test-feature",
        store: FeatureStore = InMemoryFeatureStore(),
        context: FlippingExecutionContext = emptyExecutionContext(),
    ) = FeatureEvaluationContext(
        featureName = featureName,
        store = store,
        context = context,
    )
}
