# GPX Animator as Lib

if you want use GPX Animator as Lib see manual

## Gradle
1. Add the JitPack maven repository to the list of repositories: "https://jitpack.io"
```
repositories {
	....
	maven { url 'https://jitpack.io' }
}
```
2. Add the dependency in dependencies block: 'com.github.gpx-animator:gpx-animator:{TAG}'
```
dependencies {
    ...
    implementation 'com.github.gpx-animator:gpx-animator:v1.8.2'
}

``` 

## Maven 
1. Add the JitPack maven repository to the list of repositories: "https://jitpack.io"
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
2. Add the dependency in dependencies block:
```xml
<dependency>
    <groupId>com.github.gpx-animator</groupId>
    <artifactId>gpx-animator</artifactId>
    <version>Tag</version>
</dependency>
```

see details   https://jitpack.io/#gpx-animator/gpx-animator/