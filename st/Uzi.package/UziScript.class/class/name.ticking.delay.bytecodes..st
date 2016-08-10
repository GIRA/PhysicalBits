instance creation
name: aSymbol
ticking: aBoolean
delay: delayMs
bytecodes: bytecodesArray
	^ self basicNew 
		initializeWithName: aSymbol 
		ticking: aBoolean
		delay: delayMs
		bytecodes: bytecodesArray asArray