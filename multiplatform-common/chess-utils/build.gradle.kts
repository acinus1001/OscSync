plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
        }
    }
}