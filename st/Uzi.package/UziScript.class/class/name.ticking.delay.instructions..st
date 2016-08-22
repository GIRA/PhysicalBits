instance creation
name: aSymbol
ticking: aBoolean
delay: delayMs
instructions: instructions
	^ self basicNew 
		initializeWithName: aSymbol 
		ticking: aBoolean
		delay: delayMs
		instructions: instructions asArray