rootProject.name = "OscSync"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("${rootDir}/gradle/libs.version.toml"))
        }
    }
}
