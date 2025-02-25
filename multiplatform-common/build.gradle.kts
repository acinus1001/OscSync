import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

subprojects {
    plugins.apply("org.jetbrains.kotlin.multiplatform")

    @OptIn(ExperimentalWasmDsl::class)
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        configure<KotlinMultiplatformExtension> {
            jvm()
            js {
                browser()
                nodejs()
            }
            wasmJs {
                binaries.executable()
            }
        }

    }
}