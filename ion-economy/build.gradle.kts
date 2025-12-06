dependencies {
    implementation(project(":ion-api"))
    implementation(project(":ion-database"))
    
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("org.jetbrains:annotations:24.0.0")
    
    // Vault API for economy hook
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}
