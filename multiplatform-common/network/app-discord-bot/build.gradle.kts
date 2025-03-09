plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.serialization)

            implementation(projects.multiplatformCommon.network)
            implementation(projects.multiplatformCommon.serialization)
        }
    }
}