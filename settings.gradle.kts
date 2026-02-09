// Root project name - always set this to avoid issues with directory names
rootProject.name = "ssg"

// Plugin management - configure repositories for plugin resolution
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        gradlePluginPortal()
    }
}

// Plugin management for settings
plugins {
    // Foojay Toolchains plugin to automatically download JDKs required by subprojects
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Centralized dependency resolution management
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

// Include subprojects
include(":lib")
