plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.multiplatformCommon.date)
            implementation(projects.multiplatformCommon.serialization)
        }
    }
}