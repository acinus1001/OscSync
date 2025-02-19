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
    println("${File("$rootDir/module/$moduleType")}")
    return File("$rootDir/module/$moduleType").list { dir, _ ->
        dir.isDirectory && dir.list()?.firstOrNull { it == "build.gradle.kts" } != null
    }?.toList() ?: emptyList()
}
