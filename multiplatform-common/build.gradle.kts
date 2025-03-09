@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.build.config)
}

kotlin {
    jvm()
    wasmJs {
        browser()
        binaries.executable()
    }
}

subprojects {
    plugins.apply("org.jetbrains.kotlin.multiplatform")

    @OptIn(ExperimentalWasmDsl::class)
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        configure<KotlinMultiplatformExtension> {
            jvm()
            wasmJs {
                browser()
                binaries.executable()
            }
        }

    }
}