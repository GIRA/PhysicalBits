accessing
instructionAtPC: pc
	^ pc ifNotNil: [self program instructionAtPC: pc]