plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.module.common.logger)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.aop)

    runtimeOnly(libs.aspectj.runtime)
    runtimeOnly(libs.aspectj.weaver)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)

    implementation(projects.multiplatformCommon.network)

    implementation(projects.module.common.network)
    implementation(projects.module.internal.smartappClient)
    implementation(projects.module.internal.discordClient)
    implementation(projects.module.internal.aiClient)
    implementation(projects.module.domain.database)
    implementation(projects.module.domain.smartappUserinfo)


    implementation(projects.module.domain.memberAuthentication)
}


val frontModule = projects.moduleFront.appDiscordBot
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