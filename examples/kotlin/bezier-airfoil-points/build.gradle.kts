import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.gradleup.shadow:shadow-gradle-plugin:8.3.0")
    }
}

plugins {
    kotlin("jvm") version "1.8.10"
    application
    id("com.gradleup.shadow") version "8.3.0"

}

group = "me.daanr"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        // change URLs to point to your repos, e.g. http://my.org/repo
        val releasesRepoUrl = URI("https://s01.oss.sonatype.org/content/repositories/releases/")
        val snapshotsRepoUrl = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    implementation("io.github.daniel-tucano:matplotlib4k:0.3.1")
    implementation("io.github.daniel-tucano:geomez-core:0.2.0")
    implementation("io.github.daniel-tucano:geomez-visualization:0.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "18"
}

application {
    mainClass.set("MainKt")
}