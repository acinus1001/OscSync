import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
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
            api(libs.kotlin.serialization)
        }
    }

}