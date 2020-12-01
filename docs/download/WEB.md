# Physical Bits (Web edition)

## Latest release

| Platform | File(s) |
| :---: |:---:|
| ![windows](https://gira.github.io/PhysicalBits/img/windows.png)<br>Windows | [PhysicalBits.v0.4.1-web-win32.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.1/PhysicalBits.v0.4.1-web-win32.zip) |
| ![linux](https://gira.github.io/PhysicalBits/img/linux.png)<br>Linux | [PhysicalBits.v0.4.1-web-linux.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.1/PhysicalBits.v0.4.1-web-linux.zip) |
| ![macOS](https://gira.github.io/PhysicalBits/img/apple.png)<br>macOS | [PhysicalBits.v0.4.1-web-darwin.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.1/PhysicalBits.v0.4.1-web-darwin.zip) |

For older versions visit our [releases page](https://github.com/GIRA/PhysicalBits/releases).

## Starting the application

The web version should work on all platforms.

First, extract the contents of the zip file you've just downloaded into a folder of your preference.

You should see the following files:

![start_web.1](../img/start_web.1.png)

Then, if your on Windows just run the `start.bat`. If your on Linux or macOS run the `start.sh`.

You should see a terminal pop up and, after a few seconds, a browser should open with the Physical Bits IDE.

![start_web.2](../img/start_web.2.png)

__IMPORTANT__: If, for some reason the IDE doesn't open automatically, you can do it yourself by pointing your browser to [localhost:3000/ide/index.html](http://localhost:3000/ide/index.html).

![start_web.3](../img/start_web.3.png)

### Installing the firmware

In order for Physical Bits to connect to your Arduino board you'll first need to upload the firmware using the [Arduino IDE](https://www.arduino.cc/en/Main/Software).

The `UziFirmware.ino` can be found on the `/firmware` directory inside the zip you just downloaded.

This step needs to be done once for every board you want to use with Physical Bits. We're working on making this step automatic but, for now, you'll need to do it yourself.

__IMPORTANT__: On some boards a "Low memory" warning is displayed after compilation. Don't worry about it, that is by design. The firmware will statically allocate a big chunk of memory to store the user programs at startup. We do this to be able to control how the memory is used and to properly detect and handle memory issues.

You can start programming now!

## Prerequisites

### Microsoft Visual C++ 2010 Redistributable Package

On Windows you will also need the Visual C++ 2010 Redistributable Package.

If you don't have it already you can download it from [here](https://www.microsoft.com/en-US/download/details.aspx?id=14632).
