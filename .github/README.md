# AnnotatedConfig
![banner](banner.png)

[![license](https://img.shields.io/github/license/articdive/AnnotatedConfig.svg)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg)](https://github.com/RichardLitt/standard-readme)
[![Discord Shield](https://discordapp.com/api/guilds/525595722859675648/widget.png?style=shield)](https://discord.gg/UnQtnUS)

AnnotatedConfig is a simple-to-use java-library for creating configs using annotations on fields.

AnnotatedConfig was created in mid 2020 by Articdive.  
It works for all Java 8+ apps and is built using [Gradle](https://gradle.org/).

## Table of Contents
- [Install](#install)
- [Usage](#usage)
- [Planned Features](#planned-features)
- [Maintainers](#maintainers)
- [Acknowledgements](#acknowledgements)
- [Contributing](#contributing)
- [License](#license)

## Install
### Maven and Gradle
To add AnnotatedConfig to your project using [Maven](http://maven.apache.org/) or [Gradle](https://gradle.org/):

Repository (Maven):
```
<repository>
    <id>articdive-nexus</id>
    <url>https://repo.articdive.de/repository/maven-public/</url>
</repository>
```
Dependency (Maven):
```
<dependency>
    <groupId>de.articdive</groupId>
    <artifactId>annotated-config-TYPE</artifactId>
    <version>VERSION</version>
</dependency>
```

Repository (Gradle Kotlin DSL)
```
repositories {
    maven {
        name = "Articdive's nexus repository"
        url = uri("https://repo.articdive.de/repository/maven-public/")
    }
}
```
Dependency (Gradle Kotlin DSL)
```
dependencies {
    // AnnotatedConfig Library
    implementation("de.articdive:annotated-config-TYPE:VERSION")
}
```

### Config Types
The library currently only supports Hocon as the configuration types, the artifactID is:
```
annotated-config-hocon
```

## Usage
### Defining a config:
You must extend a configuration type depending on the implementation you choose.
> HoconConfiguration  
```java
@SuppressWarnings("InnerClassMayBeStatic")
public class ExampleConfig extends HoconConfiguration {
    @Property("path")
    @Comment("This is a boolean! It will default to false if it is undefined.")
    public boolean booleanValue = false;

    @Property("some_double")
    @Comment("This is a double! It will default to 1.11 if it is undefined.")
    public double doubleValue = 1.11D;

    @Section("path_of_section")
    @Comment("Section A")
    public class SectionA {
        @Property("name")
        @Comment("This is a name.")
        public String name = "John"
    }
}
```

### Creating a config
Once you have created the properties and sections of your config you can create it like this:
```java
ExampleConfig myConfig = ExampleConfig.createConfiguration(
    new File("somepath/my_config.conf"),
    ExampleConfig.class
);
```
It is extremely important to include the class of the configuration itself when creating it!

### Defaults
If you have a default file e.g. a language file with translations you can also include a default source URL
and it will automatically copy that file from the URL before parsing it.

An example of this:
```java
ExampleConfig myConfig = ExampleConfig.createConfiguration(
    new File("my_config.conf"),
    true,
    getClass().getResource("my_default_config.conf")
    ExampleConfig.class
);
```

### Accessing a property
To access the config, we can use a getter or make the variable directly accessible 
(public, package-protected). We just need to simply access the variable.  
Using a public variable:
```java 
boolean someBoolean = myConfig.booleanValue;
```
Using a getter:
```java 
boolean someBoolean = myConfig.isBooleanValue();
```

### Accessing nested properties
To allow similar behaviour for nested properties it is important to use a **non-static** nested class
The config that is created stores 1 instance of the nested class in the "objectMap" protected variable.
To access that instance you will have to use a getter.

This is how such a getter would look like:
```java
public SectionA getSectionA() {
    return (SectionA) objectMap.get(SectionA.class);
}
```
Once you get the section you can access the variable directly (or use its own getter), you can also
write getters for those variables directly in the root-class e.g.:
```java
public String getSectionAName() {
    return ((SectionA) objectMap.get(SectionA.class)).name;
}
```

### Setting a property
Setting a property is just as easy as getting it, just access the variable directly or declare a setter in the config, 
in this case using a setter might be ideal as after you set the variable.
It also must be updated to the file stored on the disk by running the method save().

As an example:
```java
public void setSomeBoolean(boolean newValue) {
    this.someBoolean = newValue;
    save();
}
```

## Planned Features
Planned features include Toml and Yaml and possibily properties support.

The eo-yaml library is not yet adequately developed to handle AnnotatedConfig and SnakeYaml does not allow for handling
comments, if you know of any java parsers that support comments processing please open an issue to let us know.
Once some issues have been solved with eo-yaml, Yaml support will be added.

## Maintainers
[@Articdive](https://www.github.com/Articdive/)

## Acknowledgements
[@Typesafe Config](https://github.com/lightbend/config)

[@eo-yaml](https://github.com/decorators-squad/eo-yaml)

## Contributing
See [the contributing file](CONTRIBUTING.md)!

## License
[GNU General Public License v3.0 or later © Articdive ](../LICENSE)