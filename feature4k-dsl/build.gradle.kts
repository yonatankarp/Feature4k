plugins {
    id("feature4k.multiplatform")
    id("feature4k.coverage")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature4k-core"))
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
