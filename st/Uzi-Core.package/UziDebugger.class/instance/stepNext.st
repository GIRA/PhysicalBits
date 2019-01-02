actions
stepNext
	self setDebuggerBreakpoints: self program allPCs;
		sendBreakpoints;
		sendContinue