plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation(project(":ion-core"))
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
