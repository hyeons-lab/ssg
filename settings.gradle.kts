// Root project name - always set this to avoid issues with directory names
rootProject.name = "ssg"

// Plugin management - configure repositories for plugin resolution
pluginManagement {
    val vendorPluginDir = file("vendor/gradle-tailwind/plugin")
    if (vendorPluginDir.exists() && file("$vendorPluginDir/build.gradle.kts").exists()) {
        includeBuild("vendor/gradle-tailwind/plugin")
    }
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
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
                url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

// Include subprojects
include(":lib")
