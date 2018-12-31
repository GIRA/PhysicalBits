actions
stepNext
	debuggerBreakpoints addAll: (0 to: program instructions size - 1).
	protocol setAllBreakpoints; sendContinue