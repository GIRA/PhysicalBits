actions
stepNext
	self setDebuggerBreakpoints: self allPCs;
		sendBreakpoints;
		sendContinue