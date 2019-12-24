@file:Suppress("SpellCheckingInspection", "PropertyName")

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
    mainClassName = "io.ktor.server.cio.EngineMain"
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
    maven { url = uri("https://dl.bintray.com/kittinunf/maven") }
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {

    // Kt stdlib

    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    // Ktor

    compile("io.ktor:ktor-server-cio:$ktor_version")
    compile("ch.qos.logback:logback-classic:$logback_version")
    compile("io.ktor:ktor-server-core:$ktor_version")
    compile("io.ktor:ktor-locations:$ktor_version")
    compile("io.ktor:ktor-auth:$ktor_version")
    compile("io.ktor:ktor-auth-jwt:$ktor_version")
    compile("io.ktor:ktor-jackson:$ktor_version")

    // Ktor client

    compile("io.ktor:ktor-client-cio:$ktor_version")
    compile("io.ktor:ktor-client-json:$ktor_version")
    compile("io.ktor:ktor-client-json-jvm:$ktor_version")
    compile("io.ktor:ktor-client-jackson:$ktor_version")

    // Database stuff

    compile("org.postgresql:postgresql:$pg_driver_version")
    compile("org.jetbrains.exposed:exposed:$exposed_version")
    compile("com.zaxxer:HikariCP:$hikari_version")

    // Spring security for hashing

    compile("org.springframework.security:spring-security-core:$spring_security_core_version")

    // Ktor test

    testCompile("io.ktor:ktor-server-tests:$ktor_version")

    // Kolor

    compile("com.andreapivetta.kolor:kolor:0.0.2")

    // Result

    compile("com.github.kittinunf.result:result:2.2.0")
    compile("com.github.kittinunf.result:result-coroutines:2.2.0")

    // JJWT

    compile("io.jsonwebtoken:jjwt-api:0.10.7")
    runtime("io.jsonwebtoken:jjwt-impl:0.10.7")
    runtime("io.jsonwebtoken:jjwt-jackson:0.10.7")

    // Config

    implementation("com.charleskorn.kaml:kaml:0.15.0")
    compile("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

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

    waitForTcpPorts = true
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
