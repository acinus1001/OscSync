import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.kotlin.serialization)
}


kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
//        moduleName = project.name
        outputModuleName = project.name
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    port = 8090
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.mvi.kotlin.decomposed)

            implementation(projects.multiplatformCommon.serialization)
            implementation(projects.multiplatformCommon.network)
            implementation(projects.multiplatformCommon.network.appDiscordBot)
            implementation(projects.multiplatformCommon.types.appDiscordBot)
            implementation(projects.multiplatformCommon.types.member)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)
        }

    }
}