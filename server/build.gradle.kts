import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-common")
    id("com.gradleup.shadow") version "9.4.1"
}

dependencies {
    implementation(project(":http"))
    implementation(project(":logger"))
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("jweb")
    archiveClassifier.set("") // Removes the "-all" suffix from the jar filename
    manifest {
        attributes["Main-Class"] = "nturbo1.WebServerApp"
    }
    minimize()

    // Optional: Merges resource files with the same name (e.g., Log4j configs)
    // mergeServiceFiles()
}
