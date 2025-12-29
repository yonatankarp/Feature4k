# Feature4k

![CI](https://github.com/yonatankarp/Feature4k/actions/workflows/ci.yml/badge.svg)

A Kotlin Multiplatform library for feature toggling and A/B testing, inspired by [FF4J](https://ff4j.org/).

Feature4k brings comprehensive feature management to the Kotlin ecosystem, working seamlessly across JVM, Android, iOS, Desktop, and Native platforms.

## What is Feature Toggle?

Feature toggles (also known as feature flags) let you enable or disable features at runtime without redeploying your application. This allows you to:

- Deploy code to production with features turned off
- Test features in production with specific users
- Perform gradual rollouts and A/B testing
- Quickly disable problematic features without rollback

## Core Capabilities

- **Feature Toggling**: Toggle features on and off at runtime. Support for multiple code paths protected by dynamic predicates.

- **Role-based Access**: Control feature access based on user roles and groups. Perfect for canary releases, beta testing, and gradual rollouts.

- **Strategy-based Activation**: Use custom predicates to determine feature availability. Built-in support for time-based releases, date ranges, percentage rollouts, and custom expressions.

- **Monitoring & Metrics**: Track feature usage, measure adoption rates, and analyze user behavior over time.

- **Audit Trail**: Every feature change is logged—who toggled what, when, and why. Essential for compliance and troubleshooting.

- **Web Console**: Manage features, properties, and monitoring through a web interface. Built with Ktor for the server side, with potential Compose Web support in the future.

- **Flexible Storage**: Choose your backend: in-memory (default), SQL databases, Redis, MongoDB, Neo4j, or build your own adapter.

## Architecture

The library is modular—use only what you need:

- **feature4k-core** — Core feature toggling API and abstractions
- **feature4k-dsl** — Kotlin DSL for idiomatic configuration
- **feature4k-web** — Web console for feature management

## Platform Support

Feature4k runs everywhere Kotlin does:

- JVM (server-side applications, desktop)
- Android
- iOS (arm64, x64, simulator)
- macOS (Apple Silicon and Intel)
- Linux (x64)
- Windows (MinGW x64)

## Getting Started

Build the project:

```bash
./gradlew assemble
```

Run tests:

```bash
./gradlew allTests
```

---
*Inspired by [FF4J](https://github.com/ff4j/ff4j).*
