// Tests only, not published. fint-flyt-kafka 7, the version in production, reads the wire compatibility fixtures
// (FINT-130), so a rollback to it is safe. A separate project, because fint-flyt-kafka 7 needs Spring Boot 3 and
// fint-kafka 6. Delete it when all flyt apps run the version on fint-kafka-starter.

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
    sourceSets.named("test") {
        kotlin.srcDir(rootProject.file("wire-compat/cases"))
    }
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    constraints {
        testImplementation("at.yawk.lz4:lz4-java:1.13.0") {
            because("Fixes CVE-2026-59949 in the kafka-clients transitive dependency")
        }
        testImplementation("org.xerial.snappy:snappy-java:1.1.10.12") {
            because("Fixes CVE-2026-93451, CVE-2026-108106 and CVE-2026-116590 in kafka-clients")
        }
    }

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.16"))
    testImplementation(platform("com.fasterxml.jackson:jackson-bom:2.22.4"))

    testImplementation("no.novari:flyt-kafka:7.4.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("wireCompat.fixtures", rootProject.file("wire-compat/fixtures").absolutePath)
    inputs.dir(rootProject.file("wire-compat/fixtures"))
}

ktlint {
    version.set("1.8.0")
}
