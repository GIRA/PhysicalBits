# Physical Bits

[Physical Bits](https://gira.github.io/PhysicalBits/) (formerly known as UziScript) is a web-based programming environment for educational robotics that supports live coding and autonomy using a hybrid blocks/text programming language.

The current implementation supports Arduino as hardware platform. We have tested the firmware using several different boards, including: [Arduino UNO](https://store.arduino.cc/usa/arduino-uno-rev3), [Micro](https://store.arduino.cc/usa/arduino-micro), [Nano](https://store.arduino.cc/usa/arduino-nano), [MEGA 2560](https://store.arduino.cc/usa/mega-2560-r3), and [Yun](https://store.arduino.cc/usa/arduino-yun-rev-2). We have also received reports of it working successfully on other compatible boards such as [DuinoBot](https://www.robotgroup.com.ar/), [Educabot](https://educabot.com/), and [TotemDUINO](https://totemmaker.net/product/totemduino-arduino/).

We also plan to support other platforms such as [ESP8266](https://en.wikipedia.org/wiki/ESP8266) and [ESP32](https://en.wikipedia.org/wiki/ESP32) in the future.

## Table of contents

* [Motivation](#motivation)
* [Proposed Solution](#proposed-solution)
  * [Key Features](#key-features)
* [Download and Installation](#download-and-installation)
* [Usage](#usage)
  * [Text editor](#text-editor)
  * [Visual editor](#visual-editor)
  * [Monitor](#monitor)
* [Contributing](#contributing)
* [Description of the language](#description-of-the-language)
* [Implementation](#implementation)
  * [Firmware](#firmware)
  * [Task scheduler](#task-scheduler)
  * [Compiler](#compiler)

## Motivation

Most educational programming environments share the same blocks-based programming style. This is not surprising, studies show that visual programming environments help to learn programming by removing the possibility of syntax errors and simplifying the programming language, which allows students to focus on understanding the underlying concepts. However, a common problem we found when teaching using visual programming environments is the eventual transition to text-based languages. Most block-based programming environments don't aid the learner in this transition, instead they are more concerned with hiding the complicated syntax rules. 
This has led to a couple of problems:
1. Students tend to think of block-based programming as not "real programming". This perceived lack of authenticity has the potential to damage the language effectiveness as students would prefer to learn what they perceive to be a "real language" such as Java or C++ instead of a "toy language" like Scratch. 
2. Students transitioning from a blocks-based programming language to a text-based programming language can suffer from a problem referred to as "syntax overload". Some students feel overwhelmed and frustrated by the strict syntax requirements of the text-based languages. 
3. Students could potentially end up acquiring poor programming habits that damage their ability to work successfully with text-based languages.

Additionally, some studies reveal that learning the syntax is not even the main problem because the students only struggle with it at an early stage. Instead, a more challenging aspect of learning programming is to be able to correctly predict the impact that changes to the source code have on the behavior of the program when it is executed. Teaching programming presents a dichotomy that is not easy to grasp for beginners: the source code is explicit and visible while the execution dynamics are implicit and harder to understand.

In order to solve this problem, some studies propose to design educational programming environments in a way that makes the relationship between the source code and its effects more explicit. In this regard, most virtual introductory programming environments provide a feature often described as liveness: allowing to change a program while it is running. In a live programming environment, the users change the program and receive immediate feedback on the effect of the change, without requiring any manual compilation steps and minimizing the time wasted waiting for the code to begin executing. Live programming shortens the feedback loop and encourages experimentation and programming by "Trial & error". Some environments also support monitoring the internal state of the program by showing the value of the variables as well as highlighting the currently executing blocks. Although these features are present in most virtual introductory programming environments, they are rarely seen in environments designed to program physical devices such as robots. And the environments that support live programming usually do so in detriment of the autonomy of the robot, requiring a computer connected at all times in order to run the programs and send the commands to the robot as they are executed. Due to the latency of the communication, these environments are limited to projects that do not require precise timing and cannot be used in many robotics competitions.

This project is our attempt to solve this problems and provide a better programming experience for educational robotics.

## Proposed Solution

We propose the implementation of a concurrent programming language supported by a virtual machine running on the Arduino. We call this language UziScript and we expect it to become a suitable compilation target for visual programming environments such as [Physical Etoys](http://tecnodacta.com.ar/gira/projects/physical-etoys), [Scratch for Arduino](http://s4a.cat), and [Ardublock](http://blog.ardublock.com/), among others.

### Key Features

* __Block-based and text-based programming__: UziScript includes a block-based programming language suitable for beginners but it also supports text-based programming for more advanced users. To ease the transition UziScript automatically generates the textual code from the blocks (and viceversa).
* __Concurrency__: Most educational robotics projects require the implementation of a device that performs two or more simultaneous tasks. UziScript allows the definition of concurrent tasks that will be executed independently from each other.
* __Autonomy__: UziScript programs are stored and executed autonomously in the Arduino without requiring a connection to the computer.
* __Interactive programming__: If the board is connected to the computer UziScript allows to inspect and monitor the program state while it runs. Furthermore, every change made to the program can be automatically compiled and transmitted to the Arduino, which allows to see the effects of the change almost immediately.
* __Debugging__: Without debugging tools the process of fixing programming errors can be frustrating for an inexperienced user. UziScript's debugger provides mechanisms for error handling and step-by-step code execution.
* __Portability__: Although we currently only support Arduino, we plan to port the VM to other hardware platforms.
* __Open source__: All the code in this project is open source (see [LICENSE](/LICENSE)).

## Download and Installation

You can find the latest release [here](https://github.com/GIRA/UziScript/releases).

## Usage

IMPORTANT: This whole section is outdated since v0.3.

After unzipping the package you'll find two folders: "UziFirmware" and "Tools".

For every board you want to use with UziScript you'll first need to upload the firmware. This is a very simple procedure, since the UziScript firmware is just an Arduino sketch you can use the Arduino IDE as you would with any other sketch. For more detailed instructions on how to upload sketches, see [here](https://www.arduino.cc/en/Guide/Environment).

Once you have uploaded the firmware to your board you'll need to open the "Tools" folder and click on the appropriate file for your platform: `*.bat` for Windows, `*.app` for macOS, and `*.sh` for Linux. This will open a Squeak image like the one below.

<p align="center">
  <img width="100%" src="./img/uzi_squeak.png?raw=true">
</p>

The control panel is an internal tool that allows us to connect to an Arduino board, monitor its state, program it in UziScript, and debug it using our experimental debugger. However, you will mostly need it to start/stop the UziServer. This server will allow you to use the web tools. The server should already be started (if not you just need to click the "Start server" button), and if you point your browser to [http://localhost:8080](http://localhost:8080) you should see a website like the one below.

<p align="center">
  <img width="100%" src="./img/uzi_webindex.png?raw=true">
</p>

### Text editor

This simple editor will allow you to write programs using the [UziScript language](#description-of-the-language). The left bar will allow you to choose a serial port, connect to your board, verify your program, run it on your board, and finally install it on the board's memory so that it runs the next time the board is powered.

<p align="center">
  <img width="100%" src="./img/uzi_texteditor.png?raw=true">
</p>

### Visual editor

Since learning a new syntax can be difficult (especially for beginners) we have developed a block-based language. In order to help in the transition to text-based languages we have also implemented automatic code generation from this visual language to the UziScript syntax (and viceversa). You can see this in action by opening this editor alongside the text editor. If also you activate the "Run automatically" checkbox all the changes made to the program will be sent immediately to the board (if connected).

<p align="center">
  <img width="100%" src="./img/uzi_visualeditor.png?raw=true">
</p>

### Monitor

This tool allows you to monitor the value over time of any pin on your board or any global variable on your program.

<p align="center">
  <img width="100%" src="./img/uzi_monitor.png?raw=true">
</p>

## Contributing

If you're interested in contributing to this project see [CONTRIBUTING.md](/docs/CONTRIBUTING.md).

## Description of the language

UziScript syntax is based on C, which is familiar to most programmers including Arduino developers. The `task` keyword has been added to represent behavior that can be executed periodically at a configurable rate. For example, the following code will declare a task that will toggle the LED on pin 13 every second.

```
task blink() running 1/s { toggle(D13); }
```

UziScript does not require any type declarations, so to distinguish a function from a procedure two new keywords are introduced: `func` and `proc`.

```
func isOn(pin) { return read(pin) > 0.5; }

proc toggle(pin) {
  if isOn(pin) { turnOff(pin); }
  else { turnOn(pin); }
}
```

A program can have any number of tasks, and each task can be defined with a different interval as well as a different starting state, which can be either `running` or `stopped`. If no starting state is specified the task will run just once and then it will stop. This is especially useful to initialize variables and can be used as a substitute to the Arduino `setup()` function.

The execution of each task at the correct time is performed automatically by the virtual machine but the user can invoke certain primitives to start, stop, pause, or resume a given task. Each task execution is independent, it has its own stack, and it shares memory with other tasks through specially defined global variables. This design allows users to write sequential programs in Arduino’s usual style and make them run concurrently without being concerned about the processor scheduling.

Primitive instructions like `delay()` are provided to allow the user to block the executing task for a given amount of time without affecting the rest. Arduino related primitives are also included but in some cases their names and behavior were modified to offer a simplified interface with the hardware. For example, the Arduino `digitalRead()` and `analogRead()` functions are merged into a single UziScript function called `read()`, which accepts a pin number and returns a floating-point value. If the pin is digital the resulting value can either be 0 or 1 but if the pin is analog the function will normalize its value between 0 and 1. An equivalent implementation of the `write()` procedure is also provided. We believe these small design details make the language more accessible to beginners by providing a concise (and consistent) interface to the hardware.

UziScript also supports external libraries that can extend the primitive functionality of the language. You can find examples [here](/uzi/libraries).

The UziScript grammar, written as a PEG, can be found [here](/docs/uzi.pegjs). Since the actual parser is written in Smalltalk using PetitParser, the grammar provided here is for illustrative purposes only.

## Implementation

### Firmware

The UziScript firmware is a regular Arduino sketch written in C++ that can be uploaded using the Arduino IDE.

<p align="center">
  <img width="75%" src="./img/uzi_architecture.png?raw=true">
</p>

Internally, the firmware implements a stack-based high-level language virtual machine that uses a decode and dispatch bytecode interpreter to execute UziScript programs. This implementation was chosen mainly because of its simplicity. Since the purpose of this language is educational, performance is not currently considered a high priority.

For now, the stack and global variables are the only available memory to the user program. There is no heap or dynamic memory allocation implemented yet. This allows for simpler virtual machine code and compact object code. Almost all the instructions can be encoded using one byte for both the opcode and its arguments and just a few special instructions (such as branches) require an extra byte.

Apart from the virtual machine, the firmware includes a monitor program that allows to interact with a computer through the serial port. Periodically, this monitor program will send the status of the Arduino and receive commands, allowing the host computer to fully control the virtual machine.

By having these two programs running on the Arduino we can provide an interactive programming experience with a short feedback loop without sacrificing autonomy. Moreover, the monitor program permits the implementation of debugging tools that allow the user to stop the execution of any task, inspect the value of all the variables, explore the call stack, and execute instructions step by step. These kind of debugging capabilities, which we consider to be essential in an educational context, are only available on the Arduino platform using either extra hardware or the more advanced Arduino Zero.

### Task scheduler

As most Arduino boards contain a single microcontroller, they can only execute one thread at a time. This means all the tasks defined in the program must share a single processor. The virtual machine, apart from executing the program instructions, is responsible for handling the task scheduling. It decides which task gets executed and when to preemptively interrupt it.

The scheduling strategy is simple, the virtual machine will cycle through the task list scheduling the tasks whose time to run is reached. It will then execute each instruction until a blocking operation occurs, in which case it will interrupt the current task and start executing the next one. Each task will store its execution context (stack, program counter, and frame pointer) in order to be able to later resume the execution from the point where it was interrupted. Some of the blocking operations that will force a context switch include: the `yield` instruction, all the `delay()` procedures, a reverse jump, writing to the serial port when the buffer is full, and reading from the serial port when the buffer is empty.

Below is a simplified example of one of the possible ways the scheduler could interleave the execution of the instructions between two tasks.

<p align="center">
  <img src="./img/uzi_scheduling.png?raw=true">
</p>

This strategy has the advantage of being simple (which is important, considering this is an educational project) and it guarantees that all of the tasks will have a chance to run. However, it does not provide any real-time guarantees. In the future, we might improve the implementation by incorporating a real-time scheduler.

### Compiler

On the computer side we implemented a small set of tools that allow to edit, compile, debug, and transmit the programs to the Arduino board through the serial port. All these tools were developed using [Squeak](https://squeak.org/), an open source version of [Smalltalk](https://en.wikipedia.org/wiki/Smalltalk). We are also working on a web-based version of the tools that will allow to write UziScript programs using a visual interface supported by [Blockly](https://developers.google.com/blockly/).

The compilation process transforms UziScript programs into bytecode suitable for the virtual machine to execute. Below is an example of the generated bytecode from a simple UziScript program. As you can see, the notation used to represent the bytecodes is also valid Smalltalk code that, when evaluated, will produce an instance of the program.

<p align="center">
  <img src="./img/Uzi_bytecode.png?raw=true">
</p>

You can find a detailed description of the instruction set [here](/docs/ISA.md).
