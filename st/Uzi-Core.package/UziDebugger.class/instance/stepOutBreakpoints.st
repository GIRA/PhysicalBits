actions
stepOutBreakpoints
	^ (self program instructionAtPC: pc)
		ifNil: [#()]
		ifNotNil: [:instr | instr stepOutOn: self]