plugins {
    id("org.jetbrains.kotlin.js") version "1.3.72"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation("io.kotest:kotest-assertions-core:4.1.3")

    testImplementation(kotlin("test-js"))
}

kotlin.target.nodejs { }
