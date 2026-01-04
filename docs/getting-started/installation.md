# Installation

Add IonAPI to your project using Gradle or Maven.

### Gradle (Kotlin DSL)
```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // All-in-one (recommended)
    implementation("com.github.mattbaconz:IonAPI:1.5.0")
    
    // OR individual modules:
    // implementation("com.ionapi:ion-api:1.5.0")
    // implementation("com.ionapi:ion-database:1.5.0")
    // implementation("com.ionapi:ion-economy:1.5.0")
}
```

### Gradle (Groovy)
```groovy
repositories {
    mavenCentral()
    maven { url 'https://repo.papermc.io/repository/maven-public/' }
}

dependencies {
    implementation 'com.github.mattbaconz:IonAPI:1.5.0'
}
```

### Maven
```xml
<dependencies>
    <dependency>
        <groupId>com.github.mattbaconz</groupId>
        <artifactId>IonAPI</artifactId>
        <version>1.5.0</version>
    </dependency>
</dependencies>
```
