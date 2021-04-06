# Physical Bits firmware

The Physical Bits firmware is just a simple Arduino sketch written in C++. We designed it this way because we wanted users to be able to easily upload it with the [Arduino IDE](https://www.arduino.cc/en/Main/Software) (and also modify it, if necessary).

![firmware](/docs/img/firmware.png)

## Quick start

Simply open the `.ino` file on the [Arduino IDE](https://www.arduino.cc/en/Main/Software), choose the board type and port, and hit the upload button.

__IMPORTANT__: On some boards a "Low memory" warning is displayed after compilation. Don't worry about it, that is by design. The firmware will statically allocate a big chunk of memory to store the user programs at startup. We do this to be able to control how the memory is used and to properly detect and handle memory issues.
