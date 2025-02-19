rootProject.name = "OscSync"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("${rootDir}/gradle/libs.version.toml"))
        }
    }
}

include("module")
include(
    "module:application",
    "module:domain",
    "module:internal",
    "module:common",
)

val applicationModuleName: List<String> = emptyList()
val domainModuleName: List<String> = emptyList()
val internalModuleName: List<String> = emptyList()
val commonModuleName: List<String> = emptyList()

applicationModuleName.forEach { includeModule("application", it) }
domainModuleName.forEach { includeModule("domain", it) }
internalModuleName.forEach { includeModule("internal", it) }
commonModuleName.forEach { includeModule("common", it) }

fun includeModule(moduleType: String, moduleName: String) {
    include("module:$moduleType:$moduleName")
}
