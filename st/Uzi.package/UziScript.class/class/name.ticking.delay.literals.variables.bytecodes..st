instance creation
name: aSymbol
ticking: aBoolean
delay: delayMs
literals: literalsArray
variables: variablesArray
bytecodes: bytecodesArray
	^ self basicNew 
		initializeWithName: aSymbol 
		ticking: aBoolean
		delay: delayMs
		literals: literalsArray
		variables: variablesArray
		bytecodes: bytecodesArray