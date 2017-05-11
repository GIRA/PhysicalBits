initialization
initializeWithProgram: anUziProgramBuilder 
scriptName: aString 
ticking: aBoolean 
delay: anInteger 
argumentNames: argumentNames
localNames: localNames

	program := anUziProgramBuilder.
	scriptName := aString.
	ticking := aBoolean.
	delay := anInteger.
	args := argumentNames collect: [:each | UziVariable named: each].
	locals := localNames collect: [:each | UziVariable named: each].
	self initialize