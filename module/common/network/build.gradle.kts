dependencies {
    api(libs.retrofit2)
    implementation(libs.retrofit2.kotlin.serialization.converter)
    implementation(projects.module.common.serialization)

    implementation(projects.module.common)
}