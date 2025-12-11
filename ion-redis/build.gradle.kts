dependencies {
    implementation(project(":ion-api"))
    
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
    
    // Lettuce Redis client - marked as compileOnly to keep IonAPI lightweight
    // Developers must shade Lettuce themselves if using ion-redis
    compileOnly("io.lettuce:lettuce-core:6.3.0.RELEASE")
}
