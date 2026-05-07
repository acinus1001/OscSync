@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    js {
        browser()
    }
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.multiplatformCommon.network)
            implementation(projects.multiplatformCommon.serialization)
            implementation(projects.multiplatformCommon.types.member)

            implementation(libs.kotlinx.datetime)
        }
    }
}