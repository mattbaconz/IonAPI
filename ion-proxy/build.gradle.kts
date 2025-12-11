dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
    
    api(project(":ion-api"))
    
    // Optional Redis support
    compileOnly("redis.clients:jedis:5.1.0")
}
