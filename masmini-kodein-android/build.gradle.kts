plugins {
    id("com.android.library")
    kotlin("android")
}


android {
    compileSdkVersion(30)
    buildToolsVersion("29.0.3")

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(project(":masmini-kodein"))

    api("androidx.fragment:fragment-ktx:1.3.1")
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    api("org.kodein.di:kodein-di-framework-android-x:$5.2.0")

    testImplementation("junit:junit:1.1.2")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}