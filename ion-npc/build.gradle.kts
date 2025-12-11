dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("io.netty:netty-all:4.1.97.Final")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("com.google.code.gson:gson:2.10.1")
    
    api(project(":ion-api"))
}
