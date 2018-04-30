Uzi
===

UziParser is built using PetitParser (http://scg.unibe.ch/research/helvetia/petitparser). 
UziServer uses the REST package (https://github.com/RichoM/REST), which in turn uses WebClient (http://www.squeaksource.com/WebClient/).

To load the compilation tools make sure you have a Squeak image with Filetree and evaluate the following script:
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
