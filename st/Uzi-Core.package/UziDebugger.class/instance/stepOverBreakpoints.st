actions
stepOverBreakpoints
	^ (((debuggerProgram
		instructionAtPC: pc)
		ifNil: [#()]
		ifNotNil: [:instr | instr stepOverOn: self])
			collect: [:each | each breakpointPC])