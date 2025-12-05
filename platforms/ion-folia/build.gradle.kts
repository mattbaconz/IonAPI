plugins {
    id("com.gradleup.shadow") version "9.2.2"
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
