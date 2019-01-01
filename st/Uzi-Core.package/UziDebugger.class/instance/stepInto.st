actions
stepInto
	self clearDebuggerBreakpoints.
	self allStatements ifNotEmpty: [:stmts || breakpoints |
		breakpoints := stmts
			collect: [:each | self pcForInstruction: each]
			thenSelect: [:each | each notNil].
		self setDebuggerBreakpoints: breakpoints].
	self sendBreakpoints; sendContinue