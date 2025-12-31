package com.yonatankarp.feature4k.strategy

import com.yonatankarp.feature4k.core.FeatureEvaluationContext
import com.yonatankarp.feature4k.core.FlippingExecutionContext
import com.yonatankarp.feature4k.core.IdentifierFixtures.FEATURE_UID
import com.yonatankarp.feature4k.store.FeatureStore
import com.yonatankarp.feature4k.store.InMemoryFeatureStore
import com.yonatankarp.feature4k.strategy.FlippingExecutionContextFixture.emptyExecutionContext

object FeatureEvaluationContextFixture {
    /**
     * Creates a FeatureEvaluationContext for testing strategy evaluation.
     *
     * This fixture provides a convenient way to create evaluation contexts with
     * sensible defaults for testing. Each parameter can be customized as needed.
     *
     * @param featureName The name of the feature being evaluated (default: [FEATURE_UID])
     * @param store The feature store to use (default: new InMemoryFeatureStore instance)
     * @param context The flipping execution context (default: empty context)
     * @return A FeatureEvaluationContext configured for testing
     */
    fun featureEvaluationContext(
        featureName: String = FEATURE_UID,
        store: FeatureStore = InMemoryFeatureStore(),
        context: FlippingExecutionContext = emptyExecutionContext(),
    ) = FeatureEvaluationContext(
        featureName = featureName,
        store = store,
        context = context,
    )
}
