plugins {
    java
}

allprojects {
    group = "com.ionapi"
    version = "1.2.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io") // For VaultAPI
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

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
}

// Note: All-in-one JAR is handled by JitPack automatically
// Developers can use: implementation("com.github.mattbaconz:IonAPI:1.1.0")
// JitPack will bundle all modules together

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
        bottom = "Copyright © 2024 mattbaconz. All rights reserved."
    }
}
