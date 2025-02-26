plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)
        }

        jvmMain.dependencies {
            api(libs.ktor.client.cio)
        }

        wasmJsMain.dependencies {
            api(libs.ktor.client.js)
        }
    }
}