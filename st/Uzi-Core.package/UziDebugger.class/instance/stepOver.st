actions
stepOver
	self clearDebuggerBreakpoints.
	self nextInstructionsForStepOver ifNotEmpty: [:next || breakpoints |
		breakpoints := next
			collect: [:each | self pcForInstruction: each]
			thenSelect: [:each | each notNil].
		self setDebuggerBreakpoints: breakpoints].
	self sendBreakpoints; sendContinue