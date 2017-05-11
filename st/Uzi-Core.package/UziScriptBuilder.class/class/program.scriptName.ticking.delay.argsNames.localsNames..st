instance creation
program: anUziProgramBuilder 
scriptName: scriptName 
ticking: isTicking 
delay: delayMs
argsNames: argsNames
localsNames: localsNames

	^ self basicNew 
		initializeWithProgram: anUziProgramBuilder
		scriptName: scriptName 
		ticking: isTicking 
		delay: delayMs
		argumentNames: argsNames
		localNames: localsNames