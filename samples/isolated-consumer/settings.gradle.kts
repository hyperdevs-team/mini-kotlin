@file:Suppress("UnstableApiUsage")

rootProject.name = "isolated-consumer"

include(":app")
include(":message-feature")
include(":mini-common")
include(":mini-processor")

project(":mini-common").projectDir = file("../../mini-common")
project(":mini-processor").projectDir = file("../../mini-processor")

includeBuild("../../convention-plugins")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = java.net.URI("https://jitpack.io") }
    }

    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}
