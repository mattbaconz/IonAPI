dependencies {
    api(project(":ion-api"))
    
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
    
    // Testing frameworks (provided as API for test consumers)
    api("org.junit.jupiter:junit-jupiter-api:5.10.0")
    api("org.mockito:mockito-core:5.7.0")
}
