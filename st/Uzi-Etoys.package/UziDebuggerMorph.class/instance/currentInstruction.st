accessing
currentInstruction
	^ (debugger instructionAtPC: (stackFrame 
		ifNil: [debugger pc]
		ifNotNil: [stackFrame pc]))