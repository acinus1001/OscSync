import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}


kotlin {
    js {
        outputModuleName = project.name
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    port = 8090
                    static(project.projectDir.resolve("build/processedResources/js/main").path)
                    static(project.projectDir.resolve("build/kotlin-webpack/js/developmentExecutable").path)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
//            implementation(projects.multiplatformCommon.serialization)
//            implementation(projects.multiplatformCommon.network)
//            implementation(projects.multiplatformCommon.network.appDiscordBot)
//            implementation(projects.multiplatformCommon.types.appDiscordBot)
//            implementation(projects.multiplatformCommon.types.member)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)
        }

        jsMain.dependencies {
//            implementation(compose.runtime)
            implementation(compose.html.core)
        }
    }
}