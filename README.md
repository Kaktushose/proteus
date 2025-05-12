[![Generic badge](https://release-badges-generator.vercel.app/api/releases.svg?user=kaktushose&repo=proteus&gradient=92e236,92e236)](https://github.com/Kaktushose/proteus/releases/latest)
[![Java CI](https://github.com/Kaktushose/proteus/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/Kaktushose/proteus/actions/workflows/ci.yml)
[![Release Deployment](https://github.com/Kaktushose/proteus/actions/workflows/cd.yml/badge.svg)](https://github.com/Kaktushose/proteus/actions/workflows/cd.yml)
[![license-shield](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)]()
# Proteus
Proteus (named after the Greek shape-shifting sea god) is a modern, bidirectional type adapting library for Java.

```java
Proteus proteus = Proteus.global();

// define types and mapper
Type<Integer> integerType = Type.of(Integer.class);
Type<String> stringType = Type.of(String.class);

// register mapper
proteus.map(integerType).to(stringType, Mapper.lossless((source, context) -> MappingResult.success(String.valueOf(source))));

// attempt conversion
ConversionResult<String> result = proteus.convert(0, integerType, stringType);

// check result
switch (result) {
    case ConversionResult.Success<String>(String success) -> System.out.println(success);
    case ConversionResult.Failure<?> failure -> System.out.println(failure.detailedMessage());
}
```

## Features

- Bidirectional, Multistep Type Conversion

- Extensible & Threadsafe API

- Shortest Path Usage & Path Caching

- Comprehensive Error Messages

- Framework-friendly Design

## Download
Proteus is distributed via maven central. You can find the latest release [here](https://github.com/Kaktushose/proteus/releases/latest).

### Gradle
```kotlin
repositories {
   mavenCentral()
}
dependencies {
   implementation("io.github.kaktushose:proteus:VERSION")
}
```
### Maven
```xml
<dependency>
   <groupId>io.github.kaktushose</groupId>
   <artifactId>proteus</artifactId>
   <version>VERSION</version>
</dependency>
```

## Contribution

If you think that something is missing, and you want to add it yourself, feel free to open a pull request. Please consider opening an issue
first, so we can discuss if your changes fit to Proteus. 
