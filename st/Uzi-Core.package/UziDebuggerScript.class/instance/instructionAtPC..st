accessing
instructionAtPC: pc
	^ self instructions
		detect: [:instr | instr interval includes: pc]
		ifNone: [nil]