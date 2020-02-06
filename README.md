![alt text](src/main/resources/gpx_animator_banner.png "GPX Animator Banner")

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

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/zdila/gpx-animator)

For your first contribution to this repository, you can take a look at the issues listed here: [Good first issue](https://github.com/zdila/gpx-animator/contribute)


Build
-----------

GPX Animator uses the [Gradle](https://gradle.org/) build system to create the JAR file. You don't need to have Gradle installed on your system, this project makes use of the Gradle Wrapper.

```
./gradlew assemble
```

After a successful build, the JAR file can be found in the `build/libs` directory.

Run
-----------

To start GPX Animator from the sources, simply run the following command:

```
./gradlew run
```

If not already done, the project will be compiled automatically.

Test
-----------

In the directory `src/test/` tests are located that can be run with the following command:

```
./gradlew test
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

* Airplane icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [flaticon](https://www.flaticon.com/).
* Bicycle icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [flaticon](https://www.flaticon.com/).
* Bus icon made by [monkik](https://www.flaticon.com/authors/monkik) from [flaticon](https://www.flaticon.com/).
* Car icon made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [flaticon](https://www.flaticon.com/).
* Jogging icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [flaticon](https://www.flaticon.com/).
* Riding icon made by [mynamepong](https://www.flaticon.com/authors/mynamepong) from [flaticon](https://www.flaticon.com/).
* Sailing icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [flaticon](https://www.flaticon.com/).
* Ship icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [flaticon](https://www.flaticon.com/).
* Tramway icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [flaticon](https://www.flaticon.com/).
* Train icon made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [flaticon](https://www.flaticon.com/).
* Trekking icon made by [monkik](https://www.flaticon.com/authors/monkik) from [flaticon](https://www.flaticon.com/).

To create the installers, we use a free license of [Install4J](https://www.ej-technologies.com/products/install4j/overview.html) for open-source projects.

[![Install4J multi-platform installer builder](https://www.ej-technologies.com/images/product_banners/install4j_large.png)](https://www.ej-technologies.com/products/install4j/overview.html)
