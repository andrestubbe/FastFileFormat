# Building FastFileFormat from Source

## Prerequisites

- JDK 17+
- Maven 3.9+

## Build

```bash
mvn clean package
```

To run unit tests:
```bash
mvn test
```

## Installation

### JitPack (Recommended)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastFileFormat</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastFileFormat:0.1.0'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

## Download Pre-built JAR

See [Releases Page](https://github.com/andrestubbe/FastFileFormat/releases)
