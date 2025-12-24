# ✨✨✨ Feature4k - Feature Flipping for Kotlin Multiplatform ✨✨✨

**Feature4k** is a Kotlin Multiplatform port of the popular [FF4J](https://ff4j.org/) library. It brings the power of feature toggling and property management to the entire Kotlin ecosystem, enabling consistent feature management across Server (JVM), Mobile (Android, iOS), and Desktop/Native applications.

## 🚀 Vision

Feature4k aims to provide the same comprehensive feature set as FF4J but redesigned for the modern, multiplatform Kotlin world.

## 🤘 Key Features (Planned & In Progress)

### 🚦 Feature Toggling
Enable and disable features at runtime without deployments. Implement multiple code paths protected by dynamic predicates.
*Status: 🚧 Skeleton Ready*

### 👮 Role-based Toggling
Control feature access not just by flags but by user roles and groups (e.g., Canary Releases, Beta Testers).
*Status: 📅 Planned*

### 🧠 Strategy-based Toggling
Implement custom predicates (Strategy Pattern) to evaluate if a feature is enabled (e.g., Time-based, Release Date, Expression-based).
*Status: 📅 Planned*

### 📊 Features Monitoring
Collect and record events and metrics to compute dashboards and analyze feature usage over time.
*Status: 📅 Planned*

### 📝 Audit Trail
Trace every action (create, update, delete, toggle) for troubleshooting and security auditing.
*Status: 📅 Planned*

### 🖥️ Web Console (KMP Ready)
Administrate Feature4k (features, properties, monitoring) via a web UI.
* **Server-side**: Ktor-based HTML console (Planned)
* **Client-side**: Compose Web / Kotlin/JS (Potential future)

### 💾 Wide Choice of Stores
Just like FF4J, Feature4k is designed to support multiple storage backends. The architecture allows you to pick only what you need.
* **In-Memory**: Default implementation
* **Databases**: SQL, NoSQL (Redis, Mongo, Neo4j, etc.) - *Coming soon*

## 📦 Modular Architecture

Feature4k is built with modularity in mind. You only include what you use.

- **`feature4k-core`**: The core API.
- **`feature4k-dsl`**: Kotlin idiomatic DSL for configuration.
- **`feature4k-web`**: Server-side Web Console.
- **`feature4k-test`**: Shared test fixtures.

## 🛠 Supported Platforms

- **JVM** (Server, Desktop)
- **Android** (Native)
- **iOS** (Arm64, X64, Simulator)
- **Native** (Linux, Windows, macOS)

## 🔨 Getting Started

### Building the Project
```bash
./gradlew assemble
```

## 🚀 CI/CD

### Continuous Integration

![CI](https://github.com/yonatankarp/Feature4k/actions/workflows/ci.yml/badge.svg)

Every pull request and push to the `main` branch automatically triggers:
- Build for all supported platforms (JVM, linuxX64)
- Test execution for all platforms
- Gradle dependency caching for faster builds

### Publishing Releases

Releases are automatically published to GitHub Packages when you push a version tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The release workflow will:
- Extract version from the tag (e.g., `v1.0.0` → `1.0.0`)
- Build all platform artifacts
- Sign artifacts with GPG (if configured)
- Publish to GitHub Packages
- Create a GitHub Release with auto-generated release notes

### Required Secrets

To enable release publishing with GPG signing, configure these repository secrets:

- `GPG_PRIVATE_KEY`: GPG private key for artifact signing (optional)
- `GPG_PASSPHRASE`: GPG key passphrase (optional)

The `GITHUB_TOKEN` is automatically provided by GitHub Actions.

---
*Inspired by [FF4J](https://github.com/ff4j/ff4j).*