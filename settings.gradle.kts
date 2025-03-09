@file:Suppress("UnstableApiUsage")

rootProject.name = "OscSync"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
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
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}


include("module")
include("module-front")
include("multiplatform-common")

// module 서브모듈 전체적용
val moduleTypeList = listOf("application", "domain", "internal", "common")
moduleTypeList.forEach { moduleType ->
    include("module:$moduleType")
    println("$moduleType : ${getSubModuleName(moduleType)}")
    getSubModuleName(moduleType).forEach { subModuleName -> includeModule(moduleType, subModuleName) }
}

// module front 서브모듈 전체적용
getSubModuleNameOfPrimaryModule("module-front").forEach { subModuleName ->
    println("front: $subModuleName")
    include("module-front:$subModuleName")
}

// multiplatform common 서브모듈 전체적용
getSubModuleNameOfPrimaryModule("multiplatform-common").forEach { subModuleName ->
    println("multi-comon: $subModuleName")
    include("multiplatform-common:$subModuleName")
}

// multiplatform common types 서브모듈 전체적용
getSubModuleNameOfPrimaryModule("multiplatform-common/types").forEach { subModuleName ->
    println("multi-comon:types: $subModuleName")
    include("multiplatform-common:types:$subModuleName")
}

// multiplatform common network 서브모듈 전체적용
getSubModuleNameOfPrimaryModule("multiplatform-common/network").forEach { subModuleName ->
    println("multi-comon:network: $subModuleName")
    include("multiplatform-common:network:$subModuleName")
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

fun getSubModuleNameOfPrimaryModule(primaryModuleName: String): List<String> {
    val moduleDir = File("$rootDir/$primaryModuleName")

    return moduleDir.listFiles()?.filter { subDir ->
        subDir.isDirectory && File(subDir, "build.gradle.kts").exists()
    }?.map { it.name } ?: emptyList()
}