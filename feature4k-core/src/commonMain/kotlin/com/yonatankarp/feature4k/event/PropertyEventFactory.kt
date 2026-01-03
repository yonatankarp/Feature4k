package com.yonatankarp.feature4k.event

import com.yonatankarp.feature4k.utils.Uid
import kotlinx.datetime.Clock

/**
 * Factory for creating property store events with common audit metadata.
 *
 * This factory provides a centralized way to create property events with consistent
 * audit metadata (source, user, hostname) across the application.
 *
 * Example usage:
 * ```kotlin
 * val factory = PropertyEventFactory(source = "WEB_API", user = "admin")
 * val event = factory.created("my-property-name")
 * ```
 *
 * @property source Default source system (e.g., "WEB_API", "JAVA_API")
 * @property user Default user who triggered events
 * @property host Default hostname where events are generated
 * @author Yonatan Karp-Rudin
 */
class PropertyEventFactory(
    val source: String? = null,
    val user: String? = null,
    val host: String? = null,
) {
    /**
     * Creates a PropertyCreated event.
     *
     * @param uid The property identifier (property name)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun created(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): PropertyStoreEvent.Created = PropertyStoreEvent.Created(
        uid = uid,
        eventUid = Uid.generate(),

        timestamp = Clock.System.now(),
        source = source,
        user = user,
        host = host,
        value = value,
        duration = duration,
        customProperties = customProperties,
    )

    /**
     * Creates a PropertyUpdated event.
     *
     * @param uid The property identifier (property name)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun updated(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): PropertyStoreEvent.Updated = PropertyStoreEvent.Updated(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = Clock.System.now(),
        source = source,
        user = user,
        host = host,
        value = value,
        duration = duration,
        customProperties = customProperties,
    )

    /**
     * Creates a PropertyDeleted event.
     *
     * @param uid The property identifier (property name)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun deleted(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): PropertyStoreEvent.Deleted = PropertyStoreEvent.Deleted(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = Clock.System.now(),
        source = source,
        user = user,
        host = host,
        value = value,
        duration = duration,
        customProperties = customProperties,
    )

    companion object {
        /**
         * Default event factory with no audit metadata.
         */
        val Default = PropertyEventFactory()
    }
}
