plugins {
    id("feature4k.multiplatform")
    id("feature4k.coverage")
    id("feature4k.documentation")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature4k-core"))
        }
    }
}
