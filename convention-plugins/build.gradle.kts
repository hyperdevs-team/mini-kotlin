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
    `java-gradle-plugin`
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle)
    implementation(libs.androidgitversion)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.sdk.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.sdk.get())
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = libs.versions.java.sdk.get()
    }
}

gradlePlugin {
    plugins {
        register("androidApp") {
            id = "mini.android.plugins.androidApp"
            implementationClass = "mini.android.plugins.AndroidAppConventionPlugin"
        }

        register("androidLib") {
            id = "mini.android.plugins.androidLib"
            implementationClass = "mini.android.plugins.AndroidLibConventionPlugin"
        }

        register("javaLib") {
            id = "mini.android.plugins.javaLib"
            implementationClass = "mini.android.plugins.JavaLibConventionPlugin"
        }
    }
}