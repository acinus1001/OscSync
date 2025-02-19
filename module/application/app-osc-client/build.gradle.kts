plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(projects.module.common)
    implementation(projects.module.internal.oscPublisher)

    implementation(libs.spring.boot.starter.web)
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.4.2")
}