actions
stepOut
	self clearDebuggerBreakpoints.
	self setDebuggerBreakpoints: self stepOutBreakpoints.
	self sendBreakpoints; sendContinue