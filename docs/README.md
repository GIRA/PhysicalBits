# UziScript Documentation

UziScript is a concurrent programming language and virtual machine for educational robotics. The current implementation only supports Arduino as hardware platform but we plan to support other boards in the future.

## Table of contents

* [Motivation](#motivation)
  * [Limited support for concurrency](#limited-support-for-concurrency)
  * [Interactivity vs Autonomy](#interactivity-vs-autonomy)
  * [Lack of debugging facilities](#lack-of-debugging-facilities)
  * [Wrong abstractions](#wrong-abstractions)
* [Proposed Solution](#proposed-solution)
* [Download and Installation](#download-and-installation)
* [Usage](#usage)
  * [Text editor](#text-editor)
  * [Visual editor](#visual-editor)
  * [Monitor](#monitor)
* [Contributing](#contributing)
  * [Getting started](#getting-started)
  * [Dependencies](#dependencies)
* [Description of the language](#description-of-the-language)
* [Implementation](#implementation)
  * [Firmware](#firmware)
  * [Task scheduler](#task-scheduler)
  * [Compiler](#compiler)

## Motivation

Arduino has become one of the most popular platforms for building electronic projects, especially among hobbyists, artists, designers, and people just starting with electronics. The Arduino IDE and software library provide an abstraction layer over the hardware details that makes it possible to build interesting projects without a complete understanding of more advanced microcontroller concepts such as interrupts, ports, registers, timers, and such. At the same time, this abstraction layer can be bypassed to access advanced features if the user needs them. These characteristics make the Arduino platform suitable for both beginners and experts.

However, the Arduino language (based on C++) is still too complex for some of the most inexperienced users, especially young children. For this reason, a lot of educational programming environments have been developed, mostly offering a visual programming language that allows users to start programming without learning a new syntax. Some of these tools have been very successful but, in our experience, most of them suffer from one (or several) of the following issues.

### Limited support for concurrency

There is one aspect in which the Arduino language lacks proper abstractions: concurrency. For all but the simplest projects, the `setup()` and `loop()` [program structure](http://playground.arduino.cc/ArduinoNotebookTraduccion/Structure) proposed by Arduino is not expressive enough. Even moderately complex problems require some sort of simultaneous task execution.

Furthermore, most educational robotics projects require the implementation of a device that performs two or more simultaneous tasks. This poses a limitation on the type of educational projects that can be carried out, especially if the teaching subject is not robotics or programming itself.

### Interactivity vs Autonomy

ACAACA problema del modo directo vs modo compilado. Capaz esta sección debería ir antes que la de concurrency, así mencionamos que los problemas de concurrencia también los tienen algunos de los lenguajes visuales que compilan a arduino (como minibloq)

### Lack of debugging facilities

ACAACA Mencionar que sin hardware especial no se puede debugguear. Que estás limitado a hacer tracing por el serie. También tenés una extensión del visual studio que te instrumenta el código para poder meter breakpoints pero anda como el orto.

### Wrong abstractions

ACAACA no se si "wrong" es la forma de describirlo. Capaz "confusing". La idea es plantear acá que la API de arduino es confusa en varios sentidos (sobre todo en los métodos para leer/escribir los pines). No se si es un punto suficientemente fuerte como para incluirlo.

## Proposed Solution

We propose the implementation of a concurrent programming language supported by a virtual machine running on the Arduino. We call this language UziScript and we expect it to become a suitable compilation target for visual programming environments such as [Physical Etoys](http://tecnodacta.com.ar/gira/projects/physical-etoys), [Scratch for Arduino](http://s4a.cat), and [Ardublock](http://blog.ardublock.com/), among others.

Given that the main purpose of this programming language is educational, it was designed based on the following principles:
* __Simplicity__: It should be easy to reason about the virtual machine and understand how it performs its job.
* __Abstraction__: the language should provide high-level functions that hide away some of the details regarding both beginner and advanced microcontroller concepts (such as timers, interruptions, concurrency, pin modes, and such). These concepts can later be introduced at a pace compatible with the needs of the student.
* __Monitoring__: It should be possible to monitor the state of the board while it is connected to the computer.
* __Autonomy__: The programs must be able to run without a computer connected to the board.
* __Debugging__: the toolchain must provide mechanisms for error handling and step by step code execution. Without debugging tools, the process of fixing bugs can be frustrating for an inexperienced user.

## Download and Installation

ACAACA subir el zip y acá meter el link.

## Usage

After unzipping the package you'll find two folders: "UziFirmware" and "Tools". 

For every board you want to use with UziScript you'll first need to upload the firmware. This is a very simple procedure, since the UziScript firmware is just an Arduino sketch you can use the Arduino IDE as you would with any other sketch. For more detailed instructions on how to upload sketches, see [here](https://www.arduino.cc/en/Guide/Environment).

Once you have uploaded the firmware to your board you'll need to open the "Tools" folder and click on the appropriate file for your platform: `*.bat` for Windows, `*.app` for macOS, and `*.sh` for Linux. This will open a Squeak image like the one below.

<p align="center">
  <img width="100%" src="./img/uzi_squeak.png?raw=true">
</p>

The control panel is an internal tool that allows us to connect to an Arduino board, monitor its state, program it in UziScript, and debug it using our experimental debugger. However, you will mostly need it to start/stop the UziServer. This server will allow you to use the web tools. After starting the server (by clicking the "Start server" button), point your browser to [http://localhost:8080](http://localhost:8080) and you should see a website like the one below.

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

ACAACA descripción de la organización del repositorio, qué hay en cada carpeta, etc.

### Getting started

Before you can start contributing to UziScript, you'll need to clone this repository and make sure you also recursively clone the submodules (this is *very* important).

#### Firmware

For the firmware, since it is a simple Arduino sketch, you only need the Arduino IDE. However, to make development easier we also use Visual Studio 2017 with a very simple Arduino simulator we developed for this project. The simulator is extremely limited so it's not exactly the same as compiling for the Arduino but it makes things a lot easier especially when it comes to debugging and unit testing. The source code for the Uzi firmware can be found here: [/c++/UziFirmware/UziFirmware.ino](/c++/UziFirmware/UziFirmware.ino). If you want to use the Visual Studio IDE you can find the solution here: [/c++/Simulator/Simulator.sln](/c++/Simulator/).

<p align="center">
  <img width="100%" src="./img/uzi_simulator.png?raw=true">
</p>

#### Compilation tools

All the compilation tools are written in [Squeak Smalltalk](http://squeak.org/). To load them into your image, open up a Workspace and evaluate the following script. Make sure you have [filetree](https://github.com/dalehenrich/filetree) installed, otherwise the script will fail. It will ask you the path to the root of the current repository and it will then load all the necessary packages.
```smalltalk
git := FileDirectory on: (UIManager default 
	request: 'Path to git repository?' 
	initialAnswer: (gitPath ifNil: [FileDirectory default pathName])).
uzi := MCFileTreeRepository directory: git / 'st'.
rest := MCFileTreeRepository directory: git / 'st' / 'REST' / 'st'.
MCRepositoryGroup default addRepository: uzi.
MCRepositoryGroup default addRepository: rest.
load := [:ass || repo pckgName versionName version |
	repo := ass key.
	pckgName := ass value.
	versionName := repo allVersionNames 
		detect: [:name | name beginsWith: pckgName].
	version := repo versionNamed: versionName.
	version load].
{
	uzi -> 'PetitParser'.
	rest -> 'REST'.
	uzi -> 'Uzi-Core'.
	uzi -> 'Uzi-EEPROM'.
	uzi -> 'Uzi-Etoys'
} do: load.
(Smalltalk at: #Uzi) perform: #defaultDirectory: with: git.
```

Once the script has finished installing everything, you can open the control panel by evaluating:
```smalltalk
UziProtocolMorph new openInHand.
```

#### Web tools

All the web tools are written in plain html and javascript. You'll find the source code in here: [/web](/web).

#### Dependencies

UziParser is built using [PetitParser](http://scg.unibe.ch/research/helvetia/petitparser) by Lukas Renggli. 
UziServer uses the [REST package](https://github.com/RichoM/REST), which in turn uses [WebClient](http://www.squeaksource.com/WebClient/) by Andreas Raab. The above script should take care of loading everything but if you find any problem, please let me know.

In the case of the web tools, I decided to locally host all the dependencies (bootstrap, jquery, blockly, etc.). This way the tools can be used without an internet connection.

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
