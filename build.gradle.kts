plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

group = "dev.kuro9"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

allprojects {

}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.webflux)

    implementation(libs.kotlin.serialization)

    implementation(libs.java.osc)
}