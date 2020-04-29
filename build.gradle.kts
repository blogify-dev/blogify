@file:Suppress("SpellCheckingInspection", "PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version:      String by project
val kotlin_version:    String by project
val logback_version:   String by project
val pg_driver_version: String by project
val exposed_version:   String by project
val hikari_version:    String by project
val spring_security_core_version: String by project

plugins {
    application
    kotlin("jvm") version "1.3.41"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.60"

    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("com.avast.gradle.docker-compose") version "0.9.4"
}

group   = "blogify"
version = "0.1.0"

application {
    mainClassName = "blogify.backend.bootstrap.BlogifyApplicationBootstrapper"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.3.61")
    }
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://jitpack.io")
    maven { url = uri("https://dl.bintray.com/kittinunf/maven") }
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {

    // Kt stdlib

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    // Submodules

    implementation(project(":reflect"))

    // Ktor

    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-network-tls:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")

    // Metadata Extractor

    implementation("com.drewnoakes:metadata-extractor:2.12.0")

    // Ktor client

    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-json:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")

    // Database stuff

    implementation("org.postgresql:postgresql:$pg_driver_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation("com.github.Benjozork:exposed-postgres-extensions:alpha-1")

    // Spring security for hashing

    implementation("org.springframework.security:spring-security-core:$spring_security_core_version")

    // Kolor

    implementation("com.andreapivetta.kolor:kolor:0.0.2")

    // Result

    implementation("com.github.kittinunf.result:result:2.2.0")
    implementation("com.github.kittinunf.result:result-coroutines:2.2.0")

    // JJWT

    implementation("io.jsonwebtoken:jjwt-api:0.10.7")
    runtime("io.jsonwebtoken:jjwt-impl:0.10.7")
    runtime("io.jsonwebtoken:jjwt-jackson:0.10.7")

    // Config

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-configparser:0.14.0")

    // Testing

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.5.2")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.5.2")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

// Fat jar

tasks.withType<Jar> {
    destinationDirectory.set(File("./build/dist/jar"))

    manifest {
        attributes (
            mapOf (
                "Main-Class" to application.mainClassName
            )
        )
    }
}

dockerCompose {
    useComposeFiles = mutableListOf("./docker-compose.yml")

    projectName = "blogify"

    waitForTcpPorts = false
    stopContainers  = true
}

tasks.register("buildAngularProd", Exec::class) {
    workingDir = File("src/blogify/frontend")
    commandLine = listOf("npm", "run", "build")
}

// blogifyDeploy : this builds angular app, packs the jar and runs the docker-compose config
tasks.register("blogifyDeploy", GradleBuild::class) {
    tasks = mutableListOf("buildAngularProd", "shadowJar", "composeUp")
}

// Local test deploy : this packs the jar and runs the docker-compose config
tasks.register("localTestDeploy", GradleBuild::class) {
    tasks = mutableListOf("shadowJar", "composeUp")
}
