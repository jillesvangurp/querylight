@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl


plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven("https://maven.tryformation.com/releases") {
        content {
            includeGroup("com.jillesvangurp")
            includeGroup("com.github.jillesvangurp")
            includeGroup("com.tryformation")
            includeGroup("com.tryformation.fritz2")
        }
    }
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
    }
    macosX64 { // on macOS
    }
    linuxX64 {

    }
    mingwX64 {

    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
        d8()
    }
// no kotest support yet ..
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmWasi() {
//        nodejs()
//    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("com.jillesvangurp:kotlinx-serialization-extensions:_")
                implementation(KotlinX.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(Testing.kotest.assertions.core)
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))

            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        nativeTest {
            dependencies {
//                implementation(kotlin("test-native"))
            }

        }
        wasmJsTest {
            dependencies {
                implementation(kotlin("test-wasm-js"))
            }
        }


        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                languageVersion = "1.9"
                apiVersion = "1.9"
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
