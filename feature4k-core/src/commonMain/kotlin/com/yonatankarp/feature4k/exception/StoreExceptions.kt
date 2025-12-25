package com.yonatankarp.feature4k.exception

import com.yonatankarp.feature4k.exception.Feature4kException.StoreException

/**
 * Thrown when a store operation fails due to an underlying storage issue.
 *
 * @property operation The operation that failed (e.g., "read", "write", "delete")
 * @author Yonatan Karp-Rudin
 */
class StoreOperationException(
    val operation: String,
    message: String? = null,
    cause: Throwable? = null,
) : StoreException(message ?: "Store operation '$operation' failed", cause)

/**
 * Thrown when a store connection cannot be established or is lost.
 *
 * @author Yonatan Karp-Rudin
 */
class StoreConnectionException(
    message: String = "Failed to connect to store",
    cause: Throwable? = null,
) : StoreException(message, cause)
