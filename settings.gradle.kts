pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Feature4k"

include(
    ":feature4k-core",
    ":feature4k-dsl",
    ":feature4k-test",
    ":feature4k-web",
)
