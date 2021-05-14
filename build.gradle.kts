import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.32"
    id("org.jetbrains.dokka") version "1.4.32"
    `java-library`
    `maven-publish`
    signing
    jacoco
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.12"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("net.researchgate.release").version("2.8.1")
}

val tagVersion = System.getenv("GITHUB_REF")?.split('/')?.last()
val groupId = "io.getunleash"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
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
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events("passed", "skipped", "failed")
    }
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}


tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Unleash Proxy Client")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/Unleash/unleash-proxy-client-kotlin/tree/${tagVersion ?: "main"}/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}


java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["kotlin"])
            groupId = "io.getunleash"
            artifactId = "unleash-proxy-client-kotlin"
            version = "${version}"
            pom {
                name.set("Unleash Proxy Client")
                description.set("An Unleash Proxy Client for use when you don't want to evaluate toggles in memory")
                url.set("https://docs.getunleash.io/unleash-proxy-client-kotlin/index.html")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("chrkolst")
                        name.set("Christopher Kolstad")
                        email.set("chriswk@getunleash.ai")
                    }
                    developer {
                        id.set("ivarconr")
                        name.set("Ivar Conradi Østhus")
                        email.set("ivarconr@getunleash.ai")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Unleash/unleash-proxy-client-kotlin")
                    developerConnection.set("scm:git:ssh://git@github.com:Unleash/unleash-proxy-client-kotlin")
                    url.set("https://github.com/Unleash/unleash-proxy-client-kotlin")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}
