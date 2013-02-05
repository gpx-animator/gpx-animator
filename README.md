GPX Animator
============

Introduction
------------

GPX Animator generates video frames from GPX as series of images.
Generated images can be then encoded to video with ffmpeg or similar software.

You can build it from sources or download from https://github.com/zdila/gpx-animator/raw/master/dist/GpxAnimator.jar.
For program help run:

```
java -jar GpxAnimator.jar --help
```

Basic usage
-----------

```
java -jar GpxAnimator.jar --input track.gpx
```

Features
--------
* supports multiple GPX tracks with mutliple track segments
* skipping idle parts
* configurable color, label, width and time offset per track
* configurable video size, fps and speedup or total video time
* background map from any public TMS server

Changelog
---------
https://github.com/zdila/gpx-animator/blob/master/CHANGELOG.md

