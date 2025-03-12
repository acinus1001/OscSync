import dev.kuro9.build.config.Profile
import dev.kuro9.build.config.ProjectInfo

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.build.config)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.ktor.client.resources)
            api(libs.ktor.client.logging)
            implementation(projects.multiplatformCommon)
        }

        jvmMain.dependencies {
            api(libs.ktor.client.cio)
        }

        wasmJsMain.dependencies {
            api(libs.ktor.client.js)
        }
    }
}

private val profile: Profile by ProjectInfo

buildConfig {
    when (profile) {
        Profile.PRODUCTION -> TODO()

        Profile.DEVELOPMENT -> {
            buildConfigField("API_SERVER_HOST", "localhost")
            buildConfigField("API_SERVER_PORT", 8080)
            buildConfigField("API_SERVER_METHOD", "HTTP")
        }
    }

    useKotlinOutput {
        internalVisibility = false
    }
}