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

import org.gradle.api.publish.maven.internal.publication.DefaultMavenPom

fun DefaultMavenPom.setMetadata(artifactName: String, artifactVersion: String) {
    name.set("Mini ($artifactName)")
    description.set("Mini is a minimal Flux architecture written in Kotlin that also adds a mix of useful features to build UIs fast.")
    url.set("https://github.com/hyperdevs-team/mini-kotlin/releases/tag/$artifactVersion")
    //version.set(miniVersion)
    inceptionYear.set("2017")

    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }

    organization {
        name.set("HyperDevs")
        url.set("https://github.com/hyperdevs-team")
    }

    issueManagement {
        system.set("GitHub Issues")
        url.set("https://github.com/hyperdevs-team/mini-kotlin/issues")
    }

    scm {
        connection.set("scm:git:git@github.com:hyperdevs-team/mini-kotlin.git")
        url.set("https://github.com/hyperdevs-team/mini-kotlin.git")
    }

    developers {
        developer {
            name.set("Estefanía Sarasola Elvira")
            id.set("yamidragut")
            url.set("https://github.com/yamidragut")
            roles.set(listOf("Maintainer"))
        }

        developer {
            name.set("Sara Lucía Pérez")
            id.set("Babelia13")
            url.set("https://github.com/Babelia13")
            roles.set(listOf("Maintainer"))
        }

        developer {
            name.set("Adrián García")
            id.set("adriangl")
            url.set("https://github.com/adriangl")
            roles.set(listOf("Maintainer"))
        }

        developer {
            name.set("Francisco García Sierra")
            id.set("FrangSierra")
            url.set("https://github.com/FrangSierra")
            roles.set(listOf("Maintainer", "Initial Work"))
        }

        developer {
            name.set("Pablo Orgaz")
            id.set("pabloogc")
            url.set("https://github.com/pabloogc")
            roles.set(listOf("Initial Work"))
        }
    }
}