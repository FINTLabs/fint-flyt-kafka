import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath(platform("com.fasterxml.jackson:jackson-bom:2.22.4"))
        constraints {
            classpath("org.apache.httpcomponents.client5:httpclient5:5.6.5")
            classpath("org.apache.httpcomponents.core5:httpcore5:5.4.4")
            classpath("org.apache.httpcomponents.core5:httpcore5-h2:5.4.4")
            classpath("org.apache.commons:commons-lang3:3.21.0")
        }
    }
}

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.ben.manes.versions)
}

group = "no.novari"
version = findProperty("version") as String? ?: "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

// Build with JDK 25, publish for Java 21, as fint-kafka-starter. binary-compatibility-validator cannot read Java 25 class files.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

kotlin {
    // Locked API: fint-flyt-gateway-starter is compiled against one version, and apps may pull in a newer one.
    explicitApi()
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjdk-release=21")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

apiValidation {
    ignoredProjects += "compat-v7"
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    constraints {
        ktlint(libs.logback.core)
        implementation(libs.lz4.java) {
            because("Fixes CVE-2026-59949 in the kafka-clients transitive dependency")
        }
        implementation(libs.snappy.java) {
            because("Fixes CVE-2026-93451, CVE-2026-108106 and CVE-2026-116590 in kafka-clients")
        }
    }

    implementation(platform(libs.netty.bom))
    api(platform(libs.spring.boot.dependencies))

    api(libs.kafka.starter)
    api("tools.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Wire compatibility tests (FINT-130): test cases shared with compat-v7, fixtures in wire-compat/fixtures.
kotlin.sourceSets.named("test") {
    kotlin.srcDir("wire-compat/cases")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("wireCompat.fixtures", file("wire-compat/fixtures").absolutePath)
    systemProperty("wireCompat.write", providers.gradleProperty("writeWireCompatFixtures").isPresent)
    inputs.dir("wire-compat/fixtures")
}

ktlint {
    version.set(libs.versions.ktlint.cli)
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
