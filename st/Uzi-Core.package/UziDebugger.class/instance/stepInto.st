actions
stepInto
	self clearDebuggerBreakpoints.
	self allStatements ifNotEmpty: [:nodes || breakpoints |
		breakpoints := nodes
			collect: [:each | self pcForNode: each]
			thenSelect: [:each | each notNil].
		self setDebuggerBreakpoints: breakpoints].
	self sendBreakpoints; sendContinue