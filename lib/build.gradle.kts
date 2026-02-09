plugins {
  id("buildsrc.convention.kotlin-jvm")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dokka)
  alias(libs.plugins.tailwind)
  alias(libs.plugins.vanniktechMavenPublish)
  alias(libs.plugins.ktfmt)
  `java-library`
  signing
}

group = "com.hyeons-lab"

version = libs.versions.artifactVersion.get()

dependencies {
  api(libs.bundles.kotlinx.html)
  implementation(libs.kotlinx.io.core)
  implementation(libs.kotlinxSerialization)

  // Testing
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions.core)
  testImplementation(libs.kotest.property)
}

tasks.withType<Jar> {
  manifest {
    attributes["Implementation-Title"] = "Static Site Generator"
    attributes["Implementation-Version"] = version
  }
}

tasks.withType<AbstractArchiveTask> { setProperty("archiveBaseName", "hyeons-lab-ssg") }

mavenPublishing {
  // Target the new Sonatype Central Portal
  publishToMavenCentral()

  // Coordinates for this module
  coordinates(groupId = group.toString(), artifactId = "ssg", version = version.toString())

  pom {
    name.set("Hyeons Lab Static Site Generator")
    description.set(
      "A lightweight, type-safe static site generator written in Kotlin that generates modern HTML with Tailwind CSS support"
    )
    url.set("https://github.com/hyeons-lab/ssg")

    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
      }
    }

    scm {
      url.set("https://github.com/hyeons-lab/ssg")
      connection.set("scm:git:git://github.com/hyeons-lab/ssg.git")
      developerConnection.set("scm:git:ssh://git@github.com/hyeons-lab/ssg.git")
    }

    developers {
      developer {
        id.set("hyeons-lab")
        name.set("Hyeon's Lab")
        organization.set("Hyeon's Lab")
        organizationUrl.set("https://github.com/hyeons-lab")
      }
    }
  }

  // Sign all publications (only if credentials are available)
  signAllPublications()
}

// Configure signing to be optional when credentials are not available
signing {
  setRequired {
    // Only require signing when publishing to Maven Central (not for Maven Local)
    gradle.taskGraph.allTasks.any { it.name.contains("ToMavenCentral") }
  }
}

// Dokka V2 configuration for generating HTML documentation from KDoc
dokka {
  dokkaPublications.html { outputDirectory.set(layout.buildDirectory.dir("docs/kdoc")) }

  dokkaSourceSets.main {
    moduleName.set("Hyeons' Lab SSG")

    // Include package-level documentation
    includes.from("Module.md")

    // Source links to GitHub
    sourceLink {
      localDirectory.set(file("src/main/kotlin"))
      remoteUrl("https://github.com/hyeons-lab/ssg/tree/main/lib/src/main/kotlin")
      remoteLineSuffix.set("#L")
    }
  }
}

// ktfmt configuration for Google style formatting
ktfmt { googleStyle() }
