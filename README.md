GPX Animator
============

Introduction
------------

GPX Animator generates video from GPX files.
More information can be found at https://gpx-animator.app.

For program help run:

```
java -jar GpxAnimator.jar --help
```

Basic usage
-----------

```
java -jar GpxAnimator.jar --input track.gpx
```

Contributing
-----------

You can open this project in a preconfigured Gitpod online IDE based on Theia (Visual Studio Code) and edit, run, test, debug and commit directly from your browser.

[Start Gitpod online IDE](https://gitpod.io/#https://github.com/zdila/gpx-animator)

Build
-----------

GPX Animator uses the [Maven](https://maven.apache.org/) build system to create the JAR file. You don't need to have Maven installed on your system, this project makes use of the Maven Wrapper.

```
./mvnw clean package
```

After a successful build, the JAR file can be found in the `target` directory.

Run
-----------

To start GPX Animator from the sources, simply run the following command:

```
./mvnw exec:java
```

The artifact must have been created before (see [Build](#build) section).

Test
-----------

In the directory `src/test/` tests are located that can be run with the following command:

```
./mvnw test
```

Features
--------
* supports multiple GPX tracks with multiple track segments
* skipping idle parts
* configurable color, label, width and time offset per track
* configurable video size, fps and speedup or total video time
* background map from any public TMS server

Tutorials
--------
- [GPS Tracks Animation mit "GPX Animator" (Marcus Bersheim)](https://www.youtube.com/watch?v=AtcBVrbB6bg) :de:


Credits
--------
Icons included in application and their source:

* Bicycle icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [flaticon](https://www.flaticon.com/).
* Trekking icon made by [monki](https://www.flaticon.com/authors/monkik) from [flaticon](https://www.flaticon.com/).
