plugins {
    kotlin("multiplatform") version "1.4.10"
    id("com.github.ben-manes.versions") version "0.33.0" // gradle dependencyUpdates -Drevision=release
    id("org.jmailen.kotlinter") version "3.2.0"
    id("maven-publish")

}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js {
        nodejs()
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
                implementation("io.kotest:kotest-assertions-core:4.3.0")
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
            url = uri("file://$projectDir/localRepo")
        }
    }
}

kotlinter {
    // run gradle formatKotlin to fix
    ignoreFailures = true
}

val ktLint by tasks.creating(org.jmailen.gradle.kotlinter.tasks.LintTask::class) {
    group = "verification"
    source(files("src"))

}

val ktFormat by tasks.creating(org.jmailen.gradle.kotlinter.tasks.FormatTask::class) {
    group = "formatting"
    source(files("src"))
}
