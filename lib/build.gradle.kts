plugins {
    id("buildsrc.convention.kotlin-jvm")
    `java-library`
    `maven-publish`
}

group = "com.hyeonslab"
version = "0.0.6"

dependencies {
    api(libs.bundles.kotlinx.html)
    implementation(libs.kotlinx.io.core)
    implementation(libs.kotlinxSerialization)
}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Title"] = "Static Site Generator"
        attributes["Implementation-Version"] = version
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.hyeonslab"
            artifactId = "ssg"
            version = version
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.withType<AbstractArchiveTask> {
    setProperty("archiveBaseName", "hyeons-lab-ssg")
}
