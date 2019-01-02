accessing
instructionAtPC: pc
	^ pc ifNotNil: [program instructionAtPC: pc]