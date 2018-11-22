Uzi
===

UziScript is a concurrent programming language and virtual machine for educational robotics. The current implementation only supports Arduino as hardware platform.

## Table of contents

* [Motivation](#motivation)
* [Proposed Solution](#proposed-solution)
* [Documentation](#documentation)
* [Installation](#installation)
* [Dependencies](#dependencies)

## Motivation

Arduino has become one of the most popular platforms for building electronic projects, especially among hobbyists, artists, designers, and people just starting with electronics. The Arduino IDE and software library provide an abstraction layer over the hardware details that makes it possible to build interesting projects without a complete understanding of more advanced microcontroller concepts such as interrupts, ports, registers, timers, and such. At the same time, this abstraction layer can be bypassed to access advanced features if the user needs them. These characteristics make the Arduino platform suitable for both beginners and experts.

However, there is one aspect in which the Arduino language lacks proper abstractions: concurrency. For all but the simplest projects, the `setup()` and `loop()` [program structure](http://playground.arduino.cc/ArduinoNotebookTraduccion/Structure) proposed by Arduino is not expressive enough. Even moderately complex problems require some sort of simultaneous task execution.

Furthermore, most educational robotics projects require the implementation of a device that performs two or more simultaneous tasks. This poses a limitation on the type of educational projects that can be carried out, especially if the teaching subject is not robotics or programming itself.

## Proposed Solution

We propose the implementation of a concurrent programming language supported by a virtual machine running on the Arduino. We call this language UziScript and we expect it to become a suitable compilation target for visual programming environments such as [Physical Etoys](http://tecnodacta.com.ar/gira/projects/physical-etoys), [Scratch for Arduino](http://s4a.cat), and [Ardublock](http://blog.ardublock.com/), among others.

Given that the main purpose of this programming language is educational, it was designed based on the following principles:
* __Simplicity__: It should be easy to reason about the virtual machine and understand how it performs its job.
* __Abstraction__: the language should provide high-level functions that hide away some of the details regarding both beginner and advanced microcontroller concepts (such as timers, interruptions, concurrency, pin modes, and such). These concepts can later be introduced at a pace compatible with the needs of the student.
* __Monitoring__: It should be possible to monitor the state of the board while it is connected to the computer.
* __Autonomy__: The programs must be able to run without a computer connected to the board.
* __Debugging__: the toolchain must provide mechanisms for error handling and step by step code execution. Without debugging tools, the process of fixing bugs can be frustrating for an inexperienced user.

## Documentation

You can find more detailed documentation [here](/docs).

## Installation

The Uzi firmware is a simple sketch that you can upload to your board using the standard Arduino IDE. You can find the source code here: [/c++/UziFirmware/UziFirmware.ino](/c++/UziFirmware/UziFirmware.ino)

The compilation tools are written in [Squeak Smalltalk](http://squeak.org/). To load them into your image, open up a Workspace and evaluate the following script. Make sure you have [filetree](https://github.com/dalehenrich/filetree) installed, otherwise the script will fail. It will ask you the path to the root of the current repository (which you should have cloned in your file system) and it will then load all the necessary packages.
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
	uzi -> 'WebClient'.
	rest -> 'REST'.
	uzi -> 'Uzi-Core'.
	uzi -> 'Uzi-EEPROM'.
	uzi -> 'Uzi-Etoys'
} do: load.
(Smalltalk at: #Uzi) perform: #defaultDirectory: with: git.
```

You can open the tools by evaluating:
```smalltalk
UziProtocolMorph new openInHand.
```

## Dependencies

UziParser is built using [PetitParser](http://scg.unibe.ch/research/helvetia/petitparser). 
UziServer uses the [REST package](https://github.com/RichoM/REST), which in turn uses [WebClient](http://www.squeaksource.com/WebClient/). The above script should take care of loading everything but if you find any problem, please let me know.
