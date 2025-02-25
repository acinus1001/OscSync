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
}

tasks.named("build") {
    println("=======build start ======")
    dependsOn("${projects.moduleFront.identityPath}:wasmJsBrowserProductionWebpack")
}

tasks.named("bootRun") {
    println("=======run start ======")
    dependsOn("${projects.moduleFront.identityPath}:wasmJsBrowserProductionWebpack")
}

tasks.named("processResources") {
    dependsOn(copyFrontendToBackend)
}

val copyFrontendToBackend by tasks.registering(Copy::class) {
    dependsOn("${projects.moduleFront.identityPath}:wasmJsBrowserProductionWebpack")
    val frontEndBuildDir = "${rootProject.projectDir.path}/module-front/build"
    println("======= Checking Frontend Build Directory: $frontEndBuildDir =======")
    from("$frontEndBuildDir/kotlin-webpack/wasmJs/productionExecutable/") {
        include("**/*")
    }
    from("$frontEndBuildDir/processedResources/wasmJs/main/") {
        include("**/*")
    }

    println("=======cp start $projectDir ======")

    into("$projectDir/src/main/resources/static")
    // into("$projectDir/build/resources/main/static/.")
}