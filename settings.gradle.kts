@file:Suppress("UnstableApiUsage")

rootProject.name = "OscSync"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("${rootDir}/gradle/libs.version.toml"))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("module")

val moduleTypeList = listOf("application", "domain", "internal", "common")
moduleTypeList.forEach { moduleType ->
    include("module:$moduleType")
    println("$moduleType : ${getSubModuleName(moduleType)}")
    getSubModuleName(moduleType).forEach { subModuleName -> includeModule(moduleType, subModuleName) }
}

fun includeModule(moduleType: String, moduleName: String) {
    println("module:$moduleType:$moduleName")
    include("module:$moduleType:$moduleName")
}

fun getSubModuleName(moduleType: String): List<String> {
    val moduleDir = File("$rootDir/module/$moduleType")

    return moduleDir.listFiles()?.filter { subDir ->
        subDir.isDirectory && File(subDir, "build.gradle.kts").exists()
    }?.map { it.name } ?: emptyList()
}
