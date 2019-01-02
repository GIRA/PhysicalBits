actions
stepIntoBreakpoints
	^ (self program instructionAtPC: pc)
		ifNil: [#()]
		ifNotNil: [:instr | instr stepIntoOn: self]