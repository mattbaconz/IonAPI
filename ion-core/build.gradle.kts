plugins {
    id("java-library")
}

dependencies {
    api(project(":ion-api"))
    compileOnly("org.jetbrains:annotations:24.0.0")
}
