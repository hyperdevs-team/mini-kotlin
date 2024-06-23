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

package mini.android.plugins.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import com.gladed.androidgitversion.AndroidGitVersionExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPom
import org.gradle.kotlin.dsl.configure

data class Version(val name: String, val code: Int)

internal fun Project.applyVersioning(): Version {
    pluginManager.apply("com.gladed.androidgitversion")
    project.extensions.configure<AndroidGitVersionExtension> {
        codeFormat = "MNNPP"
        baseCode = 1
    }

    return Version(
        project.extensions.getByType(AndroidGitVersionExtension::class.java).name(),
        project.extensions.getByType(AndroidGitVersionExtension::class.java).code()
    )
}

internal fun Project.applyAndroidAppVersioning(versionName: String, versionCode: Int) {
    project.extensions.configure<ApplicationExtension>{
        defaultConfig {
            this.versionName = versionName
            this.versionCode = versionCode
        }
    }
}

internal fun Project.applyAndroidLibPublishing(versionName: String) {
    val publishingName = "release"

    pluginManager.apply("maven-publish")

    project.extensions.configure<LibraryExtension> {
        publishing {
            singleVariant(publishingName) {
                withSourcesJar()
                withJavadocJar()
            }
        }
    }

    project.afterEvaluate {
        project.extensions.configure<PublishingExtension> {
            publications {
                create(publishingName, MavenPublication::class.java) {
                    from(components.getByName(publishingName))

                    artifactId = project.name
                    version = versionName

                    (pom as DefaultMavenPom).setMetadata(
                        project.name,
                        versionName
                    )
                }
            }
        }
    }
}

internal fun Project.applyJavaLibPublishing(versionName: String) {
    val publishingName = "maven"

    pluginManager.apply("java")
    pluginManager.apply("maven-publish")

    project.extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    project.extensions.configure<PublishingExtension> {
        publications {
            create(publishingName, MavenPublication::class.java) {
                from(components.getByName("java"))

                artifactId = project.name
                version = versionName

                (pom as DefaultMavenPom).setMetadata(
                    project.name,
                    versionName
                )
            }
        }
    }
}