plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("TelegramKt")
}