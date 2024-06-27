plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.convention.androidApp)
}

android {
    namespace = "mini.android.sample"

    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.android.buildTools.get()

    defaultConfig {
        applicationId = "mini.android.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
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

    kotlinOptions {
        jvmTarget = libs.versions.java.sdk.get()
    }

    lint {
        abortOnError = false
    }

    configurations.configureEach {
        // This library is included with two different versions
        resolutionStrategy.force("com.google.code.findbugs:jsr305:3.0.1")
    }
}

dependencies {
    implementation(project(":mini-android"))
    implementation(project(":mini-kodein-android"))

    // kapt(project(":mini-processor"))
    ksp(project(":mini-processor"))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Support
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.compose)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)

    implementation(libs.bundles.androidx.lifecycle)

    // Misc
    implementation("com.github.minikorp:grove:1.0.3")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso)
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("com.agoda.kakao:kakao:2.4.0")
}