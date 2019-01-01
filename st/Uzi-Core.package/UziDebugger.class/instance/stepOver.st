actions
stepOver
	self clearDebuggerBreakpoints.
	self setDebuggerBreakpoints: (((debuggerProgram
		instructionAtPC: pc)
		ifNil: [#()]
		ifNotNil: [:instr | instr stepOverOn: self])
			collect: [:each | each pc]).
	self sendBreakpoints; sendContinue