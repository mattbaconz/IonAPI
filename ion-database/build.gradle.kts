dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")

    api(project(":ion-api"))

    // Database drivers (compileOnly so users can choose their driver)
    compileOnly("com.mysql:mysql-connector-j:8.2.0")
    compileOnly("org.postgresql:postgresql:42.7.1")
    compileOnly("org.xerial:sqlite-jdbc:3.44.1.0")
    compileOnly("com.h2database:h2:2.2.224")

    // HikariCP for connection pooling (Paper provides this)
    compileOnly("com.zaxxer:HikariCP:5.1.0")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("com.h2database:h2:2.2.224")
}

tasks.test {
    useJUnitPlatform()
}
