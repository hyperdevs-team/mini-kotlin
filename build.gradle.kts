buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.4.21"))
        classpath("com.android.tools.build:gradle:3.6.4")
    }
}

plugins {
    kotlin("jvm") version "1.4.21"
    `maven-publish`
}

fun runCommand(command: String): String {
    val runtime = Runtime.getRuntime()
    val process = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        runtime.exec("bash $command")
    } else {
        runtime.exec(command)
    }
    val stream = process.apply { waitFor() }.inputStream
    return stream.reader().readText().trim()
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        google()
    }
}

repositories {
    jcenter()
    mavenCentral()
    //maven { url = URI.parse("https://jitpack.io") }
    google()
}

