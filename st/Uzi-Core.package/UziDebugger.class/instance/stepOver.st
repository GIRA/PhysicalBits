actions
stepOver
	self nextStatements ifNotEmpty: [:nodes || breakpoints |
		self clearDebuggerBreakpoints.
		breakpoints := nodes
			collect: [:each | self pcForNode: each]
			thenSelect: [:each | each notNil].
		self setDebuggerBreakpoints: breakpoints].
	protocol sendContinue