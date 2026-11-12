// Tests only, not published. fint-flyt-kafka 7, the version in production, reads the wire compatibility fixtures
// (FINT-130), so a rollback to it is safe. A separate project, because fint-flyt-kafka 7 needs Spring Boot 3 and
// fint-kafka 6. Delete it when all flyt apps run the version on fint-kafka-starter.
// Not locked: Dependabot only rewrites the root lockfile, so a lockfile here fails the build after its version bumps.
// The BOMs and the fixed flyt-kafka version keep the resolution stable.

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

dependencies {
    constraints {
        testImplementation(libs.lz4.java) {
            because("Fixes CVE-2026-59949 in the kafka-clients transitive dependency")
        }
        testImplementation(libs.snappy.java) {
            because("Fixes CVE-2026-93451, CVE-2026-108106 and CVE-2026-116590 in kafka-clients")
        }
    }

    testImplementation(platform(libs.spring.boot.dependencies.v7))
    testImplementation(platform(libs.jackson.bom.v7))

    testImplementation(libs.flyt.kafka.v7)
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
    version.set(libs.versions.ktlint.cli)
}
