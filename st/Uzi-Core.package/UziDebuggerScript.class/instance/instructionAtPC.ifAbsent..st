accessing
instructionAtPC: pc ifAbsent: aBlock
	^ self instructions
		detect: [:instr | instr interval includes: pc]
		ifNone: aBlock