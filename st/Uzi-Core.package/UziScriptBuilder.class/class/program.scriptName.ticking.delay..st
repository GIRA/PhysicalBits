instance creation
program: anUziProgramBuilder 
scriptName: scriptName 
ticking: isTicking 
delay: delayMs

	^ self basicNew 
		initializeWithProgram: anUziProgramBuilder
		scriptName: scriptName 
		ticking: isTicking 
		delay: delayMs