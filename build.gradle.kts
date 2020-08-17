plugins {
    kotlin("multiplatform") version "1.4.0"
    id("com.github.ben-manes.versions") version "0.28.0" // gradle dependencyUpdates -Drevision=release
    id("org.jmailen.kotlinter") version "2.4.1"
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
                implementation("io.kotest:kotest-assertions-core:4.1.3")
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
