actions
stepOverBreakpoints
	^ (program instructionAtPC: pc)
		ifNil: [#()]
		ifNotNil: [:instr | instr stepOverOn: self]