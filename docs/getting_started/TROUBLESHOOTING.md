---
sort: 5
---
# Troubleshooting

These are some of the most common problems found when using Physical Bits. If your problem doesn't appear on the list please submit an [issue](https://github.com/GIRA/PhysicalBits/issues). Thanks.

## I can't connect to the Arduino!

There are multiple reasons why Physical Bits might fail to connect to your Arduino board. Here are some of the possible reasons:

### Wrong firmware

The first thing you need to do is __make sure that the firmware is correctly uploaded__ to your Arduino board. You can find the instructions [here](./FIRMWARE.md). After you uploaded the firmware please make sure you've selected the correct port and try connecting again.

### Visual C++ 2010 Redistributable Package (Windows only)

If you're on __Windows__, you might need to install the [Visual C++ 2010 Redistributable Package](https://www.microsoft.com/en-US/download/details.aspx?id=14632)

### Serial port permissions (Linux only)

On __Linux__, you might need to __add your user to the dialout group__ (as explained [here](https://askubuntu.com/a/58122)). The easiest way to do it is:
1. Open a terminal and execute `sudo usermod -a -G dialout $USER`
2. Log out and then log back for the group changes to take effect (some systems might require a reboot)
