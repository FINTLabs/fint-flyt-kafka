import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.springframework.boot.gradle.plugin.SpringBootPlugin

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath(platform("com.fasterxml.jackson:jackson-bom:2.22.2"))
        constraints {
            classpath("org.apache.httpcomponents.client5:httpclient5:5.6.4")
            classpath("org.apache.httpcomponents.core5:httpcore5:5.4.3")
            classpath("org.apache.httpcomponents.core5:httpcore5-h2:5.4.3")
            classpath("org.apache.commons:commons-lang3:3.20.0")
        }
    }
}

plugins {
    id("org.springframework.boot") version "3.5.16" apply false
    id("java-library")
    id("maven-publish")
    id("io.github.ben-manes.versions") version "0.61.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.spring") version "2.4.10"
}

group = "no.novari"
version = findProperty("version") as String? ?: "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

dependencies {
    constraints {
        ktlint("ch.qos.logback:logback-core:1.6.3")
        implementation("at.yawk.lz4:lz4-java:1.11.2") {
            because("Fixes CVE-2026-59949 in the kafka-clients transitive dependency")
        }
    }

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.22.2"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.26.1"))
    implementation(platform("io.netty:netty-bom:4.2.17.Final"))
    api(platform(SpringBootPlugin.BOM_COORDINATES))

    api("no.novari:kafka:6.2.0")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlin:kotlin-reflect")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.apache.logging.log4j:log4j-api")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

ktlint {
    version.set("1.8.0")
}

tasks.named("check") {
    dependsOn("ktlintCheck")
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.fintlabs.no/releases")
            credentials {
                username = System.getenv("REPOSILITE_USERNAME")
                password = System.getenv("REPOSILITE_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
