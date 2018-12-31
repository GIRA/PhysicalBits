actions
stepInto
	"
	Clear debugger breakpoints
	Set debugger breakpoint on all statement instructions
	Continue
	"
	self allStatements ifNotEmpty: [:nodes || breakpoints |
		self clearDebuggerBreakpoints.
		breakpoints := nodes
			collect: [:each | self pcForNode: each]
			thenSelect: [:each | each notNil].
		self setDebuggerBreakpoints: breakpoints].
	protocol sendContinue