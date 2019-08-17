@file:Suppress("SpellCheckingInspection", "PropertyName")

val ktor_version:    String by project
val kotlin_version:  String by project
val logback_version: String by project
val h2_version:      String by project
val exposed_version: String by project
val hikari_version:  String by project

plugins {
    application
    kotlin("jvm") version "1.3.41"

    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group   = "blogify"
version = "alpha-0.0.1"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {

    // Kt stdlib

    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    // Ktor

    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("ch.qos.logback:logback-classic:$logback_version")
    compile("io.ktor:ktor-server-core:$ktor_version")
    compile("io.ktor:ktor-locations:$ktor_version")
    compile("io.ktor:ktor-auth:$ktor_version")
    compile("io.ktor:ktor-auth-jwt:$ktor_version")
    compile("io.ktor:ktor-jackson:$ktor_version")

    // Database stuff

    compile("com.h2database:h2:$h2_version")
    compile("org.jetbrains.exposed:exposed:$exposed_version")
    compile("com.zaxxer:HikariCP:$hikari_version")

    // Ktor test

    testCompile("io.ktor:ktor-server-tests:$ktor_version")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

// Fat jar

tasks.withType<Jar> {
    destinationDir = File("./build/dist/jar/")

    manifest {
        attributes (
            mapOf (
                "Main-Class" to application.mainClassName
            )
        )
    }
}