Uzi
===

UziScript is a concurrent programming language and virtual machine for educational robotics. The current implementation only supports Arduino as hardware platform.

## Installation

The Uzi firmware is a simple sketch that you can upload to your board using the standard Arduino IDE. You can find the source code here: [/c++/UziFirmware/UziFirmware.ino](/c++/UziFirmware/UziFirmware.ino)

The compilation tools are written in [Squeak Smalltalk](http://squeak.org/). To load them into your image, open up a Workspace and evaluate the following script. Make sure you have filetree (https://github.com/dalehenrich/filetree) installed, otherwise the script will fail. It will ask you the path to the root of the current repository (which you should have cloned in your file system) and it will then load all the necessary packages.
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
(Smalltalk at: #Uzi) gitDirectory: git.
```

You can open the tools by evaluating:
```smalltalk
UziProtocolMorph new openInHand.
```

## Dependencies

UziParser is built using PetitParser (http://scg.unibe.ch/research/helvetia/petitparser). 
UziServer uses the REST package (https://github.com/RichoM/REST), which in turn uses WebClient (http://www.squeaksource.com/WebClient/). The above script should take care of loading everything but if you find any problem, please let me know.