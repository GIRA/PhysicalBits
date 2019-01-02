actions
stepIntoBreakpoints
	^ (program instructionAtPC: pc)
		ifNil: [#()]
		ifNotNil: [:instr | instr stepIntoOn: self]