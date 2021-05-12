import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("org.jetbrains.dokka") version "1.4.32"
    `java-library`
    `maven-publish`
    signing
}

val tagVersion = System.getenv("GITHUB_REF")?.split('/')?.last()
val groupId = "io.getunleash"
version = tagVersion?.trimStart('v') ?: "WIP"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.1")
    testImplementation("com.google.guava:guava-testlib:30.1-jre")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Unleash Proxy Client")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/Unleash/unleash-proxy-client-kotlin/tree/${tagVersion ?: "main"}/"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

