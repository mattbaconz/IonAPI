import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
    `maven-publish`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

// Disable the default jar task for the root project to prevent conflict with shadowJar publishing
tasks.jar {
    archiveClassifier.set("bare")
    enabled = false
}

allprojects {
    group = "com.github.mattbaconz"
    version = "1.4.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io") // For VaultAPI
    }
}

// Subprojects configuration
subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    
    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
            links("https://docs.oracle.com/en/java/javase/21/docs/api/")
            links("https://jd.papermc.io/paper/1.20/")
        }
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}

// Root project dependencies (all modules) for the "All-in-one" JAR
dependencies {
    implementation(project(":ion-api"))
    implementation(project(":ion-core"))
    implementation(project(":ion-gui"))
    implementation(project(":ion-item"))
    implementation(project(":ion-ui"))
    implementation(project(":ion-tasks"))
    implementation(project(":ion-database"))
    implementation(project(":ion-proxy"))
    implementation(project(":ion-npc"))
    implementation(project(":ion-placeholder"))
    implementation(project(":ion-inject"))
    implementation(project(":ion-compat"))
    implementation(project(":ion-economy"))
    implementation(project(":ion-redis"))
}

// Configure Shadow JAR for the root project to combine all modules
tasks.withType<ShadowJar> {
    archiveClassifier.set("") // Produce IonAPI-1.2.0.jar
    // Merge service files (like plugin.yml if multiple, though unlikely here)
    mergeServiceFiles()
    // Minimize to remove unused classes
    // minimize() // Removed to prevent empty JAR when root project has no code
}

// Publish the root project's Shadow JAR
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "IonAPI"
            version = project.version.toString()
            
            // Publish the shadow jar artifact
            artifact(tasks["shadowJar"]) {
                classifier = ""
            }
        }
    }
}

// Generate aggregated Javadoc for all modules
tasks.register<Javadoc>("aggregateJavadoc") {
    group = "documentation"
    description = "Generates aggregated Javadoc for all modules"
    
    setDestinationDir(file("$buildDir/docs/javadoc"))
    
    subprojects.forEach { subproject ->
        subproject.tasks.withType<Javadoc>().forEach { javadocTask ->
            source += javadocTask.source
            classpath += javadocTask.classpath
        }
    }
    
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        links("https://docs.oracle.com/en/java/javase/21/docs/api/")
        links("https://jd.papermc.io/paper/1.20/")
        windowTitle = "IonAPI v${project.version} API Documentation"
        docTitle = "IonAPI v${project.version}"
        header = "<b>IonAPI v${project.version}</b>"
        bottom = "Copyright © 2025 mattbaconz. All rights reserved."
    }
}
