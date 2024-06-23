plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.convention.androidLib)
}

android {
    namespace = "mini.kodein.android"

    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.android.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("mini-kodein-android.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.sdk.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.sdk.get())
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = libs.versions.java.sdk.get()
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.sdk.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.sdk.get())
}

kotlin {
    jvmToolchain(libs.versions.java.sdk.get().toInt())
}

dependencies {
    api(project(":mini-kodein"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kodein.framework.androidx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso)
}

