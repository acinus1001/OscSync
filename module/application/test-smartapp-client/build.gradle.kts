plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(projects.module.common.logger)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)

    implementation(projects.module.common.network)
    implementation(projects.module.internal.smartappClient)
    implementation(projects.module.domain.database)
    implementation(projects.multiplatformCommon.types.testSmartappClient)
}

val frontModule = projects.moduleFront.testSmartappClient
val frontIdentityPath = frontModule.identityPath
val frontModuleBuildPath = "${rootProject.projectDir.path}/module-front/${frontModule.name}/build"

tasks.named("build") {
    println("=======build start $frontIdentityPath======")
    dependsOn("$frontIdentityPath:wasmJsBrowserProductionWebpack")
}

tasks.named("bootRun") {
    println("=======run start ======")
    dependsOn("$frontIdentityPath:wasmJsBrowserProductionWebpack")
}

tasks.named("processResources") {
    dependsOn(copyFrontendToBackend)
}

val copyFrontendToBackend by tasks.registering(Copy::class) {
    dependsOn("$frontIdentityPath:wasmJsBrowserProductionWebpack")
    delete("$projectDir/src/main/resources/static")
    println("======= Checking Frontend Build Directory: $frontModuleBuildPath =======")
    from("$frontModuleBuildPath/kotlin-webpack/wasmJs/productionExecutable/") {
        include("**/*")
    }
    from("$frontModuleBuildPath/processedResources/wasmJs/main/") {
        include("**/*")
    }

    println("=======cp start $projectDir ======")

    into("$projectDir/src/main/resources/static")
}