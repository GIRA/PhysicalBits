initialization
initializeWithProgram: anUziProgramBuilder 
scriptName: aString 
ticking: aBoolean 
delay: anInteger 
argumentNames: argumentNames

	program := anUziProgramBuilder.
	scriptName := aString.
	ticking := aBoolean.
	delay := anInteger.
	args := argumentNames collect: [:each | UziVariable named: each].
	self initialize