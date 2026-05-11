plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    
    implementation(projects.module.domain.database)
    implementation(projects.module.common.logger)

    implementation(projects.multiplatformCommon.date)
    implementation(projects.multiplatformCommon.types.smartthings)

    implementation(libs.smartapp.core)
    implementation(libs.smartapp.spring)
    implementation(libs.smartthings.client)

    implementation("org.apache.httpcomponents:httpclient:4.5.14")
//    implementation("org.apache.httpcomponents.client5:httpclient5:5.6.1")
}