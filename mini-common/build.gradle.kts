/*
 * Copyright 2024 HyperDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("idea")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.convention.javaLib)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    // Optional Rx and Android bindings, one day these should be modules,
    // for now we compile against them but let user package library
    compileOnly(libs.android.library)

    compileOnly(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kluent)
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

idea {
    module {
        val sourceFoldersToAdd = listOf(
            "build/generated/source/kapt/main",
            "build/generated/source/kaptKotlin/main",
            "build/generated/source/ksp/main"
        ).map { File(it) }

        sourceDirs.addAll(sourceFoldersToAdd)
        generatedSourceDirs.addAll(sourceFoldersToAdd)
    }
}

