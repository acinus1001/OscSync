plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.core)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}