plugins {
    kotlin("jvm")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":masmini-common"))
    api("org.kodein.di:kodein-di-jvm:7.4.0")
}
