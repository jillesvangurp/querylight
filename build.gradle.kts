@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
    }
    macosX64("native") { // on macOS
        // linuxX64("native") // on Linux
        // mingwX64("native") // on Windows
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(Testing.kotest.assertions.core)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))

            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

publishing {
    repositories {
        maven {
            // GOOGLE_APPLICATION_CREDENTIALS env var must be set for this to work
            // public repository is at https://maven.tryformation.com/releases
            url = uri("gcs://mvn-public-tryformation/releases")
            name = "FormationPublic"
        }
    }
}
