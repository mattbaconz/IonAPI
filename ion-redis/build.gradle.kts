dependencies {
    implementation(project(":ion-api"))
    
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
    
    // Lettuce Redis client
    implementation("io.lettuce:lettuce-core:6.3.0.RELEASE")
}
