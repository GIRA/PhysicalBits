accessing
currentInstruction
	^ stackFrame ifNotNil: [(debugger instructionAtPC: stackFrame pc)]