plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.serialization)
            api(libs.kotlin.serialization.json)
            api(libs.kotlin.serialization.protobuf)
            api(libs.kotlinx.datetime)
        }
    }

}