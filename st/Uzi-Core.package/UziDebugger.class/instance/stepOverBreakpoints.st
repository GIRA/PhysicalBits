actions
stepOverBreakpoints
	^ (self program instructionAtPC: pc)
		ifNil: [#()]
		ifNotNil: [:instr | instr stepOverOn: self]