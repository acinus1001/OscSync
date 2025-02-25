import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }
    wasmJs {
        binaries.executable()
    }

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