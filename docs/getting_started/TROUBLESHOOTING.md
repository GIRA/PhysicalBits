---
sort: 4
---
# Troubleshooting

Here I'm listing some of the most common problems found when using Physical Bits. If your problem doesn't appear on the list please submit an [issue](https://github.com/GIRA/PhysicalBits/issues). Thanks.

## I can't connect to the Arduino

Possible reasons:

### Wrong firmware

The first thing you need to __make sure is that the firmware is correctly uploaded__ to your Arduino board (see [here](./FIRMWARE.md)). After you uploaded the firmware please make sure you've selected the correct port and try again.

### Visual C++ 2010 Redistributable Package

If you're on __Windows__, you might need to __install the Visual C++ 2010 Redistributable Package__. You can download it from [here](https://www.microsoft.com/en-US/download/details.aspx?id=14632).

### Serial port permissions

On __Linux__, you might need to __add your user to the dialout group__ (as explained [here](https://askubuntu.com/a/58122)).

The easiest way to do it is:
1. Open a terminal and execute `sudo usermod -a -G dialout $USER`
2. Log out and then log back for the group changes to take effect (some systems might require a reboot)
