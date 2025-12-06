plugins {
    java
}

allprojects {
    group = "com.ionapi"
    version = "1.1.0"

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
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}

// Note: All-in-one JAR is handled by JitPack automatically
// Developers can use: implementation("com.github.mattbaconz:IonAPI:1.1.0")
// JitPack will bundle all modules together
