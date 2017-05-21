accessing
currentInstruction
	^ pc ifNotNil: [self instructionAtPC: pc]