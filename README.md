# Feature4k

<div align="center">

[![CI](https://github.com/yonatankarp/Feature4k/actions/workflows/ci.yml/badge.svg)](https://github.com/yonatankarp/Feature4k/actions/workflows/ci.yml)
[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![GitHub release](https://img.shields.io/github/v/release/yonatankarp/Feature4k)](https://github.com/yonatankarp/Feature4k/releases)
[![CodeRabbit Reviews](https://img.shields.io/coderabbit/prs/github/yonatankarp/Feature4k?utm_source=oss&utm_medium=github&utm_campaign=yonatankarp%2FFeature4k&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)](https://coderabbit.ai)

### Code Quality

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_Feature4k&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=yonatankarp_Feature4k)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_Feature4k&metric=coverage)](https://sonarcloud.io/summary/new_code?id=yonatankarp_Feature4k)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_Feature4k&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=yonatankarp_Feature4k)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_Feature4k&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=yonatankarp_Feature4k)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_Feature4k&metric=bugs)](https://sonarcloud.io/summary/new_code?id=yonatankarp_Feature4k)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_Feature4k&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=yonatankarp_Feature4k)

</div>

---

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
