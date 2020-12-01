---
sort: 1
---

# Installing the firmware

In order for Physical Bits to connect to your Arduino board you'll first need to upload the firmware using the [Arduino IDE](https://www.arduino.cc/en/Main/Software).

The `UziFirmware.ino` can be found on the `/firmware` directory. It's just a simple Arduino sketch, the only difference with most other sketches is that it's composed of multiple files.

![firmware](../img/firmware.png)

__IMPORTANT 1__: You need to upload the firmware *once* for every board you want to use with Physical Bits. We're working on making this step automatic but, for now, you'll need to do it yourself.

__IMPORTANT 2__: On some boards a *"Low memory"* warning is displayed after compilation. Don't worry about it, that is by design. The firmware will statically allocate a big chunk of memory to store the user programs at startup. We do this to be able to control how the memory is used and to properly detect and handle memory issues.
