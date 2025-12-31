package com.yonatankarp.feature4k.audit

import com.yonatankarp.feature4k.audit.AuditFixtures.ADMIN_USER
import com.yonatankarp.feature4k.audit.AuditFixtures.LOCALHOST
import com.yonatankarp.feature4k.audit.AuditFixtures.WEB_API_SOURCE
import com.yonatankarp.feature4k.core.IdentifierFixtures.FEATURE_UID
import com.yonatankarp.feature4k.core.IdentifierFixtures.PROPERTY_UID
import com.yonatankarp.feature4k.store.FeatureStoreEvent
import com.yonatankarp.feature4k.store.PropertyStoreEvent
import com.yonatankarp.feature4k.utils.Uid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Test fixtures for audit events.
 * These can be reused across different test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object EventFixtures {

    /**
     * Creates a basic FeatureStoreEvent.Created with minimal fields.
     */
    fun featureCreatedEvent(
        uid: String = FEATURE_UID,
        timestamp: Instant = Clock.System.now(),
        user: String? = null,
        source: String? = null,
        host: String? = null,
    ) = FeatureStoreEvent.Created(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = timestamp,
        user = user,
        source = source,
        host = host,
        duration = null,
        value = null,
        customProperties = emptyMap(),
    )

    /**
     * Creates a FeatureStoreEvent.Updated event.
     */
    fun featureUpdatedEvent(
        uid: String = FEATURE_UID,
        timestamp: Instant = Clock.System.now(),
        user: String? = null,
    ) = FeatureStoreEvent.Updated(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = timestamp,
        user = user,
        source = null,
        host = null,
        duration = null,
        value = null,
        customProperties = emptyMap(),
    )

    /**
     * Creates a FeatureStoreEvent.Deleted event.
     */
    fun featureDeletedEvent(
        uid: String = FEATURE_UID,
        timestamp: Instant = Clock.System.now(),
    ) = FeatureStoreEvent.Deleted(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = timestamp,
        user = null,
        source = null,
        host = null,
        duration = null,
        value = null,
        customProperties = emptyMap(),
    )

    /**
     * Creates a FeatureStoreEvent.Enabled event.
     */
    fun featureEnabledEvent(
        uid: String = FEATURE_UID,
        timestamp: Instant = Clock.System.now(),
    ) = FeatureStoreEvent.Enabled(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = timestamp,
        user = null,
        source = null,
        host = null,
        duration = null,
        value = null,
        customProperties = emptyMap(),
    )

    /**
     * Creates a FeatureStoreEvent.Disabled event.
     */
    fun featureDisabledEvent(
        uid: String = FEATURE_UID,
        timestamp: Instant = Clock.System.now(),
    ) = FeatureStoreEvent.Disabled(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = timestamp,
        user = null,
        source = null,
        host = null,
        duration = null,
        value = null,
        customProperties = emptyMap(),
    )

    /**
     * Creates a FeatureStoreEvent.Checked event.
     */
    fun featureCheckedEvent(
        uid: String = FEATURE_UID,
        timestamp: Instant = Clock.System.now(),
        value: String? = "true",
    ) = FeatureStoreEvent.Checked(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = timestamp,
        user = null,
        source = null,
        host = null,
        duration = null,
        value = value,
        customProperties = emptyMap(),
    )

    /**
     * Creates a PropertyStoreEvent.Created event.
     */
    fun propertyCreatedEvent(
        uid: String = PROPERTY_UID,
        timestamp: Instant = Clock.System.now(),
    ) = PropertyStoreEvent.Created(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = timestamp,
        user = null,
        source = null,
        host = null,
        duration = null,
        value = null,
        customProperties = emptyMap(),
    )

    /**
     * Creates an event with custom properties for testing metadata.
     */
    fun eventWithCustomProperties(
        uid: String = FEATURE_UID,
        customProperties: Map<String, String> = mapOf("key1" to "value1", "key2" to "value2"),
    ) = FeatureStoreEvent.Created(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = Clock.System.now(),
        user = null,
        source = null,
        host = null,
        duration = null,
        value = null,
        customProperties = customProperties,
    )

    /**
     * Creates an event with full audit metadata.
     */
    fun fullAuditEvent(
        uid: String = FEATURE_UID,
        timestamp: Instant = Clock.System.now(),
        user: String = ADMIN_USER,
        source: String = WEB_API_SOURCE,
        host: String = LOCALHOST,
        duration: Long = 100L,
    ) = FeatureStoreEvent.Created(
        uid = uid,
        eventUid = Uid.generate(),
        timestamp = timestamp,
        user = user,
        source = source,
        host = host,
        duration = duration,
        value = null,
        customProperties = mapOf("environment" to "test"),
    )

    /**
     * Creates an event with a relative timestamp offset.
     *
     * @param uid The unique identifier for the feature
     * @param offset Duration offset from the base time
     * @param baseTime Base timestamp to apply offset from (defaults to Clock.System.now())
     */
    fun eventWithOffset(
        uid: String = FEATURE_UID,
        offset: Duration,
        baseTime: Instant = Clock.System.now(),
    ) = featureCreatedEvent(
        uid = uid,
        timestamp = baseTime + offset,
    )
}
