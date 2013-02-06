FAQ
===

Q: Application is crashing with java.lang.OutOfMemoryError: Java heap space
---------------------------------------------------------------------------

A: Your GPX trace covers probably too big area. Try to specify lower --zoom, --width and/or --height.

Q: Does it run on Linux, Windows, WhateverOS?
---------------------------------------------

A: Application will run on any platform where you can run Java.

For Ubuntu / Debian it is very easy to install:
```
sudo apt-get install default-jre ffmpeg
java -jar GpxAnimator.jar --help
```

Q: My GPX is missing point timestamps
-------------------------------------

A: No problem. Add --forced-point-time-interval option.

Q: Video is very short or long
------------------------------

Fine-tune --speedup option or specify exact --total-time in seconds.

Q: I have some other problem
----------------------------

Feel free to write me at m.zdila@freemap.sk.

