actions
stepIntoBreakpoints
	^ (debuggerProgram instructionAtPC: pc)
		ifNil: [#()]
		ifNotNil: [:instr | instr stepIntoOn: self]