actions
stepOver
	self clearDebuggerBreakpoints.
	self nextStatements ifNotEmpty: [:nodes || breakpoints |
		breakpoints := nodes
			collect: [:each | self pcForNode: each]
			thenSelect: [:each | each notNil].
		self setDebuggerBreakpoints: breakpoints].
	self sendBreakpoints; sendContinue