# Arduino Simulator

This project is a very simple (and limited) simulator for the Arduino platform. We followed the instructions found [here](https://playground.arduino.cc/Code/VisualStudio/) (and modified the code a little). After that we added a C# GUI to make the simulator more interactive.

![simulator](/docs/img/simulator.png)

The goal of this project is to be able to develop, debug, and test the firmware without an actual Arduino board. In that regard, this project has been very successful.

It allowed us to use Visual Studio (which is much nicer than the Arduino IDE) to write most of the firmware code. Moreover, it allowed us to write unit tests and to use a debugger to execute the code step by step when needed.

The simulation is not perfect, though, so testing on the actual hardware is still necessary. But for the most part, this has been an invaluable tool.

__PROBLEM__: Unfortunately, the GUI is implemented using C# and [Windows Forms](https://docs.microsoft.com/en-us/dotnet/desktop/winforms/?view=netdesktop-5.0). At the time that seemed to be a reasonable choice because we didn't care much for portability and everyone in the team used Windows anyway. However, now I regret that decision and would like to replace Windows forms with a cross-platform alternative. I would probably keep C# as it doesn't bother me much (and it's a nice language).
