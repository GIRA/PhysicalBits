accessing
currentInstruction
	^ (debugger instructionAtPC: (stackFrame 
		ifNil: [debugger pc]
		ifNotNil: [:sf | sf pc]))