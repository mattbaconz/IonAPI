plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation(project(":ion-core"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
