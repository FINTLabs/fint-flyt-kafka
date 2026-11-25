import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

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
        // Spring Boot 4.2 manages Kotlin 2.4, so apps may run with a 2.4 stdlib.
        apiVersion.set(KotlinVersion.KOTLIN_2_4)
    }
    // The stdlib version Spring Boot manages, not the Kotlin plugin's, so the library does not upgrade it in apps.
    coreLibrariesVersion =
        libs.versions.kotlin.stdlib
            .get()
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

// The Spring Boot BOM sets versions in this build only. It is not published, so it cannot change the app's Spring Boot
// or Kotlin version. versionMapping below publishes the resolved versions instead.
val springBootBom = configurations.dependencyScope("springBootBom")
listOf("compileClasspath", "runtimeClasspath", "testCompileClasspath", "testRuntimeClasspath").forEach {
    configurations.named(it) { extendsFrom(springBootBom.get()) }
}

dependencies {
    constraints {
        ktlint(libs.logback.core)
    }

    springBootBom(platform(libs.spring.boot.dependencies))

    api(libs.kafka.starter)
    api("tools.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlin:kotlin-reflect")

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
            // Publish the versions resolved from the Spring Boot BOM, so consumers do not need the BOM.
            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
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
