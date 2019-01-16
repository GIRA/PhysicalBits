## Contributing

First of all, thank you for your interest in the project. This repository has the following structure:

    .
    ├── c++/
    │   ├── Simulator/           # Arduino simulator (useful for testing without an Arduino board)
    │   └── UziFirmware/         # UziScript firmware (an Arduino sketch)
    ├── docs/                    # Documentation files (you're here)
    ├── st/                      # Smalltalk source (including all the compilation tools and the web server)
    ├── uzi/
    │   ├── libraries/           # UziScript libraries
    │   ├── tests/               # Tests files
    │   └── uzi.xml              # Notepad++ syntax highlighter 
    ├── web/                     # Web tools
    ├── LICENSE
    └── README.md

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
	uzi -> 'Uzi-Morphic'.
	uzi -> 'Uzi-Etoys'
} do: load.
(Smalltalk at: #Uzi) perform: #defaultDirectory: with: git.
```

Once the script has finished installing everything, you can open the control panel by evaluating:
```smalltalk
UziControlPanelMorph new openInHand.
```

#### Web tools

All the web tools are written in plain html and javascript. You'll find the source code in here: [/web](/web).

#### Dependencies

UziParser is built using [PetitParser](http://scg.unibe.ch/research/helvetia/petitparser) by Lukas Renggli. 
UziServer uses the [REST package](https://github.com/RichoM/REST), which in turn uses [WebClient](http://www.squeaksource.com/WebClient/) by Andreas Raab. The above script should take care of loading everything but if you find any problem, please let me know.

In the case of the web tools, I decided to locally host all the dependencies (bootstrap, jquery, blockly, etc.). This way the tools can be used without an internet connection.
