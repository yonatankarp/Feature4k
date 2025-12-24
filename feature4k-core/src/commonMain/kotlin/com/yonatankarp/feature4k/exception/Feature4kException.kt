package com.yonatankarp.feature4k.exception

/**
 * Base sealed class for all Feature4k exceptions.
 *
 * This hierarchy allows for type-safe exception handling and provides
 * domain-specific error information throughout the feature flag system.
 */
sealed class Feature4kException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Base class for feature-related exceptions
     */
    sealed class FeatureException(message: String, cause: Throwable? = null) : Feature4kException(message, cause)

    /**
     * Base class for property-related exceptions
     */
    sealed class PropertyException(message: String, cause: Throwable? = null) : Feature4kException(message, cause)

    /**
     * Base class for group-related exceptions
     */
    sealed class GroupException(message: String, cause: Throwable? = null) : Feature4kException(message, cause)

    /**
     * Base class for authorization-related exceptions
     */
    sealed class AuthorizationException(message: String, cause: Throwable? = null) : Feature4kException(message, cause)

    /**
     * Base class for store-related exceptions
     */
    sealed class StoreException(message: String, cause: Throwable? = null) : Feature4kException(message, cause)
}
