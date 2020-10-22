# Download

## Latest release

| Platform | File(s) |
| :---: |:---:|
| ![web](https://gira.github.io/PhysicalBits/img/browsers.png)<br>Web | [PhysicalBITS.v0.4.0-web.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.0/PhysicalBITS.v0.4.0-web.zip) <br> (Cross-platform)|
| ![web](https://gira.github.io/PhysicalBits/img/windows.png)<br>Windows | [PhysicalBITS.v0.4.0-win32-ia32.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.0/PhysicalBITS.v0.4.0-win32-ia32.zip) (32 bits)<br>[PhysicalBITS.v0.4.0-win32-x64.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.0/PhysicalBITS.v0.4.0-win32-x64.zip) (64 bits) |
| ![web](https://gira.github.io/PhysicalBits/img/macos.png)<br>macOS | [PhysicalBITS.v0.4.0-darwin-x64.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.0/PhysicalBITS.v0.4.0-darwin-x64.zip) |

For older versions visit our [releases page](https://github.com/GIRA/PhysicalBits/releases).

## Installation

### Installing Java

Unfortunately, all versions require Java. We're working on removing this dependency but, for now, you'll need it in order to run Physical Bits.

If you already have a version of Java installed on your computer you can skip this step. Otherwise, we recommend you to download and install a version of the [OpenJDK](https://openjdk.java.net/).

### Installing the firmware

In order for Physical Bits to connect to your Arduino board you'll first need to upload the firmware using the [Arduino IDE](https://www.arduino.cc/en/Main/Software).

The `UziFirmware.ino` can be found on the `/firmware` directory inside the zip you just downloaded.

This step needs to be done once for every board you want to use with Physical Bits. We're working on making this step automatic but, for now, you'll need to do it yourself.

## Starting the application

The web version should work on all platforms, after unzipping simply run the `start.bat` (on Windows) or `start.sh` (on Linux or macOS). After a few seconds a browser should open with the Physical Bits IDE.

The desktop versions use [electron](https://www.electronjs.org/) to provide a native experience but in the background they run the same server as the web version. Simply run the `PhysicalBits.exe` (on Windows) or `PhysicalBits.app` (on macOS).
