import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        configure<KotlinMultiplatformExtension> {
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                moduleName = project.name
                browser {
                    println("moduleName = ${project.name}")
                    val moduleName = project.name
                    val rootDirPath = project.rootDir.path
                    val projectDirPath = project.projectDir.path
                    val backendPath = "$rootDirPath/module/application/$moduleName/src/main/resources/static"
                    println("rootDirPath: $rootDirPath")
                    println("projectDirPath: $projectDirPath")
                    println("backendPath: $backendPath")
                    commonWebpackConfig {
                        outputFileName = "composeApp.js"
                        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
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
        }
    }
}

