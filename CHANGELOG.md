# Changelog

## Version 1.7.0

**Release date: work in progress** 🚧

### New features

* Realtime preview while rendering is in progress (can be disabled)
* System notifications when rendering stops or finishes
* Map data update over the air
* Specify a separate font for waypoints (optional)
* Read speed from GPX file (if present) for more acurate speed data
* All important features in the menu are now accessible via hotkeys
* Recently opened files can be accessed via hotkeys
* All dialogs can be closed using the `Esc` key
* License information can be found in the `Help` menu
* New plugin support for extending GPX Animator easily
* Delay the start of the animation (keep first frame)
* Force mandatory map attributions (if needed)

### Fixed bugs

* Accidentally cropping of photos
* Crazy path information with dots in filenames
* Incorrect handling of keep last frame option
* Incorrect handling of photo directory in saved configurations
* Incorrect handling of background image in saved configurations
* Fixed `NoSuchElementException` when loading empty GPX files
* Fixed a bunch of (possible) `NullPointerException`s

### Maintenance

* Replaced the end of life [Xuggler](http://www.xuggle.com/xuggler/status) library with [JavaCV](https://github.com/bytedeco/javacv)

---

## Version 1.6.1

Release date: 2021-03-14

* Fixing date time parsing error when time zone information is missing
* Fixing date from the 1970s when forced point time interval option was set
* Fixing waypoints do not require a time anymore
* Fixing negative total time crashing the rendering process
* Accepting uppercase file endings of GPX and PNG files
* Adding trim options to command line help

---

## Version 1.6.0

Release date: 2021-01-09

* Adding multiple GPX files at once
* Adding an animation to smoothly fade photos in and out
* Providing alternative speed units (km/h, mph, min/km, min/mi, knots...)
* Adding support for moving map
* Adding support for pre-drawing a track
* Adding support to change the pre-draw color per track
* New error dialog with additional internal information for better error reports
* New and clear error message on missing track data in GPX file
* Fixing `NullPointerException` when starting GPX Animator the first time
* Fixing `NullPointerException` when showing the Changelog
* Fixing a bug causing image series export to stop working
* Migrating to Java 15
* Fixing a bunch of compiler warnings
* Adding a Motorcycle icon
* Adding support for choosing an external PNG file as a track icon
* Track icons can now be mirrored to match the general direction
* Adding font selection for the text in the animation
* Margin for track, logo, attribution and informations can be set separately
* Positions of logo, attribution, and informations are now configurable
* Logo, attribution, and informations can now hidden
* Adding support for background images (including transparency in PNG files)
* Adding support for track point comments in GPX files
* Fixing error in video frame size calculation with user specified bounding box
* Fixing some errors in animation length calculation when total time was set
* Fixing errors with incomplete latitude/longitude (bounding box) settings
* Fixing 21 broken background maps
* Removing 8 broken background maps
* Up to 15 % faster rendering

---

## Version 1.5.2

Release date: 2020-08-21

* Improved error messages
* Fixing a bug which prevented disabling the text in the animation.
* Fixing a bug in percentage calculator (never reached 100%)
* Fixing crazy behaviour of Latitude and Longitude spinner
* Fixing spinners not being able to go negative using the down arrow on first attempt
* Automatic repair of broken files produced by Sigma Rox 12 bicycle computer
* Show correct version number when using a saved file from an older version

---

## Version 1.5.1

Release date: 2020-08-07

* Fixing a bug that prevents photos from being shown in the animation
* Fixing a bug that does not load the photos directory from saved files
* Fixing a bug that broke the command line use since v1.4
* Fixing a bug that shows an old status message on the progress bar

---

## Version 1.5

Release date: 2020-07-03

**Most important changes for users:**
* Show the changelog on the first start after install/update
* Adding a button to easily select common video resolutions
* Adding a bunch of new track icons
* Adding profile picture / logo to the animation
* Making video background color configurable
* Fixing UI on HiDPI (high-resolution) displays
* Fixing loading of *Keep Last Frame* configuration option
* Default output directory is no longer the application directory
* Saving a configuration as a default setting
* New application wide settings (_File > Preferences_)
* Adding preferences to set a custom default track color
* Adding preferences to configure the map tile cache
* Tile cache now enabled by default (24 hour caching)
* New installers for Windows, macOS and Linux systems
* Integration of automatic application updates using the installer
* New translation for German users (give feedback, please)
* New support e-mail address on the website
* New application icon and splash screen

**Most important changes for developers:**
* GPX-Animator now uses Gradle to build the project
* Integrating Spotbugs to search for potential bugs automatically
* Integrating PMD to search for potential bugs automatically
* Default Java version for GPX Animator is now Java 11 LTS
* Using logging instead direct console output
* Internationalizing the codebase for easier translation

---

## Version 1.4

Release date: 2019-12-20

* showing speed in animation
* adding photos to the animation
* adding coordinates to the animation
* asking before overwriting an existing output file
* adding link to the FAQ in the help menu
* adding "Open Recent File" menu
* adding new map source: OpenTopoMap
* adding optional map tile cache to reduce download size
* adding optional icons to the current track position
* allow negative time offset for GPX tracks
* UI consistency: Make all labels upper case words
* GPX-Animator now has a custom domain: https://gpx-animator.app
* fixed incorrect zoom factor calculation
* fixed time offset problem with negative time offset values
* fixed a lot of minor issues

---

## Version 1.3.1

Release date: 2018-07-31

* fixed some issues and added compatibility for Java 9+

---

## Version 1.3.0

Release date: 2015-11-22

* configurable bounding box

---

## Version 1.2.4

Release date: 2015-11-02

* fixed #18 (error saving configuration)

---

## Version 1.2.3

Release date: 2015-08-23

* fixed NPE on empty attribution
* fixed not showing marker when tail was empty or zero
* updated maps
* removed unused --debug option

---

## Version 1.2.2

Release date: 2015-01-18

* fixed NPE when adding new track
* preselect different color to newly added tracks

---

## Version 1.2.1

Release date: 2015-01-11

* handle all ISO 8601 date formats in GPX
* remember directory in file dialogs
* store relative paths in project XML file

---

## Version 1.2.0

Release date: 2014-02-03

* preparation for interactive map view configuration
* fixed bug in computing video dimensions

---

## Version 1.1.0

Release date: 2013-06-24

* configurable map attribution
* display track label in GUI tab title
* hide inactive marker after tail timeout
* added program icon
* fixed reading of alpha channel
* fixed alpha channel interpolation for tail
* fixed parsing of GPX dates with milliseconds

---

## Version 1.0.0

Release date: 2013-03-23

* added configuration GUI
* direct video rendering support
* minor rendering improvements

---

## Version 0.8

Release date: 2013-02-09

* added waypoint support

---

## Version 0.7

Release date: 2013-02-05

* added support for forced time intervals of GPS points
* configurable flashback color and duration

---

## Version 0.6

Release date: 2013-02-04

* added video dimension settings
* added track offset support (per track)
* skipping idle video frames

---

## Version 0.5

Release date: 2013-02-03

* outlining texts
* improved user experience ;-)
* fixing some indexed PNG background map tiles

---

## Version 0.4

Release date: 2013-02-03

* indicate inactive locations
* track multisegment support
* direct color specification

---

## Version 0.3

Release date: 2013-02-03

* background map support

---

## Version 0.2

Release date: 2013-02-02

* configurable hues
* more parameters to configure
* marker labels
* multitrack support
* improved tail highlighting - now based on time
* customizable tail length

---

## Version 0.1

Release date: 2013-01-31

* initial version
