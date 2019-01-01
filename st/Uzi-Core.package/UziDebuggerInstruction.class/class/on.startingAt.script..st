instance creation
on: aCollectionOfUziInstructions startingAt: startPC script: anUziDebuggerScript
	^ self basicNew
		initializeWithInstructions: aCollectionOfUziInstructions 
		start: startPC
		script: anUziDebuggerScript