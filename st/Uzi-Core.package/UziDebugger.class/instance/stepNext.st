actions
stepNext
	self setDebuggerBreakpoints: program allPCs;
		sendBreakpoints;
		sendContinue