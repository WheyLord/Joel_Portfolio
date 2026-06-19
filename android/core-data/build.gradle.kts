plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.joelhellsten.golfswing.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core-engine"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.gson)
    ksp(libs.room.compiler)
    implementation(libs.coroutines.android)
}
