# Standalone

## Latest release

| Platform | File(s) |
| :---: |:---:|
| ![windows](https://gira.github.io/PhysicalBits/img/windows.png)<br>Windows | [PhysicalBITS.v0.4.0-win32-ia32.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.0/PhysicalBITS.v0.4.0-win32-ia32.zip) (32 bits)<br>[PhysicalBITS.v0.4.0-win32-x64.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.0/PhysicalBITS.v0.4.0-win32-x64.zip) (64 bits) |
| ![macOS](https://gira.github.io/PhysicalBits/img/macos.png)<br>macOS | [PhysicalBITS.v0.4.0-darwin-x64.zip](https://github.com/GIRA/PhysicalBits/releases/download/v0.4.0/PhysicalBITS.v0.4.0-darwin-x64.zip) |
For older versions visit our [releases page](https://github.com/GIRA/PhysicalBits/releases).

## Starting the application

The desktop versions use [electron](https://www.electronjs.org/) to provide a native experience but in the background they run the same server as the web version.

First, extract the contents from the zip file into a folder of your preference.

Depending on your operating system, you should see something like this:

![start_desktop.1](./img/start_desktop.1.png)

Now, you just have to run the `PhysicalBits.exe` (on Windows) or `PhysicalBits.app` (on macOS).

The following loading screen should appear right away and, after a few seconds of the robot jumping impatiently, the IDE should open.

![start_desktop.2](./img/impatient_bot.gif)
![start_desktop.3](./img/start_desktop.3.png)

### Installing the firmware

In order for Physical Bits to connect to your Arduino board you'll first need to upload the firmware using the [Arduino IDE](https://www.arduino.cc/en/Main/Software).

The `UziFirmware.ino` can be found on the `/firmware` directory inside the zip you just downloaded.

This step needs to be done once for every board you want to use with Physical Bits. We're working on making this step automatic but, for now, you'll need to do it yourself.

__IMPORTANT__: On some boards a "Low memory" warning is displayed after compilation. Don't worry about it, that is by design. The firmware will statically allocate a big chunk of memory to store the user programs at startup. We do this to be able to control how the memory is used and to properly detect and handle memory issues.

You can start programming now!

## Prerequisites

### Java

Unfortunately, all versions require Java. We're working on removing this dependency but, for now, you'll need it in order to run Physical Bits.

If you already have a version of Java installed on your computer you can skip this step. Otherwise, we recommend you to download and install a version of the [OpenJDK](https://openjdk.java.net/).

### Microsoft Visual C++ 2010 Redistributable Package

On Windows you will also need the Visual C++ 2010 Redistributable Package.

If you don't have it already you can download it from [here](https://www.microsoft.com/en-US/download/details.aspx?id=14632).
