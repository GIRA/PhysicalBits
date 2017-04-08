instance creation
name: aSymbol
ticking: aBoolean
delay: delayMs
locals: localsArray
instructions: instructions
	^ self basicNew 
		initializeWithName: aSymbol 
		ticking: aBoolean
		delay: delayMs
		locals: localsArray
		instructions: instructions asArray