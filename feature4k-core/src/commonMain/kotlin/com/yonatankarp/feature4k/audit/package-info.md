# Audit Package

The `audit` package provides interfaces and contracts for persisting and querying feature and property store events for audit trail and compliance purposes.

## Overview

This package works in conjunction with the `store` package's event system to provide:

1. **Event Publishing** - Async publishing of store events to persistent audit storage
2. **Event Querying** - Flexible querying of historical events by time, entity, and custom filters
3. **Implementation Contracts** - Test contracts ensuring consistent behavior across different storage backends

## Architecture

```
┌─────────────────┐
│  FeatureStore   │ Emits events via StoreEventEmitter
│  PropertyStore  │ (Real-time observation)
└────────┬────────┘
         │
         v
    StoreEvent ──────────┐
         │               │
         v               v
 ┌───────────────┐  ┌──────────────┐
 │EventPublisher │  │ Observers    │
 │(Audit Trail)  │  │ (Cache, etc) │
 └───────┬───────┘  └──────────────┘
         │
         v
 ┌───────────────┐
 │EventRepository│
 │(Persistence)  │
 └───────────────┘
```

## Core Components

### EventPublisher

Interface for publishing store events to audit storage. Implementations should:

- Process events asynchronously to avoid blocking store operations
- Handle errors gracefully without propagating exceptions
- Support backpressure and buffering as needed

**Example Implementation Pattern:**
```kotlin
class AuditEventPublisher(
    private val repository: EventRepository
) : EventPublisher {
    override suspend fun publish(event: StoreEvent) {
        try {
            repository.save(event)
        } catch (e: Exception) {
            logger.error("Failed to publish audit event", e)
            // Don't rethrow - audit failures shouldn't break main operations
        }
    }
}
```

### EventRepository

Interface for persisting and querying audit events. Provides:

- **save(event)** - Persist a single event
- **findByTimeRange(start, end)** - Query events within a time window
- **findByUid(uid)** - Query all events for a specific feature/property
- **findAll()** - Retrieve complete audit history
- **findBy(predicate)** - Custom filtering using predicates

**Query Examples:**
```kotlin
// Find all events for a feature
val featureHistory = repository.findByUid("feature-123")

// Find events in last 24 hours
val recent = repository.findByTimeRange(
    start = Clock.System.now() - 24.hours,
    end = Clock.System.now()
)

// Find all feature creation events by admin
val adminCreations = repository.findBy {
    it is FeatureStoreEvent.Created && it.user == "admin"
}

// Find slow operations
val slowOps = repository.findBy {
    it.duration != null && it.duration > 1000
}
```

### EventRepositoryContract

Abstract test class that defines the behavioral contract for all EventRepository implementations.
Implementations should extend this class to ensure:

- Correct event persistence and retrieval
- Proper time-based filtering
- Accurate entity-based queries
- Chronological ordering of results
- Handling of edge cases (empty results, no matches, etc.)

**Usage:**
```kotlin
class InMemoryEventRepositoryTest : EventRepositoryContract() {
    override suspend fun createRepository() = InMemoryEventRepository()
}
```

## Event Model

The audit system uses `StoreEvent` and its subtypes:

- **FeatureStoreEvent**: Events from feature stores (Created, Updated, Deleted, Enabled, Disabled, Checked, RoleUpdated, RoleDeleted)
- **PropertyStoreEvent**: Events from property stores (Created, Updated, Deleted)

Each event includes rich audit metadata:
- `uid` - Entity identifier (feature/property ID)
- `eventUid` - Unique event identifier
- `timestamp` - When the event occurred
- `user` - Who performed the action
- `source` - Source system (e.g., "WEB_API", "JAVA_API")
- `host` - Hostname where event originated
- `duration` - Operation duration in milliseconds
- `value` - Additional value data
- `customProperties` - Custom metadata map

## Implementation Guidelines

### Repository Implementations

When implementing EventRepository:

1. **Thread Safety**: Use coroutine-safe collections and synchronization
2. **Ordering**: Always return events in chronological order (oldest first)
3. **Time Filtering**: Time ranges should be inclusive (start <= timestamp <= end)
4. **Filtering**: The `findBy` predicate should be applied after time/uid filtering for efficiency
5. **Performance**: Consider indexing by uid and timestamp for common queries

### Publisher Implementations

When implementing EventPublisher:

1. **Non-Blocking**: Never block the caller - use coroutines or async mechanisms
2. **Error Handling**: Log errors but don't propagate exceptions
3. **Buffering**: Consider buffering events for batch writes
4. **Backpressure**: Handle situations where repository is slower than event generation

## Testing

Use the provided fixtures and contracts:

```kotlin
import com.yonatankarp.feature4k.audit.EventFixtures.*

// Test with realistic event data
val event = featureCreatedEvent(uid = "test-feature", user = "admin")
repository.save(event)

// Or create events with specific timestamps
val oldEvent = eventWithOffset("feature1", -2.hours)
val newEvent = eventWithOffset("feature2", 0.hours)
```

## See Also

- `com.yonatankarp.feature4k.store` - Store interfaces and event emission
- `com.yonatankarp.feature4k.audit.emission` - Real-time event observation
- `EventFixtures` in feature4k-test - Test utilities for creating audit events
