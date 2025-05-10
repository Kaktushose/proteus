# Proteus
### A modern, bidirectional type adapting library for Java.

---

## Resources

This is the official documentation for Proteus. You might also find the following resources helpful:

- [Proteus Wiki](https://github.com/Kaktushose/proteus/wiki)
- [Release Notes](https://github.com/Kaktushose/proteus/releases)

Having trouble or found a bug? Feel free to open an issue [here](https://github.com/Kaktushose/proteus/issues).

## Getting Proteus
Proteus is distributed via maven central.

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

## Example Usage
### Proteus Instance
The intended way of integrating Proteus in your project is using the global Proteus instance. Thus, all libraries or 
frameworks that work with Proteus have a shared pool of mappers and can benefit from each other. You can
access the global instance like this:

```java
Proteus proteus = Proteus.global();
```

This global instance is thread-safe. You can also create a new empty Proteus instance by calling [`Proteus#create()`](io.github.kaktushose.proteus/io/github/kaktushose/proteus/Proteus.html#create())
or use the builder via [`Proteus#builder()`](io.github.kaktushose.proteus/io/github/kaktushose/proteus/Proteus.html#builder()).
These instances will have their own pool of mappers and will not interact with the global instance. 

### Types
Proteus doesn't just use the type of the data that you want to convert. Instead, Proteus provides an additional layer.
You can define types by using one of the static factory methods of the [`Type`](io.github.kaktushose.proteus/io/github/kaktushose/proteus/type/Type.html)
class. 

The simplest way of creating a type reference looks like this:
```java
Type<String> stringType = Type.of(String.class);
```

This type doesn't have any identity yet. You can add context to your types by providing an implementation of the [`Format`](io.github.kaktushose.proteus/io/github/kaktushose/proteus/type/Format.html)
interface:

```java
Type<String> stringType = Type.of(new JSONFormat(), String.class);
```

### Mappers
In Proteus there are three types of mappers:

1. lossy unidirectional mappers
2. lossless unidirectional mappers
3. bidirectional mappers (always lossless)

The following example will register a lossless UniMapper from Integer to String:
```java
Type<Integer> integerType = Type.of(Integer.class);
Type<String> stringType = Type.of(String.class);

proteus.map(integerType).to(stringType, Mapper.lossless((source, context) -> MappingResult.success(String.valueOf(source))));
```

### Conversion
Proteus keeps track of all registered mappers by using an unweighted, undirected graph. This means that the conversion
can have any number of intermediate steps and no explicit mapper for `A -> B` is always required. Proteus will always search
for the shortest path possible to convert from one type to another. 

### Full Example

```java
// get proteus instance
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
