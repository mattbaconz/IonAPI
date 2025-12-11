plugins {
    id("java-library")
}

dependencies {
    api(project(":ion-api"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
}
