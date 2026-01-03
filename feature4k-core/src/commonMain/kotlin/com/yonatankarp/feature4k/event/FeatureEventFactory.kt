package com.yonatankarp.feature4k.event

import com.yonatankarp.feature4k.utils.Uid
import kotlinx.datetime.Clock

/**
 * Factory for creating feature store events with common audit metadata.
 *
 * This factory provides a centralized way to create feature events with consistent
 * audit metadata (source, user, hostname) across the application.
 *
 * Example usage:
 * ```kotlin
 * val factory = FeatureEventFactory(source = "WEB_API", user = "admin")
 * val event = factory.created("my-feature-uid")
 * ```
 *
 * @property source Default source system (e.g., "WEB_API", "JAVA_API")
 * @property user Default user who triggered events
 * @property host Default hostname where events are generated
 * @author Yonatan Karp-Rudin
 */
class FeatureEventFactory(
    val source: String? = null,
    val user: String? = null,
    val host: String? = null,
) {
    /**
     * Creates a FeatureCreated event.
     *
     * @param uid The feature identifier (maps to Feature.uid)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun created(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): FeatureStoreEvent.Created = FeatureStoreEvent.Created(
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
     * Creates a FeatureUpdated event.
     *
     * @param uid The feature identifier (maps to Feature.uid)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun updated(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): FeatureStoreEvent.Updated = FeatureStoreEvent.Updated(
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
     * Creates a FeatureDeleted event.
     *
     * @param uid The feature identifier (maps to Feature.uid)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun deleted(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): FeatureStoreEvent.Deleted = FeatureStoreEvent.Deleted(
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
     * Creates a FeatureEnabled event.
     *
     * @param uid The feature identifier (maps to Feature.uid)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun enabled(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): FeatureStoreEvent.Enabled = FeatureStoreEvent.Enabled(
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
     * Creates a FeatureDisabled event.
     *
     * @param uid The feature identifier (maps to Feature.uid)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun disabled(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): FeatureStoreEvent.Disabled = FeatureStoreEvent.Disabled(
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
     * Creates a FeatureChecked event.
     *
     * @param uid The feature identifier (maps to Feature.uid)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun checked(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): FeatureStoreEvent.Checked = FeatureStoreEvent.Checked(
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
     * Creates a FeatureRoleUpdated event.
     *
     * @param uid The feature identifier (maps to Feature.uid)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun roleUpdated(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): FeatureStoreEvent.RoleUpdated = FeatureStoreEvent.RoleUpdated(
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
     * Creates a FeatureRoleDeleted event.
     *
     * @param uid The feature identifier (maps to Feature.uid)
     * @param value Optional additional value
     * @param duration Optional operation duration in milliseconds
     * @param customProperties Additional custom metadata
     */
    fun roleDeleted(
        uid: String,
        value: String? = null,
        duration: Long? = null,
        customProperties: Map<String, String> = emptyMap(),
    ): FeatureStoreEvent.RoleDeleted = FeatureStoreEvent.RoleDeleted(
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
        val Default = FeatureEventFactory()
    }
}
